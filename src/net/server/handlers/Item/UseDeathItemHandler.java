package net.server.handlers.Item;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CField.userpool.UserRemote;

public final class UseDeathItemHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        int itemId = iPacket.readInt();
        c.getPlayer().setItemEffect(itemId);
        c.announce(UserRemote.setActiveEffectItem(c.getPlayer().getId(), itemId));
    }
}
