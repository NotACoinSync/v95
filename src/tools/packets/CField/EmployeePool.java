package tools.packets.CField;

import net.SendOpcode;
import server.maps.objects.HiredMerchant;
import tools.data.output.MaplePacketLittleEndianWriter;

public class EmployeePool {

    public static byte[] EmployeeEnterField(HiredMerchant hm) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SPAWN_HIRED_MERCHANT.getValue());
        oPacket.writeInt(hm.getOwnerId());
        oPacket.writeInt(hm.getItemId());
        oPacket.writeShort((short) hm.getPosition().getX());
        oPacket.writeShort((short) hm.getPosition().getY());
        oPacket.writeShort(0);
        oPacket.writeMapleAsciiString(hm.getOwnerName());
        oPacket.write(0x05);
        oPacket.writeInt(hm.getObjectId());
        oPacket.writeMapleAsciiString(hm.getDescription());
        oPacket.write(hm.getItemId() % 10);
        oPacket.write(new byte[]{1, 4});
        return oPacket.getPacket();
    }

    public static byte[] EmployeeLeaveField(int dwEmployeeId) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.DESTROY_HIRED_MERCHANT.getValue());
        oPacket.writeInt(dwEmployeeId);
        return oPacket.getPacket();
    }

    public static byte[] EmployeeMiniRoomBalloon() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.DESTROY_HIRED_MERCHANT.getValue());
        oPacket.writeInt(0);
        // CEmployee::SetBalloon
        oPacket.write(0); // nMiniRoomType
        oPacket.writeInt(0); // dwMiniRoomSN
        oPacket.writeMapleAsciiString(""); // Must be text on Balloon
        oPacket.write(0); // nMaxUsers
        oPacket.write(0); // nCurUsers
        oPacket.write(0); // bGameOn
        return oPacket.getPacket();
    }
}
