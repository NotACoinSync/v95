package net;

import java.util.LinkedHashMap;
import java.util.Map;
import net.server.channel.handlers.*;
import net.server.handlers.*;
import net.server.handlers.BattleRecordMan.*;
import net.server.handlers.Coconut.*;
import net.server.handlers.ContiMove.*;
import net.server.handlers.DamageMeter.*;
import net.server.handlers.Engage.*;
import net.server.handlers.FuncKeyMappedMan.*;
import net.server.handlers.Guild.*;
import net.server.handlers.GuildBoss.*;
import net.server.handlers.Interaction.*;
import net.server.handlers.Item.*;
import net.server.handlers.Login.*;
import net.server.handlers.NPC.*;
import net.server.handlers.OpenGatePool.*;
import net.server.handlers.Parcel.*;
import net.server.handlers.Player.*;
import net.server.handlers.Quest.*;
import net.server.handlers.SnowBall.*;
import net.server.handlers.Wedding.*;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

public final class PacketProcessor {

    private final static Map<String, PacketProcessor> instances = new LinkedHashMap<>();
    private MaplePacketHandler[] handlers;

    private PacketProcessor() {
        int maxRecvOp = 0;
        for (RecvOpcode op : RecvOpcode.values()) {
            if (op.getValue() > maxRecvOp) {
                maxRecvOp = op.getValue();
            }
        }
        handlers = new MaplePacketHandler[maxRecvOp + 1];
    }

    public MaplePacketHandler getHandler(short packetId) {
        if (packetId > handlers.length) {
            return null;
        }
        MaplePacketHandler handler = handlers[packetId];
        if (handler != null) {
            return handler;
        }
        return null;
    }

    public void registerHandler(RecvOpcode code, MaplePacketHandler handler) {
        try {
            handlers[code.getValue()] = handler;
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.log(LogType.ERROR, LogFile.EXCEPTION, e, "Error registering handler - " + code.name());
        }
    }

    public synchronized static PacketProcessor getProcessor(int world, int channel) {
        final String lolpair = world + " " + channel;
        PacketProcessor processor = instances.get(lolpair);
        if (processor == null) {
            processor = new PacketProcessor();
            processor.reset(channel);
            instances.put(lolpair, processor);
        }
        return processor;
    }

