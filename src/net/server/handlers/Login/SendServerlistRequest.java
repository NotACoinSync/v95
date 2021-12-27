package net.server.handlers.Login;

import java.util.Map;

import client.MapleClient;
import constants.ServerConstants;
import constants.WorldConstants;
import constants.WorldConstants.WorldInfo;
import net.AbstractMaplePacketHandler;
import net.login.LoginServer;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.Login;

public final class SendServerlistRequest extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        // Server server = Server.getInstance();
        sendInfo(c);
        //c.announce(CLogin.RecommendWorldMessage(server.worldRecommendedList()));
    }

    public static void sendInfo(MapleClient c) {
        for (WorldInfo worldInfo : WorldConstants.WorldInfo.values()) {
            if (worldInfo.isEnabled()) {
                if (c != null && ((c.getGMLevel() > 0) || worldInfo.isSelectable())) {
                    Map<Integer, Integer> load = LoginServer.getInstance().getLoad(worldInfo.ordinal());
                    if (load != null) {
                        c.sendServerList(false);// disable in charlist request handler?
                        c.announce(Login.WorldInformation(worldInfo.ordinal(), ServerConstants.WORLD_NAMES[worldInfo.ordinal()], worldInfo.getFlag(), worldInfo.getEventMessage(), load));
                    } else {
                        c.sendServerList(true);
                    }
                }
            }
        }
        c.announce(Login.getEndOfServerList());
        //c.announce(CLogin.LatestConnectedWorld(0));// too lazy to make a check lol
    }
}
