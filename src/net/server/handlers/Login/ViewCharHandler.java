package net.server.handlers.Login;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import client.MapleCharacter;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import net.login.LoginCharacter;
import tools.DatabaseConnection;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.Login;

public final class ViewCharHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        try {
            short nCountRelatedSvrs;
            List<Integer> worlds;
            List<LoginCharacter> chars;
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT world, id FROM characters WHERE accountid = ? AND deleted = 0")) {
                ps.setInt(1, c.getAccID());
                nCountRelatedSvrs = 0;
                worlds = new ArrayList<>();
                chars = new ArrayList<>();
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int cworld = rs.getByte("world");
                        boolean inside = false;
                        for (int w : worlds) {
                            if (w == cworld) {
                                inside = true;
                            }
                        }
                        if (!inside) {
                            worlds.add(cworld);
                        }
                        LoginCharacter chr = LoginCharacter.loadCharFromDB(rs.getInt("id"), c, false);
                        chars.add(chr);
                        nCountRelatedSvrs++;
                    }
                }
            }
            int nCountCharacters = nCountRelatedSvrs + 3 - nCountRelatedSvrs % 3;
            c.announce(Login.ViewAllCharResult(1, 0, null, nCountRelatedSvrs, nCountCharacters));
            for (Iterator<Integer> it = worlds.iterator(); it.hasNext();) {
                int world = it.next();
                List<MapleCharacter> chrList = new ArrayList<>();
                for (LoginCharacter chr : chars) {
                    if (chr.getWorld() == world) {
                        chrList.add(chr);
                    }
                }
                c.announce(Login.ViewAllCharResult(0, world, chrList, 0, 0));
            }
        } catch (Exception e) {
            Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
        }
    }
}
