package tools.packets.CField;

import net.SendOpcode;
import server.maps.objects.Kite;
import tools.data.output.MaplePacketLittleEndianWriter;

public class MessageBoxPool {

    public static byte[] CreateFailed() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SPAWN_KITE_MESSAGE.getValue());
        return oPacket.getPacket();
    }

    public static byte[] MessageBoxEnterField(Kite kite) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SPAWN_KITE.getValue());
        oPacket.writeInt(kite.getObjectId());
        oPacket.writeInt(kite.getItemID());
        oPacket.writeMapleAsciiString(kite.getMessage());
        oPacket.writeMapleAsciiString(kite.getPlayerName());
        oPacket.writePos(kite.getPosition());
        return oPacket.getPacket();
    }

    /**
     * AnimationType 0 is 10/10 AnimationType 1 just vanishes
     */
    public static byte[] MessageBoxLeaveField(int objectid, byte animationType) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.DESTROY_KITE.getValue());
        oPacket.write(animationType);
        oPacket.writeInt(objectid);
        return oPacket.getPacket();
    }
}
