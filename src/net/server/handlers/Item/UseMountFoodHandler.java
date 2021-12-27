package net.server.handlers.Item;

import client.MapleClient;
import client.inventory.MapleInventoryType;
import constants.ExpTable;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.WvsContext;

public final class UseMountFoodHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.skip(6);
        int itemid = slea.readInt();
        if (c.getPlayer().getInventory(MapleInventoryType.USE).findById(itemid) != null) {
            if (c.getPlayer().getMount() != null && c.getPlayer().getMount().getTiredness() > 0) {
                c.getPlayer().getMount().setTiredness(Math.max(c.getPlayer().getMount().getTiredness() - 30, 0));
                c.getPlayer().getMount().setExp(2 * c.getPlayer().getMount().getLevel() + 6 + c.getPlayer().getMount().getExp());
                int level = c.getPlayer().getMount().getLevel();
                boolean levelup = c.getPlayer().getMount().getExp() >= ExpTable.getMountExpNeededForLevel(level) && level < 31;
                if (levelup) {
                    c.getPlayer().getMount().setLevel(level + 1);
                }
                c.getPlayer().getMap().announce(WvsContext.SetTamingMobInfo(c.getPlayer().getId(), c.getPlayer().getMount(), levelup));
                MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemid, 1, true, true);
                c.announce(WvsContext.enableActions());
            }
        }
    }
}
