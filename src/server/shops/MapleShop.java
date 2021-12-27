package server.shops;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import client.MapleClient;
import client.MessageType;
import client.autoban.AutobanFactory;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.ItemConstants;
import server.ItemData;
import server.ItemInformationProvider;
import server.MapleInventoryManipulator;
import tools.DatabaseConnection;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CField.ShopDlg;

public class MapleShop {

    private static final Set<Integer> rechargeableItems = new LinkedHashSet<>();
    private int id;
    private int npcId;
    private List<MapleShopItem> items;

    static {
        for (int i = 2070000; i < 2070017; i++) {
            rechargeableItems.add(i);
        }
        rechargeableItems.add(2331000);// Blaze Capsule
        rechargeableItems.add(2332000);// Glaze Capsule
        rechargeableItems.add(2070018);
        rechargeableItems.remove(2070014);
        for (int i = 2330000; i <= 2330005; i++) {
            rechargeableItems.add(i);
        }
    }

    private MapleShop(int id, int npcId) {
        this.id = id;
        this.npcId = npcId;
        items = new ArrayList<>();
    }

    private void addItem(MapleShopItem item) {
        items.add(item);
    }

    private MapleShopItem findBySlot(short slot) {
        return items.get(slot);
    }

    public int getNpcId() {
        return npcId;
    }

    public int getId() {
        return id;
    }

    public void sendShop(MapleClient c) {
        if (c.getPlayer().getScriptDebug()) {
            c.getPlayer().dropMessage(MessageType.MAPLETIP, "Shop: " + this.id + " with npc: " + this.npcId);
        }
        c.getPlayer().setShop(this);
        c.announce(ShopDlg.open(c, getNpcId(), items));
    }

    public void buy(MapleClient c, short slot, int itemId, short quantity, int discountPrice) {
        MapleShopItem item = findBySlot(slot);
        int Price = item.getPrice();
        int TokenPrice = item.getTokenPrice();
        if (item != null) {
            if (item.getItemId() != itemId) {
                c.announce(ShopDlg.transact(ShopDlg.ShopRes.BuyUnknown));
                AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Gave incorrect itemid for slot");
                return;
            }
        } else {
            c.announce(ShopDlg.transact(ShopDlg.ShopRes.BuyUnknown));
            AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Trying to buy null item.");
            return;
        }
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        if (item != null && Price >= 0) {
            if (discountPrice != Price && discountPrice > -1) {
                Price = discountPrice;
            }
            if (c.getPlayer().getMeso() >= (long) Price * quantity) {
                if (c.getPlayer().canHoldItem(new Item(itemId, quantity))) {
                    Item i = null;
                    if (ii.getInventoryType(itemId) == MapleInventoryType.EQUIP) {
                        i = ii.getEquipById(itemId);
                    } else {
                        i = new Item(itemId, quantity);
                    }
                    if (!ItemConstants.isRechargable(itemId)) { // Pets can't be bought from shops
                        MapleInventoryManipulator.addFromDrop(c, i, false);
                        c.getPlayer().gainMeso(-(Price * quantity), false);
                    } else {
                        short slotMax = ii.getItemData(item.getItemId()).getSlotMax(c);
                        quantity = slotMax;
                        i.setQuantity(quantity);
                        MapleInventoryManipulator.addFromDrop(c, i, false);
                        c.getPlayer().gainMeso(-Price, false);
                    }
                    c.announce(ShopDlg.transact(ShopDlg.ShopRes.BuySuccess));
                } else {
                    c.announce(ShopDlg.transact(ShopDlg.ShopRes.BuyNoStock));
                }
            } else {
                c.announce(ShopDlg.transact(ShopDlg.ShopRes.BuyNoMoney));
            }
        } else if (item != null && TokenPrice > 0) {
            if (discountPrice != TokenPrice && discountPrice > -1) {
                TokenPrice = discountPrice;
            }
            if (c.getPlayer().getInventory(ii.getInventoryType(item.getTokenItemID())).countById(item.getTokenItemID()) >= (long) TokenPrice * quantity) {
                if (c.getPlayer().canHoldItem(new Item(itemId, quantity))) {
                    Item i = null;
                    if (ii.getInventoryType(itemId) == MapleInventoryType.EQUIP) {
                        i = ii.getEquipById(itemId);
                    } else {
                        i = new Item(itemId, quantity);
                    }
                    if (!ItemConstants.isRechargable(itemId)) {
                        MapleInventoryManipulator.addFromDrop(c, i, false);
                        MapleInventoryManipulator.removeById(c, ii.getInventoryType(item.getTokenItemID()), item.getTokenItemID(), TokenPrice * quantity, true, false);
                    } else {
                        short slotMax = ii.getItemData(item.getItemId()).getSlotMax(c);
                        quantity = slotMax;
                        i.setQuantity(quantity);
                        MapleInventoryManipulator.addFromDrop(c, i, false);
                        MapleInventoryManipulator.removeById(c, ii.getInventoryType(item.getTokenItemID()), item.getTokenItemID(), TokenPrice * quantity, true, false);
                    }
                    c.announce(ShopDlg.transact(ShopDlg.ShopRes.BuySuccess));
                } else {
                    c.announce(ShopDlg.transact(ShopDlg.ShopRes.BuyNoStock));
                }
            } else {
                c.announce(ShopDlg.transact(ShopDlg.ShopRes.BuyNoToken));
            }
        } else {
            c.announce(ShopDlg.transact(ShopDlg.ShopRes.BuyUnknown));
            AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Trying to packet edit Price when buy item in shop.");
        }
    }

