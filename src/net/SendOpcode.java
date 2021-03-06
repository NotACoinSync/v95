package net;

import constants.ServerConstants;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import tools.ExternalCodeTableGetter;
import tools.IntValueHolder;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

public enum SendOpcode implements IntValueHolder {
    // CLogin::OnPacket
    LOGIN_STATUS,
    GUEST_ID_LOGIN,
    ACCOUNT_INFO,
    SERVERSTATUS,
    GENDER_DONE,
    CONFIRM_EULA_RESULT,
    CHECK_PINCODE,
    UPDATE_PINCODE,
    VIEW_ALL_CHAR,
    SELECT_CHARACTER_BY_VAC,
    SERVERLIST,
    CHARLIST,
    SERVER_IP,
    CHAR_NAME_RESPONSE,
    ADD_NEW_CHAR_ENTRY,
    DELETE_CHAR_RESPONSE,
    LAST_CONNECTED_WORLD,
    RECOMMENDED_WORLD_MESSAGE,
    EXTRA_CHARINFO_RESULT,
    CHECK_SPW_RESULT,
    // CClientSocket::OnPacket
    CHANGE_CHANNEL,
    PING,
    AUTHEN_CODE_CHANGED,
    AUTHEN_MESSAGE,
    CHANNEL_SELECTED,
    HACKSHIELD_REQUEST,
    RELOG_RESPONSE,
    CHECK_CRC_RESULT,
    // CWvsContext::OnPacket
    INVENTORY_OPERATION,
    INVENTORY_GROW,
    STAT_CHANGED,
    GIVE_BUFF,
    CANCEL_BUFF,
    FORCED_STAT_SET,
    FORCED_STAT_RESET,
    UPDATE_SKILLS,
    SKILL_USE_RESULT,
    FAME_RESPONSE,
    SHOW_STATUS_INFO,
    OPEN_FULL_CLIENT_DOWNLOAD_LINK,
    MEMO_RESULT,
    MAP_TRANSFER_RESULT,
    ANTI_MACRO_RESULT,
    CLAIM_RESULT,
    CLAIM_AVAILABLE_TIME,
    CLAIM_STATUS_CHANGED,
    SET_TAMING_MOB_INFO,
    QUEST_CLEAR,
    ENTRUSTED_SHOP_CHECK_RESULT,
    SKILL_LEARN_ITEM_RESULT,
    SKILL_RESET_ITEM_RESULT,
    GATHER_ITEM_RESULT,
    SORT_ITEM_RESULT,
    SUE_CHARACTER_RESULT,
    TRADE_MONEY_LIMIT,
    SET_GENDER,
    GUILD_BBS_PACKET,
    CHAR_INFO,
    PARTY_OPERATION,
    EXPEDITION_RESULT,
    BUDDYLIST,
    GUILD_OPERATION,
    ALLIANCE_OPERATION,
    SPAWN_PORTAL,
    OPEN_GATE,
    SERVERMESSAGE,
    INCUBATOR_RESULT,
    SHOP_SCANNER_RESULT,
    SHOP_LINK_RESULT,
    MARRIAGE_REQUEST,
    MARRIAGE_RESULT,
    WEDDING_GIFT_RESULT,
    NOTIFY_MARRIED_PARTNER_MAP_TRANSFER,
    CASH_PET_FOOD_RESULT,
    SET_WEEK_EVENT_MESSAGE,
    SET_POTION_DISCOUNT_RATE,
    BRIDLE_MOB_CATCH_FAIL,
    MONSTER_BOOK_SET_CARD,
    MONSTER_BOOK_SET_COVER,
    HOUR_CHANGED,
    MINIMAP_ON_OFF,
    CONSULT_AUTHKEY_UPDATE,
    CLASS_COMPETITION_AUTHKEY_UPDATE,
    WEB_BOARD_AUTHKEY_UPDATE,
    SESSION_VALUE,
    PARTY_VALUE,
    FIELD_SET_VARIABLE,
    BONUS_EXP_RATE_CHANGED,
    POTION_DISCOUNT_RATE_CHANGED,
    FAMILY_CHART_RESULT,
    FAMILY_INFO_RESULT,
    FAMILY_RESULT,
    FAMILY_JOIN_REQUEST,
    FAMILY_JOIN_REQUEST_RESULT,
    FAMILY_JOIN_ACCEPTED,
    FAMILY_PRIVILEGE_LIST,
    FAMILY_FAMOUS_POINT_INC_RESULT,
    FAMILY_NOTIFY_LOGIN_OR_LOGOUT,
    FAMILY_SET_PRIVILEGE,
    FAMILY_SUMMON_REQUEST,
    NOTIFY_LEVELUP,
    NOTIFY_MARRIAGE,
    NOTIFY_JOB_CHANGE,
    MAPLE_TV_USE_RES,
    AVATAR_MEGAPHONE_RESULT,
    SET_AVATAR_MEGAPHONE,
    CLEAR_AVATAR_MEGAPHONE,
    CANCEL_NAME_CHANGE_RESULT,
    CANCEL_TRANSFER_WORLD_RESULT,
    DESTROY_SHOP_RESULT,
    FAKE_GM_NOTICE,
    SUCCESS_IN_USE_GACHAPON_BOX,
    NEW_YEAR_CARD_RES,
    RANDOM_MORPH_RES,
    CANCEL_NAME_CHANGE_BY_OTHER,
    SET_BUY_EQUIP_EXT,
    SET_PASSENGER_REQUEST,
    SCRIPT_PROGRESS_MESSAGE,
    DATA_CRC_CHECK_FAILED,
    CAKE_PIE_EVENT_RESULT,
    UPDATE_GM_BOARD,
    SHOW_SLOT_MESSAGE,
    WILD_HUNTER_INFO,
    ACCOUNT_MORE_INFO,
    FIND_FRIEND,
    STAGE_CHANGE,
    DRAGON_BALL_BOX,
    ASK_WHETHER_USE_PAMS_SONG,
    TRANSFER_CHANNEL,
    DISALLOWED_DELIVERY_QUEST_LIST,
    MACRO_SYS_DATA_INIT,
    // CStage::OnPacket
    SET_FIELD,
    SET_ITC,
    SET_CASH_SHOP,
    // CField::OnPacket
    BLOCKED_MAP,
    BLOCKED_SERVER,
    FIELD_SPECIFIC_DATA,
    MULTICHAT,
    WHISPER,
    SPOUSE_CHAT,
    SUMMON_ITEM_INAVAILABLE,
    FIELD_EFFECT,
    FIELD_OBSTACLE_ONOFF,
    FIELD_OBSTACLE_ONOFF_STATUS,
    FIELD_OBSTACLE_ALL_RESET,
    BLOW_WEATHER,
    PLAY_JUKEBOX,
    ADMIN_RESULT,
    OX_QUIZ,
    GMEVENT_INSTRUCTIONS,
    CLOCK,
    SET_QUEST_CLEAR,
    SET_QUEST_TIME,
    WARN_MESSAGE,
    SET_OBJECT_STATE,
    STOP_CLOCK,
    QUICKSLOT_SET,
    FOOTHOLD_INFO,
    REQUEST_FOOTHOLD_INFO,
    HONTALE_TIMER,
    CHAOS_ZAKUM_TIMER,
    HONTAIL_TIMER,
    ZAKUM_TIMER,
    // CUserPool::OnPacket
    SPAWN_PLAYER,
    REMOVE_PLAYER_FROM_MAP,
    // CUserLocal::OnPacket
    CANCEL_CHAIR,
    FACIAL_EXPRESSION_LOCAL,
    USER_EFFECT_LOCAL,
    TELEPORT_LOCAL,
    MESOGIVE_SUCCEEDED,
    MESOGIVE_FAILED,
    MESO_BAG_SUCCEEDED,
    MESO_BAG_FAILED,
    FIELD_FADE_INOUT,
    FIELD_FADE_OUT_FORCE,
    UPDATE_QUEST_INFO,
    NOTIFY_HPDEC_BYFIELD,
    PLAY_EVENT_SOUND,
    PLAYER_HINT,
    MAKER_RESULT,
    OPEN_CLASS_COMPETITION_PAGE,
    OPEN_UI,
    OPEN_UI_WITH_OPTION,
    LOCK_UI,
    DISABLE_UI,
    SPAWN_GUIDE,
    TALK_GUIDE,
    SHOW_COMBO,
    RANDOM_EMOTION,
    RESIGN_QUEST_RETURN,
    PASS_MATE_NAME,
    RADIO_SCHEDULE,
    OPEN_SKILL_GUIDE,
    NOTICE_MSG,
    CHAT_MSG,
    BUFFZONE_EFFECT,
    GOTO_COMMODITY_SN,
    DAMAGE_METER,
    TIME_BOMB_ATTACK,
    PASSIVE_MOVE,
    FOLLOW_CHARACTER_FAILED,
    VENGEANCE_SKILL_APPLY,
    EX_JABLIN_APPLY,
    ASK_APSP_EVENT,
    QUEST_GUIDE_RESULT,
    DELIVERY_QUEST,
    COOLDOWN,
    // CUserPool::OnUserRemotePacket
    CLOSE_RANGE_ATTACK,
    RANGED_ATTACK,
    MAGIC_ATTACK,
    ENERGY_ATTACK,
    SKILL_EFFECT,
    MOVING_SHOOT_ATTACK_PREPARE,
    CANCEL_SKILL_EFFECT,
    DAMAGE_PLAYER,
    FACIAL_EXPRESSION,
    SHOW_ITEM_EFFECT,
    SHOW_UPGRADE_TOMB_EFFECT,
    USER_EFFECT_REMOTE,
    THROW_GRENADE,
    MOVE_PLAYER,
    SHOW_CHAIR,
    UPDATE_CHAR_LOOK,
    GIVE_FOREIGN_BUFF,
    CANCEL_FOREIGN_BUFF,
    UPDATE_PARTYMEMBER_HP,
    GUILD_NAME_CHANGED,
    GUILD_MARK_CHANGED,
    // CUserPool::OnUserCommonPacket 
    CHATTEXT,
    CHATTEXT_CWKPQ,
    CHALKBOARD,
    UPDATE_CHAR_BOX,
    SHOW_CONSUME_EFFECT,
    SHOW_SCROLL_EFFECT,
    SHOW_ITEM_HYPER_UPGRADE_EFFECT,
    SHOW_ITEM_OPTION_UPGRADE_EFFECT,
    SHOW_ITEM_RELEASE_EFFECT,
    SHOW_ITEM_UNRELEASE_EFFECT,
    HIT_BY_USER,
    TESLA_TRIANGLE,
    FOLLOW_CHARACTER,
    SHOW_PQ_REWARD,
    SET_PHASE,
    SET_PORTAL_USABLE,
    SHOW_RECOVERY_UPGRADE_COUNT_EFFECT,
    // CUser::OnPetPacket
    SPAWN_PET,
    EVOLVE_PET,
    MOVE_PET,
    PET_CHAT,
    PET_NAMECHANGE,
    PET_EXCEPTION_LIST_RESULT,
    PET_COMMAND,
    // CUser::OnDragonPacket
    SPAWN_DRAGON,
    MOVE_DRAGON,
    REMOVE_DRAGON,
    // CAdminShopDlg::OnPacket
    ADMIN_SHOP_RESULT,
    ADMIN_SHOP_COMMODITY,
    // AffectedAreaPool::OnPacket
    SPAWN_MIST,
    REMOVE_MIST,
    // CDropPool::OnPacket
    DROP_ITEM_FROM_MAPOBJECT,
    REMOVE_ITEM_FROM_MAP,
    // CEmployeePool::OnPacket
    SPAWN_HIRED_MERCHANT,
    DESTROY_HIRED_MERCHANT,
    UPDATE_HIRED_MERCHANT,
    // CFuncKeyMappedMan::OnPacket
    KEYMAP,
    AUTO_HP_POT,
    AUTO_MP_POT,
    // CMapLoadable::OnPacket
    SET_BACK_EFFECT,
    SET_MAP_OBJECT_VISIBLE,
    CLEAR_BACK_EFFECT,
    // CMapleTVMan::OnPacket
    SEND_TV,
    REMOVE_TV,
    ENABLE_TV,
    BOARD_SET_FLASH_CHANGE_EVENT,
    // MessageBoxPool::OnPacket
    SPAWN_KITE_MESSAGE,
    SPAWN_KITE,
    DESTROY_KITE,
    // CMobPool::OnPacket        
    SPAWN_MONSTER,
    KILL_MONSTER,
    SPAWN_MONSTER_CONTROL,
    MOB_CRC_KEY_CHANGED,
    // CMobPool::OnMobPacket 
    MOVE_MONSTER,
    MOVE_MONSTER_RESPONSE,
    STAT_SET_MONSTER,
    STAT_RESET_MONSTER,
    SUSPEND_RESET_MONSTER,
    AFFECTED_MONSTER,
    DAMAGED_MONSTER,
    MONSTER_SPECIAL_EFFECT_BY_SKILL,
    SHOW_MONSTER_HP,
    SHOW_DRAGGED,
    CATCH_MONSTER,
    EFFECT_BY_ITEM,
    SPEAKING_MONSTER,
    INC_CHARGE_COUNT_MONSTER,
    SKILL_DELAY_MONSTER,
    ESCORT_FULL_PATH_MONSTER,
    ESCORT_STOP_SAY_MONSTER,
    ESCORT_RETURN_BEFORE_MONSTER,
    NEXT_ATTACK_MONSTER,
    ATTACKED_BY_MONSTER,
    // CNpcPool::OnPacket
    IMITATED_NPC_RESULT,
    IMITATED_NPC_DATA,
    LIMITED_NPC_DISABLE_INFO,
    SPAWN_NPC,
    REMOVE_NPC,
    SPAWN_NPC_REQUEST_CONTROLLER,
    // CNpcPool::OnNpcPacket
    MOVE_NPC,
    UPDATE_LIMITED_INFO,
    NPC_SPECIAL_ACTION,
    SET_NPC_SCRIPTABLE,
    // COpenGatePool::OnPacket
    OPEN_GATE_CREATED,
    OPEN_GATE_REMOVED,
    // CReactorPool::OnPacket
    REACTOR_HIT,
    REACTOR_MOVE,
    REACTOR_SPAWN,
    REACTOR_DESTROY,
    // CScriptMan::OnPacket
    NPC_TALK,
    // CShopDlg::OnPacket
    OPEN_NPC_SHOP,
    CONFIRM_SHOP_TRANSACTION,
    // CSummonedPool::OnPacket
    SPAWN_SPECIAL_MAPOBJECT,
    REMOVE_SPECIAL_MAPOBJECT,
    MOVE_SUMMON,
    SUMMON_ATTACK,
    DAMAGE_SUMMON,
    SUMMON_SKILL,
    // CTownPortalPool ::OnPacket
    SPAWN_DOOR,
    REMOVE_DOOR,
    // CField_Coconut::OnPacket
    COCONUT_HIT,
    COCONUT_SCORE,
    // CField_SnowBall::OnPacket
    SNOWBALL_STATE,
    HIT_SNOWBALL,
    SNOWBALL_MESSAGE,
    LEFT_KNOCK_BACK,
    // CField_MonsterCarnival::OnPacket
    MONSTER_CARNIVAL_START,
    MONSTER_CARNIVAL_OBTAINED_CP,
    MONSTER_CARNIVAL_PARTY_CP,
    MONSTER_CARNIVAL_SUMMON,
    MONSTER_CARNIVAL_MESSAGE,
    MONSTER_CARNIVAL_DIED,
    MONSTER_CARNIVAL_LEAVE,
    MONSTER_CARNIVAL_RESULT,
    // CField_KillCount::OnPacket
    KILL_COUNT_INFO,
    // CField_GuildBoss::OnPacket
    GUILD_BOSS_HEALER_MOVE,
    GUILD_BOSS_PULLEY_STATE_CHANGE,
    // CField_AriantArena::OnPacket
    ARIANT_ARENA_USER_SCORE,
    ARIANT_ARENA_SHOW_RESULT,
    // CField_ContiMove::OnPacket
    CONTI_MOVE,
    CONTI_STATE,
    // CField_Tournament::OnPacket
    TOURNAMENT,
    TOURNAMENT_MATCH_TABLE,
    TOURNAMENT_SET_PRIZE,
    TOURNAMENT_UEW,
    // CField_Witchtower::OnPacket
    WITCH_TOWER_SCORE_UPDATE,
    // CUIVega::OnPacket
    VEGA_S,
    VEGA_RESULT,
    VEGA_FAIL,
    VEGA_E,
    // CITC::OnPacket
    ITC_CHARGE_PARAM_RESULT,
    ITC_QUERY_CASH_RESULT,
    ITC_NORMAL_ITEM_RESULT,
    // CRPSGameDlg::OnPacket
    RPS_GAME,
    // CParcelDlg::OnPacket
    PARCEL,
    // CUIMessenger::OnPacket
    MESSENGER,
    // CMiniRoomBaseDlg::OnPacketBase
    PLAYER_INTERACTION,
    // CTrunkDlg::OnPacket
    STORAGE,
    // CStoreBankDlg::OnPacket
    FREDRICK_MESSAGE,
    FREDRICK,
    // CField_Wedding::OnPacket
    WEDDING_PROGRESS,
    WEDDING_CREMONY_END,
    // CField_Massacre::OnPacket
    MASSACRE_INC_GUAGE,
    MASSACRE_RESULT,
    // CUIItemUpgrade::OnPacket
    ITEM_UPGRADE_S,
    ITEM_UPGRADE_RESULT,
    ITEM_UPGRADE_FAIL,
    ITEM_UPGRADE_E,
    // CBattleRecordMan::OnPacket
    DOT_DAMAGE_INFO,
    SERVER_ON_CALC_REQUEST_RESULT,
    // CCashShop::OnPacket
    CASHSHOP_CHARGEPARAM_RESULT,
    CASHSHOP_QUERY_CASH_RESULT,
    CASHSHOP_OPERATION,
    CASHSHOP_PURCHASE_EXP_CHANGED,
    CASHSHOP_GIFT_MATE_INFO_RESULT,
    CASHSHOP_CHECK_DUPLICATED_ID_RESULT,
    CASHSHOP_CHECK_NAME_CHANGE_POSSIBLE_RESULT,
    CASHSHOP_CHECK_TRANSFER_WORLD_POSSIBLE_RESULT,    
    CASHSHOP_GACHAPON_STAMP_ITEM_RESULT,
    CASHSHOP_CASH_ITEM_GACHAPON_RESULT,
    CASHSHOP_CASH_GACHAPON_OPEN_RESULT,
    CASHSHOP_ONE_A_DAY,
    CASHSHOP_NOTICE_FREE_CASH_ITEM,
    CASHSHOP_MEMBER_SHOP_RESULT,
    // CUICharacterSaleDlg::OnPacket
    CHECK_DUPLICATED_ID_RESULT_IN_CS,
    CREATE_NEW_CHARACTER_RESULT_IN_CS,
    CREATE_NEW_CHARACTER_FAIL_IN_CS,
    CHARACTER_SALE,
    // Unknown Stuff:
    CHECK_SSN2ON_CREATE_NEW_CHARACTER_RESULT,
    CHECK_SPWON_CREATE_NEW_CHARACTER_RESULT,
    FIRST_SSNON_CREATE_NEW_CHARACTER_RESULT,
    LOGOUT_GIFT,
    // Not Updated Opcodes:
    FORCED_MAP_EQUIP,
    SHEEP_RANCH_INFO,
    SHEEP_RANCH_CLOTHES,;

    private int code = -2;

    @Override
    public void setValue(int code) {
        this.code = code;
    }

    @Override
    public int getValue() {
        return code;
    }

    public static SendOpcode getOpcodeByOp(int op) {
        for (SendOpcode opcode : values()) {
            if (opcode.getValue() == op) {
                return opcode;
            }
        }
        return null;
    }

    public static Properties getDefaultProperties() throws FileNotFoundException, IOException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(new File("sendops-" + ServerConstants.VERSION + ".properties"))) {
            props.load(fis);
        }
        return props;
    }

    static {
        try {
            ExternalCodeTableGetter.populateValues(getDefaultProperties(), values());
        } catch (IOException e) {
            Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
        }
    }
}
