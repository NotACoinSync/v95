package net.server.channel.handlers;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleKeyBinding;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

public final class QuickSlotSaveHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        if (slea.available() < 32) {// Apparently this is 34?
            return; // Err? Client sets length of packet to 32 automatically. Must be a packet edit...
        }
        for (int i = 0; i <= 7; i++) {
            int action = slea.readInt();
            chr.changeKeybinding(93 + i, new MapleKeyBinding(8, action));
        }
    }
}
