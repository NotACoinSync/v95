package net.server.channel.handlers;

import client.MapleCharacter;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CField.userpool.UserLocal;
import tools.packets.CField.userpool.UserRemote;

public final class SendGetUpFromChairRequest extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (player == null) {
            c.disconnect(true, false);
            return;
        }
        int id = slea.readShort();
        if (id == -1) { // Cancel Chair
            c.getPlayer().setChair(0);
            c.announce(UserLocal.SitResult(-1));
            c.getPlayer().getMap().announce(c.getPlayer(), UserRemote.setActivePortableChair(c.getPlayer().getId(), 0), false);
        } else { // Use In-Map Chair
            c.getPlayer().setChair(id);
            c.announce(UserLocal.SitResult(id));
        }
    }
}
