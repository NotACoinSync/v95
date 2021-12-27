package net.server.channel.handlers;

import client.*;
import client.autoban.AutobanFactory;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.player.SecondaryStat;
import constants.EquipSlot;
import constants.GameConstants;
import constants.ItemConstants;
import constants.skills.*;
import java.awt.Point;
import java.util.*;
import net.AbstractMaplePacketHandler;
import net.server.handlers.DamageMeter.AddDamageInfo;
import server.ItemData;
import server.ItemData.SkillData;
import server.ItemInformationProvider;
import server.MapleStatEffect;
import server.TimerManager;
import server.life.*;
import server.maps.MapleMap;
import server.partyquest.Pyramid;
import tools.Pair;
import tools.Randomizer;
import tools.data.input.LittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CField.MobPool;
import tools.packets.CField.userpool.UserRemote;
import tools.packets.WvsContext;

public abstract class AbstractDealDamageHandler extends AbstractMaplePacketHandler {

    public static class AttackInfo {

        public int numAttacked, numDamage, numAttackedAndDamage, skillID, skillLevel, AttackActionType, AttackAction, rangedirection, KeyDown, display, AttackTime, Phase, mesoCount, itemID;
        public int projectile;
        public Map<Integer, List<Pair<Integer, Boolean>>> allDamage;
        public boolean isHH = false, isTempest = false, ranged, magic, body;
        public int AttackSpeed = 4;
        public Point position = new Point();
        public Point attackerPosition = new Point();
        public List<Point> mobPositions = new ArrayList<>(), mobPositionPrev = new ArrayList<>();
        public List<Integer> mesos = new ArrayList<>();

        public MapleStatEffect getAttackEffect(MapleCharacter player, Skill theSkill) {
            Skill mySkill = theSkill;
            if (mySkill == null) {
                mySkill = SkillFactory.getSkill(GameConstants.getHiddenSkill(skillID));
            }
            int skillLevel = player.getSkillLevel(mySkill);
            if (mySkill.getId() % 10000000 == 1020) {
                if (player.getPartyQuest() instanceof Pyramid) {
                    if (((Pyramid) player.getPartyQuest()).useSkill()) {
                        skillLevel = 1;
                    }
                }
            }
            if (skillLevel == 0) {
                return null;
            }
            if (display > 80) { // Hmm
                if (!theSkill.getAction()) {
                    AutobanFactory.FAST_ATTACK.autoban(player, "WZ Edit; adding action to a skill: " + display);
                    return null;
                }
            }
            return mySkill.getEffect(skillLevel);
        }
    }

