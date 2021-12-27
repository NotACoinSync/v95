package server.events.gm;

import java.util.concurrent.ScheduledFuture;

import client.MapleCharacter;
import server.TimerManager;
import tools.packets.CField.Clock;
import tools.packets.WvsContext;

public class MapleOla {

    private MapleCharacter chr;
    private long time = 0;
    private long timeStarted = 0;
    private ScheduledFuture<?> schedule = null;

    public MapleOla(final MapleCharacter chr) {
        this.chr = chr;
        this.schedule = TimerManager.getInstance().schedule("ola1", new Runnable() {

            @Override
            public void run() {
                if (chr.getMapId() >= 109030001 && chr.getMapId() <= 109030303) {
                    chr.changeMap(chr.getMap().getReturnMap());
                }
                resetTimes();
            }
        }, 360000);
    }

    public void startOla() { // TODO: Messages
        chr.getMap().startEvent();
        chr.getClient().announce(Clock.Created(360, 2));
        this.timeStarted = System.currentTimeMillis();
        this.time = 360000;
        chr.getMap().getPortal("join00").setPortalStatus(true);
        chr.getClient().announce(WvsContext.BroadcastMsg.encode(0, "The portal has now opened. Press the up arrow key at the portal to enter."));
    }

    public boolean isTimerStarted() {
        return time > 0 && timeStarted > 0;
    }

    public long getTime() {
        return time;
    }

    public void resetTimes() {
        this.time = 0;
        this.timeStarted = 0;
        schedule.cancel(false);
    }

    public long getTimeLeft() {
        return time - (System.currentTimeMillis() - timeStarted);
    }
}
