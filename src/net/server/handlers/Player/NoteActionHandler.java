package net.server.handlers.Player;

import client.MapleCharacter;
import client.MapleClient;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.AbstractMaplePacketHandler;
import tools.DatabaseConnection;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CashShopPacket;
import tools.packets.WvsContext;

public final class NoteActionHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (player == null) {
            c.disconnect(true, false);
            return;
        }
        int action = iPacket.readByte();
        if (action == 0 && player.getCashShop().getAvailableNotes() > 0) {
            String charname = iPacket.readMapleAsciiString();
            String message = iPacket.readMapleAsciiString();
            try {
                if (player.getCashShop().isOpened()) {
                    c.announce(CashShopPacket.CashItemResult.showCashInventory(c));
                }
                player.sendNote(charname, message, (byte) 1);
                player.getCashShop().decreaseNotes();
            } catch (SQLException e) {
                Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
            }
        } else if (action == 1) {
            int num = iPacket.readByte();
            iPacket.readByte();
            iPacket.readByte();
            int fame = 0;
            for (int i = 0; i < num; i++) {
                int id = iPacket.readInt();
                iPacket.readByte(); // Fame, but we read it from the database :)
                PreparedStatement ps;
                try {
                    ps = DatabaseConnection.getConnection().prepareStatement("SELECT `fame` FROM notes WHERE id=? AND deleted=0");
                    ps.setInt(1, id);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        fame += rs.getInt("fame");
                    }
                    rs.close();
                    ps = DatabaseConnection.getConnection().prepareStatement("UPDATE notes SET `deleted` = 1 WHERE id = ?");
                    ps.setInt(1, id);
                    ps.executeUpdate();
                    ps.close();
                } catch (SQLException e) {
                    Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
                }
            }
            if (fame > 0) {
                player.gainFame(fame);
                c.announce(WvsContext.OnMessage.getShowFameGain(fame));
            }
        }
    }
}