    // private int numRand = 11;// A number of random number for calculate damage
    protected synchronized void applyAttack(AttackInfo attack, final MapleCharacter player, int attackCount) {
        Skill skill = null;
        Skill theSkill = null;
        MapleStatEffect attackEffect = null;
        final int job = player.getJob().getId();
        if (player.isBanned()) {
            return;
        }
        if (!player.isAlive()) {
            return;
        }
        Item weapon = player.getInventory(MapleInventoryType.EQUIPPED).getItem((short) EquipSlot.WEAPON.getSlots()[0]);
        if (weapon == null && (!player.getJob().isA(MapleJob.PIRATE) && player.getJob().isA(MapleJob.THUNDERBREAKER1))) {
            AutobanFactory.PACKET_EDIT.alert(player, "Tried to attack without a weapon");
            return;
        }
        if (weapon instanceof Equip) {
            Equip eqp = (Equip) weapon;
            if (eqp.getDurability() == 0) {
                AutobanFactory.PACKET_EDIT.alert(player, "Tried to attack with a weapon on 0 durability");
                return;
            }
        }
        if (attack.skillID != 0) {
            skill = SkillFactory.getSkill(attack.skillID);
            theSkill = SkillFactory.getSkill(GameConstants.getHiddenSkill(attack.skillID)); // returns back the skill id if its not a hidden skill so we are gucci
            attackEffect = attack.getAttackEffect(player, theSkill);
            if (attackEffect == null) {
                player.getClient().announce(WvsContext.enableActions());
                return;
            }
            if (player.getMp() < attackEffect.getMpCon()) {
                AutobanFactory.MPCON.addPoint(player.getAutobanManager(), "Skill: " + attack.skillID + "; Player MP: " + player.getMp() + "; MP Needed: " + attackEffect.getMpCon());
            }
            if (attack.skillID != Cleric.Heal) {
                if (player.isAlive()) {
                    if (attack.skillID == NightWalker.POISON_BOMB || attack.position != null) { // Poison Bomb
                        attackEffect.applyTo(player, new Point(attack.position.x, attack.position.y));
                    } else if (attack.skillID != Aran.BODY_PRESSURE) {
                        attackEffect.applyTo(player);
                    }
                } else {
                    player.getClient().announce(WvsContext.enableActions());
                }
            }
            int mobCount = attackEffect.getMobCount();
            if (attack.skillID == DawnWarrior.FINAL_ATTACK || attack.skillID == Page.FINAL_ATTACK_BW || attack.skillID == Page.FINAL_ATTACK_SWORD || attack.skillID == Fighter.FINAL_ATTACK_SWORD || attack.skillID == Fighter.FINAL_ATTACK_AXE || attack.skillID == Spearman.FINAL_ATTACK_SPEAR || attack.skillID == Spearman.FINAL_ATTACK_POLEARM || attack.skillID == WindArcher.FINAL_ATTACK || attack.skillID == DawnWarrior.FINAL_ATTACK || attack.skillID == Hunter.FINAL_ATTACK || attack.skillID == Crossbowman.FINAL_ATTACK) {
                mobCount = 15;// :(
            }
            if (attack.skillID == Aran.HIDDEN_FULL_DOUBLE || attack.skillID == Aran.HIDDEN_FULL_TRIPLE || attack.skillID == Aran.HIDDEN_OVER_DOUBLE || attack.skillID == Aran.HIDDEN_OVER_TRIPLE) {
                mobCount = 12;
            }
            if (attack.numAttacked > mobCount) {
                AutobanFactory.MOB_COUNT.autoban(player, "Skill: " + attack.skillID + "; Count: " + attack.numAttacked + " Max: " + attackEffect.getMobCount());
                return;
            }
        }
        int totDamage = 0;
        final MapleMap map = player.getMap();
        if (attack.skillID == ChiefBandit.MesoExplosion) {
            player.message("Meso Explosion has been blocked.");
            return;
        }
        for (Integer oned : attack.allDamage.keySet()) {
            final MapleMonster monster = map.getMonsterByOid(oned.intValue());
            if (monster != null) {
                int totDamageToOneMonster = 0;
                List<Pair<Integer, Boolean>> onedList = attack.allDamage.get(oned);
                for (Pair<Integer, Boolean> eachd : onedList) {// TODO: Crit boolean?
                    if (eachd.left < 0) {
                        eachd.left += Integer.MAX_VALUE;
                    }
                    totDamageToOneMonster += eachd.left;
                }
                totDamage += totDamageToOneMonster;
                player.checkMonsterAggro(monster);
                if (player.getBuffedValue(SecondaryStat.PickPocket) != null && (attack.skillID == 0 || attack.skillID == Rogue.DOUBLE_STAB || attack.skillID == Bandit.SAVAGE_BLOW || attack.skillID == ChiefBandit.ASSAULTER || attack.skillID == ChiefBandit.BAND_OF_THIEVES || attack.skillID == Shadower.ASSASSINATE || attack.skillID == Shadower.TAUNT || attack.skillID == Shadower.BOOMERANG_STEP)) {
                    Skill pickpocket = SkillFactory.getSkill(ChiefBandit.PICKPOCKET);
                    int delay = 0;
                    final int maxmeso = player.getBuffedValue(SecondaryStat.PickPocket).intValue();
                    for (Pair<Integer, Boolean> eachd : onedList) {// TODO: Crit boolean?
                        eachd.left += Integer.MAX_VALUE;
                        if (pickpocket.getEffect(player.getSkillLevel(pickpocket)).makeChanceResult()) {
                            final Integer eachdf;
                            if (eachd.left < 0) {
                                eachdf = eachd.left + Integer.MAX_VALUE;
                            } else {
                                eachdf = eachd.left;
                            }
                            TimerManager.getInstance().schedule("pickPocket", new Runnable() {

                                @Override
                                public void run() {
                                    player.getMap().spawnMesoDrop(Math.min((int) Math.max(((double) eachdf / (double) 20000) * (double) maxmeso, (double) 1), maxmeso), new Point((int) (monster.getPosition().getX() + Randomizer.nextInt(100) - 50), (int) (monster.getPosition().getY())), monster, player, true, (byte) 2);
                                }
                            }, delay);
                            delay += 100;
                        }
                    }
                } else if (attack.skillID == Marauder.ENERGY_DRAIN || attack.skillID == ThunderBreaker.ENERGY_DRAIN || attack.skillID == NightWalker.VAMPIRE || attack.skillID == Assassin.DRAIN) {
                    player.addHP(Math.min(monster.getMaxHp(), Math.min((int) ((double) totDamage * (double) SkillFactory.getSkill(attack.skillID).getEffect(player.getSkillLevel(SkillFactory.getSkill(attack.skillID))).getX() / 100.0), player.getMaxHp() / 2)));
                } else if (attack.skillID == Bandit.STEAL) {
                    Skill steal = SkillFactory.getSkill(Bandit.STEAL);
                    if (steal.getEffect(player.getSkillLevel(steal)).makeChanceResult()) {
                        List<MonsterDropEntry> toSteals = MapleMonsterInformationProvider.getInstance().retrieveDrop(monster.getId());
                        Collections.shuffle(toSteals);
                        int toSteal = toSteals.get(rand(0, (toSteals.size() - 1))).itemId;
                        ItemInformationProvider ii = ItemInformationProvider.getInstance();
                        Item item;
                        if (ItemConstants.getInventoryType(toSteal).equals(MapleInventoryType.EQUIP)) {
                            item = ii.randomizeStats((Equip) ii.getEquipById(toSteal));
                        } else {
                            item = new Item(toSteal, (byte) 0, (short) 1, -1);
                        }
                        player.getMap().spawnItemDrop(monster, player, item, monster.getPosition(), false, false);
                    }
                    monster.setItemStolen();
                } else if (attack.skillID == FPArchMage.FIRE_DEMON) {
                    monster.setTempEffectiveness(Element.ICE, ElementalEffectiveness.WEAK, SkillFactory.getSkill(FPArchMage.FIRE_DEMON).getEffect(player.getSkillLevel(SkillFactory.getSkill(FPArchMage.FIRE_DEMON))).getDuration() * 1000);
                } else if (attack.skillID == ILArchMage.ICE_DEMON) {
                    monster.setTempEffectiveness(Element.FIRE, ElementalEffectiveness.WEAK, SkillFactory.getSkill(ILArchMage.ICE_DEMON).getEffect(player.getSkillLevel(SkillFactory.getSkill(ILArchMage.ICE_DEMON))).getDuration() * 1000);
                } else if (attack.skillID == Outlaw.HOMING_BEACON || attack.skillID == Corsair.BULLSEYE) {
                    player.setMarkedMonster(monster.getObjectId());
                    player.announce(WvsContext.setTemporaryStat(player, 1, attack.skillID, Collections.singletonList(new Pair<>(SecondaryStat.GuidedBullet, new BuffDataHolder(0, 0, monster.getObjectId())))));
                }
                if (job == 2111 || job == 2112) {
                    if (player.getBuffedValue(SecondaryStat.WeaponCharge) != null) {
                        Skill snowCharge = SkillFactory.getSkill(Aran.SNOW_CHARGE);
                        if (totDamageToOneMonster > 0) {
                            // MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.Speed, snowCharge.getEffect(player.getSkillLevel(snowCharge)).getX()), snowCharge, null, false);
                            // monster.applyStatus(player, monsterStatusEffect, false, snowCharge.getEffect(player.getSkillLevel(snowCharge)).getY() * 1000);
                            MobStatData data = new MobStatData(MobStat.Speed, snowCharge.getEffect(player.getSkillLevel(snowCharge)).getX(), snowCharge.id, snowCharge.getEffect(player.getSkillLevel(snowCharge)).getY() * 1000);
                            monster.applyStatus(player, data, snowCharge, false, false);
                        }
                    }
                }
                if (player.getBuffedValue(SecondaryStat.HamString) != null) {
                    Skill hamstring = SkillFactory.getSkill(Bowmaster.HAMSTRING);
                    if (hamstring.getEffect(player.getSkillLevel(hamstring)).makeChanceResult()) {
                        // MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.Speed, hamstring.getEffect(player.getSkillLevel(hamstring)).getX()), hamstring, null, false);
                        // monster.applyStatus(player, monsterStatusEffect, false, hamstring.getEffect(player.getSkillLevel(hamstring)).getY() * 1000);
                        MobStatData data = new MobStatData(MobStat.Speed, hamstring.getEffect(player.getSkillLevel(hamstring)).getX(), hamstring.id, hamstring.getEffect(player.getSkillLevel(hamstring)).getY() * 1000);
                        monster.applyStatus(player, data, hamstring, false, false);
                    }
                }
                if (player.getBuffedValue(SecondaryStat.EvanSlow) != null) {
                    Skill slow = SkillFactory.getSkill(Evan.SLOW);
                    if (slow.getEffect(player.getSkillLevel(slow)).makeChanceResult()) {
                        // MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.Speed, slow.getEffect(player.getSkillLevel(slow)).getX()), slow, null, false);
                        // monster.applyStatus(player, monsterStatusEffect, false, slow.getEffect(player.getSkillLevel(slow)).getY() * 60 * 1000);
                        MobStatData data = new MobStatData(MobStat.Speed, slow.getEffect(player.getSkillLevel(slow)).getX(), slow.id, slow.getEffect(player.getSkillLevel(slow)).getY() * 60 * 1000);
                        monster.applyStatus(player, data, slow, false, false);
                    }
                }
                if (player.getBuffedValue(SecondaryStat.Blind) != null) {
                    Skill blind = SkillFactory.getSkill(Marksman.BLIND);
                    if (blind.getEffect(player.getSkillLevel(blind)).makeChanceResult()) {
                        MobStatData data = new MobStatData(MobStat.ACC, blind.getEffect(player.getSkillLevel(blind)).getX(), blind.id, blind.getEffect(player.getSkillLevel(blind)).getY() * 1000);
                        monster.applyStatus(player, data, blind, false, false);
                        // MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.ACC, blind.getEffect(player.getSkillLevel(blind)).getX()), blind, null, false);
                        // monster.applyStatus(player, monsterStatusEffect, false, blind.getEffect(player.getSkillLevel(blind)).getY() * 1000);
                    }
                }
                if (job == 121 || job == 122) {
                    for (int charge = 1211005; charge < 1211007; charge++) {
                        Skill chargeSkill = SkillFactory.getSkill(charge);
                        if (player.isBuffFrom(SecondaryStat.WeaponCharge, chargeSkill)) {
                            if (totDamageToOneMonster > 0) {
                                if (charge == WhiteKnight.BW_ICE_CHARGE || charge == WhiteKnight.SWORD_ICE_CHARGE) {
                                    monster.setTempEffectiveness(Element.ICE, ElementalEffectiveness.WEAK, chargeSkill.getEffect(player.getSkillLevel(chargeSkill)).getY() * 1000);
                                    break;
                                }
                                if (charge == WhiteKnight.BW_FIRE_CHARGE || charge == WhiteKnight.SWORD_FIRE_CHARGE) {
                                    monster.setTempEffectiveness(Element.FIRE, ElementalEffectiveness.WEAK, chargeSkill.getEffect(player.getSkillLevel(chargeSkill)).getY() * 1000);
                                    break;
                                }
                            }
                        }
                    }
                    if (job == 122) {
                        for (int charge = 1221003; charge < 1221004; charge++) {
                            Skill chargeSkill = SkillFactory.getSkill(charge);
                            if (player.isBuffFrom(SecondaryStat.WeaponCharge, chargeSkill)) {
                                if (totDamageToOneMonster > 0) {
                                    monster.setTempEffectiveness(Element.HOLY, ElementalEffectiveness.WEAK, chargeSkill.getEffect(player.getSkillLevel(chargeSkill)).getY() * 1000);
                                    break;
                                }
                            }
                        }
                    }
                } else if (player.getBuffedValue(SecondaryStat.ComboDrain) != null) {
                    if (player.getBuffedValue(SecondaryStat.ComboDrain) != null) {
                        skill = SkillFactory.getSkill(21100005);
                        player.setHp(player.getHp() + ((totDamage * skill.getEffect(player.getSkillLevel(skill)).getX()) / 100), true);
                        player.updateSingleStat(MapleStat.HP, player.getHp());
                    }
                } else if (job == 412 || job == 422 || job == 1411 || job == 1412 || job == MapleJob.BLADE_MASTER.getId()) {
                    int skillid = 0;
                    switch (player.getJob()) {
                        case BLADE_MASTER:
                            skillid = 4340001;
                            break;
                        case NIGHTLORD:
                            skillid = 4120005;
                            break;
                        case SHADOWER:
                            skillid = 4220005;
                            break;
                        case NIGHTWALKER3:
                        case NIGHTWALKER4:
                            skillid = 14110004;
                            break;
                        default:
                            skillid = 0;
                            break;
                    }
                    if (skillid != 0) {
                        Skill type = SkillFactory.getSkill(skillid);
                        if (player.getSkillLevel(type) > 0) {
                            MapleStatEffect venomEffect = type.getEffect(player.getSkillLevel(type));
                            for (int i = 0; i < attackCount; i++) {
                                if (venomEffect.makeChanceResult()) {
                                    if (monster.getVenomMulti() < 3) {
                                        monster.setVenomMulti((monster.getVenomMulti() + 1));
                                        MobStatData data = new MobStatData(MobStat.Poison, venomEffect.getDuration());
                                        data.rOption = skillid;
                                        monster.applyStatus(player, data, type, false, true);
                                    }
                                }
                            }
                        }
                    }
                } else if (job == 521 || job == 522) { // from what I can gather this is how it should work
                    if (!monster.isBoss() && attack.skillID == Outlaw.FLAME_THROWER) {
                        Skill type = SkillFactory.getSkill(Outlaw.FLAME_THROWER);
                        if (player.getSkillLevel(type) > 0) {
                            MapleStatEffect DoT = type.getEffect(player.getSkillLevel(type));
                            MobStatData data = new MobStatData(MobStat.Poison, DoT.getDuration());
                            data.rOption = Outlaw.FLAME_THROWER;
                            monster.applyStatus(player, data, type, true, false);
                        }
                    }
                } else if (job >= 311 && job <= 322) {
                    if (!monster.isBoss()) {
                        Skill mortalBlow;
                        if (job == 311 || job == 312) {
                            mortalBlow = SkillFactory.getSkill(Ranger.MORTAL_BLOW);
                        } else {
                            mortalBlow = SkillFactory.getSkill(Sniper.MORTAL_BLOW);
                        }
                        if (player.getSkillLevel(mortalBlow) > 0) {
                            MapleStatEffect mortal = mortalBlow.getEffect(player.getSkillLevel(mortalBlow));
                            if (monster.getHp() <= (monster.getStats().getHp() * mortal.getX()) / 100) {
                                if (Randomizer.rand(1, 100) <= mortal.getY()) {
                                    player.getMap().announce(MobPool.SpecialEffectBySkill(monster.getObjectId(), mortalBlow.getId(), player.getId(), mortalBlow.getDelay()));
                                    player.battleAnaylsis.addDamage(mortalBlow.id, monster.getHp());
                                    map.damageMonster(player, monster, monster.getHp());
                                }
                            }
                        }
                    }
                } else if (job >= 430 && job <= 434) {
                    if (attack.skillID == BladeLord.OWL_SPIRIT) {
                        Skill owlSpirit = SkillFactory.getSkill(BladeLord.OWL_SPIRIT);
                        if (player.getSkillLevel(owlSpirit) > 0) {
                            MapleStatEffect spirit = owlSpirit.getEffect(player.getSkillLevel(owlSpirit));
                            if (!monster.isBoss() && spirit.makeChanceResult()) {
                                // Don't think this skill has a special effect by skill
                                player.battleAnaylsis.addDamage(owlSpirit.id, monster.getHp());
                                map.damageMonster(player, monster, monster.getHp());
                                spirit.applyTo(player);
                            }
                        }
                    }
                }
                if (attack.skillID != 0) {
                    if (attackEffect.getFixDamage() != -1) {
                        if (totDamageToOneMonster != attackEffect.getFixDamage() && totDamageToOneMonster != 0) {
                            AutobanFactory.FIX_DAMAGE.autoban(player, "Hit: " + String.valueOf(totDamageToOneMonster) + " damage. Calc: " + attackEffect.getFixDamage());
                        }
                    }
                }
                if (totDamageToOneMonster > 0 && attackEffect != null && attackEffect.getMonsterStati().size() > 0) {
                    if (attackEffect.makeChanceResult()) {
                        monster.applyStatus(player, attackEffect.getMonsterStati(), theSkill, attackEffect.isPoison(), false);
                    }
                }
                if (attack.isHH && !monster.isBoss()) {
                    map.damageMonster(player, monster, monster.getHp() - 1);
                } else if (attack.isHH) {
                    int HHDmg = (player.calculateMaxBaseDamage(player.getTotalWatk()) * (SkillFactory.getSkill(Paladin.HEAVENS_HAMMER).getEffect(player.getSkillLevel(SkillFactory.getSkill(Paladin.HEAVENS_HAMMER))).getDamage() / 100));
                    int damage = (int) (Math.floor(Math.random() * (HHDmg / 5) + HHDmg * .8));
                    player.battleAnaylsis.addDamage(attack.skillID, damage);
                    map.damageMonster(player, monster, damage);
                } else if (attack.isTempest && !monster.isBoss()) {
                    player.battleAnaylsis.addDamage(attack.skillID, monster.getHp());
                    map.damageMonster(player, monster, monster.getHp());
                } else if (attack.isTempest) {
                    int TmpDmg = (player.calculateMaxBaseDamage(player.getTotalWatk()) * (SkillFactory.getSkill(Aran.COMBO_TEMPEST).getEffect(player.getSkillLevel(SkillFactory.getSkill(Aran.COMBO_TEMPEST))).getDamage() / 100));
                    int damage = (int) (Math.floor(Math.random() * (TmpDmg / 5) + TmpDmg * .8));
                    player.battleAnaylsis.addDamage(attack.skillID, damage);
                    map.damageMonster(player, monster, damage);
                } else {
                    player.battleAnaylsis.addDamage(attack.skillID, totDamageToOneMonster);
                    map.damageMonster(player, monster, totDamageToOneMonster);
                }
                if (monster.isBuffed(MobStat.PCounter)) {
                    for (MobStatData data : monster.getMobStats().values()) {
                        if (data.stat.equals(MobStat.PCounter)) {
                            player.addHP(-data.pCounter);
                            map.announce(player, UserRemote.Hit(player.getId(), 0, monster.getId(), data.pCounter, 0, 0, false, 0, true, monster.getObjectId(), 0, 0), true);
                        }
                    }
                }
                if (monster.isBuffed(MobStat.MCounter)) {
                    for (MobStatData data : monster.getMobStats().values()) {
                        if (data.stat.equals(MobStat.MCounter)) {
                            player.addHP(-data.mCounter);
                            map.announce(player, UserRemote.Hit(player.getId(), 0, monster.getId(), data.mCounter, 0, 0, false, 0, true, monster.getObjectId(), 0, 0), true);
                        }
                    }
                }
            }
        }
    }

