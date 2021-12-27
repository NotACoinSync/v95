package tools.packets.CField;

import net.SendOpcode;
import server.reactors.MapleReactor;
import tools.data.output.MaplePacketLittleEndianWriter;

public class ReactorPool {

    public static byte[] ReactorChangeState(MapleReactor reactor, int stance) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.REACTOR_HIT.getValue());
        oPacket.writeInt(reactor.getObjectId());
        oPacket.write(reactor.getCurrStateAsByte());
        oPacket.writePos(reactor.getPosition());
        oPacket.writeShort(stance);
        oPacket.write(0);
        oPacket.write(3); // frame delay, set to 5 since there doesn't appear to be a fixed formula for it
        return oPacket.getPacket();
    }

    public static byte[] ReactorMove(MapleReactor reactor) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.REACTOR_MOVE.getValue());
        oPacket.writeInt(reactor.getObjectId());
        oPacket.writePos(reactor.getPosition());
        return oPacket.getPacket();
    }

    public static byte[] ReactorEnterField(MapleReactor reactor) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.REACTOR_SPAWN.getValue());
        oPacket.writeInt(reactor.getObjectId());
        oPacket.writeInt(reactor.getId());
        oPacket.write(reactor.getCurrStateAsByte());
        oPacket.writePos(reactor.getPosition());
        oPacket.write(0);
        oPacket.writeMapleAsciiString("");// ?
        return oPacket.getPacket();
    }

    public static byte[] ReactorLeaveField(MapleReactor reactor) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.REACTOR_DESTROY.getValue());
        oPacket.writeInt(reactor.getObjectId());
        oPacket.write(reactor.getCurrStateAsByte());
        oPacket.writePos(reactor.getPosition());
        return oPacket.getPacket();
    }
}
