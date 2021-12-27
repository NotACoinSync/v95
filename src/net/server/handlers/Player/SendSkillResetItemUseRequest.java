package net.server.handlers.Player;

import client.*;
import client.autoban.AutobanFactory;
import client.inventory.*;
import constants.GameConstants;
import constants.skills.*;
import java.util.Map;
import java.util.TreeMap;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.WvsContext;

public class SendSkillResetItemUseRequest extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (player == null) {
            c.disconnect(true, false);
            return;
        }
        if (!player.isAlive()) {
            c.announce(WvsContext.enableActions());
            return;
        }
        int timestamp = iPacket.readInt();
        player.getAutobanManager().setTimestamp(4, timestamp, 3);

        short slot = iPacket.readShort();
        int itemId = iPacket.readInt();
        Item toUse = player.getInventory(MapleInventoryType.USE).getItem(slot);
        if (toUse != null && toUse.getQuantity() > 0) {
            if (toUse.getItemId() != itemId && toUse.getItemId() / 10000 != 250) {
                AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use mismatched item in SendSkillResetItemUseRequest");
                return;
            }
            if (player.getLevel() < 10) {
                player.message("You cannot use this item, if you're below level 10.");
                return;
            }
            byte resetSkillLevel = 0;
            byte additionalSp = 0;
            Map<Integer, Byte> resetSkills = new TreeMap<>();
            for (Skill skill : player.getSkills().keySet()) {
                if (player.getSkillLevel(skill) > 0 && !skill.isBeginnerSkill() && isSkillVisible(skill.getId())) {
                    resetSkills.put(skill.getId(), player.getSkillLevel(skill));
                }
            }
            if (resetSkills.size() <= 0) {
                c.announce(WvsContext.SkillResetItemResult(player.getId(), false));
                return;
            }
            for (int resetSkill : resetSkills.keySet()) {
                additionalSp += resetSkills.get(resetSkill);
                player.setSkillLevel(resetSkill, resetSkillLevel);
            }
            if (additionalSp > 0) {
                int newPlayerSp = additionalSp + player.getRemainingSp();
                player.setRemainingSp(newPlayerSp);
                c.announce(WvsContext.SkillResetItemResult(player.getId(), true));
            }
        } else {
            AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use a null item in SkillBookHandler");
        }
    }

    private boolean isSkillVisible(int skillId) {
        return skillId != BladeSpecialist.TORNADO_SPIN_TWIRL
                && skillId / 10000 != 2001
                && skillId != 1014
                && skillId != 10001015
                && skillId != Rogue.KEEN_EYES
                && skillId != Rogue.LUCKY_SEVEN
                && !GameConstants.isHiddenSkills(skillId);
    }
}
