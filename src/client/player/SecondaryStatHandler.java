package client.player;

import client.BuffDataHolder;
import client.player.buffs.twostate.*;
import java.util.ArrayList;
import java.util.List;
import tools.Pair;
import tools.data.output.LittleEndianWriter;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.packets.PacketHelper;

public class SecondaryStatHandler {

    public final List<TemporaryStatBase> temporaryStat = new ArrayList<>(7);

    public SecondaryStatHandler() {
        for (TSIndex enIndex : TSIndex.values()) {
            if (enIndex == TSIndex.PartyBooster) {
                temporaryStat.add(new PartyBooster());
            } else if (enIndex == TSIndex.GuidedBullet) {
                temporaryStat.add(new GuidedBullet());
            } else if (enIndex == TSIndex.EnergyCharged) {
                temporaryStat.add(new TemporaryStatBase(true));
            } else {
                temporaryStat.add(new TwoStateTemporaryStat(enIndex != TSIndex.RideVehicle));
            }
        }
    }

    public TemporaryStatBase getTemporaryState(int index) {
        return temporaryStat.get(index);
    }

    public void setTemporaryState(int index, TemporaryStatBase statBase) {
        temporaryStat.set(index, statBase);
    }

    // SecondaryStat::DecodeForLocal
    public void encodeLocal(LittleEndianWriter oPacket, List<Pair<SecondaryStat, BuffDataHolder>> statUps, int buffId, int buffLength) {
        PacketHelper.encodeFlag(oPacket, statUps);
        for (Pair<SecondaryStat, BuffDataHolder> statUp : statUps) {
            if (statUp.getLeft().isDisease()) {
                oPacket.writeShort(statUp.getRight().getValue());
                oPacket.writeShort(statUp.getRight().getSourceID());
                oPacket.writeShort(statUp.getRight().getSourceLevel());
            } else {
                if (statUp.getLeft().equals(SecondaryStat.PartyBooster)) {
                    oPacket.writeShort(0);
                    oPacket.writeInt(statUp.getRight().getValue());
                    oPacket.writeInt(buffId);
                } else {
                    oPacket.writeShort(statUp.getRight().getValue());
                    oPacket.writeInt(buffId);
                }
            }
            oPacket.writeInt(buffLength);
        }
        oPacket.write(0); // DefenseAtt_CS
        oPacket.write(0); // DefenseState_CS
        for (Pair<SecondaryStat, BuffDataHolder> statup : statUps) {
            List<SecondaryStat> SwallowBuffs = new ArrayList<>();
            if (statup.getLeft().isSwallowBuffs()) {
                SwallowBuffs.add(statup.getLeft());
                if (SwallowBuffs.size() <= 5) {
                    oPacket.write(0); // SwallowBuffTime
                }
            }
            if (statup.getLeft().equals(SecondaryStat.Dice)) {
                for (int i = 22; i >= 1; i--) {
                    oPacket.writeInt(0); // Dice.Info
                }
            }
            if (statup.getLeft().equals(SecondaryStat.BlessingArmor)) {
                oPacket.write(0); // BlessingArmorIncPAD              
            }
        }
        temporaryStat.forEach(stat -> stat.encodeForClient(oPacket));
        oPacket.writeShort(0); // tDelay
        boolean isMovementAffectingStat = statUps.stream().anyMatch(stat -> stat.getLeft().isMovementAffectingStat());
        if (isMovementAffectingStat) {
            oPacket.write(0); // Stat
        }
    }

