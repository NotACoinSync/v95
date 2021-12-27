package tools.packets.CField;

import net.SendOpcode;
import server.events.gm.MapleSnowball;
import tools.data.output.MaplePacketLittleEndianWriter;

public class SnowBall {

    public static byte[] State(boolean entermap, int state, MapleSnowball ball0, MapleSnowball ball1) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SNOWBALL_STATE.getValue());
        if (entermap) {
            oPacket.skip(21);
        } else {
            oPacket.write(state);// 0 = move, 1 = roll, 2 is down disappear, 3 is up disappear
            oPacket.writeInt(ball0.getSnowmanHP() / 75);
            oPacket.writeInt(ball1.getSnowmanHP() / 75);
            oPacket.writeShort(ball0.getPosition());// distance snowball down, 84 03 = max
            oPacket.write(-1);
            oPacket.writeShort(ball1.getPosition());// distance snowball up, 84 03 = max
            oPacket.write(-1);
        }
        return oPacket.getPacket();
    }
    
    public static byte[] Hit(int what, int damage) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.HIT_SNOWBALL.getValue());
        oPacket.write(what);
        oPacket.writeInt(damage);
        return oPacket.getPacket();
    }

    /**
     Sends a Snowball Message Possible values for message
     1: ... Team's snowball has passed the stage 1.
     2: ... Team's snowball has passed the stage 2.
     3: ... Team's snowball has passed the stage 3.
     4: ... Team is attacking the snowman, stopping the progress
     5: ... Team is moving again
     */
    public static byte[] Message(int team, int message) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SNOWBALL_MESSAGE.getValue());
        oPacket.write(team); // 0 is down, 1 is up
        oPacket.writeInt(message);
        return oPacket.getPacket();
    }

    public static byte[] Touch() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.LEFT_KNOCK_BACK.getValue());
        return oPacket.getPacket();
    }
}
