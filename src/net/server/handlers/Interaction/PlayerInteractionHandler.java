package net.server.handlers.Interaction;

import client.MapleCharacter;
import client.MapleClient;
import client.MessageType;
import client.autoban.AutobanFactory;
import client.inventory.Item;
import client.inventory.ItemFactory;
import client.inventory.MapleInventoryType;
import constants.FeatureSettings;
import constants.ItemConstants;
import java.sql.SQLException;
import java.util.Arrays;
import net.AbstractMaplePacketHandler;
import server.*;
import server.maps.FieldLimit;
import server.maps.objects.HiredMerchant;
import server.maps.objects.MapleMapObject;
import server.maps.objects.MapleMapObjectType;
import server.maps.objects.miniroom.Omok;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CField.EmployeePool;
import tools.packets.CField.MiniRoomBaseDlg;
import tools.packets.CField.userpool.UserCommon;
import tools.packets.PacketHelper;
import tools.packets.WvsContext;

public final class PlayerInteractionHandler extends AbstractMaplePacketHandler {

    public static class A {

        public static final int // Just general things for the below
                MRP_Create = 0x0, MRP_CreateResult = 0x1, MRP_Invite = 0x2, MRP_InviteResult = 0x3, MRP_Enter = 0x4, MRP_EnterResult = 0x5, MRP_Chat = 0x6, MRP_GameMessage = 0x7, MRP_UserChat = 0x8, MRP_Avatar = 0x9, MRP_Leave = 0xA, MRP_Balloon = 0xB, MRP_NotAvailableField = 0xC, MRP_FreeMarketClip = 0xD, MRP_CheckSSN2 = 0xE,
                // TRUNK
                TRP_PutItem = 0xF, TRP_PutMoney = 0x10, TRP_Trade = 0x11, TRP_UnTrade = 0x12, TRP_MoveItemToInventory = 0x13, TRP_ItemCRC = 0x14, TRP_LimitFail = 0x15,
                // PlayerShopPool?
                PSP_PutItem = 0x16, PSP_BuyItem = 0x17, PSP_BuyResult = 0x18, PSP_Refresh = 0x19, PSP_AddSoldItem = 0x1A, PSP_MoveItemToInventory = 0x1B, PSP_Ban = 0x1C, PSP_KickedTimeOver = 0x1D, PSP_DeliverBlackList = 0x1E, PSP_AddBlackList = 0x1F, PSP_DeleteBlackList = 0x20,
                // Merchants
                ESP_PutItem = 0x21, ESP_BuyItem = 0x22, ESP_BuyResult = 0x23, ESP_Refresh = 0x24, ESP_AddSoldItem = 0x25, ESP_MoveItemToInventory = 0x26, ESP_GoOut = 0x27, ESP_ArrangeItem = 0x28, ESP_WithdrawAll = 0x29, ESP_WithdrawAllResult = 0x2A, ESP_WithdrawMoney = 0x2B, ESP_WithdrawMoneyResult = 0x2C, ESP_AdminChangeTitle = 0x2D, ESP_DeliverVisitList = 0x2E, ESP_DeliverBlackList = 0x2F, ESP_AddBlackList = 0x30, ESP_DeleteBlackList = 0x31,
                // MiniGameRoomPool?
                MGRP_TieRequest = 0x32, MGRP_TieResult = 0x33, MGRP_GiveUpRequest = 0x34, MGRP_GiveUpResult = 0x35, MGRP_RetreatRequest = 0x36, MGRP_RetreatResult = 0x37, MGRP_LeaveEngage = 0x38, MGRP_LeaveEngageCancel = 0x39, MGRP_Ready = 0x3A, MGRP_CancelReady = 0x3B, MGRP_Ban = 0x3C, MGRP_Start = 0x3D, MGRP_GameResult = 0x3E, MGRP_TimeOver = 0x3F,
                // Omok
                ORP_PutStoneChecker = 0x40, ORP_InvalidStonePosition = 0x41, ORP_InvalidStonePosition_Normal = 0x42, ORP_InvalidStonePosition_By33 = 0x43,
                // MatchGamePool?
                MGP_TurnUpCard = 0x44, MGP_MatchCard = 0x45;
    }