    public void reset(int channel) {
        handlers = new MaplePacketHandler[handlers.length];
        registerHandler(RecvOpcode.PONG, new KeepAliveHandler());
        registerHandler(RecvOpcode.CLIENT_DUMP_LOG, new ClientDumpLogHandler());
        registerHandler(RecvOpcode.CLIENT_ERROR, new ClientErrorHandler());
        if (channel < 0) {// login
            registerHandler(RecvOpcode.ACCEPT_TOS, new AcceptToSHandler());
            registerHandler(RecvOpcode.CHECK_PIN_CODE, new AfterLoginHandler());
            registerHandler(RecvOpcode.SERVERLIST_REREQUEST, new SendServerlistRequest());
            registerHandler(RecvOpcode.CHARLIST_REQUEST, new CharlistRequestHandler());
            registerHandler(RecvOpcode.SELECT_CHARACTER, new CharSelectedHandler());
            registerHandler(RecvOpcode.LOGIN_PASSWORD, new LoginPasswordHandler());
            //registerHandler(RecvOpcode.RELOG, new RelogRequestHandler());
            registerHandler(RecvOpcode.SERVERLIST_REQUEST, new SendServerlistRequest());
            registerHandler(RecvOpcode.SERVERSTATUS_REQUEST, new ServerStatusRequestHandler());
            registerHandler(RecvOpcode.CHECK_CHAR_NAME, new CheckCharNameHandler());
            registerHandler(RecvOpcode.CREATE_CHAR, new CreateCharHandler());
            registerHandler(RecvOpcode.DELETE_CHAR, new DeleteCharHandler());
            registerHandler(RecvOpcode.VIEW_ALL_CHAR, new ViewCharHandler());
            registerHandler(RecvOpcode.SELECT_CHARACTER_BY_VAC, new SelectCharacterByVACHandler());
            registerHandler(RecvOpcode.UPDATE_PIN_CODE, new RegisterPinHandler());
            registerHandler(RecvOpcode.GUEST_LOGIN, new GuestLoginHandler());
            registerHandler(RecvOpcode.SET_GENDER, new SendSetGenderPacket());
            registerHandler(RecvOpcode.ENABLE_SPW_REQUEST, new RegisterPicHandler());
            registerHandler(RecvOpcode.CHECK_SPW_REQUEST, new CharSelectedWithPicHandler());
            registerHandler(RecvOpcode.ENABLE_SPW_REQUEST_BY_ACV, new ViewAllPicRegisterHandler());
            registerHandler(RecvOpcode.CHECK_SPW_REQUEST_BY_ACV, new ViewAllCharSelectedWithPicHandler());
        } else {
            // CHANNEL HANDLERS
            registerHandler(RecvOpcode.STALK_BEGIN, new UIMiniMapHandler());
            registerHandler(RecvOpcode.CHANGE_CHANNEL, new ChangeChannelHandler());
            registerHandler(RecvOpcode.NPC_TALK, new SendNPCTalk());
            registerHandler(RecvOpcode.NPC_TALK_MORE, new SendNPCMoreTalk());
            registerHandler(RecvOpcode.QUEST_ACTION, new QuestActionHandler());
            registerHandler(RecvOpcode.NPC_SHOP, new SendShopDlgRequest());
            registerHandler(RecvOpcode.GATHER_ITEM, new GatherItemHandler());
            registerHandler(RecvOpcode.ITEM_MOVE, new ItemMoveHandler());
            registerHandler(RecvOpcode.MESO_DROP, new MesoDropHandler());
            registerHandler(RecvOpcode.MIGRATE_IN, new MigrateInHandler());
            registerHandler(RecvOpcode.CHANGE_MAP, new SendTransferFieldRequest());
            registerHandler(RecvOpcode.MOB_MOVE, new MoveLifeHandler());
            registerHandler(RecvOpcode.CLOSE_RANGE_ATTACK, new TryDoingMeleeAttack());
            registerHandler(RecvOpcode.RANGED_ATTACK, new TryDoingShootAttack());
            registerHandler(RecvOpcode.MAGIC_ATTACK, new TryDoingMagicAttack());
            registerHandler(RecvOpcode.BODY_ATTACK, new TryDoingBodyAttack());
            registerHandler(RecvOpcode.MOVING_SHOOT_ATTACK_PREPARE, new TryDoingSmoothingMovingShootAttackPrepare());
            registerHandler(RecvOpcode.TAKE_DAMAGE, new SendSetDamaged());
            registerHandler(RecvOpcode.MOVE_PLAYER, new MovePlayerHandler());
            registerHandler(RecvOpcode.USE_CASH_ITEM, new SendUseCashItem());
            registerHandler(RecvOpcode.USE_ITEM, new SendPortalScrollUseRequest());
            registerHandler(RecvOpcode.USE_RETURN_SCROLL, new SendPortalScrollUseRequest());
            registerHandler(RecvOpcode.USE_UPGRADE_SCROLL, new ScrollHandler());
            registerHandler(RecvOpcode.USE_SUMMON_BAG, new UseSummonBag());
            registerHandler(RecvOpcode.FACE_EXPRESSION, new FaceExpressionHandler());
            registerHandler(RecvOpcode.CHANGE_STAT, new SendStatChangeRequest());
            registerHandler(RecvOpcode.ITEM_PICKUP, new SendDropPickUpRequest());
            registerHandler(RecvOpcode.CHAR_INFO_REQUEST, new CharInfoRequestHandler());
            registerHandler(RecvOpcode.CANCEL_ITEM_EFFECT, new CancelItemEffectHandler());
            registerHandler(RecvOpcode.DISTRIBUTE_AP, new SendAbilityUpRequest());
            registerHandler(RecvOpcode.DISTRIBUTE_SP, new SendSkillUpRequest());
            registerHandler(RecvOpcode.USER_EFFECT_LOCAL, new SendSkillEffectRequest());
            registerHandler(RecvOpcode.CHANGE_KEYMAP, new SendFuncKeyMappedMan());
            registerHandler(RecvOpcode.SPECIAL_MOVE, new SendSkillUseRequest());
            registerHandler(RecvOpcode.USE_SKILL_RESET_BOOK, new SendSkillResetItemUseRequest());
            registerHandler(RecvOpcode.USE_SKILL_BOOK, new SendSkillLearnItemUseRequest());
            registerHandler(RecvOpcode.CANCEL_BUFF, new SendSkillCancelRequest());
            registerHandler(RecvOpcode.USE_INNER_PORTAL, new TryRegisterTeleport());
            registerHandler(RecvOpcode.CHANGE_MAP_SPECIAL, new ChangeMapSpecialHandler());
            registerHandler(RecvOpcode.STORAGE, new SendTrunkDlgRequest());
            registerHandler(RecvOpcode.GIVE_FAME, new GiveFameHandler());
            registerHandler(RecvOpcode.PARTYCHAT, new PartyChatHandler());
            registerHandler(RecvOpcode.USE_DOOR, new DoorHandler());
            registerHandler(RecvOpcode.OPEN_GATE, new TryEnterOpenGate());
            registerHandler(RecvOpcode.ENTER_MTS, new EnterMTSHandler());
            registerHandler(RecvOpcode.ENTER_CASHSHOP, new EnterCashShopHandler());
            registerHandler(RecvOpcode.DAMAGE_SUMMON, new DamageSummonHandler());
            registerHandler(RecvOpcode.MOVE_SUMMON, new MoveSummonHandler());
            registerHandler(RecvOpcode.SUMMON_ATTACK, new SummonDamageHandler());
            registerHandler(RecvOpcode.USE_ITEMEFFECT, new UseItemEffectHandler());
            registerHandler(RecvOpcode.USE_CHAIR, new SendSitOnPortableChairRequest());
            registerHandler(RecvOpcode.CANCEL_CHAIR, new SendGetUpFromChairRequest());
            registerHandler(RecvOpcode.DAMAGE_REACTOR, new ReactorHitHandler());
            registerHandler(RecvOpcode.BBS_OPERATION, new BBSOperationHandler());
            registerHandler(RecvOpcode.SKILL_EFFECT, new SendSkillPrepareRequest());
            registerHandler(RecvOpcode.NPC_ACTION, new SendNPCMove());
            registerHandler(RecvOpcode.CASHSHOP_QUERY_CASH_REQUEST, new TouchingCashShopHandler());
            registerHandler(RecvOpcode.CASHSHOP_CASH_ITEM_REQUEST, new CashOperationHandler());
            registerHandler(RecvOpcode.CASHSHOP_CHECK_COUPON_REQUEST, new CouponCodeHandler());
            registerHandler(RecvOpcode.SPAWN_PET, new SpawnPetHandler());
            registerHandler(RecvOpcode.MOVE_PET, new MovePetHandler());
            registerHandler(RecvOpcode.PET_CHAT, new PetChatHandler());
            registerHandler(RecvOpcode.PET_COMMAND, new PetCommandHandler());
            registerHandler(RecvOpcode.PET_FOOD, new SendPetFoodItemUseRequest());
            registerHandler(RecvOpcode.PET_LOOT, new PetLootHandler());
            registerHandler(RecvOpcode.MOB_APPLY_CTRL, new AutoAggroHandler());
            registerHandler(RecvOpcode.MOB_SELF_DESTRUCT, new MobSelfDestructHandler());
            registerHandler(RecvOpcode.CANCEL_DEBUFF, new CancelDebuffHandler());
            registerHandler(RecvOpcode.SKILL_MACRO, new SendSkillMacroRequest());
            registerHandler(RecvOpcode.NOTE_ACTION, new NoteActionHandler());
            registerHandler(RecvOpcode.CLOSE_CHALKBOARD, new CloseChalkboardHandler());
            registerHandler(RecvOpcode.USE_MOUNT_FOOD, new UseMountFoodHandler());
            //registerHandler(RecvOpcode.MTS_OPERATION, new MTSHandler());
            registerHandler(RecvOpcode.SPOUSE_CHAT, new SpouseChatHandler());
            registerHandler(RecvOpcode.PET_AUTO_POT, new PetAutoPotHandler());
            registerHandler(RecvOpcode.PET_EXCLUDE_ITEMS, new PetExcludeItemsHandler());
            registerHandler(RecvOpcode.TROCK_ADD_MAP, new TrockAddMapHandler());
            registerHandler(RecvOpcode.HIRED_MERCHANT_REQUEST, new HiredMerchantRequest());
            registerHandler(RecvOpcode.MOB_DAMAGE_MOB, new MobDamageMobHandler());
            registerHandler(RecvOpcode.REPORT, new ReportHandler());
            registerHandler(RecvOpcode.MONSTER_BOOK_COVER, new MonsterBookCoverHandler());
            registerHandler(RecvOpcode.AUTO_DISTRIBUTE_AP, new AutoAssignRequest());
            registerHandler(RecvOpcode.MAKER_SKILL, new MakerSkillHandler());
            registerHandler(RecvOpcode.FAMILY_REGISTER_JUNIOR, new FamilyAddHandler());
            registerHandler(RecvOpcode.FAMILY_USE_PRIVILEGE, new FamilyUseHandler());
            registerHandler(RecvOpcode.GOLD_HAMMER_REQUEST, new UseHammerHandler());
            registerHandler(RecvOpcode.SCRIPTED_ITEM, new SendScriptRunItemRequest());
            registerHandler(RecvOpcode.TOUCHING_REACTOR, new TouchReactorHandler());
            registerHandler(RecvOpcode.SUMMON_SKILL, new BeholderHandler());
            registerHandler(RecvOpcode.GENERAL_CHAT, new SendChatMessage());
            registerHandler(RecvOpcode.ADMIN_COMMAND, new AdminCommandHandler());
            registerHandler(RecvOpcode.ADMIN_LOG, new AdminLogHandler());
            registerHandler(RecvOpcode.ALLIANCE_REQUEST, new AllianceRequestHandler());
            registerHandler(RecvOpcode.ALLIANCE_OPERATION, new AllianceOperationHandler());
            registerHandler(RecvOpcode.EXP_UP_ITEM_USE_REQUEST, new UseSolomonHandler());
            registerHandler(RecvOpcode.TEMP_EXP_USE_REQUEST, new UseGachaExpHandler());
            registerHandler(RecvOpcode.USE_ITEM_REWARD, new ItemRewardHandler());
            registerHandler(RecvOpcode.USE_GACHAPON_REMOTE, new SendUseGachaponRemoteRequest());
            registerHandler(RecvOpcode.FAMILY_JOIN_RESULT, new AcceptFamilyHandler());
            registerHandler(RecvOpcode.DUEY_ACTION, new DueyHandler());
            registerHandler(RecvOpcode.USE_DEATHITEM, new UseDeathItemHandler());
            registerHandler(RecvOpcode.USE_MAPLELIFE, new UseMapleLifeHandler());
            registerHandler(RecvOpcode.USE_CATCH_ITEM, new UseCatchItemHandler());
            registerHandler(RecvOpcode.MOB_DAMAGE_MOB_FRIENDLY, new MobDamageMobFriendlyHandler());
            //registerHandler(RecvOpcode.PARTY_SEARCH_REGISTER, new PartySearchRegisterHandler());
            registerHandler(RecvOpcode.SORT_ITEM, new SortItemHandler());
            registerHandler(RecvOpcode.SNOWBALL_TOUCH, new SnowBall_Update());
            registerHandler(RecvOpcode.SNOWBALL_HIT, new Snowball_BasicActionAttack());
            registerHandler(RecvOpcode.COCONUT_HIT, new Coconut_BasicActionAttack());
            registerHandler(RecvOpcode.CONTISTATE, new ContiMove_Init());
            registerHandler(RecvOpcode.ARAN_COMBO_COUNTER, new RequestIncCombo());
            registerHandler(RecvOpcode.ACCEPT_APSP_EVENT, new RequestAskAPSPEvent());
            registerHandler(RecvOpcode.CLICK_GUIDE, new ClickGuideHandler());
            registerHandler(RecvOpcode.FREDRICK_ACTION, new FredrickHandler());
            registerHandler(RecvOpcode.MONSTER_CARNIVAL_REQUEST, new MonsterCarnivalHandler());
            registerHandler(RecvOpcode.REMOTE_STORE, new SendRemoteShopOpenRequest());
            registerHandler(RecvOpcode.MARRIAGE_PROGRESS, new Wedding_SendBuff());
            registerHandler(RecvOpcode.GUEST_BLESS, new Wedding_SendGuestBless());
            registerHandler(RecvOpcode.ADMIN_CHAT, new AdminChatHandler());
            registerHandler(RecvOpcode.MOVE_DRAGON, new MoveDragonHandler());
            registerHandler(RecvOpcode.QUICKSLOT_CHANGE, new QuickSlotSaveHandler());
            registerHandler(RecvOpcode.PASSIVE_SKILL_INFO_UPDATE, new UpdatePassiveSkillData());
            registerHandler(RecvOpcode.UPDATE_SCREEN_SETTING, new UpdateScreenSettingHander());
            registerHandler(RecvOpcode.USER_ATTACK_USER_SPECIFIC, new UserAttackUserSpecificHandler());
            registerHandler(RecvOpcode.USER_PAMS_SONG_USE_REQUEST, new UserPamsSongUseRequestHandler());
            registerHandler(RecvOpcode.QUEST_GUIDE_REQUEST, new QuestGuideRequestHandler());
            registerHandler(RecvOpcode.USER_REPEAT_EFFECT_REMOVE, new UserRepeatEffectRemoveHandler());
            registerHandler(RecvOpcode.CRC_STATE_RESPONSE, new CRCStateHandler());
            registerHandler(RecvOpcode.RPS_ACTION, new RPSActionHandler());
            registerHandler(RecvOpcode.OWL_WARP, new OwlWarpHandler());
            registerHandler(RecvOpcode.USE_WATER_OF_LIFE, new UseWaterOfLifeHandler());
            registerHandler(RecvOpcode.CASH_ITEM_GACHAPON_REQUEST, new CashItemGachaponRequestHandler());
            registerHandler(RecvOpcode.USE_SHOP_SCANNER_ITEM, new SendShopScannerItemUseRequest());            
            registerHandler(RecvOpcode.STATE_CHANGE_BY_PORTABLE_CHAIR_REQUEST, new StateChangeByPoralChairRequestHandler());
            registerHandler(RecvOpcode.REQUIRE_FIELD_OBSTACLE_STATUS, new RequireFieldObstacleStatusHandler());
            registerHandler(RecvOpcode.MOB_CRC_KEY_CHANGED_REPLY, new MobCrcKeyChangedReplyHandler());
            registerHandler(RecvOpcode.INVITE_PARTY_MATCH_REQUEST, new PartySearchStartHandler());
            registerHandler(RecvOpcode.INVITE_PARTY_MATCH_CANCEL, new PartyInviteMatchCancelHandler());
            registerHandler(RecvOpcode.CALC_DAMAGE_STAT_SET_REQUEST, new UserCalcDamageStatSetRequestHandler());
            registerHandler(RecvOpcode.FAMILY_INFO_REQUEST, new FamilyOpenHandler());
            registerHandler(RecvOpcode.ACCOUNT_MORE_INFO, new AccountMoreInfoHandler());
            registerHandler(RecvOpcode.FIND_FRIEND, new FindFriendHandler());
            registerHandler(RecvOpcode.THROW_GRENADE, new ThrowGrenadeHandler());
            registerHandler(RecvOpcode.ADMIN_SHOP_REQUEST, new SendAdminShopRequest());
            registerHandler(RecvOpcode.BATTLERECORD_ONOFF_REQUEST, new RequestOnCalc());
            registerHandler(RecvOpcode.QUEST_RECORD_SET_STATE, new SaveDamageMeterInfoHandler());
            registerHandler(RecvOpcode.MARRIAGE_ACTION, new MarriageActionHandler());
            registerHandler(RecvOpcode.PLAYER_INTERACTION, new PlayerInteractionHandler());
            registerHandler(RecvOpcode.MESSENGER, new MessengerHandler());
            registerHandler(RecvOpcode.PARTY_REQUEST, new PartyRequestHandler());
            registerHandler(RecvOpcode.PARTY_RESULT, new PartyResultHandler());
            registerHandler(RecvOpcode.GUILD_RESULT, new DenyGuildRequestHandler());
            registerHandler(RecvOpcode.GUILD_REQUEST, new GuildOperationHandler());
            registerHandler(RecvOpcode.WHISPER, new SendWhisperRequest());
            registerHandler(RecvOpcode.PULLEY_HIT, new GuildBoss_BasicActionAttack());
            registerHandler(RecvOpcode.BUDDYLIST_MODIFY, new SendModifyFriendRequest());
            registerHandler(RecvOpcode.FOLLOW_CHARACTER_REQUEST, new FollowCharacterRequest());
            registerHandler(RecvOpcode.PASSENGER_RESULT, new SetPassengerResultHandler());
            registerHandler(RecvOpcode.ITEM_RELEASE, new ItemReleaseRequestHandler());
            registerHandler(RecvOpcode.HYPER_UPGRADE_ITEM, new SendHyperUpgradeItemUseRequest());
            registerHandler(RecvOpcode.ITEM_OPTION_UPGRADE_ITEM, new ItemOptionUpgradeItemUseHandler());
            registerHandler(RecvOpcode.MOB_SKILL_DELAY_END, new MobSkillDelayEndHandler());
            registerHandler(RecvOpcode.DRAGONBALL_BOX_REQUEST, new SendDragonBallBoxRequestHandler());
            registerHandler(RecvOpcode.DRAGONBALL_BOX_SUMMON_REQUEST, new SendDragonBallSummonRequestHandler());
            registerHandler(RecvOpcode.LOGOUT_GIFT, new SendLogoutGift());
        }
    }
}
