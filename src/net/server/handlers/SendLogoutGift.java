package net.server.handlers;

import client.MapleCharacter;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.WvsContext;

public class SendLogoutGift extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (player == null) {
            c.disconnect(true, false);
            return;
        }
        int selection = iPacket.readInt();
        if (selection >= 0 && selection < 3) {
            player.setPredictQuit(selection);
            c.announce(WvsContext.LogoutGift());
        }
    }

}