    public enum Action {
        CREATE(0),
        INVITE(2),
        DECLINE(3),
        VISIT(4),
        ROOM(5), // CMiniRoomBaseDlg::OnEnterResultStatic
        CHAT(6),
        CHAT_THING(8),
        EXIT(0xA),
        OPEN(0xB),
        TRADE_BIRTHDAY(0x0E),
        SET_ITEMS(0xF),
        SET_MESO(0x10),
        CONFIRM(0x11),
        TRANSACTION(0x14),
        ADD_ITEM(0x16),
        BUY(0x17),
        UPDATE_MERCHANT(0x19),
        REMOVE_ITEM(0x1B),
        BAN_PLAYER(0x1C),
        MERCHANT_THING(0x1D),
        OPEN_STORE(0x1E),
        PUT_ITEM(0x21),
        MERCHANT_BUY(0x22),
        TAKE_ITEM_BACK(0x26),
        MAINTENANCE_OFF(0x27),
        MERCHANT_ORGANIZE(0x28),
        CLOSE_MERCHANT(0x29),
        REAL_CLOSE_MERCHANT(0x2A),
        MERCHANT_MESO(0x2B),
        SOMETHING(0x2D),
        VIEW_VISITORS(0x2E),
        BLACKLIST(0x2F),
        REQUEST_TIE(0x32),
        ANSWER_TIE(0x33),
        GIVE_UP(0x34),
        EXIT_AFTER_GAME(0x38),
        CANCEL_EXIT(0x39),
        READY(0x3A),
        UN_READY(0x3B),
        KICK(0x3C),
        START(0x3D),
        GET_RESULT(0x3E),
        SKIP(0x3F),
        MOVE_OMOK(0x40),
        SELECT_CARD(0x44);

        final byte code;

        private Action(int code) {
            this.code = (byte) code;
        }

