package net.server.channel.handlers;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CField.userpool.UserCommon;

public final class CloseChalkboardHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        c.getPlayer().setChalkboard(null);
        c.getPlayer().getMap().announce(UserCommon.ADBoard(c.getPlayer().getId(), c.getPlayer().getChalkboard(), true));
    }
}
