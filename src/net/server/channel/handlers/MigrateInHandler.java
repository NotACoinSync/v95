package net.server.channel.handlers;

import client.*;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import constants.FeatureSettings;
import constants.GameConstants;
import constants.ServerConstants;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import net.server.PlayerBuffValueHolder;
import net.server.channel.Channel;
import net.server.channel.CharacterIdChannelPair;
import net.server.guild.MapleAlliance;
import net.server.guild.MapleGuild;
import net.server.world.MaplePartyCharacter;
import net.server.world.PartyOperation;
import net.world.family.Family;
import net.world.family.FamilyCharacter;
import server.ItemInformationProvider;
import server.quest.MapleQuest;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CField.FuncKeyMappedMan;
import tools.packets.CField.NpcPool;
import tools.packets.FamilyPackets;
import tools.packets.Login;
import tools.packets.Stage;
import tools.packets.WvsContext;

public final class MigrateInHandler extends AbstractMaplePacketHandler {

    @Override
    public final boolean validateState(MapleClient c) {
        return !c.isLoggedIn();
    }

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, final MapleClient c) {
        long start = System.currentTimeMillis();
        final int cid = iPacket.readInt();
        MapleCharacter player = ChannelServer.getInstance().getMTSCharacterById(cid);
        if (player != null) {
            ChannelServer.getInstance().removeMTSPlayer(cid);
        }
        if (player == null) {
            player = ChannelServer.getInstance().getPlayerFromTempStorage(cid);
            if (player != null) {
                ChannelServer.getInstance().removePlayerFromTempStorage(cid);
            }
        }
        boolean newcomer = false;
        if (player == null) {
            try {
                player = MapleCharacter.loadCharFromDB(cid, c, true);
                newcomer = true;
            } catch (SQLException e) {
                Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
            }
        } else {
            player.newClient(c);
        }
        if (player == null) {
            c.disconnect(true, true, false);
            return;
        }
        c.setPlayer(player);
        addPlayerToField(c);
        c.setAccID(player.getAccountID());
        c.updateLoginState(MapleClient.LOGIN_LOGGEDIN);
        // Kết thúc cấu trúc cho Login.
        addKeyMap(c);
        addDragon(c);
        addBuffs(c, cid);
        addFriends(c);
        addGuild(c, newcomer);
        addParty(c);
        addMessenger(c);
        player.showNote();
        addEquip(c);
        addPet(c, newcomer);
        addGender(c);
        addReport(c);
        addSkills(c);
        addScriptedNPC(c);
        addCouple(c);
        addAlphaItem(c);
        addEvents(c);
        addHourChanged(c);
        addElite(c);
        addQuests(c);
        c.isFakeLogin = false;
        Logger.log(LogType.INFO, LogFile.LOGIN, "Account: " + c.getAccountName() + " Player: " + c.getPlayer().getName() + " logged in at " + Calendar.getInstance().getTime().toString() + " Took: " + ((System.currentTimeMillis() - start) / 1000.0) + " seconds.");
    }

    private void addAlphaItem(MapleClient c) {
        if (c.isAlphaUser()) {
            if (c.getPlayer().getItemQuantity(1142099, true) == 0) {
                Item item = ItemInformationProvider.getInstance().getEquipById(1142099);
                item.setOwner(c.getPlayer().getName());
                // MapleInventoryManipulator.addFromDrop(c, item, false);
            }
        }
    }

    private void addCouple(MapleClient c) {
        if (c.getPlayer().getMarriedTo() > 0) {
            try {// move this up to timer?
                if (!c.isFakeLogin) {
                    ChannelServer.getInstance().getWorldInterface().broadcastPacket(Arrays.asList(c.getPlayer().getMarriedTo()), WvsContext.NotifyWeddingPartnerTransfer(c.getPlayer().getMapId(), c.getPlayer().getId()));
                }
            } catch (RemoteException | NullPointerException ex) {
                Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
            }
        }
    }

    private void addElite(MapleClient c) {
        c.updateEliteStatus(true, false);
    }

    private void addEquip(MapleClient c) {
        if (c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).findById(1122017) != null) {
            c.getPlayer().equipPendantOfSpirit();
        }
    }

    private void addFriends(MapleClient c) {
        int buddyIds[] = c.getPlayer().getBuddylist().getBuddyIds();
        CharacterNameAndId pendingBuddyRequest = c.getPlayer().getBuddylist().pollPendingRequest();
        if (!c.isFakeLogin) {
            try {
                ChannelServer.getInstance().getWorldInterface().loggedOn(c.getPlayer().getName(), c.getPlayer().getId(), c.getChannel(), buddyIds);
            } catch (RemoteException | NullPointerException ex) {
                Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
            }
        }
        try {
            for (CharacterIdChannelPair onlineBuddy : ChannelServer.getInstance().getWorldInterface().multiBuddyFind(c.getPlayer().getId(), buddyIds)) {
                BuddylistEntry ble = c.getPlayer().getBuddylist().get(onlineBuddy.getCharacterId());
                ble.setChannel(onlineBuddy.getChannel());
                c.getPlayer().getBuddylist().put(ble);
            }
        } catch (RemoteException | NullPointerException ex) {
            Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
        }
        c.announce(WvsContext.FriendResult.UpdateFriend(c.getPlayer().getBuddylist().getBuddies()));
        if (pendingBuddyRequest != null) {
            c.announce(WvsContext.FriendResult.requestBuddylistAdd(pendingBuddyRequest.getId(), c.getPlayer().getId(), pendingBuddyRequest.getName()));
        }
    }

    private void addBuffs(MapleClient c, int cid) {
        try {
            List<PlayerBuffValueHolder> buffs = ChannelServer.getInstance().getWorldInterface().getBuffsFromStorage(cid);
            if (buffs != null) {
                c.getPlayer().silentGiveBuffs(buffs);
            }
        } catch (RemoteException | NullPointerException ex) {
            Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
        }

    }

    private void addGender(MapleClient c) {
        c.announce(WvsContext.SetGender(c.getPlayer().getGender()));
    }

    private void addGuild(MapleClient c, boolean newcomer) {
        if (c.getPlayer().getGuildId() > 0) {
            try {
                MapleGuild playerGuild = ChannelServer.getInstance().getWorldInterface().getGuild(c.getPlayer().getGuildId(), c.getPlayer().getMGC());
                if (playerGuild == null) {
                    c.getPlayer().deleteGuild(c.getPlayer().getGuildId());
                    c.getPlayer().resetMGC();
                    c.getPlayer().setGuildId(0);
                } else {
                    if (!c.isFakeLogin && newcomer) {
                        ChannelServer.getInstance().getWorldInterface().setGuildMemberOnline(c.getPlayer().getMGC(), true, c.getChannel());
                    }
                    c.announce(WvsContext.GuildResult.showGuildInfo(c.getPlayer()));
                    int allianceId = c.getPlayer().getGuild().getAllianceId();
                    if (allianceId > 0) {
                        MapleAlliance newAlliance = ChannelServer.getInstance().getWorldInterface().getAlliance(allianceId);
                        if (newAlliance == null) {
                            newAlliance = MapleAlliance.loadAlliance(allianceId);
                            if (newAlliance != null) {
                                ChannelServer.getInstance().getWorldInterface().addAlliance(allianceId, newAlliance);
                            } else {
                                c.getPlayer().getGuild().setAllianceId(0);
                            }
                        }
                        if (newAlliance != null) {
                            c.announce(WvsContext.AllianceResult.getAllianceInfo(newAlliance));
                            c.announce(WvsContext.AllianceResult.getGuildAlliances(newAlliance));
                            if (!c.isFakeLogin && newcomer) {
                                ChannelServer.getInstance().getWorldInterface().allianceMessage(allianceId, WvsContext.AllianceResult.allianceMemberOnline(c.getPlayer(), true), c.getPlayer().getId(), -1);
                            }
                        }
                    }
                }
            } catch (RemoteException | NullPointerException ex) {
                Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
            }
        } 
    }

    private void addMessenger(MapleClient c) {
        c.getPlayer().checkMessenger();
        if (FeatureSettings.FAMILY) {
            if (c.getPlayer().getFamilyId() >= 0) {
                try {
                    Family family = ChannelServer.getInstance().getWorldInterface().getFamily(c.getPlayer().getFamilyId());
                    c.announce(FamilyPackets.priviliegeList(c.getPlayer()));// should be grabbing shit from ^
                    c.announce(FamilyPackets.getFamilyInfo(family, family.members.get(c.getPlayer().getId())));
                } catch (RemoteException | NullPointerException ex) {
                    Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
                }
            } else {
                Family fake = new Family();
                fake.bossID = c.getPlayer().getId();
                fake.familyName = "";
                FamilyCharacter fc = new FamilyCharacter();
                fc.characterID = fake.bossID;
                c.announce(FamilyPackets.priviliegeList(c.getPlayer()));
                c.announce(FamilyPackets.getFamilyInfo(fake, fc));
            }
        }
    }

    private void addEvents(MapleClient c) {
        if (Calendar.getInstance().getTimeInMillis() < ServerConstants.expEventEnd) {
            c.getPlayer().dropMessage(MessageType.NOTICE, "You have been blessed with the Maple Tree and have gained a 50% bonus exp boost.");
        }
        if (GameConstants.isWeekend()) {
            c.getPlayer().dropMessage(MessageType.NOTICE, "The Strength of the weekend has given you a 20% bonus exp boost.");
        }
    }

    private void addQuests(MapleClient c) {
        MapleQuest.getInstance(8248).forceComplete(c.getPlayer(), 9209001, false);
        MapleQuest.getInstance(8249).forceComplete(c.getPlayer(), 9209001, false);
        MapleQuest.getInstance(28433).forceComplete(c.getPlayer(), 9010000, false);
        MapleQuest.getInstance(28433).forceComplete(c.getPlayer(), 9010000, false);
        MapleQuest.getInstance(28436).forceComplete(c.getPlayer(), 9010000, false);
    }

    private void addScriptedNPC(MapleClient c) {
        c.announce(NpcPool.setNPCScriptable(9201074, ""));
        c.announce(NpcPool.setNPCScriptable(9000037, ""));
    }

    private void addParty(MapleClient c) {
        if (c.getPlayer().isInParty()) {
            MaplePartyCharacter pchar = c.getPlayer().getMPC();
            pchar.setChannel(c.getChannel());
            pchar.setMapId(c.getPlayer().getMapId());
            pchar.setOnline(true);
            try {
                ChannelServer.getInstance().getWorldInterface().updateParty(c.getPlayer().getPartyId(), PartyOperation.LOG_ONOFF, pchar);
            } catch (RemoteException | NullPointerException ex) {
                Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
            }
        }
        c.getPlayer().updatePartyCharacter();
    }

    private void addPet(MapleClient c, boolean newcomer) {
        if (newcomer) {
            for (MaplePet pet : c.getPlayer().getPets()) {
                if (pet != null) {
                    c.getPlayer().startFullnessSchedule(ItemInformationProvider.getInstance().getItemData(pet.getItemId()).hungry, pet, c.getPlayer().getPetIndex(pet));
                }
            }
        }
    }

    private void addPlayerToField(MapleClient c) {
        MapleCharacter player = c.getPlayer();
        Channel cserv = c.getChannelServer();
        if (player.getCashShop().isOpened()) {
            player.getCashShop().open(0);
        }
        cserv.addPlayer(player);
        c.announce(Stage.SetField(player.getMap(), (byte) 0, null, player, true));
        player.getMap().addPlayer(player);
        if (!player.isHidden()) {
            player.toggleHide(true);
        }
    }

    private void addKeyMap(MapleClient c) {
        c.getPlayer().sendKeymap();
        c.getPlayer().sendMacros();
        if (c.getPlayer().getKeymap().get(93) != null) {
            c.getPlayer().announce(FuncKeyMappedMan.getQuickSlots(c.getPlayer().getKeymap(), false));
        } else {
            c.getPlayer().announce(FuncKeyMappedMan.getQuickSlots(c.getPlayer().getKeymap(), true));
        }
        if (c.getPlayer().getKeymap().get(91) != null) {
            c.getPlayer().announce(FuncKeyMappedMan.PetConsumeItemInit(c.getPlayer().getKeymap().get(91).getAction()));
        }
        if (c.getPlayer().getKeymap().get(92) != null) {
            c.getPlayer().announce(FuncKeyMappedMan.PetConsumeMPItemInit(c.getPlayer().getKeymap().get(92).getAction()));
        }
    }

    private void addDragon(MapleClient c) {
        if (c.getPlayer().getJob().isA(MapleJob.EVAN1)) {
            c.getPlayer().createDragon();
        }
    }

    private void addReport(MapleClient c) {
        c.announce(WvsContext.ClaimSvrStatusChanged());
    }

    private void addSkills(MapleClient c) {
        int linkedLevel = (c.getPlayer().getExplorerLinkedLevel() / 10);
        c.getPlayer().changeSkillLevel(SkillFactory.getSkill(10000000 * c.getPlayer().getJobType() + 12), (byte) (linkedLevel > 20 ? 20 : linkedLevel), 20, -1);
        c.getPlayer().expirationTask();
        c.getPlayer().getStats().recalcLocalStats(c.getPlayer());
    }

    private void addHourChanged(MapleClient c) {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        c.announce(WvsContext.HourChanged((short) calendar.get(Calendar.DAY_OF_WEEK)));
    }
}
