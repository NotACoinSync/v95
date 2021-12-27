package net.server.handlers.Item;

import client.MapleCharacter;
import client.MapleClient;
import client.autoban.AutobanFactory;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.WvsContext;

public class SendShopScannerItemUseRequest extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (player == null) {
            c.disconnect(true, false);
            return;
        }
        if (!player.isAlive()) {
            c.announce(WvsContext.enableActions());
            return;
        }
        short slot = iPacket.readShort();
        int itemId = iPacket.readInt();
        Item toUse = player.getInventory(MapleInventoryType.USE).getItem(slot);
        if (toUse != null && toUse.getQuantity() > 0) {
            if (toUse.getItemId() != itemId && toUse.getItemId() / 10000 == 231) {
                AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use mismatched item in SendShopScannerItemUseRequest");
                return;
            }
            // TODO?
        } else {
            AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use a null item in SendShopScannerItemUseRequest");
        }
    }
}
