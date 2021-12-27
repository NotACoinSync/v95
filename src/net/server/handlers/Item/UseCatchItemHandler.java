package net.server.handlers.Item;

import client.MapleCharacter;
import client.MapleClient;
import client.autoban.AutobanManager;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import server.ItemInformationProvider;
import server.MapleInventoryManipulator;
import server.life.MapleMonster;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CField.MobPool;
import tools.packets.WvsContext;

public final class UseCatchItemHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        AutobanManager abm = chr.getAutobanManager();
        abm.setTimestamp(5, slea.readInt(), 3);
        slea.readShort();
        int itemId = slea.readInt();
        int monsterid = slea.readInt();
        MapleMonster mob = chr.getMap().getMonsterByOid(monsterid);
        if (chr.getInventory(ItemInformationProvider.getInstance().getInventoryType(itemId)).countById(itemId) <= 0) {
            return;
        }
        if (mob == null) {
            return;
        }
        switch (itemId) {
            case 2270000:
                if (mob.getId() == 9300101) {
                    chr.getMap().announce(MobPool.CatchEffect(monsterid, itemId, (byte) 1));
                    mob.getMap().killMonster(mob, null, false);
                    MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
                    MapleInventoryManipulator.addFromDrop(c, new Item(1902000, (short) 1), false);
                }
                c.announce(WvsContext.enableActions());
                break;
            case 2270001:
                if (mob.getId() == 9500197) {
                    if ((abm.getLastSpam(10) + 1000) < System.currentTimeMillis()) {
                        if (mob.getHp() < ((mob.getMaxHp() / 10) * 4)) {
                            chr.getMap().announce(MobPool.CatchEffect(monsterid, itemId, (byte) 1));
                            mob.getMap().killMonster(mob, null, false);
                            MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
                            MapleInventoryManipulator.addFromDrop(c, new Item(4031830, (short) 1), false);
                        } else {
                            abm.spam(10);
                            c.announce(WvsContext.BridleMobCatchFail(0, itemId));
                        }
                    }
                }
                c.announce(WvsContext.enableActions());
                break;
            case 2270002:
                if (mob.getId() == 9300157) {
                    if ((abm.getLastSpam(10) + 800) < System.currentTimeMillis()) {
                        if (mob.getHp() < ((mob.getMaxHp() / 10) * 4)) {
                            if (Math.random() < 0.5) { // 50% chance
                                chr.getMap().announce(MobPool.CatchEffect(monsterid, itemId, (byte) 1));
                                mob.getMap().killMonster(mob, null, false);
                                MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
                                MapleInventoryManipulator.addFromDrop(c, new Item(4031868, (short) 1), false);
                            } else {
                                chr.getMap().announce(MobPool.CatchEffect(monsterid, itemId, (byte) 0));
                            }
                            abm.spam(10);
                        } else {
                            c.announce(WvsContext.BridleMobCatchFail(0, itemId));
                        }
                    }
                }
                c.announce(WvsContext.enableActions());
                break;
            case 2270003:
                if (mob.getId() == 9500320) {
                    if (mob.getHp() < ((mob.getMaxHp() / 10) * 4)) {
                        chr.getMap().announce(MobPool.CatchEffect(monsterid, itemId, (byte) 1));
                        mob.getMap().killMonster(mob, null, false);
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
                        MapleInventoryManipulator.addFromDrop(c, new Item(4031887, (short) 1), false);
                    } else {
                        c.announce(WvsContext.BridleMobCatchFail(0, itemId));
                    }
                }
                c.announce(WvsContext.enableActions());
                break;
            case 2270005:
                if (mob.getId() == 9300187) {
                    if (mob.getHp() < ((mob.getMaxHp() / 10) * 3)) {
                        chr.getMap().announce(MobPool.CatchEffect(monsterid, itemId, (byte) 1));
                        mob.getMap().killMonster(mob, null, false);
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
                        MapleInventoryManipulator.addFromDrop(c, new Item(2109001, (short) 1), false);
                    } else {
                        c.announce(WvsContext.BridleMobCatchFail(0, itemId));
                    }
                }
                c.announce(WvsContext.enableActions());
                break;
            case 2270006:
                if (mob.getId() == 9300189) {
                    if (mob.getHp() < ((mob.getMaxHp() / 10) * 3)) {
                        chr.getMap().announce(MobPool.CatchEffect(monsterid, itemId, (byte) 1));
                        mob.getMap().killMonster(mob, null, false);
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
                        MapleInventoryManipulator.addFromDrop(c, new Item(2109002, (short) 1), false);
                    } else {
                        c.announce(WvsContext.BridleMobCatchFail(0, itemId));
                    }
                }
                c.announce(WvsContext.enableActions());
                break;
            case 2270007:
                if (mob.getId() == 9300191) {
                    if (mob.getHp() < ((mob.getMaxHp() / 10) * 3)) {
                        chr.getMap().announce(MobPool.CatchEffect(monsterid, itemId, (byte) 1));
                        mob.getMap().killMonster(mob, null, false);
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
                        MapleInventoryManipulator.addFromDrop(c, new Item(2109003, (short) 1), false);
                    } else {
                        c.announce(WvsContext.BridleMobCatchFail(0, itemId));
                    }
                }
                c.announce(WvsContext.enableActions());
                break;
            case 2270004:
                if (mob.getId() == 9300175) {
                    if (mob.getHp() < ((mob.getMaxHp() / 10) * 4)) {
                        chr.getMap().announce(MobPool.CatchEffect(monsterid, itemId, (byte) 1));
                        mob.getMap().killMonster(mob, null, false);
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
                        MapleInventoryManipulator.addFromDrop(c, new Item(4001169, (short) 1), false);
                    } else {
                        c.announce(WvsContext.BridleMobCatchFail(0, itemId));
                    }
                }
                c.announce(WvsContext.enableActions());
                break;
            case 2270008:
                if (mob.getId() == 9500336) {
                    if ((abm.getLastSpam(10) + 3000) < System.currentTimeMillis()) {
                        abm.spam(10);
                        chr.getMap().announce(MobPool.CatchEffect(monsterid, itemId, (byte) 1));
                        mob.getMap().killMonster(mob, null, false);
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
                        MapleInventoryManipulator.addFromDrop(c, new Item(2022323, (short) 1), false);
                    } else {
                        chr.message("You cannot use the Fishing Net yet.");
                    }
                }
                c.announce(WvsContext.enableActions());
                break;
            default:
                System.out.println("UseCatchItemHandler: \r\n" + slea.toString());
                c.announce(WvsContext.enableActions());
                break;
        }
    }
}
