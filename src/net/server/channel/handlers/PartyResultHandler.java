package net.server.channel.handlers;

import client.MapleCharacter;
import client.MapleClient;
import client.MessageType;
import constants.ServerConstants;
import java.rmi.RemoteException;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import net.server.world.PartyOperation;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.WvsContext;

public final class PartyResultHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        int operation = iPacket.readByte();
        MapleCharacter player = c.getPlayer();
        MapleParty party = player.getParty();
        MaplePartyCharacter partyplayer = player.getMPC();
        switch (operation) {
            case WvsContext.PartyResult.Result.InviteParty_Sent: {
                break;
            }
            case WvsContext.PartyResult.Result.InviteParty_Rejected: {
                if (player.isIronMan()) {
                    c.announce(WvsContext.enableActions());
                    return;
                }
                int partyid = iPacket.readInt();
                try {
                    party = ChannelServer.getInstance().getWorldInterface().getParty(partyid);
                    ChannelServer.getInstance().getWorldInterface().removePartyInvited(party.getId(), c.getPlayer().getName());
                } catch (Exception ex) {
                    Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
                    player.dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
                    c.announce(WvsContext.enableActions());
                }
                break;
            }
            case WvsContext.PartyResult.Result.InviteParty_Accepted: {
                if (player.isIronMan()) {
                    c.announce(WvsContext.enableActions());
                    return;
                }
                int partyid = iPacket.readInt();
                try {
                    if (c.getPlayer().getParty() == null) {
                        party = ChannelServer.getInstance().getWorldInterface().getParty(partyid);
                        if (party != null) {
                            if (party.getMembers().size() < 6) {
                                if (ChannelServer.getInstance().getWorldInterface().isPartyInvited(party.getId(), c.getPlayer().getName())) {
                                    ChannelServer.getInstance().getWorldInterface().removePartyInvited(party.getId(), c.getPlayer().getName());
                                    partyplayer = new MaplePartyCharacter(player);
                                    ChannelServer.getInstance().getWorldInterface().updateParty(party.getId(), PartyOperation.JOIN, partyplayer);
                                    player.receivePartyMembers();
                                    player.updatePartyCharacter();
                                } else {
                                    c.announce(WvsContext.PartyResult.partyStatusMessage(1));
                                }
                            } else {
                                c.announce(WvsContext.PartyResult.partyStatusMessage(17));
                            }
                        } else {
                            c.announce(WvsContext.BroadcastMsg.encode(5, "The person you have invited to the party is already in one."));
                        }
                    } else {
                        c.announce(WvsContext.BroadcastMsg.encode(5, "You can't join the party as you are already in one."));
                    }
                } catch (RemoteException ex) {
                    Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
                    player.dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
                    c.announce(WvsContext.enableActions());
                }
                break;
            }
            default:
                Logger.log(LogType.INFO, LogFile.GENERAL_INFO, "Unknown Party Result: " + operation + " Data: " + iPacket.toString());
                System.out.println("Unknown Party Result: " + operation + " Data: " + iPacket.toString());
                break;
        }
    }
}
