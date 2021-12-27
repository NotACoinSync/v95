package net.server.channel.handlers;

import client.MapleCharacter;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.WvsContext;

public final class UserPamsSongUseRequestHandler extends AbstractMaplePacketHandler { // TODO

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        boolean bPamsSongUse = slea.readByte() == 1 ? true : false;
        MapleCharacter chr = c.getPlayer();
        if (chr != null) {
            c.announce(WvsContext.AskWhetherUsePamsSong());
            c.announce(WvsContext.enableActions());
        }
    }
}
