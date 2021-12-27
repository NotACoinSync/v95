package net.server.handlers.FuncKeyMappedMan;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleKeyBinding;
import client.Skill;
import client.SkillFactory;
import client.autoban.AutobanFactory;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

public final class SendFuncKeyMappedMan extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (player == null) {
            c.disconnect(true, false);
            return;
        }
        int mode = iPacket.readInt();
        switch (mode) {
            case 0:
                // SaveFuncKeyMap
                int numChanges = iPacket.readInt();
                for (int i = 0; i < numChanges; i++) {
                    int key = iPacket.readInt();
                    int type = iPacket.readByte();
                    int action = iPacket.readInt();
                    if (type == 1) {
                        Skill skill = SkillFactory.getSkill(action);
                        boolean isBanndedSkill;
                        if (skill != null) {
                            isBanndedSkill = GameConstants.bannedBindSkills(skill.getId());
                            if (!player.isGM() && (isBanndedSkill || (!player.isGM() && GameConstants.isGMSkills(skill.getId())) || (!GameConstants.is_correct_job_for_skill_root(c.getPlayer().getJob().getId(), skill.getId() / 10000) && !GameConstants.isBeginnerSkill(skill.getId())))) { // for those skills are are "technically" in the beginner tab, like bamboo rain in Dojo or skills you find in PYPQ
                                AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), c.getPlayer().getName() + " tried to packet edit keymapping. skill: " + skill.getId());
                                c.disconnect(false, false);
                                return;
                            }
                            if (c.getPlayer().getSkillLevel(skill) < 1) {
                                c.disconnect(false, false);
                                return;
                            }
                        }
                    } else if (type == 2) {
                        if (c.getPlayer().getItemQuantity(action, false) < 1) {
                            c.disconnect(false, false);
                            return;
                        }
                    }
                    c.getPlayer().changeKeybinding(key, new MapleKeyBinding(type, action));
                }
                break;
            case 1:
                // ChangePetConsumeItemID
                int PetConsumeItemID = iPacket.readInt();
                if (PetConsumeItemID != 0 && c.getPlayer().getInventory(MapleInventoryType.USE).findById(PetConsumeItemID) == null) {
                    c.disconnect(false, false); // Don't let them send a packet with a use item they dont have.
                    return;
                }
                player.changeKeybinding(91, new MapleKeyBinding(7, PetConsumeItemID));
                break;
            case 2:
                // ChangePetConsumeMPItemID
                int PetConsumeMPItemID = iPacket.readInt();
                if (PetConsumeMPItemID != 0 && c.getPlayer().getInventory(MapleInventoryType.USE).findById(PetConsumeMPItemID) == null) {
                    c.disconnect(false, false); // Don't let them send a packet with a use item they dont have.
                    return;
                }
                player.changeKeybinding(92, new MapleKeyBinding(7, PetConsumeMPItemID));
                break;
            default:
                break;
        }
    }

}
