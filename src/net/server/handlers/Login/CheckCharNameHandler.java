package net.server.handlers.Login;

import client.MapleCharacter;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.Login;

public final class CheckCharNameHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        String name = slea.readMapleAsciiString();
        c.announce(Login.CheckDuplicatedIDResult(name, !MapleCharacter.canCreateChar(name)));
    }
}
