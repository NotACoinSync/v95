package net.server.channel.handlers;

import client.*;
import client.player.SecondaryStat;
import constants.ServerConstants;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import net.server.PlayerBuffValueHolder;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import net.server.world.PartyOperation;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.WvsContext;

public final class PartyRequestHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        int operation = iPacket.readByte();
        MapleCharacter player = c.getPlayer();
        MapleParty party = player.getParty();
        MaplePartyCharacter partyplayer = player.getMPC();
        switch (operation) {
            case WvsContext.PartyResult.Request.CreateNewParty: {
                if (player.getLevel() < 10) {
                    c.announce(WvsContext.PartyResult.partyStatusMessage(10));
                    return;
                }
                if (player.getParty() == null) {
                    partyplayer = new MaplePartyCharacter(player);
                    try {
                        party = ChannelServer.getInstance().getWorldInterface().createParty(partyplayer);
                        player.setParty(party);
                        player.setMPC(partyplayer);
                        player.silentPartyUpdate();
                        c.announce(WvsContext.PartyResult.partyCreated(party, partyplayer));
                    } catch (NullPointerException | RemoteException ex) {
                        Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
                        player.dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
                    }
                } else {
                    c.announce(WvsContext.BroadcastMsg.encode(5, "You can't create a party as you are already in one."));
                }
                break;
            }
            case WvsContext.PartyResult.Request.WithdrawParty: {
                if (party != null && partyplayer != null) {
                    if (partyplayer.equals(party.getLeader())) {
                        try {
                            ChannelServer.getInstance().getWorldInterface().updateParty(party.getId(), PartyOperation.DISBAND, partyplayer);
                        } catch (RemoteException | NullPointerException ex) {
                            Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
                            player.dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
                            c.announce(WvsContext.enableActions());
                        }
                        if (player.getEventInstance() != null) {
                            player.getEventInstance().disbandParty();
                        }
                    } else {
                        try {
                            ChannelServer.getInstance().getWorldInterface().updateParty(party.getId(), PartyOperation.LEAVE, partyplayer);
                        } catch (RemoteException | NullPointerException ex) {
                            Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
                            player.dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
                            c.announce(WvsContext.enableActions());
                        }
                        if (player.getEventInstance() != null) {
                            player.getEventInstance().leftParty(player);
                        }
                    }
                    for (PlayerBuffValueHolder buff : player.getAllBuffs()) {
                        for (Pair<SecondaryStat, BuffDataHolder> p : buff.getEffect().getStatups()) {
                            if (p.left.equals(SecondaryStat.DarkAura) || p.left.equals(SecondaryStat.BlueAura) || p.left.equals(SecondaryStat.YellowAura)) {
                                if (player.getSkillLevel(p.right.getSourceID()) <= 0) {
                                    player.cancelEffectFromSecondaryStat(p.left);
                                }
                            }
                        }
                    }
                    player.setParty(null);
                }
                break;
            }
            case WvsContext.PartyResult.Request.JoinParty: {
                break;
            }
            case WvsContext.PartyResult.Request.InviteParty: {
                String name = iPacket.readMapleAsciiString();
                try {
                    String response = ChannelServer.getInstance().getWorldInterface().getGuildInviteResponse(name);
                    // MapleCharacter invited = world.getCharacterByName(name);
                    if (response != null) {
                        if (response.equals("below10")) {
                            // if(invited.getLevel() < 10){ // min requirement is level 10
                            c.announce(WvsContext.BroadcastMsg.encode(5, "The player you have invited does not meet the requirements."));
                            return;
                        }
                        if (response.equals("ironman") || player.isIronMan()) {
                            // if(invited.isIronMan() || player.isIronMan()){
                            c.announce(WvsContext.enableActions());
                            return;
                        }
                        // if(invited.getParty() == null){
                        if (!response.equals("inparty")) {
                            if (player.getParty() == null) {
                                partyplayer = new MaplePartyCharacter(player);
                                try {
                                    party = ChannelServer.getInstance().getWorldInterface().createParty(partyplayer);
                                    player.setParty(party);
                                    player.setMPC(partyplayer);
                                    c.announce(WvsContext.PartyResult.partyCreated(party, partyplayer));
                                } catch (RemoteException | NullPointerException ex) {
                                    Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
                                    player.dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
                                    c.announce(WvsContext.enableActions());
                                }
                            }
                            if (party.getMembers().size() < 6) {
                                // player.getParty().addInvited(name);
                                List<String> toPlayer = new ArrayList<>();
                                toPlayer.add(name);
                                try {
                                    ChannelServer.getInstance().getWorldInterface().addPartyInvited(player.getParty().getId(), name);
                                    ChannelServer.getInstance().getWorldInterface().broadcastPacketToPlayers(toPlayer, WvsContext.PartyResult.partyInvite(player));
                                } catch (RemoteException | NullPointerException ex) {
                                    Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
                                    player.dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
                                    c.announce(WvsContext.enableActions());
                                }
                                // invited.getClient().announce(MaplePacketCreator.partyInvite(player));
                                // invited.getClient().announce(MaplePacketCreator.partyInvite(player));
                            } else {
                                c.announce(WvsContext.PartyResult.partyStatusMessage(17));
                            }
                        } else {
                            c.announce(WvsContext.PartyResult.partyStatusMessage(16));
                        }
                    } else {
                        c.announce(WvsContext.PartyResult.partyStatusMessage(19));
                    }
                } catch (RemoteException | NullPointerException ex) {
                    Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
                    player.dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
                    c.announce(WvsContext.enableActions());
                }
                break;
            }
            case WvsContext.PartyResult.Request.KickParty: {
                int cid = iPacket.readInt();
                if (partyplayer.equals(party.getLeader())) {
                    MaplePartyCharacter expelled = party.getMemberById(cid);
                    if (expelled != null) {
                        try {
                            ChannelServer.getInstance().getWorldInterface().updateParty(party.getId(), PartyOperation.EXPEL, expelled);
                        } catch (RemoteException | NullPointerException ex) {
                            Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
                            player.dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
                            c.announce(WvsContext.enableActions());
                        }
                        if (player.getEventInstance() != null) {
                            if (expelled.isOnline()) {
                                player.getEventInstance().disbandParty();
                            }
                        }
                    }
                }
                break;
            }
            case WvsContext.PartyResult.Request.ChangePartyBoss: {
                int newLeader = iPacket.readInt();
                MaplePartyCharacter newLeadr = party.getMemberById(newLeader);
                party.setLeader(newLeadr);
                try {
                    ChannelServer.getInstance().getWorldInterface().updateParty(party.getId(), PartyOperation.CHANGE_LEADER, newLeadr);
                } catch (RemoteException | NullPointerException ex) {
                    Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
                    player.dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
                    c.announce(WvsContext.enableActions());
                }
                break;
            }
        }
    }
}
