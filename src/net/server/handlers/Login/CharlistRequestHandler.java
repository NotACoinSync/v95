package net.server.handlers.Login;

import client.MapleClient;
import constants.ServerConstants;
import constants.WorldConstants;
import constants.WorldConstants.WorldInfo;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.WvsContext;

public final class CharlistRequestHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        c.sendServerList(false);
        slea.readByte();
        
        int world = slea.readByte();
        c.setWorld(world);
        
        c.setChannel(slea.readByte());
        WorldInfo worldInfo = WorldConstants.WorldInfo.values()[world];
        if (c.getGMLevel() > 0 || worldInfo.isSelectable()) {
            c.sendCharList(world);
        } else {
            c.announce(WvsContext.BroadcastMsg.encode(1, ServerConstants.WORLD_NAMES[worldInfo.ordinal()] + " is currently unavailable."));
        }
        slea.readInt(); // lan ip
    }
}
