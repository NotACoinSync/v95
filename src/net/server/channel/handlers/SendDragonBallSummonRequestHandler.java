package net.server.channel.handlers;

import client.MapleClient;
import client.autoban.AutobanFactory;
import client.inventory.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.WvsContext;

public class SendDragonBallSummonRequestHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int DragonBallBoxID = 0;
        if (c.getPlayer().getInventory(MapleInventoryType.USE).findById(DragonBallBoxID) == null) {
            AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use null DragonBall Box");
            return;
        }
        c.announce(WvsContext.DragonBallBox(10000, 0, true, true, true));
        c.announce(WvsContext.enableActions());       
    }
}
