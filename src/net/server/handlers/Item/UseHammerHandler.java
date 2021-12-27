package net.server.handlers.Item;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.UIItemUpgrade;

public final class UseHammerHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        c.announce(UIItemUpgrade.sendHammerMessage());
    }
}
