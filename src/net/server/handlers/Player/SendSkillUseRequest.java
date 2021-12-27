package net.server.handlers.Player;

import client.*;
import client.MapleCharacter.CancelCooldownAction;
import client.autoban.AutobanFactory;
import client.player.SecondaryStat;
import constants.FeatureSettings;
import constants.GameConstants;
import constants.skills.*;
import constants.skills.resistance.*;
import java.awt.Point;
import java.util.concurrent.ScheduledFuture;
import net.AbstractMaplePacketHandler;
import server.MapleStatEffect;
import server.TimerManager;
import server.life.MapleMonster;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CField.MobPool;
import tools.packets.CField.userpool.UserLocal;
import tools.packets.EffectPacket;
import tools.packets.PacketHelper;
import tools.packets.WvsContext;

public final class SendSkillUseRequest extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (player == null) {
            c.disconnect(true, false);
            return;
        }
        int timestamp = iPacket.readInt();
        player.getAutobanManager().setTimestamp(4, timestamp, 3);

        int skillID = iPacket.readInt();

        if (!player.isGM()) {
            if ((!GameConstants.isPQSkillMap(player.getMapId()) && GameConstants.isPqSkill(skillID)) || GameConstants.isGMSkills(skillID) || (!GameConstants.is_correct_job_for_skill_root(player.getJob().getId(), skillID / 10000) && !GameConstants.isBeginnerSkill(skillID))) {
                AutobanFactory.PACKET_EDIT.alert(player, player.getName() + " tried to packet edit skills. Using skill: " + skillID + " without it being in their job.");
                c.disconnect(true, false);
                return;
            }
        }
        Point pos = null;

        int __skillLevel = iPacket.readByte();

        Skill skill = SkillFactory.getSkill(skillID);
        int skillLevel = player.getSkillLevel(skill);
        if (skillID % 10000000 == 1010 || skillID % 10000000 == 1011) {
            skillLevel = 1;
            player.setDojoEnergy(0);
            c.announce(WvsContext.SessionValue("energy", 0));
        }
        if (skillLevel == 0 || skillLevel != __skillLevel) {
            AutobanFactory.PACKET_EDIT.alert(player, player.getName() + " tried to packet edit skill Level. Using skill: " + skillID + " without any Level.");
            c.disconnect(true, false);
            return;
        }
        MapleStatEffect effect = skill.getEffect(skillLevel);
        if (effect.getCooldown() > 0) {
            if (player.skillisCooling(skillID)) {
                return;
            } else if (skillID != Corsair.BATTLE_SHIP) {
                if (FeatureSettings.COOLDOWNS) {
                    c.announce(PacketHelper.skillCooldown(skillID, effect.getCooldown()));
                    ScheduledFuture<?> timer = TimerManager.getInstance().schedule("smh-cancel", new CancelCooldownAction(player, skillID), effect.getCooldown() * 1000);
                    player.addCooldown(skillID, System.currentTimeMillis(), effect.getCooldown() * 1000, timer);
                }
            }
        }
        if (player.isAlive()) {
            switch (skillID) {
                case NightLord.ShadowStars:
                    int stars = iPacket.readInt();
                    break;
                case BladeMaster.ChainsOfHell: {
                    int Count = iPacket.readInt();
                    for (int i = 0; i < Count; i++) {
                        int mobId = iPacket.readInt();
                        byte success = iPacket.readByte();
                        MapleMonster monster = player.getMap().getMonsterByOid(mobId);
                        if (monster != null) {
                            if (!monster.isBoss()) {
                                monster.switchController(player, monster.isControllerHasAggro());
                            }
                        }
                    }
                    byte direction = iPacket.readByte();
                    c.announce(WvsContext.enableActions());
                    break;
                }
                case Hero.MonsterMagnet:
                case Paladin.MonsterMagnet:
                case DarkKnight.MonsterMagnet: {
                    int Count = iPacket.readInt();
                    for (int i = 0; i < Count; i++) {
                        int mobId = iPacket.readInt();
                        int itemID = iPacket.readInt();
                        byte success = iPacket.readByte();
                        player.getMap().announce(player, MobPool.EffectByItem(mobId, itemID, success), false);
                        MapleMonster monster = player.getMap().getMonsterByOid(mobId);
                        if (monster != null) {
                            if (!monster.isBoss()) {
                                monster.switchController(player, monster.isControllerHasAggro());
                            }
                        }
                    }
                    byte direction = iPacket.readByte();
                    c.announce(WvsContext.enableActions());
                    break;
                }
                case BattleMage.YellowAura: {
                    if (player.getBuffEffect(SecondaryStat.YellowAura) != null) {
                        skill.getEffect(skillLevel).remove(player, player, true);
                    }
                    break;
                }
                case BattleMage.BlueAura: {
                    if (player.getBuffEffect(SecondaryStat.BlueAura) != null) {
                        skill.getEffect(skillLevel).remove(player, player, true);
                    }
                    break;
                }
                case BattleMage.DarkAura: {
                    if (player.getBuffEffect(SecondaryStat.DarkAura) != null) {
                        skill.getEffect(skillLevel).remove(player, player, true);
                    }
                    break;
                }
                case Priest.MysticDoor: {
                    if (!player.canDoor()) {
                        player.message("Please wait 5 seconds before casting Mystic Door again.");
                        c.announce(WvsContext.enableActions());
                        return;
                    }
                    break;
                }
                case Brawler.MP_RECOVERY: {
                    Skill s = SkillFactory.getSkill(skillID);
                    MapleStatEffect eff = s.getEffect(player.getSkillLevel(s));
                    double x = eff.getX();
                    double y = eff.getY();
                    int lose = (int) (player.getMaxHp() / x);
                    int gain = (int) (lose * (y / 100));
                    player.setHp(player.getHp() - lose);
                    player.updateSingleStat(MapleStat.HP, player.getHp());

                    player.setMp(player.getMp() + gain);
                    player.updateSingleStat(MapleStat.MP, player.getMp());
                    break;
                }
                case WildHunter.Capture: {
                    int capturedMobID = iPacket.readInt();
                    MapleMonster mob = player.getMap().getMonsterByOid(capturedMobID);
                    if (player.getCapturedMobs() < 5 && ((player.getAutobanManager().getLastSpam(10) + 800) < System.currentTimeMillis())) {
                        if (mob.getHp() < ((mob.getMaxHp() / 10) * 4)) {
                            if (Math.random() < 0.5) {
                                player.addCapturedMob(capturedMobID);
                                mob.getMap().killMonster(mob, null, false);
                                c.announce(EffectPacket.Local.SkillUse.showCaptureEffect(player.getLevel(), skillID, skillLevel, (byte) 0));
                            }
                        } else {
                            c.announce(EffectPacket.Local.SkillUse.showCaptureEffect(player.getLevel(), skillID, skillLevel, (byte) 1));
                        }
                    } else {
                        c.announce(EffectPacket.Local.SkillUse.showCaptureEffect(player.getLevel(), skillID, skillLevel, (byte) 2));
                    }
                    break;
                }
                case WildHunter.CallOfTheHunter: {
                    int capturedMobID = iPacket.readInt();
                    short X = iPacket.readShort();
                    short Y = iPacket.readShort();
                    byte left = iPacket.readByte();
                    if (player.getCapturedMob(capturedMobID) != 0) {
                        c.announce(EffectPacket.Local.SkillUse.LoadMoreWildEffect(player.getLevel(), skillLevel, left, X, Y));
                        c.getPlayer().getMap().announce(EffectPacket.Remote.SkillUse.LoadMoreWildEffect(player.getId(), player.getLevel(), skillLevel, left, X, Y));
                    }
                    break;
                }
                case ChiefBandit.MesoExplosion: {
                    // Blocked and handled in ParseDamage.
                    break;
                }
                case Evan.RecoveryAura:
                case Shadower.SmokeScreen:
                case BattleMage.PartyShield: {
                    short X = iPacket.readShort();
                    short Y = iPacket.readShort();
                    pos = new Point(X, Y);
                    break;
                }
                case Beginner.Soaring:
                case Noblesse.Soaring:
                case Legend.Soaring:
                case Evan.Soaring:
                case Citizen.Soaring: {
                    if (player.getBuffedValue(SecondaryStat.Flying) == null) {
                        player.setBuffedValue(SecondaryStat.Flying, 1);
                    } else {
                        player.cancelEffectFromSecondaryStat(SecondaryStat.Flying);
                    }
                    break;
                }
                case Citizen.Test: {
                    int InputNo_Result = iPacket.readInt();
                    c.announce(UserLocal.DamageMeter(InputNo_Result));
                    break;
                }
                case Beginner.EchoOfHero:
                case Noblesse.EchoOfHero:
                case Evan.EchoOfHero:
                case Legend.EchoOfHero:
                case Citizen.EchoOfHero:
                case SuperGM.HealAndDispel:
                case SuperGM.Haste:
                case SuperGM.HolySymbol:
                case SuperGM.Bless:
                case SuperGM.Resurrection:
                case SuperGM.HyperBody: {
                    byte aa = iPacket.readByte();
                    for (int i = 0; i < aa; i++) {
                        int aaa = iPacket.readInt();
                    }
                    short aaaa = iPacket.readShort();
                    // TODO: WTF is this?
                    break;
                }
                case BladeMaster.MonsterBomb: {
                    byte a = iPacket.readByte(); // == 1
                    int monsterId = iPacket.readInt();
                    short cc = iPacket.readShort(); // == 0
                    // TODO: WTF is this?
                    break;
                }
                default: {
                    break;
                }
            }
            if (skillID % 10000000 == 1004) {
                iPacket.readShort();
            }
            skill.getEffect(skillLevel).applyTo(player, pos);
            c.announce(WvsContext.SkillUseResult());
        } else {
            player.message("Cannot use skill while you're dead.");
            c.announce(WvsContext.enableActions());
        }
    }
}
