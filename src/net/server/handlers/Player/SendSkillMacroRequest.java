package net.server.handlers.Player;

import client.MapleCharacter;
import client.MapleClient;
import client.SkillMacro;
import client.autoban.AutobanFactory;
import constants.GameConstants;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

public final class SendSkillMacroRequest extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (player == null) {
            c.disconnect(true, false);
            return;
        }
        // MACROSYSDATA::Decode
        int count = iPacket.readByte();
        // SINGLEMACRO::Decode
        for (int i = 0; i < count; i++) {
            String name = iPacket.readMapleAsciiString();
            int mute = iPacket.readByte();
            int skill1 = iPacket.readInt();
            int skill2 = iPacket.readInt();
            int skill3 = iPacket.readInt();
            if (skill1 != 0 && c.getPlayer().getSkillLevel(skill1) == 0) {
                AutobanFactory.PACKET_EDIT.autoban(c.getPlayer(), "Tried to set a macro with skill: " + skill1 + " without any levels in that skill");
            }
            if (skill2 != 0 && c.getPlayer().getSkillLevel(skill2) == 0) {
                AutobanFactory.PACKET_EDIT.autoban(c.getPlayer(), "Tried to set a macro with skill: " + skill2 + " without any levels in that skill");
            }
            if (skill3 != 0 && c.getPlayer().getSkillLevel(skill3) == 0) {
                AutobanFactory.PACKET_EDIT.autoban(c.getPlayer(), "Tried to set a macro with skill: " + skill3 + " without any levels in that skill");
            }
            if (GameConstants.isHiddenSkills(skill1)) {
                AutobanFactory.PACKET_EDIT.autoban(c.getPlayer(), "Tried to set a macro with skill: " + skill1 + " when its a hidden skill.");
            }
            if (GameConstants.isHiddenSkills(skill2)) {
                AutobanFactory.PACKET_EDIT.autoban(c.getPlayer(), "Tried to set a macro with skill: " + skill2 + " when its a hidden skill.");
            }
            if (GameConstants.isHiddenSkills(skill3)) {
                AutobanFactory.PACKET_EDIT.autoban(c.getPlayer(), "Tried to set a macro with skill: " + skill3 + " when its a hidden skill.");
            }
            SkillMacro macro = new SkillMacro(skill1, skill2, skill3, name, mute, i);
            c.getPlayer().updateMacros(i, macro);
        }
    }
}
