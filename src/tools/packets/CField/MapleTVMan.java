package tools.packets.CField;

import client.MapleCharacter;
import java.util.List;
import net.SendOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;
import static tools.packets.PacketHelper.encodeAvatarLook;

public class MapleTVMan {

    /**
     * Sends MapleTV
     *
     * @param chr The character shown in TV
     * @param messages The message sent with the TV
     * @param type The type of TV
     * @param partner The partner shown with chr
     * @return the SEND_TV packet
     */
    public static byte[] SetMessage(MapleCharacter chr, List<String> messages, int type, MapleCharacter partner) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SEND_TV.getValue());
        oPacket.write(partner != null ? 3 : 1);
        oPacket.write(type); // Heart = 2 Star = 1 Normal = 0
        encodeAvatarLook(oPacket, chr, false);
        oPacket.writeMapleAsciiString(chr.getName());
        if (partner != null) {
            oPacket.writeMapleAsciiString(partner.getName());
        } else {
            oPacket.writeShort(0);
        }
        for (int i = 0; i < messages.size(); i++) {
            if (i == 4 && messages.get(4).length() > 15) {
                oPacket.writeMapleAsciiString(messages.get(4).substring(0, 15));
            } else {
                oPacket.writeMapleAsciiString(messages.get(i));
            }
        }
        oPacket.writeInt(1337); // time limit shit lol 'Your thing still start in blah blah seconds'
        if (partner != null) {
            encodeAvatarLook(oPacket, partner, false);
        }
        return oPacket.getPacket();
    }

    /**
     * Removes TV
     *
     * @return The Remove TV Packet
     */
    public static byte[] ClearMessage() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.REMOVE_TV.getValue());
        return oPacket.getPacket();
    }

    public static byte[] SendMessageResult() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.ENABLE_TV.getValue());
        oPacket.write(1);
        oPacket.write(2);
        return oPacket.getPacket();
    }
}
