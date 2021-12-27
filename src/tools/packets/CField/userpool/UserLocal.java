package tools.packets.CField.userpool;

import java.util.List;
import net.SendOpcode;
import server.movement.MovePath;
import tools.Pair;
import tools.data.output.MaplePacketLittleEndianWriter;

public class UserLocal {

    // Hiển thị trạng thái ngồi của nhân vật
    public static byte[] SitResult(int id) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CANCEL_CHAIR.getValue());
        if (id == -1) {
            oPacket.write(0);
        } else {
            oPacket.write(1);
            oPacket.writeShort(id);
        }
        return oPacket.getPacket();
    }

    // Hiển thị trạng thái khuôn mặt chế độ mình tôi
    public static byte[] Emotion(int dwCharacterID, int nEmotion, boolean bEmotionByItemOption) {
        return UserRemote.Emotion(dwCharacterID, nEmotion, bEmotionByItemOption);
    }

    public static byte[] Teleport(byte portalId, int mode) { // Teleport to Portal
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.TELEPORT_LOCAL.getValue());
        oPacket.write(mode); // enableActions
        oPacket.write(portalId);
        return oPacket.getPacket();
    }

    // Thông báo số tiền khi mở Meso Bag/Random Meso Bag
    public static byte[] MesoGive_Succeeded(int amount) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MESOGIVE_SUCCEEDED.getValue());
        oPacket.writeInt(amount);
        return oPacket.getPacket();
    }

    public static byte[] MesoGive_Failed() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MESOGIVE_FAILED.getValue());
        return oPacket.getPacket();
    }

    public static byte[] RandomMesobag_Succeeded(byte nRank, int amount) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MESO_BAG_SUCCEEDED.getValue());
        oPacket.write(nRank);
        oPacket.write(amount);
        return oPacket.getPacket();
    }

    public static byte[] RandomMesobag_Failed() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MESO_BAG_FAILED.getValue());
        return oPacket.getPacket();
    }

    // Chuyển cảnh phục vụ cho các Intro hoặc quest đặc biệt
    public static byte[] FieldFadeInOut(int tFadeIn, int tFadeOut, int tDelay, int nAlpha) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.FIELD_FADE_INOUT.getValue());
        oPacket.writeInt(tFadeIn);
        oPacket.writeInt(tDelay);
        oPacket.writeInt(tFadeOut);
        oPacket.writeInt(nAlpha);
        return oPacket.getPacket();
    }

    public static byte[] FieldFadeInOutForce(int tFadeOut) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.FIELD_FADE_OUT_FORCE.getValue());
        oPacket.writeInt(tFadeOut);
        return oPacket.getPacket();
    }

    public static class QuestResult {

        public static byte[] addQuestTimer(final short usQuestID, final int tRemain) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
            oPacket.write(6);
            oPacket.writeShort(1); // Không có ai làm hai nhiệm vụ mà cần gia hạn thời gian cả hai đâu.
            oPacket.writeShort(usQuestID);
            oPacket.writeInt(tRemain);
            // AddQuestTimer
            return oPacket.getPacket();
        }

        public static byte[] removeQuestTimer(final short usQuestID) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
            oPacket.write(7);
            oPacket.writeShort(1); // Không có ai làm hai nhiệm vụ mà cần gia hạn thời gian cả hai đâu.
            oPacket.writeShort(usQuestID); // bTimeKeepQuestTimer = 0
            // RemoveQuestTimer
            return oPacket.getPacket();
        }

        public static byte[] updateQuestInfo(short usQuestID, int npc) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
            oPacket.write(8);
            oPacket.writeShort(usQuestID);
            oPacket.writeInt(npc);
            return oPacket.getPacket();
        }

        public static byte[] updateQuestTimer(final short usQuestID) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
            oPacket.write(9);
            oPacket.writeShort(1); // Không có ai làm hai nhiệm vụ mà cần gia hạn thời gian cả hai đâu.
            oPacket.writeShort(usQuestID); // bTimeKeepQuestTimer = 0
            // RemoveQuestTimer
            return oPacket.getPacket();
        }

        public static byte[] updateQuestFinish(short quest, int npc, short nextquest) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
            oPacket.write(0xA);
            oPacket.writeShort(quest);
            oPacket.writeInt(npc);
            oPacket.writeShort(nextquest);
            return oPacket.getPacket();
        }

        // The [%s] quest expired because the time limit ended
        public static byte[] questExpired(short usQuestID) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
            oPacket.write(0x11);
            oPacket.writeShort(usQuestID);
            return oPacket.getPacket();
        }

        public static byte[] resetQuestTimer(short usQuestID) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
            oPacket.write(0x12);
            oPacket.writeShort(usQuestID);
            // ResetQuestTimer
            return oPacket.getPacket();
        }

        public static byte[] requiredQuestItem(short nJobCategory) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
            oPacket.write(0xC);
            oPacket.writeShort(nJobCategory);
            return oPacket.getPacket();
        }

        /**
         * 0xD = "You do not have enough mesos." 0xF = "Unable to retrieve it
         * due to the equipment\r\n currently being worn by the character." 0xB
         * = "The quest has ended\r\ndue to an unknown error."; 0x10 = "You may
         * not possess more than \r\none of this item."
         *
         * @param type
         *
         * @return
         */
        public static byte[] questFailure(byte type) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
            oPacket.write(type);
            return oPacket.getPacket();
        }
    }

    public static byte[] NotifyHPDecByField(int nDamage) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.NOTIFY_HPDEC_BYFIELD.getValue());
        oPacket.writeInt(nDamage);
        return oPacket.getPacket();
    }

    /**
     * Gửi người chơi gợi ý
     *
     * @param hint The hint it's going to send.
     * @param width How tall the box is going to be.
     * @param height How long the box is going to be.
     *
     * @return The player hint packet.
     */
    public static byte[] BalloonMsg(String hint, int width, int height) {
        if (width < 1) {
            width = hint.length() * 10;
            if (width < 40) {
                width = 40;
            }
        }
        if (height < 5) {
            height = 5;
        }
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PLAYER_HINT.getValue());
        oPacket.writeMapleAsciiString(hint);
        oPacket.writeShort(width);
        oPacket.writeShort(height);
        oPacket.write(1); // boolean false = Decode 2 ints
        return oPacket.getPacket();
    }

    // Mở nhạc sự kiện
    public static byte[] PlayEventSound(String SoundName) { // Sound/Field.img/%s
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PLAY_EVENT_SOUND.getValue());
        oPacket.writeMapleAsciiString(SoundName);
        return oPacket.getPacket();
    }

    // Mở nhạc minigame
    public static byte[] PlayMinigameSound(String SoundName) { // Sound/MiniGame.img/
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PLAY_EVENT_SOUND.getValue());
        oPacket.writeMapleAsciiString(SoundName);
        return oPacket.getPacket();
    }

    public static byte[] makerResult(boolean success, int itemMade, int itemCount, int mesos, List<Pair<Integer, Integer>> itemsLost, boolean catalyst, int catalystID, int amtINCBuffGem, List<Integer> gems) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MAKER_RESULT.getValue());
        oPacket.writeInt(success ? 0 : 1); // 0 = success, 1 = fail
        oPacket.writeInt(1); // 1 or 2 doesn't matter, same methods
        oPacket.writeBoolean(!success);
        if (success) {
            oPacket.writeInt(itemMade);
            oPacket.writeInt(itemCount);
        }
        oPacket.writeInt(itemsLost.size()); // Loop
        for (Pair<Integer, Integer> item : itemsLost) {
            oPacket.writeInt(item.getLeft());
            oPacket.writeInt(item.getRight());
        }
        oPacket.writeInt(amtINCBuffGem);
        for (int i = 0; i < amtINCBuffGem; i++) {
            oPacket.writeInt(gems.get(i));
        }
        oPacket.write(catalyst ? 1 : 0); // stimulator
        if (catalyst) {
            oPacket.writeInt(catalystID);
        }
        oPacket.writeInt(mesos);
        return oPacket.getPacket();
    }

    public static byte[] makerResultCrystal(int itemIdGained, int itemIdLost) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MAKER_RESULT.getValue());
        oPacket.writeInt(0); // Always successful!
        oPacket.writeInt(3); // Monster Crystal
        oPacket.writeInt(itemIdGained);
        oPacket.writeInt(itemIdLost);
        return oPacket.getPacket();
    }

    public static byte[] makerResultDesynth(int itemId, int mesos, List<Pair<Integer, Integer>> itemsGained) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MAKER_RESULT.getValue());
        oPacket.writeInt(0); // Always successful!
        oPacket.writeInt(4); // Mode Desynth
        oPacket.writeInt(itemId); // Item desynthed
        oPacket.writeInt(itemsGained.size()); // Loop of items gained, (int, int)
        for (Pair<Integer, Integer> item : itemsGained) {
            oPacket.writeInt(item.getLeft());
            oPacket.writeInt(item.getRight());
        }
        oPacket.writeInt(mesos); // Mesos spent.
        return oPacket.getPacket();
    }

    public static byte[] OpenClassCompetitionPage() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.OPEN_CLASS_COMPETITION_PAGE.getValue());
        return oPacket.getPacket();
    }

    /**
     * Mở UI tiện ích 0x01 - Equipment Inventory. 0x02 - Stat Window. 0x03 -
     * Skill Window. 0x05 - Keyboard Settings. 0x06 - Quest window. 0x09 -
     * Monsterbook Window. 0x0A - Char Info 0x0B - Guild BBS 0x12 - Monster
     * Carnival Window 0x16 - Party Search. 0x17 - Item Creation Window. 0x1A -
     * My Ranking O.O 0x1B - Family Window 0x1C - Family Pedigree 0x1D - GM
     * Story Board /funny shet 0x1E - Envelop saying you got mail from an admin
     * 0x1F - Medal Window 0x20 - Maple Event (???) 0x21 - Invalid Pointer Crash
     *
     * @param ui
     *
     * @return
     */
    public static byte[] openUI(byte nUIType) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter(3);
        oPacket.writeShort(SendOpcode.OPEN_UI.getValue());
        oPacket.write(nUIType);
        return oPacket.getPacket();
    }

    /**
     * Mở UI tiện ích theo lựa chọn. 7 - UI_Toggle 21 - Request Party Adver
     * Search UI 33 - Character Repair Durability Dlg UI
     *
     * @param nUIType
     * @param nDefaultTab
     *
     * @return
     */
    public static byte[] openUIWithOption(int nUIType, int nDefaultTab) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter(3);
        oPacket.writeShort(SendOpcode.OPEN_UI_WITH_OPTION.getValue());
        oPacket.writeInt(nUIType);
        oPacket.writeInt(nDefaultTab);
        return oPacket.getPacket();
    }

    /**
     * Khoá màn hình chuyển sang chế độ di chuyển, dùng cho INTRO
     *
     * @param bSet
     *
     * @return
     */
    public static byte[] SetDirectionMode(boolean bSet) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter(3);
        oPacket.writeShort(SendOpcode.LOCK_UI.getValue());
        oPacket.write(bSet ? 1 : 0);
        oPacket.writeInt(0);
        return oPacket.getPacket();
    }

    // Khoá màn hình chuyển sang chế độ xem phim
    public static byte[] SetStandAloneMode(boolean bSet) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.DISABLE_UI.getValue());
        oPacket.write(bSet ? 1 : 0);
        return oPacket.getPacket();
    }

    public static byte[] HireTutor(boolean spawn) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter(3);
        oPacket.writeShort(SendOpcode.SPAWN_GUIDE.getValue());
        oPacket.writeBoolean(spawn);
        return oPacket.getPacket();
    }

    public static byte[] talkGuides(String talk) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.TALK_GUIDE.getValue());
        oPacket.write(0);
        oPacket.writeMapleAsciiString(talk);
        oPacket.write(new byte[]{(byte) 0xC8, 0, 0, 0, (byte) 0xA0, (byte) 0x0F, 0, 0});
        return oPacket.getPacket();
    }

    public static byte[] TutorMsg(int nIdx, int nDuration, String sMsg, boolean bSet) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.TALK_GUIDE.getValue());
        oPacket.writeBoolean(bSet);
        if (bSet) {
            oPacket.writeInt(nIdx);
            oPacket.writeInt(nDuration);
        } else {
            oPacket.writeMapleAsciiString(sMsg);
            oPacket.writeInt(nIdx);
            oPacket.writeInt(nDuration);
        }
        return oPacket.getPacket();
    }

    public static byte[] IncComboResponse(int nCombo) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SHOW_COMBO.getValue());
        oPacket.writeInt(nCombo);
        return oPacket.getPacket();
    }

    // Dùng cho SendConsumeCashItemUseRequest (Handling)
    public static byte[] RandomEmotion(int nItemID) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.RANDOM_EMOTION.getValue());
        oPacket.writeInt(nItemID);
        return oPacket.getPacket();
    }

    // Dùng cho ResignQuest(119) (Handling)
    public static byte[] ResignQuestReturn(short usQuestID) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.RESIGN_QUEST_RETURN.getValue());
        oPacket.writeShort(usQuestID);
        return oPacket.getPacket();
    }

    public static byte[] PassMateName(short nQuestID, String sMateName) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PASS_MATE_NAME.getValue());
        oPacket.writeShort(nQuestID);
        oPacket.writeMapleAsciiString(sMateName); // SetQuestMateName
        return oPacket.getPacket();
    }

    public static byte[] RadioSchedule(String sTrack, int tPassSec) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.RADIO_SCHEDULE.getValue());
        oPacket.writeMapleAsciiString(sTrack);
        oPacket.writeInt(tPassSec);
        return oPacket.getPacket();
    }

    public static byte[] OpenSkillGuide(String sMateName, int tPassSec) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.OPEN_SKILL_GUIDE.getValue());
        return oPacket.getPacket();
    }

    public static byte[] NoticeMsg(String sMsg) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.NOTICE_MSG.getValue());
        oPacket.writeMapleAsciiString(sMsg);
        return oPacket.getPacket();
    }

    /**
     * 0: Normal Chat 1: Whisper 2: Party 3: Buddy 4: Guild 5: Alliance 6:
     * Spouse [Dark Red] 7: Grey 8: Yellow 9: Light Yellow 10: Blue 11: White
     * 12: Red 13: Light Blue
     */
    public static byte[] ChatMsg(int nType, String sMsg) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CHAT_MSG.getValue());
        oPacket.writeShort(nType);
        oPacket.writeMapleAsciiString(sMsg);
        return oPacket.getPacket();
    }

    // Hiển thị hiệu ứng của Khu vực có Buff
    public static byte[] BuffzoneEffect(int nItemID) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.BUFFZONE_EFFECT.getValue());
        oPacket.writeInt(nItemID);
        return oPacket.getPacket();
    }

    //Phân tích lại SendMigrateToShopRequest - "The Cash Shop is not available for the Guest ID Users."
    public static byte[] GoToCommoditySN(int nItemID) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.BUFFZONE_EFFECT.getValue());
        oPacket.writeInt(nItemID);
        return oPacket.getPacket();
    }

    // CDamageMeter
    public static byte[] DamageMeter(int nDuration) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.DAMAGE_METER.getValue());
        oPacket.writeInt(nDuration);
        return oPacket.getPacket();
    }

    // Bom nổ liên quan đến TryDoingMeleeAttack
    public static byte[] TimeBombAttack(int nSkillID, int nTimeBombX, int nInvincible, int nUserImpactDeg, int nDamage) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.TIME_BOMB_ATTACK.getValue());
        oPacket.writeInt(nSkillID);
        oPacket.writeInt(nTimeBombX);
        oPacket.writeInt(nInvincible);
        oPacket.writeInt(nUserImpactDeg);
        oPacket.writeInt(nDamage);
        return oPacket.getPacket();
    }

    public static byte[] PassiveMove(MovePath moves) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PASSIVE_MOVE.getValue());
        moves.encode(oPacket);
        return oPacket.getPacket();
    }

    public static class FollowCharacterFailType {

        public static final int INVALID_PLACE = 1, // "You are currently in a place where you cannot accept the follow request." 
                IS_FOLLOWING = 2/*
                 Needs dwDriverID to be set, otherwise does 1
                 */, // "Your target is already following %s."
                CANT_ACCEPT = 3, // Follow target cannot accept the request at this time.
                ALREADY_FOLLOWING = 4, // You cannot send a follow request while you are already following someone.
                DENIED = 5, // The follow request has not been accepted.
                TOO_FAR = 6, // You are too far away.
                UNKNOWN = 7; // Cannot be used on this item.
    }

    public static byte[] followCharacterFailed(int nError, int dwDriverID) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.FOLLOW_CHARACTER_FAILED.getValue());
        oPacket.writeInt(nError);
        oPacket.writeInt(dwDriverID);// needed for already following
        return oPacket.getPacket();
    }

    // Sử dụng chiêu của Demon Slayer - Vengeance
    public static byte[] VengeanceSkillApply() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.VENGEANCE_SKILL_APPLY.getValue());
        oPacket.writeInt(3120010);
        return oPacket.getPacket();
    }

    public static byte[] ExJablinApply() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.EX_JABLIN_APPLY.getValue());
        return oPacket.getPacket();
    }

    // OnAskAPSPEvent(195) (Handling)
    public static byte[] AskAPSPEvent(int bNoEnterInput) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.ASK_APSP_EVENT.getValue());
        oPacket.writeInt(bNoEnterInput);
        oPacket.writeInt(6);
        return oPacket.getPacket();
    }

    // Chưa hoàn thiện
    public static byte[] QuestGuideResult() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.QUEST_GUIDE_RESULT.getValue());
        oPacket.write(0);
        oPacket.writeShort(0);
        oPacket.writeInt(0); // Nếu v3 > 0 = Location cannot be displayed.   
        /*
         if (v3 > 0) {
         do {
         oPacket.writeInt(dwDemandItemID);
         oPacket.writeShort(v8);
         if (v8 > 0) {
         do {
         oPacket.writeInt(dwDemandMobID);
         --v8;
         } while (v8 > 0);
         }
         --v3;
         } while (v3 > 0);
         }
         */
        return oPacket.getPacket();
    }

    // Chưa hoàn thiện
    public static byte[] DeliveryQuest(List<Integer> DisallowedDeliveryList, int nItemPos, int nItemID) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.DELIVERY_QUEST.getValue());
        oPacket.writeInt(nItemPos);
        oPacket.writeInt(nItemID);
        // CQuestMan::DecodeDisallowedDeliveryList
        oPacket.writeInt(DisallowedDeliveryList.size());
        if (DisallowedDeliveryList.size() > 0) {
            DisallowedDeliveryList.forEach((DisallowedDelivery) -> {
                oPacket.writeInt(DisallowedDelivery);
            });
        }
        return oPacket.getPacket();
    }

    public static byte[] SkillCooltimeSet(int nSkillID) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.COOLDOWN.getValue());
        oPacket.writeInt(nSkillID);
        oPacket.writeShort(0);
        return oPacket.getPacket();
    }
}
