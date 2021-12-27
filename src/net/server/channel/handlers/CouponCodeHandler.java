package net.server.channel.handlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import client.MapleClient;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import server.ItemInformationProvider;
import server.MapleInventoryManipulator;
import tools.DatabaseConnection;
import tools.packets.PacketHelper;
import tools.ObjectParser;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CashShopPacket;
import tools.packets.WvsContext;

public final class CouponCodeHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.skip(2);
        String code = slea.readMapleAsciiString();
        boolean validcode = false;
        int type = -1;
        int item = -1;
        validcode = getNXCodeValid(code, validcode);
        if (validcode) {
            type = getNXCode(code, "type");
            item = getNXCode(code, "item");
            try {
                Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("UPDATE nxcode SET `valid` = 0 WHERE code = ?");
                ps.setString(1, code);
                ps.executeUpdate();
                ps.close();
                ps = con.prepareStatement("UPDATE nxcode SET `user` = ? WHERE code = ?");
                ps.setString(1, c.getPlayer().getName());
                ps.setString(2, code);
                ps.executeUpdate();
                ps.close();
            } catch (SQLException e) {
                Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
            }
            switch (type) {
                case 1:
                case 2:
                case 4:
                    c.getPlayer().getCashShop().gainCash(type, item);
                    c.getPlayer().getClient().announce(WvsContext.BroadcastMsg.encode(1, "You have redeemed your coupon!"));
                    break;
                case 3:// maplepoint
                    c.getPlayer().getCashShop().gainCash(0, item);
                    c.getPlayer().getCashShop().gainCash(2, (item / 5000));
                    c.getPlayer().getClient().announce(WvsContext.BroadcastMsg.encode(1, "You have redeemed your coupon!"));
                    break;
                case 5:
                    Item i;
                    if (ItemInformationProvider.getInstance().getInventoryType(item) == MapleInventoryType.EQUIP) {
                        i = ItemInformationProvider.getInstance().getEquipById(item);
                    } else {
                        i = new Item(item, (short) 1);
                    }
                    MapleInventoryManipulator.addFromDrop(c, i, false);
                    c.getPlayer().getClient().announce(WvsContext.BroadcastMsg.encode(1, "You have redeemed your coupon!"));
                    c.announce(CashShopPacket.CashItemResult.showCouponRedeemedItem(item));
                    break;
            }
            c.announce(CashShopPacket.QueryCashResult(c.getPlayer()));
        } else {
            c.announce(CashShopPacket.CashItemResult.showCashShopMessage((byte) 0xB0));
        }
        c.announce(PacketHelper.enableCSUse());
    }

    private int getNXCode(String code, String type) {
        int item = type.equals("item") ? ObjectParser.isInt(code).intValue() : 5;
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT `" + type + "` FROM nxcode WHERE code = ?");
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                item = rs.getInt(type);
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
        }
        return item;
    }

    private boolean getNXCodeValid(String code, boolean validcode) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT `valid` FROM nxcode WHERE code = ?");
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                validcode = rs.getInt("valid") != 0;
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
        }
        return validcode || (ObjectParser.isInt(code) != null && ItemInformationProvider.getInstance().getItemData(ObjectParser.isInt(code)).exists);
    }
}
