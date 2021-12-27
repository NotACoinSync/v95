package net.server.channel.handlers;

import client.MapleClient;
import client.autoban.AutobanFactory;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CField.userpool.UserRemote;

public final class UseItemEffectHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        Item toUse;
        int itemId = iPacket.readInt();
        if (itemId == 4290001 || itemId == 4290000) {
            toUse = c.getPlayer().getInventory(MapleInventoryType.ETC).findById(itemId);
        } else {
            toUse = c.getPlayer().getInventory(MapleInventoryType.CASH).findById(itemId);
        }
        if (toUse == null || toUse.getQuantity() < 1) {
            AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to do use null item in UseItemEffect");
            if (itemId != 0) {
                return;
            }
        }
        c.getPlayer().setItemEffect(itemId);
        c.getPlayer().getMap().announce(c.getPlayer(), UserRemote.setActiveEffectItem(c.getPlayer().getId(), itemId), false);
    }
}
