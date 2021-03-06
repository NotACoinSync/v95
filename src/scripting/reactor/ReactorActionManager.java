package scripting.reactor;

import java.awt.Point;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import scripting.AbstractPlayerInteraction;
import server.ItemInformationProvider;
import server.life.MapleLifeFactory;
import server.life.MapleNPC;
import server.maps.MapMonitor;
import server.maps.ReactorDropEntry;
import server.reactors.MapleReactor;
import tools.packets.CField.NpcPool;

/**
 * @author Lerk
 */
public class ReactorActionManager extends AbstractPlayerInteraction {

    private MapleReactor reactor;
    private MapleClient client;

    public ReactorActionManager(MapleClient c, MapleReactor reactor) {
        super(c);
        this.reactor = reactor;
        this.client = c;
    }

    public void dropItems() {
        dropItems(false, 0, 0, 0, 0);
    }

    public void dropItems(boolean meso, int mesoChance, int minMeso, int maxMeso) {
        dropItems(meso, mesoChance, minMeso, maxMeso, 0);
    }

    public void dropItems(boolean meso, int mesoChance, int minMeso, int maxMeso, int minItems) {
        List<ReactorDropEntry> chances = null;
        if (reactor.getAction() != null) {
            chances = ReactorScriptManager.getInstance().getDrops(reactor.getAction());
        }
        if (chances == null || chances.isEmpty()) {
            chances = ReactorScriptManager.getInstance().getDrops(reactor.getId());
        }
        List<ReactorDropEntry> items = new LinkedList<>();
        int numItems = 0;
        if (meso && Math.random() < (1 / (double) mesoChance)) {
            items.add(new ReactorDropEntry(0, mesoChance, -1));
        }
        Iterator<ReactorDropEntry> iter = chances.iterator();
        while (iter.hasNext()) {
            ReactorDropEntry d = iter.next();
            double chance = d.chance;
            if (Math.random() < ((double) 1 / chance)) {
                if (!getPlayer().needQuestItem(d.questid, d.itemId)) {
                    continue;
                }
                numItems++;
                items.add(d);
            }
        }
        while (meso && items.size() < minItems) {
            items.add(new ReactorDropEntry(0, mesoChance, -1));
            numItems++;
        }
        java.util.Collections.shuffle(items);
        final Point dropPos = new Point(reactor.getPosition());
        dropPos.x -= (12 * numItems);
        int delay = 0;
        for (ReactorDropEntry d : items) {
            if (d.itemId == 0) {
                int range = maxMeso - minMeso;
                int displayDrop = (int) (Math.random() * range) + minMeso;
                int mesoDrop = (int) (displayDrop * client.getPlayer().getStats().getMesoRate());
                reactor.getMap().spawnMesoDrop(mesoDrop, dropPos, reactor, client.getPlayer(), false, (byte) 0);
            } else {
                Item drop;
                ItemInformationProvider ii = ItemInformationProvider.getInstance();
                if (ii.getInventoryType(d.itemId) != MapleInventoryType.EQUIP) {
                    drop = new Item(d.itemId, (short) 0, (short) 1);
                } else {
                    drop = ii.randomizeStats((Equip) ii.getEquipById(d.itemId));
                }
                reactor.getMap().spawnItemDrop(reactor, getPlayer(), drop, dropPos, false, false, delay);
            }
            delay += 200;
            // http://i.imgur.com/DEsI0og.png
            dropPos.x += 25;
        }
    }

    public void dropItem(int itemid, int quantity) {
        Item drop;
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        if (ii.getInventoryType(itemid) != MapleInventoryType.EQUIP) {
            drop = new Item(itemid, (short) 0, (short) 1);
        } else {
            drop = ii.randomizeStats((Equip) ii.getEquipById(itemid));
        }
        final Point dropPos = reactor.getPosition();
        reactor.getMap().spawnItemDrop(reactor, getPlayer(), drop, dropPos, true, false);
    }

    public void spawnMonster(int id) {
        spawnMonster(id, 1, getPosition());
    }

    public void createMapMonitor(int mapId, String portal) {
        new MapMonitor(client.getChannelServer().getMap(mapId), portal);
    }

    public void spawnMonster(int id, int qty) {
        spawnMonster(id, qty, getPosition());
    }

    public void spawnMonster(int id, int qty, int x, int y) {
        spawnMonster(id, qty, new Point(x, y));
    }

    private void spawnMonster(int id, int qty, Point pos) {
        for (int i = 0; i < qty; i++) {
            reactor.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(id), pos);
        }
    }

    public Point getPosition() {
        Point pos = reactor.getPosition();
        pos.y -= 10;
        return pos;
    }

    public void spawnNpc(int npcId) {
        spawnNpc(npcId, getPosition());
    }

    public void spawnNpc(int npcId, Point pos) {
        MapleNPC npc = MapleLifeFactory.getNPC(npcId);
        if (npc != null) {
            npc.setPosition(pos);
            npc.setCy(pos.y);
            npc.setRx0(pos.x + 50);
            npc.setRx1(pos.x - 50);
            npc.setFh(reactor.getMap().getMapData().getFootholds().findBelow(pos).getId());
            reactor.getMap().addMapObject(npc);
            reactor.getMap().announce(NpcPool.NpcEnterField(npc));
        }
    }

    public MapleReactor getReactor() {
        return reactor;
    }

    public void spawnFakeMonster(int id) {
        reactor.getMap().spawnFakeMonsterOnGroundBelow(MapleLifeFactory.getMonster(id), getPosition());
    }

    public void reviveReactor() {
        getReactor().setAlive(true);
    }

    public void setCanSetAlive(boolean canSetAlive) {
        getReactor().setCanSetAlive(canSetAlive);
    }

    public void gainExp(MapleCharacter player, int exp, double multiplier, boolean show, String logData) {
        logData += "From Reactor: " + reactor.getId() + " Action: " + reactor.getAction();
        super.gainExp(player, exp, multiplier, show, logData);
    }

    public Item gainItem(MapleCharacter mc, int id, short quantity, boolean randomStats, boolean showMessage, long expires, boolean checkSpace, String gainLog) {
        gainLog += " Reactor: " + reactor.getId() + " Action: " + reactor.getAction();
        return super.gainItem(mc, id, quantity, randomStats, showMessage, expires, checkSpace, gainLog);
    }
}
