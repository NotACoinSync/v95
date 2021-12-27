package net.server.channel.handlers;

import client.player.SecondaryStat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import client.*;
import constants.skills.DarkKnight;
import constants.skills.resistance.Mechanic;
import net.AbstractMaplePacketHandler;
import server.ItemInformationProvider;
import server.MapleStatEffect;
import server.maps.objects.MapleSummon;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CField.SummonedPool;
import tools.packets.EffectPacket;
import tools.packets.WvsContext;

public final class BeholderHandler extends AbstractMaplePacketHandler {

    public class BeholderBuff {

        public static final int BUFF_PDD = 0x0, BUFF_MDD = 0x1, BUFF_ACC = 0x2, BUFF_EVA = 0x3, BUFF_PAD = 0x4;
    }

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        // System.out.println(slea.toString());
        Collection<MapleSummon> summons = c.getPlayer().getSummons().values();
        int summonedID = iPacket.readInt();
        MapleSummon summon = null;
        for (MapleSummon sum : summons) {
            if (sum.getObjectId() == summonedID) {
                summon = sum;
            }
        }
        if (summon != null) {
            int skillId = iPacket.readInt(); // 1320008
            Skill bBuff = SkillFactory.getSkill(skillId);
            if (c.getPlayer().getSkillLevel(bBuff) > 0) { // TODO: a check on beholder skill to see if its been x time since last execution
                switch (skillId) {
                    case DarkKnight.AURA_OF_BEHOLDER:
                    case Mechanic.HealingRobotHLX: {
                        byte attackAction = iPacket.readByte();
                        c.getPlayer().getMap().announce(c.getPlayer(), SummonedPool.Skill(c.getPlayer().getId(), skillId, attackAction), true);
                        c.getPlayer().addHP(bBuff.getEffect(c.getPlayer().getSkillLevel(bBuff)).getHp());
                        c.announce(EffectPacket.Local.SkillAffected(skillId, c.getPlayer().getSkillLevel(bBuff)));
                        c.getPlayer().getMap().announce(c.getPlayer(), EffectPacket.Remote.SkillAffected(c.getPlayer().getId(), skillId, c.getPlayer().getSkillLevel(bBuff)), false);
                        break;
                    }
                    case DarkKnight.HEX_OF_BEHOLDER: {
                        byte attackAction = iPacket.readByte();
                        byte buffType = iPacket.readByte();
                        // System.out.println("A");
                        int itemid = 2022125 + buffType;// gives a name/desc to the items.
                        MapleStatEffect effect = bBuff.getEffect(c.getPlayer().getSkillLevel(bBuff));
                        List<Pair<SecondaryStat, BuffDataHolder>> stat = new ArrayList<>();
                        switch (buffType) {
                            case BeholderBuff.BUFF_PDD:
                                stat.add(new Pair<>(SecondaryStat.PDD, new BuffDataHolder(itemid, c.getPlayer().getSkillLevel(bBuff), effect.getWdef())));
                                break;
                            case BeholderBuff.BUFF_MDD:
                                stat.add(new Pair<>(SecondaryStat.MDD, new BuffDataHolder(itemid, c.getPlayer().getSkillLevel(bBuff), effect.getMdef())));
                                break;
                            case BeholderBuff.BUFF_ACC:
                                stat.add(new Pair<>(SecondaryStat.ACC, new BuffDataHolder(itemid, c.getPlayer().getSkillLevel(bBuff), effect.getAcc())));
                                break;
                            case BeholderBuff.BUFF_EVA:
                                stat.add(new Pair<>(SecondaryStat.EVA, new BuffDataHolder(itemid, c.getPlayer().getSkillLevel(bBuff), effect.getAvoid())));
                                break;
                            case BeholderBuff.BUFF_PAD:
                                stat.add(new Pair<>(SecondaryStat.PAD, new BuffDataHolder(itemid, c.getPlayer().getSkillLevel(bBuff), effect.getWatk())));
                                break;
                        }
                        if (stat != null && !stat.isEmpty()) {
                            ItemInformationProvider ii = ItemInformationProvider.getInstance();
                            ii.getItemData(itemid).itemEffect.applyTo(c.getPlayer(), c.getPlayer(), false, null, effect.getDuration(), true);
                            c.announce(WvsContext.setTemporaryStat(c.getPlayer(), -itemid, effect.getDuration(), stat));// does cancelling work?
                            // effect.applyTo(c.getPlayer());// is this needed?
                            c.getPlayer().getMap().announce(c.getPlayer(), SummonedPool.Skill(c.getPlayer().getId(), skillId, attackAction), true);
                            c.announce(EffectPacket.Local.SkillAffected(skillId, c.getPlayer().getSkillLevel(bBuff)));
                            c.getPlayer().getMap().announce(c.getPlayer(), EffectPacket.Remote.SkillAffected(c.getPlayer().getId(), skillId, c.getPlayer().getSkillLevel(bBuff)), false);
                        }
                        break;
                    }
                    case Mechanic.BotsnTots: {
                        byte attackAction = iPacket.readByte();
                        iPacket.readInt();
                        c.getPlayer().getMap().announce(c.getPlayer(), SummonedPool.Skill(c.getPlayer().getId(), skillId, attackAction), true);
                        break;
                    }
                    default:
                        break;
                }
            }
        } else {
            c.getPlayer().getSummons().clear();
        }
    }
}
