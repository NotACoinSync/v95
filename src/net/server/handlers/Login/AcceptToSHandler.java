package net.server.handlers.Login;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.Login;

public final class AcceptToSHandler extends AbstractMaplePacketHandler {

    @Override
    public boolean validateState(MapleClient c) {
        return !c.isLoggedIn();
    }

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (slea.available() == 0 || slea.readByte() != 1 || c.acceptToS()) {
            c.disconnect(false, false);
            return;
        }
        if (c.finishLogin() == 0) {
            c.announce(Login.CheckPasswordResult(c));
        } else {
            c.announce(Login.getLoginFailed(9));
        }
    }
}
