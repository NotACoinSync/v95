package net.server.handlers.Coconut;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import server.events.gm.MapleCoconut;
import server.events.gm.MapleCoconuts;
import server.maps.MapleMap;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CField.Coconut;
import tools.packets.WvsContext;

public final class Coconut_BasicActionAttack extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        int attack = iPacket.readShort();
        int delay = iPacket.readShort();
        MapleMap map = c.getPlayer().getMap();
        MapleCoconut event = map.getCoconut();
        if (event == null) {
            return;
        }
        MapleCoconuts nut = event.getCoconut(attack);
        if (!nut.isHittable()) {
            return;
        }
        if (System.currentTimeMillis() < nut.getHitTime()) {
            return;
        }
        if (nut.getHits() > 2 && Math.random() < 0.4) {
            if (Math.random() < 0.01 && event.getStopped() > 0) {
                nut.setHittable(false);
                event.stopCoconut();
                map.announce(Coconut.Hit(false, attack, 1, delay));
                return;
            }
            nut.setHittable(false); // for sure :)
            nut.resetHits(); // For next event (without restarts)
            if (Math.random() < 0.05 && event.getBombings() > 0) {
                map.announce(Coconut.Hit(false, attack, 2, delay));
                event.bombCoconut();
            } else if (event.getFalling() > 0) {
                map.announce(Coconut.Hit(false, attack, 3, delay));
                event.fallCoconut();
                if (c.getPlayer().getTeam() == 0) {
                    event.addMapleScore();
                    map.announce(WvsContext.BroadcastMsg.encode(5, c.getPlayer().getName() + " of Team Maple knocks down a coconut."));
                } else {
                    event.addStoryScore();
                    map.announce(WvsContext.BroadcastMsg.encode(5, c.getPlayer().getName() + " of Team Story knocks down a coconut."));
                }
                map.announce(Coconut.Score(event.getMapleScore(), event.getStoryScore()));
            }
        } else {
            nut.hit();
            map.announce(Coconut.Hit(false, attack, 1, delay));
        }
    }
}
