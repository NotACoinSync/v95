package tools.packets.CField;

import net.SendOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

public class KillCount {    

    public static byte[] encode(int OnKill) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.KILL_COUNT_INFO.getValue());
        oPacket.writeInt(OnKill);
        return oPacket.getPacket();
    }

}