    protected AttackInfo parseDamage(LittleEndianAccessor iPacket, MapleCharacter player, boolean ranged, boolean magic, boolean body) {
        AttackInfo ret = new AttackInfo();
        iPacket.readByte(); // FieldKey
        iPacket.readInt(); // pDrInfo.dr0
        iPacket.readInt(); // pDrInfo.dr1
        ret.numAttackedAndDamage = iPacket.readByte(); // DamagePerMob | 16 * Range
        ret.numAttacked = (ret.numAttackedAndDamage >>> 4) & 0xF;
        ret.numDamage = ret.numAttackedAndDamage & 0xF;
        ret.allDamage = new HashMap<>();
        iPacket.readInt(); // pDrInfo.dr2
        iPacket.readInt(); // pDrInfo.dr3
        ret.skillID = iPacket.readInt();
        if (ret.skillID > 0) {
            ret.skillLevel = player.getSkillLevel(ret.skillID);
        }
        byte CombatOrders = iPacket.readByte();
        iPacket.readInt(); // get_rand(pDrInfo.dr0, 0)
        iPacket.readInt(); // GetCrc32
        ret.ranged = ranged;
        ret.magic = magic;
        ret.body = body;
        if (magic) {
            iPacket.readInt(); // v460.dr0
            iPacket.readInt(); // v460.dr1
            iPacket.readInt(); // v460.dr2
            iPacket.readInt(); // v460.dr3
            iPacket.readInt(); // get_rand(v460.dr0, 0)
            iPacket.readInt(); // GetCrc32
        }
        iPacket.readInt(); // SKILLLEVELDATA::GetCrc
        iPacket.readInt(); // SKILLLEVELDATA::GetCrc        
        if (!body) {
            if (GameConstants.is_keydown_skill(ret.skillID)
                    || ret.skillID == FPArchMage.BIG_BANG
                    || ret.skillID == ILArchMage.BIG_BANG
                    || ret.skillID == Bishop.BIG_BANG
                    || ret.skillID == Evan.ICE_BREATH
                    || ret.skillID == Evan.FIRE_BREATH) {
                ret.KeyDown = iPacket.readInt(); // tKeyDown
            } else {
                ret.KeyDown = -1;
            }
        }
        if (ret.skillID == Paladin.HEAVENS_HAMMER) {
            ret.isHH = true;
        } else if (ret.skillID == Aran.COMBO_TEMPEST) {
            ret.isTempest = true;
        }
        Skill skill = null;
        if (ret.skillID > 0) {
            skill = SkillFactory.getSkill(ret.skillID);
            if (skill != null) {
                Item item = player.getInventory(MapleInventoryType.EQUIPPED).getItem((short) EquipSlot.WEAPON.getSlots()[0]);
                if (item != null) {
                    Equip wep = (Equip) item;
                    if (wep.hasLearnedSkills()) {
                        ItemData data = ItemInformationProvider.getInstance().getItemData(wep.getItemId());
                        SkillData skillData = data.skillData.get((int) wep.getItemLevel());
                        if (skillData != null) {
                            for (Pair<Integer, Integer> p : skillData.skills) {
                                if (p.left == ret.skillID) {
                                    ret.skillLevel += p.right;
                                }
                            }
                        }
                    }
                }
                ret.skillLevel = Math.min(ret.skillLevel, skill.getMaxLevel());
            } else {
                Logger.log(LogType.INFO, LogFile.GENERAL_ERROR, null, "Gave skill %d but is null.", ret.skillID);
            }
        }
        if (ranged) {
            ret.display = iPacket.readByte(); // nOption
            iPacket.readByte(); // NextShootExJablin && CUserLocal::CheckApplyExJablin(Skill, AttackAction);
            ret.AttackAction = iPacket.readShort(); // (int) AttackAction & 0x7FFF | ( (boolean) Left << 15)
            iPacket.readInt(); // GETCRC32Svr<long>(v214.x, 95u);  
            ret.AttackActionType = iPacket.readByte(); // nAttackType 
            ret.AttackSpeed = iPacket.readByte();
            ret.AttackTime = iPacket.readInt();
            ret.Phase = iPacket.readInt();
            iPacket.readShort(); // ProperBulletPosition
            iPacket.readShort(); // pnCashItemPos
            iPacket.readByte(); // nShootRange0a
            /*
            if (!GameConstants.is_shoot_skill_not_consuming_bullet(ret.skillID)) {
                ret.itemID = iPacket.readInt();
            }*/
        } else {
            ret.display = iPacket.readByte(); // nOption
            ret.AttackAction = iPacket.readShort(); // (int) AttackAction & 0x7FFF | ( (boolean) Left << 15)
            iPacket.readInt(); // GETCRC32Svr<long>(v214.x, 95u);
            ret.AttackActionType = iPacket.readByte(); // nAttackType 
            ret.AttackSpeed = iPacket.readByte();
            ret.AttackTime = iPacket.readInt();
            ret.Phase = iPacket.readInt();        
        }
        boolean mesoExplosion = ret.skillID == ChiefBandit.MesoExplosion;
        if (mesoExplosion) {
            player.message("Meso Explosion has been blocked.");
            return ret;
        }
        // TODO: Check Player Attack Speed:
        if (player.getStats().getAttackSpeed() != ret.AttackSpeed) {
            player.getStats().recalcLocalStats(player);
            if (player.getStats().getAttackSpeed() != ret.AttackSpeed) {
                Logger.log(LogType.INFO, LogFile.ATTACK_SPEED, player.getName() + " has AttackSpeed: " + player.getStats().getAttackSpeed() + " but client gave: " + ret.AttackSpeed + " Skill: " + ret.skillID + " Level: " + ret.skillLevel);
                AutobanFactory.WZ_EDIT.log(player, "Calculated Attack Speed: " + player.getStats().getAttackSpeed() + " Client Attack Speed: " + ret.AttackSpeed);
            }
        }
        int lastSkill = player.getAutobanManager().getLastSkill();
        int tTotalDelay = GameConstants.getAttackDelay(ret.skillID, skill);
        if (lastSkill == -1 || lastSkill == ret.skillID) {
            int v9 = 6;
            if (player.getJob().isA(MapleJob.MAGICIAN)) {
                Item wep = player.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -EquipSlot.WEAPON.getSlots()[0]);
                if (wep != null) {
                    v9 = ItemInformationProvider.getInstance().getItemData(wep.getItemId()).attackSpeed;
                }
            }
            Integer booster = player.getBuffedValue(SecondaryStat.Booster);
            if (booster == null) {
                booster = 0;
            }
            int v10 = get_attack_speed_degree(v9, ret.skillID, booster);
            long v11 = System.currentTimeMillis();
            int tDelay = (v10 + 10) * (tTotalDelay + 6 * ret.AttackAction) / 16;
        }
        player.getAutobanManager().spam(3, ret.AttackTime);
        player.getAutobanManager().setLastSkill(ret.skillID);

