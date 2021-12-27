package tools.packets.CField;

import net.SendOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

public class Clock {

    public static byte Coconut = 0, 
            BattleField = 2, Dojang = 2, Massacre = 2, SpaceGAGA = 2,
            CakePieEvent = 100;

    public static byte[] Created(int tDuration) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CLOCK.getValue());
        oPacket.write(Coconut);
        oPacket.writeInt(tDuration);
        return oPacket.getPacket();
    }

    public static byte[] Created(int hour, int min, int sec) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CLOCK.getValue());
        oPacket.write(1);
        oPacket.write(hour);
        oPacket.write(min);
        oPacket.write(sec);
        return oPacket.getPacket();
    }

    public static byte[] Created(int tDuration, int type) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CLOCK.getValue());
        oPacket.write(type);
        oPacket.writeInt(tDuration);
        return oPacket.getPacket();
    }

    public static byte[] Created(int tDuration, boolean create) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CLOCK.getValue());
        oPacket.write(3);
        oPacket.writeBoolean(create);
        if (create) oPacket.writeInt(tDuration);
        return oPacket.getPacket();
    }

    public static byte[] Created(int tDuration, int nTimerType, boolean create) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CLOCK.getValue());
        oPacket.write(CakePieEvent);
        oPacket.writeBoolean(create);
        if (create) {
            oPacket.write(nTimerType);
            oPacket.writeInt(tDuration);
        }
        return oPacket.getPacket();
    }

    public static byte[] Destroy() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.STOP_CLOCK.getValue());
        return oPacket.getPacket();
    }

}
