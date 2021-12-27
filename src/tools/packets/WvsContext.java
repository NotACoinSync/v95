package tools.packets;

import client.*;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.ModifyInventory;
import client.player.SecondaryStat;
import constants.GameConstants;
import constants.ItemConstants;
import constants.ServerConstants;
import java.awt.Point;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import net.SendOpcode;
import net.channel.ChannelServer;
import net.server.guild.MapleAlliance;
import net.server.guild.MapleGuild;
import net.server.guild.MapleGuildCharacter;
import net.server.guild.MapleGuildSummary;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import net.server.world.PartyOperation;
import net.world.WorldServer;
import server.ItemInformationProvider;
import server.MaplePlayerShopItem;
import server.events.CakePieEvent;
import server.maps.objects.MapleDoor;
import server.maps.objects.PlayerShop;
import server.propertybuilder.ExpProperty;
import tools.Pair;
import tools.StringUtil;
import tools.data.output.LittleEndianWriter;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import static tools.packets.PacketHelper.encodeAvatarLook;

public class WvsContext {

    public static final List<Pair<MapleStat, Integer>> EMPTY_STATUPDATE = Collections.emptyList();

    public static byte[] InventoryOperation(boolean updateTick, final List<ModifyInventory> mods) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.INVENTORY_OPERATION.getValue());
        oPacket.writeBoolean(updateTick);
        oPacket.write(mods.size());
        // oPacket.write(0); v104 :)
        int addMovement = -1;
        for (ModifyInventory mod : mods) {
            oPacket.write(mod.getMode());
            oPacket.write(mod.getInventoryType());
            oPacket.writeShort(mod.getMode() == 2 ? mod.getOldPosition() : mod.getPosition());// nPOS
            switch (mod.getMode()) {
                case 0: {// add item
                    PacketHelper.encodeItemSlotBase(oPacket, mod.getItem(), true);
                    break;
                }
                case 1: {// update quantity
                    oPacket.writeShort(mod.getQuantity());
                    break;
                }
                case 2: {// move
                    oPacket.writeShort(mod.getPosition());// nPOS2
                    if (mod.getPosition() < 0 || mod.getOldPosition() < 0) {
                        addMovement = mod.getOldPosition() < 0 ? 1 : 2;
                    }
                    break;
                }
                case 3: {// remove
                    if (mod.getPosition() < 0) {
                        addMovement = 2;
                    }
                    break;
                }
                // exp, int
            }
            mod.clear();
        }
        if (addMovement > -1) {
            oPacket.write(addMovement);
        }
        return oPacket.getPacket();
    }

    public static byte[] InventoryOperationFull() {
        return InventoryOperation(true, Collections.<ModifyInventory>emptyList());
    }

    public static byte[] InventoryGrow(int type, int newLimit) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.INVENTORY_GROW.getValue());
        oPacket.write(type);
        oPacket.write(newLimit);
        return oPacket.getPacket();
    }

    public static byte[] enableActions() {
        return StatChanged(EMPTY_STATUPDATE, true, null);
    }

    public static byte[] StatChanged(List<Pair<MapleStat, Integer>> stats, MapleCharacter character) {
        return StatChanged(stats, false, character);
    }

    public static byte[] StatChanged(List<Pair<MapleStat, Integer>> stats, boolean itemReaction, MapleCharacter character) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.STAT_CHANGED.getValue());
        oPacket.write(itemReaction ? 1 : 0);
        // GW_CharacterStat::DecodeChangeStat
        int updateMask = 0;
        for (Pair<MapleStat, Integer> statUpdate : stats) {
            updateMask |= statUpdate.getLeft().getValue();
        }
        List<Pair<MapleStat, Integer>> mystats = stats;
        if (mystats.size() > 1) {
            Collections.sort(mystats, (Pair<MapleStat, Integer> o1, Pair<MapleStat, Integer> o2) -> {
                int val1 = o1.getLeft().getValue();
                int val2 = o2.getLeft().getValue();
                return (val1 < val2 ? -1 : (val1 == val2 ? 0 : 1));
            });
        }
        oPacket.writeInt(updateMask);
        for (Pair<MapleStat, Integer> statupdate : mystats) {
            switch (statupdate.getLeft()) {
                case SKIN:
                case LEVEL:
                    oPacket.write(statupdate.getRight().byteValue());
                    break;
                case JOB:
                case STR:
                case DEX:
                case INT:
                case LUK:
                case AVAILABLEAP:
                case FAME:
                    oPacket.writeShort(statupdate.getRight().shortValue());
                    break;
                case AVAILABLESP:
                    if (GameConstants.hasExtendedSPTable(character.getJob())) {
                        oPacket.write(character.getRemainingSpSize());
                        for (int i = 0; i < character.getRemainingSps().length; i++) {
                            if (character.getRemainingSpBySkill(i) > 0) {
                                oPacket.write(i);
                                oPacket.write(character.getRemainingSpBySkill(i));
                            }
                        }
                    } else {
                        oPacket.writeShort(statupdate.getRight().shortValue());
                    }
                    break;
                case FACE:
                case HAIR:
                case EXP:
                case MESO:
                case GACHAEXP:
                case HP:
                case MAXHP:
                case MP:
                case MAXMP:
                    oPacket.writeInt(statupdate.getRight());
                    break;
                case PETSN:
                    oPacket.writeLong(0);
                    break;
                case PETSN2:
                    oPacket.writeLong(0);
                    break;
                case PETSN3:
                    oPacket.writeLong(0);
                    break;
                default:
                    oPacket.writeInt(statupdate.getRight());
                    break;
            }
        }
        oPacket.write(0); // Boolean, if true decode 1 and CUserLocal::SetSecondaryStatChangedPoint        
        oPacket.write(0); // Boolean, if true decode 8 and CBattleRecordMan::SetBattleRecoveryInfo
        return oPacket.getPacket();
    }

    public static byte[] setTemporaryStat(MapleCharacter character, int buffId, int buffLength, List<Pair<SecondaryStat, BuffDataHolder>> statUps) {// CWvsContext::OnTemporaryStatSet
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.GIVE_BUFF.getValue());
        character.secondaryStat.encodeLocal(oPacket, statUps, buffId, buffLength);
        return oPacket.getPacket();
    }

    public static byte[] setTemporaryStat_FinalAttack(int skillid, int time) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.GIVE_BUFF.getValue());
        oPacket.writeLong(0);
        oPacket.writeShort(0);
        oPacket.write(0);// some 80 and 0 bs DIRECTION
        oPacket.write(0x80);// let's just do 80, then 0
        oPacket.writeInt(0);
        oPacket.writeShort(1);
        oPacket.writeInt(skillid);
        oPacket.writeInt(time);
        oPacket.writeInt(0);
        return oPacket.getPacket();
    }

    public static byte[] resetTemporaryStat(List<SecondaryStat> statups) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CANCEL_BUFF.getValue());
        PacketHelper.encodeFlagFromList(oPacket, statups);
        boolean isMovementAffectingStat = statups.stream().anyMatch(stat -> stat.isMovementAffectingStat());
        if (isMovementAffectingStat) {
            oPacket.write(0); // Stat
        }
        return oPacket.getPacket();
    }

    public static class ForcedStat {

        public static byte[] encode(List<Pair<MapleForcedStat, Integer>> fstats) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.FORCED_STAT_SET.getValue());
            int updateMask = 0;
            for (Pair<MapleForcedStat, Integer> statupdate : fstats) {
                updateMask |= statupdate.getLeft().getValue();
            }
            List<Pair<MapleForcedStat, Integer>> myFstats = fstats;
            if (myFstats.size() > 1) {
                Collections.sort(myFstats, (Pair<MapleForcedStat, Integer> o1, Pair<MapleForcedStat, Integer> o2) -> {
                    int val1 = o1.getLeft().getValue();
                    int val2 = o2.getLeft().getValue();
                    return (val1 < val2 ? -1 : (val1 == val2 ? 0 : 1));
                });
            }
            oPacket.writeInt(updateMask);
            for (Pair<MapleForcedStat, Integer> statupdate : myFstats) {
                switch (statupdate.getLeft()) {
                    case STR:
                    case DEX:
                    case INT:
                    case LUK:
                    case PAD:
                    case PDD:
                    case MAD:
                    case MDD:
                    case ACC:
                    case EVA:
                        oPacket.writeShort(statupdate.getRight().byteValue());
                        break;
                    case SPEED:
                    case JUMP:
                    case SPEEDMAX_CS:
                        oPacket.write(statupdate.getRight().byteValue());
                        break;
                }
            }
            return oPacket.getPacket();
        }

        public static byte[] aranGodlyStats() {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.FORCED_STAT_SET.getValue());
            oPacket.write(new byte[]{(byte) 0x1F, (byte) 0x0F, 0, 0, (byte) 0xE7, 3, (byte) 0xE7, 3, (byte) 0xE7, 3, (byte) 0xE7, 3, (byte) 0xFF, 0, (byte) 0xE7, 3, (byte) 0xE7, 3, (byte) 0x78, (byte) 0x8C});
            return oPacket.getPacket();
        }

        public static byte[] Reset() {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.FORCED_STAT_RESET.getValue());
            return oPacket.getPacket();
        }
    }

    public static byte[] ChangeSkillRecordResult(int skillid, int level, int masterlevel, long expiration) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.UPDATE_SKILLS.getValue());
        oPacket.write(1);
        oPacket.writeShort(1);
        oPacket.writeInt(skillid);
        oPacket.writeInt(level);
        oPacket.writeInt(masterlevel);
        PacketHelper.encodeExpirationTime(oPacket, expiration);
        oPacket.write(4);
        return oPacket.getPacket();
    }

    public static byte[] SkillUseResult() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SKILL_USE_RESULT.getValue());
        oPacket.write(1);
        return oPacket.getPacket();
    }

    public static class GivePopularityResult {

        /**
         Kết quả của Status như sau:
         0: "You have raised/dropped '%s''s level of fame." => giveFameResponse
         1: "The user name is incorrectly entered."
         2: "Users under level 15 are unable to toggle with fame."
         3: "You can't raise or drop a level of fame anymore for today."
         4: "You can't raise or drop a level of fame of that character anymore
         for this month."
         5: "'%s' have raised/dropped '%s''s level of fame." => receiveFame
         6: "The level of fame has neither been raised or dropped due to an
         unexpected error."

         @param status Lựa chọn từng chế độ mà hiển thị GivePopularityResult

         @return
         */
        public static byte[] encode(byte status) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.FAME_RESPONSE.getValue());
            oPacket.write(status);
            return oPacket.getPacket();
        }

        public static byte[] Send(int mode, String charname, int newfame) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.FAME_RESPONSE.getValue());
            oPacket.write(0);
            oPacket.writeMapleAsciiString(charname);
            oPacket.write(mode);
            oPacket.writeShort(newfame);
            oPacket.writeShort(0);
            return oPacket.getPacket();
        }

        public static byte[] Receive(int mode, String charnameFrom) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.FAME_RESPONSE.getValue());
            oPacket.write(5);
            oPacket.writeMapleAsciiString(charnameFrom);
            oPacket.write(mode);
            return oPacket.getPacket();
        }
    }

    public static class OnMessage {

        public static class MessageType {

            public static final int DropPickUpMessage = 0x0,
                    QuestRecordMessage = 0x1,
                    CashItemExpireMessage = 0x2,
                    IncEXPMessage = 0x3,
                    IncSPMessage = 0x4,
                    IncPOPMessage = 0x5,
                    IncMoneyMessage = 0x6,
                    IncGPMessage = 0x7,
                    GiveBuffMessage = 0x8,
                    GeneralItemExpireMessage = 0x9,
                    SystemMessage = 10,
                    QuestRecordExMessage = 11,
                    ItemProtectExpireMessage = 12,
                    ItemExpireReplaceMessage = 13,
                    SkillExpireMessage = 14;
        }

        public static byte[] getShowInventoryFull() {
            return getShowInventoryStatus(0xff);
        }

        public static byte[] showItemUnavailable() {
            return getShowInventoryStatus(0xfe);
        }

        public static byte[] getShowInventoryStatus(int mode) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
            oPacket.write(MessageType.DropPickUpMessage);
            oPacket.write(mode);
            oPacket.writeInt(0);
            oPacket.writeInt(0);
            return oPacket.getPacket();
        }

        public static byte[] forfeitQuest(short quest) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
            oPacket.write(MessageType.QuestRecordMessage);
            oPacket.writeShort(quest);
            oPacket.write(0);
            return oPacket.getPacket();
        }

        public static byte[] completeQuest(short quest, long time) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
            oPacket.write(MessageType.QuestRecordMessage);
            oPacket.writeShort(quest);
            oPacket.write(2);
            oPacket.writeLong(PacketHelper.getTime(time));
            return oPacket.getPacket();
        }

        public static byte[] updateQuest(MapleQuestStatus q, int infoNumber) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
            oPacket.write(MessageType.QuestRecordMessage);
            oPacket.writeShort(infoNumber != 0 ? infoNumber : q.getQuest().getId());
            if (infoNumber != 0) {
                oPacket.write(1);
            } else {
                oPacket.write(q.getStatus().getId());
            }
            oPacket.writeMapleAsciiString(q.getQuestData());
            oPacket.writeLong(0);
            return oPacket.getPacket();
        }

        public static byte[] itemExpired(int itemid) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
            oPacket.write(MessageType.CashItemExpireMessage);
            oPacket.writeInt(itemid);
            return oPacket.getPacket();
        }

        public static byte[] getShowExpGain(ExpProperty property) {// CWvsContext::OnIncEXPMessage
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
            oPacket.write(MessageType.IncEXPMessage);
            oPacket.writeBoolean(property.white);
            oPacket.writeInt(property.gain);
            oPacket.writeBoolean(property.inChat);
            oPacket.writeInt(property.bonusEvent); // monster book bonus (Bonus Event Exp)
            oPacket.write(property.EventPercentage);
            oPacket.write(property.PartyBonusPercentage);
            oPacket.writeInt(property.wedding); // wedding bonus
            if (property.EventPercentage > 0) {
                oPacket.write(property.PlayTimeHour);
            }
            if (property.inChat) { // quest bonus rate stuff
                oPacket.write(property.QuestBonusRate);
                if (property.QuestBonusRate > 0) {
                    oPacket.write(property.QuestBonusRemainCount);
                }
            }
            oPacket.write(property.PartyBonusEventRate); // 0 = party bonus, 100 = 1x Bonus EXP, 200 = 2x Bonus EXP
            oPacket.writeInt(property.party); // party bonus
            oPacket.writeInt(property.equip); // equip bonus
            oPacket.writeInt(property.cafe); // Internet Cafe Bonus
            oPacket.writeInt(property.rainbow); // Rainbow Week Bonus
            oPacket.writeInt(property.PartyExpRingExp);
            oPacket.writeInt(property.CakePieEventBonus);
            return oPacket.getPacket();
        }

        public static byte[] incSPMessage(short nJob, byte sp) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
            oPacket.write(MessageType.IncSPMessage);
            oPacket.writeShort(nJob);
            oPacket.write(sp);
            return oPacket.getPacket();
        }

        /**
         Gets a packet telling the client to show a fame gain.

         @param gain How many fame gained.

         @return The meso gain packet.
         */
        public static byte[] getShowFameGain(int gain) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
            oPacket.write(MessageType.IncPOPMessage);
            oPacket.writeInt(gain);
            return oPacket.getPacket();
        }

        /**
         Gets a packet telling the client to show a meso gain.

         @param gain How many mesos gained.

         @return The meso gain packet.
         */
        public static byte[] getShowMesoGain(int gain) {
            return getShowMesoGain(gain, false);
        }

        /**
         Gets a packet telling the client to show a meso gain.

         @param gain   How many mesos gained.
         @param inChat Show in the chat window?

         @return The meso gain packet.
         */
        public static byte[] getShowMesoGain(int gain, boolean inChat) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
            if (!inChat) {
                oPacket.write(0);
                oPacket.writeShort(1); // v83
            } else {
                oPacket.write(MessageType.IncMoneyMessage);
            }
            oPacket.writeInt(gain);
            oPacket.writeShort(0);
            return oPacket.getPacket();
        }

        public static byte[] getGPMessage(int gpChange) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
            oPacket.write(MessageType.IncGPMessage);
            oPacket.writeInt(gpChange);
            return oPacket.getPacket();
        }

        public static byte[] getItemMessage(int itemid) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
            oPacket.write(MessageType.GiveBuffMessage);// was 7
            oPacket.writeInt(itemid);
            return oPacket.getPacket();
        }

        public static byte[] showInfoText(String text) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
            oPacket.write(MessageType.SystemMessage);// was 9
            oPacket.writeMapleAsciiString(text);
            return oPacket.getPacket();
        }

        public static byte[] getDojoInfoMessage(String message) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
            oPacket.write(MessageType.SystemMessage);
            oPacket.writeMapleAsciiString(message);
            return oPacket.getPacket();
        }

        public static byte[] bunnyPacket() {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
            oPacket.write(MessageType.SystemMessage);
            oPacket.writeAsciiString("Protect the Moon Bunny!!!");
            return oPacket.getPacket();
        }

        public static byte[] getDojoInfo(String info) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
            oPacket.write(MessageType.QuestRecordExMessage);
            oPacket.write(new byte[]{(byte) 0xB7, 4});// QUEST ID f5
            oPacket.writeMapleAsciiString(info);
            return oPacket.getPacket();
        }

        public static byte[] updateDojoStats(MapleCharacter character, int belt) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
            oPacket.write(MessageType.QuestRecordExMessage);
            oPacket.write(new byte[]{(byte) 0xB7, 4}); // ?
            oPacket.writeMapleAsciiString("pt=" + character.getDojoPoints() + ";belt=" + belt + ";tuto=" + (character.getFinishedDojoTutorial() ? "1" : "0"));
            return oPacket.getPacket();
        }

        public static byte[] updateAreaInfo(int area, String info) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
            oPacket.write(MessageType.QuestRecordExMessage); // 0x0B in v95
            oPacket.writeShort(area);// infoNumber
            oPacket.writeMapleAsciiString(info);
            return oPacket.getPacket();
        }
    }

    public static byte[] OpenFullClientDownloadLink() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.OPEN_FULL_CLIENT_DOWNLOAD_LINK);
        return oPacket.getPacket();
    }

    public static class MemoResult {

        public static byte[] Show(ResultSet notes, int count) throws SQLException {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.MEMO_RESULT.getValue());
            oPacket.write(3);
            oPacket.write(count);
            for (int i = 0; i < count; i++) {
                oPacket.writeInt(notes.getInt("id"));
                oPacket.writeMapleAsciiString(notes.getString("from") + " ");
                oPacket.writeMapleAsciiString(notes.getString("message"));
                oPacket.writeLong(PacketHelper.getTime(notes.getLong("timestamp")));
                oPacket.write(notes.getByte("fame"));// FAME :D
                notes.next();
            }
            return oPacket.getPacket();
        }

        public static byte[] Sent() {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.MEMO_RESULT.getValue());
            oPacket.write(4); // The note has successfully been sent
            return oPacket.getPacket();
        }

        public static byte[] Error(int error) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.MEMO_RESULT.getValue());
            oPacket.write(5);
            oPacket.write(error);
            // 0: "The other character is online now.\r\nPlease use the whisper function,"
            // 1: "Please check the name of the receiving character."
            // 2: "The receiver's inbox is full.\r\nPlease try again."
            return oPacket.getPacket();
        }

        public static byte[] Notify() { // CWvsContext::OnMemoNotify_Receive
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.MEMO_RESULT.getValue());
            oPacket.write(7);
            return oPacket.getPacket();
        }
    }

    public static class MapTransferResult {

        public static byte[] encode(byte type, boolean vip) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.MAP_TRANSFER_RESULT.getValue());
            oPacket.write(type);
            // 5,8: "You cannot go to that place."
            // 6,7: "%s is currently difficult to locate, so\r\nthe teleport will not take place."
            // 9: "It's the map you're currently on."
            // 10: "This map is not available to enter for the list."
            // 11: "Users below level 7 are not allowed \r\nto go out from Maple Island."
            oPacket.writeBoolean(vip);
            return oPacket.getPacket();
        }

        public static byte[] UpdateFieldList(MapleCharacter character, boolean delete, boolean vip) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.MAP_TRANSFER_RESULT.getValue());
            oPacket.write(delete ? 2 : 3);
            oPacket.writeBoolean(vip);
            if (vip) {
                List<Integer> map = character.getVipTrockMaps();
                for (int i = 0; i < 10; i++) {
                    oPacket.writeInt(map.get(i));
                }
            } else {
                List<Integer> map = character.getTrockMaps();
                for (int i = 0; i < 5; i++) {
                    oPacket.writeInt(map.get(i));
                }
            }
            return oPacket.getPacket();
        }
    }

    public static class AntiMacroResult {
        // TODO: Hệ thống Anti Marco sẽ code sau!
    }

    public static byte[] ClaimResult(byte mode, boolean success, MapleCharacter character) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CLAIM_RESULT.getValue());
        oPacket.write(mode);
        // 3: "You have been reported by a user."
        // 65: "Please try again later."
        // 66: "Please re-check the character name, then try again."
        // 67: "You do not have enough mesos to report."
        // 68: "Unable to connect to the server."
        // 69: "You have exceeded\r\nthe number of reports available."
        // 71: "You may only report from %d to %d."
        // 72: "Unable to report due to\n\npreviously being cited for a false report."
        if (mode == 2) {
            oPacket.writeBoolean(success);
            // The report has been successfully made.\r\nYou may not report for the rest of the day.
            oPacket.writeInt(character.getPossibleReports());
            // Your report has been successfully registered.\r\nYou have %d reports left this week.
        }
        return oPacket.getPacket();
    }

    public static byte[] SetClaimSvrAvailableTime(byte ClaimSvrOpenTime, byte ClaimSvrCloseTime) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CLAIM_AVAILABLE_TIME.getValue());
        oPacket.write(ClaimSvrOpenTime);
        oPacket.write(ClaimSvrCloseTime);
        return oPacket.getPacket();
    }

    public static byte[] ClaimSvrStatusChanged() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CLAIM_STATUS_CHANGED.getValue());
        oPacket.write(1); // ClaimSvrConnected
        return oPacket.getPacket();
    }

    public static byte[] SetTamingMobInfo(int charid, MapleMount mount, boolean levelup) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SET_TAMING_MOB_INFO.getValue());
        oPacket.writeInt(charid);
        oPacket.writeInt(mount.getLevel()); // TamingMobLevel
        oPacket.writeInt(mount.getExp());// TamingMobExp
        oPacket.writeInt(mount.getTiredness()); // TamingMobFatigue
        oPacket.write(levelup ? (byte) 1 : (byte) 0);
        return oPacket.getPacket();
    }

    public static byte[] QuestClear(int QuestID) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.QUEST_CLEAR.getValue());
        oPacket.writeShort(QuestID);
        return oPacket.getPacket();
    }

    public static class EntrustedShopCheckResult {

        public static byte[] encode(byte type) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.ENTRUSTED_SHOP_CHECK_RESULT.getValue());
            oPacket.write(type);
            // 7: CWvsContext::SendOpenShopRequest
            // 9: "Please use this after retrieving items\r\n from Fredrick of Free Market."
            // 0xA: "Another character is currently using the item.\r\nPlease log on as a different character and close\r\nthe store, or empty out the Store Bank."
            // 0xB: "You are currently unable to open the store."
            // 0xF: "Please retrieve your items from Fredrick."
            return oPacket.getPacket();
        }

        public static byte[] searchShop(int SearchedShop) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.ENTRUSTED_SHOP_CHECK_RESULT.getValue());
            oPacket.write(0xD);
            oPacket.writeInt(SearchedShop);
            return oPacket.getPacket();
        }

        public static byte[] remainingShop(boolean success) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.ENTRUSTED_SHOP_CHECK_RESULT.getValue());
            oPacket.write(0xE);
            oPacket.writeBoolean(success);
            return oPacket.getPacket();
        }

        public static byte[] remoteChannelChange(int channelId) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.ENTRUSTED_SHOP_CHECK_RESULT.getValue());
            oPacket.write(0x10);
            oPacket.writeInt(0);
            oPacket.write(channelId);
            return oPacket.getPacket();
        }

        public static byte[] remoteManageShop(int v23, int v24, long liCashItemSN) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.ENTRUSTED_SHOP_CHECK_RESULT.getValue());
            oPacket.write(0x11);
            oPacket.writeInt(v23);
            oPacket.writeShort(v24);
            oPacket.writeLong(liCashItemSN);
            // You can't sell items while managing the store.\r\nWould you like to start managing the store?
            return oPacket.getPacket();
        }

        public static byte[] renameShop(boolean allow, String sMsg) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.ENTRUSTED_SHOP_CHECK_RESULT.getValue());
            oPacket.write(0x12);
            oPacket.writeBoolean(allow);
            if (allow) {
                oPacket.writeMapleAsciiString(sMsg);
            }
            return oPacket.getPacket();
        }
    }

    public static byte[] SkillLearnItemResult(int dwCharacterID, int skillid, int maxlevel, boolean canuse, boolean success) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SKILL_LEARN_ITEM_RESULT.getValue());
        oPacket.writeBoolean(true);// bOnExclRequest
        oPacket.writeInt(dwCharacterID);
        oPacket.writeBoolean(true);// bIsMaterbook
        oPacket.writeInt(skillid);
        oPacket.writeInt(maxlevel);
        oPacket.writeBoolean(canuse);// bUsed
        oPacket.writeBoolean(success);// bSucceed
        return oPacket.getPacket();
    }

    public static byte[] SkillResetItemResult(int dwCharacterID, boolean success) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SKILL_LEARN_ITEM_RESULT.getValue());
        oPacket.writeBoolean(true);// bOnExclRequest
        oPacket.writeInt(dwCharacterID);
        oPacket.writeBoolean(success);
        // The SP Reset Scroll has reset your SP! - SP Reset Scroll failed.
        return oPacket.getPacket();
    }

    public static byte[] GatherItemResult(int TI) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.GATHER_ITEM_RESULT.getValue());
        oPacket.write(0);
        oPacket.write(TI);
        return oPacket.getPacket();
    }

    public static byte[] SortItemResult(int TI) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SORT_ITEM_RESULT.getValue());
        oPacket.write(0);
        oPacket.write(TI);
        return oPacket.getPacket();
    }

    public static byte[] SueCharacterResult(byte mode) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SUE_CHARACTER_RESULT.getValue());
        oPacket.write(mode);
        // 0: "You have succesfully reported the user."
        // 1: "Unable to locate the user."
        // 2: "You may only report users 10 times a day."
        // 3: "You have been reported to the GM's by a user."
        // 4: "Your request did not go through for unknown reasons. Please try again later."
        return oPacket.getPacket();
    }

    public static byte[] TradeMoneyLimit(boolean limited) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.TRADE_MONEY_LIMIT.getValue());
        oPacket.writeBoolean(limited);
        // Nếu Limited là false thì hiển thị các thông báo dưới:
        // "Players that are Level 15 and below"
        // "may only trade 1 million mesos per day."
        // "You have reached the limit today,"
        // "please try again tomorrow.""
        return oPacket.getPacket();
    }

    public static byte[] SetGender(int nGender) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SET_GENDER.getValue());
        oPacket.write(nGender);
        return oPacket.getPacket();
    }

    public static class GuildBBSPacket {

        public static final int LoadListResult = 6,
                ViewEntryResult = 7,
                EntryNotFound = 8;

        public static byte[] LoadListResult(ResultSet rs, int start) throws SQLException {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.GUILD_BBS_PACKET.getValue());
            oPacket.write(LoadListResult);
            if (!rs.last()) {
                oPacket.write(0);
                oPacket.writeInt(0);
                oPacket.writeInt(0);
                return oPacket.getPacket();
            }
            int threadCount = rs.getRow();
            if (rs.getInt("localthreadid") == 0) { // has a notice
                oPacket.write(1);
                PacketHelper.addThread(oPacket, rs);
                threadCount--; // one thread didn't count (because it's a notice)
            } else {
                oPacket.write(0);
            }
            if (!rs.absolute(start + 1)) { // seek to the thread before where we start
                rs.first(); // uh, we're trying to start at a place past possible
                start = 0;
            }
            oPacket.writeInt(threadCount);
            oPacket.writeInt(Math.min(10, threadCount - start));
            for (int i = 0; i < Math.min(10, threadCount - start); i++) {
                PacketHelper.addThread(oPacket, rs);
                rs.next();
            }
            return oPacket.getPacket();
        }

        public static byte[] ViewEntryResult(int localthreadid, ResultSet threadRS, ResultSet repliesRS) throws SQLException, RuntimeException {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.GUILD_BBS_PACKET.getValue());
            oPacket.write(ViewEntryResult);
            oPacket.writeInt(localthreadid);
            oPacket.writeInt(threadRS.getInt("postercid"));
            oPacket.writeLong(PacketHelper.getTime(threadRS.getLong("timestamp")));
            oPacket.writeMapleAsciiString(threadRS.getString("name"));
            oPacket.writeMapleAsciiString(threadRS.getString("startpost"));
            oPacket.writeInt(threadRS.getInt("icon"));
            if (repliesRS != null) {
                int replyCount = threadRS.getInt("replycount");
                oPacket.writeInt(replyCount);
                int i;
                for (i = 0; i < replyCount && repliesRS.next(); i++) {
                    oPacket.writeInt(repliesRS.getInt("replyid"));
                    oPacket.writeInt(repliesRS.getInt("postercid"));
                    oPacket.writeLong(PacketHelper.getTime(repliesRS.getLong("timestamp")));
                    oPacket.writeMapleAsciiString(repliesRS.getString("content"));
                }
                if (i != replyCount || repliesRS.next()) {
                    throw new RuntimeException(String.valueOf(threadRS.getInt("threadid")));
                }
            } else {
                oPacket.writeInt(0);
            }
            return oPacket.getPacket();
        }

        public static byte[] EntryNotFound() {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.GUILD_BBS_PACKET.getValue());
            oPacket.write(EntryNotFound);
            // "This message had been deleted by another user.\r\nPlease check again."
            return oPacket.getPacket();
        }
    }

    public static byte[] CharacterInfo(MapleCharacter character) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CHAR_INFO.getValue());
        oPacket.writeInt(character.getId()); // dwCharacterId
        oPacket.write(character.getLevel()); // nLevel
        oPacket.writeShort(character.getJob().getId()); // nJob
        oPacket.writeShort(character.getFame()); // nPOP
        oPacket.writeBoolean(character.getMarriedTo() > 0 && character.getMarriageRingID() > 0);
        String guildName = "";
        String allianceName = "";
        MapleGuildSummary gs = null;
        try {
            gs = ChannelServer.getInstance().getWorldInterface().getGuildSummary(character.getGuildId());
        } catch (RemoteException | NullPointerException ex) {
            Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
        }
        if (character.getGuildId() > 0 && gs != null) {
            guildName = gs.getName();
            MapleAlliance alliance = null;
            try {
                alliance = ChannelServer.getInstance().getWorldInterface().getAlliance(gs.getAllianceId());
            } catch (RemoteException | NullPointerException ex) {
                Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
            }
            if (alliance != null) {
                allianceName = alliance.getName();
            }
        }
        oPacket.writeMapleAsciiString(guildName); // sCommunity
        oPacket.writeMapleAsciiString(allianceName); // sAlliance
        oPacket.write(0);// pMedalInfo
        MaplePet[] pets = character.getPets();
        Item inv = character.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -114);
        oPacket.writeBoolean(character.isMultiPetActivated()); // bPetActivated
        for (int i = 0; i < 3; i++) { // CUIUserInfo::SetMultiPetInfo
            if (pets[i] != null) {
                oPacket.write(pets[i].getUniqueId());
                oPacket.writeInt(pets[i].getItemId()); // dwTemplateID
                oPacket.writeMapleAsciiString(pets[i].getName());// sName
                oPacket.write(pets[i].getLevel()); // nLevel
                oPacket.writeShort(pets[i].getCloseness()); // nTameness
                oPacket.write(pets[i].getFullness()); // nRepleteness
                oPacket.writeShort(0);// usPetSkill
                oPacket.writeInt(inv != null ? inv.getItemId() : 0);// nItemID
                // oPacket.write(i == 2 ? 0 : 1);//
            }
        }
        oPacket.write(0); // end of pets
        if (character.getMount() != null && character.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -18) != null) {
            oPacket.writeBoolean(true);
            oPacket.writeInt(character.getMount().getLevel()); // nLevel
            oPacket.writeInt(character.getMount().getExp()); // nExp
            oPacket.writeInt(character.getMount().getTiredness()); // nFatigue
        } else {
            oPacket.writeBoolean(false);
        }
        oPacket.write(character.getCashShop().getWishList().size());
        for (int sn : character.getCashShop().getWishList()) {
            oPacket.writeInt(sn);
        }
        oPacket.writeInt(character.getMonsterBook().getBookLevel());
        oPacket.writeInt(character.getMonsterBook().getNormalCard());
        oPacket.writeInt(character.getMonsterBook().getSpecialCard());
        oPacket.writeInt(character.getMonsterBook().getTotalCards());
        oPacket.writeInt(character.getMonsterBookCover() > 0 ? ItemInformationProvider.getInstance().getCardMobId(character.getMonsterBookCover()) : 0);
        Item medal = character.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -49);
        if (medal != null) {
            oPacket.writeInt(medal.getItemId());
        } else {
            oPacket.writeInt(0);
        }
        ArrayList<Short> medalQuests = new ArrayList<>();
        List<MapleQuestStatus> completed = character.getCompletedQuests();
        for (MapleQuestStatus q : completed) {
            if (q.getQuest().getId() >= 29000) { // && q.getQuest().getId() <= 29923
                medalQuests.add(q.getQuest().getId());
            }
        }
        Collections.sort(medalQuests);
        oPacket.writeShort(medalQuests.size());
        for (Short s : medalQuests) {
            oPacket.writeShort(s);
        }
        List<Integer> chairs = new ArrayList<>();
        for (Item item : character.getInventory(MapleInventoryType.SETUP).list()) {
            if (ItemConstants.is_chair(item.getItemId())) {
                chairs.add(item.getItemId());
            }
        }
        oPacket.writeInt(chairs.size());
        for (int itemid : chairs) {
            oPacket.writeInt(itemid);
        }
        return oPacket.getPacket();
    }

    public static class ExpedtionResult {

        public static final int Get = 0x39,
                Get_Created = 0x3B,
                Get_Joined = 0x3D,
                Removed = 0x3A,
                Removed_Left = 0x41,
                Removed_Kicked = 0x43,
                Removed_Disbanded = 0x44,
                Notice_Joined = 0x3C,
                Notice_Left = 0x40,
                Notice_Kicked = 0x42,
                Joined = 0x3E,
                MasterChanged = 0x45,
                Modified = 0x46,
                Invite = 0x48,
                ResponseInvite = 0x49;

        public static byte[] Get(int nRetCode) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.EXPEDITION_RESULT.getValue());
            oPacket.write(nRetCode);
            // EXPEDITION::encode
            oPacket.writeZeroBytes(900); // TODO: Cái này khó vãi lol
            return oPacket.getPacket();
        }

        public static byte[] Removed(int nRetCode) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.EXPEDITION_RESULT.getValue());
            oPacket.write(nRetCode);
            return oPacket.getPacket();
        }

        public static byte[] Notice(int nRetCode, String sCharName) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.EXPEDITION_RESULT.getValue());
            oPacket.write(nRetCode);
            oPacket.writeMapleAsciiString(sCharName);
            return oPacket.getPacket();
        }

        public static byte[] Joined() {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.EXPEDITION_RESULT.getValue());
            oPacket.write(Joined);
            return oPacket.getPacket();
        }

        public static byte[] MasterChanged(int nMasterPartyIndex) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.EXPEDITION_RESULT.getValue());
            oPacket.write(MasterChanged);
            oPacket.writeInt(nMasterPartyIndex);
            return oPacket.getPacket();
        }

        public static byte[] Modified(int nMasterPartyIndex) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.EXPEDITION_RESULT.getValue());
            oPacket.write(Modified);
            oPacket.writeInt(nMasterPartyIndex);
            return oPacket.getPacket();
        }

        public static byte[] Invite(int nLevel) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.EXPEDITION_RESULT.getValue());
            oPacket.write(Invite);
            oPacket.writeInt(nLevel);
            return oPacket.getPacket();
        }

        public static byte[] ResponseInvite(int nResponse) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.EXPEDITION_RESULT.getValue());
            oPacket.write(ResponseInvite);
            oPacket.writeInt(nResponse);
            return oPacket.getPacket();
        }
    }

    public static class PartyResult {

        public static class Request {

            public static final int LoadParty = 0, CreateNewParty = 1, WithdrawParty = 2, JoinParty = 3, InviteParty = 4, KickParty = 5, ChangePartyBoss = 6;
        }

        public static class Result {

            public static final int LoadParty_Done = 7,
                    CreateNewParty_Done = 8,
                    CreateNewParty_AlreayJoined = 9,
                    CreateNewParty_Beginner = 10,
                    CreateNewParty_Unknown = 11,
                    WithdrawParty_Done = 12,
                    WithdrawParty_NotJoined = 13,
                    WithdrawParty_Unknown = 14,
                    JoinParty_Done = 15,
                    JoinParty_Done2 = 16,
                    JoinParty_AlreadyJoined = 17,
                    JoinParty_AlreadyFull = 18,
                    JoinParty_OverDesiredSize = 19,
                    JoinParty_UnknownUser = 20,
                    JoinParty_Unknown = 21,
                    InviteParty_Sent = 22,
                    InviteParty_BlockedUser = 23,
                    InviteParty_AlreadyInvited = 24,
                    InviteParty_AlreadyInvitedByInviter = 25,
                    InviteParty_Rejected = 26,
                    InviteParty_Accepted = 27,
                    KickParty_Done = 28,
                    KickParty_FieldLimit = 29,
                    KickParty_Unknown = 30,
                    ChangePartyBoss_Done = 31,
                    ChangePartyBoss_NotSameField = 32,
                    ChangePartyBoss_NoMemberInSameField = 33,
                    ChangePartyBoss_NotSameChannel = 34,
                    ChangePartyBoss_Unknown = 35,
                    AdminCannotCreate = 36,
                    AdminCannotInvite = 37, UserMigration = 38, ChangeLevelOrJob = 39, CanNotInThisField = 40, // Correct
                    ServerMsg = 41, PartyInfo_TownPortalChanged = 42, PartyInfo_OpenGate = 43;
            /*
             SuccessToSelectPQReward = 40,
             FailToSelectPQReward = 41,
             ReceivePQReward = 42,
             FailToRequestPQReward = 43,
             CanNotInThisField = 44,
             ServerMsg = 45,
             PartyInfo_TownPortalChanged = 46,
             PartyInfo_OpenGate = 47;
             */
        }

        public static byte[] partyCreated(MapleParty party, MaplePartyCharacter partychar) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.PARTY_OPERATION.getValue());
            oPacket.write(PartyResult.Result.CreateNewParty_Done);
            oPacket.writeInt(party.getId());
            if (!partychar.getDoors().isEmpty()) {
                MapleDoor door = partychar.getDoors().get(0);
                if (door.getTown().getId() == partychar.getMapId()) {
                    oPacket.writeInt(partychar.getDoors().get(0).getTarget().getId());
                    oPacket.writeInt(partychar.getDoors().get(0).getTown().getId());
                    oPacket.writeInt(partychar.getDoors().get(0).getSkillID());
                    oPacket.writeShort(partychar.getDoors().get(0).getTownPortal().getPosition().x);
                    oPacket.writeShort(partychar.getDoors().get(0).getTownPortal().getPosition().y);
                } else {
                    oPacket.writeInt(partychar.getDoors().get(0).getTown().getId());
                    oPacket.writeInt(partychar.getDoors().get(0).getTarget().getId());
                    oPacket.writeInt(partychar.getDoors().get(0).getSkillID());
                    oPacket.writeShort(partychar.getDoors().get(0).getPosition().x);
                    oPacket.writeShort(partychar.getDoors().get(0).getPosition().y);
                }
            } else {
                oPacket.writeInt(999999999); // dwTownID
                oPacket.writeInt(999999999); // dwFieldID
                oPacket.writeInt(0);
                oPacket.writeShort(0);
                oPacket.writeShort(0);
            }
            return oPacket.getPacket();
        }

        public static byte[] partyInvite(MapleCharacter from) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.PARTY_OPERATION.getValue());
            oPacket.write(PartyResult.Request.InviteParty);
            oPacket.writeInt(from.getParty().getId());
            oPacket.writeMapleAsciiString(from.getName());
            oPacket.writeInt(from.getLevel());
            oPacket.writeInt(from.getJob().getId());
            oPacket.write(0);
            return oPacket.getPacket();
        }

        /**
         10: A beginner can't create a party. 1/11/14/19: Your request for a
         party didn't work due to an unexpected error. 13: You have yet to
         join a party. 16: Already have joined a party. 17: The party you're
         trying to join is already in full capacity. 19: Unable to find the
         requested character in this channel.

         @param message

         @return
         */
        public static byte[] partyStatusMessage(int message) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.PARTY_OPERATION.getValue());
            oPacket.write(message);
            return oPacket.getPacket();
        }

        /**
         23: 'Char' have denied request to the party.

         @param message
         @param charname

         @return
         */
        public static byte[] partyStatusMessage(int message, String charname) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.PARTY_OPERATION.getValue());
            oPacket.write(message);
            oPacket.writeMapleAsciiString(charname);
            return oPacket.getPacket();
        }

        // PARTYDATA::encode
        private static void PartyDataEncode(int forchannel, MapleParty party, LittleEndianWriter lew, boolean leaving) {
            List<MaplePartyCharacter> partymembers = new ArrayList<>(party.getMembers());
            while (partymembers.size() < 6) {
                partymembers.add(new MaplePartyCharacter());
            }
            for (MaplePartyCharacter partychar : partymembers) {
                lew.writeInt(partychar.getId());
            }
            for (MaplePartyCharacter partychar : partymembers) {
                lew.writeAsciiString(PacketHelper.getRightPaddedStr(partychar.getName(), '\0', 13));
            }
            for (MaplePartyCharacter partychar : partymembers) {
                lew.writeInt(partychar.getJobId());
            }
            for (MaplePartyCharacter partychar : partymembers) {
                lew.writeInt(partychar.getLevel());
            }
            for (MaplePartyCharacter partychar : partymembers) {
                if (partychar.isOnline()) {
                    lew.writeInt(partychar.getChannel());
                } else {
                    lew.writeInt(-2);
                }
            }
            lew.writeInt(party.getLeader().getId());
            for (MaplePartyCharacter partychar : partymembers) {
                if (partychar.getChannel() == forchannel) {
                    lew.writeInt(partychar.getMapId());
                } else {
                    lew.writeInt(999999999);
                }
            }
            for (MaplePartyCharacter partyChar : partymembers) {
                lew.writeInt(partyChar.getPQReward());
                lew.writeInt(partyChar.getPQRewardType());
            }
            lew.writeInt(party.getLeader().getPQRewardMobTemplateID());                        
            for (MaplePartyCharacter partychar : partymembers) {
                if (partychar.getChannel() == forchannel && !leaving) {
                    if (!partychar.getDoors().isEmpty()) {
                        lew.writeInt(partychar.getDoors().get(0).getTown().getId());
                        lew.writeInt(partychar.getDoors().get(0).getTarget().getId());
                        lew.writeInt(partychar.getDoors().get(0).getSkillID());
                        lew.writeInt(partychar.getDoors().get(0).getPosition().x);
                        lew.writeInt(partychar.getDoors().get(0).getPosition().y);
                    } else {
                        lew.writeInt(999999999);
                        lew.writeInt(999999999);
                        lew.writeInt(0);// nSkillID
                        lew.writeInt(0);
                        lew.writeInt(0);
                    }
                } else {
                    lew.writeInt(999999999);
                    lew.writeInt(999999999);
                    lew.writeInt(0);// nSkillID
                    lew.writeInt(0);
                    lew.writeInt(0);
                }
            }
            lew.writeInt(party.getLeader().getbPQReward());
        }

        public static byte[] updateParty(int forChannel, MapleParty party, PartyOperation op, MaplePartyCharacter target) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.PARTY_OPERATION.getValue());
            switch (op) {
                case DISBAND:
                case EXPEL:
                case LEAVE:
                    oPacket.write(PartyResult.Result.WithdrawParty_Done);
                    oPacket.writeInt(party.getId());
                    oPacket.writeInt(target.getId());
                    if (op == PartyOperation.DISBAND) {
                        oPacket.write(0);
                        oPacket.writeInt(party.getId());
                    } else {
                        oPacket.write(1);
                        if (op == PartyOperation.EXPEL) {
                            oPacket.write(1);
                        } else {
                            oPacket.write(0);
                        }
                        oPacket.writeMapleAsciiString(target.getName());
                        PartyDataEncode(forChannel, party, oPacket, false);
                    }
                    break;
                case JOIN:
                    oPacket.write(PartyResult.Result.JoinParty_Done);
                    oPacket.writeInt(party.getId());
                    oPacket.writeMapleAsciiString(target.getName());
                    PartyDataEncode(forChannel, party, oPacket, false);
                    break;
                case SILENT_UPDATE:
                case LOG_ONOFF:
                    oPacket.write(PartyResult.Result.LoadParty_Done);
                    oPacket.writeInt(party.getId());
                    PartyDataEncode(forChannel, party, oPacket, false);
                    break;
                case CHANGE_LEADER:
                    oPacket.write(PartyResult.Result.ChangePartyBoss_Done);
                    oPacket.writeInt(target.getId());
                    oPacket.write(0);
                    break;
                case UPDATE_DOOR:
                    oPacket.write(PartyResult.Result.PartyInfo_TownPortalChanged);
                    oPacket.write(party.getIndex(target));
                    if (target.getDoors().isEmpty()) {
                        oPacket.writeInt(999999999);
                        oPacket.writeInt(999999999);
                        oPacket.writeInt(0);// skillid
                        oPacket.writeShort(0);
                        oPacket.writeShort(0);
                    } else {
                        oPacket.writeInt(target.getDoors().get(0).getTown().getId());
                        oPacket.writeInt(target.getDoors().get(0).getTarget().getId());
                        oPacket.writeInt(target.getDoors().get(0).getSkillID());
                        oPacket.writePos(target.getDoors().get(0).getPosition());
                    }
                    break;
            }
            return oPacket.getPacket();
        }

        // Doesn't exist in v90/v83, it was sending the level/job update back in v83.
        @Deprecated
        public static byte[] partyPortal(int townId, int targetId, int nSkillID, Point position) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.PARTY_OPERATION.getValue());
            oPacket.write(PartyResult.Result.PartyInfo_OpenGate);
            oPacket.writeInt(townId);
            oPacket.writeInt(targetId);
            oPacket.writeInt(nSkillID);
            oPacket.writePos(position);
            return oPacket.getPacket();
        }
    }

    public static class FriendResult {

        public static class Info {

            public static final int First = 0x0,
                    LoadRequest = 0x1,
                    LoadResult = 0x2,
                    SaveRequest = 0x3,
                    SaveResult = 0x4;
        }

        public static class Find {

            public static final int MyInfoRequest = 0x5,
                    MyInfoResult = 0x6,
                    SearchRequest = 0x7,
                    SearchResult = 0x8,
                    SearchResult_Error = 0x9,
                    DetailRequest = 0xA,
                    DetailResult = 0xB,
                    ErrorCode_OverflowQueue = 0xC;
        }

        public static class Request {

            public static final int LoadFriend = 0x0,
                    SetFriend = 0x1,
                    AcceptFriend = 0x2,
                    DeleteFriend = 0x3,
                    NotifyLogin = 0x4,
                    NotifyLogout = 0x5,
                    IncMaxCount = 0x6;
        }

        public static class Result {

            public static final int LoadFriend_Done = 0x7,
                    NotifyChange_FriendInfo = 0x8,
                    Invite = 0x9,
                    SetFriend_Done = 0xA,
                    SetFriend_FullMe = 0xB,
                    SetFriend_FullOther = 0xC,
                    SetFriend_AlreadySet = 0xD,
                    SetFriend_Master = 0xE,
                    SetFriend_UnknownUser = 0xF,
                    SetFriend_Unknown = 0x10,
                    AcceptFriend_Unknown = 0x11,
                    DeleteFriend_Done = 0x12,
                    DeleteFriend_Unknown = 0x13,
                    Notify = 0x14,
                    IncMaxCount_Done = 0x15,
                    IncMaxCount_Unknown = 0x16,
                    PleaseWait = 0x17;
        }

        public static byte[] UpdateFriend(Collection<BuddylistEntry> buddylist) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.BUDDYLIST.getValue());
            oPacket.write(Result.LoadFriend_Done);// CWvsContext::CFriend::Reset
            oPacket.write((int) buddylist.stream().filter(BuddylistEntry::isVisible).count());
            for (BuddylistEntry buddy : buddylist) {
                if (buddy.isVisible()) {
                    buddy.encode(oPacket);
                }
            }
            for (BuddylistEntry buddy : buddylist) {
                if (buddy.isVisible()) {
                    oPacket.writeInt(buddy.inShop ? 1 : 0);
                }
            }
            return oPacket.getPacket();
        }

        public static byte[] buddylistMessage(byte message) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.BUDDYLIST.getValue());
            oPacket.write(message);
            return oPacket.getPacket();
        }

        public static byte[] requestBuddylistAdd(int cidFrom, int cid, String nameFrom) {// Make this use a BuddylistEntry?
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.BUDDYLIST.getValue());
            oPacket.write(Result.Invite);
            oPacket.writeInt(cidFrom);// dwFriendID
            oPacket.writeMapleAsciiString(nameFrom);
            oPacket.writeInt(0);// nLevel
            oPacket.writeInt(0);// nJobCode
            // GW_Friend::encode
            // struct GW_Friend
            oPacket.writeInt(cid);// dwFriendID
            oPacket.writeNullTerminatedAsciiString(nameFrom, 13);
            oPacket.write(0);// nFlag
            oPacket.writeInt(0);// nChannelID
            oPacket.writeNullTerminatedAsciiString("Default Group", 17);
            // CWvsContext::CFriend::Insert
            oPacket.write(0);
            return oPacket.getPacket();
        }

        public static byte[] updateBuddyChannel(BuddylistEntry entry) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.BUDDYLIST.getValue());
            oPacket.write(Result.Notify);
            oPacket.writeInt(entry.getCharacterId());
            oPacket.writeBoolean(entry.inShop);
            oPacket.writeInt(entry.getChannel());
            return oPacket.getPacket();
        }

        public static byte[] updateBuddyCapacity(int capacity) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.BUDDYLIST.getValue());
            oPacket.write(Result.IncMaxCount_Done);
            oPacket.write(capacity);
            return oPacket.getPacket();
        }

        public static byte[] MyInfo(int dwPlayStyle, int dwActivityAregs) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.FIND_FRIEND.getValue());
            oPacket.write(Find.MyInfoResult);
            oPacket.writeInt(dwPlayStyle);
            oPacket.writeInt(dwActivityAregs);
            return oPacket.getPacket();
        }

        public static byte[] Search(List<Integer> SearchInfo) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.FIND_FRIEND.getValue());
            oPacket.write(Find.SearchResult);
            oPacket.writeShort(SearchInfo.size());
            if (SearchInfo.size() > 0) {
                for (int i : SearchInfo) {
                    oPacket.writeInt(i);
                    oPacket.writeMapleAsciiString("");
                    oPacket.write(i);
                    oPacket.writeShort(i);
                    oPacket.writeInt(i);
                    oPacket.writeInt(i);
                }
            }
            return oPacket.getPacket();
        }

        public static byte[] RequestError() {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.FIND_FRIEND.getValue());
            oPacket.write(Find.SearchResult_Error);
            oPacket.write(12);
            return oPacket.getPacket();
        }

        public static byte[] DetailInfo(MapleCharacter character) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.FIND_FRIEND.getValue());
            oPacket.write(Find.DetailResult);
            oPacket.writeInt(character.getId());
            PacketHelper.encodeAvatarLook(oPacket, character, false);
            return oPacket.getPacket();
        }
    }

    public static class GuildResult {

        public static class Request {

            public static final int LoadGuild = 0x0,
                    InputGuildName = 0x1,
                    CheckGuildName = 0x2,
                    CreateGuildAgree = 0x3,
                    CreateNewGuild = 0x4,
                    InviteGuild = 0x5,
                    JoinGuild = 0x6,
                    WithdrawGuild = 0x7,
                    KickGuild = 0x8,
                    RemoveGuild = 0x9,
                    IncMaxMemberNum = 0xA,
                    ChangeLevel = 0xB,
                    ChangeJob = 0xC,
                    SetGradeName = 0xD,
                    SetMemberGrade = 0xE,
                    SetMark = 0xF,
                    SetNotice = 0x10,
                    InputMark = 0x11,
                    CheckQuestWaiting = 0x12,
                    CheckQuestWaiting2 = 0x13,
                    InsertQuestWaiting = 0x14,
                    CancelQuestWaiting = 0x15,
                    RemoveQuestCompleteGuild = 0x16,
                    IncPoint = 0x17,
                    IncCommitment = 0x18,
                    SetQuestTime = 0x19,
                    ShowGuildRanking = 0x1A,
                    SetSkill = 0x1B;
        }

        public static class Send {

            public static final int LoadGuild_Done = 0x1C,
                    CheckGuildName_Available = 0x1D,
                    CheckGuildName_AlreadyUsed = 0x1E,
                    CheckGuildName_Unknown = 0x1F,
                    CreateGuildAgree_Reply = 0x20,
                    CreateGuildAgree_Unknown = 0x21,
                    CreateNewGuild_Done = 0x22,
                    CreateNewGuild_AlreayJoined = 0x23,
                    CreateNewGuild_GuildNameAlreayExist = 0x24,
                    CreateNewGuild_Beginner = 0x25,
                    CreateNewGuild_Disagree = 0x26,
                    CreateNewGuild_NotFullParty = 0x27,
                    CreateNewGuild_Unknown = 0x28,
                    JoinGuild_Done = 0x29,
                    JoinGuild_AlreadyJoined = 0x2A,
                    JoinGuild_AlreadyFull = 0x2B,
                    JoinGuild_UnknownUser = 0x2C,
                    JoinGuild_Unknown = 0x2D,
                    WithdrawGuild_Done = 0x2E,
                    WithdrawGuild_NotJoined = 0x2F,
                    WithdrawGuild_Unknown = 0x30,
                    KickGuild_Done = 0x31,
                    KickGuild_NotJoined = 0x32,
                    KickGuild_Unknown = 0x33,
                    RemoveGuild_Done = 0x34,
                    RemoveGuild_NotExist = 0x35,
                    RemoveGuild_Unknown = 0x36,
                    InviteGuild_BlockedUser = 0x37,
                    InviteGuild_AlreadyInvited = 0x38,
                    InviteGuild_Rejected = 0x39,
                    AdminCannotCreate = 0x3A,
                    AdminCannotInvite = 0x3B,
                    IncMaxMemberNum_Done = 0x3C,
                    IncMaxMemberNum_Unknown = 0x3D,
                    ChangeLevelOrJob = 0x3E,
                    NotifyLoginOrLogout = 0x3F,
                    SetGradeName_Done = 0x40,
                    SetGradeName_Unknown = 0x41,
                    SetMemberGrade_Done = 0x42,
                    SetMemberGrade_Unknown = 0x43,
                    SetMemberCommitment_Done = 0x44,
                    SetMark_Done = 0x45,
                    SetMark_Unknown = 0x46,
                    SetNotice_Done = 0x47,
                    InsertQuest = 0x48,
                    NoticeQuestWaitingOrder = 0x49,
                    SetGuildCanEnterQuest = 0x4A,
                    IncPoint_Done = 0x4B,
                    ShowGuildRanking = 0x4C,
                    GuildQuest_NotEnoughUser = 0x4D,
                    GuildQuest_RegisterDisconnected = 0x4E,
                    GuildQuest_NoticeOrder = 0x4F,
                    Authkey_Update = 0x50,
                    SetSkill_Done = 0x51,
                    ServerMsg = 0x52;
        }

        public static byte[] showGuildInfo(MapleCharacter c) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.GUILD_OPERATION.getValue());
            oPacket.write(0x1A); // signature for showing guild info
            if (c == null) { // show empty guild (used for leaving, expelled)
                oPacket.write(0);
                return oPacket.getPacket();
            }
            MapleGuild g = null;
            try {
                g = ChannelServer.getInstance().getWorldInterface().getGuild(c.getMGC());
            } catch (RemoteException | NullPointerException ex) {
                Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
            }
            if (g == null) { // failed to read from DB - don't show a guild
                oPacket.write(0);
                return oPacket.getPacket();
            } else {
                c.setGuildRank(c.getGuildRank());
            }
            oPacket.write(1); // bInGuild
            oPacket.writeInt(g.getId());
            oPacket.writeMapleAsciiString(g.getName());
            for (int i = 1; i <= 5; i++) {
                oPacket.writeMapleAsciiString(g.getRankTitle(i));
            }
            Collection<MapleGuildCharacter> members = g.getMembers();
            oPacket.write(members.size()); // then it is the size of all the members
            for (MapleGuildCharacter mgc : members) {// and each of their character ids o_O
                oPacket.writeInt(mgc.getId());
            }
            for (MapleGuildCharacter mgc : members) {
                oPacket.writeAsciiString(PacketHelper.getRightPaddedStr(mgc.getName(), '\0', 13));
                oPacket.writeInt(mgc.getJobId());
                oPacket.writeInt(mgc.getLevel());
                oPacket.writeInt(mgc.getGuildRank());
                oPacket.writeInt(mgc.isOnline() ? 1 : 0);
                oPacket.writeInt(mgc.getGP());// people use to think this was guild 'signature'.. how retarded were they
                oPacket.writeInt(mgc.getAllianceRank());
            }
            oPacket.writeInt(g.getCapacity());
            oPacket.writeShort(g.getLogoBG());
            oPacket.write(g.getLogoBGColor());
            oPacket.writeShort(g.getLogo());
            oPacket.write(g.getLogoColor());
            oPacket.writeMapleAsciiString(g.getNotice());
            oPacket.writeInt(g.getGP());
            oPacket.writeInt(g.getAllianceId());
            return oPacket.getPacket();
        }

        public static byte[] guildMemberOnline(int gid, int cid, boolean bOnline) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.GUILD_OPERATION.getValue());
            oPacket.write(0x3d);
            oPacket.writeInt(gid);
            oPacket.writeInt(cid);
            oPacket.write(bOnline ? 1 : 0);
            return oPacket.getPacket();
        }

        public static byte[] guildInvite(int gid, String charName, int level, int jobCode) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.GUILD_OPERATION.getValue());
            oPacket.write(0x05);
            oPacket.writeInt(gid);
            oPacket.writeMapleAsciiString(charName);
            oPacket.writeInt(level);// nLevel
            oPacket.writeInt(jobCode);// nJobCode
            return oPacket.getPacket();
        }

        public static byte[] denyGuildInvitation(String charname) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.GUILD_OPERATION.getValue());
            oPacket.write(0x37);
            oPacket.writeMapleAsciiString(charname);
            return oPacket.getPacket();
        }

        public static byte[] genericGuildMessage(byte code) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.GUILD_OPERATION.getValue());
            oPacket.write(code);
            return oPacket.getPacket();
        }

        public static byte[] newGuildMember(MapleGuildCharacter mgc) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.GUILD_OPERATION.getValue());
            oPacket.write(0x27);
            oPacket.writeInt(mgc.getGuildId());
            oPacket.writeInt(mgc.getId());
            oPacket.writeAsciiString(PacketHelper.getRightPaddedStr(mgc.getName(), '\0', 13));
            oPacket.writeInt(mgc.getJobId());
            oPacket.writeInt(mgc.getLevel());
            oPacket.writeInt(mgc.getGuildRank()); // should be always 5 but whatevs
            oPacket.writeInt(mgc.isOnline() ? 1 : 0); // should always be 1 too
            oPacket.writeInt(1); // ? could be guild signature, but doesn't seem to matter
            oPacket.writeInt(3);
            return oPacket.getPacket();
        }

        // someone leaving, mode == 0x2c for leaving, 0x2f for expelled
        public static byte[] memberLeft(MapleGuildCharacter mgc, boolean bExpelled) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.GUILD_OPERATION.getValue());
            oPacket.write(bExpelled ? 0x2F : 0x2C);
            oPacket.writeInt(mgc.getGuildId());
            oPacket.writeInt(mgc.getId());
            oPacket.writeMapleAsciiString(mgc.getName());
            return oPacket.getPacket();
        }

        public static byte[] changeRank(MapleGuildCharacter mgc) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.GUILD_OPERATION.getValue());
            oPacket.write(0x40);
            oPacket.writeInt(mgc.getGuildId());
            oPacket.writeInt(mgc.getId());
            oPacket.write(mgc.getGuildRank());
            return oPacket.getPacket();
        }

        public static byte[] guildNotice(int gid, String notice) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.GUILD_OPERATION.getValue());
            oPacket.write(0x44);
            oPacket.writeInt(gid);
            oPacket.writeMapleAsciiString(notice);
            return oPacket.getPacket();
        }

        public static byte[] guildMemberLevelJobUpdate(MapleGuildCharacter mgc) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.GUILD_OPERATION.getValue());
            oPacket.write(0x3C);
            oPacket.writeInt(mgc.getGuildId());
            oPacket.writeInt(mgc.getId());
            oPacket.writeInt(mgc.getLevel());
            oPacket.writeInt(mgc.getJobId());
            return oPacket.getPacket();
        }

        public static byte[] rankTitleChange(int gid, String[] ranks) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.GUILD_OPERATION.getValue());
            oPacket.write(0x3E);
            oPacket.writeInt(gid);
            for (int i = 0; i < 5; i++) {
                oPacket.writeMapleAsciiString(ranks[i]);
            }
            return oPacket.getPacket();
        }

        public static byte[] guildDisband(int gid) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.GUILD_OPERATION.getValue());
            oPacket.write(0x32);
            oPacket.writeInt(gid);
            oPacket.write(1);
            return oPacket.getPacket();
        }

        public static byte[] guildQuestWaitingNotice(byte channel, int waitingPos) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.GUILD_OPERATION.getValue());
            oPacket.write(0x4C);
            oPacket.write(channel);
            oPacket.write(waitingPos);
            return oPacket.getPacket();
        }

        public static byte[] guildEmblemChange(int gid, short bg, byte bgcolor, short logo, byte logocolor) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.GUILD_OPERATION.getValue());
            oPacket.write(0x42);
            oPacket.writeInt(gid);
            oPacket.writeShort(bg);
            oPacket.write(bgcolor);
            oPacket.writeShort(logo);
            oPacket.write(logocolor);
            return oPacket.getPacket();
        }

        public static byte[] guildCapacityChange(int gid, int capacity) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.GUILD_OPERATION.getValue());
            oPacket.write(0x3A);
            oPacket.writeInt(gid);
            oPacket.write(capacity);
            return oPacket.getPacket();
        }

        public static byte[] showGuildRanks(int npcid, ResultSet rs) throws SQLException {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.GUILD_OPERATION.getValue());
            oPacket.write(0x49);
            oPacket.writeInt(npcid);
            if (!rs.last()) { // no guilds o.o
                oPacket.writeInt(0);
                return oPacket.getPacket();
            }
            oPacket.writeInt(rs.getRow()); // number of entries
            rs.beforeFirst();
            while (rs.next()) {
                oPacket.writeMapleAsciiString(rs.getString("name"));
                oPacket.writeInt(rs.getInt("GP"));
                oPacket.writeInt(rs.getInt("logo"));
                oPacket.writeInt(rs.getInt("logoColor"));
                oPacket.writeInt(rs.getInt("logoBG"));
                oPacket.writeInt(rs.getInt("logoBGColor"));
            }
            return oPacket.getPacket();
        }

        public static byte[] showPlayerRanks(int npcid, ResultSet rs) throws SQLException {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.GUILD_OPERATION.getValue());
            oPacket.write(0x49);
            oPacket.writeInt(npcid);
            if (!rs.last()) {
                oPacket.writeInt(0);
                return oPacket.getPacket();
            }
            oPacket.writeInt(rs.getRow());
            rs.beforeFirst();
            while (rs.next()) {
                oPacket.writeMapleAsciiString(rs.getString("name"));
                oPacket.writeInt(rs.getInt("level"));
                oPacket.writeInt(0);
                oPacket.writeInt(0);
                oPacket.writeInt(0);
                oPacket.writeInt(0);
            }
            return oPacket.getPacket();
        }

        public static byte[] updateGP(int gid, int GP) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.GUILD_OPERATION.getValue());
            oPacket.write(0x48);
            oPacket.writeInt(gid);
            oPacket.writeInt(GP);
            return oPacket.getPacket();
        }
    }

    public static class AllianceResult {

        private static void getGuildInfo(final MaplePacketLittleEndianWriter oPacket, MapleGuild guild) {
            oPacket.writeInt(guild.getId());
            oPacket.writeMapleAsciiString(guild.getName());
            for (int i = 1; i <= 5; i++) {
                oPacket.writeMapleAsciiString(guild.getRankTitle(i));
            }
            Collection<MapleGuildCharacter> members = guild.getMembers();
            oPacket.write(members.size());
            for (MapleGuildCharacter mgc : members) {
                oPacket.writeInt(mgc.getId());
            }
            for (MapleGuildCharacter mgc : members) {
                oPacket.writeAsciiString(PacketHelper.getRightPaddedStr(mgc.getName(), '\0', 13));
                oPacket.writeInt(mgc.getJobId());
                oPacket.writeInt(mgc.getLevel());
                oPacket.writeInt(mgc.getGuildRank());
                oPacket.writeInt(mgc.isOnline() ? 1 : 0);
                oPacket.writeInt(mgc.getGP());
                oPacket.writeInt(mgc.getAllianceRank());
            }
            oPacket.writeInt(guild.getCapacity());
            oPacket.writeShort(guild.getLogoBG());
            oPacket.write(guild.getLogoBGColor());
            oPacket.writeShort(guild.getLogo());
            oPacket.write(guild.getLogoColor());
            oPacket.writeMapleAsciiString(guild.getNotice());
            oPacket.writeInt(guild.getGP());
            oPacket.writeInt(guild.getAllianceId());
        }

        public static byte[] getAllianceInfo(MapleAlliance alliance) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
            oPacket.write(0x0C);
            oPacket.write(1);
            addAllianceInfo(oPacket, alliance);
            return oPacket.getPacket();
        }

        public static byte[] makeNewAlliance(MapleAlliance alliance, MapleClient c) throws RemoteException {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
            oPacket.write(0x0F);
            addAllianceInfo(oPacket, alliance);
            for (Integer guild : alliance.getGuilds()) {
                getGuildInfo(oPacket, ChannelServer.getInstance().getWorldInterface().getGuild(guild, c.getPlayer().getMGC()));
            }
            return oPacket.getPacket();
        }

        public static byte[] getGuildAlliances(MapleAlliance alliance) throws RemoteException {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
            oPacket.write(0x0D);
            oPacket.writeInt(alliance.getGuilds().size());
            for (Integer guild : alliance.getGuilds()) {
                getGuildInfo(oPacket, ChannelServer.getInstance().getWorldInterface().getGuild(guild, null));
            }
            return oPacket.getPacket();
        }

        public static byte[] addGuildToAlliance(MapleAlliance alliance, int newGuild) throws RemoteException {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
            oPacket.write(0x12);
            addAllianceInfo(oPacket, alliance);
            oPacket.writeInt(newGuild);
            getGuildInfo(oPacket, WorldServer.getInstance().getGuild(newGuild, null));
            return oPacket.getPacket();
        }

        private static void addAllianceInfo(MaplePacketLittleEndianWriter oPacket, MapleAlliance alliance) {
            oPacket.writeInt(alliance.getId());
            oPacket.writeMapleAsciiString(alliance.getName());
            for (int i = 1; i <= 5; i++) {
                oPacket.writeMapleAsciiString(alliance.getRankTitle(i));
            }
            oPacket.write(alliance.getGuilds().size());
            for (Integer guild : alliance.getGuilds()) {
                oPacket.writeInt(guild);
            }
            oPacket.writeInt(alliance.getCapacity());
            oPacket.writeMapleAsciiString(alliance.getNotice());
        }

        public static byte[] allianceMemberOnline(MapleCharacter mc, boolean online) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
            oPacket.write(0x0E);
            oPacket.writeInt(mc.getGuild().getAllianceId());
            oPacket.writeInt(mc.getGuildId());
            oPacket.writeInt(mc.getId());
            oPacket.write(online ? 1 : 0);
            return oPacket.getPacket();
        }

        public static byte[] allianceNotice(int id, String notice) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
            oPacket.write(0x1C);
            oPacket.writeInt(id);
            oPacket.writeMapleAsciiString(notice);
            return oPacket.getPacket();
        }

        public static byte[] changeAllianceRankTitle(int alliance, String[] ranks) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
            oPacket.write(0x1A);
            oPacket.writeInt(alliance);
            for (int i = 0; i < 5; i++) {
                oPacket.writeMapleAsciiString(ranks[i]);
            }
            return oPacket.getPacket();
        }

        public static byte[] updateAllianceJobLevel(MapleCharacter mc) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
            oPacket.write(0x18);
            oPacket.writeInt(mc.getGuild().getAllianceId());
            oPacket.writeInt(mc.getGuildId());
            oPacket.writeInt(mc.getId());
            oPacket.writeInt(mc.getLevel());
            oPacket.writeInt(mc.getJob().getId());
            return oPacket.getPacket();
        }

        public static byte[] removeGuildFromAlliance(MapleAlliance alliance, int expelledGuild) throws RemoteException {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
            oPacket.write(0x10);
            oPacket.writeInt(alliance.getId());
            oPacket.writeMapleAsciiString(alliance.getName());
            for (int i = 1; i <= 5; i++) {
                oPacket.writeMapleAsciiString(alliance.getRankTitle(i));
            }
            oPacket.write(alliance.getGuilds().size());
            for (Integer guild : alliance.getGuilds()) {
                oPacket.writeInt(guild);
            }
            oPacket.writeInt(2);
            oPacket.writeMapleAsciiString(alliance.getNotice());
            oPacket.writeInt(expelledGuild);
            getGuildInfo(oPacket, WorldServer.getInstance().getGuild(expelledGuild, null));
            oPacket.write(0x01);
            return oPacket.getPacket();
        }

        public static byte[] disbandAlliance(int alliance) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
            oPacket.write(0x1D);
            oPacket.writeInt(alliance);
            return oPacket.getPacket();
        }

        public static byte[] sendShowInfo(int allianceid, int characterID) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
            oPacket.write(0x02);
            oPacket.writeInt(allianceid);
            oPacket.writeInt(characterID);
            return oPacket.getPacket();
        }

        public static byte[] sendInvitation(int allianceid, int characterID, final String allianceName) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
            oPacket.write(3);
            oPacket.writeInt(allianceid);
            oPacket.writeMapleAsciiString(allianceName);
            oPacket.writeMapleAsciiString("Required");
            return oPacket.getPacket();
        }

        public static byte[] sendChangeGuild(int allianceid, int characterID, int guildid, int option) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
            oPacket.write(0x07);
            oPacket.writeInt(allianceid);
            oPacket.writeInt(guildid);
            oPacket.writeInt(characterID);
            oPacket.write(option);
            return oPacket.getPacket();
        }

        public static byte[] sendChangeLeader(int allianceid, int characterID, int victim) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
            oPacket.write(0x08);
            oPacket.writeInt(allianceid);
            oPacket.writeInt(characterID);
            oPacket.writeInt(victim);
            return oPacket.getPacket();
        }

        public static byte[] sendChangeRank(int allianceid, int characterID, int int1, byte byte1) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
            oPacket.write(0x09);
            oPacket.writeInt(allianceid);
            oPacket.writeInt(characterID);
            oPacket.writeInt(int1);
            oPacket.writeInt(byte1);
            return oPacket.getPacket();
        }
    }

    public static class TownPortal {

        public static byte[] Created(int townId, int targetId, int skillid, Point pos) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.SPAWN_PORTAL.getValue());
            oPacket.writeInt(townId);
            oPacket.writeInt(targetId);
            oPacket.writeInt(skillid);
            oPacket.writePos(pos != null ? pos : null);
            return oPacket.getPacket();
        }

        public static byte[] Removed(Point pos) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.SPAWN_PORTAL.getValue());
            oPacket.writeInt(999999999);
            oPacket.writeInt(999999999);
            oPacket.writeInt(0);
            oPacket.writePos(pos);
            return oPacket.getPacket();
        }
    }

    public static class OpenGate {

        public static byte[] encode() {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.OPEN_GATE.getValue());
            oPacket.writeShort(0);
            oPacket.writeShort(0);
            return oPacket.getPacket();
        }
    }

    public static class BroadcastMsg {

        public static byte[] encode(String pDlg) {
            return BroadcastMsg.encode(4, 0, pDlg, true, false, 0);
        }

        public static byte[] encode(int type, String pDlg) {
            return BroadcastMsg.encode(type, 0, pDlg, false, false, 0);
        }

        public static byte[] encode(int type, String pDlg, int nTemplateID) {
            return BroadcastMsg.encode(type, 0, pDlg, false, false, nTemplateID);
        }

        public static byte[] encode(int type, int channel, String pDlg) {
            return BroadcastMsg.encode(type, channel, pDlg, false, false, 0);
        }

        public static byte[] encode(int type, int channel, String pDlg, boolean bWhisperIcon) {
            return BroadcastMsg.encode(type, channel, pDlg, false, bWhisperIcon, 0);
        }

        private static byte[] encode(int type, int channel, String pDlg, boolean sNotice, boolean bWhisperIcon, int nTemplateID) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.SERVERMESSAGE.getValue());
            oPacket.write(type);
            if (sNotice || type == 4) {
                oPacket.write(1);
            }
            if (type != 23 && type != 24) {
                oPacket.writeMapleAsciiString(pDlg);
            }
            switch (type) {
                case 3:
                case 0x14:
                    oPacket.write(channel);
                    oPacket.writeBoolean(bWhisperIcon);
                    break;
                case 9:
                    oPacket.write(channel);
                    break;
                case 0xB:
                case 6:
                case 7:
                case 0x12:
                    oPacket.writeInt(nTemplateID);
                    break;
                default:
                    break;
            }
            return oPacket.getPacket();
        }

        public static byte[] ItemMegaphone(Item item, String pDlg, int channel, boolean bWhisperIcon) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.SERVERMESSAGE.getValue());
            oPacket.write(8);
            oPacket.writeMapleAsciiString(pDlg);
            oPacket.write(channel);
            oPacket.write(bWhisperIcon ? 1 : 0);
            oPacket.write(item != null ? item.getPosition() : 0);
            if (item != null) {
                PacketHelper.encodeItemSlotBase(oPacket, item, true);
            }
            return oPacket.getPacket();
        }

        public static byte[] MultiMegaphone(String[] pDlgs, int channel, boolean bWhisperIcon) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.SERVERMESSAGE.getValue());
            oPacket.write(0xA);
            if (pDlgs[0] != null) {
                oPacket.writeMapleAsciiString(pDlgs[0]);
            }
            oPacket.write(pDlgs.length);
            for (int i = 1; i < pDlgs.length; i++) {
                if (pDlgs[i] != null) {
                    oPacket.writeMapleAsciiString(pDlgs[i]);
                }
            }
            oPacket.write(channel);
            oPacket.write(bWhisperIcon ? 1 : 0);
            return oPacket.getPacket();
        }

        public static byte[] Gachapon(MapleCharacter character, Item item, String strFieldname) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.SERVERMESSAGE.getValue());
            oPacket.write(0xC);
            oPacket.writeMapleAsciiString(character.getName() + " : got a(n)");
            oPacket.writeInt(character.getClient().getChannel());
            oPacket.writeMapleAsciiString(strFieldname);
            PacketHelper.encodeItemSlotBase(oPacket, item, true);
            return oPacket.getPacket();
        }

        public static byte[] unknown(MapleCharacter character, Item item, boolean E) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.SERVERMESSAGE.getValue());
            oPacket.write(E ? 0xE : 0xD);
            oPacket.writeMapleAsciiString(character.getName());
            PacketHelper.encodeItemSlotBase(oPacket, item, true);
            return oPacket.getPacket();
        }
    }

    public static byte[] IncubatorResult(Item item) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.INCUBATOR_RESULT.getValue());
        oPacket.writeInt(item.getItemId());
        oPacket.writeShort(item.getQuantity());
        return oPacket.getPacket();
    }

    public static byte[] ShopScannerResult(MapleClient c, byte sortByPrice, int itemid, List<PlayerShop> shops, List<MaplePlayerShopItem> items) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SHOP_SCANNER_RESULT.getValue());
        oPacket.write(6);
        oPacket.writeInt(0);// nNpcShopPrice
        oPacket.writeInt(itemid);
        oPacket.writeInt(items.size());
        for (PlayerShop hm : shops) {
            for (MaplePlayerShopItem item : hm.getItems()) {
                if (item.getItem().getItemId() == itemid && item.isExist()) {
                    oPacket.writeMapleAsciiString(hm.getOwnerName());
                    oPacket.writeInt(hm.getMapId());
                    oPacket.writeMapleAsciiString(hm.getDescription());
                    oPacket.writeInt(item.getItem().getQuantity());
                    oPacket.writeInt(item.getItem().getPerBundle());
                    oPacket.writeInt(item.getPrice());
                    oPacket.writeInt(hm.getOwnerId());
                    oPacket.write(hm.getFreeSlot() == -1 ? 1 : 0);
                    oPacket.write(ItemInformationProvider.getInstance().getInventoryType(item.getItem().getItemId()).getType());
                    if (item.getItem().getItemId() / 1000000 == 1) {
                        PacketHelper.encodeItemSlotBase(oPacket, item.getItem(), true);
                    }
                }
            }
        }
        return oPacket.getPacket();
    }

    public static byte[] ShopLinkResult(int type) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SHOP_LINK_RESULT.getValue());
        oPacket.write(type);
        return oPacket.getPacket();
    }

    public static byte[] MarriageRequest(String CharacterName, int CharacterID, boolean wishList) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MARRIAGE_REQUEST.getValue());
        oPacket.write(wishList ? 9 : 0);
        if (!wishList) {
            oPacket.writeMapleAsciiString(CharacterName); // name
            oPacket.writeInt(CharacterID);
        }
        return oPacket.getPacket();
    }

    public static class MarriageResult {

        public static byte[] encode(int mode) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.MARRIAGE_RESULT.getValue());
            oPacket.write(mode);
            return oPacket.getPacket();
        }

        public static byte[] Done(MapleCharacter character, boolean wedding, int marriageid) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.MARRIAGE_RESULT.getValue());
            oPacket.write(wedding ? 12 : 11);
            oPacket.writeInt(marriageid);
            oPacket.writeInt(character.getGender() == 0 ? character.getId() : character.getMarriedTo());
            oPacket.writeInt(character.getGender() == 0 ? character.getMarriedTo() : character.getId());
            oPacket.writeShort(wedding ? 3 : 1); // impossible, always 1
            if (wedding) {
                oPacket.writeInt(character.getMarriageRingID());
                oPacket.writeInt(character.getMarriageRingID());
            } else {
                oPacket.writeInt(character.getEngagementRingID());
                oPacket.writeInt(character.getEngagementRingID());
            }
            oPacket.writeAsciiString(StringUtil.getRightPaddedStr(character.getGender() == 0 ? character.getName() : MapleCharacter.getNameById(character.getMarriedTo()), '\0', 13));
            oPacket.writeAsciiString(StringUtil.getRightPaddedStr(character.getGender() == 0 ? MapleCharacter.getNameById(character.getMarriedTo()) : character.getName(), '\0', 13));
            return oPacket.getPacket();
        }

        public static byte[] Invite(String groom, String bride, int nType) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.MARRIAGE_RESULT.getValue());
            oPacket.write(15);
            oPacket.writeMapleAsciiString(groom);
            oPacket.writeMapleAsciiString(bride);
            oPacket.writeShort(nType);
            // 0 = Cathedral Normal
            // 1 = Cathedral Premium
            // 2 = Chapel Normal
            return oPacket.getPacket();
        }

        public static byte[] Result() {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.MARRIAGE_RESULT.getValue());
            oPacket.write(36);
            oPacket.write(1);
            oPacket.writeMapleAsciiString("You are now engaged.");
            return oPacket.getPacket();
        }
    }

    public static class WeddingGiftResult {

        public static byte[] encode(int mode) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.WEDDING_GIFT_RESULT.getValue());
            oPacket.write(mode);
            return oPacket.getPacket();
        }

        public static byte[] Give(MapleCharacter character) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.WEDDING_GIFT_RESULT.getValue());
            oPacket.write(0x0B);
            // CWishListGiveDlg::SetWishList
            oPacket.write(0);
            // CWishListGiveDlg::SetGetItems
            int dbcharFlag = -1;
            oPacket.writeLong(dbcharFlag);
            if ((dbcharFlag & DBChar.ItemSlotEquip) > 0) {
                oPacket.write(0);
                for (Item item : character.getInventory(MapleInventoryType.EQUIP).list()) {
                    PacketHelper.encodeItemSlotBase(oPacket, item);
                }
            }
            if ((dbcharFlag & DBChar.ItemSlotConsume) > 0) {
                oPacket.write(0);
                for (Item item : character.getInventory(MapleInventoryType.USE).list()) {
                    PacketHelper.encodeItemSlotBase(oPacket, item);
                }
            }
            if ((dbcharFlag & DBChar.ItemSlotInstall) > 0) {
                oPacket.write(0);
                for (Item item : character.getInventory(MapleInventoryType.SETUP).list()) {
                    PacketHelper.encodeItemSlotBase(oPacket, item);
                }
            }
            if ((dbcharFlag & DBChar.ItemSlotEtc) > 0) {
                oPacket.write(0);
                for (Item item : character.getInventory(MapleInventoryType.ETC).list()) {
                    PacketHelper.encodeItemSlotBase(oPacket, item);
                }
            }
            if ((dbcharFlag & DBChar.ItemSlotCash) > 0) {
                oPacket.write(0);
                for (Item item : character.getInventory(MapleInventoryType.CASH).list()) {
                    PacketHelper.encodeItemSlotBase(oPacket, item);
                }
            }
            return oPacket.getPacket();
        }

        public static byte[] Receive(MapleCharacter character) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.WEDDING_GIFT_RESULT.getValue());
            oPacket.write(0xF);
            // CWishListRecvDlg::SetGetItems
            int dbcharFlag = -1;
            oPacket.writeLong(dbcharFlag);
            if ((dbcharFlag & DBChar.ItemSlotEquip) > 0) {
                oPacket.write(0);
                for (Item item : character.getInventory(MapleInventoryType.EQUIP).list()) {
                    PacketHelper.encodeItemSlotBase(oPacket, item);
                }
            }
            if ((dbcharFlag & DBChar.ItemSlotConsume) > 0) {
                oPacket.write(0);
                for (Item item : character.getInventory(MapleInventoryType.USE).list()) {
                    PacketHelper.encodeItemSlotBase(oPacket, item);
                }
            }
            if ((dbcharFlag & DBChar.ItemSlotInstall) > 0) {
                oPacket.write(0);
                for (Item item : character.getInventory(MapleInventoryType.SETUP).list()) {
                    PacketHelper.encodeItemSlotBase(oPacket, item);
                }
            }
            if ((dbcharFlag & DBChar.ItemSlotEtc) > 0) {
                oPacket.write(0);
                for (Item item : character.getInventory(MapleInventoryType.ETC).list()) {
                    PacketHelper.encodeItemSlotBase(oPacket, item);
                }
            }
            if ((dbcharFlag & DBChar.ItemSlotCash) > 0) {
                oPacket.write(0);
                for (Item item : character.getInventory(MapleInventoryType.CASH).list()) {
                    PacketHelper.encodeItemSlotBase(oPacket, item);
                }
            }
            return oPacket.getPacket();
        }
    }

    public static byte[] NotifyWeddingPartnerTransfer(int dwMarriedPartnerCurFieldID, int nMarriedPartnerID) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.NOTIFY_MARRIED_PARTNER_MAP_TRANSFER.getValue());
        oPacket.writeInt(dwMarriedPartnerCurFieldID);
        oPacket.writeInt(nMarriedPartnerID);
        return oPacket.getPacket();
    }

    public static byte[] CashPetFoodResult(boolean consumed) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CASH_PET_FOOD_RESULT.getValue());
        oPacket.writeBoolean(consumed);
        return oPacket.getPacket();
    }

    // Dòng chữ màu vàng chạy khắp server
    public static byte[] SetWeekEventMessage(String tip) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SET_WEEK_EVENT_MESSAGE.getValue());
        oPacket.write(1); // WeekEventMessagePrinted
        oPacket.writeMapleAsciiString(tip);
        return oPacket.getPacket();
    }

    public static byte[] SetPotionDiscountRate(int nPotionDiscountRate) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SET_POTION_DISCOUNT_RATE.getValue());
        oPacket.write(nPotionDiscountRate / 100);
        return oPacket.getPacket();
    }

    public static byte[] BridleMobCatchFail(int bReason, int nItemID) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.BRIDLE_MOB_CATCH_FAIL.getValue());
        oPacket.write(bReason);
        // 1 = "Element Rock cannot be used right after usage.", 
        // 2 = "Unable to capture the monster; the monster is too strong."
        oPacket.writeInt(nItemID);
        oPacket.writeInt(0);
        return oPacket.getPacket();
    }

    public static class MonsterBook {

        public static byte[] SetCard(boolean full, int nCardID, int nCardCount) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.MONSTER_BOOK_SET_CARD.getValue());
            oPacket.write(full ? 0 : 1);
            if (!full) {
                oPacket.writeInt(nCardID);
                oPacket.writeInt(nCardCount);
            }
            return oPacket.getPacket();
        }

        public static byte[] SetCover(int nCardID) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.MONSTER_BOOK_SET_COVER.getValue());
            oPacket.writeInt(nCardID);
            return oPacket.getPacket();
        }
    }

    public static byte[] HourChanged(short wDayOfWeek) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.HOUR_CHANGED.getValue());
        oPacket.writeShort(wDayOfWeek);
        oPacket.writeShort(0);
        // CDayOfWeek::SetCurrentDay(wDayOfWeek)
        return oPacket.getPacket();
    }

    public static byte[] MiniMapOnOff(boolean MiniMapOnOff) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MINIMAP_ON_OFF.getValue());
        oPacket.writeBoolean(MiniMapOnOff);
        return oPacket.getPacket();
    }

    public static byte[] SessionValue(String sKey, int sVal) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SESSION_VALUE.getValue());
        oPacket.writeMapleAsciiString(sKey);
        // sKey = "energy", "massacre_hit", "massacre_miss", "massacre_cool", "massacre_skill", "PRaid_Team", "balloon_Team", "redTeam", "blueTeam"
        oPacket.writeMapleAsciiString(Integer.toString(sVal));
        return oPacket.getPacket();
    }

    public static byte[] PartyValue(String sKey, int PartyRaidPoint) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PARTY_VALUE.getValue());
        oPacket.writeMapleAsciiString(sKey);
        // sKey = "PRaid_Point"
        oPacket.writeMapleAsciiString(Integer.toString(PartyRaidPoint));
        return oPacket.getPacket();
    }

    public static byte[] FieldSetVariable(String sKey, int PartyRaidStageMine) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.FIELD_SET_VARIABLE.getValue());
        oPacket.writeMapleAsciiString(sKey);
        // sKey = "Red_Stage", "Blue_Stage", "Bamboo_Used"
        oPacket.writeMapleAsciiString(Integer.toString(PartyRaidStageMine));
        return oPacket.getPacket();
    }

    public static byte[] BonusExpRateChanged(int type, int nHour, int ItemID) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.BONUS_EXP_RATE_CHANGED.getValue());
        oPacket.writeInt(type);
        oPacket.writeInt(nHour);
        // nHour > 0 = "After %d hrs. of equipping %s, additional %d%% bonus EXP will be rewarded when you hunt monsters."
        // nHour < 0 = "For equipping %s, additional %d%% bonus EXP will be rewarded when you hunt monsters."
        oPacket.writeInt(ItemID);
        return oPacket.getPacket();
    }

    public static byte[] PotionDiscountRateChanged(int type, int nHour, int ItemID) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.POTION_DISCOUNT_RATE_CHANGED.getValue());
        oPacket.writeInt(type);
        oPacket.writeInt(ItemID);
        // For equipping %s, potions will be discounted %d%% when you visit store.
        return oPacket.getPacket();
    }

    /**
     Gửi packet thông báo "Lên cấp" đến Guild hoặc Family với các nType như sau:
     1: [Family]: <%s> %s has reached Lv. %d. - The Reps you have received from
     %s will be reduced in half.
     1: [Family]: <%s> %s has reached Lv. %d.
     2: [Guild]: <%s> %s has reached Lv. %d.
     */
    public static byte[] NotifyLevelUp(int nType, int level, String strCharacterName) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.NOTIFY_LEVELUP.getValue());
        oPacket.write(nType);
        oPacket.writeInt(level);
        oPacket.writeMapleAsciiString(strCharacterName);
        return oPacket.getPacket();
    }

    /**
     Gửi packet thông báo "Đã Cưới" đến Guild hoặc Family với các nType như sau:
     0: [Guild] <%s> %s is now married. Please congratulate them.
     1: [Family] <%s> %s is now married. Please congratulate them.
     */
    public static byte[] NotifyWedding(int nType, String strCharacterName) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.NOTIFY_MARRIAGE.getValue());
        oPacket.write(nType);
        oPacket.writeMapleAsciiString("> " + strCharacterName); // To fix the stupid packet lol
        return oPacket.getPacket();
    }

    /**
     Gửi packet thông báo "Chuyển Nghề" đến Guild hoặc Family với các nType như
     sau:
     0: [Guild] <%s> %s has advanced to a(an) %s.
     1: [Family] <%s> %s has advanced to a(an) %s.
     */
    public static byte[] NotifyJobChange(int nType, int nJob, String strCharacterName) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.NOTIFY_JOB_CHANGE.getValue());
        oPacket.write(nType);
        oPacket.writeInt(nJob);
        oPacket.writeMapleAsciiString("> " + strCharacterName);
        return oPacket.getPacket();
    }

    // Gửi Packet về Maple TV Use Res
    public static byte[] MapleTVUseRes(String text) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MAPLE_TV_USE_RES.getValue());
        oPacket.writeMapleAsciiString(text);
        return oPacket.getPacket();
    }

    // Gửi Packet về Avatar Megaphone Res
    public static byte[] AvatarMegaphoneRes(int delay, String text) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.AVATAR_MEGAPHONE_RESULT.getValue());
        oPacket.write(delay);
        oPacket.writeMapleAsciiString(text);
        return oPacket.getPacket();
    }

    // Gửi Packet về sử dụng Avatar Super Megaphone
    public static byte[] SetAvatarMegaphone(MapleCharacter character, int nItemID, String nMedal, List<String> message, int nChannel, boolean bWhisper) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SET_AVATAR_MEGAPHONE.getValue());
        oPacket.writeInt(nItemID);
        oPacket.writeMapleAsciiString(nMedal + character.getName()); // sName
        for (String s : message) {
            oPacket.writeMapleAsciiString(s);
        }
        oPacket.writeInt(nChannel); // channel
        oPacket.writeBoolean(bWhisper);
        encodeAvatarLook(oPacket, character, true);
        return oPacket.getPacket();
    }

    // Gửi Packet về việc xoá bỏ Tiger Megaphone
    public static byte[] ClearAvatarMegaphone() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CLEAR_AVATAR_MEGAPHONE.getValue());
        return oPacket.getPacket();
    }

    // Gửi Packet về kết quả huỷ bỏ đổi tên của nhân vật
    public static byte[] CancelNameChangeResult(int type, boolean allow, String result) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CANCEL_NAME_CHANGE_RESULT.getValue());
        oPacket.write(type);
        oPacket.writeBoolean(allow);
        if (allow) {
            oPacket.writeMapleAsciiString(result);
        }
        return oPacket.getPacket();
    }

    // Gửi Packet về kết quả huỷ bỏ đổi thế giới của nhân vật
    public static byte[] CancelTransferWorldResult(int type, boolean allow, String result) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CANCEL_TRANSFER_WORLD_RESULT.getValue());
        oPacket.write(type);
        oPacket.writeBoolean(allow);
        if (allow) {
            oPacket.writeMapleAsciiString(result);
        }
        return oPacket.getPacket();
    }

    // Gửi Packet về kết quả huỷ bỏ đổi thế giới của nhân vật
    public static byte[] DestroyShopResult(int ForcedClosesByGM, boolean Error, String result) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CANCEL_TRANSFER_WORLD_RESULT.getValue());
        oPacket.write(ForcedClosesByGM);
        oPacket.writeBoolean(Error);
        oPacket.writeMapleAsciiString(result);
        return oPacket.getPacket();
    }

    // Gửi Packet về thông báo GM ão
    public static byte[] FakeGMNotice() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.FAKE_GM_NOTICE.getValue());
        oPacket.write(0);
        return oPacket.getPacket();
    }

    // Gửi Packet về thành công khi sử dụng Gachaphon Box
    public static byte[] SuccessInUsegachaponBox(int CountedDummy) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SUCCESS_IN_USE_GACHAPON_BOX.getValue());
        oPacket.writeInt(CountedDummy);
        return oPacket.getPacket();
    }

    // Gửi Packet về New Year Card Record
    public static byte[] NewYearCardRes(int mode, int type, List<Integer> NewYearCardRecord) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.NEW_YEAR_CARD_RES.getValue());
        oPacket.write(mode);
        switch (mode) {
            case 4:
            case 6:
                // GW_NewYearCardRecord::encode
                // CUserPool::OnNewYearCardRecordAdd
                break;
            case 5:
            case 7:
            case 9:
            case 11:
                oPacket.write(type);
                // 15 = "You cannot send a card to yourself!"
                // 16 = "You have no free slot to store card.\r\ntry later on please."
                // 17 = "You have no card to send."
                // 18 = "Wrong inventory information !"
                // 19 = "Cannot find such character !"
                // 20 = "Incoherent Data !  "
                // 21 = "An error occured during DB operation."
                // 22 = "An unknown error occured !"
                break;
            case 8:
                oPacket.writeInt(NewYearCardRecord.size());
                // "Successfully deleted a New Year Card."
                break;
            case 10:
                oPacket.writeInt(NewYearCardRecord.size());
                if ((NewYearCardRecord.size() - 1) <= 98 && NewYearCardRecord.size() > 0) {
                    for (int i : NewYearCardRecord) {
                        oPacket.writeInt(i); // dwSN
                        oPacket.writeInt(0);
                        oPacket.writeMapleAsciiString(""); // sSenderName
                    }
                }
                // CUIFadeYesNo::CreateNewYearCardArrived
                break;
            case 12:
                oPacket.writeInt(0); // dwSN
                oPacket.writeMapleAsciiString(""); // dwSN
                // CUIFadeYesNo::CreateNewYearCardArrived
                break;
            case 13:
                oPacket.writeInt(0); // dwSN
                oPacket.writeInt(0); // dwCharacterID
                // CUserPool::OnNewYearCardRecordAdd
                break;
            case 14:
                oPacket.writeInt(0); // dwSN
                // CUserPool::OnNewYearCardRecordRemove
                break;

        }
        return oPacket.getPacket();
    }

    // Gửi Packet về Random Morph Res
    public static byte[] RandomMorphRes(boolean random, boolean inTown, String sTarget) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SUCCESS_IN_USE_GACHAPON_BOX.getValue());
        oPacket.writeBoolean(random);
        if (random) {
            oPacket.writeBoolean(inTown);
            if (!inTown) {
                oPacket.writeMapleAsciiString(sTarget); // Failed to find user %s.
            }
        }
        return oPacket.getPacket();
    }

    // Gửi Packet về việc huỷ bỏ đổi tên từ người khác
    public static byte[] CancelNameChangebyOther() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CANCEL_NAME_CHANGE_BY_OTHER.getValue());
        // The character you logged on with\r\nhas not requested a name change.
        return oPacket.getPacket();
    }

    // Gửi Packet về điều chỉnh mua trang bị đã có sẵn
    public static byte[] SetBuyEquipExt(boolean BuyEquipExt) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SET_BUY_EQUIP_EXT.getValue());
        oPacket.writeBoolean(BuyEquipExt);
        return oPacket.getPacket();
    }

    // Gửi Packet về điều chỉnh yêu cầu người đi theo (Follower)
    public static byte[] SetPassenserRequest(int nPassenserID) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SET_BUY_EQUIP_EXT.getValue());
        oPacket.writeInt(nPassenserID);
        return oPacket.getPacket();
    }

    // Gửi Packet về hiển thị tin nhắn về quá trình Script
    public static byte[] ScriptProgressMessage(String sMsg) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SCRIPT_PROGRESS_MESSAGE.getValue());
        oPacket.writeMapleAsciiString(sMsg);
        return oPacket.getPacket();
    }

    // Gửi Packet về việc thất bại khi kiểm tra dữ liệu CRC
    public static byte[] DataCRCCheckFailed(String text) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.DATA_CRC_CHECK_FAILED.getValue());
        oPacket.writeMapleAsciiString(text);
        return oPacket.getPacket();
    }

    // Gửi Packet về Cake Pie Event
    public static byte[] CakePieEventResult(CakePieEvent CakePieEvent) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CAKE_PIE_EVENT_RESULT.getValue());
        oPacket.write(CakePieEvent.getCount());
        if (CakePieEvent.getCount() > 0) {
            oPacket.writeInt(CakePieEvent.getFieldID());
            oPacket.writeInt(CakePieEvent.getItemID());
            oPacket.write(CakePieEvent.getPercentage());
            oPacket.writeBoolean(ServerConstants.CakePie);
            oPacket.write(CakePieEvent.getWinnerTeam());
        }
        // CCakePieEvent::SetEventItemInfo
        return oPacket.getPacket();
    }

    // Gửi Packet về cập nhập bảng GM qua đường Website
    public static byte[] UpdateGMBoard(int WebOpBoardIndex, String WebOpBoardURL) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.UPDATE_GM_BOARD.getValue());
        oPacket.writeInt(WebOpBoardIndex);
        oPacket.writeMapleAsciiString(WebOpBoardURL);
        // [GM] You have received a letter for the Maple Team. Please click on the envelope at the top right.
        return oPacket.getPacket();
    }

    // Gửi Packet về hiển thị kết quả khi sắp xếp vật phẩm
    public static byte[] ShowSlotMessage(int nEmptySlotMessageType) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SHOW_SLOT_MESSAGE.getValue());
        oPacket.write(nEmptySlotMessageType);
        return oPacket.getPacket();
    }

    // Gửi Packet về hiển thị thông tin của Báo Điên
    public static byte[] WildHunterInfo(MapleCharacter character) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.WILD_HUNTER_INFO.getValue());
        PacketHelper.encodeJaguar(oPacket, character);
        return oPacket.getPacket();
    }

    // Gửi Packet về thông tin thêm của tài khoản
    public static byte[] AccountMoreInfo(int mode, boolean save) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.ACCOUNT_MORE_INFO.getValue());
        oPacket.write(mode);
        if (mode == 4) {
            // CUIAccountMoreInfo::OnSaveAccountMoreInfoResult
            oPacket.writeBoolean(save);
        } else if (mode == 3) {
            // CUIAccountMoreInfo::OnLoadAccountMoreInfoResult
            oPacket.writeInt(0);
            oPacket.writeInt(0);
            oPacket.writeInt(0); // dwPlayStyle
            oPacket.writeInt(0); // dwActivityAregs
        }
        return oPacket.getPacket();
    }

    // Gửi Packet về thay đổi trạng thái trong StageSystem??
    public static byte[] StageChange(String state, int nStagePeriod) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.STAGE_CHANGE.getValue());
        oPacket.writeMapleAsciiString(state);
        oPacket.write(nStagePeriod);
        return oPacket.getPacket();
    }

    // Gửi Packet về Dragon Ball Box
    public static byte[] DragonBallBox(int tRemainTime, int nOrb, boolean bShowUI, boolean SetOrb, boolean bAbleToSummon) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.DRAGON_BALL_BOX.getValue());
        oPacket.writeInt(tRemainTime);
        oPacket.writeBoolean(bShowUI);
        oPacket.writeBoolean(SetOrb);
        oPacket.writeBoolean(bAbleToSummon);
        if (!SetOrb) {
            oPacket.writeInt(nOrb);
        }
        return oPacket.getPacket();
    }

    // Gửi Packet về Pams Song
    public static byte[] AskWhetherUsePamsSong() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.ASK_WHETHER_USE_PAMS_SONG.getValue());
        return oPacket.getPacket();
    }

    // Gửi Packet về chuyển kênh
    public static byte[] TransferChannel(int nTargetChannel, String text) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.TRANSFER_CHANNEL.getValue());
        oPacket.writeInt(nTargetChannel);
        oPacket.writeMapleAsciiString(text);
        return oPacket.getPacket();
    }

    public static byte[] getMacros(SkillMacro[] macros) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MACRO_SYS_DATA_INIT.getValue());
        int count = 0;
        for (int i = 0; i < 5; i++) {
            if (macros[i] != null) {
                count++;
            }
        }
        oPacket.write(count);
        for (int i = 0; i < 5; i++) {
            SkillMacro macro = macros[i];
            if (macro != null) {
                oPacket.writeMapleAsciiString(macro.getName());
                oPacket.write(macro.getShout());
                oPacket.writeInt(macro.getSkill1());
                oPacket.writeInt(macro.getSkill2());
                oPacket.writeInt(macro.getSkill3());
            }
        }
        return oPacket.getPacket();
    }

    public static byte[] SetPassengerRequest(int nPassengerID) {//
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SET_PASSENGER_REQUEST.getValue());
        oPacket.writeInt(nPassengerID);
        return oPacket.getPacket();
    }
    
    public static byte[] LogoutGift() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.LOGOUT_GIFT.getValue());
        return oPacket.getPacket();
    }
}
