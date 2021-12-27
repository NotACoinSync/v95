package net.server.handlers.Login;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.Login;

public final class AfterLoginHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        byte c2 = slea.readByte();
        byte c3 = 5;
        if (slea.available() > 0) {
            c3 = slea.readByte();
        }
        if (c2 == 1 && c3 == 1) {
            if (c.getPin() == null) {
                c.announce(Login.CheckPinCodeResult((byte) 1));
            } else {
                c.announce(Login.CheckPinCodeResult((byte) 4));
            }
        } else if (c2 == 1 && c3 == 0) {
            String pin = slea.readMapleAsciiString();
            if (c.checkPin(pin)) {
                c.announce(Login.CheckPinCodeResult((byte) 0));
            } else {
                c.announce(Login.CheckPinCodeResult((byte) 2));
            }
        } else if (c2 == 2 && c3 == 0) {
            String pin = slea.readMapleAsciiString();
            if (c.checkPin(pin)) {
                c.announce(Login.CheckPinCodeResult((byte) 1));
            } else {
                c.announce(Login.CheckPinCodeResult((byte) 2));
            }
        } else if (c2 == 0 && c3 == 5) {
            c.updateLoginState(MapleClient.LOGIN_NOTLOGGEDIN);
        } else {
            c.announce(Login.CheckPinCodeResult((byte) 3));
        }
    }
}
