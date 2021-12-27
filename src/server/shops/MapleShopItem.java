package server.shops;

import java.sql.ResultSet;
import java.sql.SQLException;

import client.MapleClient;
import constants.ItemConstants;
import server.ItemData;
import server.ItemInformationProvider;
import tools.data.output.LittleEndianWriter;

public class MapleShopItem {

    private short buyable;
    private int itemid;
    private int price;
    private byte discountRate;
    private int tokenItemID, tokenPrice;
    private int itemPeriod;
    private int levelLimited;

    public MapleShopItem(ResultSet rs) throws SQLException {
        itemid = rs.getInt("itemid");
        price = rs.getInt("price");
        discountRate = rs.getByte("discountRate");
        tokenItemID = rs.getInt("tokenItemID");
        tokenPrice = rs.getInt("tokenPrice");
        itemPeriod = rs.getInt("itemPeriod");
        levelLimited = rs.getInt("levelLimited");
        buyable = 1000;
    }

    public MapleShopItem(short buyable, int itemid) {
        this.buyable = buyable;
        this.itemid = itemid;
    }

    public short getBuyable() {
        return buyable;
    }

    public int getItemId() {
        return itemid;
    }

    public int getPrice() {
        return price;
    }

    public int getTokenItemID() {
        return tokenItemID;
    }

    public int getTokenPrice() {
        return tokenPrice;
    }

    public void encode(MapleClient c, LittleEndianWriter lew) {
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        ItemData data = ii.getItemData(getItemId());
        lew.writeInt(itemid);
        lew.writeInt(price);
        lew.write(discountRate);
        lew.writeInt(tokenItemID);
        lew.writeInt(tokenPrice);
        lew.writeInt(itemPeriod); // Can be used x minutes after purchase
        lew.writeInt(levelLimited);
        if (!ItemConstants.isRechargable(getItemId())) {
            lew.writeShort(1); // nQuantity
            lew.writeShort(getBuyable());
        } else {
            lew.writeDouble(data.unitPrice);
            lew.writeShort(data.getSlotMax(c));
        }
    }
}
