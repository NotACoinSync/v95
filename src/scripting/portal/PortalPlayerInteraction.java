package scripting.portal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import client.MapleCharacter;
import client.MapleClient;
import scripting.AbstractPlayerInteraction;
import server.MaplePortal;
import tools.DatabaseConnection;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CField.userpool.UserLocal;
import tools.packets.EffectPacket;

public class PortalPlayerInteraction extends AbstractPlayerInteraction {

    private MaplePortal portal;

    public PortalPlayerInteraction(MapleClient c, MaplePortal portal) {
        super(c);
        this.portal = portal;
    }

    public MaplePortal getPortal() {
        return portal;
    }

    public boolean hasLevel30Character() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = DatabaseConnection.getConnection().prepareStatement("SELECT `level` FROM `characters` WHERE accountid = ?");
            ps.setInt(1, getPlayer().getAccountID());
            rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getInt("level") >= 30) {
                    ps.close();
                    rs.close();
                    return true;
                }
            }
        } catch (SQLException sqle) {
            Logger.log(LogType.ERROR, LogFile.EXCEPTION, sqle);
        } finally {
            try {
                if (ps != null && !ps.isClosed()) {
                    ps.close();
                }
                if (rs != null && !rs.isClosed()) {
                    rs.close();
                }
            } catch (SQLException ex) {
                Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
            }
        }
        return false;
    }

    public void blockPortal() {
        c.getPlayer().blockPortal(getPortal().getScriptName());
    }

    public void unblockPortal() {
        c.getPlayer().unblockPortal(getPortal().getScriptName());
    }

    public void playPortalSound() {
        c.announce(EffectPacket.Local.PlayPortalSE());
    }

    @Override
    public void gainExp(MapleCharacter player, int exp, double multiplier, boolean show, String logData) {
        logData += "From Portal: " + portal.getId() + " Script: " + portal.getScriptName();
        super.gainExp(player, exp, multiplier, show, logData);
    }
}
