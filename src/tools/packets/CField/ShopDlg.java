package tools.packets.CField;

import client.MapleClient;
import java.util.List;
import net.SendOpcode;
import server.shops.MapleShopItem;
import tools.data.output.MaplePacketLittleEndianWriter;

public class ShopDlg {
    
    public static byte[] open(MapleClient c, int NpcTemplateID, List<MapleShopItem> shopItemList) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.OPEN_NPC_SHOP.getValue());
        oPacket.writeInt(NpcTemplateID);
        oPacket.writeShort(shopItemList.size()); // Count
        for (MapleShopItem shopItem : shopItemList) {
            shopItem.encode(c, oPacket);
        }
        return oPacket.getPacket();
    }

    public static class ShopReq {

        public static final int Buy = 0x0, 
                Sell = 0x1, 
                Recharge = 0x2, 
                Close = 0x3;
    }

    public static class ShopRes {

        public static final int BuySuccess = 0, 
                BuyNoStock = 1, 
                BuyNoMoney = 2, 
                BuyUnknown = 3, 
                SellSuccess = 4, 
                SellNoStock = 5, 
                SellIncorrectRequest = 6, 
                SellUnkonwn = 7, 
                RechargeSuccess = 8, 
                RechargeNoStock = 9, 
                RechargeNoMoney = 10, 
                RechargeIncorrectRequest = 11, 
                RechargeUnknown = 12, 
                BuyNoToken = 13, 
                LimitLevel_Less = 14, 
                LimitLevel_More = 15, 
                CantBuyAnymore = 16, 
                TradeBlocked = 17, 
                BuyLimit = 18, 
                ServerMsg = 19;
    }
    
    public static byte[] transact(int code) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CONFIRM_SHOP_TRANSACTION.getValue());
        oPacket.write(code);
        if (code == ShopRes.ServerMsg) {
            oPacket.writeBoolean(true);// if false, no msg
            oPacket.writeMapleAsciiString("test");
        } else if (code == ShopRes.LimitLevel_Less || code == ShopRes.LimitLevel_More) {
            oPacket.writeInt(0);
        }
        return oPacket.getPacket();
    }
}
