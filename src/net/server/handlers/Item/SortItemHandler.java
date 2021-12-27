package net.server.handlers.Item;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.WvsContext;

public final class SortItemHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        player.getAutobanManager().setTimestamp(4, iPacket.readInt(), 3);
        byte inventoryType = iPacket.readByte();
        if (inventoryType < 1 || inventoryType > 5) {
            c.disconnect(false, false);
            return;
        }
        if (inventoryType == 5) {
            player.dropMessage("Unfortunately, sorting of cash inventory is disabled.");
            return;
        }
        final MapleInventoryType invType = MapleInventoryType.getByType(inventoryType);
        MapleInventory inventory = player.getInventory(MapleInventoryType.getByType(inventoryType));
        final List<Item> itemList = new LinkedList<>();
        inventory.list().forEach((item) -> {
            itemList.add(item.copy(false)); // clone all items T___T.
        });
        itemList.forEach((itemStats) -> {
            MapleInventoryManipulator.removeItem(c, invType, itemStats.getPosition(), itemStats.getQuantity(), true, true);
        });
        final List<Item> sortedItemsList = sortItems(itemList);
        sortedItemsList.forEach((item) -> {
            MapleInventoryManipulator.addFromDrop(c, item, false);
        });
        c.announce(WvsContext.SortItemResult(inventoryType));
        c.announce(WvsContext.enableActions());
    }

    private static List<Item> sortItems(final List<Item> passedMap) {
        final List<Integer> itemIds = new ArrayList<>(); // empty list.
        passedMap.forEach((item) -> {
            itemIds.add(item.getItemId()); // adds all item ids to the empty list to be sorted.
        });
        Collections.sort(itemIds); // sorts item ids
        final List<Item> sortedList = new LinkedList<>(); // ordered list pl0x <3.
        for (Integer val : itemIds) {
            for (Item item : passedMap) {
                if (val == item.getItemId()) { // Goes through every index and finds the first value that matches
                    sortedList.add(item);
                    passedMap.remove(item);
                    break;
                }
            }
        }
        return sortedList;
    }
}
