package tools.packets.CField;

import net.SendOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

public class OpenGatePool {

    public static byte[] Created(int objectId, short x, short y, byte enter) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.OPEN_GATE_CREATED.getValue());
        oPacket.write(0);
        oPacket.writeInt(objectId);
        oPacket.writeShort(x);
        oPacket.writeShort(y);
        oPacket.write(enter);
        oPacket.writeInt(0);
        return oPacket.getPacket();
    }

    public static byte[] Removed() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter(10);
        oPacket.writeShort(SendOpcode.OPEN_GATE_REMOVED.getValue());
        oPacket.write(0);
        oPacket.writeInt(0);
        oPacket.write(0);
        return oPacket.getPacket();
    }
}
