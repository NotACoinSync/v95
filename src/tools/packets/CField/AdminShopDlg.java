package tools.packets.CField;

import net.SendOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

public class AdminShopDlg {

    public static class Request {

        public static final int OpenShop = 0x0, Trade = 0x1, Close = 0x2, WishItem = 0x3;
    }

    public static class Result {

        public static final int Trade = 0x4, SoldOut = 0x5;
    }

    public static class ResultFail {

        public static final int None = 0, WrongSN = 1, WrongLevel = 2, WrongPeriod = 3, NotEnoughMeso = 4, TooMuchMeso = 5, SoldOut = 6, SoldOutForTheDay = 7, SoldOutForThePerson = 8, OverCount = 9, TradeBlocked = 10, Unknown = 11;
    }

    public static class CommodityState {

        public static final int OnSale = 0x0, SoldOut = 0x1, SoldOutForTheDay = 0x2, SoldOutForThePerson = 0x3;
    }

    /*
     1, 2, 3: 'That's not something I barter. I'll show you the list again for
     you to choose from.'
     4: 'Are you begging?'
     5: 'Why do you carry around so much Mesos? Come back after emptying your
     pockets.'
     6: 'Taking your precious time, huh? I don't barter this item anymore.'
     7: 'I'm done bartering this item for the day. Come back tomorrow.'
     8: 'Why are you obsessing over this item? I won't barter anymore.'
     9: 'You're greedy. How about you lower the quantity?'
     10: 'Items or mesos cannot be moved.\r\nPlease contact customer support.'
     11: 'I'm a little preoccupied right now, so come back later.'
     */
    public static byte[] encode(int mode) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.ADMIN_SHOP_RESULT.getValue());
        oPacket.write(4);
        oPacket.write(mode);
        return oPacket.getPacket();
    }

    public static byte[] open(int NpctemplateID, short CommodityCount) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.ADMIN_SHOP_COMMODITY.getValue());
        oPacket.writeInt(NpctemplateID);
        oPacket.writeShort(CommodityCount);
        for (int i = 0; i < CommodityCount; i++) {
            oPacket.writeInt(i * 10);// nSN
            oPacket.writeInt(2000000);// nItemID
            oPacket.writeInt(100);// nPrice
            oPacket.write(CommodityState.OnSale);// nSaleState, probably CommodityState
            oPacket.writeShort(200);// nMaxPerSlot
        }
        oPacket.write(0); // bAskItemWishList
        return oPacket.getPacket();
    }
}
