package net.server.channel.handlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.ItemFactory;
import client.inventory.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import tools.DatabaseConnection;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CField.StoreBankDlg;

public class FredrickHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        byte operation = slea.readByte();
        switch (operation) {
            case 0x19: // Will never come...
                // c.announce(MaplePacketCreator.getFredrick((byte) 0x24));
                break;
            case 0x1A: // SendCalculateFeeRequest
                List<Pair<Item, MapleInventoryType>> items;
                try {
                    items = ItemFactory.MERCHANT.loadItems(chr.getId(), false);
                    if (!check(chr, items)) {
                        c.announce(StoreBankDlg.Message(33));
                        return;
                    }
                    chr.gainMeso(chr.getMerchantMeso(), false);
                    chr.setMerchantMeso(0);
                    if (deleteItems(chr)) {
                        for (int i = 0; i < items.size(); i++) {
                            Item item = items.get(i).getLeft();
                            item.setQuantity((short) (item.getQuantity() * item.getPerBundle()));
                            item.setPerBundle((short) 1);
                            MapleInventoryManipulator.addFromDrop(c, item, false);
                        }
                        c.announce(StoreBankDlg.Message(30));
                    } else {
                        chr.message("An unknown error has occured.");
                    }
                    break;
                } catch (SQLException ex) {
                    Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
                }
                break;
            case 0x1B: // SendGetAllRequest
                break;
            case 0x1C: // Exit
                break;
            default:
        }
    }

    private static boolean check(MapleCharacter chr, List<Pair<Item, MapleInventoryType>> items) {
        if (chr.getMeso() + chr.getMerchantMeso() < 0) {
            return false;
        }
        if (!chr.canHoldItemsType(items)) {
            return false;
        }
        return true;
    }

    private static boolean deleteItems(MapleCharacter chr) {
        try {
            Connection con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM `inventoryitems` WHERE `type` = ? AND `characterid` = ?")) {
                ps.setInt(1, ItemFactory.MERCHANT.getValue());
                ps.setInt(2, chr.getId());
                ps.execute();
            }
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}
