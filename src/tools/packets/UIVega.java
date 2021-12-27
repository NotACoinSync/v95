package tools.packets;

import net.SendOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

public class UIVega {

    public static byte[] Result(int type) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.VEGA_RESULT.getValue());
        oPacket.write(type);
        return oPacket.getPacket();
    }

    public static byte[] Fail() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.VEGA_FAIL.getValue());
        oPacket.write(1);
        return oPacket.getPacket();
    }
}
