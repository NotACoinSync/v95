package server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import client.MapleClient;
import client.inventory.Item;
import client.inventory.ItemFactory;
import client.inventory.MapleInventoryType;
import constants.ItemConstants;
import tools.DatabaseConnection;
import tools.Pair;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CField.TrunkDlg;

public class MapleStorage {

    private int id;
    private List<Item> items;
    private int meso;
    private byte slots;
    private Map<MapleInventoryType, List<Item>> typeItems = new HashMap<>();

    private MapleStorage(int id, byte slots, int meso) {
        this.id = id;
        this.slots = slots;
        this.items = new LinkedList<>();
        this.meso = meso;
    }

    private static MapleStorage create(int id, int world) {
        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO storages (accountid, world, slots, meso) VALUES (?, ?, 4, 0)")) {
                ps.setInt(1, id);
                ps.setInt(2, world);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
        }
        return loadOrCreateFromDB(id, world);
    }

    public static MapleStorage loadOrCreateFromDB(int id, int world) {
        MapleStorage ret = null;
        int storeId;
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT storageid, slots, meso FROM storages WHERE accountid = ? AND world = ?");
            ps.setInt(1, id);
            ps.setInt(2, world);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return create(id, world);
            } else {
                storeId = rs.getInt("storageid");
                ret = new MapleStorage(storeId, (byte) rs.getInt("slots"), rs.getInt("meso"));
                rs.close();
                ps.close();
                for (Pair<Item, MapleInventoryType> item : ItemFactory.STORAGE.loadItems(ret.id, false)) {
                    ret.items.add(item.getLeft());
                }
            }
        } catch (SQLException ex) {
            Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
        }
        return ret;
    }

    public byte getSlots() {
        return slots;
    }

    public boolean gainSlots(int slots) {
        slots += this.slots;
        if (slots <= 48) {
            this.slots = (byte) slots;
            return true;
        }
        return false;
    }

    public void setSlots(byte set) {
        this.slots = set;
    }

    public void saveToDB(Connection con) {
        try {
            try (PreparedStatement ps = con.prepareStatement("UPDATE storages SET slots = ?, meso = ? WHERE storageid = ?")) {
                ps.setInt(1, slots);
                ps.setInt(2, meso);
                ps.setInt(3, id);
                ps.executeUpdate();
            }
            List<Pair<Item, MapleInventoryType>> itemsWithType = new ArrayList<>();
            for (Item item : items) {
                itemsWithType.add(new Pair<>(item, ItemInformationProvider.getInstance().getInventoryType(item.getItemId())));
            }
            ItemFactory.STORAGE.saveItems(itemsWithType, id, con);
        } catch (SQLException ex) {
            Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
        }
    }

    public Item getItem(byte slot) {
        return items.get(slot);
    }

    public Item takeOut(byte slot) {
        Item ret = items.remove(slot);
        MapleInventoryType type = ItemInformationProvider.getInstance().getInventoryType(ret.getItemId());
        typeItems.remove(type, new ArrayList<>(filterItems(type)));
        return ret;
    }

    public void store(MapleClient c, Item item) {
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        MapleInventoryType type = ii.getInventoryType(item.getItemId());
        items.add(item);
        typeItems.put(type, new ArrayList<>(filterItems(type)));
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    private List<Item> filterItems(MapleInventoryType type) {
        List<Item> ret = new LinkedList<>();
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        for (Item item : items) {
            if (ii.getInventoryType(item.getItemId()) == type) {
                ret.add(item);
            }
        }
        return ret;
    }

    public byte getSlot(MapleInventoryType type, byte slot) {
        byte ret = 0;
        for (Item item : items) {
            if (item == typeItems.get(type).get(slot)) {
                return ret;
            }
            ret++;
        }
        return -1;
    }

    public void sendStorage(MapleClient c, int npcId) {
        final ItemInformationProvider ii = ItemInformationProvider.getInstance();
        Collections.sort(items, (Item o1, Item o2) -> {
            if (ii.getInventoryType(o1.getItemId()).getType() < ii.getInventoryType(o2.getItemId()).getType()) {
                return -1;
            } else if (ii.getInventoryType(o1.getItemId()) == ii.getInventoryType(o2.getItemId())) {
                return 0;
            }
            return 1;
        });
        for (MapleInventoryType type : MapleInventoryType.values()) {
            typeItems.put(type, new ArrayList<>(items));
        }
        c.announce(TrunkDlg.encode(npcId, slots, meso, typeItems, items));
    }

    public void sendPutItem(MapleClient c, MapleInventoryType type) {
        if (items != null) {
            c.announce(TrunkDlg.encode(TrunkDlg.PutItems_In, slots, type, items));
        } else {
            c.getPlayer().message("Trying to put null item in Trunk (" + items.size() + " items)");
        }
    }

    public void sendGetItem(MapleClient c, MapleInventoryType type) {
        if (items != null) {
            c.announce(TrunkDlg.encode(TrunkDlg.PutItems_Out, slots, type, items));
        } else {
            c.getPlayer().message("Trying to get null item out Trunk (" + items.size() + " items)");
        }
    }

    public void sendPutMesos(MapleClient c) {
        c.announce(TrunkDlg.encode(TrunkDlg.PutMesos_In, slots, meso));
    }

    public void sendGetMesos(MapleClient c) {
        c.announce(TrunkDlg.encode(TrunkDlg.PutMesos_Out, slots, meso));
    }

    public int getMeso() {
        return meso;
    }

    public void setMeso(int meso) {
        if (meso < 0) {
            throw new RuntimeException();
        }
        this.meso = meso;
    }

    public boolean isFull() {
        return items.size() >= slots;
    }

    public boolean hasRoom(MapleClient c, Item item) {
        int itemid = item.getItemId();
        int quantity = item.getQuantity();
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        double max = ii.getItemData(itemid).getSlotMax(c);// making it a double for math later
        if (isFull() && max == 1) {
            return false;// if the inventory is full, and the maxstack is 1, we can't add it
        }
        boolean rechargeable = ItemConstants.isRechargable(itemid);
        if (isFull() && rechargeable) {
            return false;// bullet/star - Same as ^. stars/bullets don't stack
        }
        int newQ = quantity;
        for (Item i : listById(itemid)) {// check to see if any room exists
            if (i.softEquals(item)) {
                int qLeft = (int) (max - i.getQuantity());
                newQ -= qLeft;
            }
        }
        if (newQ <= 0) {
            return true;// if the current items can store all the quantity
        }
        double required = newQ / max;
        double slotsAvailable = getNumFreeSlot();
        return slotsAvailable >= required;
    }

    public short getNumFreeSlot() {
        if (isFull()) {
            return 0;
        }
        return (short) (slots - items.size());
    }

    public List<Item> listById(int itemId) {
        List<Item> ret = new ArrayList<>();
        for (Item item : items) {
            if (item.getItemId() == itemId) {
                ret.add(item);
            }
        }
        if (ret.size() > 1) {
            Collections.sort(ret);
        }
        return ret;
    }

    public void close() {
        typeItems.clear();
    }
}
