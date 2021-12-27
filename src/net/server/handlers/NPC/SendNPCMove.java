package net.server.handlers.NPC;

import client.MapleCharacter;
import client.MapleClient;
import net.server.channel.handlers.AbstractMovementPacketHandler;
import server.life.MapleNPC;
import server.maps.MapleMapData;
import server.maps.objects.MapleMapObject;
import server.maps.objects.MapleMapObjectType;
import server.movement.MovePath;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CField.NpcPool;

public final class SendNPCMove extends AbstractMovementPacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        final MapleCharacter player = c.getPlayer();
        if (player == null) {
            c.disconnect(true, false);
            return;
        }
        if (iPacket.available() > 4) {
            MapleMapData data = c.getPlayer().getMap().getMapData();
            for (MapleMapObject mmo : data.getMapObjects()) {
                if (mmo.getType() == MapleMapObjectType.NPC) {
                    MapleNPC npc = (MapleNPC) mmo;
                    MovePath res = new MovePath();
                    if (npc != null && res != null && res.lElem.size() > 0) {
                        res.decode(iPacket);
                        updatePosition(res, npc, 0);
                        player.getMap().announce(player, NpcPool.Move(npc, res), npc.getPosition());
                    }
                }
            }
        }
    }
}
