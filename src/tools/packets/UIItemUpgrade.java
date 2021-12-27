package tools.packets;

import net.SendOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

public class UIItemUpgrade {

    public static byte[] ItemUpgradeResult(byte ReturnResult, int Upgrades, int Result, int IUC) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.ITEM_UPGRADE_RESULT.getValue());
        oPacket.write(ReturnResult);
        if (ReturnResult == 0x41 || ReturnResult == 0x42) {
            oPacket.writeInt(Upgrades);
        }
        oPacket.writeInt(Result);
        oPacket.writeInt(IUC);
        return oPacket.getPacket();
    }

    public static byte[] sendHammerData(int hammerUsed) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.ITEM_UPGRADE_RESULT.getValue());
        oPacket.write(0x39);
        oPacket.writeInt(0);
        oPacket.writeInt(hammerUsed);
        return oPacket.getPacket();
    }

    public static byte[] sendHammerMessage() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.ITEM_UPGRADE_RESULT.getValue());
        oPacket.write(0x3D);
        oPacket.writeInt(0);
        return oPacket.getPacket();
    }
}
