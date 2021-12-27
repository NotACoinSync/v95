package tools.packets.CField;

import net.SendOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

public class AriantArena {

    public static byte[] UserScore(boolean isDead, String result, int Score) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.ARIANT_ARENA_USER_SCORE.getValue());
        oPacket.writeBoolean(isDead);
        if (!isDead) {
            oPacket.writeMapleAsciiString(result);
            oPacket.writeInt(Score);
        }
        return oPacket.getPacket();
    }

    public static byte[] ShowResult(int team1, int team2) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.ARIANT_ARENA_SHOW_RESULT.getValue());
        return oPacket.getPacket();
    }  
}
