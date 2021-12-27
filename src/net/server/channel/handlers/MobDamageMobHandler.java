package net.server.channel.handlers;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import server.life.MapleMonster;
import server.maps.MapleMap;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 * @author Jay Estrella
 */
public final class MobDamageMobHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (c.getPlayer() == null) {
            return;
        }
        int from = slea.readInt();
        slea.readInt();
        int to = slea.readInt();
        slea.readByte();
        int dmg = slea.readInt();
        MapleMap map = c.getPlayer().getMap();
        MapleMonster fromMob = map.getMonsterByOid(from);
        MapleMonster toMob = map.getMonsterByOid(to);
        if (fromMob != null && toMob != null) {
            map.damageMonster(c.getPlayer(), toMob, dmg);
        }
    }
}
