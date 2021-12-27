package net.server.handlers.Player;

import client.MapleCharacter;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CField.userpool.UserLocal;

public class RequestAskAPSPEvent extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (player == null) {
            c.disconnect(true, false);
            return;
        }
        int bNoEnterInput = iPacket.readInt();
        int Activate = iPacket.readInt();
        if (Activate == 6) {
            c.announce(UserLocal.AskAPSPEvent(bNoEnterInput));
        }
    }
}
