package net.server.handlers.Player;

import client.MapleClient;
import client.autoban.AutobanFactory;
import client.inventory.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CField.userpool.UserRemote;
import tools.packets.WvsContext;

public final class SendSitOnPortableChairRequest extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int itemId = slea.readInt();
        if (c.getPlayer().getInventory(MapleInventoryType.SETUP).findById(itemId) == null) {
            AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use null chair");
            return;
        }
        c.getPlayer().setChair(itemId);
        c.getPlayer().getMap().announce(c.getPlayer(), UserRemote.setActivePortableChair(c.getPlayer().getId(), itemId), false);
        c.announce(WvsContext.enableActions());
    }
}
