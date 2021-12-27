package net.server.handlers.OpenGatePool;

import client.MapleCharacter;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

public class TryEnterOpenGate extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (player == null) {
            c.disconnect(true, false);
            return;
        }
        int objectId = iPacket.readInt();
        short x = iPacket.readShort();
        short y = iPacket.readShort();
        byte enter = iPacket.readByte();
    }
}
