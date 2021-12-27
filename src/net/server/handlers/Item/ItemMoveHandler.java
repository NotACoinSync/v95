package net.server.handlers.Item;

import client.MapleClient;
import client.inventory.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.WvsContext;

public final class ItemMoveHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.skip(4);
        if (c.getPlayer().getAutobanManager().getLastSpam(6) + 300 > System.currentTimeMillis()) {
            c.announce(WvsContext.enableActions());
            return;
        }
        if (!c.getPlayer().isAlive()) {
            c.announce(WvsContext.enableActions());
            return;
        }
        MapleInventoryType type = MapleInventoryType.getByType(slea.readByte());
        byte src = (byte) slea.readShort();
        short action = slea.readShort();
        short quantity = slea.readShort();
        if (src < 0 && action > 0) {
            MapleInventoryManipulator.unequip(c, src, action);
        } else if (action < 0) {
            MapleInventoryManipulator.equip(c, src, action);
        } else if (action == 0) {
            MapleInventoryManipulator.drop(c, type, src, quantity);
        } else {
            MapleInventoryManipulator.move(c, type, src, action);
        }
        c.getPlayer().getAutobanManager().spam(6);
    }
}
