package net.server.handlers.Login;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import net.SendOpcode;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

public class ClientDumpLogHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        short callType = iPacket.readShort();
        int errorCode = iPacket.readInt();
        short backupBufferSize = iPacket.readShort();
        int rawSeq = iPacket.readInt();
        short type = iPacket.readShort();
        byte[] backupBuffer = iPacket.read(backupBufferSize - 6);
        
        String data = "- RawSeq: " + rawSeq + 
                "\r\n" + "- ErrorCode: " + errorCode + 
                "\r\n" + "- BackupBufferSize: " + backupBufferSize + 
                "\r\n" + "- CallType: " + callType + 
                "\r\n" + "- Character: " + (c.isPlayerNull() ? "" : c.getPlayer().getName()) + " Map: " + (c.isPlayerNull() ? "" : c.getPlayer().getMap().getId()) + " - Account: " + c.getAccountName() + 
                "\r\n" + "- Opcode: " + SendOpcode.getOpcodeByOp(type);
        System.out.println(data);
        Logger.log(LogType.INFO, LogFile.PACKET_ERROR, data);
    }

    @Override
    public boolean validateState(MapleClient c) {
        return true;
    }
}