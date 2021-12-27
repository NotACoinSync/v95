package tools.packets.CField;

import net.SendOpcode;
import server.maps.objects.MapleAffectedArea;
import tools.data.output.MaplePacketLittleEndianWriter;

public class AffectedAreaPool {

    public static byte[] Created(int affectedAreaId, int ownerID, int skillID, int skillLevel, MapleAffectedArea affectedArea) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SPAWN_MIST.getValue());
        oPacket.writeInt(affectedAreaId);
        oPacket.writeInt(affectedArea.getAffectedAreaType()); 
        oPacket.writeInt(ownerID);
        oPacket.writeInt(skillID); // CItemInfo::GetAreaBuffItem. Item/Cash/0528.img/%08d/tile
        oPacket.write(skillLevel); 
        oPacket.writeShort(affectedArea.getSkillDelay()); // Skill delay, effectDelay / 100 (e.g. 3240ms -> 32)
        oPacket.writeInt(affectedArea.getBox().x);
        oPacket.writeInt(affectedArea.getBox().y);
        oPacket.writeInt(affectedArea.getBox().x + affectedArea.getBox().width);
        oPacket.writeInt(affectedArea.getBox().y + affectedArea.getBox().height);
        oPacket.writeInt(affectedArea.getInfo()); // pInfo if type == 3 
        oPacket.writeInt(affectedArea.getPhase()); // nPhase if type == 3
        return oPacket.getPacket();
    }

    public static byte[] Removed(int affectedAreaId) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.REMOVE_MIST.getValue());
        oPacket.writeInt(affectedAreaId);
        return oPacket.getPacket();
    }
}
