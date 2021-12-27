package tools.packets.CField;

import java.util.List;
import net.SendOpcode;
import server.DueyPackages;
import tools.packets.PacketHelper;
import tools.Randomizer;
import tools.data.output.MaplePacketLittleEndianWriter;

public class ParcelDlg {

    public static byte[] encode(int mode) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PARCEL.getValue());
        oPacket.write(mode);
        return oPacket.getPacket();
    }

    public static byte[] removeItemFromDuey(boolean remove, int Package) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PARCEL.getValue());
        oPacket.write(0x17);
        oPacket.writeInt(Package);
        oPacket.write(remove ? 3 : 4);
        return oPacket.getPacket();
    }

    public static byte[] sendDueyMSG(byte operation) {
        return sendDuey(operation, null);
    }

    public static byte[] sendDuey(byte operation, List<DueyPackages> packages) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PARCEL.getValue());
        oPacket.write(operation);
        if (operation == 8) {
            oPacket.write(0);
            oPacket.write(packages.size());
            for (DueyPackages dp : packages) {
                oPacket.writeInt(dp.getPackageId());
                oPacket.writeAsciiString(dp.getSender());
                for (int i = dp.getSender().length(); i < 13; i++) {
                    oPacket.write(0);
                }
                oPacket.writeInt(dp.getMesos());
                oPacket.writeLong(PacketHelper.getTime(dp.sentTimeInMilliseconds()));
                oPacket.writeLong(0); // Contains message o____o.
                for (int i = 0; i < 48; i++) {
                    oPacket.writeInt(Randomizer.nextInt(Integer.MAX_VALUE));
                }
                oPacket.writeInt(0);
                oPacket.write(0);
                if (dp.getItem() != null) {
                    oPacket.write(1);
                    PacketHelper.encodeItemSlotBase(oPacket, dp.getItem(), true);
                } else {
                    oPacket.write(0);
                }
            }
            oPacket.write(0);
        }
        return oPacket.getPacket();
    }
}
