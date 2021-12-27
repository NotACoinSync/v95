package net.server.channel.handlers;

import client.MapleCharacter;
import client.MapleClient;
import client.autoban.AutobanFactory;
import client.command.CommandHandler;
import client.command.Commands;
import tools.BigBrother;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CField.userpool.UserCommon;

public final class SendChatMessage extends net.AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (player == null) {
            c.disconnect(true, false);
            return;
        }
        int timestamp = iPacket.readInt();
        player.getAutobanManager().setTimestamp(4, timestamp, 3);
        
        String text = iPacket.readMapleAsciiString();
        if (player.isChatBanned()) {
            player.dropMessage(5, "You are curently banned from using chat!");
            return;
        }
        if (player.getAutobanManager().getLastSpam(7) + 200 > System.currentTimeMillis()) {
            return;
        }
        if (text.length() > Byte.MAX_VALUE && !player.isGM()) {
            AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), c.getPlayer().getName() + " tried to packet edit in General Chat with text length of " + text.length());
            c.disconnect(false, false);
            return;
        }
        char heading = text.charAt(0);
        if (heading == '/' || heading == '!' || heading == '@') {
            String[] sp = text.split(" ");
            sp[0] = sp[0].toLowerCase().substring(1);
            // TimerManager.getInstance().execute("generalChat-" + sp[0], ()-> {
            if (!CommandHandler.handleCommand(c, text)) {
                if (!Commands.executePlayerCommand(c, sp, heading)) {
                    if (player.isGM()) {
                        if (Commands.executeGMCommand(c, sp)) {
                            Logger.log(LogType.INFO, LogFile.COMMAND, c.getAccountName(), MapleCharacter.makeMapleReadable(c.getPlayer().getName()) + " used: " + text);
                        }
                    }
                } else {
                    Logger.log(LogType.INFO, LogFile.COMMAND, c.getAccountName(), MapleCharacter.makeMapleReadable(c.getPlayer().getName()) + " used: " + text);
                }
            } else {
                Logger.log(LogType.INFO, LogFile.COMMAND, c.getAccountName(), MapleCharacter.makeMapleReadable(c.getPlayer().getName()) + " used: " + text);
            }
            // });
        } else {
            boolean bOnlyBalloon = iPacket.readBoolean();
            if (player.getMap().isMuted() && !player.isGM()) {
                player.dropMessage(5, "The map you are in is currently muted. Please try again later.");
                return;
            }
            if (!player.isHidden()) {
                player.getMap().announce(UserCommon.Chat(player.getId(), text, player.getWhiteChat(), bOnlyBalloon));
            } else {
                player.getMap().announceGM(UserCommon.Chat(player.getId(), text, player.getWhiteChat(), bOnlyBalloon));
            }
            if (!bOnlyBalloon) {
                BigBrother.general(text, player.getName(), player.getMap().getAllPlayer());
            }
        }
        player.getAutobanManager().spam(7);
    }
}
