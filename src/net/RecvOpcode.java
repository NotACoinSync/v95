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

public enum RecvOpcode implements IntValueHolder {
    LOGIN_PASSWORD,
    GUEST_LOGIN,
    SERVERLIST_REREQUEST,
    CHARLIST_REQUEST,
    SERVERSTATUS_REQUEST,
    ACCEPT_TOS,
    SET_GENDER,
    CHECK_PIN_CODE,
    UPDATE_PIN_CODE,
    SERVERLIST_REQUEST,
    LOGOUT_WORLD,
    VIEW_ALL_CHAR,
    SELECT_CHARACTER_BY_VAC,
    RESET_VAC,
    SELECT_CHARACTER,
    MIGRATE_IN,
    CHECK_CHAR_NAME,
    CREATE_CHAR,
    DELETE_CHAR,
    PONG,
    CLIENT_START,
    CLIENT_ERROR,
    RELOG,
    ENABLE_SPW_REQUEST,
    CHECK_SPW_REQUEST,
    ENABLE_SPW_REQUEST_BY_ACV,
    CHECK_SPW_REQUEST_BY_ACV,
    CHECK_OTP_REQUEST,
    CHECK_DELETE_CHARACTER_OTP,
    SSO_ERROR_LOG,
    CLIENT_DUMP_LOG,
    CHECK_EXTRA_CHAR_INFO,
    CREATE_NEW_CHARACTER_EX,
    // Channel Opcodes
    CHANGE_MAP,
    CHANGE_CHANNEL,
    ENTER_CASHSHOP,
    MOVE_PLAYER,
    CANCEL_CHAIR,
    USE_CHAIR,
    CLOSE_RANGE_ATTACK,
    RANGED_ATTACK,
    MAGIC_ATTACK,
    BODY_ATTACK,
    MOVING_SHOOT_ATTACK_PREPARE,
    TAKE_DAMAGE,
    GENERAL_CHAT,
    CLOSE_CHALKBOARD,
    FACE_EXPRESSION,
    USE_ITEMEFFECT,
    USE_DEATHITEM,
    HP,
    PREMIUM,
    BAN_MAP_BY_MOB,
    MONSTER_BOOK_COVER,
    NPC_TALK,
    REMOTE_STORE,
    NPC_TALK_MORE,
    NPC_SHOP,
    STORAGE,
    HIRED_MERCHANT_REQUEST,
    FREDRICK_ACTION,
    DUEY_ACTION,
    USER_EFFECT_LOCAL,
    OWL_OPEN,
    OWL_WARP,
    ADMIN_SHOP_REQUEST,
    GATHER_ITEM,
    SORT_ITEM,
    ITEM_MOVE,
    USE_ITEM,
    CANCEL_ITEM_EFFECT,
    STATE_CHANGE_BY_PORTABLE_CHAIR_REQUEST,
    USE_SUMMON_BAG,
    PET_FOOD,
    USE_MOUNT_FOOD,
    SCRIPTED_ITEM,
    USE_CASH_ITEM,
    DESTROY_PET_ITEM,
    USE_CATCH_ITEM,
    USE_SKILL_BOOK,
    USE_SKILL_RESET_BOOK,
    USE_SHOP_SCANNER_ITEM,
    USE_TELEPORT_ROCK,
    USE_RETURN_SCROLL,
    USE_UPGRADE_SCROLL,
    HYPER_UPGRADE_ITEM,
    ITEM_OPTION_UPGRADE_ITEM,
    UI_OPEN_ITEM,
    ITEM_RELEASE,
    DISTRIBUTE_AP,
    AUTO_DISTRIBUTE_AP,
    CHANGE_STAT,
    CHANGE_STAT_BY_ITEM_OPTION,
    DISTRIBUTE_SP,
    SPECIAL_MOVE,
    CANCEL_BUFF,
    SKILL_EFFECT,
    MESO_DROP,
    GIVE_FAME,
    PARTY,
    CHAR_INFO_REQUEST,
    SPAWN_PET,
    CANCEL_DEBUFF,
    CHANGE_MAP_SPECIAL,
    USE_INNER_PORTAL,
    TROCK_ADD_MAP,
    ANTI_MARCO_ITEM_USE,
    ANTI_MARCO_SKILL_USE,
    ANTI_MARCO_QUESTION,
    REPORT,
    QUEST_ACTION,
    CALC_DAMAGE_STAT_SET_REQUEST,
    THROW_GRENADE,
    SKILL_MACRO,
    USE_ITEM_REWARD,
    LOTTERY_ITEM_USE,
    MAKER_SKILL,
    SUE_CHARACTER,
    USE_GACHAPON_BOX,
    USE_GACHAPON_REMOTE,
    USE_WATER_OF_LIFE,
    REPAIR_DURABILITY_ALL,
    REPAIR_DURABILITY,
    QUEST_RECORD_SET_STATE,
    CLIENT_TIMER_END_REQUEST,
    FOLLOW_CHARACTER_REQUEST,
    FOLLOW_CHARACTER_WITHDRAW,
    SELECT_PQ_REWARD,
    REQUEST_PQ_REWARD,
    PASSENGER_RESULT,
    ADMIN_CHAT,
    PARTYCHAT,
    WHISPER,
    SPOUSE_CHAT,
    MESSENGER,
    PLAYER_INTERACTION,
    PARTY_REQUEST,
    PARTY_RESULT,
    EXPEDITION_REQUEST,
    PARTY_ADVER_REQUEST,
    GUILD_REQUEST,
    GUILD_RESULT,
    ADMIN_COMMAND,
    ADMIN_LOG,
    BUDDYLIST_MODIFY,
    NOTE_ACTION,
    NOTE_FLAG_REQUEST,
    USE_DOOR,
    OPEN_GATE,
    SLIDE_REQUEST,
    CHANGE_KEYMAP,
    RPS_ACTION,
    MARRIAGE_ACTION,
    WEDDING_WISHLIST_REQUEST,
    MARRIAGE_PROGRESS,
    GUEST_BLESS,
    ITEM_VAC_ALERT,
    STALK_BEGIN,
    ALLIANCE_REQUEST,
    ALLIANCE_OPERATION,
    FAMILY_CHART_REQUEST,
    FAMILY_INFO_REQUEST,
    FAMILY_REGISTER_JUNIOR,
    FAMILY_UNREGISTER_JUNIOR,
    FAMILY_UNREGISTER_PARENT,
    FAMILY_JOIN_RESULT,
    FAMILY_USE_PRIVILEGE,
    FAMILY_SET_PRECEPT,
    FAMILY_SUMMON_RESULT,
    CHAT_BLOCK_USER_REQ,
    BBS_OPERATION,
    ENTER_MTS,
    EXP_UP_ITEM_USE_REQUEST,
    TEMP_EXP_USE_REQUEST,
    NEW_YEAR_CARD_REQUEST,
    RANDOM_MORPH_REQUEST,
    CASH_ITEM_GACHAPON_REQUEST,
    CASH_GACHAPON_OPEN_REQUEST,
    CHANGE_MAPLE_POINT_REQUEST,
    CLICK_GUIDE,
    ARAN_COMBO_COUNTER,
    MOB_CRC_KEY_CHANGED_REPLY,
    REQUEST_SESSION_VALUE,
    UPDATE_GM_BOARD,
    ACCOUNT_MORE_INFO,
    FIND_FRIEND,
    ACCEPT_APSP_EVENT,
    DRAGONBALL_BOX_REQUEST,
    DRAGONBALL_BOX_SUMMON_REQUEST,
    MOVE_PET,
    PET_CHAT,
    PET_COMMAND,
    PET_LOOT,
    PET_AUTO_POT,
    PET_EXCLUDE_ITEMS,
    MOVE_SUMMON,
    SUMMON_ATTACK,
    DAMAGE_SUMMON,
    SUMMON_SKILL,
    SUMMON_REMOVE,
    MOVE_DRAGON,
    QUICKSLOT_CHANGE,
    PASSIVE_SKILL_INFO_UPDATE,
    UPDATE_SCREEN_SETTING,
    USER_ATTACK_USER_SPECIFIC,
    USER_PAMS_SONG_USE_REQUEST,
    QUEST_GUIDE_REQUEST,
    USER_REPEAT_EFFECT_REMOVE,
    MOB_MOVE,
    MOB_APPLY_CTRL,
    MOB_DROP_PICKUP_REQUEST,
    MOB_HIT_BY_OBSTACLE,
    MOB_DAMAGE_MOB_FRIENDLY,
    MOB_SELF_DESTRUCT,
    MOB_DAMAGE_MOB,
    MOB_SKILL_DELAY_END,
    MOB_TIME_BOMB_END,
    MOB_ESCORT_COLLISION,
    MOB_REQUEST_ESCORT_INFO,
    MOB_ESCORT_STOP_END_REQUEST,
    NPC_ACTION,
    NPC_SPECIAL_ACTION,
    ITEM_PICKUP,
    DAMAGE_REACTOR,
    TOUCHING_REACTOR,
    REQUIRE_FIELD_OBSTACLE_STATUS,
    EVENT_START,
    SNOWBALL_HIT,
    SNOWBALL_TOUCH,
    COCONUT_HIT,
    TOURNAMENT_MATCH_TABLE,
    PULLEY_HIT,
    MONSTER_CARNIVAL_REQUEST,
    CONTISTATE,
    INVITE_PARTY_MATCH_REQUEST,
    INVITE_PARTY_MATCH_CANCEL,
    REQUEST_FOOT_HOLD_INFO,
    FOOT_HOLD_INFO,
    CASHSHOP_CHARGE_PARAM_REQUEST,
    CASHSHOP_QUERY_CASH_REQUEST,
    CASHSHOP_CASH_ITEM_REQUEST,
    CASHSHOP_CHECK_COUPON_REQUEST,
    CASHSHOP_GIFT_MATE_INFO_REQUEST,
    CHECK_SSN2_ON_CREATE_NEW_CHARACTER,
    CHECK_SPW_ON_CREATE_NEW_CHARACTER,
    CHECK_SSN_ON_CREATE_NEW_CHARACTER,
    RAISE_REFRESH,
    RAISE_UI_STATE,
    RAISE_INC_EXP,
    RAISE_ADD_PIECE,
    SEND_MATE_MAIL,
    REQUEST_GUILD_BOARD_AUTH_KEY,
    REQUEST_CONSULT_AUTH_KEY,
    REQUEST_CLASS_COMPETITION_AUTH_KEY,
    REQUEST_WEB_BOARD_AUTH_KEY,
    GOLD_HAMMER_REQUEST,
    GOLD_HAMMER_COMPLETE,
    ITEM_UPGRADE_COMPLETE,
    BATTLERECORD_ONOFF_REQUEST,
    MAPLETV_SEND_MESSAGE_REQUEST,
    MAPLETV_UPDATE_VIEW_COUNT,
    ITC_CHARGE_PARAM_REQUEST,
    ITC_QUERY_CASH_REQUEST,
    ITC_ITEM_REQUEST,
    CHECK_DUPLICATED_ID_IN_CASHSHOP,
    LOGOUT_GIFT,
    USE_MAPLELIFE,
    CRC_STATE_RESPONSE,;

    private int code = -2;

    @Override
    public void setValue(int code) {
        this.code = code;
    }

    @Override
    public final int getValue() {
        return code;
    }

    public static RecvOpcode getOpcodeByOp(int op) {
        for (RecvOpcode opcode : values()) {
            if (opcode.getValue() == op) {
                return opcode;
            }
        }
        return null;
    }

    public static Properties getDefaultProperties() throws FileNotFoundException, IOException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(new File("recvops-" + ServerConstants.VERSION + ".properties"))) {
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
