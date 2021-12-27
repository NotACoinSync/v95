package net.server.handlers.Wedding;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import server.ItemInformationProvider;
import server.maps.MapleMap;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.WvsContext;

public class Wedding_SendBuff extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        MapleMap map = player.getMap();
        if (map == null) {
            c.disconnect(true, false);
            return;
        }
        byte currentStep = iPacket.readByte();
        switch (currentStep) {
            case 2: {
                if (map.getId() == 680000210) {
                    // ???
                }
                break;
            }
            case 6: {
                short slot = iPacket.readShort();
                int itemid = iPacket.readInt();
                short quantity = iPacket.readShort();
                MapleInventoryType type = ItemInformationProvider.getInstance().getInventoryType(itemid);
                Item item = player.getInventory(type).getItem(slot);
                if (itemid == item.getItemId() && quantity <= item.getQuantity()) {
                    // c.getPlayer().addItemToWishList(item);
                    c.announce(WvsContext.WeddingGiftResult.Give(player));
                }
                break;
            }
            default:
                break;
        }
    }
}
