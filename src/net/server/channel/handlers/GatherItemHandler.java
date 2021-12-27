package net.server.channel.handlers;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.WvsContext;

public final class GatherItemHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        chr.getAutobanManager().setTimestamp(2, slea.readInt(), 3);
        MapleInventoryType inventoryType = MapleInventoryType.getByType(slea.readByte());
        if (inventoryType.equals(MapleInventoryType.MESO) || c.getPlayer().getInventory(inventoryType).isFull()) {
            c.announce(WvsContext.enableActions());
            return;
        }
        MapleInventory inventory = c.getPlayer().getInventory(inventoryType);
        boolean sorted = false;
        while (!sorted) {
            byte freeSlot = (byte) inventory.getNextFreeSlot();
            if (freeSlot != -1) {
                short itemSlot = -1;
                for (byte i = (byte) (freeSlot + 1); i <= inventory.getSlotLimit(); i++) {
                    if (inventory.getItem(i) != null) {
                        itemSlot = i;
                        break;
                    }
                }
                if (itemSlot > 0) {
                    MapleInventoryManipulator.move(c, inventoryType, itemSlot, freeSlot);
                } else {
                    sorted = true;
                }
            } else {
                sorted = true;
            }
        }
        c.announce(WvsContext.GatherItemResult(inventoryType.getType()));
        c.announce(WvsContext.enableActions());
    }
}
