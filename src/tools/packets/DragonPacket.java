package tools.packets;

import net.SendOpcode;
import server.maps.objects.MapleDragon;
import server.movement.MovePath;
import tools.data.output.MaplePacketLittleEndianWriter;

public class DragonPacket {

    public static byte[] Created(MapleDragon dragon) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SPAWN_DRAGON.getValue());
        oPacket.writeInt(dragon.ownerid);
        oPacket.writeInt(dragon.getPosition().x);
        oPacket.writeInt(dragon.getPosition().y);
        oPacket.write(dragon.getStance());
        oPacket.writeShort(0);
        oPacket.writeShort(dragon.jobid);
        return oPacket.getPacket();
    }

    public static byte[] Move(MapleDragon dragon, MovePath moves) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MOVE_DRAGON.getValue());
        oPacket.writeInt(dragon.ownerid);
        moves.encode(oPacket);
        return oPacket.getPacket();
    }

    /**
     * Sends a request to remove Mir<br>
     *
     * @param chrid - Needs the specific Character ID
     * @return The packet
     */
    public static byte[] Destroy(int chrid) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.REMOVE_DRAGON.getValue());
        oPacket.writeInt(chrid);
        return oPacket.getPacket();
    }
}
