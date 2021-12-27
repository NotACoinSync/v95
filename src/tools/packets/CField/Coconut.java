package tools.packets.CField;

import net.SendOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

public class Coconut {

    public static byte[] Hit(boolean target, int id, int state, int delay) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.COCONUT_HIT.getValue());
        oPacket.writeShort(target ? -1 : id);
        oPacket.writeShort(delay);
        oPacket.write(target ? 0 : state);
        return oPacket.getPacket();
    }

    public static byte[] Score(int team1, int team2) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.COCONUT_SCORE.getValue());
        oPacket.writeShort(team1);
        oPacket.writeShort(team2);
        return oPacket.getPacket();
    }
}
