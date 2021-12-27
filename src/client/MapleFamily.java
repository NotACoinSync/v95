package client;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import tools.DatabaseConnection;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

public class MapleFamily {

    private static int id;
    private static Map<Integer, MapleFamilyEntry> members = new HashMap<Integer, MapleFamilyEntry>();

    public MapleFamily(int cid) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT familyid FROM family_character WHERE cid = ?");
            ps.setInt(1, cid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                id = rs.getInt("familyid");
            }
            ps.close();
            rs.close();
            getMapleFamily();
        } catch (SQLException ex) {
            Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
        }
    }

    private static void getMapleFamily() {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM family_character WHERE familyid = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                MapleFamilyEntry ret = new MapleFamilyEntry();
                ret.setFamilyId(id);
                ret.setRank(rs.getInt("rank"));
                ret.setReputation(rs.getInt("reputation"));
                ret.setTotalJuniors(rs.getInt("totaljuniors"));
                ret.setFamilyName(rs.getString("name"));
                ret.setJuniors(rs.getInt("juniorsadded"));
                ret.setTodaysRep(rs.getInt("todaysrep"));
                int cid = rs.getInt("cid");
                ret.setChrId(cid);
                members.put(cid, ret);
            }
            rs.close();
            ps.close();
        } catch (SQLException sqle) {
            Logger.log(LogType.ERROR, LogFile.EXCEPTION, sqle);
        }
    }

    public MapleFamilyEntry getMember(int cid) {
        if (members.containsKey(cid)) {
            return members.get(cid);
        }
        return null;
    }

    public Map<Integer, MapleFamilyEntry> getMembers() {
        return members;
    }
}
