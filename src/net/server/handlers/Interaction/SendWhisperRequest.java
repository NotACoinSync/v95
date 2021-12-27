package net.server.handlers.Interaction;

import client.CharacterLocation;
import client.MapleClient;
import client.autoban.AutobanFactory;
import client.autoban.AutobanManager;
import java.rmi.RemoteException;
import java.util.ArrayList;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import tools.BigBrother;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.Field;

public final class SendWhisperRequest extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        byte mode = iPacket.readByte();
        AutobanManager abm = c.getPlayer().getAutobanManager();
        abm.setTimestamp(0, iPacket.readInt(), 3);
        switch (mode) {
            case 6: // Whisper 
            {
                String WhisperTarget = iPacket.readMapleAsciiString();
                WhisperTarget = WhisperTarget.replace("[GM] ", "");
                String text = iPacket.readMapleAsciiString();
                if (c.getPlayer().isChatBanned()) {
                    c.getPlayer().dropMessage(5, "You are curently banned from talking!");
                    return;
                }
                if (c.getPlayer().getAutobanManager().getLastSpam(7) + 200 > System.currentTimeMillis()) {
                    return;
                }
                if (text.length() > Byte.MAX_VALUE && !c.getPlayer().isGM() && c.getPlayer().getClient().getGMLevel() < 1) {
                    AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), c.getPlayer().getName() + " tried to packet edit with whispers with text length of " + text.length());
                    c.disconnect(false, false);
                    return;
                }
                // MapleCharacter player = c.getWorldServer().getCharacterByName(recipient);
                ArrayList<String> player = new ArrayList<>();
                player.add(WhisperTarget);
                // if(player != null){
                try {
                    CharacterLocation findInfo = ChannelServer.getInstance().getWorldInterface().find(WhisperTarget);
                    ChannelServer.getInstance().getWorldInterface().broadcastPacketToPlayers(player, Field.Whisper.getWhisper(c.getPlayer().getName(), c.getChannel(), c.getPlayer().isGM(), text));
                    if (findInfo == null || findInfo.gmLevel > c.getPlayer().getGMLevel()) {
                        c.announce(Field.Whisper.getWhisperReply(WhisperTarget, (byte) 0));
                    } else {
                        c.announce(Field.Whisper.getWhisperReply(WhisperTarget, (byte) 1));
                    }
                    BigBrother.whisper("[" + WhisperTarget + "] " + text, c.getPlayer().getName(), WhisperTarget);
                } catch (RemoteException | NullPointerException ex) {
                    Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
                    c.announce(Field.Whisper.getWhisperReply(WhisperTarget, (byte) 0));
                }
                c.getPlayer().getAutobanManager().spam(7);
                break;
            }
            case 5: // SendChatMsgWhisper
            {
                String recipient = iPacket.readMapleAsciiString();
                try {
                    CharacterLocation findInfo = ChannelServer.getInstance().getWorldInterface().find(recipient);
                    if (findInfo != null) {
                        if (findInfo.gmLevel > c.getPlayer().getGMLevel()) {
                            c.announce(Field.Whisper.getWhisperReply(recipient, (byte) 0));
                            return;
                        }
                        if (findInfo.cashshop || findInfo.mts) {
                            c.announce(Field.Whisper.getFindReply(recipient, -1, findInfo.cashshop ? 2 : 0));
                            return;
                        }
                        if (findInfo.channel != c.getChannel()) {
                            c.announce(Field.Whisper.getFindReply(recipient, findInfo.channel, 3));
                        } else {
                            c.announce(Field.Whisper.getFindReply(recipient, findInfo.mapid, 1));
                        }
                    } else {
                        c.announce(Field.Whisper.getWhisperReply(recipient, (byte) 0));
                    }
                } catch (RemoteException | NullPointerException ex) {
                    Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
                    c.announce(Field.Whisper.getWhisperReply(recipient, (byte) 0));
                }
                break;
            }
            case 68: // SendLocationWhisper
            {
                String WhisperTarget = iPacket.readMapleAsciiString();
                WhisperTarget = WhisperTarget.replace("[GM] ", "");
                if (c.getPlayer().isChatBanned()) {
                    c.getPlayer().dropMessage(5, "You are curently banned from whisper!");
                    return;
                }
                if (c.getPlayer().getAutobanManager().getLastSpam(7) + 200 > System.currentTimeMillis()) {
                    return;
                }
                try {
                    CharacterLocation findInfo = ChannelServer.getInstance().getWorldInterface().find(WhisperTarget);
                    if (findInfo == null || findInfo.gmLevel > c.getPlayer().getGMLevel()) {
                        c.getPlayer().message("Please check again.");
                        return;
                    } else {
                        c.getPlayer().message("Your whisper friend is at " + findInfo.mapid);
                    }
                } catch (RemoteException | NullPointerException ex) {
                    Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
                }
                c.getPlayer().getAutobanManager().spam(7);
                break;
            }
            default:
                break;
        }
    }
}
