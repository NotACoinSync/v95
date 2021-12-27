package tools.packets;

import client.*;
import client.inventory.*;
import client.player.SecondaryStat;
import constants.EquipSlot;
import constants.GameConstants;
import constants.ItemConstants;
import constants.ServerConstants;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import net.SendOpcode;
import net.server.PlayerCoolDownValueHolder;
import net.server.handlers.Interaction.PlayerInteractionHandler;
import server.*;
import server.quest.MapleQuest;
import tools.Pair;
import tools.StringUtil;
import tools.data.output.LittleEndianWriter;
import tools.data.output.MaplePacketLittleEndianWriter;

public class PacketHelper {

    private final static long FT_UT_OFFSET = 116444592000000000L;
    private final static long DEFAULT_TIME = 150842304000000000L;
    public final static long ZERO_TIME = 94354848000000000L;
    private final static long PERMANENT = 150841440000000000L;
    public final static int MaxBuffStat = 8;

    public static long getTime(long realTimestamp) {
        if (realTimestamp == -1) {
            return DEFAULT_TIME;// high number ll
        } else if (realTimestamp == -2) {
            return ZERO_TIME;
        } else if (realTimestamp == -3) {
            return PERMANENT;
        }
        return realTimestamp * 10000 + FT_UT_OFFSET;
    }

    private static void encodeCharacterStat(final MaplePacketLittleEndianWriter oPacket, MapleCharacter player) {
        oPacket.writeInt(player.getId()); // character id
        oPacket.writeAsciiString(StringUtil.getRightPaddedStr(player.getName(), '\0', 13));// sCharacterName
        oPacket.write(player.getGender()); // gender (0 = male, 1 = female)
        oPacket.write(player.getSkinColor().getId()); // skin color
        oPacket.writeInt(player.getFace()); // face
        oPacket.writeInt(player.getHair()); // hair
        for (int i = 0; i < 3; i++) {// pet locker sn, 24 bytes
            if (player.getPet(i) != null) { // Checked GMS.. and your pets stay when going into the cash shop.
                oPacket.writeLong(player.getPet(i).getUniqueId());
            } else {
                oPacket.writeLong(0);
            }
        }
        oPacket.write(player.getLevel()); // LEVEL
        oPacket.writeShort(player.getJob().getId()); // JOB
        oPacket.writeShort(player.getStr()); // STR
        oPacket.writeShort(player.getDex()); // DEX
        oPacket.writeShort(player.getInt()); // INT
        oPacket.writeShort(player.getLuk()); // LUK
        oPacket.writeInt(player.getHp()); // HP
        oPacket.writeInt(player.getMaxHp()); // MHP
        oPacket.writeInt(player.getMp()); // MP
        oPacket.writeInt(player.getMaxMp()); // MMP
        oPacket.writeShort(player.getRemainingAp()); // remaining ap
        if (GameConstants.hasExtendedSPTable(player.getJob())) {
            oPacket.write(player.getRemainingSpSize());
            for (int i = 0; i < player.getRemainingSps().length; i++) {
                if (player.getRemainingSpBySkill(i) > 0) {
                    oPacket.write(i);
                    oPacket.write(player.getRemainingSpBySkill(i));
                }
            }
        } else {
            oPacket.writeShort(player.getRemainingSp()); // remaining sp
        }
        oPacket.writeInt(player.getExp()); // current exp
        oPacket.writeShort(player.getFame()); // fame
        oPacket.writeInt(player.getGachaExp()); // Gacha Exp
        oPacket.writeInt(player.getMapId()); // current map id
        oPacket.write(player.getInitialSpawnpoint()); // spawnpoint
        oPacket.writeInt(0);// nPlayTime
        oPacket.writeShort(player.getSubJob());
    }

    public static void encodeAvatarLook(final LittleEndianWriter oPacket, MapleCharacter player, boolean mega) {
        oPacket.write(player.getGender()); // nGender
        oPacket.write(player.getSkinColor().getId()); // nSkin
        oPacket.writeInt(player.getFace()); // nFace
        oPacket.write(mega ? 0 : 1);
        oPacket.writeInt(player.getHair()); // anHairEquip
        encodeCharacterEquipment(oPacket, player);
    }

    public static void encodeAvatarLook(final MaplePacketLittleEndianWriter oPacket, MapleCharacterLook mcl, boolean mega) {
        oPacket.write(mcl.getGender());
        oPacket.write(mcl.getSkinColor().getId()); // skin color
        oPacket.writeInt(mcl.getFace()); // face
        oPacket.write(mega ? 0 : 1);
        oPacket.writeInt(mcl.getHair()); // hair
        encodeCharacterEquipment(oPacket, mcl);
    }

