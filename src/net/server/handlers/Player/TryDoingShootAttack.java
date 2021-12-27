package net.server.handlers.Player;

import client.*;
import client.MapleCharacter.CancelCooldownAction;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.inventory.MapleWeaponType;
import client.player.SecondaryStat;
import constants.ItemConstants;
import constants.skills.*;
import net.server.channel.handlers.AbstractDealDamageHandler;
import server.*;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CField.userpool.UserRemote;
import tools.packets.PacketHelper;

public final class TryDoingShootAttack extends AbstractDealDamageHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (player == null) {
            c.disconnect(true, false);
            return;
        }
        AttackInfo attack = parseDamage(iPacket, player, true, false, false);
        if (player.getBuffEffect(SecondaryStat.Morph) != null) {
            if (player.getBuffEffect(SecondaryStat.Morph).isMorphWithoutAttack()) {
                // How are they attacking when the client won't let them?
                player.getClient().disconnect(false, false);
                return;
            }
        }
        switch (attack.skillID) {
            case Buccaneer.ENERGY_ORB:
            case ThunderBreaker.SPARK:
            case Shadower.TAUNT:
            case NightLord.TAUNT: {
                player.getMap().announce(player, UserRemote.rangedAttack(player, attack), false);
                applyAttack(attack, player, 1);
                break;
            }
            case Aran.COMBO_SMASH:
            case Aran.COMBO_PENRIL:
            case Aran.COMBO_TEMPEST: {
                player.getMap().announce(player, UserRemote.rangedAttack(player, attack), false);
                if (attack.skillID == Aran.COMBO_SMASH && player.getCombo() >= 30) {
                    player.setCombo((short) 0);
                    applyAttack(attack, player, 1);
                } else if (attack.skillID == Aran.COMBO_PENRIL && player.getCombo() >= 100) {
                    player.setCombo((short) 0);
                    applyAttack(attack, player, 2);
                } else if (attack.skillID == Aran.COMBO_TEMPEST && player.getCombo() >= 200) {
                    player.setCombo((short) 0);
                    applyAttack(attack, player, 4);
                }
                break;
            }
            default: {
                Item weapon = player.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11);
                MapleWeaponType type = ItemInformationProvider.getInstance().getWeaponType(weapon.getItemId());
                if (type == MapleWeaponType.NOT_A_WEAPON) {
                    return;
                }
                int projectile = 0;
                byte bulletCount = 1;
                MapleStatEffect effect = null;
                if (attack.skillID != 0) {
                    effect = attack.getAttackEffect(player, null);
                    bulletCount = effect.getBulletCount();
                    if (effect.getCooldown() > 0) {
                        c.announce(PacketHelper.skillCooldown(attack.skillID, effect.getCooldown()));
                    }
                }
                boolean hasShadowPartner = player.getBuffedValue(SecondaryStat.ShadowPartner) != null;
                if (hasShadowPartner) {
                    bulletCount *= 2;
                }
                MapleInventory inv = player.getInventory(MapleInventoryType.USE);
                for (short i = 1; i <= inv.getSlotLimit(); i++) {
                    Item item = inv.getItem(i);
                    if (item != null) {
                        int id = item.getItemId();
                        boolean bow = ItemConstants.isArrowForBow(id);
                        boolean cbow = ItemConstants.isArrowForCrossBow(id);
                        if (item.getQuantity() >= bulletCount) { // Fixes the bug where you can't use your last arrow.
                            ItemData data = ItemInformationProvider.getInstance().getItemData(id);
                            if (player.getLevel() < data.reqLevel) {
                                continue;
                            }
                            if (type == MapleWeaponType.CLAW && ItemConstants.isThrowingStar(id) && weapon.getItemId() != 1472063) {
                                projectile = id;
                                break;
                            } else if ((type == MapleWeaponType.GUN && ItemConstants.isBullet(id))) {
                                projectile = id;
                                break;
                            } else if ((type == MapleWeaponType.BOW && bow) || (type == MapleWeaponType.CROSSBOW && cbow) || (weapon.getItemId() == 1472063 && (bow || cbow))) {
                                projectile = id;
                                break;
                            }
                        }
                    }
                }
                boolean soulArrow = player.getBuffedValue(SecondaryStat.SoulArrow) != null;
                boolean shadowClaw = player.getBuffedValue(SecondaryStat.SpiritJavelin) != null;
                if (projectile != 0) {
                    if (!soulArrow && !shadowClaw && attack.skillID != 11101004 && attack.skillID != 15111007 && attack.skillID != 14101006) {
                        byte bulletConsume = bulletCount;
                        if (effect != null && effect.getBulletConsume() != 0) {
                            bulletConsume = (byte) (effect.getBulletConsume() * (hasShadowPartner ? 2 : 1));
                        }
                        MapleInventoryManipulator.removeStarById(c, MapleInventoryType.USE, projectile, bulletConsume, false, !ItemConstants.isThrowingStar(projectile));
                    }
                }
                if (projectile != 0 || soulArrow || attack.skillID == 11101004 || attack.skillID == 15111007 || attack.skillID == 14101006) {
                    int visProjectile = projectile;
                    if (ItemConstants.isThrowingStar(projectile)) {
                        MapleInventory cash = player.getInventory(MapleInventoryType.CASH);
                        for (int i = 1; i <= cash.getSlotLimit(); i++) {
                            Item item = cash.getItem((short) i);
                            if (item != null) {
                                if (item.getItemId() / 1000 == 5021) {
                                    visProjectile = item.getItemId();
                                    break;
                                }
                            }
                        }
                    } else // bow, crossbow
                    if (soulArrow || attack.skillID == 3111004 || attack.skillID == 3211004 || attack.skillID == 11101004 || attack.skillID == 15111007 || attack.skillID == 14101006) {
                        visProjectile = 0;
                    }
                    byte[] packet;
                    attack.projectile = visProjectile;
                    switch (attack.skillID) {
                        case 3121004: // Hurricane
                        case 3221001: // Pierce
                        case 5221004: // Rapid Fire
                        case 13111002: // KoC Hurricane
                            packet = UserRemote.rangedAttack(player, attack);
                            break;
                        default:
                            packet = UserRemote.rangedAttack(player, attack);
                            break;
                    }
                    player.getMap().announce(player, packet, false, true);
                    if (effect != null) {
                        int money = effect.getMoneyCon();
                        if (money != 0) {
                            int moneyMod = money / 2;
                            money += Randomizer.nextInt(moneyMod);
                            if (money > player.getMeso()) {
                                money = player.getMeso();
                            }
                            player.gainMeso(-money, false);
                        }
                    }
                    if (attack.skillID != 0) {
                        Skill skill = SkillFactory.getSkill(attack.skillID);
                        MapleStatEffect effect_ = skill.getEffect(player.getSkillLevel(skill));
                        if (effect_.getCooldown() > 0) {
                            if (player.skillisCooling(attack.skillID)) {
                                return;
                            } else {
                                c.announce(PacketHelper.skillCooldown(attack.skillID, effect_.getCooldown()));
                                player.addCooldown(attack.skillID, System.currentTimeMillis(), effect_.getCooldown() * 1000, TimerManager.getInstance().schedule("rah-cancel", new CancelCooldownAction(player, attack.skillID), effect_.getCooldown() * 1000));
                            }
                        }
                    }
                    if ((player.getSkillLevel(SkillFactory.getSkill(NightWalker.VANISH)) > 0 || player.getSkillLevel(SkillFactory.getSkill(WindArcher.WIND_WALK)) > 0) && attack.numAttacked > 0 && player.getBuffSource(SecondaryStat.DarkSight) != 9101004) {
                        player.cancelEffectFromSecondaryStat(SecondaryStat.DarkSight);
                        player.cancelBuffStats(SecondaryStat.DarkSight);
                        player.cancelEffectFromSecondaryStat(SecondaryStat.WindWalk);
                        player.cancelBuffStats(SecondaryStat.WindWalk);
                    }
                    applyAttack(attack, player, bulletCount);
                }
                break;
            }
        }
    }
}
