package net.server.handlers.ContiMove;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

public final class ContiMove_Init extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        int mapId = iPacket.readInt();
        int type = iPacket.readByte();
    }
}
