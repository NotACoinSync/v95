package net.server.channel.handlers;

import java.rmi.RemoteException;

import client.MapleCharacter;
import client.MapleClient;
import client.MessageType;
import client.autoban.AutobanFactory;
import constants.ServerConstants;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import tools.packets.PacketHelper;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.WvsContext;
import tools.packets.Field;

public final class PartyChatHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (player.isChatBanned()) {
            player.dropMessage(5, "You are curently banned from talking!");
            return;
        }
        if (player.getAutobanManager().getLastSpam(7) + 200 > System.currentTimeMillis()) {
            return;
        }
        slea.readInt();
        int type = slea.readByte(); // 0 for buddys, 1 for partys
        int numRecipients = slea.readByte();
        int recipients[] = new int[numRecipients];
        for (int i = 0; i < numRecipients; i++) {
            recipients[i] = slea.readInt();
        }
        String chattext = slea.readMapleAsciiString();
        if (chattext.length() > Byte.MAX_VALUE && !player.isGM()) {
            AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), c.getPlayer().getName() + " tried to packet edit chats with text length of " + chattext.length());
            c.disconnect(true, false);
            return;
        }
        if (type == 0) {
            try {
                ChannelServer.getInstance().getWorldInterface().buddyChat(recipients, player.getName(), player.getId(), chattext);
            } catch (RemoteException | NullPointerException ex) {
                Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
                player.dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
                c.announce(WvsContext.enableActions());
            }
        } else if (type == 1 && player.isInParty()) {
            try {
                ChannelServer.getInstance().getWorldInterface().partyChat(player.getParty(), chattext, player.getName());
            } catch (RemoteException | NullPointerException ex) {
                Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
                player.dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
                c.announce(WvsContext.enableActions());
            }
        } else if (type == 2 && player.getGuildId() > 0) {
            try {
                ChannelServer.getInstance().getWorldInterface().guildChat(player.getGuildId(), player.getName(), player.getId(), chattext);
            } catch (RemoteException | NullPointerException ex) {
                Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
                player.dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
                c.announce(WvsContext.enableActions());
            }
        } else if (type == 3 && player.getGuild() != null) {
            int allianceId = player.getGuild().getAllianceId();
            if (allianceId > 0) {
                try {
                    ChannelServer.getInstance().getWorldInterface().allianceMessage(allianceId, Field.GroupMessage(player.getName(), chattext, 3), player.getId(), -1);
                } catch (RemoteException | NullPointerException ex) {
                    Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
                    player.dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
                    c.announce(WvsContext.enableActions());
                }
            }
        }
        player.getAutobanManager().spam(7);
    }
}
