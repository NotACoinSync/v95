package net.server.handlers.Login;

import java.util.Map;

import client.MapleClient;
import constants.ServerConstants;
import net.AbstractMaplePacketHandler;
import net.login.LoginServer;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.Login;

public final class ServerStatusRequestHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        byte bWorld = (byte) slea.readByte();
        byte bPopulateLevel = (byte) slea.readByte();
        int bOverUserLimit;
        int num = 0;
        if (LoginServer.getInstance().getLoad(bWorld) != null) {
            Map<Integer, Integer> load = LoginServer.getInstance().getLoad(bWorld);
            for (int players : load.values()) {
                num += players;
            }
        }
        if (num >= ServerConstants.CHANNEL_LOAD) {
            bOverUserLimit = 2;
        } else if (num >= ServerConstants.CHANNEL_LOAD * .8) { // More than 80 percent o___o
            bOverUserLimit = 1;
        } else {
            bOverUserLimit = 0;
        }
        c.sendServerList(false);
        c.announce(Login.CheckUserLimitResult(bOverUserLimit, bPopulateLevel));
    }
}
