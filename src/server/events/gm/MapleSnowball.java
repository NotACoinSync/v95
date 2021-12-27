package server.events.gm;

import client.MapleCharacter;
import java.util.LinkedList;
import java.util.List;
import server.TimerManager;
import server.maps.MapleMap;
import tools.packets.CField.Clock;
import tools.packets.CField.SnowBall;

public class MapleSnowball {

    private MapleMap map;
    private int position = 0;
    private int hits = 3;
    private int snowmanhp = 1000;
    private boolean hittable = false;
    private int team;
    private boolean winner = false;
    List<MapleCharacter> characters = new LinkedList<MapleCharacter>();

    public MapleSnowball(int team, MapleMap map) {
        this.map = map;
        this.team = team;
        for (MapleCharacter chr : map.getCharacters()) {
            if (chr.getTeam() == team) {
                characters.add(chr);
            }
        }
    }

    public void startEvent() {
        if (hittable == true) {
            return;
        }
        for (MapleCharacter chr : characters) {
            if (chr != null) {
                chr.announce(SnowBall.State(false, 1, map.getSnowball(0), map.getSnowball(1)));
                chr.announce(Clock.Created(600, 2));
            }
        }
        hittable = true;
        TimerManager.getInstance().schedule("startEvent", new Runnable() {

            @Override
            public void run() {
                if (map.getSnowball(team).getPosition() > map.getSnowball(team == 0 ? 1 : 0).getPosition()) {
                    for (MapleCharacter chr : characters) {
                        if (chr != null) {
                            chr.announce(SnowBall.State(false, 3, map.getSnowball(0), map.getSnowball(0)));
                        }
                    }
                    winner = true;
                } else if (map.getSnowball(team == 0 ? 1 : 0).getPosition() > map.getSnowball(team).getPosition()) {
                    for (MapleCharacter chr : characters) {
                        if (chr != null) {
                            chr.announce(SnowBall.State(false, 4, map.getSnowball(0), map.getSnowball(0)));
                        }
                    }
                    winner = true;
                } // Else
                warpOut();
            }
        }, 10 * 60 * 1000);
    }

    public boolean isHittable() {
        return hittable;
    }

    public void setHittable(boolean hit) {
        this.hittable = hit;
    }

    public int getPosition() {
        return position;
    }

    public int getSnowmanHP() {
        return snowmanhp;
    }

    public void setSnowmanHP(int hp) {
        this.snowmanhp = hp;
    }

    public void hit(int attackType, int damage, int delay) {
        if (attackType < 2) {
            if (damage > 0) {
                this.hits--;
            } else {
                if (this.snowmanhp - damage < 0) {
                    this.snowmanhp = 0;
                    TimerManager.getInstance().schedule("snowball-hit", new Runnable() {

                        @Override
                        public void run() {
                            setSnowmanHP(7500);
                            message(5);
                        }
                    }, delay);
                } else {
                    this.snowmanhp -= damage;
                }
                map.announce(SnowBall.State(false, 1, map.getSnowball(0), map.getSnowball(1)));
            }
        }
        if (this.hits == 0) {
            this.position += 1;
            if (this.position == 45) {
                map.getSnowball(team == 0 ? 1 : 0).message(1);
            } else if (this.position == 290) {
                map.getSnowball(team == 0 ? 1 : 0).message(2);
            } else if (this.position == 560) {
                map.getSnowball(team == 0 ? 1 : 0).message(3);
            }
            this.hits = 3;
            map.announce(SnowBall.State(false, 0, map.getSnowball(0), map.getSnowball(1)));
            map.announce(SnowBall.State(false, 1, map.getSnowball(0), map.getSnowball(1)));
        }
        map.announce(SnowBall.Hit(attackType, damage));
    }

    public void message(int message) {
        for (MapleCharacter chr : characters) {
            if (chr != null) {
                chr.announce(SnowBall.Message(team, message));
            }
        }
    }

    public void warpOut() {
        TimerManager.getInstance().schedule("snowball-warpout", new Runnable() {

            @Override
            public void run() {
                if (winner == true) {
                    map.warpOutByTeam(team, 109050000);
                } else {
                    map.warpOutByTeam(team, 109050001);
                }
                map.setSnowball(team, null);
            }
        }, 10000);
    }
}
