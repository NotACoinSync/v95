package tools.packets;

import constants.ServerConstants;
import java.net.InetAddress;
import net.SendOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

public class ClientSocket {
    
    /**
     Gets a packet telling the client the IP of the new channel.

     @param inetAddr The InetAddress of the requested channel server.
     @param port     The port the channel is on.

     @return The server IP packet.
     */
    public static byte[] MigrateCommand(InetAddress inetAddr, int port) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CHANGE_CHANNEL.getValue());
        oPacket.write(1);
        byte[] addr = inetAddr.getAddress();
        oPacket.write(addr);
        oPacket.writeShort(port);
        return oPacket.getPacket();
    }

    /**
     Sends a ping packet.

     @return The packet.
     */
    public static byte[] AliveReq() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PING.getValue());
        return oPacket.getPacket();
    }
    

    public static final byte[] AuthenCodeChanged() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.AUTHEN_CODE_CHANGED.getValue());
        oPacket.write(0);
        oPacket.writeInt(0);
        return oPacket.getPacket();
    }
    

    public static final byte[] AuthenMessage(int ulArgument, int nSessionCount) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.AUTHEN_MESSAGE.getValue());
        oPacket.writeInt(ulArgument);
        oPacket.write(nSessionCount);
        // CWvsContext::ShowPremiumArgument
        return oPacket.getPacket();
    }
    

    public static final byte[] CheckClientIntegrityRequest(short stResponseBuf, long stRequestBuf) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.AUTHEN_MESSAGE.getValue());
        oPacket.write(2);
        oPacket.writeShort(stResponseBuf);
        oPacket.writeLong(stRequestBuf);
        return oPacket.getPacket();
    }

    /**
     Gets the response to a relog request.

     @return The relog response packet.
     */
    public static byte[] getRelogResponse() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.RELOG_RESPONSE.getValue());
        oPacket.write(1);// 1 O.O Must be more types ):
        return oPacket.getPacket();
    }
    
    public static byte[] CheckCrcResult() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CHECK_CRC_RESULT.getValue());
        oPacket.write(1);
        return oPacket.getPacket();
    }


    /**
     Sends a hello packet.

     @param sendIv       the IV used by the server for sending
     @param recvIv       the IV used by the server for receiving

     @return
     */
    public static byte[] getHello(byte[] sendIv, byte[] recvIv) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(14);
        oPacket.writeShort(ServerConstants.VERSION);
        oPacket.writeMapleAsciiString(ServerConstants.PATCH);
        oPacket.write(recvIv);
        oPacket.write(sendIv);
        oPacket.write(8);// locale
        return oPacket.getPacket();
    }
}
