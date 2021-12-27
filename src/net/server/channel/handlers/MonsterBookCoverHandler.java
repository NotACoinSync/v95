package net.server.channel.handlers;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.WvsContext;

public final class MonsterBookCoverHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int id = slea.readInt();
        if (id == 0 || id / 10000 == 238) {
            c.getPlayer().setMonsterBookCover(id);
            c.announce(WvsContext.MonsterBook.SetCover(id));
        }
    }
}
