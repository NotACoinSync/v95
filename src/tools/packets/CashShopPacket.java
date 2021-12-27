package tools.packets;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Equip;
import client.inventory.Item;
import java.util.List;
import net.SendOpcode;
import server.cashshop.*;
import tools.Pair;
import tools.StringUtil;
import tools.data.output.MaplePacketLittleEndianWriter;

public class CashShopPacket {

    public static class CashItemReq {

        public static final short Buy = 3, Gift = 4, SetWish = 5, IncSlotCount = 6, IncTrunkCount = 7, IncCharSlotCount = 8, MoveLtoS = 14, MoveStoL = 15, Couple = 30, BuyPackage = 31, BuyNormal = 33, FriendShip = 36;
    }

    public static byte[] ChargeParamResult(MapleClient c) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CASHSHOP_CHARGEPARAM_RESULT.getValue());
        oPacket.writeMapleAsciiString(c.getAccountName()); // m_sNexonClubID
        return oPacket.getPacket();
    }

    public static byte[] QueryCashResult(MapleCharacter player) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CASHSHOP_QUERY_CASH_RESULT.getValue());
        oPacket.writeInt(player.getCashShop().getCash(1)); // NexonCash
        oPacket.writeInt(player.getCashShop().getCash(2)); // MaplePoint
        oPacket.writeInt(player.getCashShop().getCash(4)); // PrepaidNXCash
        return oPacket.getPacket();
    }

    public static class CashItemResult {

        public static final short LimitGoodsCountChanged = 84,
                LoadLocker_Done = 88,
                LoadLocker_Failed = 89,
                LoadGift_Done = 90,
                LoadGift_Failed = 91,
                LoadWish_Done = 92,
                LoadWish_Failed = 93,
                SetWish_Done = 98,
                SetWish_Failed = 99,
                Buy_Done = 100,
                Buy_Failed = 101,
                UseCoupon_Done = 102,
                GiftCoupon_Done = 104,
                UseCoupon_Failed = 105,
                Gift_Done = 107,
                Gift_Failed = 108,
                IncSlotCount_Done = 109,
                IncSlotCount_Failed = 110,
                IncTrunkCount_Done = 111,
                IncTrunkCount_Failed = 112,
                IncCharSlotCount_Done = 113,
                IncCharSlotCount_Failed = 114,
                IncBuyCharCount_Done = 115,
                IncBuyCharCount_Failed = 116,
                EnableEquipSlotExt_Done = 117,
                EnableEquipSlotExt_Failed = 118,
                MoveLtoS_Done = 119,
                MoveLtoS_Failed = 120,
                MoveStoL_Done = 121,
                MoveStoL_Failed = 122,
                Destroy_Done = 123,
                Destroy_Failed = 124,
                Expire_Done = 125,
                Rebate_Done = 150,
                Rebate_Failed = 151,
                Couple_Done = 152,
                Couple_Failed = 153,
                BuyPackage_Done = 154,
                BuyPackage_Failed = 155,
                GiftPackage_Done = 156,
                GiftPackage_Failed = 157,
                BuyNormal_Done = 158,
                BuyNormal_Failed = 159,
                FriendShip_Done = 162,
                FriendShip_Failed = 163,
                FreeCashItem_Done = 170,
                PurchaseRecord = 175,
                PurchaseRecord_Failed = 176,
                NameChangeResBuy_Done = 179,
                TransferWord_Done = 181,
                TransferWord_Failed = 182,
                CashGachaponOpen_Done = 183,
                CashGachaponOpen_Failed = 184,
                CashGachaponCopy_Done = 185,
                CashGachaponCopy_Failed = 186,
                ChangeMaplePoint_Done = 187,
                ChangeMaplePoint_Failed = 188;

        public static byte[] registerLimitGoodsCountChanged(int itemId, int SN, int remainCount) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
            oPacket.write(LimitGoodsCountChanged);
            oPacket.writeInt(itemId);
            oPacket.writeInt(SN);
            oPacket.writeInt(remainCount);
            return oPacket.getPacket();
        }

        public static byte[] showCashInventory(MapleClient c) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
            oPacket.write(LoadLocker_Done);
            oPacket.writeShort(c.getPlayer().getCashShop().getInventory().size());
            for (Item item : c.getPlayer().getCashShop().getInventory()) {
                encodeCashItem(oPacket, item, c.getAccID());
            }
            oPacket.writeShort(c.getPlayer().getStorage().getSlots());
            oPacket.writeShort(c.getCharacterSlots());
            oPacket.writeShort(c.nBuyCharacterCount);// m_nBuyCharacterCount
            oPacket.writeShort(0);// m_nCharacterCount
            return oPacket.getPacket();
        }

        public static byte[] showWishList(MapleCharacter mc, boolean update) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
            if (update) {
                oPacket.write(SetWish_Done);
            } else {
                oPacket.write(LoadWish_Done);
            }
            for (int sn : mc.getCashShop().getWishList()) {
                oPacket.writeInt(sn);
            }
            for (int i = mc.getCashShop().getWishList().size(); i < 10; i++) {
                oPacket.writeInt(0);
            }
            return oPacket.getPacket();
        }

        public static byte[] showBoughtCashItem(Item item, int accountId) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
            oPacket.write(Buy_Done);
            encodeCashItem(oPacket, item, accountId);
            return oPacket.getPacket();
        }

        /*
         00 = Due to an unknown error, failed
         A4 = Due to an unknown error, failed + warpout
         A5 = You don't have enough cash.
         A6 = long as shet msg
         A7 = You have exceeded the allotted limit of price for gifts.
         A8 = You cannot send a gift to your own account. Log in on the char and
         purchase
         A9 = Please confirm whether the character's name is correct.
         AA = Gender restriction!
         * //Skipped a few
         B0 = Wrong Coupon Code
         B1 = Disconnect from CS because of 3 wrong coupon codes < lol
         B2 = Expired Coupon
         B3 = Coupon has been used already
         B4 = Nexon internet cafes? lolfk

         BB = inv full
         C2 = not enough mesos? Lol not even 1 mesos xD
         C4 = Birthday failure bitch
         */
        public static byte[] showCashShopMessage(byte message) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
            oPacket.write(LoadLocker_Failed);// technically each one has its own failed mode.
            oPacket.write(message);
            return oPacket.getPacket();
        }

        public static byte[] showGifts(List<Pair<Item, String>> gifts) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
            oPacket.write(LoadGift_Done);
            oPacket.writeShort(gifts.size());
            for (Pair<Item, String> gift : gifts) {
                encodeCashItem(oPacket, gift.getLeft(), 0, gift.getRight());
            }
            return oPacket.getPacket();
        }

        public static byte[] showGiftSucceed(String to, CashItemData item) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
            oPacket.write(Gift_Done);
            oPacket.writeMapleAsciiString(to);
            oPacket.writeInt(item.nItemid);
            oPacket.writeShort(item.nCount);
            oPacket.writeInt(item.nPrice);
            return oPacket.getPacket();
        }

        public static byte[] showBoughtInventorySlots(int type, short slots) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
            oPacket.write(IncSlotCount_Done);
            oPacket.write(type);
            oPacket.writeShort(slots);
            return oPacket.getPacket();
        }

        public static byte[] showBoughtStorageSlots(short slots) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
            oPacket.write(IncTrunkCount_Done);
            oPacket.writeShort(slots);
            return oPacket.getPacket();
        }

        public static byte[] showBoughtCharacterSlot(short slots) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
            oPacket.write(IncCharSlotCount_Done);
            oPacket.writeShort(slots);
            return oPacket.getPacket();
        }

        public static byte[] takeFromCashInventory(Item item) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
            oPacket.write(MoveLtoS_Done);
            oPacket.writeShort(item.getPosition());
            PacketHelper.encodeItemSlotBase(oPacket, item, true);
            return oPacket.getPacket();
        }

        public static byte[] putIntoCashInventory(Item item, int accountId) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
            oPacket.write(MoveStoL_Done);
            encodeCashItem(oPacket, item, accountId);
            return oPacket.getPacket();
        }

        public static byte[] showBoughtCashPackage(List<Item> cashPackage, int accountId) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
            oPacket.write(BuyPackage_Done);
            oPacket.write(cashPackage.size());
            for (Item item : cashPackage) {
                encodeCashItem(oPacket, item, accountId);
            }
            oPacket.writeShort(0);
            return oPacket.getPacket();
        }

        public static byte[] showBoughtQuestItem(int itemId) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
            oPacket.write(BuyNormal_Done);
            oPacket.writeInt(1);
            oPacket.writeShort(1);
            oPacket.write(0x0B);
            oPacket.write(0);
            oPacket.writeInt(itemId);
            return oPacket.getPacket();
        }

        public static byte[] showCouponRedeemedItem(int itemid) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
            oPacket.writeShort(UseCoupon_Done); // v72
            oPacket.writeInt(0);
            oPacket.writeInt(1);
            oPacket.writeShort(1);
            oPacket.writeShort(0x1A);
            oPacket.writeInt(itemid);
            oPacket.writeInt(0);
            return oPacket.getPacket();
        }
    }

    public static byte[] PurchaseExpChanged(int expChanged) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CASHSHOP_PURCHASE_EXP_CHANGED.getValue());
        oPacket.write(expChanged);
        return oPacket.getPacket();
    }

    public static byte[] GiftMateInfoResult(MapleCharacter player, Boolean correctPLayerName, int SSN2, int CommSN, String giveTo, String text) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CASHSHOP_GIFT_MATE_INFO_RESULT.getValue());
        oPacket.writeBoolean(correctPLayerName); // Please confirm whether\r\nthe character's name is correct.
        oPacket.writeInt(SSN2);
        oPacket.writeInt(CommSN);
        oPacket.writeMapleAsciiString(giveTo);
        oPacket.writeMapleAsciiString(text);
        PacketHelper.encodeCharacter(oPacket, player);
        return oPacket.getPacket();
    }

    public static byte[] CheckDuplicatedIDResult(String newName, Boolean used) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CASHSHOP_CHECK_DUPLICATED_ID_RESULT.getValue());
        oPacket.writeMapleAsciiString(newName);
        oPacket.writeBoolean(used);
        // @used : false => "This name can be used. \r\nIf you wish to use this name,\r\nplease press OK."
        return oPacket.getPacket();
    }

    public static byte[] CheckNameChangePossibleResult(byte type, int birthDate) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CASHSHOP_CHECK_NAME_CHANGE_POSSIBLE_RESULT.getValue());
        oPacket.writeInt(0);
        oPacket.write(type);
        // 0 : CUIChangingLicenseNotice::SetBirthDate(v8, nBirthDate);
        // 1 : "The name change is already submitted \r\ndue to the item purchase"
        // 2 : "This applies to the limitations on the request.\r\nPlease check if you were recently banned \r\nwithin 3 months."
        // 3 : "This applies to the limitations on the request.\r\nPlease check if you requested \r\nfor the name change within a month."
        // > 3 : "An unknown error has occured."
        oPacket.writeInt(birthDate);
        return oPacket.getPacket();
    }

    public static byte[] CheckTransferWorldPossibleResult(byte dlgNotice, int birthDate) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CASHSHOP_CHECK_TRANSFER_WORLD_POSSIBLE_RESULT.getValue());
        oPacket.writeInt(0);
        oPacket.write(dlgNotice);
        oPacket.writeInt(birthDate);
        // TODO: Unused
        return oPacket.getPacket();
    }

    public static byte[] onCashShopGachaponStampResult(boolean inventoryFull, int stamps) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CASHSHOP_GACHAPON_STAMP_ITEM_RESULT.getValue());
        oPacket.writeBoolean(inventoryFull);
        if (inventoryFull) {
            oPacket.writeInt(stamps);
            // "You have acquired %d Gachapon Stamps\r\nby purchasing the Gachapon Ticket.
        }
        return oPacket.getPacket();
    }

    public static class CashItemGachaponResult {

        public static byte[] checkExceededCashItems() {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.CASHSHOP_CASH_ITEM_GACHAPON_RESULT.getValue());
            oPacket.write(192);
            // Please check and see if you have exceeded\r\nthe number of cash items you can have.
            return oPacket.getPacket();
        }

        public static byte[] encode(int accountid, long sn, int remainingBoxes, Item item, int selectedItemID, int selectedItemCount, boolean bJackpot) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.CASHSHOP_CASH_ITEM_GACHAPON_RESULT.getValue());
            oPacket.write(193);
            oPacket.writeLong(sn);// sn of the box used
            oPacket.writeInt(remainingBoxes);
            encodeCashItem(oPacket, item, accountid);
            oPacket.writeInt(selectedItemID);// the itemid of the liSN?
            oPacket.write(selectedItemCount);// the total count now? o.O
            oPacket.writeBoolean(bJackpot);// "CashGachaponJackpot"
            return oPacket.getPacket();
        }

    }

    public static void encodeCashItem(final MaplePacketLittleEndianWriter oPacket, Item item, int accountId) {
        encodeCashItem(oPacket, item, accountId, null);
    }

    public static void encodeCashItem(final MaplePacketLittleEndianWriter oPacket, Item item, int accountId, String giftMessage) {
        boolean isGift = giftMessage != null;
        boolean isRing = false;
        Equip equip = null;
        if (item.getType() == 1) {
            equip = (Equip) item;
            isRing = equip.getRingId() > -1;
        }
        oPacket.writeLong(item.getPetId() > -1 ? item.getPetId() : isRing ? equip.getRingId() : item.getCashId());
        if (!isGift) {
            oPacket.writeInt(accountId);
            oPacket.writeInt(0);// dwCharacterID
        }
        oPacket.writeInt(item.getItemId());
        if (!isGift) {
            oPacket.writeInt(item.getOldSN());
            oPacket.writeShort(item.getQuantity());
        }
        oPacket.writeAsciiString(StringUtil.getRightPaddedStr(item.getGiftFrom(), '\0', 13));
        if (isGift) {
            oPacket.writeAsciiString(StringUtil.getRightPaddedStr(giftMessage, '\0', 73));
            return;
        }
        PacketHelper.encodeExpirationTime(oPacket, item.getExpiration());
        oPacket.writeInt(0);// nPaybackRate
        oPacket.writeInt(0);// nDiscountRate
    }
}