        // CalcDamage::PDamage -> CalcDamage::MDamage
        double calcDmgMax;
        // Source: https://ayumilovemaple.wordpress.com/2009/09/06/maplestory-formula-compilation/
        // Special Damage Formulas: Find the base damage to base further calculations on. Several skills have their own formula in this section.
        if (magic && ret.skillID != 0) {
            calcDmgMax = ((player.getTotalMagic() * player.getTotalMagic() / 1000D + player.getTotalMagic()) / 30D + player.getTotalInt() / 200D);
            // calcDmgMax = (chr.getTotalMagic() * chr.getTotalMagic() / 1000 + chr.getTotalMagic()) / 30 + chr.getTotalInt() / 200;
        } else if (ret.skillID == 4001344 || ret.skillID == NightWalker.LUCKY_SEVEN || ret.skillID == NightLord.TRIPLE_THROW) {
            calcDmgMax = (player.getTotalLuk() * 5) * player.getTotalWatk() / 100;
        } else if (ret.skillID == DragonKnight.DRAGON_ROAR) {// weapon type 43, 44
            calcDmgMax = (player.getTotalStr() * 4 + player.getTotalDex()) * player.getTotalWatk() / 100;
        } else if (ret.skillID == NightLord.VENOMOUS_STAR || ret.skillID == Shadower.VENOMOUS_STAB) {
            calcDmgMax = (int) (18.5 * (player.getTotalStr() + player.getTotalLuk()) + player.getTotalDex() * 2) / 100 * player.calculateMaxBaseDamage(player.getTotalWatk());
        } else {
            calcDmgMax = player.calculateMaxBaseDamage(player.getTotalWatk());
        }
        double originalCalcMaxDmg = calcDmgMax;
        if (ret.skillID != 0) {// Calculates damage from specific skills
            if (ret.skillLevel <= 0) {
                Logger.log(LogType.ERROR, LogFile.GENERAL_ERROR, player.getName() + " used skill " + ret.skillID + " with a skill level of " + ret.skillLevel);
            }
            MapleStatEffect effect = skill.getEffect(ret.skillLevel);
            if (magic) {
                if (null != player.getJob()) { // Since the skill is magic based, use the magic formula
                    switch (player.getJob()) {
                        case IL_ARCHMAGE:
                        case IL_MAGE: {
                            int skillLvl = player.getSkillLevel(ILMage.ELEMENT_AMPLIFICATION);
                            if (skillLvl > 0) {
                                calcDmgMax = calcDmgMax * SkillFactory.getSkill(ILMage.ELEMENT_AMPLIFICATION).getEffect(skillLvl).getY() / 100;
                            }
                            break;
                        }
                        case FP_ARCHMAGE:
                        case FP_MAGE: {
                            int skillLvl = player.getSkillLevel(FPMage.ELEMENT_AMPLIFICATION);
                            if (skillLvl > 0) {
                                calcDmgMax = calcDmgMax * SkillFactory.getSkill(FPMage.ELEMENT_AMPLIFICATION).getEffect(skillLvl).getY() / 100;
                            }
                            break;
                        }
                        case BLAZEWIZARD3:
                        case BLAZEWIZARD4: {
                            int skillLvl = player.getSkillLevel(BlazeWizard.ELEMENT_AMPLIFICATION);
                            if (skillLvl > 0) {
                                calcDmgMax = calcDmgMax * SkillFactory.getSkill(BlazeWizard.ELEMENT_AMPLIFICATION).getEffect(skillLvl).getY() / 100;
                            }
                            break;
                        }
                        case EVAN7:
                        case EVAN8:
                        case EVAN9:
                        case EVAN10: {
                            int skillLvl = player.getSkillLevel(Evan.MAGIC_AMPLIFICATION);
                            if (skillLvl > 0) {
                                calcDmgMax = calcDmgMax * SkillFactory.getSkill(Evan.MAGIC_AMPLIFICATION).getEffect(skillLvl).getY() / 100;
                            }
                            break;
                        }
                        default:
                            break;
                    }
                }
                if (effect.getMatk() != 0) {
                    calcDmgMax *= effect.getMatk();
                }
                if (ret.skillID == Cleric.Heal) {
                    // This formula is still a bit wonky, but it is fairly accurate.
                    calcDmgMax = (int) Math.round((player.getTotalInt() * 4.8 + player.getTotalLuk() * 4) * player.getTotalMagic() / 1000);
                    calcDmgMax = calcDmgMax * effect.getHp() / 100;
                }
            } else if (ret.skillID == Hermit.SHADOW_MESO) {
                // Shadow Meso also has its own formula
                calcDmgMax = effect.getMoneyCon() * 10;
                calcDmgMax = (int) Math.floor(calcDmgMax * 1.5);
            } else {
                // Normal damage formula for skills
                switch (ret.skillID) {
                    case Hunter.ARROW_BOMB:
                        calcDmgMax = calcDmgMax * effect.getX() / 100;
                        break;
                    default:
                        calcDmgMax = calcDmgMax * effect.getDamage() / 100;
                        break;
                }
            }
        }
        Integer comboBuff = player.getBuffedValue(SecondaryStat.ComboCounter);
        if (comboBuff != null && comboBuff > 0) {
            int oid = player.isCygnus() ? DawnWarrior.COMBO : Crusader.COMBO;
            int advcomboid = player.isCygnus() ? DawnWarrior.ADVANCED_COMBO : Hero.ADVANCED_COMBO;
            if (comboBuff > 6) {// 150%
                // Advanced Combo
                MapleStatEffect ceffect = SkillFactory.getSkill(advcomboid).getEffect(player.getSkillLevel(advcomboid));
                calcDmgMax = (int) Math.floor(calcDmgMax * (ceffect.getDamage() + 50) / 100 + 0.20 + (comboBuff - 5) * 0.04);
            } else {// 120%
                // Normal Combo
                MapleStatEffect ceffect = SkillFactory.getSkill(oid).getEffect(player.getSkillLevel(oid));
                calcDmgMax = (int) Math.floor(calcDmgMax * (ceffect.getDamage() + 20) / 100 + Math.floor((comboBuff - 1) * (player.getSkillLevel(oid) / 6)) / 100);
            }
            if (GameConstants.isFinisherSkill(ret.skillID)) {
                // Finisher skills do more damage based on how many orbs the player has.
                int orbs = comboBuff - 1;
                if (orbs == 2) {
                    calcDmgMax *= 1.2;
                } else if (orbs == 3) {
                    calcDmgMax *= 1.54;
                } else if (orbs == 4) {
                    calcDmgMax *= 2;
                } else if (orbs >= 5) {
                    calcDmgMax *= 2.5;
                }
            }
        }
        if (player.getBuffedValue(SecondaryStat.WindWalk) != null) {
            calcDmgMax *= player.getBuffEffect(SecondaryStat.WindWalk).getDamage() / 100;
        }
        if (player.getEnergyBar() == 15000) {
            int energycharge = player.isCygnus() ? ThunderBreaker.ENERGY_CHARGE : Marauder.ENERGY_CHARGE;
            MapleStatEffect ceffect = SkillFactory.getSkill(energycharge).getEffect(player.getSkillLevel(energycharge));
            /*
             TODO:
             calcDmgMax += calcDmgMax * (ceffect.getDamage() / 100D);
             System.out.println(calcDmgMax);
             */
        }
        if (player.getMapId() >= 914000000 && player.getMapId() <= 914000500) {
            calcDmgMax += 80000; // Aran Tutorial.
        }
        boolean canCrit = false;
        if (player.getJob().isA(MapleJob.THIEF) || player.getJob().isA(MapleJob.NIGHTWALKER1) || player.getJob().isA(MapleJob.WINDARCHER1) || player.getJob() == MapleJob.ARAN3 || player.getJob() == MapleJob.ARAN4 || player.getJob() == MapleJob.MARAUDER || player.getJob() == MapleJob.BUCCANEER) {
            canCrit = true;
        }
        boolean shadowPartner = false;
        if (player.getBuffEffect(SecondaryStat.ShadowPartner) != null) {
            shadowPartner = true;
        }
        if (ret.skillID == NightWalker.VAMPIRE) {
            calcDmgMax *= 1.2; // I believe vampire ups the max crit damage, but I am not positive...
            shadowPartner = false; // SP doesn't add extra lines to Vampire...
        }
        if (player.getJob().isA(MapleJob.NIGHTWALKER1) && player.getBuffEffect(SecondaryStat.DarkSight) != null) {
            int skillLvl = player.getSkillLevel(NightWalker.VANISH);
            if (skillLvl > 0) {
                MapleStatEffect ceffect = SkillFactory.getSkill(NightWalker.VANISH).getEffect(skillLvl);
                calcDmgMax = calcDmgMax * ceffect.getDamage() / 100;
            }
        }
        if (ret.skillID != 0) {
            int fixed = ret.getAttackEffect(player, SkillFactory.getSkill(ret.skillID)).getFixDamage();
            if (fixed > 0) {
                calcDmgMax = fixed;
            }
        }
        for (int i = 0; i < ret.numAttacked; i++) {
            int MobId = iPacket.readInt();
            int HitAction = iPacket.readByte();
            int ForeAction = iPacket.readByte();
            int FrameIdx = iPacket.readByte();
            byte GetCalcDamageStatIndex = iPacket.readByte();
            Point mobPosition = new Point(iPacket.readShort(), iPacket.readShort());
            Point mobPositionPrev = new Point(iPacket.readShort(), iPacket.readShort());
            iPacket.readShort(); // tDelay

            ret.mobPositions.add(i, mobPosition);
            ret.mobPositionPrev.add(i, mobPositionPrev);
            List<Pair<Integer, Boolean>> allDamageNumbers = new ArrayList<>();
            MapleMonster monster = player.getMap().getMonsterByOid(MobId);

            if (player.getBuffEffect(SecondaryStat.WeaponCharge) != null) {
                // Charge, so now we need to check elemental effectiveness
                int sourceID = player.getBuffSource(SecondaryStat.WeaponCharge);
                int level = player.getBuffedValue(SecondaryStat.WeaponCharge);
                if (monster != null) {
                    switch (sourceID) {
                        case WhiteKnight.BW_FIRE_CHARGE:
                        case WhiteKnight.SWORD_FIRE_CHARGE:
                            if (monster.getStats().getEffectiveness(Element.FIRE) == ElementalEffectiveness.WEAK) {
                                calcDmgMax *= 1.05 + level * 0.015;
                            }
                            break;
                        case WhiteKnight.BW_ICE_CHARGE:
                        case WhiteKnight.SWORD_ICE_CHARGE:
                            if (monster.getStats().getEffectiveness(Element.ICE) == ElementalEffectiveness.WEAK) {
                                calcDmgMax *= 1.05 + level * 0.015;
                            }
                            break;
                        case WhiteKnight.BW_LIT_CHARGE:
                        case WhiteKnight.SWORD_LIT_CHARGE:
                            if (monster.getStats().getEffectiveness(Element.LIGHTING) == ElementalEffectiveness.WEAK) {
                                calcDmgMax *= 1.05 + level * 0.015;
                            }
                            break;
                        case Paladin.BW_HOLY_CHARGE:
                        case Paladin.SWORD_HOLY_CHARGE:
                            if (monster.getStats().getEffectiveness(Element.HOLY) == ElementalEffectiveness.WEAK) {
                                calcDmgMax *= 1.2 + level * 0.015;
                            }
                            break;
                        default:
                            break;
                    }
                } else {
                    // Since we already know the skill has an elemental attribute, but we dont know if the monster is weak or not, lets take the safe approach and just assume they are weak.
                    calcDmgMax *= 1.5;
                }
            }
            if (ret.skillID != 0) {
                if (skill.getElement() != Element.NEUTRAL && player.getBuffedValue(SecondaryStat.ElementalReset) == null) {
                    // The skill has an element effect, so we need to factor that in.
                    if (monster != null) {
                        ElementalEffectiveness eff = monster.getEffectiveness(skill.getElement());
                        if (eff == ElementalEffectiveness.WEAK) {
                            calcDmgMax *= 1.5;
                        } else if (eff == ElementalEffectiveness.STRONG) {
                            // calcDmgMax *= 0.5;
                        }
                    } else {
                        // Since we already know the skill has an elemental attribute, but we dont know if the monster is weak or not, lets
                        // take the safe approach and just assume they are weak.
                        calcDmgMax *= 1.5;
                    }
                }
                if (ret.skillID == Hermit.SHADOW_WEB) {
                    if (monster != null) {
                        calcDmgMax = monster.getHp() / (50 - player.getSkillLevel(skill));
                    }
                }
            }
            // long rand[] = new long[numRand];// we need save it as long to store unsigned int
            // for(int in = 0; in < numRand; in++){
            // rand[in] = chr.getCRand().random();
            // }
            Skill shadowP = SkillFactory.getSkill(player.getJob().isA(MapleJob.NIGHTWALKER1) ? 14111000 : 4111002);
            // int originalCalcMinDmg = chr.calculateMinBaseDamage(chr.getTotalWatk());
            int shadowPLevel = player.getSkillLevel(shadowP);
            for (int j = 0; j < ret.numDamage; j++) {
                int damage = iPacket.readInt();
                // System.out.println("minDmg(nocalc): " + originalCalcMinDmg);
                // System.out.println("MaxDmg(nocalc): " + originalCalcMaxDmg);
                // double dmgCalcOrg = chr.getCRand().RandomInRange(rand[j % numRand], originalCalcMaxDmg, originalCalcMinDmg);
                // double pd = (monster != null && monster.getStats() != null ? monster.getStats().getPDDamage() : 0);
                // double dmgCalc = Math.max(0, 100.0 - pd) / 100.0 * dmgCalcOrg;
                // System.out.println("Calculated: " + dmgCalc + "(" + dmgCalcOrg + ")");
                double hitDmgMax = calcDmgMax;
                if (ret.skillID == Buccaneer.BARRAGE) {
                    if (j > 3) {
                        hitDmgMax *= Math.pow(2, (j - 3));
                    }
                }
                if (ret.skillID == NightWalker.VAMPIRE) {
                    hitDmgMax *= 3;
                }
                if (shadowPartner && shadowPLevel > 0) {
                    // For shadow partner, the second half of the hits only do 50% damage. So calc that
                    // in for the crit effects.
                    if (j >= ret.numDamage / 2) {
                        double y = ret.skillID == 0 ? shadowP.getEffect(shadowPLevel).getX() : shadowP.getEffect(shadowPLevel).getY();
                        hitDmgMax *= y / 100;
                    }
                }
                if (damage > GameConstants.MAX_DAMAGE && !player.isGM()) {
                    AutobanFactory.CLIENT_EDIT.alert(player, "Increased Damage Cap Dealt: " + damage);
                    damage = GameConstants.MAX_DAMAGE;
                }
                if (ret.skillID == Marksman.SNIPE) {
                    damage = 195000 + Randomizer.nextInt(5000);
                    hitDmgMax = 200000;
                }
                double maximumCriticalDamage = hitDmgMax;
                if (canCrit) { // They can crit, so up the max.
                    maximumCriticalDamage *= 2;// Need more strict checks for crits.
                } else if (player.playerStat.critDamage != 0) {
                    maximumCriticalDamage *= player.playerStat.critDamage;
                }
                if (hitDmgMax > GameConstants.MAX_DAMAGE) {
                    hitDmgMax = GameConstants.MAX_DAMAGE;
                }
                maximumCriticalDamage = Math.round(maximumCriticalDamage);
                // System.out.println("CDMG: " + damage + " maxWithCrit: " + maxWithCrit + " maxDmg: " + Math.round(hitDmgMax) + " calcBase: " + originalCalcMaxDmg);
                if (damage > maximumCriticalDamage) {
                    AutobanFactory.DAMAGE_HACK.alert(player, "CDMG: " + damage + " maxWithCrit: " + maximumCriticalDamage + " maxDmg: " + Math.round(hitDmgMax) + " calcBase: " + originalCalcMaxDmg);
                    // AutobanFactory.DAMAGE_HACK.alert(chr, "DMG: " + damage + " MaxDMG: " + maxWithCrit + " SID: " + ret.skill + " SLevel: " + ret.skilllevel + " MobID: " + (monster != null ? monster.getId() : "null") + " Map: " + chr.getMap().getMapData().getMapName() + " (" + chr.getMapId() + ") calcDmgMaxFinal: " + calcDmgMax + " originalCalcDmgMax: " + originalCalcMaxDmg);
                }
                if (AddDamageInfo.getActivate()) {
                    AddDamageInfo.setTotalDamage(damage);
                }
                // Add a ab point if its over 4x what we calculated.

                /*
                 TODO: Check Damage Hack ở đây
                 if (damage > maxWithCrit * 4 && maxWithCrit != 0) {
                 AutobanFactory.DAMAGE_HACK.addPoint(player.getAutobanManager(),
                 "DMG: " + damage + " MaxDMG: " + maxWithCrit + " SID: " +
                 ret.skillID + " MobID: " + (monster != null ? monster.getId() :
                 "null") + " Map: (" + player.getMapId() + ")");
                 }
                 if (ret.skillID == Marksman.SNIPE || (canCrit && damage >
                 hitDmgMax)) {
                 damage = -Integer.MAX_VALUE + damage - 1;
                 // If the skill is a crit, inverse the damage to make it show
                 up on clients.
                 }
                 */
                boolean crit = ret.skillID == Marksman.SNIPE || ((canCrit || player.playerStat.critDamage != 0) && damage > hitDmgMax);
                allDamageNumbers.add(new Pair<>(damage, crit));
            }
            iPacket.readInt(); // CMob::GetCrc
            ret.allDamage.put(MobId, allDamageNumbers);
        }
        ret.attackerPosition.x = iPacket.readShort();
        ret.attackerPosition.y = iPacket.readShort();
        if (ret.skillID == NightWalker.POISON_BOMB) { // Poison Bomb
            ret.position.setLocation(iPacket.readShort(), iPacket.readShort());
        }
        if (ranged) {
            if (GameConstants.is_wildhunter_job(player.getJob().getId())) {
                iPacket.readShort(); // Point BodyRelMove.y
            }
            ret.position.setLocation(iPacket.readShort(), iPacket.readShort());
            if (ret.skillID == ThunderBreaker.SPARK) {
                iPacket.readInt(); // time ReserveSpark
            }
        }
        if (magic) {
            boolean dragon = iPacket.readByte() == 0; // Dragon == 0?
            if (player.getDragon() != null && dragon) {
                short DragonX = iPacket.readShort(); // ret.attackerPositionByDragon.x
                short DragonY = iPacket.readShort(); // ret.attackerPositionByDragon.y
                Point DragonPosition = new Point(DragonX, DragonY);
                if (player.getDragon().getPosition() != DragonPosition) {
                    AutobanFactory.DISTANCE_HACK.alert(player, player.getName() + " is trying to hacking the dragon attack distance.");
                }
            }
        }
        return ret;
    }

    int highest = 0;

    private static int rand(int l, int u) {
        return (int) ((Math.random() * (u - l + 1)) + l);
    }

    public int get_attack_speed_degree(int degree, int skillId, int weaponBooster) {// nWeaponBooster is the nX value of the Booster buffstat active
        int v3 = degree;
        int result;
        if (skillId == Rogue.DOUBLE_STAB) {
            v3 = degree - 2;
        }
        result = weaponBooster + v3;
        if (result <= 2) {
            result = 2;
        }
        if (result >= 10) {
            result = 10;
        }
        return result;
    }
}
