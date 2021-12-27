package tools.packets.CField;

import net.SendOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

public class MonsterCarnival { // TODO

    public static byte[] Enter() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MONSTER_CARNIVAL_START.getValue());
        return oPacket.getPacket();
    }

    public static byte[] PersonalCP() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MONSTER_CARNIVAL_OBTAINED_CP.getValue());
        return oPacket.getPacket();
    }

    public static byte[] TeamCP() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MONSTER_CARNIVAL_PARTY_CP.getValue());
        return oPacket.getPacket();
    }

    public static byte[] RequestResult_true() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MONSTER_CARNIVAL_SUMMON.getValue());
        return oPacket.getPacket();
    }

    public static byte[] RequestResult_false() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MONSTER_CARNIVAL_MESSAGE.getValue());
        return oPacket.getPacket();
    }

    public static byte[] ProcessForDeath() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MONSTER_CARNIVAL_DIED.getValue());
        return oPacket.getPacket();
    }

    public static byte[] ShowMemberOutMsg() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MONSTER_CARNIVAL_LEAVE.getValue());
        return oPacket.getPacket();
    }

    public static byte[] ShowGameResult() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MONSTER_CARNIVAL_RESULT.getValue());
        return oPacket.getPacket();
    }
}
