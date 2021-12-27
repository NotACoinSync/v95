/*
 * This file is part of the OdinMS Maple Story Server
 * Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 * Matthias Butz <matze@odinms.de>
 * Jan Christian Meyer <vimes@odinms.de>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation version 3 as published by
 * the Free Software Foundation. You may not use, modify or distribute
 * this program under any other version of the GNU Affero General Public
 * License.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.server.channel.handlers;

import java.rmi.RemoteException;

import client.CharacterLocation;
import client.MapleClient;
import client.MessageType;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import net.server.guild.MapleAlliance;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.WvsContext;

/**
 * @author XoticStory
 */
public final class AllianceOperationHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        try {
            System.out.println("Alliance operation");
            MapleAlliance alliance = null;
            if (c.getPlayer().getGuild() != null && c.getPlayer().getGuild().getAllianceId() > 0) {
                alliance = ChannelServer.getInstance().getWorldInterface().getAlliance(c.getPlayer().getGuild().getAllianceId());
            }
            if (alliance == null) {
                c.getPlayer().dropMessage("You are not in an alliance.");
                c.announce(WvsContext.enableActions());
                return;
            } else if (c.getPlayer().getMGC().getAllianceRank() > 2 || !alliance.getGuilds().contains(c.getPlayer().getGuildId())) {
                c.announce(WvsContext.enableActions());
                return;
            }
            switch (slea.readByte()) {
                case 0x01:
                    ChannelServer.getInstance().getWorldInterface().allianceMessage(alliance.getId(), WvsContext.AllianceResult.sendShowInfo(c.getPlayer().getGuild().getAllianceId(), c.getPlayer().getId()), -1, -1);
                    break;
                case 0x02: { // Leave Alliance
                    if (c.getPlayer().getGuild().getAllianceId() == 0 || c.getPlayer().getGuildId() < 1 || c.getPlayer().getGuildRank() != 1) {
                        return;
                    }
                    ChannelServer.getInstance().getWorldInterface().allianceMessage(alliance.getId(), WvsContext.AllianceResult.sendChangeGuild(c.getPlayer().getGuildId(), c.getPlayer().getId(), c.getPlayer().getGuildId(), 2), -1, -1);
                    break;
                }
                case 0x03: // send alliance invite
                    String charName = slea.readMapleAsciiString();
                    CharacterLocation location = ChannelServer.getInstance().getWorldInterface().find(charName);
                    if (location == null) {
                        c.getPlayer().dropMessage("The player is not online.");
                    } else {
                        String status = ChannelServer.getInstance().getWorldInterface().getGuildInviteStatus(charName);
                        if (status != null) {
                            if (status.equals("Good")) {
                                ChannelServer.getInstance().getWorldInterface().allianceMessage(alliance.getId(), WvsContext.AllianceResult.sendInvitation(c.getPlayer().getGuild().getAllianceId(), c.getPlayer().getId(), "test"), -1, -1);
                            } else {
                                c.getPlayer().dropMessage(status);
                            }
                        }
                    }
                    break;
                case 0x04: {
                    int guildid = slea.readInt();
                    // slea.readMapleAsciiString();//guild name
                    if (c.getPlayer().getGuild().getAllianceId() != 0 || c.getPlayer().getGuildRank() != 1 || c.getPlayer().getGuildId() < 1) {
                        return;
                    }
                    ChannelServer.getInstance().getWorldInterface().allianceMessage(alliance.getId(), WvsContext.AllianceResult.sendChangeGuild(guildid, c.getPlayer().getId(), c.getPlayer().getGuildId(), 0), -1, -1);
                    break;
                }
                case 0x06: { // Expel Guild
                    int guildid = slea.readInt();
                    int allianceid = slea.readInt();
                    if (c.getPlayer().getGuild().getAllianceId() == 0 || c.getPlayer().getGuild().getAllianceId() != allianceid) {
                        return;
                    }
                    ChannelServer.getInstance().getWorldInterface().allianceMessage(alliance.getId(), WvsContext.AllianceResult.sendChangeGuild(allianceid, c.getPlayer().getId(), guildid, 1), -1, -1);
                    break;
                }
                case 0x07: { // Change Alliance Leader
                    if (c.getPlayer().getGuild().getAllianceId() == 0 || c.getPlayer().getGuildId() < 1) {
                        return;
                    }
                    ChannelServer.getInstance().getWorldInterface().allianceMessage(alliance.getId(), WvsContext.AllianceResult.sendChangeLeader(c.getPlayer().getGuild().getAllianceId(), c.getPlayer().getId(), slea.readInt()), -1, -1);
                    break;
                }
                case 0x08:
                    String ranks[] = new String[5];
                    for (int i = 0; i < 5; i++) {
                        ranks[i] = slea.readMapleAsciiString();
                    }
                    ChannelServer.getInstance().getWorldInterface().setAllianceRanks(alliance.getId(), ranks);
                    ChannelServer.getInstance().getWorldInterface().allianceMessage(alliance.getId(), WvsContext.AllianceResult.changeAllianceRankTitle(alliance.getId(), ranks), -1, -1);
                    break;
                case 0x09: {
                    int int1 = slea.readInt();
                    byte byte1 = slea.readByte();
                    ChannelServer.getInstance().getWorldInterface().allianceMessage(alliance.getId(), WvsContext.AllianceResult.sendChangeRank(c.getPlayer().getGuild().getAllianceId(), c.getPlayer().getId(), int1, byte1), -1, -1);
                    break;
                }
                case 0x0A:
                    String notice = slea.readMapleAsciiString();
                    ChannelServer.getInstance().getWorldInterface().setAllianceNotice(alliance.getId(), notice);
                    ChannelServer.getInstance().getWorldInterface().allianceMessage(alliance.getId(), WvsContext.AllianceResult.allianceNotice(alliance.getId(), notice), -1, -1);
                    break;
                default:
                    Logger.log(LogType.INFO, LogFile.GENERAL_ERROR, "Unhandled AllIANCE_OPERATION packet: \n" + slea.toString());
            }
            alliance.saveToDB();
        } catch (RemoteException | NullPointerException ex) {
            Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
            c.announce(WvsContext.enableActions());
            c.getPlayer().dropMessage(MessageType.ERROR, "Error communicating if the WorldServer.");
        }
    }
}
