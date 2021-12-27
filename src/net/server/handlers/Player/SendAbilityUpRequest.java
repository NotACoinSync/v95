package net.server.handlers.Player;

import client.*;
import client.autoban.AutobanManager;
import constants.GameConstants;
import constants.skills.*;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.WvsContext;

public final class SendAbilityUpRequest extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        AutobanManager abm = player.getAutobanManager();
        int timestamp = slea.readInt();// TODO: Readd
        abm.setTimestamp(0, timestamp, 3);
        int dwFlag = slea.readInt();
        
        if (player.getRemainingAp() > 0) {
            if (addStat(c, dwFlag)) {
                player.setRemainingAp(player.getRemainingAp() - 1);
                player.updateSingleStat(MapleStat.AVAILABLEAP, player.getRemainingAp());
            }
        }
        c.announce(WvsContext.enableActions());
    }

    public static boolean addStat(MapleClient c, int apTo) {
        switch (apTo) {
            case 64: // Str
                if (c.getPlayer().getStr() >= GameConstants.maxAbilityStat) {
                    return false;
                }
                c.getPlayer().addStat(1, 1);
                break;
            case 128: // Dex
                if (c.getPlayer().getDex() >= GameConstants.maxAbilityStat) {
                    return false;
                }
                c.getPlayer().addStat(2, 1);
                break;
            case 256: // Int
                if (c.getPlayer().getInt() >= GameConstants.maxAbilityStat) {
                    return false;
                }
                c.getPlayer().addStat(3, 1);
                break;
            case 512: // Luk
                if (c.getPlayer().getLuk() >= GameConstants.maxAbilityStat) {
                    return false;
                }
                c.getPlayer().addStat(4, 1);
                break;
            case 2048: // HP
                addHP(c.getPlayer(), addHP(c));
                break;
            case 8192: // MP
                addMP(c.getPlayer(), addMP(c));
                break;
            default:
                c.announce(WvsContext.enableActions());
                return false;
        }
        return true;
    }

    static int addHP(MapleClient c) {
        MapleCharacter player = c.getPlayer();
        MapleJob job = player.getJob();
        int MaxHP = player.getMaxHp();
        if (player.getHpMpApUsed() > 9999 || MaxHP >= 30000) {
            return MaxHP;
        }
        if (job.isA(MapleJob.WARRIOR) || job.isA(MapleJob.DAWNWARRIOR1) || job.isA(MapleJob.ARAN1)) {
            Skill increaseHP = SkillFactory.getSkill(job.isA(MapleJob.DAWNWARRIOR1) ? DawnWarrior.MAX_HP_INCREASE : Warrior.IMPROVED_MAXHP);
            int sLvl = player.getSkillLevel(increaseHP);
            if (sLvl > 0) {
                MaxHP += increaseHP.getEffect(sLvl).getY();
            }
            MaxHP += 20;
        } else if (job.isA(MapleJob.MAGICIAN) || job.isA(MapleJob.BLAZEWIZARD1)) {
            MaxHP += 6;
        } else if (job.isA(MapleJob.BOWMAN) || job.isA(MapleJob.WINDARCHER1) || job.isA(MapleJob.THIEF) || job.isA(MapleJob.NIGHTWALKER1)) {
            MaxHP += 16;
        } else if (job.isA(MapleJob.PIRATE) || job.isA(MapleJob.THUNDERBREAKER1)) {
            Skill increaseHP = SkillFactory.getSkill(Brawler.IMPROVE_MAX_HP);
            int sLvl = player.getSkillLevel(increaseHP);
            if (sLvl > 0) {
                MaxHP += increaseHP.getEffect(sLvl).getY();
            }
            MaxHP += 18;
        } else {
            MaxHP += 8;
        }
        return MaxHP;
    }

    static int addMP(MapleClient c) {
        MapleCharacter player = c.getPlayer();
        int MaxMP = player.getMaxMp();
        MapleJob job = player.getJob();
        if (player.getHpMpApUsed() > 9999 || player.getMaxMp() >= 30000) {
            return MaxMP;
        }
        if (job.isA(MapleJob.WARRIOR) || job.isA(MapleJob.DAWNWARRIOR1) || job.isA(MapleJob.ARAN1)) {
            MaxMP += 2;
        } else if (job.isA(MapleJob.MAGICIAN) || job.isA(MapleJob.BLAZEWIZARD1)) {
            Skill increaseMP = SkillFactory.getSkill(job.isA(MapleJob.BLAZEWIZARD1) ? BlazeWizard.INCREASING_MAX_MP : Magician.IMPROVED_MAX_MP_INCREASE);
            int sLvl = player.getSkillLevel(increaseMP);
            if (sLvl > 0) {
                MaxMP += increaseMP.getEffect(sLvl).getY();
            }
            MaxMP += 18;
        } else if (job.isA(MapleJob.BOWMAN) || job.isA(MapleJob.WINDARCHER1) || job.isA(MapleJob.THIEF) || job.isA(MapleJob.NIGHTWALKER1)) {
            MaxMP += 10;
        } else if (job.isA(MapleJob.PIRATE) || job.isA(MapleJob.THUNDERBREAKER1)) {
            MaxMP += 14;
        } else {
            MaxMP += 6;
        }
        MaxMP += player.getTotalInt() / 10;
        return MaxMP;
    }

    static void addHP(MapleCharacter player, int MaxHP) {
        MaxHP = Math.min(30000, MaxHP);
        player.setHpMpApUsed(player.getHpMpApUsed() + 1);
        player.setMaxHp(MaxHP);
        player.updateSingleStat(MapleStat.MAXHP, MaxHP);
    }

    static void addMP(MapleCharacter player, int MaxMP) {
        MaxMP = Math.min(30000, MaxMP);
        player.setHpMpApUsed(player.getHpMpApUsed() + 1);
        player.setMaxMp(MaxMP);
        player.updateSingleStat(MapleStat.MAXMP, MaxMP);
    }
}