    public static void encodeCharacter(final MaplePacketLittleEndianWriter oPacket, MapleCharacter player) {// CharacterData::Decode
        long dbcharFlag = -1;
        oPacket.writeLong(dbcharFlag);
        oPacket.write(0); // nCombatOrders
        oPacket.write(0); // aliRemoveSN
        if ((dbcharFlag & DBChar.Character) > 0) {
            encodeCharacterStat(oPacket, player);// GW_CharacterStat::Decode
            oPacket.write(player.getBuddylist().getCapacity());// friendMax
            oPacket.write(player.getExplorerLinkedName() == null ? 0 : 1);
            if (player.getExplorerLinkedName() != null) {
                oPacket.writeMapleAsciiString(player.getExplorerLinkedName());
            }
        }
        if ((dbcharFlag & DBChar.Money) > 0) {
            oPacket.writeInt(player.getMeso());
        }

        encodeInventory(dbcharFlag, oPacket, player);

        if ((dbcharFlag & DBChar.SkillRecord) > 0) {
            encodeSkill(oPacket, player);
        }
        if ((dbcharFlag & DBChar.SkillCooltime) > 0) {
            encodeSkillCooldown(oPacket, player);
        }
        if ((dbcharFlag & DBChar.QuestRecord) > 0) {
            encodeQuest(oPacket, player);
        }
        if ((dbcharFlag & DBChar.QuestComplete) > 0) {
            encodeCompletedQuest(oPacket, player);
        }
        if ((dbcharFlag & DBChar.MiniGameRecord) > 0) {
            encodeMiniGame(oPacket, player);
        }
        if ((dbcharFlag & DBChar.CoupleRecord) > 0) {
            encodeRing(oPacket, player);
        }
        if ((dbcharFlag & DBChar.MapTransfer) > 0) {
            encodeTeleport(oPacket, player);
        }
        if ((dbcharFlag & DBChar.MonsterBookCover) > 0) {
            oPacket.writeInt(player.getMonsterBookCover());
        }
        if ((dbcharFlag & DBChar.MonsterBookCard) > 0) {
            encodeMonsterBook(oPacket, player);
        }
        if ((dbcharFlag & DBChar.NewYearCard) > 0) {
            encodeNewYearCardRecord(oPacket, player);
        }
        if ((dbcharFlag & DBChar.QuestRecordEx) > 0) {
            encodeArea(oPacket, player);
        }
        if ((dbcharFlag & DBChar.WildHunterInfo) > 0) {
            encodeJaguar(oPacket, player);
        }
        if ((dbcharFlag & DBChar.QuestComplete_Old) > 0) {
            oPacket.writeShort(0);
        }
        if ((dbcharFlag & DBChar.VisitorQuestLog) > 0) {
            oPacket.writeShort(0);
        }
    }

    private static void encodeNewYearCardRecord(final MaplePacketLittleEndianWriter oPacket, MapleCharacter player) {
        oPacket.writeShort(0);
    }

    private static void encodeTeleport(final MaplePacketLittleEndianWriter oPacket, MapleCharacter player) {
        final List<Integer> tele = player.getTrockMaps();
        final List<Integer> viptele = player.getVipTrockMaps();
        for (int i = 0; i < 5; i++) {
            oPacket.writeInt(tele.get(i));
        }
        for (int i = 0; i < 10; i++) {
            oPacket.writeInt(viptele.get(i));
        }
    }

    private static void encodeMiniGame(final MaplePacketLittleEndianWriter oPacket, MapleCharacter player) {
        oPacket.writeShort(0);
        /*
         for (int m = size; m > 0; m--) {
         oPacket.writeInt(0); // nGameID
         oPacket.writeInt(0); // nWin
         oPacket.writeInt(0); // nDraw
         oPacket.writeInt(0); // nLose
         oPacket.writeInt(0); // nScore
         }
         */
    }

    private static void encodeArea(final MaplePacketLittleEndianWriter oPacket, MapleCharacter player) {
        Map<Short, String> areaInfos = player.getAreaInfos();
        oPacket.writeShort(areaInfos.size());
        for (Short area : areaInfos.keySet()) {
            oPacket.writeShort(area);
            oPacket.writeMapleAsciiString(areaInfos.get(area));
            // CharacterData::InitQuestExFromRawStr
        }
    }

    public static void encodeJaguar(final MaplePacketLittleEndianWriter oPacket, MapleCharacter player) {
        if (!(player.getJob().getId() >= 3300 && player.getJob().getId() <= 3312)) {
            return;
        }
        oPacket.write(player.getIntNRecord(111112)); // JAGUAR
        for (int i = 0; i < 5; i++) { // Id của 5 con quái WH có thể bắt được.
            oPacket.writeInt(player.getCapturedMob(i));
        }
    }

    private static void encodeCharacterEquipment(final LittleEndianWriter oPacket, MapleCharacter player) {
        MapleInventory equip = player.getInventory(MapleInventoryType.EQUIPPED);
        Collection<Item> ii = ItemInformationProvider.getInstance().canWearEquipment(player, equip.list());
        Map<Short, Integer> myEquip = new LinkedHashMap<>();
        Map<Short, Integer> maskedEquip = new LinkedHashMap<>();
        for (Item item : ii) {
            short pos = (byte) (item.getPosition() * -1);
            if (pos < 100 && myEquip.get(pos) == null) {
                myEquip.put(pos, item.getItemId());
            } else if (pos > 100 && pos != 111) { // don't ask. o.o
                pos -= 100;
                if (myEquip.get(pos) != null) {
                    maskedEquip.put(pos, myEquip.get(pos));
                }
                myEquip.put(pos, item.getItemId());
            } else if (myEquip.get(pos) != null) {
                maskedEquip.put(pos, item.getItemId());
            }
        }
        for (Entry<Short, Integer> entry : myEquip.entrySet()) {
            oPacket.write(entry.getKey());
            oPacket.writeInt(entry.getValue());
        }
        oPacket.write(0xFF);
        for (Entry<Short, Integer> entry : maskedEquip.entrySet()) {
            oPacket.write(entry.getKey());
            oPacket.writeInt(entry.getValue());
        }
        oPacket.write(0xFF);
        Item cWeapon = equip.getItem((short) -111);
        oPacket.writeInt(cWeapon != null ? cWeapon.getItemId() : 0);
        for (int i = 0; i < 3; i++) {
            if (player.getPet(i) != null) {
                oPacket.writeInt(player.getPet(i).getItemId());
            } else {
                oPacket.writeInt(0);
            }
        }
    }

