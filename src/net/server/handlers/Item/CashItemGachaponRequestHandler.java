package net.server.handlers.Item;

import client.MapleClient;
import client.inventory.Item;
import client.inventory.ItemFactory;
import client.inventory.MapleInventoryType;
import constants.ItemConstants;
import java.util.List;
import java.util.Random;
import net.AbstractMaplePacketHandler;
import net.server.channel.handlers.CashOperationHandler;
import server.ItemData;
import server.ItemInformationProvider;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CashShopPacket;
import tools.packets.WvsContext;

public class CashItemGachaponRequestHandler extends AbstractMaplePacketHandler {

    private Random rand = new Random();

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        long sn = slea.readLong();
        int remainingBoxes = -1;
        for (Item item : c.getPlayer().getCashShop().getInventory()) {
            if (item.getCashId() == sn && item.getItemId() == 5222000) {
                remainingBoxes = item.getQuantity();
            }
        }
        if (remainingBoxes <= 0) {
            c.announce(CashShopPacket.CashItemGachaponResult.checkExceededCashItems());
            c.announce(WvsContext.enableActions());
            return;
        }
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        List<ItemData> items = ii.getItemData();
        ItemData itemData = null;
        while (itemData == null && items.size() >= 100) {
            itemData = items.get(rand.nextInt(items.size()));
            if (!itemData.isCash) {
                itemData = null;
                continue;
            }
            if (itemData.itemid / 1000000 != 1) {
                itemData = null;
                continue;
            }
            int shortened = itemData.itemid / 10000;
            if (shortened == 111 || shortened == 180 || shortened == 190 || shortened == 191 || shortened == 193 || shortened == 194) {
                itemData = null;
                continue;
            }
            if (ItemConstants.isPet(itemData.itemid)) {
                itemData = null;
                continue;
            }
            /*if(!MapleItemInformationProvider.getInstance().getItemData(itemData.itemid).exists){
				itemData = null;
				continue;
			}*/
            if (CashOperationHandler.blocked(itemData.itemid)) {
                itemData = null;
                continue;
            }
            remainingBoxes--;// since we used one
            Item i = null;
            if (ii.getInventoryType(itemData.itemid) == MapleInventoryType.EQUIP) {
                i = ii.getEquipById(itemData.itemid);
            } else {
                i = new Item(itemData.itemid, (short) 0, (short) 1);
            }
            i.setExpiration(System.currentTimeMillis() + (180 * 24 * 60 * 60 * 1000L));
            c.announce(CashShopPacket.CashItemGachaponResult.encode(c.getAccID(), sn, remainingBoxes, i, i.getItemId(), 1, false));
            c.getPlayer().getCashShop().addToInventory(i);
            for (Item item : c.getPlayer().getCashShop().getInventory()) {
                if (item.getCashId() == sn) {
                    item.setQuantity((short) (item.getQuantity() - 1));
                    if (item.getQuantity() <= 0) {
                        c.getPlayer().getCashShop().removeFromInventory(item);
                        ItemFactory.deleteItem(item);
                    }
                    break;
                }
            }
            break;
        }
    }
}
