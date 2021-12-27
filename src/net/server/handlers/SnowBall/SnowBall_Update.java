package net.server.handlers.SnowBall;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CField.SnowBall;
import tools.packets.WvsContext;

public class SnowBall_Update extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor iPacket, final MapleClient c) {
        c.announce(SnowBall.Touch());
        c.announce(WvsContext.enableActions());
    }
}
