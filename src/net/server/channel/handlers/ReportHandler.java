package net.server.channel.handlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

import client.MapleCharacter;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.DatabaseConnection;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.WvsContext;

public final class ReportHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int type = slea.readByte(); // 01 = Conversation claim 00 = illegal program
        String victim = slea.readMapleAsciiString();
        int reason = slea.readByte();
        String description = slea.readMapleAsciiString();
        if (type == 0) {
            if (c.getPlayer().getPossibleReports() >= 0) {
                if (c.getPlayer().getMeso() > 299) {
                    c.getPlayer().decreaseReports();
                    c.getPlayer().gainMeso(-300, true);
                } else {
                    c.announce(WvsContext.SueCharacterResult((byte) 4));
                    return;
                }
            } else {
                c.announce(WvsContext.SueCharacterResult((byte) 2));
                return;
            }
            try {
                addReport(c.getPlayer().getId(), MapleCharacter.getIdByName(victim), 0, description, null);
            } catch (NullPointerException ex) {
                Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
            }
        } else if (type == 1) {
            String chatlog = slea.readMapleAsciiString();
            if (chatlog == null) {
                return;
            }
            if (c.getPlayer().getPossibleReports() >= 0) {
                if (c.getPlayer().getMeso() > 299) {
                    c.getPlayer().decreaseReports();
                    c.getPlayer().gainMeso(-300, true);
                } else {
                    c.announce(WvsContext.SueCharacterResult((byte) 4));
                    return;
                }
            }
            try {
                addReport(c.getPlayer().getId(), MapleCharacter.getIdByName(victim), reason, description, chatlog);
            } catch (NullPointerException ex) {
                Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
            }
        }
    }

    @SuppressWarnings("deprecation")
    public void addReport(int reporterid, int victimid, int reason, String description, String chatlog) {
        Calendar calendar = Calendar.getInstance();
        Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("INSERT INTO reports (`reporttime`, `reporterid`, `victimid`, `reason`, `chatlog`, `status`) VALUES (?, ?, ?, ?, ?, ?)");
            ps.setString(1, currentTimestamp.toGMTString().toString());
            ps.setInt(2, reporterid);
            ps.setInt(3, victimid);
            ps.setInt(4, reason);
            ps.setString(5, chatlog);
            ps.setString(6, description);
            ps.addBatch();
            ps.executeBatch();
            ps.close();
        } catch (SQLException ex) {
            Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
        }
    }
}