    // SecondaryStatHandler::DecodeForRemote
    public void encodeRemote(LittleEndianWriter oPacket, List<Pair<SecondaryStat, BuffDataHolder>> statUps) {
        PacketHelper.encodeFlag(oPacket, statUps);
        for (Pair<SecondaryStat, BuffDataHolder> statUp : statUps) {
            if (!statUp.getLeft().isNoValueStats()) {
                if (statUp.getLeft().isDisease()) {
                    if (statUp.getLeft() == SecondaryStat.Poison) {
                        oPacket.writeShort(statUp.getRight().getValue());
                    }
                    oPacket.writeShort(statUp.getRight().getSourceID());
                    oPacket.writeShort(statUp.getRight().getSourceLevel());
                } else if (statUp.getLeft() == SecondaryStat.Speed || statUp.getLeft() == SecondaryStat.ComboCounter || statUp.getLeft() == SecondaryStat.Cyclone) {
                    oPacket.write(statUp.getRight().getValue());
                } else if (statUp.getLeft() == SecondaryStat.Morph || statUp.getLeft() == SecondaryStat.Ghost) {
                    oPacket.writeShort(statUp.getRight().getValue());
                } else {
                    oPacket.writeInt(statUp.getRight().getValue());
                }
            }
        }
        oPacket.write(0); // DefenseAtt_CS
        oPacket.write(0); // DefenseState_CS
        temporaryStat.forEach(stat -> stat.encodeForClient(oPacket));
    }

