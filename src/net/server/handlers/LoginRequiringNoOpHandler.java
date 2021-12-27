package net.server.handlers;

import client.MapleClient;
import net.MaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

public final class LoginRequiringNoOpHandler implements MaplePacketHandler {

    private static LoginRequiringNoOpHandler instance = new LoginRequiringNoOpHandler();

    public static LoginRequiringNoOpHandler getInstance() {
        return instance;
    }

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    }

    @Override
    public boolean validateState(MapleClient c) {
        return c.isLoggedIn();
    }
}
