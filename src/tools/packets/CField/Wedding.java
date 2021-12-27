package tools.packets.CField;

import net.SendOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

public class Wedding {

    /**
     * Packet Wedding để hiển thị Pelvis Bebop và kích hoạt Hiệu ứng Lễ cưới
     * giữa hai nhân vật
     */
    public static byte[] Progress(boolean SetBlessEffect, int dwGroomID, int dwBrideID) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.WEDDING_PROGRESS.getValue());
        oPacket.writeBoolean(SetBlessEffect);
        oPacket.writeInt(dwGroomID);
        oPacket.writeInt(dwBrideID);
        return oPacket.getPacket();
    }

    /**
     * Packet Wedding để kết thúc lễ cưới giữa hai nhân vật
     */
    public static byte[] CremonyEnd() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.WEDDING_CREMONY_END.getValue());
        return oPacket.getPacket();
    }
}