    private static void encodeCharacterEquipment(final MaplePacketLittleEndianWriter oPacket, MapleCharacterLook mcl) {
        Map<Short, Integer> myEquip = new LinkedHashMap<>();
        Map<Short, Integer> maskedEquip = new LinkedHashMap<>();
        for (Short p : mcl.getEquips().keySet()) {
            int itemid = mcl.getEquips().get(p);
            short pos = (byte) (p * -1);
            if (pos < 100 && myEquip.get(pos) == null) {
                myEquip.put(pos, itemid);
            } else if (pos > 100 && pos != 111) { // don't ask. o.o
                pos -= 100;
                if (myEquip.get(pos) != null) {
                    maskedEquip.put(pos, myEquip.get(pos));
                }
                myEquip.put(pos, itemid);
            } else if (myEquip.get(pos) != null) {
                maskedEquip.put(pos, itemid);
            }
        }
        for (Entry<Short, Integer> entry : myEquip.entrySet()) {
            oPacket.write(entry.getKey());
            oPacket.writeInt(entry.getValue());
        }
        oPacket.write(0xFF);
        for (Entry<Short, Integer> entry : maskedEquip.entrySet()) {
            oPacket.write(entry.getKey());
            oPacket.writeInt(entry.getValue());
        }
        oPacket.write(0xFF);
        Integer cWeapon = mcl.getEquips().get((short) -111);
        oPacket.writeInt(cWeapon != null ? cWeapon : 0);// nWeaponStickerID
        for (int i = 0; i < 3; i++) { // anPetID
            oPacket.writeInt(0);
        }
    }

    public static void encodeCharacterInformation(final MaplePacketLittleEndianWriter oPacket, MapleCharacter player, boolean viewall) {
        encodeCharacterStat(oPacket, player); // GW_CharacterStat::Decode
        encodeAvatarLook(oPacket, player, false); // AvatarLook::Decode
        if (!viewall) {
            oPacket.write(0); // nCount
        }
        if (player.isGM()) {
            oPacket.write(0); // nCount
            return;
        }
        oPacket.writeBoolean(ServerConstants.ENABLE_WORLD_RANK); // world rank enabled (next 4 ints are not sent if disabled) Short??
        oPacket.writeInt(player.getRank()); // world rank
        oPacket.writeInt(player.getRankMove()); // move (negative is downwards)
        oPacket.writeInt(player.getJobRank()); // job rank
        oPacket.writeInt(player.getJobRankMove()); // move (negative is downwards)
    }

    private static void encodeQuest(final MaplePacketLittleEndianWriter oPacket, MapleCharacter player) {
        oPacket.writeShort(player.getStartedQuestsSize());
        for (MapleQuestStatus q : player.getStartedQuests()) {
            oPacket.writeShort(q.getQuest().getId());
            oPacket.writeMapleAsciiString(q.getQuestData());
            if (q.getQuest().startQuestData.infoNumber > 0) {
                oPacket.writeShort(q.getQuest().startQuestData.infoNumber);
                oPacket.writeMapleAsciiString(q.getQuestData());
            }
        }
    }

    private static void encodeCompletedQuest(final MaplePacketLittleEndianWriter oPacket, MapleCharacter player) {
        List<MapleQuestStatus> completed = player.getCompletedQuests();
        oPacket.writeShort(completed.size());
        for (MapleQuestStatus q : completed) {
            oPacket.writeShort(q.getQuest().getId());
            oPacket.writeLong(getTime(q.getCompletionTime()));
        }
    }

    public static void encodeItemSlotBase(final MaplePacketLittleEndianWriter oPacket, Item item) {
        encodeItemSlotBase(oPacket, item, false);
    }

    public static void encodeExpirationTime(final MaplePacketLittleEndianWriter oPacket, long time) {
        oPacket.writeLong(getTime(time));
    }

    public static void encodeItemSlotBase(final MaplePacketLittleEndianWriter oPacket, Item item, boolean zeroPosition) {
        encodeItemSlotBase(oPacket, item, zeroPosition, false);
    }

