package net.server.handlers.Login;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.Login;

public class SendSetGenderPacket extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        byte type = iPacket.readByte(); // ?
        if (type == 1 && c.getGender() == 10) { // Packet shouldn't come if Gender isn't 10.
            c.setGender(iPacket.readByte());
            c.announce(Login.CheckPasswordResult(c));
        }
    }
}
