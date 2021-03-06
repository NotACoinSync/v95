package server.expeditions;

import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;

import client.MapleCharacter;
import net.channel.ChannelServer;
import server.TimerManager;
import server.life.MapleMonster;
import server.maps.MapleMap;
import tools.BigBrother;
import tools.packets.PacketHelper;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CField.Clock;
import tools.packets.WvsContext;
import tools.packets.Field;

public class MapleExpedition {

    private static final int[] EXPEDITION_BOSSES = {
        8800000, // - Zakum's first body
        8800001, // - Zakum's second body
        8800002, // - Zakum's third body
        8800003, // - Zakum's Arm 1
        8800004, // - Zakum's Arm 2
        8800005, // - Zakum's Arm 3
        8800006, // - Zakum's Arm 4
        8800007, // - Zakum's Arm 5
        8800008, // - Zakum's Arm 6
        8800009, // - Zakum's Arm 7
        8800010, // - Zakum's Arm 8
        8810000, // - Horntail's Left Head
        8810001, // - Horntail's Right Head
        8810002, // - Horntail's Head A
        8810003, // - Horntail's Head B
        8810004, // - Horntail's Head C
        8810005, // - Horntail's Left Hand
        8810006, // - Horntail's Right Hand
        8810007, // - Horntail's Wings
        8810008, // - Horntail's Legs
        8810009, // - Horntail's Tails
        9420546, // - Scarlion Boss
        9420547, // - Scarlion Boss
        9420548, // - Angry Scarlion Boss
        9420549, // - Furious Scarlion Boss
        9420541, // - Targa
        9420542, // - Targa
        9420543, // - Angry Targa
        9420544, // - Furious Targa
};
    private int identifier;
    private MapleCharacter leader;
    private MapleExpeditionType type;
    private boolean registering;
    private MapleMap startMap;
    private ArrayList<String> bossLogs;
    private ScheduledFuture<?> schedule;
    private List<MapleCharacter> members = new ArrayList<>();
    private List<Integer> banned;
    private List<Integer> joined = new ArrayList<>();
    private long startTime;
    private int channel;

    public MapleExpedition(MapleCharacter player, MapleExpeditionType met) {
        identifier = ThreadLocalRandom.current().nextInt();
        this.banned = new ArrayList<>();
        leader = player;
        members.add(leader);
        startMap = player.getMap();
        type = met;
        bossLogs = new ArrayList<>();
        channel = leader.getClient().getChannel();
        beginRegistration();
    }

    private void beginRegistration() {
        registering = true;
        startMap.announce(Clock.Created(type.getRegistrationTime() * 60, 2));
        startMap.announce(WvsContext.BroadcastMsg.encode(6, leader.getName() + " has been declared the expedition captain. Please register for the expedition."));
        scheduleRegistrationEnd();
    }

    private void scheduleRegistrationEnd() {
        final MapleExpedition exped = this;
        schedule = TimerManager.getInstance().schedule("expedRegEnd-" + type.name(), new Runnable() {

            @Override
            public void run() {
                if (registering) {
                    leader.getClient().getChannelServer().getExpeditions().remove(exped);
                    startMap.announce(WvsContext.BroadcastMsg.encode(6, "Time limit has been reached. Expedition has been disbanded."));
                }
                dispose(false);
            }
        }, type.getRegistrationTime() * 60 * 1000);
    }

    public void dispose(boolean log) {
        if (schedule != null) {
            schedule.cancel(false);
        }
        if (log && !registering) {
            BigBrother.logExpedition(this);
        }
        ChannelServer.getInstance().getChannel(channel).getExpeditions().remove(this);
        members.clear();
        startMap = null;
    }

    public void start() {
        registering = false;
        startMap.announce(Clock.Destroy());
        broadcastExped(WvsContext.BroadcastMsg.encode(6, "The expedition has started! The expedition leader is waiting inside!"));
        startTime = System.currentTimeMillis();
        try {
            ChannelServer.getInstance().getWorldInterface().broadcastGMPacket(WvsContext.BroadcastMsg.encode(6, type.toString() + " Expedition started with leader: " + leader.getName()));
        } catch (RemoteException | NullPointerException ex) {
            Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
        }
    }

    public String addMember(MapleCharacter player) {
        if (!registering) {
            return "Sorry, this expedition is already underway. Registration is closed!";
        }
        if (banned.contains(player.getId())) {
            return "Sorry, you've been banned from this expedition by #b" + leader.getName() + "#k.";
        }
        if (members.size() >= type.getMaxSize()) { // Would be a miracle if anybody ever saw this
            return "Sorry, this expedition is full!";
        }
        if (player.isIronMan()) {
            return "Sorry, IronMan are not allowed to join expeditions.";
        }
        if (leader.isIronMan()) {
            return "Sorry, IronMan are not allowed to invite players to expeditions.";
        }
        if (members.add(player)) {
            broadcastExped(WvsContext.BroadcastMsg.encode(6, player.getName() + " has joined the expedition!"));
            return "You have registered for the expedition successfully!";
        }
        return "Sorry, something went really wrong. Report this on the forum with a screenshot!";
    }

    private void broadcastExped(byte[] data) {
        for (MapleCharacter member : members) {
            member.getClient().announce(data);
        }
    }

    public boolean removeMember(MapleCharacter chr) {
        boolean ret = members.remove(chr);
        System.out.println("Removing player: " + chr.getName() + " Ret: " + ret + " empty: " + members.isEmpty());
        if (members.isEmpty()) {
            dispose(true);// yes? no?
        }
        return ret;
    }

    public MapleExpeditionType getType() {
        return type;
    }

    public List<MapleCharacter> getMembers() {
        return members;
    }

    public MapleCharacter getLeader() {
        return leader;
    }

    public boolean contains(MapleCharacter player) {
        for (MapleCharacter member : members) {
            if (member.getId() == player.getId()) {
                return true;
            }
        }
        return false;
    }

    public boolean isLeader(MapleCharacter player) {
        return leader.equals(player);
    }

    public boolean isRegistering() {
        return registering;
    }

    public boolean isInProgress() {
        return !registering;
    }

    public void ban(MapleCharacter player) {
        if (!banned.contains(player)) {
            banned.add(player.getId());
            members.remove(player);
        }
    }

    public long getStartTime() {
        return startTime;
    }

    public ArrayList<String> getBossLogs() {
        return bossLogs;
    }

    public void monsterKilled(MapleCharacter chr, MapleMonster mob) {
        for (int i = 0; i < EXPEDITION_BOSSES.length; i++) {
            if (mob.getId() == EXPEDITION_BOSSES[i]) { // If the monster killed was a boss
                String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
                bossLogs.add(">" + mob.getName() + " was killed after " + BigBrother.getTimeString(startTime) + " - " + timeStamp + "\r\n");
                return;
            }
        }
    }

    /**
     * @return If the player has joined the event instance(Actually entered the
     * map)
     */
    public boolean hasJoined(MapleCharacter chr) {
        return joined.contains(chr.getId());
    }

    public void addJoined(MapleCharacter chr) {
        joined.add(chr.getId());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + channel;
        result = prime * result + identifier;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MapleExpedition other = (MapleExpedition) obj;
        if (channel != other.channel) {
            return false;
        }
        if (identifier != other.identifier) {
            return false;
        }
        if (type != other.type) {
            return false;
        }
        return true;
    }
}
