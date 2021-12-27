package net.server.handlers.Login;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.packets.PacketHelper;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.Login;

public final class DeleteCharHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        String pic = slea.readMapleAsciiString();
        int cid = slea.readInt();
        if (c.checkPic(pic)) {
            c.announce(Login.DeleteCharacterResult(cid, 0));
            c.deleteCharacter(c.getWorld(), cid);
        } else {
            c.announce(Login.DeleteCharacterResult(cid, 20));
        }
    }
}
