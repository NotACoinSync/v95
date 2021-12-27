package tools;

import java.util.ArrayList;
import java.util.List;

import client.MapleClient;
import net.RecvOpcode;
import net.SendOpcode;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 Logs packets to console and file.
 */
public class MapleLogger {

    public static List<String> monitored = new ArrayList<>();
    public static List<String> ignored = new ArrayList<>();
    public static boolean log = false;
    private static boolean logUnhandled = true;

    public static void logRecv(MapleClient c, short packetId, Object message) {
        if (c == null || c.isPlayerNull()) {
            return;
        }
        if (!monitored.contains(c.getPlayer().getName())) {
            return;
        }
        RecvOpcode recv = getRecvOpcodeFromValue(packetId);
        //
        if (recv != null && isRecvBlocked(recv)) {
            return;
        }
        String packet = recv == null ? "" + packetId : recv.toString() + "(" + packetId + ")";
        packet += "\r\n" + HexTool.toString((byte[]) message);
        Logger.log(LogType.INFO, LogFile.PACKET_LOGS, c.getAccountName(), c.getPlayer() + " - " + packet + "\r\n\r\n");
    }

    public static void logRecv(short packetId, Object message) {
        if (!log) {
            return;
        }
        RecvOpcode recv = getRecvOpcodeFromValue(packetId);
        //
        if (recv != null) {
            if (isRecvBlocked(recv)) {
                return;
            }
        } else {
            if (logUnhandled) {
                System.out.println("Unhandled Recv: " + packetId + "\r\n" + HexTool.toString((byte[]) message));
            }
            return;
        }
        String packet = recv.toString() + "(" + packetId + ")";
        packet += "\r\n" + HexTool.toString((byte[]) message);
        System.out.println("Recv: " + packet);
    }

    public static void logSend(MapleClient c, short packetId, Object message) {
        if (c == null || c.isPlayerNull()) {
            return;
        }
        if (!monitored.contains(c.getPlayer().getName())) {
            return;
        }
        SendOpcode send = getSendOpcodeFromValue(packetId);
        //
        if (send != null && isSendBlocked(send)) {
            return;
        }
        String packet = send == null ? "" + packetId : send.toString() + "(" + packetId + ")";
        packet += "\r\n" + HexTool.toString((byte[]) message);
        Logger.log(LogType.INFO, LogFile.PACKET_LOGS, c.getAccountName(), c.getPlayer() + " - " + packet + "\r\n\r\n");
    }

    public static void logSend(short packetId, Object message) {
        if (!log) {
            return;
        }
        SendOpcode send = getSendOpcodeFromValue(packetId);
        //
        if (send != null) {
            if (isSendBlocked(send)) {
                return;
            }
        } else {
            if (logUnhandled) {
                System.out.println("Unhandled Send: " + packetId + "\r\n" + HexTool.toString((byte[]) message));
            }
            return;
        }
        String packet = send.toString() + "(" + packetId + ")";
        packet += "\r\n" + HexTool.toString((byte[]) message);
        System.out.println("Send: " + packet);
    }

    public static final boolean isRecvBlocked(RecvOpcode op) {
        if (op == null) {
            return false;
        }
        switch (op) {
            case PONG:
            case MOVE_PLAYER:
            case GENERAL_CHAT:
            case TAKE_DAMAGE:
            case MOVE_PET:
            case MOB_MOVE:
            case NPC_ACTION:
            case MOVE_DRAGON:
            case CHANGE_STAT:
            case UPDATE_SCREEN_SETTING:
                return true;
            default:
                return false;
        }
    }

    public static final boolean isSendBlocked(SendOpcode op) {
        if (op == null) {
            return false;
        }
        switch (op) {
            case PING:
            case MOVE_NPC:
            case MOVE_MONSTER_RESPONSE:
            case SPAWN_NPC:
            case SPAWN_NPC_REQUEST_CONTROLLER:
            case MOVE_PLAYER:
            case SPAWN_MONSTER:
            case KILL_MONSTER:
            case MOVE_MONSTER:
            case MOVE_DRAGON:
            case STAT_CHANGED:
                return true;
            default:
                return false;
        }
    }

    public static final RecvOpcode getRecvOpcodeFromValue(int value) {
        for (RecvOpcode op : RecvOpcode.values()) {
            if (op.getValue() == value) {
                return op;
            }
        }
        return null;
    }

    public static final SendOpcode getSendOpcodeFromValue(int value) {
        for (SendOpcode op : SendOpcode.values()) {
            if (op.getValue() == value) {
                return op;
            }
        }
        return null;
    }
}
