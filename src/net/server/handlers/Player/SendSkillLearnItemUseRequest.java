package net.server.handlers.Player;

import client.*;
import client.autoban.AutobanFactory;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import server.ItemData;
import server.ItemInformationProvider;
import server.MapleInventoryManipulator;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.WvsContext;

public final class SendSkillLearnItemUseRequest extends AbstractMaplePacketHandler {

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
            if (toUse.getItemId() != itemId) {
                AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use mismatched item in SendSkillLearnItemUseRequest");
                return;
            }
            ItemData skillData = ItemInformationProvider.getInstance().getItemData(toUse.getItemId());
            boolean canuse;
            boolean success = false;
            int skill = 0;
            int maxlevel = 0;
            if (skillData == null) {
                AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use a non-skillbook in SkillBookhandler");
                return;
            }
            if (skillData.skills.isEmpty()) {
                c.announce(WvsContext.SkillLearnItemResult(player.getId(), skill, maxlevel, false, success));
                return;
            }
            Skill skill2 = null;
            if (c.getPlayer().getJob() != null && c.getPlayer().getJob().getJobTree() != null) {
                top:
                for (MapleJob job : c.getPlayer().getJob().getJobTree()) {
                    for (int id : skillData.skills) {
                        if (id / 10000 == job.getId()) {
                            skill2 = SkillFactory.getSkill(id);
                            break top;
                        }
                    }
                }
            }
            if (skill2 == null) {
                canuse = false;
            } else if (player.getReincarnations() < skillData.rcount) {
                canuse = false;
            } else if ((player.getSkillLevel(skill2) >= skillData.reqSkillLevel || skillData.reqSkillLevel == 0) && player.getMasterLevel(skill2) < skillData.masterLevel) {
                canuse = true;
                if (Randomizer.nextInt(101) < skillData.success && skillData.success != 0) {
                    success = true;
                    player.changeSkillLevel(skill2, player.getSkillLevel(skill2), Math.max(skillData.masterLevel, player.getMasterLevel(skill2)), -1);
                } else {
                    success = false;
                }
                MapleInventoryManipulator.removeItem(c, MapleInventoryType.USE, slot, (short) 1, true, false);
            } else {
                canuse = false;
            }
            c.announce(WvsContext.SkillLearnItemResult(player.getId(), skill, maxlevel, canuse, success));
        } else {
            AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use a null item in SkillBookHandler");
        }
    }
}
