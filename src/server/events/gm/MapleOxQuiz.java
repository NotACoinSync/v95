package server.events.gm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import client.ExpGainType;
import client.MapleCharacter;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import server.TimerManager;
import server.maps.MapleMap;
import server.propertybuilder.ExpProperty;
import tools.Randomizer;
import tools.packets.WvsContext;
import tools.packets.Field;

/**
 * @author FloppyDisk
 */
public final class MapleOxQuiz {

    private int round = 1;
    private int question = 1;
    private MapleMap map = null;
    private int expGain = 200;
    private static MapleDataProvider stringData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Etc.wz"));

    public MapleOxQuiz(MapleMap map) {
        this.map = map;
        this.round = Randomizer.nextInt(9);
        this.question = 1;
    }

    private boolean isCorrectAnswer(MapleCharacter chr, int answer) {
        double x = chr.getPosition().getX();
        double y = chr.getPosition().getY();
        if ((x > -234 && y > -26 && answer == 0) || (x < -234 && y > -26 && answer == 1)) {
            chr.dropMessage("Correct!");
            return true;
        }
        return false;
    }

    public void sendQuestion() {
        final long gms = map.getCharacters().stream().filter(mc -> mc.isGM()).count();
        map.announce(Field.Quiz(true, (byte) round, question));
        TimerManager.getInstance().schedule("sendQuestion", new Runnable() {

            @Override
            public void run() {
                map.announce(Field.Quiz(true, (byte) round, question));
                List<MapleCharacter> chars = new ArrayList<>(map.getCharacters());
                for (MapleCharacter chr : chars) {
                    if (chr != null) // make sure they aren't null... maybe something can happen in 12 seconds.
                    {
                        if (!isCorrectAnswer(chr, getOXAnswer(round, question)) && !chr.isGM()) {
                            chr.changeMap(chr.getMap().getReturnMap());
                        } else {
                            chr.gainExp(new ExpProperty(ExpGainType.OXQUIZ).gain(expGain).show().inChat());
                        }
                    }
                }
                // do question
                if ((round == 1 && question == 29) || ((round == 2 || round == 3) && question == 17) || ((round == 4 || round == 8) && question == 12) || (round == 5 && question == 26) || (round == 9 && question == 44) || ((round == 6 || round == 7) && question == 16)) {
                    question = 100;
                } else {
                    question++;
                }
                // send question
                if (map.getCharacters().size() - gms <= 2) {
                    map.announce(WvsContext.BroadcastMsg.encode(6, "The event has ended"));
                    map.getPortal("join00").setPortalStatus(true);
                    map.setOx(null);
                    map.setOxQuiz(false);
                    // prizes here
                    return;
                }
                sendQuestion();
            }
        }, 30000); // Time to answer = 30 seconds ( Ox Quiz packet shows a 30 second timer.
    }

    private static int getOXAnswer(int imgdir, int id) {
        return MapleDataTool.getInt(stringData.getData("OXQuiz.img").getChildByPath("" + imgdir + "").getChildByPath("" + id + "").getChildByPath("a"));
    }
}
