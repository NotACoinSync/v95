package tools.packets.CField;

import client.MapleCharacter;
import client.MapleCharacterLook;
import net.SendOpcode;
import net.server.world.MapleMessenger;
import net.server.world.MapleMessengerCharacter;
import tools.data.output.MaplePacketLittleEndianWriter;
import static tools.packets.PacketHelper.encodeAvatarLook;
import static tools.packets.PacketHelper.encodeAvatarLook;

public class UIMessenger {

    public static class UIMessengerType {

        public static final int Enter = 0,
                SelfEnterResult = 1,
                Leave = 2,
                Invite = 3,
                InviteResult = 4,
                Blocked = 5,
                Chat = 6,
                Avatar = 7,
                Migrated = 8;
    }

    public static byte[] Enter(MapleCharacter chr, int position) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MESSENGER.getValue());
        oPacket.write(UIMessengerType.Enter);
        oPacket.write(position);
        encodeAvatarLook(oPacket, chr, true);
        oPacket.writeMapleAsciiString(chr.getName());
        oPacket.write(chr.getClient().getChannel());
        oPacket.write(0);
        return oPacket.getPacket();
    }

    public static byte[] Enter(MapleCharacterLook chr, String from, int position, int channel) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MESSENGER.getValue());
        oPacket.write(UIMessengerType.Enter);
        oPacket.write(position);
        encodeAvatarLook(oPacket, chr, true);
        oPacket.writeMapleAsciiString(from);
        oPacket.write(channel);
        oPacket.write(0);
        return oPacket.getPacket();
    }

    public static byte[] SelfEnterResult(int position) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MESSENGER.getValue());
        oPacket.write(UIMessengerType.SelfEnterResult);
        oPacket.write(position);
        return oPacket.getPacket();
    }

    public static byte[] Leave(int position) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MESSENGER.getValue());
        oPacket.write(UIMessengerType.Leave);
        oPacket.write(position);
        return oPacket.getPacket();
    }

    public static byte[] Invite(String CharacterName, int messengerid, int nChannelID) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MESSENGER.getValue());
        oPacket.write(UIMessengerType.Invite);
        oPacket.writeMapleAsciiString(CharacterName);
        oPacket.write(nChannelID);
        oPacket.writeInt(messengerid);
        oPacket.write(0);
        return oPacket.getPacket();
    }

    public static byte[] InviteResult(String CharacterName, boolean invited) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MESSENGER.getValue());
        oPacket.write(UIMessengerType.InviteResult);
        oPacket.writeMapleAsciiString(CharacterName);
        oPacket.writeBoolean(invited);
        return oPacket.getPacket();
    }

    public static byte[] Blocked(String BlockedUser, boolean blocked) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MESSENGER.getValue());
        oPacket.write(UIMessengerType.Blocked);
        oPacket.writeMapleAsciiString(BlockedUser);
        oPacket.writeBoolean(!blocked);
        return oPacket.getPacket();
    }

    public static byte[] Chat(String text) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MESSENGER.getValue());
        oPacket.write(UIMessengerType.Chat);
        oPacket.writeMapleAsciiString(text);
        return oPacket.getPacket();
    }

    public static byte[] Avatar(MapleCharacter chr, int position) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MESSENGER.getValue());
        oPacket.write(UIMessengerType.Avatar);
        oPacket.write(position);
        encodeAvatarLook(oPacket, chr, true);
        return oPacket.getPacket();
    }

    public static byte[] Avatar(MapleCharacterLook chr, int position) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MESSENGER.getValue());
        oPacket.write(UIMessengerType.Avatar);
        oPacket.write(position);
        encodeAvatarLook(oPacket, chr, true);
        return oPacket.getPacket();
    }

    public static byte[] Migrated(MapleMessenger messenger) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MESSENGER.getValue());
        oPacket.write(UIMessengerType.Migrated);
        for (MapleMessengerCharacter MessCharacter : messenger.getMembers()) {
            oPacket.write(MessCharacter.getId());
            encodeAvatarLook(oPacket, MessCharacter.getCharacter(), true);
            oPacket.writeMapleAsciiString(MessCharacter.getName());
            oPacket.write(MessCharacter.getChannel());
        }
        return oPacket.getPacket();
    }
}
