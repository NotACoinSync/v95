package net.server.handlers.Login;

import client.MapleClient;
import constants.ServerConstants;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.Login;

/*
 * @author Rob
 */
public final class RegisterPinHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (!ServerConstants.ENABLE_PIN) {
            Logger.log(LogType.INFO, LogFile.LOGIN_BAN, c.getAccountName() + " tried to register a pin when pin is disabled.");
            c.getSession().close();
            return;
        }
        byte c2 = slea.readByte();
        if (c2 == 0) {
            c.updateLoginState(MapleClient.LOGIN_NOTLOGGEDIN);
        } else {
            String pin = slea.readMapleAsciiString();
            if (pin != null) {
                c.setPin(pin);
                c.announce(Login.UpdatePinCodeResult());
                c.updateLoginState(MapleClient.LOGIN_NOTLOGGEDIN);
            }
        }
    }
}