    public static void encodeItemSlotBase(final MaplePacketLittleEndianWriter oPacket, Item item, boolean zeroPosition, boolean addBundleAmount) {
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        ItemData itemData = ii.getItemData(item.getItemId());
        boolean isPet = item.getPetId() > -1;
        boolean isRing = false;
        Equip equip = null;
        short pos = item.getPosition();
        if (item.getType() == 1) {
            equip = (Equip) item;
            isRing = equip.getRingId() > -1;
        }
        if (!zeroPosition) {
            if (equip != null) {
                if (pos < 0) {
                    pos *= -1;
                }
                oPacket.writeShort(pos > 100 && pos < 999 ? pos - 100 : pos);
            } else {
                oPacket.write(pos);
            }
        }
        oPacket.write(item.getType());
        oPacket.writeInt(item.getItemId());
        oPacket.writeBoolean(itemData.isCash);
        if (itemData.isCash) {
            oPacket.writeLong(isPet ? item.getPetId() : isRing ? equip.getRingId() : item.getCashId());
        }
        encodeExpirationTime(oPacket, item.getExpiration());
        if (isPet) {
            MaplePet pet = item.getPet();
            oPacket.writeAsciiString(StringUtil.getRightPaddedStr(pet.getName(), '\0', 13));
            oPacket.write(pet.getLevel());
            oPacket.writeShort(pet.getCloseness());
            oPacket.write(pet.getFullness());
            encodeExpirationTime(oPacket, item.getExpiration());
            oPacket.writeShort(0);// nPetAttribute
            oPacket.writeShort(0);// usPetSkill
            oPacket.writeInt(pet.getRemainLife());// nRemainLife
            oPacket.writeShort(0);// nAttribute;
            // oPacket.EncodeByte(nActiveState);
            // oPacket.EncodeInt(nAutoBuffSkill);
            // oPacket.EncodeInt(nPetHue);
            // oPacket.EncodeShort(nGiantRate);
            return;
        }
        if (equip == null) {
            oPacket.writeShort(item.getQuantity() * (addBundleAmount ? item.getPerBundle() : 1));
            oPacket.writeMapleAsciiString(item.getOwner());
            oPacket.writeShort(item.getFlag()); // flag
            if (ItemConstants.isRechargable(item.getItemId())) {
                oPacket.writeLong(0);// liSN
            }
            return;
        }
        oPacket.write(equip.getUpgradeSlots()); // upgrade slots
        oPacket.write(equip.getLevel()); // level
        oPacket.writeShort(equip.getStr()); // str
        oPacket.writeShort(equip.getDex()); // dex
        oPacket.writeShort(equip.getInt()); // int
        oPacket.writeShort(equip.getLuk()); // luk
        oPacket.writeShort(equip.getHp()); // hp
        oPacket.writeShort(equip.getMp()); // mp
        oPacket.writeShort(equip.getWatk()); // watk
        oPacket.writeShort(equip.getMatk()); // matk
        oPacket.writeShort(equip.getWdef()); // wdef
        oPacket.writeShort(equip.getMdef()); // mdef
        oPacket.writeShort(equip.getAcc()); // accuracy
        oPacket.writeShort(equip.getAvoid()); // avoid
        oPacket.writeShort(equip.getHands()); // hands
        oPacket.writeShort(equip.getSpeed()); // speed
        oPacket.writeShort(equip.getJump()); // jump
        oPacket.writeMapleAsciiString(equip.getOwner()); // owner name
        oPacket.writeShort(equip.getFlag()); // Item Flags
        oPacket.writeBoolean(equip.hasLearnedSkills());// nLevelUpType, says the item can level your skill(For max level timeless/reverse)
        oPacket.write(equip.getItemLevel());
        oPacket.writeInt((int) equip.getItemExp());
        oPacket.writeInt(equip.getDurability());// nDurability
        oPacket.writeInt(equip.getVicious());// nIUC
        oPacket.write(equip.getGrade());// nGrade
        oPacket.write(equip.getChuc());// nCHUC
        oPacket.writeShort(equip.getOption1());// nOption1
        oPacket.writeShort(equip.getOption2());// nOption2
        oPacket.writeShort(equip.getOption3());// nOption3
        oPacket.writeShort(0);// nSocket1
        oPacket.writeShort(0);// nSocket2
        if (!itemData.isCash) {
            oPacket.writeLong(0);// liSN
        }
        oPacket.writeLong(getTime(-2));// ftEquipped
        oPacket.writeInt(-1);// nPrevBonusExpRate
    }

    private static void encodeInventory(long mask, final MaplePacketLittleEndianWriter oPacket, MapleCharacter player) {
        if ((mask & DBChar.InventorySize) > 0) {
            for (byte i = 1; i <= 5; i++) {
                oPacket.write(player.getInventory(MapleInventoryType.getByType(i)).getSlotLimit());
            }
        }
        if ((mask & DBChar.EquipExt) > 0) {
            final MapleQuestStatus stat = player.getQuest(MapleQuest.getInstance(122700));
            if (stat != null && stat.getCustomData() != null && Long.parseLong(stat.getCustomData()) > System.currentTimeMillis()) {
                oPacket.writeLong(getTime(Long.parseLong(stat.getCustomData())));
            } else {
                oPacket.writeLong(getTime(-2));
            }
        }
        
        MapleInventory iv = player.getInventory(MapleInventoryType.EQUIPPED);
        Collection<Item> equippedC = iv.list();
        List<Item> equipped = new ArrayList<>(equippedC.size());
        List<Item> equippedCash = new ArrayList<>(equippedC.size());
        for (Item item : equippedC) {
            if (item.getPosition() <= -100 && item.getPosition() >= -999) {
                equippedCash.add(item);
            } else {
                equipped.add(item);
            }
        }
        Collections.sort(equipped);        
        if ((mask & DBChar.ItemSlotEquip) > 0) {
            for (Item item : equipped) {
                if (item.getPosition() > EquipSlot.DP_BEGIN.getSlots()[0] || item.getPosition() < EquipSlot.DP_END.getSlots()[0]) {
                    encodeItemSlotBase(oPacket, item);
                }
            }
        }
        if ((mask & DBChar.ItemSlotEquip) > 0) {
            oPacket.writeShort(0); // start of equip cash
            for (Item item : equippedCash) {
                encodeItemSlotBase(oPacket, item);
            }
        }
        if ((mask & DBChar.ItemSlotEquip) > 0) {
            oPacket.writeShort(0); // start of equip inventory
            for (Item item : player.getInventory(MapleInventoryType.EQUIP).list()) {
                encodeItemSlotBase(oPacket, item);
            }
        }
        if ((mask & DBChar.ItemSlotEquip) > 0) {
            oPacket.writeShort(0);// evan inv
            for (Item item : equipped) {
                if (item.getPosition() <= -1000 && item.getPosition() > -1100) {
                    encodeItemSlotBase(oPacket, item);
                }
            }
        }
        if ((mask & DBChar.ItemSlotEquip) > 0) {
            oPacket.writeShort(0);
            for (Item item : equipped) {
                if (item.getPosition() <= -1100 && item.getPosition() > -1200) {
                    encodeItemSlotBase(oPacket, item);
                }
            }
        }
        if ((mask & DBChar.ItemSlotConsume) > 0) {
            oPacket.writeShort(0);
            for (Item item : player.getInventory(MapleInventoryType.USE).list()) {
                encodeItemSlotBase(oPacket, item);
            }
        }
        if ((mask & DBChar.ItemSlotInstall) > 0) {
            oPacket.write(0);
            for (Item item : player.getInventory(MapleInventoryType.SETUP).list()) {
                encodeItemSlotBase(oPacket, item);
            }
        }
        if ((mask & DBChar.ItemSlotEtc) > 0) {
            oPacket.write(0);
            for (Item item : player.getInventory(MapleInventoryType.ETC).list()) {
                encodeItemSlotBase(oPacket, item);
            }
        }
        if ((mask & DBChar.ItemSlotCash) > 0) {
            oPacket.write(0);
            for (Item item : player.getInventory(MapleInventoryType.CASH).list()) {
                encodeItemSlotBase(oPacket, item);
            }
        }
        oPacket.write(0); // end of inv
    }

