package net.server.handlers.SnowBall;

import client.MapleCharacter;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import server.events.gm.MapleSnowball;
import server.maps.MapleMap;
import tools.data.input.SeekableLittleEndianAccessor;

public final class Snowball_BasicActionAttack extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        MapleMap map = player.getMap();
        if (map == null) {
            c.disconnect(true, false);
            return;
        }
        final MapleSnowball snowball = map.getSnowball(player.getTeam());
        final MapleSnowball othersnowball = map.getSnowball(player.getTeam() == 0 ? (byte) 1 : 0);
        int attackType = iPacket.readByte();
        short damage = iPacket.readShort();
        short delay = iPacket.readShort();

        if (snowball == null || othersnowball == null || snowball.getSnowmanHP() == 0) {
            return;
        }
        if ((System.currentTimeMillis() - player.getLastSnowballAttack()) < 500) {
            return;
        }
        if (player.getTeam() != (attackType % 2)) {
            return;
        }
        player.setLastSnowballAttack(System.currentTimeMillis());
        /* Có vẻ như Client đã handle phần tính damage
        int damage = 0;
        if (attack < 2 && othersnowball.getSnowmanHP() > 0) {
            damage = 10;
        } else if (attack == 2 || attack == 3) {
            if (Math.random() < 0.03) {
                damage = 45;
            } else {
                damage = 15;
            }
        }*/
        if (attackType >= 0 && attackType <= 4) {
            snowball.hit(attackType, damage, delay);
        }
    }
}
