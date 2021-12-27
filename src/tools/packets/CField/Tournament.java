package tools.packets.CField;

import net.SendOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

public class Tournament {

    public static byte[] encode(boolean set, boolean NotEnoughUser, int type) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter(7);
        oPacket.writeShort(SendOpcode.TOURNAMENT.getValue());
        oPacket.writeBoolean(set);
        if (!set) {
            oPacket.writeBoolean(NotEnoughUser);
        } else {
            oPacket.write(type);
        }
        return oPacket.getPacket();
    }

    public static byte[] MatchTable(int nState) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter(6);
        oPacket.writeShort(SendOpcode.TOURNAMENT_MATCH_TABLE.getValue());
        oPacket.writeZeroBytes(768);
        oPacket.write(nState);
        return oPacket.getPacket();
    }

    public static byte[] SetPrize(boolean success, boolean show, int ItemID1, int ItemID2) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter(6);
        oPacket.writeShort(SendOpcode.TOURNAMENT_SET_PRIZE.getValue());
        oPacket.writeBoolean(success);
        oPacket.writeBoolean(show);
        if (show) {
        oPacket.writeInt(ItemID1);
        oPacket.writeInt(ItemID2);        
        }
        return oPacket.getPacket();
    }
    
    public static final int ReachedFinal = 2, ReachedSemiFinals = 4, ReachedRound = 8, ReachedRound2 = 16;

    public static byte[] UEW(int Type) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter(6);
        oPacket.writeShort(SendOpcode.TOURNAMENT_UEW.getValue());
        oPacket.write(Type);
        return oPacket.getPacket();
    }

}
