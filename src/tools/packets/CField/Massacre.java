package tools.packets.CField;

import net.SendOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

public class Massacre {

    public static byte[] IncGuage(int IncGauge) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MASSACRE_INC_GUAGE.getValue());
        oPacket.writeInt(IncGauge);
        return oPacket.getPacket();
    }

    public static byte[] Result(byte Rank, int IncExp) {// Type cannot be higher than 4 (Rank D), otherwise you'll crash
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MASSACRE_RESULT.getValue());
        oPacket.write(Rank);
        oPacket.writeInt(IncExp);
        return oPacket.getPacket();
    }
}