    public void encodeRemote_BackUp(final MaplePacketLittleEndianWriter oPacket, List<Pair<SecondaryStat, BuffDataHolder>> statups) {
        int[] buffmask = new int[16];
        List<Pair<Integer, Integer>> buffList = new ArrayList<>();
        buffmask[SecondaryStat.EnergyCharged.getSet()] |= SecondaryStat.EnergyCharged.getMask();
        buffmask[SecondaryStat.Dash_Speed.getSet()] |= SecondaryStat.Dash_Speed.getMask();
        buffmask[SecondaryStat.Dash_Jump.getSet()] |= SecondaryStat.Dash_Jump.getMask();
        buffmask[SecondaryStat.RideVehicle.getSet()] |= SecondaryStat.RideVehicle.getMask();
        buffmask[SecondaryStat.PartyBooster.getSet()] |= SecondaryStat.PartyBooster.getMask();
        buffmask[SecondaryStat.GuidedBullet.getSet()] |= SecondaryStat.GuidedBullet.getMask();
        buffmask[SecondaryStat.Undead.getSet()] |= SecondaryStat.Undead.getMask();
        for (Pair<SecondaryStat, BuffDataHolder> statup : statups) {
            if (statup.getLeft() == SecondaryStat.Speed) {
                buffmask[SecondaryStat.Speed.getSet()] |= SecondaryStat.Speed.getMask();
                buffList.add(new Pair<>(statup.getRight().getValue(), 1));
            }
            if (statup.getLeft() == SecondaryStat.ComboCounter) {
                buffmask[SecondaryStat.ComboCounter.getSet()] |= SecondaryStat.ComboCounter.getMask();
                buffList.add(new Pair<>(statup.getRight().getValue(), 1));
            }
            if (statup.getLeft() == SecondaryStat.WeaponCharge) {
                buffmask[SecondaryStat.WeaponCharge.getSet()] |= SecondaryStat.WeaponCharge.getMask();
                buffList.add(new Pair<>(statup.getRight().getSourceLevel(), 2));
                buffList.add(new Pair<>(statup.getRight().getSourceID(), 2));
            }
            if (statup.getLeft() == SecondaryStat.Stun) {
                buffmask[SecondaryStat.Stun.getSet()] |= SecondaryStat.Stun.getMask();
                buffList.add(new Pair<>(statup.getRight().getSourceLevel(), 2));
                buffList.add(new Pair<>(statup.getRight().getSourceID(), 2));
            }
            if (statup.getLeft() == SecondaryStat.Darkness) {
                buffmask[SecondaryStat.Darkness.getSet()] |= SecondaryStat.Darkness.getMask();
                buffList.add(new Pair<>(statup.getRight().getSourceLevel(), 2));
                buffList.add(new Pair<>(statup.getRight().getSourceID(), 2));
            }
            if (statup.getLeft() == SecondaryStat.Seal) {
                buffmask[SecondaryStat.Seal.getSet()] |= SecondaryStat.Seal.getMask();
                buffList.add(new Pair<>(statup.getRight().getSourceLevel(), 2));
                buffList.add(new Pair<>(statup.getRight().getSourceID(), 2));
            }
            if (statup.getLeft() == SecondaryStat.Weakness) {
                buffmask[SecondaryStat.Weakness.getSet()] |= SecondaryStat.Weakness.getMask();
                buffList.add(new Pair<>(statup.getRight().getSourceLevel(), 2));
                buffList.add(new Pair<>(statup.getRight().getSourceID(), 2));
            }
            if (statup.getLeft() == SecondaryStat.Curse) {
                buffmask[SecondaryStat.Curse.getSet()] |= SecondaryStat.Curse.getMask();
                buffList.add(new Pair<>(statup.getRight().getSourceLevel(), 2));
                buffList.add(new Pair<>(statup.getRight().getSourceID(), 2));
            }
            if (statup.getLeft() == SecondaryStat.Poison) {
                buffmask[SecondaryStat.Poison.getSet()] |= SecondaryStat.Poison.getMask();
                buffList.add(new Pair<>(statup.getRight().getValue(), 2));
                buffList.add(new Pair<>(statup.getRight().getSourceLevel(), 2));
                buffList.add(new Pair<>(statup.getRight().getSourceID(), 2));
            }
            if (statup.getLeft() == SecondaryStat.ShadowPartner) {
                buffmask[SecondaryStat.ShadowPartner.getSet()] |= SecondaryStat.ShadowPartner.getMask();
                buffList.add(new Pair<>(statup.getRight().getSourceLevel(), 2));
                buffList.add(new Pair<>(statup.getRight().getSourceID(), 2));
            }
            if (statup.getLeft() == SecondaryStat.DarkSight) {
                buffmask[SecondaryStat.DarkSight.getSet()] |= SecondaryStat.DarkSight.getMask();
            }
            if (statup.getLeft() == SecondaryStat.SoulArrow) {
                buffmask[SecondaryStat.SoulArrow.getSet()] |= SecondaryStat.SoulArrow.getMask();
            }
            if (statup.getLeft() == SecondaryStat.Morph) {
                buffmask[SecondaryStat.Morph.getSet()] |= SecondaryStat.Morph.getMask();
                buffList.add(new Pair<>(statup.getRight().getValue(), 2));
            }
            if (statup.getLeft() == SecondaryStat.Ghost) {
                buffmask[SecondaryStat.Ghost.getSet()] |= SecondaryStat.Ghost.getMask();
                buffList.add(new Pair<>(statup.getRight().getValue(), 2));
            }
            if (statup.getLeft() == SecondaryStat.Attract) {
                buffmask[SecondaryStat.Attract.getSet()] |= SecondaryStat.Attract.getMask();
                buffList.add(new Pair<>(statup.getRight().getSourceLevel(), 2));
                buffList.add(new Pair<>(statup.getRight().getSourceID(), 2));
            }
            if (statup.getLeft() == SecondaryStat.SpiritJavelin) {
                buffmask[SecondaryStat.SpiritJavelin.getSet()] |= SecondaryStat.SpiritJavelin.getMask();
                buffList.add(new Pair<>(statup.getRight().getValue(), 4));
            }
            if (statup.getLeft() == SecondaryStat.BanMap) {
                buffmask[SecondaryStat.BanMap.getSet()] |= SecondaryStat.BanMap.getMask();
                buffList.add(new Pair<>(statup.getRight().getSourceLevel(), 2));
                buffList.add(new Pair<>(statup.getRight().getSourceID(), 2));
            }
            if (statup.getLeft() == SecondaryStat.Barrier) {
                buffmask[SecondaryStat.Barrier.getSet()] |= SecondaryStat.Barrier.getMask();
                buffList.add(new Pair<>(statup.getRight().getSourceLevel(), 2));
                buffList.add(new Pair<>(statup.getRight().getSourceID(), 2));
            }
            if (statup.getLeft() == SecondaryStat.DojangShield) {
                buffmask[SecondaryStat.DojangShield.getSet()] |= SecondaryStat.DojangShield.getMask();
                buffList.add(new Pair<>(statup.getRight().getSourceLevel(), 2));
                buffList.add(new Pair<>(statup.getRight().getSourceID(), 2));
            }
            if (statup.getLeft() == SecondaryStat.ReverseInput) {
                buffmask[SecondaryStat.ReverseInput.getSet()] |= SecondaryStat.ReverseInput.getMask();
                buffList.add(new Pair<>(statup.getRight().getSourceLevel(), 2));
                buffList.add(new Pair<>(statup.getRight().getSourceID(), 2));
            }
            if (statup.getLeft() == SecondaryStat.RespectPImmune) {
                buffmask[SecondaryStat.RespectPImmune.getSet()] |= SecondaryStat.RespectPImmune.getMask();
                buffList.add(new Pair<>(statup.getRight().getValue(), 4));
            }
            if (statup.getLeft() == SecondaryStat.RespectMImmune) {
                buffmask[SecondaryStat.RespectMImmune.getSet()] |= SecondaryStat.RespectMImmune.getMask();
                buffList.add(new Pair<>(statup.getRight().getValue(), 4));
            }
            if (statup.getLeft() == SecondaryStat.DefenseAtt) {
                buffmask[SecondaryStat.DefenseAtt.getSet()] |= SecondaryStat.DefenseAtt.getMask();
                buffList.add(new Pair<>(statup.getRight().getValue(), 4));
            }
            if (statup.getLeft() == SecondaryStat.DefenseState) {
                buffmask[SecondaryStat.DefenseState.getSet()] |= SecondaryStat.DefenseState.getMask();
                buffList.add(new Pair<>(statup.getRight().getValue(), 4));
            }
            if (statup.getLeft() == SecondaryStat.DojangBerserk) {
                buffmask[SecondaryStat.DojangBerserk.getSet()] |= SecondaryStat.DojangBerserk.getMask();
            }
            if (statup.getLeft() == SecondaryStat.DojangInvincible) {
                buffmask[SecondaryStat.DojangInvincible.getSet()] |= SecondaryStat.DojangInvincible.getMask();
            }
            if (statup.getLeft() == SecondaryStat.WindWalk) {
                buffmask[SecondaryStat.WindWalk.getSet()] |= SecondaryStat.WindWalk.getMask();
            }
            if (statup.getLeft() == SecondaryStat.RepeatEffect) {
                buffmask[SecondaryStat.RepeatEffect.getSet()] |= SecondaryStat.RepeatEffect.getMask();
                buffList.add(new Pair<>(statup.getRight().getSourceLevel(), 2));
                buffList.add(new Pair<>(statup.getRight().getSourceID(), 2));
            }
            if (statup.getLeft() == SecondaryStat.StopPortion) {
                buffmask[SecondaryStat.StopPortion.getSet()] |= SecondaryStat.StopPortion.getMask();
                buffList.add(new Pair<>(statup.getRight().getSourceLevel(), 2));
                buffList.add(new Pair<>(statup.getRight().getSourceID(), 2));
            }
            if (statup.getLeft() == SecondaryStat.StopMotion) {
                buffmask[SecondaryStat.StopMotion.getSet()] |= SecondaryStat.StopMotion.getMask();
                buffList.add(new Pair<>(statup.getRight().getSourceLevel(), 2));
                buffList.add(new Pair<>(statup.getRight().getSourceID(), 2));
            }
            if (statup.getLeft() == SecondaryStat.Fear) {
                buffmask[SecondaryStat.Fear.getSet()] |= SecondaryStat.Fear.getMask();
                buffList.add(new Pair<>(statup.getRight().getSourceLevel(), 2));
                buffList.add(new Pair<>(statup.getRight().getSourceID(), 2));
            }
            if (statup.getLeft() == SecondaryStat.MagicShield) {
                buffmask[SecondaryStat.MagicShield.getSet()] |= SecondaryStat.MagicShield.getMask();
                buffList.add(new Pair<>(statup.getRight().getValue(), 4));
            }
            if (statup.getLeft() == SecondaryStat.Flying) {
                buffmask[SecondaryStat.Flying.getSet()] |= SecondaryStat.Flying.getMask();
            }
            if (statup.getLeft() == SecondaryStat.Frozen) {
                buffmask[SecondaryStat.Frozen.getSet()] |= SecondaryStat.Frozen.getMask();
                buffList.add(new Pair<>(statup.getRight().getSourceLevel(), 2));
                buffList.add(new Pair<>(statup.getRight().getSourceID(), 2));
            }
            if (statup.getLeft() == SecondaryStat.SuddenDeath) {
                buffmask[SecondaryStat.SuddenDeath.getSet()] |= SecondaryStat.SuddenDeath.getMask();
                buffList.add(new Pair<>(statup.getRight().getSourceLevel(), 2));
                buffList.add(new Pair<>(statup.getRight().getSourceID(), 2));
            }
            if (statup.getLeft() == SecondaryStat.FinalCut) {
                buffmask[SecondaryStat.FinalCut.getSet()] |= SecondaryStat.FinalCut.getMask();
                buffList.add(new Pair<>(statup.getRight().getSourceLevel(), 2));
                buffList.add(new Pair<>(statup.getRight().getSourceID(), 2));
            }
            if (statup.getLeft() == SecondaryStat.Cyclone) {
                buffmask[SecondaryStat.Cyclone.getSet()] |= SecondaryStat.Cyclone.getMask();
                buffList.add(new Pair<>(statup.getRight().getValue(), 1));
            }
            if (statup.getLeft() == SecondaryStat.Sneak) {
                buffmask[SecondaryStat.Sneak.getSet()] |= SecondaryStat.Sneak.getMask();
            }
            if (statup.getLeft() == SecondaryStat.MorewildDamageUp) {
                buffmask[SecondaryStat.MorewildDamageUp.getSet()] |= SecondaryStat.MorewildDamageUp.getMask();
            }
            if (statup.getLeft() == SecondaryStat.Mechanic) {
                buffmask[SecondaryStat.Mechanic.getSet()] |= SecondaryStat.Mechanic.getMask();
                buffList.add(new Pair<>(statup.getRight().getSourceLevel(), 2));
                buffList.add(new Pair<>(statup.getRight().getSourceID(), 2));
            }
            if (statup.getLeft() == SecondaryStat.DarkAura) {
                buffmask[SecondaryStat.DarkAura.getSet()] |= SecondaryStat.DarkAura.getMask();
                buffList.add(new Pair<>(statup.getRight().getSourceLevel(), 2));
                buffList.add(new Pair<>(statup.getRight().getSourceID(), 2));
            }
            if (statup.getLeft() == SecondaryStat.BlueAura) {
                buffmask[SecondaryStat.BlueAura.getSet()] |= SecondaryStat.BlueAura.getMask();
                buffList.add(new Pair<>(statup.getRight().getSourceLevel(), 2));
                buffList.add(new Pair<>(statup.getRight().getSourceID(), 2));
            }
            if (statup.getLeft() == SecondaryStat.YellowAura) {
                buffmask[SecondaryStat.YellowAura.getSet()] |= SecondaryStat.YellowAura.getMask();
                buffList.add(new Pair<>(statup.getRight().getSourceLevel(), 2));
                buffList.add(new Pair<>(statup.getRight().getSourceID(), 2));
            }
            if (statup.getLeft() == SecondaryStat.BlessingArmor) {
                buffmask[SecondaryStat.BlessingArmor.getSet()] |= SecondaryStat.BlessingArmor.getMask();
            }

        }
        for (int i = 3; i >= 0; i--) {
            oPacket.writeInt(buffmask[i]);
        }
        for (Pair<Integer, Integer> buff : buffList) {
            if (null != buff.right) {
                switch (buff.right) {
                    case 4:
                        oPacket.writeInt(buff.left);
                        break;
                    case 2:
                        oPacket.writeShort(buff.left);
                        break;
                    case 1:
                        oPacket.write(buff.left);
                        break;
                    default:
                        break;
                }
            }
        }
        oPacket.write(0); // DefenseAtt_CS
        oPacket.write(0); // DefenseState_CS
        temporaryStat.forEach(stat -> stat.encodeForClient(oPacket));
    }
}
