package net.server.channel.handlers;

import client.MapleCharacter;
import client.MapleClient;
import server.maps.objects.MapleDragon;
import server.movement.MovePath;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.DragonPacket;

public class MoveDragonHandler extends AbstractMovementPacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        final MapleCharacter player = c.getPlayer();
        if (player == null) {
            c.disconnect(true, false);
            return;
        }
        MovePath res = new MovePath();
        res.decode(iPacket);
        final MapleDragon dragon = player.getDragon();
        if (dragon != null && res != null && res.lElem.size() > 0) {
            updatePosition(res, dragon, 0);
            if (player.isHidden()) {
                player.getMap().broadcastGMMessage(player, DragonPacket.Move(dragon, res));
            } else {
                player.getMap().announce(player, DragonPacket.Move(dragon, res), dragon.getPosition());
            }
        }
    }
}