        public byte getCode() {
            return code;
        }
    }

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        byte mode = slea.readByte();
        MapleCharacter chr = c.getPlayer();
        if (mode == Action.CREATE.getCode()) {
            byte createType = slea.readByte();
            switch (createType) {
                case 3:
                    // trade
                    if (!FeatureSettings.TRADE) {
                        c.getPlayer().dropMessage(MessageType.POPUP, FeatureSettings.TRADE_DISABLED);
                        c.announce(WvsContext.enableActions());
                        return;
                    }
                    MapleTrade.startTrade(chr);
                    break;
                case 1: {
                    // omok mini game
                    if (chr.getChalkboard() != null || FieldLimit.CANNOTMINIGAME.check(chr.getMap().getMapData().getFieldLimit())) {
                        return;
                    }
                    String desc = slea.readMapleAsciiString();
                    byte password = slea.readByte(); // password
                    String pass = null;
                    if (password == 1) {
                        pass = slea.readMapleAsciiString();
                    }
                    byte type = slea.readByte(); // 20 6E 4E
                    if (!chr.haveItem(4080000 + type)) {
                        System.out.println("Doesn't have omok set");
                        return;
                    }
                    Omok mg = new Omok(chr, type);
                    mg.setTitle(desc);
                    mg.setPassword(pass);
                    MapleMiniGame game = new MapleMiniGame(chr, desc);
                    chr.setMiniGame(game);
                    game.setPassword(pass);
                    game.setOmokSetType(type);
                    game.setGameType("omok");
                    chr.getMap().addMapObject(game);
                    chr.getMap().announce(UserCommon.MiniRoomBalloon.addOmokBox(chr, 1, 0));
                    game.sendOmok(c, type);
                    break;
                }
                case 2: {
                    // matchcard
                    if (chr.getChalkboard() != null) {
                        return;
                    }
                    String desc = slea.readMapleAsciiString();
                    byte password = slea.readByte(); // password
                    String pass = null;
                    if (password == 1) {
                        pass = slea.readMapleAsciiString();
                    }
                    int type = slea.readByte(); // 20 6E 4E
                    MapleMiniGame game = new MapleMiniGame(chr, desc);
                    game.setOmokSetType(type);
                    if (type == 0) {
                        game.setMatchesToWin(6);
                    } else if (type == 1) {
                        game.setMatchesToWin(10);
                    } else if (type == 2) {
                        game.setMatchesToWin(15);
                    }
                    game.setGameType("matchcard");
                    chr.setMiniGame(game);
                    game.setPassword(pass);
                    chr.getMap().addMapObject(game);
                    chr.getMap().announce(UserCommon.MiniRoomBalloon.addMatchCardBox(chr, 1, 0));
                    game.sendMatchCard(c, type);
                    break;
                }
                case 4:
                case 5: {
                    // shop
                    if (!chr.getMap().getMapObjectsInRange(chr.getPosition(), 23000, Arrays.asList(MapleMapObjectType.SHOP, MapleMapObjectType.HIRED_MERCHANT)).isEmpty()) {
                        return;
                    }
                    if (chr.isIronMan()) {
                        c.announce(WvsContext.enableActions());
                        return;
                    }
                    if (!FeatureSettings.HIRED_MERCHANTS) {
                        chr.dropMessage(MessageType.POPUP, FeatureSettings.HIRED_MERCHANTS_DISABLED);
                        c.announce(WvsContext.enableActions());
                        return;
                    }
                    String desc = slea.readMapleAsciiString();
                    slea.skip(3);
                    int itemId = slea.readInt();
                    if (chr.getInventory(MapleInventoryType.CASH).countById(itemId) < 1) {
                        return;
                    }
                    if (chr.getMapId() > 910000000 && chr.getMapId() < 910000023 || itemId > 5030000 && itemId < 5030012 || itemId > 5140000 && itemId < 5140006) {
                        if (createType == 4) {
                            MaplePlayerShop shop = new MaplePlayerShop(c.getPlayer(), desc);
                            chr.setPlayerShop(shop);
                            chr.getMap().addMapObject(shop);
                            shop.sendShop(c);
                            c.announce(MiniRoomBaseDlg.getPlayerShopRemoveVisitor(1));
                        } else {
                            long endTime = 0;
                            for (Item item : chr.getInventory(MapleInventoryType.CASH).list()) {
                                if (item.getItemId() == itemId && item.getExpiration() > endTime) {
                                    endTime = item.getExpiration();
                                }
                            }
                            HiredMerchant merchant = new HiredMerchant(chr, itemId, desc, endTime);
                            chr.setHiredMerchant(merchant);
                            chr.getClient().getChannelServer().addHiredMerchant(chr.getId(), merchant);
                            chr.announce(MiniRoomBaseDlg.EntrustedShop.getHiredMerchant(chr, merchant, true));
                        }
                    }
                    break;
                }
                default:
                    break;
            }
        } else if (mode == Action.INVITE.getCode()) {
            int otherPlayer = slea.readInt();
            if (chr.getId() == otherPlayer) {
                return;
            }
            if (!FeatureSettings.TRADE) {
                c.getPlayer().dropMessage(MessageType.POPUP, FeatureSettings.TRADE_DISABLED);
                c.announce(WvsContext.enableActions());
                return;
            }
            MapleCharacter target = chr.getMap().getCharacterById(otherPlayer);
            if (target != null) {
                MapleTrade.inviteTrade(chr, target);
            }
        } else if (mode == Action.DECLINE.getCode()) {
            MapleTrade.declineTrade(chr);
        } else if (mode == Action.VISIT.getCode()) {
            if (chr.getTrade() != null && chr.getTrade().getPartner() != null) {
                if (!FeatureSettings.TRADE) {
                    c.getPlayer().dropMessage(MessageType.POPUP, FeatureSettings.TRADE_DISABLED);
                    c.announce(WvsContext.enableActions());
                    return;
                }
                if (!chr.getTrade().isFullTrade() && !chr.getTrade().getPartner().isFullTrade()) {
                    MapleTrade.visitTrade(chr, chr.getTrade().getPartner().getChr());
                } else {
                    c.announce(WvsContext.enableActions()); // Ill be nice and not dc u
                }
            } else {
                int oid = slea.readInt();
                MapleMapObject ob = chr.getMap().getMapObject(oid);
                if (ob instanceof MaplePlayerShop) {
                    if (chr.isIronMan()) {
                        c.announce(WvsContext.enableActions());
                        return;
                    }
                    MaplePlayerShop shop = (MaplePlayerShop) ob;
                    if (shop.isBanned(chr.getName())) {
                        chr.dropMessage(1, "You have been banned from this store.");
                        return;
                    }
                    if (shop.hasFreeSlot() && !shop.isVisitor(c.getPlayer())) {
                        shop.addVisitor(c.getPlayer());
                        chr.setPlayerShop(shop);
                        shop.sendShop(c);
                    }
                } else if (ob instanceof MapleMiniGame) {
                    byte haspass = slea.readByte();
                    MapleMiniGame game = (MapleMiniGame) ob;
                    if (game.hasFreeSlot() && !game.isVisitor(c.getPlayer())) {
                        if (haspass == 1 && game.getPassword() == null) {
                            return;
                        }
                        if (game.getPassword() != null) {
                            String pass = slea.readMapleAsciiString();
                            if (!game.getPassword().equals(pass)) {
                                chr.getClient().announce(PacketHelper.getMiniGamePassIncorrect());
                                return;
                            }
                        }
                        game.addVisitor(c.getPlayer());
                        chr.setMiniGame(game);
                        switch (game.getGameType()) {
                            case "omok":
                                game.sendOmok(c, game.getOmokSetType());
                                break;
                            case "matchcard":
                                game.sendMatchCard(c, game.getOmokSetType());
                                break;
                        }
                    } else {
                        chr.getClient().announce(PacketHelper.getMiniGameFull());
                    }
                } else if (ob instanceof HiredMerchant && chr.getHiredMerchant() == null) {
                    if (chr.isIronMan()) {
                        c.announce(WvsContext.enableActions());
                        return;
                    }
                    if (!FeatureSettings.HIRED_MERCHANTS) {
                        chr.dropMessage(MessageType.POPUP, FeatureSettings.HIRED_MERCHANTS_DISABLED);
                        c.announce(WvsContext.enableActions());
                        return;
                    }
                    HiredMerchant merchant = (HiredMerchant) ob;
                    if (merchant.isOwner(c.getPlayer())) {
                        merchant.setOpen(false);
                        merchant.removeAllVisitors("");
                        c.announce(MiniRoomBaseDlg.EntrustedShop.getHiredMerchant(chr, merchant, false));
                    } else if (!merchant.isOpen()) {
                        chr.dropMessage(1, "This shop is in maintenance, please come by later.");
                        return;
                    } else if (merchant.getFreeSlot() == -1) {
                        chr.dropMessage(1, "This shop has reached it's maximum capacity, please come by later.");
                        return;
                    } else {
                        merchant.addVisitor(c.getPlayer());
                        c.announce(MiniRoomBaseDlg.EntrustedShop.getHiredMerchant(c.getPlayer(), merchant, false));
                    }
                    chr.setHiredMerchant(merchant);
                }
            }
        } else if (mode == Action.CHAT.getCode()) { // chat lol
            slea.readInt();
            HiredMerchant merchant = chr.getHiredMerchant();
            if (chr.getTrade() != null) {
                chr.getTrade().chat(slea.readMapleAsciiString());
            } else if (chr.getPlayerShop() != null) { // mini game
                MaplePlayerShop shop = chr.getPlayerShop();
                if (shop != null) {
                    shop.chat(c, slea.readMapleAsciiString());
                }
            } else if (chr.getMiniGame() != null) {
                MapleMiniGame game = chr.getMiniGame();
                if (game != null) {
                    game.chat(c, slea.readMapleAsciiString());
                }
            } else if (merchant != null) {
                String message = chr.getName() + " : " + slea.readMapleAsciiString();
                byte slot = (byte) (merchant.getVisitorSlot(c.getPlayer()) + 1);
                merchant.getMessages().add(new Pair<>(message, slot));
                merchant.broadcastToVisitors(MiniRoomBaseDlg.EntrustedShop.hiredMerchantChat(message, slot));
            }
        } else if (mode == Action.EXIT.getCode()) {
            if (chr.getTrade() != null) {
                MapleTrade.cancelTrade(c.getPlayer());
            } else {
                MaplePlayerShop shop = chr.getPlayerShop();
                MapleMiniGame game = chr.getMiniGame();
                HiredMerchant merchant = chr.getHiredMerchant();
                if (shop != null) {
                    if (shop.isOwner(c.getPlayer())) {
                        for (MaplePlayerShopItem mpsi : shop.getItems()) {
                            if (mpsi.getItem().getPerBundle() > 1) {
                                Item iItem = mpsi.getItem().copy();
                                iItem.setQuantity((short) (mpsi.getItem().getPerBundle() * iItem.getQuantity()));
                                MapleInventoryManipulator.addFromDrop(c, iItem, false);
                            } else if (mpsi.isExist()) {
                                MapleInventoryManipulator.addFromDrop(c, mpsi.getItem(), true);
                            }
                        }
                        chr.getMap().announce(UserCommon.MiniRoomBalloon.removeCharBox(c.getPlayer()));
                        shop.removeVisitors();
                    } else {
                        shop.removeVisitor(c.getPlayer());
                    }
                    chr.setPlayerShop(null);
                } else if (game != null) {
                    chr.setMiniGame(null);
                    if (game.isOwner(c.getPlayer())) {
                        chr.getMap().announce(UserCommon.MiniRoomBalloon.removeCharBox(c.getPlayer()));
                        game.broadcastToVisitor(PacketHelper.getMiniGameClose());
                    } else {
                        game.removeVisitor(c.getPlayer());
                    }
                } else if (merchant != null) {
                    merchant.removeVisitor(c.getPlayer());
                    chr.setHiredMerchant(null);
                }
            }
        } else if (mode == Action.OPEN.getCode()) {
            if (chr.isIronMan()) {
                c.announce(WvsContext.enableActions());
                return;
            }
            if (!FeatureSettings.HIRED_MERCHANTS) {
                chr.dropMessage(MessageType.POPUP, FeatureSettings.HIRED_MERCHANTS_DISABLED);
                c.announce(WvsContext.enableActions());
                return;
            }
            MaplePlayerShop shop = chr.getPlayerShop();
            HiredMerchant merchant = chr.getHiredMerchant();
            if (shop != null && shop.isOwner(c.getPlayer())) {
                slea.readByte();// 01
                chr.getMap().announce(UserCommon.MiniRoomBalloon.addCharBox(c.getPlayer(), 4));
            } else if (merchant != null && merchant.isOwner(c.getPlayer())) {
                chr.setHasMerchant(true);
                try {
                    merchant.saveItems();
                } catch (SQLException e) {
                    Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
                }
                merchant.setOpen(true);
                chr.getMap().addMapObject(merchant);
                chr.setHiredMerchant(null);
                chr.getMap().announce(EmployeePool.EmployeeEnterField(merchant));
                slea.readByte();
            }
        } else if (mode == Action.READY.getCode()) {
            MapleMiniGame game = chr.getMiniGame();
            if (game.getOwner().getId() != c.getPlayer().getId()) {
                game.setReady(true);
                game.broadcast(PacketHelper.getMiniGameReady(game));
            }
        } else if (mode == Action.UN_READY.getCode()) {
            MapleMiniGame game = chr.getMiniGame();
            if (game.getOwner().getId() != c.getPlayer().getId()) {
                game.setReady(false);
                game.broadcast(PacketHelper.getMiniGameUnReady(game));
            }
        } else if (mode == Action.START.getCode()) {
            MapleMiniGame game = chr.getMiniGame();
            if (game.getGameType().equals("omok") && game.isReady()) {
                game.setReady(false);
                game.broadcast(PacketHelper.getMiniGameStart(game, game.getCurrentTurn()));
                chr.getMap().announce(UserCommon.MiniRoomBalloon.addOmokBox(game.getOwner(), 2, 1));
            }
            if (game.getGameType().equals("matchcard")) {
                game.shuffleList();
                game.broadcast(PacketHelper.getMatchCardStart(game, game.getCurrentTurn()));
                chr.getMap().announce(UserCommon.MiniRoomBalloon.addMatchCardBox(game.getOwner(), 2, 1));
            }
        } else if (mode == Action.GIVE_UP.getCode()) {
            MapleMiniGame game = chr.getMiniGame();
            if (game.getGameType().equals("omok")) {
                game.setCurrentTurn(game.getCurrentTurn() == 1 ? 0 : 1);
                if (game.isOwner(c.getPlayer())) {
                    game.broadcast(PacketHelper.getMiniGameOwnerForfeit(game));
                } else {
                    game.broadcast(PacketHelper.getMiniGameVisitorForfeit(game));
                }
            }
            if (game.getGameType().equals("matchcard")) {
                if (game.isOwner(c.getPlayer())) {
                    game.broadcast(PacketHelper.getMatchCardVisitorWin(game));
                } else {
                    game.broadcast(PacketHelper.getMatchCardOwnerWin(game));
                }
            }
        } else if (mode == Action.REQUEST_TIE.getCode()) {
            MapleMiniGame game = chr.getMiniGame();
            if (game.isOwner(c.getPlayer())) {
                game.broadcastToVisitor(PacketHelper.getMiniGameRequestTie(game));
            } else {
                game.getOwner().getClient().announce(PacketHelper.getMiniGameRequestTie(game));
            }
        } else if (mode == Action.ANSWER_TIE.getCode()) {
            MapleMiniGame game = chr.getMiniGame();
            byte accepted = slea.readByte();
            if (accepted == 1) {
                if (game.getGameType().equals("omok")) {
                    game.broadcast(PacketHelper.getMiniGameTie(game));
                }
                if (game.getGameType().equals("matchcard")) {
                    game.broadcast(PacketHelper.getMatchCardTie(game));
                }
            } else {
                if (game.getGameType().equals("omok")) {
                    game.broadcast(PacketHelper.getMiniGameDenyTie(game));
                }
                if (game.getGameType().equals("matchcard")) {
                    game.broadcast(PacketHelper.getMiniGameDenyTie(game));
                }
            }
        } else if (mode == Action.SKIP.getCode()) {
            MapleMiniGame game = chr.getMiniGame();
            if (game.isOwner(c.getPlayer())) {
                game.broadcast(PacketHelper.getMiniGameSkipOwner(game));
            } else {
                game.broadcast(PacketHelper.getMiniGameSkipVisitor(game));
            }
        } else if (mode == Action.MOVE_OMOK.getCode()) {
            int x = slea.readInt(); // x point
            int y = slea.readInt(); // y point
            int type = slea.readByte(); // piece ( 1 or 2; Owner has one piece, visitor has another, it switches every game.)
            chr.getMiniGame().setPiece(x, y, type, c.getPlayer());
        } else if (mode == Action.SELECT_CARD.getCode()) {
            int turn = slea.readByte(); // 1st turn = 1; 2nd turn = 0
            int slot = slea.readByte(); // slot
            MapleMiniGame game = chr.getMiniGame();
            int firstslot = game.getFirstSlot();
            if (turn == 1) {
                game.setFirstSlot(slot);
                if (game.isOwner(c.getPlayer())) {
                    game.broadcastToVisitor(PacketHelper.getMatchCardSelect(game, turn, slot, firstslot, turn));
                } else {
                    game.getOwner().getClient().announce(PacketHelper.getMatchCardSelect(game, turn, slot, firstslot, turn));
                }
            } else if ((game.getCardId(firstslot + 1)) == (game.getCardId(slot + 1))) {
                if (game.isOwner(c.getPlayer())) {
                    game.broadcast(PacketHelper.getMatchCardSelect(game, turn, slot, firstslot, 2));
                    game.setOwnerPoints();
                } else {
                    game.broadcast(PacketHelper.getMatchCardSelect(game, turn, slot, firstslot, 3));
                    game.setVisitorPoints();
                }
            } else if (game.isOwner(c.getPlayer())) {
                game.broadcast(PacketHelper.getMatchCardSelect(game, turn, slot, firstslot, 0));
            } else {
                game.broadcast(PacketHelper.getMatchCardSelect(game, turn, slot, firstslot, 1));
            }
        } else if (mode == Action.SET_MESO.getCode()) {
            chr.getTrade().setMeso(slea.readInt());
        } else if (mode == Action.SET_ITEMS.getCode()) {
            ItemInformationProvider ii = ItemInformationProvider.getInstance();
            MapleInventoryType ivType = MapleInventoryType.getByType(slea.readByte());
            Item item = chr.getInventory(ivType).getItem(slea.readShort());
            short quantity = slea.readShort();
            byte targetSlot = slea.readByte();
            if (quantity < 1 || quantity > item.getQuantity()) {
                c.announce(WvsContext.enableActions());
                return;
            }
            if (!FeatureSettings.TRADE) {
                c.getPlayer().dropMessage(MessageType.POPUP, FeatureSettings.TRADE_DISABLED);
                c.announce(WvsContext.enableActions());
                return;
            }
            if (chr.getTrade() != null) {
                if ((quantity <= item.getQuantity() && quantity >= 0) || ItemConstants.isRechargable(item.getItemId())) {
                    if (ii.getItemData(item.getItemId()).isDropRestricted()) { // ensure that undroppable items do not make it to the trade window
                        if (!((item.getFlag() & ItemConstants.KARMA) == ItemConstants.KARMA || (item.getFlag() & ItemConstants.SPIKES) == ItemConstants.SPIKES)) {
                            c.announce(WvsContext.enableActions());
                            return;
                        }
                    }
                    Item tradeItem = item.copy();
                    if (ItemConstants.isRechargable(item.getItemId())) {
                        tradeItem.setQuantity(item.getQuantity());
                        if (tradeItem.getQuantity() == item.getQuantity()) {
                            tradeItem.nSN = item.nSN;
                        }
                        MapleInventoryManipulator.removeItem(c, ivType, item.getPosition(), item.getQuantity(), false, true);
                    } else {
                        tradeItem.setQuantity(quantity);
                        if (tradeItem.getQuantity() == item.getQuantity()) {
                            tradeItem.nSN = item.nSN;
                        }
                        MapleInventoryManipulator.removeItem(c, ivType, item.getPosition(), quantity, false, true);
                    }
                    tradeItem.setPosition(targetSlot);
                    chr.getTrade().addItem(tradeItem);
                }
            }
        } else if (mode == Action.CONFIRM.getCode()) {
            if (chr.isIronMan()) {
                c.announce(WvsContext.enableActions());
                return;
            }
            MapleTrade.completeTrade(c.getPlayer());
        } else if (mode == Action.ADD_ITEM.getCode() || mode == Action.PUT_ITEM.getCode()) {
            if (chr.isIronMan()) {
                c.announce(WvsContext.enableActions());
                return;
            }
            MapleInventoryType type = MapleInventoryType.getByType(slea.readByte());
            short slot = slea.readShort();
            short bundles = slea.readShort();
            short perBundle = slea.readShort();
            int price = slea.readInt();
            Item ivItem = chr.getInventory(type).getItem(slot);
            if (ivItem == null || ivItem.getQuantity() < bundles * perBundle || ivItem.getFlag() == ItemConstants.UNTRADEABLE) {
                return;
            }
            if (perBundle <= 0 || perBundle * bundles > 2000 || bundles <= 0 || price <= 0 || price > Integer.MAX_VALUE) {
                AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), c.getPlayer().getName() + " tried to packet edit with hired merchants.");
                return;
            }
            Item sellItem = ivItem.copy();
            if (ItemConstants.isRechargable(sellItem.getItemId())) {
                if (bundles != 1 || perBundle != 1) {
                    AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), c.getPlayer().getName() + " tried to packet edit bundle/perBundle amount with throwing stars");
                    bundles = 1;
                    perBundle = ivItem.getQuantity();
                }
                sellItem.nSN = ivItem.nSN;// have to put the whole stack of throwing stars in.
            } else {
                sellItem.setQuantity(bundles);
                if (sellItem.getQuantity() * perBundle == ivItem.getQuantity()) {
                    sellItem.nSN = ivItem.nSN;// Only if you are putting all of the stack in.
                }
            }
            MaplePlayerShopItem item = new MaplePlayerShopItem(sellItem, perBundle, price);
            MaplePlayerShop shop = chr.getPlayerShop();
            HiredMerchant merchant = chr.getHiredMerchant();
            if (shop != null && shop.isOwner(c.getPlayer())) {
                shop.addItem(item);
                c.announce(MiniRoomBaseDlg.getPlayerShopItemUpdate(shop));
            } else if (merchant != null && merchant.isOwner(c.getPlayer())) {
                merchant.addItem(item);
                c.announce(MiniRoomBaseDlg.EntrustedShop.updateHiredMerchant(merchant, c.getPlayer()));
                ItemFactory.updateItemOwner(c.getPlayer(), ivItem, ItemFactory.MERCHANT);
            }
            if (ItemConstants.isRechargable(sellItem.getItemId())) {
                MapleInventoryManipulator.removeItem(c, type, slot, sellItem.getQuantity(), false, true);
            } else {
                MapleInventoryManipulator.removeItem(c, type, slot, (short) (bundles * perBundle), false, true);
            }
        } else if (mode == Action.REMOVE_ITEM.getCode()) {
            if (chr.isIronMan()) {
                c.announce(WvsContext.enableActions());
                return;
            }
            MaplePlayerShop shop = chr.getPlayerShop();
            if (shop != null && shop.isOwner(c.getPlayer())) {
                int slot = slea.readShort();
                if (slot >= shop.getItems().size() || slot < 0) {
                    AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), c.getPlayer().getName() + " tried to packet edit with a player shop removing item from slot: " + slot);
                    c.disconnect(true, false);
                    return;
                }
                MaplePlayerShopItem item = shop.getItems().get(slot);
                Item ivItem = item.getItem().copy();
                ivItem.nSN = item.getItem().nSN;
                shop.removeItem(slot);
                ivItem.setQuantity(item.getItem().getPerBundle());
                MapleInventoryManipulator.addFromDrop(c, ivItem, false);
                ItemFactory.updateItemOwner(c.getPlayer(), ivItem, ItemFactory.INVENTORY);
                c.announce(MiniRoomBaseDlg.getPlayerShopItemUpdate(shop));
            }
        } else if (mode == Action.MERCHANT_MESO.getCode()) {// Hmmmm
            if (!FeatureSettings.HIRED_MERCHANTS) {
                chr.dropMessage(MessageType.POPUP, FeatureSettings.HIRED_MERCHANTS_DISABLED);
                c.announce(WvsContext.enableActions());
            }
            /*if (!chr.getHiredMerchant().isOwner(chr) || chr.getMerchantMeso() < 1) return;
			int possible = Integer.MAX_VALUE - chr.getMerchantMeso();
			if (possible > 0) {
			    if (possible < chr.getMerchantMeso()) {
			        chr.gainMeso(possible, false);
			        chr.setMerchantMeso(chr.getMerchantMeso() - possible);
			    } else {
			        chr.gainMeso(chr.getMerchantMeso(), false);
			        chr.setMerchantMeso(0);
			    }
			    c.announce(MaplePacketCreator.updateHiredMerchant(chr.getHiredMerchant(), chr));
			}*/
        } else if (mode == Action.MERCHANT_ORGANIZE.getCode()) {
            if (!FeatureSettings.HIRED_MERCHANTS) {
                chr.dropMessage(MessageType.POPUP, FeatureSettings.HIRED_MERCHANTS_DISABLED);
                c.announce(WvsContext.enableActions());
                return;
            }
            HiredMerchant merchant = chr.getHiredMerchant();
            if (!merchant.isOwner(chr)) {
                return;
            }
            if (chr.getMerchantMeso() > 0) {
                int possible = Integer.MAX_VALUE - chr.getMerchantMeso();
                if (possible > 0) {
                    if (possible < chr.getMerchantMeso()) {
                        chr.gainMeso(possible, false);
                        chr.setMerchantMeso(chr.getMerchantMeso() - possible);
                    } else {
                        chr.gainMeso(chr.getMerchantMeso(), false);
                        chr.setMerchantMeso(0);
                    }
                }
            }
            for (int i = 0; i < merchant.getItems().size(); i++) {
                if (!merchant.getItems().get(i).isExist()) {
                    merchant.removeFromSlot(i);
                }
            }
            if (merchant.getItems().isEmpty()) {
                c.announce(MiniRoomBaseDlg.EntrustedShop.hiredMerchantOwnerLeave());
                c.announce(MiniRoomBaseDlg.EntrustedShop.leaveHiredMerchant(0x00, 0x03));
                merchant.closeShop(c, false);
                chr.setHasMerchant(false);
                return;
            }
            c.announce(MiniRoomBaseDlg.EntrustedShop.updateHiredMerchant(merchant, chr));
        } else if (mode == Action.BUY.getCode() || mode == Action.MERCHANT_BUY.getCode()) {
            if (chr.isIronMan()) {
                c.announce(WvsContext.enableActions());
                return;
            }
            if (!FeatureSettings.HIRED_MERCHANTS) {
                chr.dropMessage(MessageType.POPUP, FeatureSettings.HIRED_MERCHANTS_DISABLED);
                c.announce(WvsContext.enableActions());
                return;
            }
            int item = slea.readByte();
            short quantity = slea.readShort();
            // System.out.println(String.format("Item: %d quantity: %d", item, quantity));
            if (quantity < 1) {
                AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), c.getPlayer().getName() + " tried to packet edit with a hired merchant and or player shop. Tired to buy item " + item + " with quantity " + quantity);
                c.disconnect(true, false);
                return;
            }
            MaplePlayerShop shop = chr.getPlayerShop();
            HiredMerchant merchant = chr.getHiredMerchant();
            if (merchant != null && merchant.getOwnerName().equals(chr.getName())) {
                return;
            }
            if (shop != null && shop.isVisitor(c.getPlayer())) {
                shop.buy(c, item, quantity);
                shop.broadcast(MiniRoomBaseDlg.getPlayerShopItemUpdate(shop));
            } else if (merchant != null) {
                merchant.buy(c, item, quantity);
                merchant.broadcastToVisitors(MiniRoomBaseDlg.EntrustedShop.updateHiredMerchant(merchant, c.getPlayer()));
            }
        } else if (mode == Action.TAKE_ITEM_BACK.getCode()) {
            if (!FeatureSettings.HIRED_MERCHANTS) {
                chr.dropMessage(MessageType.POPUP, FeatureSettings.HIRED_MERCHANTS_DISABLED);
                c.announce(WvsContext.enableActions());
                return;
            }
            HiredMerchant merchant = chr.getHiredMerchant();
            if (merchant != null && merchant.isOwner(c.getPlayer())) {
                int slot = slea.readShort();
                MaplePlayerShopItem item = merchant.getItems().get(slot);
                if (!chr.canHoldItem(item.getItem())) {
                    c.announce(WvsContext.enableActions());
                    return;
                }
                if (item.getItem().getPerBundle() > 0) {
                    Item iitem = item.getItem();
                    iitem.setQuantity((short) (item.getItem().getQuantity() * item.getItem().getPerBundle()));
                    iitem.setPerBundle((short) 1);
                    MapleInventoryManipulator.addFromDrop(c, iitem, true);
                    ItemFactory.updateItemOwner(c.getPlayer(), iitem, ItemFactory.INVENTORY);
                }
                merchant.removeFromSlot(slot);
                c.announce(MiniRoomBaseDlg.EntrustedShop.updateHiredMerchant(merchant, c.getPlayer()));
            }
        } else if (mode == Action.CLOSE_MERCHANT.getCode()) {
            HiredMerchant merchant = chr.getHiredMerchant();
            if (merchant != null && merchant.isOwner(c.getPlayer())) {
                c.announce(MiniRoomBaseDlg.EntrustedShop.hiredMerchantOwnerLeave());
                c.announce(MiniRoomBaseDlg.EntrustedShop.leaveHiredMerchant(0x00, 0x03));
                merchant.closeShop(c, false);
                chr.setHasMerchant(false);
            }
        } else if (mode == Action.MAINTENANCE_OFF.getCode()) {
            HiredMerchant merchant = chr.getHiredMerchant();
            if (merchant.getItems().isEmpty() && merchant.isOwner(c.getPlayer())) {
                merchant.closeShop(c, false);
                chr.setHasMerchant(false);
            }
            if (merchant != null && merchant.isOwner(c.getPlayer())) {
                merchant.getMessages().clear();
                try {
                    merchant.saveItems();
                } catch (SQLException e) {
                    Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
                }
                merchant.setOpen(true);
            }
            chr.setHiredMerchant(null);
            c.announce(WvsContext.enableActions());
        } else if (mode == Action.BAN_PLAYER.getCode()) {
            if (chr.getPlayerShop() != null && chr.getPlayerShop().isOwner(c.getPlayer())) {
                chr.getPlayerShop().banPlayer(slea.readMapleAsciiString());
            }
        } else if (mode == Action.KICK.getCode()) {
            MapleMiniGame game = chr.getMiniGame();
            if (game.isOwner(chr)) {
                MapleCharacter visitor = game.getVisitor();
                if (visitor != null) {
                    visitor.setMiniGame(null);
                    game.removeVisitor(visitor);
                    visitor.announce(PacketHelper.getMiniGameClose());
                }
            }
        }
    }
}
