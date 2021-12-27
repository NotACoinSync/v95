package net.server.channel.handlers;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

public final class FindFriendHandler extends AbstractMaplePacketHandler { // TODO

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        System.out.println("FindFriendHandler: " + slea.toString());
    }
}
