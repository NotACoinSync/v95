package client.autoban;

import java.rmi.RemoteException;

import client.MapleCharacter;
import net.channel.ChannelServer;
import net.login.LoginServer;
import net.world.WorldCenterInterface;
import tools.MapleLogger;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.WvsContext;

public enum AutobanFactory {
    MOB_COUNT,
    GENERAL,
    FIX_DAMAGE,
    DAMAGE_HACK(15, 60 * 1000),
    CLIENT_EDIT,
    DISTANCE_HACK(10, 120 * 1000),
    PORTAL_DISTANCE(5, 30000),
    PACKET_EDIT,
    ACC_HACK,
    CREATION_GENERATOR,
    HIGH_HP_HEALING,
    HIGH_MP_HEALING,
    FAST_HP_HEALING(15),
    FAST_MP_HEALING(20, 30000),
    GACHA_EXP,
    TUBI(20, 15000),
    ITEM_VAC,
    MOB_VAC,
    FAST_ITEM_PICKUP(5, 30000),
    FAST_ATTACK(10, 30000),
    MPCON(25, 30000),
    GOD_MODE(10),
    WZ_EDIT;

    private int points;
    private long expiretime;

    private AutobanFactory() {
        this(1, -1);
    }

    private AutobanFactory(int points) {
        this.points = points;
        this.expiretime = -1;
    }

    private AutobanFactory(int points, long expire) {
        this.points = points;
        this.expiretime = expire;
    }

    public int getMaximum() {
        return points;
    }

    public long getExpire() {
        return expiretime;
    }

    public void addPoint(AutobanManager ban, String reason) {
        ban.addPoint(this, reason);
    }

    public void log(MapleCharacter chr, String reason) {
        Logger.log(LogType.WARNING, LogFile.ANTICHEAT, MapleCharacter.makeMapleReadable(chr.getName()) + " caused " + name() + " " + reason);
    }

    public void alert(MapleCharacter chr, String reason) {
        if (chr != null && MapleLogger.ignored.contains(chr.getName().toLowerCase())) {
            return;
        }
        try {
            if (ChannelServer.getInstance() != null) {
                ChannelServer.getInstance().getWorldInterface().broadcastGMPacket(WvsContext.SetWeekEventMessage(MapleCharacter.makeMapleReadable(chr.getName()) + " caused " + this.name() + " " + reason));
            } else {
                for (WorldCenterInterface wci : LoginServer.getInstance().getCenterRegistry().getWorldServers().values()) {
                    wci.broadcastGMPacket(WvsContext.SetWeekEventMessage(MapleCharacter.makeMapleReadable(chr.getName()) + " caused " + this.name() + " " + reason));
                }
            }
        } catch (RemoteException | NullPointerException ex) {
            Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
        }
        Logger.log(LogType.WARNING, (this == DISTANCE_HACK || this == DAMAGE_HACK ? LogFile.ANTICHEAT_2 : LogFile.ANTICHEAT), MapleCharacter.makeMapleReadable(chr.getName()) + " caused " + name() + " " + reason);
    }

    public void autoban(MapleCharacter chr, String value) {
        chr.autoban(" (" + this.name() + ": " + value + ")", this);
    }
}
