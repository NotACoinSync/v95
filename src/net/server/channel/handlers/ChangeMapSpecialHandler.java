package net.server.channel.handlers;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import server.MaplePortal;
import server.MapleTrade;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.WvsContext;

public final class ChangeMapSpecialHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        iPacket.readByte();
        String startwp = iPacket.readMapleAsciiString();
        iPacket.readShort();
        MaplePortal portal = c.getPlayer().getMap().getPortal(startwp);
        if (portal == null || c.getPlayer().portalDelay() > System.currentTimeMillis() || c.getPlayer().getBlockedPortals().contains(portal.getScriptName())) {
            // AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use a blocked portal, or a special portal too fast.");
            c.announce(WvsContext.enableActions());
            return;
        }
        if (c.getPlayer().isBanned()) {
            return;
        }
        if (c.getPlayer().getTrade() != null) {
            MapleTrade.cancelTrade(c.getPlayer());
        }
        portal.enterPortal(c);
    }
}
