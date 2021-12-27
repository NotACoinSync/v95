package net.server.handlers.Login;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

public class ClientErrorHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        Logger.log(LogType.ERROR, LogFile.CLIENT_ERROR, slea.toString());
    }

    @Override
    public boolean validateState(MapleClient c) {
        return true;
    }
}
