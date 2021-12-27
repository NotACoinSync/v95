package net.server.channel.handlers;

import client.MapleCharacter;
import client.MapleClient;
import server.movement.Elem;
import server.movement.MovePath;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.PetPacket;

public final class MovePetHandler extends AbstractMovementPacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        long petId = slea.readLong();
        MovePath res = new MovePath();
        res.decode(slea);
        if (res.lElem.isEmpty()) {
            return;
        }
        MapleCharacter player = c.getPlayer();
        if (player == null) {
            return;
        }
        byte slot = player.getPetIndex(petId);
        if (slot == -1) {
            return;
        }
        player.getPet(slot).updatePosition(res);
        if (c.getPlayer().bMoveAction != -1) {
            for (Elem elem : res.lElem) {
                elem.bMoveAction = c.getPlayer().bMoveAction;
            }
        }
        player.getMap().announce(player, PetPacket.movePet(player.getId(), slot, res), false);
    }
}
