package server.events.gm;

import client.MapleCharacter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import server.TimerManager;
import server.maps.MapleMap;
import tools.packets.CField.Clock;
import tools.packets.CField.Coconut;
import tools.packets.Field;

public class MapleCoconut extends MapleEvent {

    private MapleMap map = null;
    private int MapleScore = 0;
    private int StoryScore = 0;
    private int countBombing = 80;
    private int countFalling = 401;
    private int countStopped = 20;
    private List<MapleCoconuts> coconuts = new LinkedList<MapleCoconuts>();

    public MapleCoconut(MapleMap map) {
        super(1, 50);
        this.map = map;
    }

    public void startEvent() {
        map.startEvent();
        for (int i = 0; i < 506; i++) {
            coconuts.add(new MapleCoconuts(i));
        }
        map.announce(Coconut.Hit(true, 0, 0, 1000));
        setCoconutsHittable(true);
        map.announce(Clock.Created(300));
        TimerManager.getInstance().schedule("coconut1", new Runnable() {

            @Override
            public void run() {
                if (map.getId() == 109080000) {
                    if (getMapleScore() == getStoryScore()) {
                        bonusTime();
                    } else if (getMapleScore() > getStoryScore()) {
                        for (MapleCharacter chr : map.getCharacters()) {
                            if (chr.getTeam() == 0) {
                                chr.getClient().announce(Field.FieldEffect.showEffect("event/coconut/victory"));
                                chr.getClient().announce(Field.FieldEffect.playSound("Coconut/Victory"));
                            } else {
                                chr.getClient().announce(Field.FieldEffect.showEffect("event/coconut/lose"));
                                chr.getClient().announce(Field.FieldEffect.playSound("Coconut/Failed"));
                            }
                        }
                        warpOut();
                    } else {
                        for (MapleCharacter chr : map.getCharacters()) {
                            if (chr.getTeam() == 1) {
                                chr.getClient().announce(Field.FieldEffect.showEffect("event/coconut/victory"));
                                chr.getClient().announce(Field.FieldEffect.playSound("Coconut/Victory"));
                            } else {
                                chr.getClient().announce(Field.FieldEffect.showEffect("event/coconut/lose"));
                                chr.getClient().announce(Field.FieldEffect.playSound("Coconut/Failed"));
                            }
                        }
                        warpOut();
                    }
                }
            }
        }, 300000);
    }

    public void bonusTime() {
        map.announce(Clock.Created(120));
        TimerManager.getInstance().schedule("coconut-bonus", new Runnable() {

            @Override
            public void run() {
                if (getMapleScore() == getStoryScore()) {
                    for (MapleCharacter chr : map.getCharacters()) {
                        chr.getClient().announce(Field.FieldEffect.showEffect("event/coconut/lose"));
                        chr.getClient().announce(Field.FieldEffect.playSound("Coconut/Failed"));
                    }
                    warpOut();
                } else if (getMapleScore() > getStoryScore()) {
                    for (MapleCharacter chr : map.getCharacters()) {
                        if (chr.getTeam() == 0) {
                            chr.getClient().announce(Field.FieldEffect.showEffect("event/coconut/victory"));
                            chr.getClient().announce(Field.FieldEffect.playSound("Coconut/Victory"));
                        } else {
                            chr.getClient().announce(Field.FieldEffect.showEffect("event/coconut/lose"));
                            chr.getClient().announce(Field.FieldEffect.playSound("Coconut/Failed"));
                        }
                    }
                    warpOut();
                } else {
                    for (MapleCharacter chr : map.getCharacters()) {
                        if (chr.getTeam() == 1) {
                            chr.getClient().announce(Field.FieldEffect.showEffect("event/coconut/victory"));
                            chr.getClient().announce(Field.FieldEffect.playSound("Coconut/Victory"));
                        } else {
                            chr.getClient().announce(Field.FieldEffect.showEffect("event/coconut/lose"));
                            chr.getClient().announce(Field.FieldEffect.playSound("Coconut/Failed"));
                        }
                    }
                    warpOut();
                }
            }
        }, 120000);
    }

    public void warpOut() {
        setCoconutsHittable(false);
        TimerManager.getInstance().schedule("coconut-warpout", new Runnable() {

            @Override
            public void run() {
                List<MapleCharacter> chars = new ArrayList<>(map.getCharacters());
                for (MapleCharacter chr : chars) {
                    if ((getMapleScore() > getStoryScore() && chr.getTeam() == 0) || (getStoryScore() > getMapleScore() && chr.getTeam() == 1)) {
                        chr.changeMap(109050000);
                    } else {
                        chr.changeMap(109050001);
                    }
                }
                map.setCoconut(null);
            }
        }, 12000);
    }

    public int getMapleScore() {
        return MapleScore;
    }

    public int getStoryScore() {
        return StoryScore;
    }

    public void addMapleScore() {
        this.MapleScore += 1;
    }

    public void addStoryScore() {
        this.StoryScore += 1;
    }

    public int getBombings() {
        return countBombing;
    }

    public void bombCoconut() {
        countBombing--;
    }

    public int getFalling() {
        return countFalling;
    }

    public void fallCoconut() {
        countFalling--;
    }

    public int getStopped() {
        return countStopped;
    }

    public void stopCoconut() {
        countStopped--;
    }

    public MapleCoconuts getCoconut(int id) {
        return coconuts.get(id);
    }

    public List<MapleCoconuts> getAllCoconuts() {
        return coconuts;
    }

    public void setCoconutsHittable(boolean hittable) {
        for (MapleCoconuts nut : coconuts) {
            nut.setHittable(hittable);
        }
    }
}
