package net.server.channel.handlers;

import java.util.Collection;

import client.MapleCharacter;
import client.MapleClient;
import server.maps.objects.MapleSummon;
import server.movement.MovePath;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CField.SummonedPool;

public final class MoveSummonHandler extends AbstractMovementPacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int oid = slea.readInt();
        MapleCharacter player = c.getPlayer();
        Collection<MapleSummon> summons = player.getSummons().values();
        MapleSummon summon = null;
        for (MapleSummon sum : summons) {
            if (sum.getObjectId() == oid) {
                summon = sum;
                break;
            }
        }
        MovePath res = new MovePath();
        res.decode(slea);
        if (summon != null) {
            updatePosition(res, summon, 0);
            player.getMap().announce(player, SummonedPool.Move(player.getId(), oid, res), summon.getPosition());
        }
    }
}