    private static void encodeSkill(final MaplePacketLittleEndianWriter oPacket, MapleCharacter player) {
        Map<Skill, MapleCharacter.SkillEntry> skills = player.getSkills();
        int skillsSize = skills.size();
        for (Entry<Skill, MapleCharacter.SkillEntry> skill : skills.entrySet()) {
            if (GameConstants.isHiddenSkills(skill.getKey().getId())) {
                skillsSize--;
            }
        }
        oPacket.writeShort(skillsSize);
        for (Entry<Skill, MapleCharacter.SkillEntry> skill : skills.entrySet()) {
            if (GameConstants.isHiddenSkills(skill.getKey().getId())) {
                continue;
            }
            oPacket.writeInt(skill.getKey().getId());
            oPacket.writeInt(skill.getValue().skillevel);
            encodeExpirationTime(oPacket, skill.getValue().expiration);
            if (GameConstants.is_skill_need_master_level(skill.getKey().getId())) {
                oPacket.writeInt(skill.getValue().masterlevel);
            }
        }
    }

    private static void encodeSkillCooldown(final MaplePacketLittleEndianWriter oPacket, MapleCharacter player) {
        oPacket.writeShort(player.getAllCooldowns().size());
        for (PlayerCoolDownValueHolder cooling : player.getAllCooldowns()) {
            oPacket.writeInt(cooling.skillId);
            int timeLeft = (int) (cooling.length + cooling.startTime - System.currentTimeMillis());
            oPacket.writeShort(timeLeft / 1000);
        }
    }

    private static void encodeMonsterBook(final MaplePacketLittleEndianWriter oPacket, MapleCharacter player) {
        oPacket.write(0);
        Map<Integer, Integer> cards = player.getMonsterBook().getCards();
        oPacket.writeShort(cards.size());
        for (Entry<Integer, Integer> all : cards.entrySet()) {
            oPacket.writeShort(all.getKey() % 10000); // Id
            oPacket.write(all.getValue()); // Level
        }
    }

    public static void encodeRingLook(final MaplePacketLittleEndianWriter oPacket, List<MapleRing> rings) {
        Optional<MapleRing> ring = rings.stream().filter(Objects::nonNull).filter(r -> r.equipped()).findAny();
        if (ring.isPresent()) {
            oPacket.writeBoolean(true);
            MapleRing r = ring.get();
            oPacket.writeLong(r.getRingId());
            oPacket.writeLong(r.getPartnerRingId());
            oPacket.writeInt(r.getItemId());
        } else {
            oPacket.writeBoolean(false);
        }
    }

    public static void encodeMarriageRingLook(final MaplePacketLittleEndianWriter oPacket, MapleCharacter player) {
        MapleRing ring = player.getMarriageRing();
        if (ring != null) {
            oPacket.writeBoolean(ring.equipped());
            if (ring.equipped()) {
                oPacket.writeInt(player.getId());
                oPacket.writeInt(player.getMarriedTo());
                oPacket.writeInt(player.getMarriageRingID());
            }
        } else {
            oPacket.writeBoolean(false);
        }
    }

    public static void encodeFlag(final LittleEndianWriter oPacket, List<Pair<SecondaryStat, BuffDataHolder>> statUps) {
        int[] uFlagTemp = new int[MaxBuffStat];
        for (Pair<SecondaryStat, BuffDataHolder> statUp : statUps) {
            uFlagTemp[statUp.left.getSet()] |= statUp.left.getMask();
        }
        for (int i = 3; i >= 0; i--) {
            oPacket.writeInt(uFlagTemp[i]);
        }
    }

    public static void encodeFlag(final LittleEndianWriter oPacket, int[] uFlagTemp, List<Pair<SecondaryStat, BuffDataHolder>> statUps) {
        for (Pair<SecondaryStat, BuffDataHolder> statUp : statUps) {
            uFlagTemp[statUp.left.getSet()] |= statUp.left.getMask();
        }
        for (int i = 3; i >= 0; i--) {
            oPacket.writeInt(uFlagTemp[i]);
        }
    }

