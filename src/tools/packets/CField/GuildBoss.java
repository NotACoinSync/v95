package tools.packets.CField;

import net.SendOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

public class GuildBoss {

    public static byte[] HealerMove(int Y) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.GUILD_BOSS_HEALER_MOVE.getValue());
        oPacket.writeShort(Y);
        return oPacket.getPacket();
    }

    public static byte[] PulleyStateChange(int State) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.GUILD_BOSS_PULLEY_STATE_CHANGE.getValue());
        oPacket.write(State);
        return oPacket.getPacket();
    }    
}
