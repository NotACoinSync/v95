package net.server.channel.handlers;

import client.MapleCharacter;
import java.awt.Point;

import client.MapleClient;
import client.autoban.AutobanFactory;
import net.AbstractMaplePacketHandler;
import server.MaplePortal;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CField.userpool.UserCommon;

public final class TryRegisterTeleport extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (player == null) {
            c.disconnect(true, false);
            return;
        }
        byte FieldKey = slea.readByte();
        String portalName = slea.readMapleAsciiString();
        Point portalPos = slea.readPos();
        Point targetPos = slea.readPos();
        if (player.getMap().getPortal(portalName) == null) {
            AutobanFactory.WZ_EDIT.alert(player, "Used inner portal: " + portalName + " in " + player.getMapId() + " targetPos: " + targetPos.toString() + " when it doesn't exist.");
            return;
        }
        for (MaplePortal portal : player.getMap().getPortals()) {
            if (portal.getType() == 1 
                    || portal.getType() == 2 
                    || portal.getType() == 10 
                    || portal.getType() == 20) {
                if (portal.getPosition().equals(portalPos) || portal.getPosition().equals(targetPos)) {
                    player.warpToPortal(portal.getId());
                } else {
                    AutobanFactory.WZ_EDIT.alert(player, "Used inner portal: " + portalName + " in " + c.getPlayer().getMapId() + " targetPos: " + targetPos.toString() + " when it doesn't exist.");
                }
            }
        }
        if (player.passenger != -1) {
            c.announce(UserCommon.followCharacter(player.passenger, player.getId()));
        }
    }
}