    public static void encodeFlagFromList(final MaplePacketLittleEndianWriter oPacket, List<SecondaryStat> statups) {
        int[] mask = new int[MaxBuffStat];
        for (SecondaryStat statup : statups) {
            mask[statup.getSet()] |= statup.getMask();
        }
        for (int i = 3; i >= 0; i--) {
            oPacket.writeInt(mask[i]);
        }
    }

    public static void addThread(final MaplePacketLittleEndianWriter oPacket, ResultSet rs) throws SQLException {
        oPacket.writeInt(rs.getInt("localthreadid"));
        oPacket.writeInt(rs.getInt("postercid"));
        oPacket.writeMapleAsciiString(rs.getString("name"));
        oPacket.writeLong(getTime(rs.getLong("timestamp")));
        oPacket.writeInt(rs.getInt("icon"));
        oPacket.writeInt(rs.getInt("replycount"));
    }

    public static byte[] showForcedEquip(int team) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.FORCED_MAP_EQUIP.getValue());
        if (team > -1) {
            oPacket.write(team); // 00 = red, 01 = blue
        }
        return oPacket.getPacket();
    }

    public static byte[] skillCooldown(int sid, int time) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.COOLDOWN.getValue());
        oPacket.writeInt(sid);
        oPacket.writeShort(time);// Int in v97
        return oPacket.getPacket();
    }

    public static byte[] getMiniGame(MapleClient c, MapleMiniGame minigame, boolean owner, int piece) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        oPacket.write(PlayerInteractionHandler.Action.ROOM.getCode());
        oPacket.write(1);// nMiniRoomType
        oPacket.write(0);// nMaxUsers
        oPacket.write(owner ? 0 : 1);// nMyPosition
        oPacket.write(0);// for ( i = CInPacket::Decode1(iPacket); i >= 0; i = CInPacket::Decode1(iPacket) )
        encodeAvatarLook(oPacket, minigame.getOwner(), false);
        oPacket.writeMapleAsciiString(minigame.getOwner().getName());
        // job id short in later version
        if (minigame.getVisitor() != null) {
            MapleCharacter visitor = minigame.getVisitor();
            oPacket.write(1);// slot id
            encodeAvatarLook(oPacket, visitor, false);
            oPacket.writeMapleAsciiString(visitor.getName());
            // job id short in later version
        }
        oPacket.write(0xFF);
        oPacket.write(0);// slot of the person below.(owner is always 0)
        oPacket.writeInt(1);// game type
        oPacket.writeInt(minigame.getOwner().getMiniGamePoints("wins", true));
        oPacket.writeInt(minigame.getOwner().getMiniGamePoints("ties", true));
        oPacket.writeInt(minigame.getOwner().getMiniGamePoints("losses", true));
        oPacket.writeInt(2000);// score
        if (minigame.getVisitor() != null) {
            MapleCharacter visitor = minigame.getVisitor();
            oPacket.write(1);// slot of the person below(omok only has 1 other player)
            oPacket.writeInt(1);// game type
            oPacket.writeInt(visitor.getMiniGamePoints("wins", true));
            oPacket.writeInt(visitor.getMiniGamePoints("ties", true));
            oPacket.writeInt(visitor.getMiniGamePoints("losses", true));
            oPacket.writeInt(2000);// score
        }
        oPacket.write(0xFF);
        oPacket.writeMapleAsciiString(minigame.getDescription());
        oPacket.writeShort(piece);
        return oPacket.getPacket();
    }

    public static byte[] getMiniGameReady(MapleMiniGame game) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        oPacket.write(PlayerInteractionHandler.Action.READY.getCode());
        return oPacket.getPacket();
    }

    public static byte[] getMiniGameUnReady(MapleMiniGame game) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        oPacket.write(PlayerInteractionHandler.Action.UN_READY.getCode());
        return oPacket.getPacket();
    }

    public static byte[] getMiniGameStart(MapleMiniGame game, int loser) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        oPacket.write(PlayerInteractionHandler.Action.START.getCode());
        oPacket.write(loser);
        return oPacket.getPacket();
    }

    public static byte[] getMiniGameSkipOwner(MapleMiniGame game) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        oPacket.write(PlayerInteractionHandler.Action.SKIP.getCode());
        oPacket.write(0x01);
        return oPacket.getPacket();
    }

    public static byte[] getMiniGameRequestTie(MapleMiniGame game) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        oPacket.write(PlayerInteractionHandler.Action.REQUEST_TIE.getCode());
        return oPacket.getPacket();
    }

    public static byte[] getMiniGameDenyTie(MapleMiniGame game) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        oPacket.write(PlayerInteractionHandler.Action.ANSWER_TIE.getCode());
        return oPacket.getPacket();
    }

    public static byte[] getMiniGameFull() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        oPacket.write(PlayerInteractionHandler.Action.ROOM.getCode());
        oPacket.write(0);
        oPacket.write(2);
        return oPacket.getPacket();
    }

    public static byte[] getMiniGamePassIncorrect() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        oPacket.write(PlayerInteractionHandler.Action.ROOM.getCode());
        oPacket.write(0);
        oPacket.write(28);
        return oPacket.getPacket();
    }

    public static byte[] getMiniGameSkipVisitor(MapleMiniGame game) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        oPacket.writeShort(PlayerInteractionHandler.Action.SKIP.getCode());
        oPacket.write(0x00);
        return oPacket.getPacket();
    }

    public static byte[] getMiniGameMoveOmok(MapleMiniGame game, int move1, int move2, int move3) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        oPacket.write(PlayerInteractionHandler.Action.MOVE_OMOK.getCode());
        oPacket.writeInt(move1);
        oPacket.writeInt(move2);
        oPacket.write(move3);
        return oPacket.getPacket();
    }

    public static byte[] getMiniGameNewVisitor(MapleCharacter c, int slot) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        oPacket.write(PlayerInteractionHandler.Action.VISIT.getCode());
        oPacket.write(slot);
        encodeAvatarLook(oPacket, c, false);
        oPacket.writeMapleAsciiString(c.getName());
        oPacket.writeInt(1);
        oPacket.writeInt(c.getMiniGamePoints("wins", true));
        oPacket.writeInt(c.getMiniGamePoints("ties", true));
        oPacket.writeInt(c.getMiniGamePoints("losses", true));
        oPacket.writeInt(2000);
        return oPacket.getPacket();
    }

    public static byte[] getMiniGameRemoveVisitor() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        oPacket.write(PlayerInteractionHandler.Action.EXIT.getCode());
        oPacket.write(1);
        return oPacket.getPacket();
    }

    private static byte[] getMiniGameResult(MapleMiniGame game, int win, int lose, int tie, int result, int forfeit, boolean omok) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        oPacket.write(PlayerInteractionHandler.Action.GET_RESULT.getCode());
        if (tie == 0 && forfeit != 1) {
            oPacket.write(0);
        } else if (tie == 1) {
            oPacket.write(1);
        } else if (forfeit == 1) {
            oPacket.write(2);
        }
        oPacket.write(result - 1); // winner
        oPacket.writeInt(1); // unknown
        oPacket.writeInt(game.getOwner().getMiniGamePoints("wins", omok) + win); // wins
        oPacket.writeInt(game.getOwner().getMiniGamePoints("ties", omok) + tie); // ties
        oPacket.writeInt(game.getOwner().getMiniGamePoints("losses", omok) + lose); // losses
        oPacket.writeInt(2000); // points
        oPacket.writeInt(1); // start of visitor; unknown
        oPacket.writeInt(game.getVisitor().getMiniGamePoints("wins", omok) + lose); // wins
        oPacket.writeInt(game.getVisitor().getMiniGamePoints("ties", omok) + tie); // ties
        oPacket.writeInt(game.getVisitor().getMiniGamePoints("losses", omok) + win); // losses
        oPacket.writeInt(2000); // points
        game.getOwner().setMiniGamePoints(game.getVisitor(), result, omok);
        return oPacket.getPacket();
    }

    public static byte[] getMiniGameOwnerWin(MapleMiniGame game) {
        return getMiniGameResult(game, 1, 0, 0, 1, 0, true);
    }

    public static byte[] getMiniGameVisitorWin(MapleMiniGame game) {
        return getMiniGameResult(game, 0, 1, 0, 2, 0, true);
    }

    public static byte[] getMiniGameTie(MapleMiniGame game) {
        return getMiniGameResult(game, 0, 0, 1, 3, 0, true);
    }

    public static byte[] getMiniGameOwnerForfeit(MapleMiniGame game) {
        return getMiniGameResult(game, 0, 1, 0, 2, 1, true);
    }

    public static byte[] getMiniGameVisitorForfeit(MapleMiniGame game) {
        return getMiniGameResult(game, 1, 0, 0, 1, 1, true);
    }

    public static byte[] getMiniGameClose() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        oPacket.write(PlayerInteractionHandler.Action.EXIT.getCode());
        oPacket.write(1);
        oPacket.write(3);
        return oPacket.getPacket();
    }

    public static byte[] getMatchCard(MapleClient c, MapleMiniGame minigame, boolean owner, int piece) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        oPacket.write(PlayerInteractionHandler.Action.ROOM.getCode());
        oPacket.write(2);
        oPacket.write(2);
        oPacket.write(owner ? 0 : 1);
        oPacket.write(0);
        encodeAvatarLook(oPacket, minigame.getOwner(), false);
        oPacket.writeMapleAsciiString(minigame.getOwner().getName());
        if (minigame.getVisitor() != null) {
            MapleCharacter visitor = minigame.getVisitor();
            oPacket.write(1);
            encodeAvatarLook(oPacket, visitor, false);
            oPacket.writeMapleAsciiString(visitor.getName());
        }
        oPacket.write(0xFF);
        oPacket.write(0);
        oPacket.writeInt(2);
        oPacket.writeInt(minigame.getOwner().getMiniGamePoints("wins", false));
        oPacket.writeInt(minigame.getOwner().getMiniGamePoints("ties", false));
        oPacket.writeInt(minigame.getOwner().getMiniGamePoints("losses", false));
        oPacket.writeInt(2000);
        if (minigame.getVisitor() != null) {
            MapleCharacter visitor = minigame.getVisitor();
            oPacket.write(1);
            oPacket.writeInt(2);
            oPacket.writeInt(visitor.getMiniGamePoints("wins", false));
            oPacket.writeInt(visitor.getMiniGamePoints("ties", false));
            oPacket.writeInt(visitor.getMiniGamePoints("losses", false));
            oPacket.writeInt(2000);
        }
        oPacket.write(0xFF);
        oPacket.writeMapleAsciiString(minigame.getDescription());
        oPacket.write(piece);
        oPacket.write(0);
        return oPacket.getPacket();
    }

    public static byte[] getMatchCardStart(MapleMiniGame game, int loser) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        oPacket.write(PlayerInteractionHandler.Action.START.getCode());
        oPacket.write(loser);
        int last = 13;
        if (game.getMatchesToWin() > 10) {
            last = 31;
        } else if (game.getMatchesToWin() > 6) {
            last = 21;
        }
        oPacket.write(last - 1);
        for (int i = 1; i < last; i++) {
            oPacket.writeInt(game.getCardId(i));
        }
        return oPacket.getPacket();
    }

    public static byte[] getMatchCardNewVisitor(MapleCharacter c, int slot) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        oPacket.write(PlayerInteractionHandler.Action.VISIT.getCode());
        oPacket.write(slot);
        encodeAvatarLook(oPacket, c, false);
        oPacket.writeMapleAsciiString(c.getName());
        oPacket.writeInt(1);
        oPacket.writeInt(c.getMiniGamePoints("wins", false));
        oPacket.writeInt(c.getMiniGamePoints("ties", false));
        oPacket.writeInt(c.getMiniGamePoints("losses", false));
        oPacket.writeInt(2000);
        return oPacket.getPacket();
    }

    public static byte[] getMatchCardSelect(MapleMiniGame game, int turn, int slot, int firstslot, int type) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        oPacket.write(PlayerInteractionHandler.Action.SELECT_CARD.getCode());
        oPacket.write(turn);
        if (turn == 1) {
            oPacket.write(slot);
        } else if (turn == 0) {
            oPacket.write(slot);
            oPacket.write(firstslot);
            oPacket.write(type);
        }
        return oPacket.getPacket();
    }

    public static byte[] getMatchCardOwnerWin(MapleMiniGame game) {
        return getMiniGameResult(game, 1, 0, 0, 1, 0, false);
    }

    public static byte[] getMatchCardVisitorWin(MapleMiniGame game) {
        return getMiniGameResult(game, 0, 1, 0, 2, 0, false);
    }

    public static byte[] getMatchCardTie(MapleMiniGame game) {
        return getMiniGameResult(game, 0, 0, 1, 3, 0, false);
    }

    public static byte[] enableCSUse() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.write(0x12);
        oPacket.skip(6);
        return oPacket.getPacket();
    }

    public static String getRightPaddedStr(String in, char padchar, int length) {
        StringBuilder builder = new StringBuilder(in);
        for (int x = in.length(); x < length; x++) {
            builder.append(padchar);
        }
        return builder.toString();
    }

    public static byte[] shopErrorMessage(int error, int type) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        oPacket.write(0x0A);
        oPacket.write(type);
        oPacket.write(error);
        return oPacket.getPacket();
    }

    private static void encodeRing(final MaplePacketLittleEndianWriter oPacket, MapleCharacter player) {
        oPacket.writeShort(player.getCrushRings().size());
        for (MapleRing ring : player.getCrushRings()) {
            oPacket.writeInt(ring.getPartnerChrId());
            oPacket.writeAsciiString(getRightPaddedStr(ring.getPartnerName(), '\0', 13));
            oPacket.writeInt(ring.getRingId());
            oPacket.writeInt(0);
            oPacket.writeInt(ring.getPartnerRingId());
            oPacket.writeInt(0);
        }
        oPacket.writeShort(player.getFriendshipRings().size());
        for (MapleRing ring : player.getFriendshipRings()) {
            oPacket.writeInt(ring.getPartnerChrId());
            oPacket.writeAsciiString(getRightPaddedStr(ring.getPartnerName(), '\0', 13));
            oPacket.writeInt(ring.getRingId());
            oPacket.writeInt(0);
            oPacket.writeInt(ring.getPartnerRingId());
            oPacket.writeInt(0);
            oPacket.writeInt(ring.getItemId());
        }
        boolean ring = player.getMarriedTo() > 0 && player.getMarriageRingID() > 0;
        oPacket.writeShort(ring ? 1 : 0);// actually a loop like the rest.
        if (ring) {
            oPacket.writeInt(player.getMarriageID());
            oPacket.writeInt(player.getGender() == 0 ? player.getId() : player.getMarriedTo());
            oPacket.writeInt(player.getGender() == 0 ? player.getMarriedTo() : player.getId());
            oPacket.writeShort(player.getMarriedTo() > 0 ? 3 : 1);
            oPacket.writeInt(player.getMarriageRingID());
            oPacket.writeInt(player.getMarriageRingID());
            oPacket.writeAsciiString(StringUtil.getRightPaddedStr(player.getGender() == 0 ? player.getName() : MapleCharacter.getNameById(player.getMarriedTo()), '\0', 13));
            oPacket.writeAsciiString(StringUtil.getRightPaddedStr(player.getGender() == 0 ? MapleCharacter.getNameById(player.getMarriedTo()) : player.getName(), '\0', 13));
        }
    }

    public static byte[] sheepRanchInfo(byte wolf, byte sheep) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SHEEP_RANCH_INFO.getValue());
        oPacket.write(wolf);
        oPacket.write(sheep);
        return oPacket.getPacket();
    }

    public static byte[] sheepRanchClothes(int id, byte clothes) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SHEEP_RANCH_CLOTHES.getValue());
        oPacket.writeInt(id); // Character id
        oPacket.write(clothes); // 0 = sheep, 1 = wolf, 2 = Spectator (wolf without wool)
        return oPacket.getPacket();
    }
}
