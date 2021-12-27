package tools.packets;

import client.MapleCharacter;
import java.util.List;
import net.SendOpcode;
import server.MTSItemInfo;
import tools.ITCResCode;
import tools.data.output.MaplePacketLittleEndianWriter;

public class ITC {

    public static byte[] ChargeParamResult() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.ITC_CHARGE_PARAM_RESULT.getValue());
        return oPacket.getPacket();
    }

    public static byte[] QueryCashResult(MapleCharacter p) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.ITC_CHARGE_PARAM_RESULT.getValue());
        oPacket.writeInt(p.getCashShop().getCash(4)); // NexonCash_CS
        oPacket.writeInt(p.getCashShop().getCash(2)); // MaplePoint_CS
        return oPacket.getPacket();
    }

    public static class NormalItemResult { // TODO

        public static byte[] encode(ITCResCode code) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.ITC_NORMAL_ITEM_RESULT.getValue());
            oPacket.write(code.getRes());
            return oPacket.getPacket();
        }

        public static byte[] GetITCList_Done(List<MTSItemInfo> items, int tab, int type, int page, int pages, byte sortType, byte sortColumn) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.ITC_NORMAL_ITEM_RESULT.getValue());
            oPacket.write(ITCResCode.GetITCList_Done.getRes());
            oPacket.writeInt(pages * 16); // testing, change to 10 if fails
            oPacket.writeInt(items.size()); // number of items
            oPacket.writeInt(tab);
            oPacket.writeInt(type);
            oPacket.writeInt(page);
            oPacket.write(sortType);
            oPacket.write(sortColumn);
            items.forEach(mii -> mii.encode(oPacket)); // ITCITEM::encode
            oPacket.write(1);
            return oPacket.getPacket();
        }

        public static byte[] RegisterSaleEntry_Done() {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.ITC_NORMAL_ITEM_RESULT.getValue());
            oPacket.write(ITCResCode.RegisterSaleEntry_Done.getRes());
            return oPacket.getPacket();
        }

        public static byte[] GetNotifyCancelWishResult(int nx, int items) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.ITC_NORMAL_ITEM_RESULT.getValue());
            oPacket.write(ITCResCode.GetNotifyCancelWishResult.getRes());
            oPacket.writeInt(nx);
            oPacket.writeInt(items);
            return oPacket.getPacket();
        }

        public static byte[] BuyItem_Done() {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.ITC_NORMAL_ITEM_RESULT.getValue());
            oPacket.write(ITCResCode.BuyItem_Done.getRes());
            return oPacket.getPacket();
        }

        public static byte[] BuyItem_Failed() {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.ITC_NORMAL_ITEM_RESULT.getValue());
            oPacket.write(ITCResCode.BuyItem_Failed.getRes());
            return oPacket.getPacket();
        }

        public static byte[] MoveITCPurchaseItemLtoS_Done(int quantity, int pos) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.ITC_NORMAL_ITEM_RESULT.getValue());
            oPacket.write(ITCResCode.MoveITCPurchaseItemLtoS_Done.getRes());
            oPacket.writeInt(quantity);
            oPacket.writeInt(pos);
            return oPacket.getPacket();
        }

        public static byte[] GetUserSaleItem_Done(List<MTSItemInfo> items) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.ITC_NORMAL_ITEM_RESULT.getValue());
            oPacket.write(ITCResCode.GetUserSaleItem_Done.getRes());
            oPacket.writeInt(items.size());
            if (!items.isEmpty()) {
                items.forEach(mii -> mii.encode(oPacket));
            } else {
                oPacket.writeInt(0);
            }
            return oPacket.getPacket();
        }

        public static byte[] GetUserPurchaseItem_Done(List<MTSItemInfo> items) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.ITC_NORMAL_ITEM_RESULT.getValue());
            oPacket.write(ITCResCode.GetUserPurchaseItem_Done.getRes());
            oPacket.writeInt(items.size());
            items.forEach(mii -> mii.encode(oPacket));
            oPacket.write(0xD0 + items.size());
            oPacket.write(new byte[]{-1, -1, -1, 0});
            return oPacket.getPacket();
        }
    }   
}
