package tools.packets.CField;

import java.awt.Point;

import net.SendOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

public class TownPortalPool {

    /**
     * Packet gửi triệu hồi Town Portal
     *
     * @param oid The door's object ID.
     * @param pos The position of the door.
     * @param town
     * @return The remove door packet.
     */
    public static byte[] Created(int oid, Point pos, boolean town) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter(11);
        oPacket.writeShort(SendOpcode.SPAWN_DOOR.getValue());
        oPacket.writeBoolean(town);
        oPacket.writeInt(oid);
        oPacket.writePos(pos);
        return oPacket.getPacket();
    }

    /**
     * Packet gửi phá hủy Town Portal
     *
     * @param oid The door's ID.
     * @return The remove door packet.
     */
    public static byte[] Removed(int oid) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter(10);
        oPacket.writeShort(SendOpcode.REMOVE_DOOR.getValue());
        oPacket.write(0);// face
        oPacket.writeInt(oid);
        return oPacket.getPacket();
    }
}
