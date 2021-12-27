package net.server.handlers.Item;

import client.MapleCharacter;
import client.MapleClient;
import client.autoban.AutobanFactory;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.player.SecondaryStat;
import net.AbstractMaplePacketHandler;
import server.ItemInformationProvider;
import server.MapleInventoryManipulator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.WvsContext;

public final class SendPortalScrollUseRequest extends AbstractMaplePacketHandler {

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
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        int timestamp = iPacket.readInt();
        player.getAutobanManager().setTimestamp(4, timestamp, 3);
        short slot = iPacket.readShort();
        int itemId = iPacket.readInt();
        Item toUse = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);
        if (toUse != null && toUse.getQuantity() > 0 && toUse.getItemId() == itemId) {
            switch (itemId) {
                case 2022178:
                case 2022433:
                case 2050004:
                    c.getPlayer().dispelDebuffs();
                    remove(c, slot);
                    return;
                case 2050001:
                    c.getPlayer().cancelBuffStats(SecondaryStat.Darkness);
                    remove(c, slot);
                    return;
                case 2050002:
                    c.getPlayer().cancelBuffStats(SecondaryStat.Weakness);
                    remove(c, slot);
                    return;
                case 2050003:
                    c.getPlayer().cancelBuffStats(SecondaryStat.Seal);
                    c.getPlayer().cancelBuffStats(SecondaryStat.Curse);
                    remove(c, slot);
                    return;
                default:
                    if (isTownScroll(itemId)) {
                        if (ii.getItemData(toUse.getItemId()).itemEffect.applyTo(c.getPlayer())) {
                            remove(c, slot);
                        }
                        return;
                    }
                    break;
            }
            if (ii.getItemData(toUse.getItemId()).itemEffect.applyTo(c.getPlayer())) {
                remove(c, slot);
            }
            c.getPlayer().getStats().recalcLocalStats(c.getPlayer());
        } else {
            AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use a null item in SendPortalScrollUseRequest");
        }
    }

    private void remove(MapleClient c, short slot) {
        MapleInventoryManipulator.removeItem(c, MapleInventoryType.USE, slot, (short) 1, true, false);
        c.announce(WvsContext.enableActions());
    }

    private boolean isTownScroll(int itemId) {
        return itemId >= 2030000 && itemId < 2030021;
    }
}
