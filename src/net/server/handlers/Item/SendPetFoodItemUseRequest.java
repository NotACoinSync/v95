package net.server.handlers.Item;

import client.MapleCharacter;
import client.MapleClient;
import client.autoban.AutobanManager;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import net.AbstractMaplePacketHandler;
import server.ItemInformationProvider;
import server.MapleInventoryManipulator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.WvsContext;

public final class SendPetFoodItemUseRequest extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (player == null) {
            c.disconnect(true, false);
            return;
        }
        AutobanManager abm = player.getAutobanManager();
        if (abm.getLastSpam(2) + 500 > System.currentTimeMillis()) {
            c.announce(WvsContext.CashPetFoodResult(false));
            return;
        }
        abm.spam(2);
        abm.setTimestamp(1, iPacket.readInt(), 3);
        if (player.getNoPets() == 0) {
            c.announce(WvsContext.CashPetFoodResult(false));
            return;
        }
        int previousFullness = 100;
        byte slot = 0;
        MaplePet[] pets = player.getPets();
        for (byte i = 0; i < 3; i++) {
            if (pets[i] != null) {
                if (pets[i].getFullness() < previousFullness) {
                    slot = i;
                    previousFullness = pets[i].getFullness();
                }
            }
        }
        MaplePet pet = player.getPet(slot);
        short pos = iPacket.readShort();
        int itemId = iPacket.readInt();
        Item use = player.getInventory(MapleInventoryType.USE).getItem(pos);
        if (use == null || (itemId / 10000) != 212 || use.getItemId() != itemId) {
            if ((itemId / 10000) != 212) {
                if (itemId < 0) {
                    player.gainMeso(Math.abs(itemId), true);
                } else {
                    short quantity = iPacket.readShort();
                    Item item = null;
                    if (ItemInformationProvider.getInstance().getInventoryType(itemId) == MapleInventoryType.EQUIP) {
                        item = ItemInformationProvider.getInstance().getEquipById(itemId);
                    } else {
                        item = new Item(itemId, quantity);
                    }
                    MapleInventoryManipulator.addFromDrop(c, item, true);
                }
            }
            c.announce(WvsContext.CashPetFoodResult(false));
            return;
        }
        pet.feed(c.getPlayer());
        MapleInventoryManipulator.removeItem(c, MapleInventoryType.USE, pos, (short) 1, true, false);
        pet.saveToDb();
        Item petz = player.getInventory(MapleInventoryType.CASH).getItem(pet.getPosition());
        if (petz == null) { // Not a real fix but fuck it you know?
            c.announce(WvsContext.CashPetFoodResult(false));
            return;
        }
        player.forceUpdateItem(petz);
        c.announce(WvsContext.CashPetFoodResult(true));
    }
}
