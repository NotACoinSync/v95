package tools.packets.CField;

import net.SendOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

public class Witchtower {

    // Cập nhập số điểm của người chơi tại Witch Tower từ 0 - 99
    public static byte[] ScoreUpdate(int Score) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter(6);
        oPacket.writeShort(SendOpcode.ARIANT_ARENA_SHOW_RESULT.getValue());
        oPacket.write(Score);
        return oPacket.getPacket();
    }      
}
