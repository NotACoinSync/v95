package tools.packets.CField;

import net.SendOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

public class RPSGameDlg {
    
    public static final byte NotEnoughMeso = 0x6,
            InventoryFull = 0x7,
            OpenNPC = 0x8,
            Start = 0x9,
            ShowResult = 0xA,
            SelectNPC = 0xB,
            Retry = 0xC,
            Destroy = 0xD,
            SetMainButton = 0x14;          

    public static byte[] encode(byte type) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.RPS_GAME.getValue());
        oPacket.write(type);
        return oPacket.getPacket();
    }

    public static byte[] OpenNPC(int dwNpcTemplateID) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.RPS_GAME.getValue());
        oPacket.write(0x8);
        oPacket.writeInt(dwNpcTemplateID); // 9000019
        /* #e#bRock, Paper, Scissors Notice#n#k
            \r\nThe participation fee for this Rock, Paper, Scissors game is #r1,000 mesos#k.
            \r\nWinning games will allow you to obtain various Winning Streak Certificates awarded for each consecutive win, but failure to complete the challenge will result in not receiving the Cerficate.
            \r\nThe received Certificate can be traded with the NPC's Paul, Jean, Martin, and Tony.
        */
        return oPacket.getPacket();
    }

    public static byte[] Select(byte NpcSelect, byte CntStraightVictories) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.RPS_GAME.getValue());
        oPacket.write(0xB);
        oPacket.write(NpcSelect);
        oPacket.write(CntStraightVictories);
        return oPacket.getPacket();
    }
}
