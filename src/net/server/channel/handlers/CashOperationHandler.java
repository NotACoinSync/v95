package net.server.channel.handlers;

import java.rmi.RemoteException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleRing;
import client.MessageType;
import client.autoban.AutobanFactory;
import client.inventory.*;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import server.ItemData;
import server.ItemInformationProvider;
import server.MapleInventoryManipulator;
import server.cashshop.*;
import tools.DatabaseConnection;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CashShopPacket;
import tools.packets.CashShopPacket.CashItemReq;
import tools.packets.WvsContext;

public final class CashOperationHandler extends AbstractMaplePacketHandler {

    private void addPurchase(CashItemData cItem) {
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO bestitems(sn, gender, category, purchases) VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE purchases=purchases+" + cItem.nCount)) {
            ps.setInt(1, cItem.sn);
            ps.setInt(2, cItem.nCommodityGender);
            ps.setInt(3, CashItemFactory.get_category_from_SN(cItem.sn));
            ps.setInt(4, cItem.nCount);
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
        }
        CashItemFactory.addBestItem(cItem);
        LimitedGood good = CashItemFactory.getGoodFromSN(cItem.sn);
        if (good == null) {
            return;
        }
        good.nRemainCount -= cItem.nCount;
        try {
            ChannelServer.getInstance().getWorldInterface().updateLimitedGood(cItem.sn, good.nRemainCount);
        } catch (RemoteException | NullPointerException ex) {
            Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
        }
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE limitedgoods SET remainCount = ? WHERE id = ?")) {
            ps.setInt(1, good.nRemainCount);
            ps.setInt(2, good.id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
        }
    }

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {// http://i.imgur.com/NLSOIfi.png
        MapleCharacter chr = c.getPlayer();
        CashShop cs = chr.getCashShop();
        if (!cs.isOpened()) {
            c.announce(WvsContext.enableActions());
            AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to send CashOperation packet while not in CashShop");
            return;
        }
        final int action = iPacket.readByte();
        if (action == CashItemReq.Buy || action == CashItemReq.BuyPackage) {
            iPacket.readByte();
            final int useNX = iPacket.readInt();
            final int snCS = iPacket.readInt();
            CashItemData cItem = CashItemFactory.getItem(snCS);
            if (cItem == null) {
                AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to buy null item on sn " + snCS + " for " + useNX + " nx type");
                return;
            }
            if (!canBuy(cItem, cs.getCash(useNX))) {
                c.getPlayer().getClient().announce(WvsContext.BroadcastMsg.encode(1, "You may not purchase this item."));
                c.announce(CashShopPacket.QueryCashResult(c.getPlayer()));
                return;
            }
            /*if(!cItem.bOnSale || cs.getCash(useNX) < cItem.nPrice){
				c.getPlayer().getClient().announce(CWvsContext.BroadcastMsg.encode(1, "You may not purchase this item."));
				c.announce(MaplePacketCreator.QueryCashResult(c.getPlayer()));
				return;
			}*/
            if (action == CashItemReq.Buy) { // Item
                Item item = cItem.toItem();
                cs.addToInventory(item);
                c.announce(CashShopPacket.CashItemResult.showBoughtCashItem(item, c.getAccID()));
            } else { // Package
                List<Item> cashPackage = CashItemFactory.getPackage(cItem.nItemid);
                for (Item item : cashPackage) {
                    cs.addToInventory(item);
                }
                c.announce(CashShopPacket.CashItemResult.showBoughtCashPackage(cashPackage, c.getAccID()));
            }
            Logger.log(LogType.INFO, LogFile.BUY_CASH_ITEM, c.getAccountName(), c.getPlayer().getName() + " bought " + cItem.nItemid + " with sn " + cItem.sn + " for " + getAdjustedPrice(cItem));
            cs.gainCash(useNX, -getAdjustedPrice(cItem));
            c.announce(CashShopPacket.QueryCashResult(chr));
            addPurchase(cItem);
        } else if (action == CashItemReq.Gift) {// fuck genders
            // 47 21 42 01 [D9 A0 98 00] 00 [06 00 41 6C 66 72 65 64] [02 00 61 0A]
            iPacket.readInt(); // Birthday
            CashItemData cItem = CashItemFactory.getItem(iPacket.readInt());
            iPacket.readByte();
            Map<String, String> recipient = MapleCharacter.getCharacterFromDatabase(iPacket.readMapleAsciiString());
            String message = iPacket.readMapleAsciiString();
            if (!canBuy(cItem, cs.getCash(4))) {
                c.getPlayer().getClient().announce(WvsContext.BroadcastMsg.encode(1, "You may not purchase this item."));
                c.announce(CashShopPacket.QueryCashResult(c.getPlayer()));
                return;
            }
            if (message.length() < 1 || message.length() > 73) {
                AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to gift with an invalid length message");
                return;
            }
            if (recipient == null) {
                c.announce(CashShopPacket.CashItemResult.showCashShopMessage((byte) 0xA9));
                return;
            } else if (recipient.get("accountid").equals(String.valueOf(c.getAccID()))) {
                c.announce(CashShopPacket.CashItemResult.showCashShopMessage((byte) 0xA8));
                return;
            }
            cs.gift(Integer.parseInt(recipient.get("id")), chr.getName(), message, cItem.sn);
            c.announce(CashShopPacket.CashItemResult.showGiftSucceed(recipient.get("name"), cItem));
            Logger.log(LogType.INFO, LogFile.BUY_CASH_ITEM, c.getAccountName(), c.getPlayer().getName() + " gifted " + cItem.nItemid + " with sn " + cItem.sn + " for " + getAdjustedPrice(cItem) + " to " + recipient.get("name"));
            cs.gainCash(4, -getAdjustedPrice(cItem));
            c.announce(CashShopPacket.QueryCashResult(chr));
            try {
                chr.sendNote(recipient.get("name"), chr.getName() + " has sent you a gift! Go check out the Cash Shop.", (byte) 0); // fame or not
            } catch (SQLException ex) {
                Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
            }
            MapleCharacter receiver = c.getChannelServer().getPlayerStorage().getCharacterByName(recipient.get("name"));
            if (receiver != null) {
                receiver.showNote();
            }
            addPurchase(cItem);
        } else if (action == CashItemReq.SetWish) { // Modify wish list
            cs.clearWishList();
            for (byte i = 0; i < 10; i++) {
                int sn = iPacket.readInt();
                CashItemData cItem = CashItemFactory.getItem(sn);
                if (cItem != null && cItem.bOnSale && sn != 0) {
                    if (!canBuy(cItem)) {
                        c.getPlayer().getClient().announce(WvsContext.BroadcastMsg.encode(1, "You may not add this item to your wishlist."));
                        c.announce(CashShopPacket.QueryCashResult(c.getPlayer()));
                        return;
                    }
                    cs.addToWishList(sn);
                }
            }
            c.announce(CashShopPacket.CashItemResult.showWishList(chr, true));
        } else if (action == CashShopPacket.CashItemReq.IncSlotCount) { // Increase Inventory Slots
            /*slea.skip(1);
			int cash = slea.readInt();
			byte mode = slea.readByte();
			if(mode == 0){
				byte type = slea.readByte();
				if(cs.getCash(cash) < 4000)return; 
				if(chr.gainSlots(type, 4, false)){
					c.announce(MaplePacketCreator.showBoughtInventorySlots(type, chr.getSlots(type)));
					cs.gainCash(cash, -4000);
					c.announce(MaplePacketCreator.QueryCashResult(chr));
				}
			}else{
				CashModifiedData cItem = CashItemFactory.getItem(slea.readInt());
				int type = (cItem.nItemid - 9110000) / 1000;
				if(!canBuy(cItem, cs.getCash(cash)))return; 
				if(chr.gainSlots(type, 8, false)){
					c.announce(MaplePacketCreator.showBoughtInventorySlots(type, chr.getSlots(type)));
					cs.gainCash(cash, -cItem.nPrice);
					c.announce(MaplePacketCreator.QueryCashResult(chr));
				}
			}*/
            c.getPlayer().dropMessage(MessageType.POPUP, "Currently unavailable. Please try again another time.");
            c.announce(WvsContext.enableActions());
            c.announce(CashShopPacket.QueryCashResult(c.getPlayer()));
        } else if (action == CashShopPacket.CashItemReq.IncTrunkCount) { // Increase Storage Slots
            iPacket.skip(1);
            int cash = iPacket.readInt();
            byte mode = iPacket.readByte();
            if (mode == 0) {
                if (cs.getCash(cash) < 4000) {
                    return;
                }
                if (chr.getStorage().gainSlots(4)) {
                    c.announce(CashShopPacket.CashItemResult.showBoughtStorageSlots(chr.getStorage().getSlots()));
                    cs.gainCash(cash, -4000);
                    c.announce(CashShopPacket.QueryCashResult(chr));
                }
            } else {
                CashItemData cItem = CashItemFactory.getItem(iPacket.readInt());
                if (!canBuy(cItem, cs.getCash(cash))) {
                    return;
                }
                if (chr.getStorage().gainSlots(8)) {
                    c.announce(CashShopPacket.CashItemResult.showBoughtStorageSlots(chr.getStorage().getSlots()));
                    Logger.log(LogType.INFO, LogFile.BUY_CASH_ITEM, c.getAccountName(), c.getPlayer().getName() + " bought " + cItem.nItemid + " with sn " + cItem.sn + " for " + getAdjustedPrice(cItem) + " to increase trunk slots");
                    cs.gainCash(cash, -getAdjustedPrice(cItem));
                    c.announce(CashShopPacket.QueryCashResult(chr));
                }
            }
        } else if (action == CashShopPacket.CashItemReq.IncCharSlotCount) { // Increase Character Slots
            iPacket.skip(1);
            int cash = iPacket.readInt();
            CashItemData cItem = CashItemFactory.getItem(iPacket.readInt());
            if (!canBuy(cItem, cs.getCash(cash))) {
                return;
            }
            if (c.gainCharacterSlot()) {
                c.announce(CashShopPacket.CashItemResult.showBoughtCharacterSlot(c.getCharacterSlots()));
                Logger.log(LogType.INFO, LogFile.BUY_CASH_ITEM, c.getAccountName(), c.getPlayer().getName() + " bought " + cItem.nItemid + " with sn " + cItem.sn + " for " + getAdjustedPrice(cItem) + " to increase character slots");
                cs.gainCash(cash, -getAdjustedPrice(cItem));
                c.announce(CashShopPacket.QueryCashResult(chr));
            }
        } else if (action == CashShopPacket.CashItemReq.MoveLtoS) { // Take from Cash Inventory
            Item item = cs.findByCashId(iPacket.readInt());
            if (item == null) {
                AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to move an invalid item from Locker to Inventory");
                return;
            }
            if (chr.getInventory(ItemInformationProvider.getInstance().getInventoryType(item.getItemId())).addItem(item) != -1) {
                cs.removeFromInventory(item);
                ItemFactory.updateItemOwner(chr, item, c.getPlayer().getCashShop().getFactory());
                c.announce(CashShopPacket.CashItemResult.takeFromCashInventory(item));
                if (item instanceof Equip) {
                    Equip equip = (Equip) item;
                    if (equip.getRingId() >= 0) {
                        MapleRing ring = MapleRing.loadFromDb(equip.getRingId());
                        if (ring.getItemId() > 1112012) {
                            chr.addFriendshipRing(ring);
                        } else {
                            chr.addCrushRing(ring);
                        }
                    }
                }
            }
        } else if (action == CashShopPacket.CashItemReq.MoveStoL) { // Put into Cash Inventory
            int cashId = iPacket.readInt();
            iPacket.skip(4);
            MapleInventory mi = chr.getInventory(MapleInventoryType.getByType(iPacket.readByte()));
            Item item = mi.findByCashId(cashId);
            if (item == null) {
                AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to move an invalid item from Inventory to Locker");
                return;
            }
            ItemData data = ItemInformationProvider.getInstance().getItemData(item.getItemId());
            if (!data.isCash) {
                AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to move an non-cash item from Inventory to Locker");
                return;
            }
            cs.addToInventory(item);
            mi.removeSlot(item.getPosition());
            ItemFactory.updateItemOwner(chr, item, c.getPlayer().getCashShop().getFactory());
            c.announce(CashShopPacket.CashItemResult.putIntoCashInventory(item, c.getAccID()));
        } else if (action == CashItemReq.Couple) { // crush ring (action 28)
            iPacket.readInt();// Birthday
            // if (checkBirthday(c, birthday)) { //We're using a default birthday, so why restrict rings to only people who know of it?
            int toCharge = iPacket.readInt();
            int SN = iPacket.readInt();
            String recipient = iPacket.readMapleAsciiString();
            String text = iPacket.readMapleAsciiString();
            CashItemData ring = CashItemFactory.getItem(SN);
            MapleCharacter partner = c.getChannelServer().getPlayerStorage().getCharacterByName(recipient);
            if (partner == null) {
                chr.getClient().announce(WvsContext.BroadcastMsg.encode(1, "The partner you specified cannot be found.\r\nPlease make sure your partner is online and in the same channel."));
            } else {
                /*  if (partner.getGender() == chr.getGender()) {
				 chr.dropMessage("You and your partner are the same gender, please buy a friendship ring.");
				 return;
				 }*/
                if (ring.toItem() instanceof Equip) {
                    Equip item = (Equip) ring.toItem();
                    int ringid = MapleRing.createRing(ring.nItemid, chr, partner);
                    item.setRingId(ringid);
                    cs.addToInventory(item);
                    c.announce(CashShopPacket.CashItemResult.showBoughtCashItem(item, c.getAccID()));
                    cs.gift(partner.getId(), chr.getName(), text, item.getOldSN(), (ringid + 1));
                    Logger.log(LogType.INFO, LogFile.BUY_CASH_ITEM, c.getAccountName(), c.getPlayer().getName() + " bought " + item.getItemId() + " with sn " + SN + " for " + getAdjustedPrice(ring) + ". Couple ring with " + recipient + " text: " + text);
                    cs.gainCash(toCharge, -getAdjustedPrice(ring));
                    chr.addCrushRing(MapleRing.loadFromDb(ringid));
                    try {
                        chr.sendNote(partner.getName(), text, (byte) 1);
                    } catch (SQLException ex) {
                        Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
                    }
                    partner.showNote();
                    addPurchase(ring);
                }
            }
            /* } else {
			 chr.dropMessage("The birthday you entered was incorrect.");
			 }*/
            c.announce(CashShopPacket.QueryCashResult(c.getPlayer()));
        } else if (action == CashItemReq.BuyNormal) { // everything is 1 meso...
            int itemId = CashItemFactory.getItem(iPacket.readInt()).nItemid;
            if (chr.getMeso() > 0) {
                if (itemId == 4031180 || itemId == 4031192 || itemId == 4031191) {
                    chr.gainMeso(-1, false);
                    MapleInventoryManipulator.addFromDrop(c, new Item(itemId, (short) 1), false);
                    c.announce(CashShopPacket.CashItemResult.showBoughtQuestItem(itemId));
                }
            }
            c.announce(CashShopPacket.QueryCashResult(c.getPlayer()));
        } else if (action == CashItemReq.FriendShip) { // Friendship :3
            iPacket.readInt(); // Birthday
            // if (checkBirthday(c, birthday)) {
            int payment = iPacket.readByte();
            iPacket.skip(3); // 0s
            int snID = iPacket.readInt();
            CashItemData ring = CashItemFactory.getItem(snID);
            String sentTo = iPacket.readMapleAsciiString();
            int available = iPacket.readShort() - 1;
            String text = iPacket.readAsciiString(available);
            iPacket.readByte();
            MapleCharacter partner = c.getChannelServer().getPlayerStorage().getCharacterByName(sentTo);
            if (partner == null) {
                chr.dropMessage("The partner you specified cannot be found.\r\nPlease make sure your partner is online and in the same channel.");
            } else {
                // Need to check to make sure its actually an equip and the right SN...
                if (ring.toItem() instanceof Equip) {
                    Equip item = (Equip) ring.toItem();
                    int ringid = MapleRing.createRing(ring.nItemid, chr, partner);
                    item.setRingId(ringid);
                    cs.addToInventory(item);
                    c.announce(CashShopPacket.CashItemResult.showBoughtCashItem(item, c.getAccID()));
                    cs.gift(partner.getId(), chr.getName(), text, item.getOldSN(), (ringid + 1));
                    Logger.log(LogType.INFO, LogFile.BUY_CASH_ITEM, c.getAccountName(), c.getPlayer().getName() + " bought " + item.getItemId() + " with sn " + snID + " for " + getAdjustedPrice(ring) + ". Friendship ring with " + sentTo + " text: " + text);
                    cs.gainCash(payment, -getAdjustedPrice(ring));
                    chr.addFriendshipRing(MapleRing.loadFromDb(ringid));
                    try {
                        chr.sendNote(partner.getName(), text, (byte) 1);
                    } catch (SQLException ex) {
                        Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
                    }
                    partner.showNote();
                    addPurchase(ring);
                }
            }
            c.announce(CashShopPacket.QueryCashResult(c.getPlayer()));
        }
    }

    public static boolean canBuy(CashItemData item, int cash) {
        LimitedGood lg = CashItemFactory.getGoodFromSN(item.sn);
        if (lg != null) {
            if (lg.nRemainCount < item.nCount) {
                return false;
            }
            if (lg.getState() != 0) {
                return false;
            }
            int stockState = lg.getStockState(item);
            if (stockState == StockState.NoStock || stockState == StockState.NotAvailableTime) {
                return false;
            }
        }
        return item != null && item.bOnSale && getAdjustedPrice(item) <= cash && !blocked(item.nItemid);
    }

    private static int getAdjustedPrice(CashItemData item) {
        double price = item.nPrice;
        for (CategoryDiscount cd : CashItemFactory.categoryDiscount) {
            if (cd.aCategory == CashItemFactory.get_category_from_SN(item.sn)) {
                if (cd.nCategorySub == CashItemFactory.get_categorysub_from_SN(item.sn)) {
                    price -= price * (cd.nDiscountRate / (double) 100);
                }
            }
        }
        return (int) price;
    }

    @Deprecated
    public static boolean canBuy(CashItemData item) {
        return item != null && item.bOnSale && !blocked(item.nItemid);
    }

    public static boolean blocked(int id) {
        if (id >= 5211000 && id <= 5211018 || id >= 5211037 && id <= 5211049) {
            return true;// exp
        }
        if (id >= 5360000 && id <= 5360042) {
            return true;// drop
        }
        switch (id) { // All 2x exp cards
            case 5211000:
            case 5211004:
            case 5211005:
            case 5211006:
            case 5211007:
            case 5211008:
            case 5211009:
            case 5211010:
            case 5211011:
            case 5211012:
            case 5211013:
            case 5211014:
            case 5211015:
            case 5211016:
            case 5211017:
            case 5211018:
            case 5211037:
            case 5211038:
            case 5211039:
            case 5211040:
            case 5211041:
            case 5211042:
            case 5211043:
            case 5211044:
            case 5211045:
            case 5211049:
            case 5220000:// Gachapon Ticket
            case 5220010:// slot machines, just incase.
            case 5220020:// Net cafe, just incase.
            case 5451000:// Remote Gachapon
            case 5431000:// Maple Life(A-Type)
            case 5432000:// Maple Life (B-Type)
            case 5510000:// Wheel of Destiny
            case 5130000:// Safety Charm
                return true;
            default:
                return false;
        }
    }
}
