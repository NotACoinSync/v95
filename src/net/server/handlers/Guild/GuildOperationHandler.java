package net.server.handlers.Guild;

import client.MapleCharacter;
import client.MapleClient;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import net.server.guild.MapleGuild;
import net.server.guild.MapleGuildResponse;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CField.userpool.UserRemote;
import tools.packets.WvsContext;
import tools.packets.WvsContext.GuildResult;

public final class GuildOperationHandler extends AbstractMaplePacketHandler {

    private List<Invited> invited = new LinkedList<>();
    private long nextPruneTime = System.currentTimeMillis() + 20 * 60 * 1000;

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        if (System.currentTimeMillis() >= nextPruneTime) {
            Iterator<Invited> itr = invited.iterator();
            Invited inv;
            while (itr.hasNext()) {
                inv = itr.next();
                if (System.currentTimeMillis() >= inv.expiration) {
                    itr.remove();
                }
            }
            nextPruneTime = System.currentTimeMillis() + 20 * 60 * 1000;
        }
        MapleCharacter player = c.getPlayer();
        byte type = iPacket.readByte();
        switch (type) {
            case GuildResult.Request.LoadGuild: {
                // c.announce(MaplePacketCreator.showGuildInfo(mc));
                break;
            }
            case GuildResult.Request.CheckGuildName: {
                // Create, should be CreateGuildAgree or CreateNewGuild but we don't do those checks
                if (player.getGuildId() > 0 || player.getMapId() != 200000301) {
                    player.dropMessage(1, "You cannot create a new Guild while in one.");
                    return;
                }
                if (player.getMeso() < MapleGuild.CREATE_GUILD_COST) {
                    player.dropMessage(1, "You do not have enough mesos to create a Guild.");
                    return;
                }
                String guildName = iPacket.readMapleAsciiString();
                if (!isGuildNameAcceptable(guildName)) {
                    player.dropMessage(1, "The Guild name you have chosen is not accepted.");
                    return;
                }
                int guildId = 0;
                try {
                    guildId = ChannelServer.getInstance().getWorldInterface().createGuild(player.getId(), guildName);
                } catch (RemoteException | NullPointerException ex) {
                    Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
                }
                if (guildId == 0) {
                    c.announce(WvsContext.GuildResult.genericGuildMessage((byte) 0x1c));
                    return;
                }
                player.gainMeso(-MapleGuild.CREATE_GUILD_COST, true, false, true);
                player.setGuildId(guildId);
                player.setGuildRank(1);
                player.saveGuildStatus();
                c.announce(WvsContext.GuildResult.showGuildInfo(player));
                try {
                    ChannelServer.getInstance().getWorldInterface().gainGP(guildId, player.getId(), 500);
                } catch (RemoteException | NullPointerException ex) {
                    Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
                }
                player.dropMessage(1, "You have successfully created a Guild.");
                player.getMap().announce(player, UserRemote.guildNameChanged(player.getId(), guildName));
                break;
            }
            case GuildResult.Request.InviteGuild:// Invite
                if (player.getGuildId() <= 0 || player.getGuildRank() > 2) {
                    return;
                }
                String name = iPacket.readMapleAsciiString();
                MapleGuildResponse mgr = MapleGuild.sendInvite(c, name);
                if (mgr != null) {
                    c.announce(mgr.getPacket());
                } else {
                    Invited inv = new Invited(name, player.getGuildId());
                    if (!invited.contains(inv)) {
                        invited.add(inv);
                    }
                }
                break;
            case GuildResult.Request.JoinGuild: {
                if (player.getGuildId() > 0) {
                    Logger.log(LogType.WARNING, LogFile.ANTICHEAT, "[hax] " + player.getName() + " attempted to join a guild when s/he is already in one.");
                    return;
                }
                int guildId = iPacket.readInt();
                int playerId = iPacket.readInt();
                if (playerId != player.getId()) {
                    Logger.log(LogType.WARNING, LogFile.ANTICHEAT, "[hax] " + player.getName() + " attempted to join a guild with a different character id.");
                    return;
                }
                name = player.getName().toLowerCase();
                Iterator<Invited> itr = invited.iterator();
                boolean bOnList = true;
                while (itr.hasNext()) {
                    Invited inv = itr.next();
                    if (guildId == inv.guildId && name.equals(inv.name)) {
                        bOnList = true;
                        itr.remove();
                        break;
                    }
                }
                if (!bOnList) {
                    Logger.log(LogType.WARNING, LogFile.ANTICHEAT, "[hax] " + player.getName() + " is trying to join a guild that never invited him/her (or that the invitation has expired)");
                    return;
                }
                player.setGuildId(guildId); // joins the guild
                player.setGuildRank(5); // start at lowest rank
                int s = 0;
                try {
                    s = ChannelServer.getInstance().getWorldInterface().addGuildMember(player.getMGC());
                } catch (RemoteException | NullPointerException ex) {
                    Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
                }
                if (s == 0) {
                    player.dropMessage(1, "The Guild you are trying to join is already full.");
                    player.setGuildId(0);
                    return;
                }
                c.announce(WvsContext.GuildResult.showGuildInfo(player));
                try {
                    ChannelServer.getInstance().getWorldInterface().gainGP(guildId, player.getId(), 500);
                } catch (RemoteException | NullPointerException ex) {
                    Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
                }
                player.saveGuildStatus(); // update database
                try {
                    player.getMap().announce(player, UserRemote.guildNameChanged(player.getId(), ""));
                    player.getMap().announce(player, UserRemote.guildMarkChanged(player.getId(), ChannelServer.getInstance().getWorldInterface().getGuild(guildId, null)));
                } catch (RemoteException | NullPointerException ex) {
                    Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
                }
                break;
            }
            case GuildResult.Request.WithdrawGuild: {
                int playerId = iPacket.readInt();
                name = iPacket.readMapleAsciiString();
                if (playerId != player.getId() || !name.equals(player.getName()) || player.getGuildId() <= 0) {
                    Logger.log(LogType.WARNING, LogFile.ANTICHEAT, "[hax] " + player.getName() + " tried to quit guild under the name \"" + name + "\" and current guild id of " + player.getGuildId() + ".");
                    return;
                }
                try {
                    ChannelServer.getInstance().getWorldInterface().removeGP(player.getGuildId(), player.getId(), player.getGP());
                } catch (RemoteException | NullPointerException ex) {
                    Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
                }
                c.announce(WvsContext.GuildResult.updateGP(player.getGuildId(), 0));
                try {
                    ChannelServer.getInstance().getWorldInterface().leaveGuild(player.getMGC());
                } catch (RemoteException | NullPointerException ex) {
                    Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
                }
                c.announce(WvsContext.GuildResult.showGuildInfo(null));
                player.setGuildId(0);
                player.saveGuildStatus();
                player.getMap().announce(player, UserRemote.guildNameChanged(player.getId(), ""));
                break;
            }
            case GuildResult.Request.KickGuild: {
                int playerId = iPacket.readInt();
                name = iPacket.readMapleAsciiString();
                if (player.getGuildRank() > 2 || player.getGuildId() <= 0) {
                    Logger.log(LogType.WARNING, LogFile.ANTICHEAT, "[hax] " + player.getName() + " is trying to expel without rank 1 or 2.");
                    return;
                }
                try {
                    ChannelServer.getInstance().getWorldInterface().expelMember(player.getMGC(), name, playerId);
                } catch (RemoteException | NullPointerException ex) {
                    Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
                }
                break;
            }
            case GuildResult.Request.SetGradeName: {
                if (player.getGuildId() <= 0 || player.getGuildRank() != 1) {
                    Logger.log(LogType.WARNING, LogFile.ANTICHEAT, "[hax] " + player.getName() + " tried to change guild rank titles when s/he does not have permission.");
                    return;
                }
                String ranks[] = new String[5];
                for (int i = 0; i < 5; i++) {
                    ranks[i] = iPacket.readMapleAsciiString();
                }
                try {
                    ChannelServer.getInstance().getWorldInterface().changeRankTitle(player.getGuildId(), ranks);
                } catch (RemoteException | NullPointerException ex) {
                    Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
                }
                break;
            }
            case GuildResult.Request.SetMemberGrade: {
                int playerId = iPacket.readInt();
                byte newRank = iPacket.readByte();
                if (player.getGuildRank() > 2 || (newRank <= 2 && player.getGuildRank() != 1) || player.getGuildId() <= 0) {
                    Logger.log(LogType.WARNING, LogFile.ANTICHEAT, "[hax] " + player.getName() + " is trying to change rank outside of his/her permissions.");
                    return;
                }
                if (newRank <= 1 || newRank > 5) {
                    return;
                }
                try {
                    ChannelServer.getInstance().getWorldInterface().changeRank(player.getGuildId(), playerId, newRank);
                } catch (RemoteException | NullPointerException ex) {
                    Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
                }
                break;
            }
            case GuildResult.Request.SetMark: {
                if (player.getGuildId() <= 0 || player.getGuildRank() != 1 || player.getMapId() != 200000301) {
                    Logger.log(LogType.WARNING, LogFile.ANTICHEAT, "[hax] " + player.getName() + " tried to change guild emblem without being the guild leader.");
                    return;
                }
                if (player.getGuild().getMeso() < MapleGuild.CHANGE_EMBLEM_COST) {
                    c.announce(WvsContext.BroadcastMsg.encode(1, "You're guild does not have enough mesos to create a Guild."));
                    return;
                }
                short bg = iPacket.readShort();
                byte bgColor = iPacket.readByte();
                short logo = iPacket.readShort();
                byte logoColor = iPacket.readByte();
                try {
                    ChannelServer.getInstance().getWorldInterface().setGuildEmblem(player.getGuildId(), bg, bgColor, logo, logoColor);
                    player.getGuild().removeMeso(MapleGuild.CHANGE_EMBLEM_COST);
                    player.getMap().announce(player, UserRemote.guildMarkChanged(player.getId(), bg, bgColor, logo, logoColor));
                } catch (RemoteException | NullPointerException ex) {
                    Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
                }
                break;
            }
            case GuildResult.Request.SetNotice: {
                if (player.getGuildId() <= 0 || player.getGuildRank() > 2) {
                    Logger.log(LogType.WARNING, LogFile.ANTICHEAT, "[hax] " + player.getName() + " tried to change guild notice while not in a guild.");
                    return;
                }
                String notice = iPacket.readMapleAsciiString();
                if (notice.length() > 100) {
                    return;
                }
                try {
                    ChannelServer.getInstance().getWorldInterface().setGuildNotice(player.getGuildId(), notice);
                } catch (RemoteException | NullPointerException ex) {
                    Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
                }
                break;
            }
            default: {
                Logger.log(LogType.INFO, LogFile.GENERAL_ERROR, "Unhandled GUILD_OPERATION packet: \n" + iPacket.toString());
            }
        }
    }

    private boolean isGuildNameAcceptable(String name) {
        if (name.length() < 3 || name.length() > 12) {
            return false;
        }
        for (int i = 0; i < name.length(); i++) {
            if (!Character.isLowerCase(name.charAt(i)) && !Character.isUpperCase(name.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private class Invited {

        public String name;
        public int guildId;
        public long expiration;

        public Invited(String n, int id) {
            name = n.toLowerCase();
            guildId = id;
            expiration = System.currentTimeMillis() + 60 * 60 * 1000;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Invited)) {
                return false;
            }
            Invited oth = (Invited) other;
            return (guildId == oth.guildId && name.equals(oth));
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 83 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 83 * hash + this.guildId;
            return hash;
        }
    }

}
