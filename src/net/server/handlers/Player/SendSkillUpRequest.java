package net.server.handlers.Player;

import client.*;
import client.autoban.AutobanFactory;
import client.autoban.AutobanManager;
import constants.GameConstants;
import constants.JobConstants;
import constants.skills.Aran;
import constants.skills.BladeSpecialist;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.WvsContext;

public final class SendSkillUpRequest extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        AutobanManager abm = player.getAutobanManager();
        int timestamp = slea.readInt();// TODO: Readd
        abm.setTimestamp(0, timestamp, 3);
        int skillID = slea.readInt();
        if (GameConstants.isHiddenSkills(skillID)) {
            AutobanFactory.PACKET_EDIT.alert(player, "tried to packet edit in distributing sp. Tried to add sp to hidden skill " + skillID);
            c.disconnect(true, false);
            return;
        }
        if ((!GameConstants.isPQSkillMap(player.getMapId()) 
                && GameConstants.isPqSkill(skillID)) 
                || (!player.isGM() && GameConstants.isGMSkills(skillID))) { //|| (!GameConstants.is_correct_job_for_skill_root(player.getJob().getId(), skillID / 10000) && !GameConstants.isBeginnerSkill(skillID))) {
            AutobanFactory.PACKET_EDIT.alert(player, "tried to packet edit in distributing sp. Tried to use skill " + skillID + " without it being in their job.");
            c.disconnect(true, false);
            return;
        }

        int remainingSp = player.getRemainingSpBySkill(JobConstants.getSkillBookIndex(skillID / 10000));

        boolean isBeginnerSkill = false;

        if (skillID % 10000000 > 999 && skillID % 10000000 < 1003) {
            int total = 0;
            for (int i = 0; i < 3; i++) {
                total += player.getSkillLevel(SkillFactory.getSkill(player.getJobType() * 10000000 + 1000 + i));
            }
            remainingSp = Math.min((player.getLevel() - 1), 6) - total;
            isBeginnerSkill = true;
        }

        Skill skill = SkillFactory.getSkill(skillID);
        int currentLevel = player.getSkillLevel(skill);
        int maxLevel = skill.getMaxLevel();
        int masterLevel = skill.getMasterLevel();
        if (masterLevel > 0 && player.getMasterLevel(skill) <= masterLevel) {
            maxLevel = masterLevel;
        }
        if ((remainingSp > 0 && currentLevel + 1 <= maxLevel)) {
            if (!isBeginnerSkill) {
                player.setRemainingSp(player.getRemainingSpBySkill(JobConstants.getSkillBookIndex(skillID / 10000)) - 1, JobConstants.getSkillBookIndex(skillID / 10000));
            }
            player.updateSingleStat(MapleStat.AVAILABLESP, player.getRemainingSpBySkill(JobConstants.getSkillBookIndex(skillID / 10000)));
            switch (skill.getId()) {
                case Aran.FULL_SWING:
                    player.changeSkillLevel(skill, (byte) (currentLevel + 1), player.getMasterLevel(skill), player.getSkillExpiration(skill));
                    player.changeSkillLevel(SkillFactory.getSkill(Aran.HIDDEN_FULL_DOUBLE), player.getSkillLevel(skill), player.getMasterLevel(skill), player.getSkillExpiration(skill));
                    player.changeSkillLevel(SkillFactory.getSkill(Aran.HIDDEN_FULL_TRIPLE), player.getSkillLevel(skill), player.getMasterLevel(skill), player.getSkillExpiration(skill));
                    break;
                case Aran.OVER_SWING:
                    player.changeSkillLevel(skill, (byte) (currentLevel + 1), player.getMasterLevel(skill), player.getSkillExpiration(skill));
                    player.changeSkillLevel(SkillFactory.getSkill(Aran.HIDDEN_OVER_DOUBLE), player.getSkillLevel(skill), player.getMasterLevel(skill), player.getSkillExpiration(skill));
                    player.changeSkillLevel(SkillFactory.getSkill(Aran.HIDDEN_OVER_TRIPLE), player.getSkillLevel(skill), player.getMasterLevel(skill), player.getSkillExpiration(skill));
                    break;
                case BladeSpecialist.TORNADO_SPIN:
                    player.changeSkillLevel(skill, (byte) (currentLevel + 1), player.getMasterLevel(skill), player.getSkillExpiration(skill));
                    player.changeSkillLevel(SkillFactory.getSkill(BladeSpecialist.TORNADO_SPIN_TWIRL), player.getSkillLevel(skill), player.getMasterLevel(skill), player.getSkillExpiration(skill));
                    break;
                default:
                    player.changeSkillLevel(skill, (byte) (currentLevel + 1), player.getMasterLevel(skill), player.getSkillExpiration(skill));
                    break;
            }
        } else {
            System.out.println("Skill errors!!");
            System.out.println("isbeginner " + isBeginnerSkill);
            System.out.println("remainingsp " + remainingSp);
            System.out.println("curlvl " + currentLevel);
            System.out.println("maxlvl " + masterLevel);
            player.announce(WvsContext.enableActions());
        }
    }
}