    public void sell(MapleClient c, MapleInventoryType type, short slot, short quantity) {
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        Item item = c.getPlayer().getInventory(type).getItem(slot);
        if (item == null) {
            c.announce(ShopDlg.transact(ShopDlg.ShopRes.SellUnkonwn));
            AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Trying to sell null item.");
            return;
        }
        if (ItemConstants.isRechargable(item.getItemId())) {
            quantity = item.getQuantity();
        }
        if (quantity < 0) {
            c.announce(ShopDlg.transact(ShopDlg.ShopRes.SellNoStock));
            AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Trying to sell null quantity.");
            return;
        }
        short iQuant = item.getQuantity();
        if (quantity <= iQuant && iQuant >= 0) {
            ItemData data = ii.getItemData(item.getItemId());
            double price;
            if (ItemConstants.isRechargable(item.getItemId())) {
                price = data.wholePrice / (double) ii.getItemData(item.getItemId()).getSlotMax(c);
            } else {
                price = data.price;
            }
            int recvMesos = (int) Math.max(Math.ceil(price * quantity), 0);
            if (recvMesos >= 0) {
                c.getPlayer().gainMeso(recvMesos, false);
            }
            MapleInventoryManipulator.removeItem(c, type, (byte) slot, quantity, true, false);
            c.announce(ShopDlg.transact(ShopDlg.ShopRes.SellSuccess));
        } else {
            c.announce(ShopDlg.transact(ShopDlg.ShopRes.SellIncorrectRequest));
        }
    }

    public void recharge(MapleClient c, short slot) {
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        Item item = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);
        if (item == null || !ItemConstants.isRechargable(item.getItemId())) {
            c.announce(ShopDlg.transact(ShopDlg.ShopRes.RechargeUnknown));
            AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Trying to recharge null item.");
            return;
        }
        ItemData itemData = ii.getItemData(item.getItemId());
        short slotMax = ii.getItemData(item.getItemId()).getSlotMax(c);
        if (item.getQuantity() < 0) {
            c.announce(ShopDlg.transact(ShopDlg.ShopRes.RechargeNoStock));
            AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Trying to recharge null quantity.");
            return;
        }
        if (item.getQuantity() < slotMax) {
            int price = (int) Math.round(itemData.unitPrice * (slotMax - item.getQuantity()));
            if (c.getPlayer().getMeso() >= price) {
                item.setQuantity(slotMax);
                c.getPlayer().forceUpdateItem(item);
                c.getPlayer().gainMeso(-price, false, true, false);
                c.announce(ShopDlg.transact(ShopDlg.ShopRes.RechargeSuccess));
            } else {
                c.announce(ShopDlg.transact(ShopDlg.ShopRes.RechargeNoMoney));
            }
        } else {
            c.announce(ShopDlg.transact(ShopDlg.ShopRes.RechargeIncorrectRequest));
        }
    }

    public static MapleShop createFromDB(int id, boolean isShopId) {
        MapleShop ret = null;
        int shopId;
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps;
            if (isShopId) {
                ps = con.prepareStatement("SELECT * FROM shops WHERE shopid = ?");
            } else {
                ps = con.prepareStatement("SELECT * FROM shops WHERE npcid = ?");
            }
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                shopId = rs.getInt("shopid");
                ret = new MapleShop(shopId, rs.getInt("npcid"));
                rs.close();
                ps.close();
            } else {
                rs.close();
                ps.close();
                return null;
            }
            ps = con.prepareStatement("SELECT * FROM shopitems WHERE shopid = ? ORDER BY position ASC");
            ps.setInt(1, shopId);
            rs = ps.executeQuery();
            // List<Integer> recharges = new ArrayList<>(rechargeableItems);
            while (rs.next()) {
                ItemData data = ItemInformationProvider.getInstance().getItemData(rs.getInt("itemid"));
                if (data == null || !data.exists) {
                    Logger.log(LogType.ERROR, LogFile.GENERAL_ERROR, rs.getInt("itemid") + " in shop " + shopId + " is an invalid item.");
                    continue;
                }
                MapleShopItem item = new MapleShopItem(rs);
                ret.addItem(item);
            }
            re:
            for (Integer recharge : rechargeableItems) {
                for (MapleShopItem item : ret.items) {
                    if (item.getItemId() == recharge) {
                        continue re;
                    }
                }
                ret.addItem(new MapleShopItem((short) 1000, recharge));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
        }
        return ret;
    }
}
