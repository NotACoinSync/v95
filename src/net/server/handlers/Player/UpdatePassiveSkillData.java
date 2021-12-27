package net.server.handlers.Player;

import client.MapleCharacter;
import client.MapleCharacter.SkillEntry;
import client.MapleClient;
import client.autoban.AutobanManager;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

public final class UpdatePassiveSkillData extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (player == null) {
            c.disconnect(true, false);
            return;
        }
        AutobanManager abm = player.getAutobanManager();
        int timestamp = iPacket.readInt();// TODO: Readd
        abm.setTimestamp(0, timestamp, 3);

    }

    private void setPassiveSkillData(MapleCharacter player, SkillEntry skill, int skillLevel) {
        if (skill != null && skillLevel > 0) {

        }

    }
}
