package tools.packets.CField;

import net.SendOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

public class BattleRecordMan {

    public static byte[] dotDamageInfo(int CurDamage, int CurDamageSize, boolean AttrRate, int DotAttrRate) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.DOT_DAMAGE_INFO.getValue());
        oPacket.writeInt(CurDamage);
        oPacket.writeInt(CurDamageSize);
        oPacket.writeBoolean(AttrRate);
        if (AttrRate) {
            oPacket.writeInt(DotAttrRate);
        } else {
            oPacket.writeInt(0);
        }
        return oPacket.getPacket();
    }

    public static byte[] serverOnCalcRequestResult(boolean ServerOnCalc) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SERVER_ON_CALC_REQUEST_RESULT.getValue());
        oPacket.writeBoolean(ServerOnCalc);
        return oPacket.getPacket();
    }

}
