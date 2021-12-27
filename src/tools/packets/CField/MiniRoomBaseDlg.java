package tools.packets.CField;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import constants.ItemConstants;
import java.util.List;
import net.SendOpcode;
import net.server.handlers.Interaction.PlayerInteractionHandler;
import server.MaplePlayerShop;
import server.MaplePlayerShopItem;
import server.MapleTrade;
import server.maps.objects.HiredMerchant;
import server.maps.objects.HiredMerchant.SoldItem;
import tools.data.output.LittleEndianWriter;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.packets.PacketHelper;

public class MiniRoomBaseDlg {

    public class MiniRoomType {

        public static final int OMOK = 1, MEMORY_GAME = 2, TRADE = 3, PERSONAL_SHOP = 4, ENTRUSTED_SHOP = 5, CASH_TRADING = 6;
    }

    public static void addAvatar(LittleEndianWriter lew, int index, MapleCharacter chr) {// CMiniRoomBaseDlg::DecodeAvatar
        lew.write(index);
        PacketHelper.encodeAvatarLook(lew, chr, false);
        lew.writeMapleAsciiString(chr.getName());
        lew.writeShort(chr.getJob().getId());
    }

    public static class Omok {
        //
    }

    public static class MemoryGame {
        //
    }

    public static class Trade {

        public static byte[] getTradeStart(MapleClient c, MapleTrade trade, byte number) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
            oPacket.write(PlayerInteractionHandler.Action.ROOM.getCode());
            oPacket.write(MiniRoomType.TRADE);
            oPacket.write(2);// maxUsers
            oPacket.write(number);// myPosition
            if (number == 1) {
                addAvatar(oPacket, 0, trade.getPartner().getChr());
            }
            addAvatar(oPacket, number, c.getPlayer());
            oPacket.write(-1);// Client does a loop looking for index to add more avatars, -1 ends the loop.
            return oPacket.getPacket();
        }

        public static byte[] getTradeConfirmation() {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
            oPacket.write(PlayerInteractionHandler.Action.CONFIRM.getCode());
            return oPacket.getPacket();
        }

        public static byte[] getTradeCompletion(byte number) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
            oPacket.write(PlayerInteractionHandler.Action.EXIT.getCode());
            oPacket.write(number);
            oPacket.write(6);
            return oPacket.getPacket();
        }

        public static byte[] getTradeCancel(byte number) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
            oPacket.write(PlayerInteractionHandler.Action.EXIT.getCode());
            oPacket.write(number);
            oPacket.write(2);
            return oPacket.getPacket();
        }

        public static byte[] getTradeChat(MapleCharacter c, String chat, boolean owner) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
            oPacket.write(PlayerInteractionHandler.Action.CHAT.getCode());
            oPacket.write(PlayerInteractionHandler.Action.CHAT_THING.getCode());
            oPacket.write(owner ? 0 : 1);
            oPacket.writeMapleAsciiString(c.getName() + " : " + chat);
            return oPacket.getPacket();
        }

        public static byte[] getTradePartnerAdd(MapleCharacter c) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
            oPacket.write(PlayerInteractionHandler.Action.VISIT.getCode());
            addAvatar(oPacket, 1, c);
            return oPacket.getPacket();
        }

        public static byte[] getTradeInvite(MapleCharacter c) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
            oPacket.write(PlayerInteractionHandler.Action.INVITE.getCode());
            oPacket.write(MiniRoomType.TRADE);
            oPacket.writeMapleAsciiString(c.getName());
            oPacket.write(new byte[]{(byte) 0xB7, (byte) 0x50, 0, 0});// SN for CMiniRoomBaseDlg::SendInviteResult, just sent back
            return oPacket.getPacket();
        }

        public static byte[] getTradeMesoSet(byte number, int meso) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
            oPacket.write(PlayerInteractionHandler.Action.SET_MESO.getCode());
            oPacket.write(number);
            oPacket.writeInt(meso);
            return oPacket.getPacket();
        }

        public static byte[] getTradeItemAdd(byte number, Item item) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
            oPacket.write(PlayerInteractionHandler.Action.SET_ITEMS.getCode());
            oPacket.write(number);
            oPacket.write(item.getPosition());
            PacketHelper.encodeItemSlotBase(oPacket, item, true);
            return oPacket.getPacket();
        }
    }

    public static class PersonalShop {

        /**
         @param c
         @param shop
         @param owner

         @return
         */
        public static byte[] getPlayerShop(MapleClient c, MaplePlayerShop shop, boolean owner) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
            oPacket.write(PlayerInteractionHandler.Action.ROOM.getCode());
            oPacket.write(MiniRoomType.PERSONAL_SHOP);
            oPacket.write(4);
            oPacket.write(owner ? 0 : 1);
            /*
             oPacket.write(0);
             encodeAvatarLook(oPacket, shop.getOwner(), false);
             oPacket.writeMapleAsciiString(shop.getOwner().getName());
             oPacket.write(1);
             encodeAvatarLook(oPacket, shop.getOwner(), false);
             oPacket.writeMapleAsciiString(shop.getOwner().getName());
             */
            addAvatar(oPacket, 0, shop.getOwner());
            addAvatar(oPacket, 1, shop.getOwner());
            oPacket.write(-1);
            oPacket.writeMapleAsciiString(shop.getDescription());
            List<MaplePlayerShopItem> items = shop.getItems();
            oPacket.write(0x10);// slot max
            oPacket.write(items.size());
            for (MaplePlayerShopItem item : items) {
                oPacket.writeShort(item.getItem().getPerBundle());
                oPacket.writeShort(item.getItem().getQuantity());
                oPacket.writeInt(item.getPrice());
                PacketHelper.encodeItemSlotBase(oPacket, item.getItem(), true);
            }
            return oPacket.getPacket();
        }
    }

    public static class EntrustedShop {

        /*
         Possible things for ENTRUSTED_SHOP_CHECK_RESULT
         0x0E = 00 = Renaming Failed - Can't find the merchant, 01 = Renaming
         succesful
         0x10 = Changes channel to the store (Store is open at Channel 1, do you
         want to change channels?)
         0x11 = You cannot sell any items when managing.. blabla
         0x12 = FKING POPUP LOL
         */
        public static byte[] getHiredMerchant(MapleCharacter chr, HiredMerchant hm, boolean firstTime) {// Thanks Dustin
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
            oPacket.write(PlayerInteractionHandler.Action.ROOM.getCode());
            oPacket.write(MiniRoomType.ENTRUSTED_SHOP);
            oPacket.write(4);
            oPacket.writeShort(hm.getVisitorSlot(chr) + 1);
            oPacket.writeInt(hm.getItemId());
            oPacket.writeMapleAsciiString("Hired Merchant");// hm.getDescription()???
            for (int i = 0; i < 3; i++) {
                if (hm.getVisitors()[i] != null) {
                    addAvatar(oPacket, i + 1, hm.getVisitors()[i]);
                }
            }
            oPacket.write(-1);
            // CEntrustedShopDlg::OnEnterResult
            if (hm.isOwner(chr)) {
                oPacket.writeShort(hm.getMessages().size());
                for (int i = 0; i < hm.getMessages().size(); i++) {
                    oPacket.writeMapleAsciiString(hm.getMessages().get(i).getLeft());
                    oPacket.write(hm.getMessages().get(i).getRight());
                }
            } else {
                oPacket.writeShort(0);
            }
            oPacket.writeMapleAsciiString(hm.getOwnerName());
            if (hm.isOwner(chr)) {
                oPacket.writeInt(hm.getTimeLeft());// m_tPass
                oPacket.write(firstTime ? 1 : 0);
                List<SoldItem> sold = hm.getSold();
                oPacket.write(sold.size());
                for (SoldItem s : sold) {
                    oPacket.writeInt(s.getItemId());
                    oPacket.writeShort(s.getQuantity());
                    oPacket.writeInt(s.getMesos());
                    oPacket.writeMapleAsciiString(s.getBuyer());
                }
                oPacket.writeLong(chr.getMerchantMeso());
            }
            oPacket.writeMapleAsciiString(hm.getDescription());// sTitle
            oPacket.write(0x10); // TODO SLOTS, which is 16 for most stores...slotMax
            // Goes to some update method or something.
            oPacket.writeInt(chr.getMeso());
            oPacket.write(hm.getItems().size());
            if (hm.getItems().isEmpty()) {
                oPacket.write(0);// Hmm??
            } else {
                for (MaplePlayerShopItem item : hm.getItems()) {
                    oPacket.writeShort(item.getBundles() + (ItemConstants.isRechargable(item.getItem().getItemId()) && item.getItem().getQuantity() == 0 ? 1 : 0));
                    oPacket.writeShort(item.getPerBundle());
                    oPacket.writeInt(item.getPrice());
                    PacketHelper.encodeItemSlotBase(oPacket, item.getItem(), true);
                }
            }
            return oPacket.getPacket();
        }

        public static byte[] updateHiredMerchant(HiredMerchant hm, MapleCharacter chr) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
            oPacket.write(PlayerInteractionHandler.Action.UPDATE_MERCHANT.getCode());
            oPacket.writeInt(chr.getMeso());
            oPacket.write(hm.getItems().size());
            for (MaplePlayerShopItem item : hm.getItems()) {
                oPacket.writeShort(item.getBundles() + (ItemConstants.isRechargable(item.getItem().getItemId()) && item.getItem().getQuantity() == 0 ? 1 : 0));
                oPacket.writeShort(item.getPerBundle());
                oPacket.writeInt(item.getPrice());
                PacketHelper.encodeItemSlotBase(oPacket, item.getItem(), true);
            }
            return oPacket.getPacket();
        }

        public static byte[] hiredMerchantChat(String message, byte slot) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
            oPacket.write(PlayerInteractionHandler.Action.CHAT.getCode());
            oPacket.write(PlayerInteractionHandler.Action.CHAT_THING.getCode());
            oPacket.write(slot);
            oPacket.writeMapleAsciiString(message);
            return oPacket.getPacket();
        }

        public static byte[] hiredMerchantVisitorLeave(int slot) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
            oPacket.write(PlayerInteractionHandler.Action.EXIT.getCode());
            if (slot != 0) {
                oPacket.write(slot);
            }
            return oPacket.getPacket();
        }

        public static byte[] hiredMerchantOwnerLeave() {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
            oPacket.write(PlayerInteractionHandler.Action.REAL_CLOSE_MERCHANT.getCode());
            oPacket.write(0);
            return oPacket.getPacket();
        }

        public static byte[] leaveHiredMerchant(int slot, int status2) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
            oPacket.write(PlayerInteractionHandler.Action.EXIT.getCode());
            oPacket.write(slot);
            oPacket.write(status2);
            return oPacket.getPacket();
        }

        public static byte[] hiredMerchantVisitorAdd(MapleCharacter chr, int slot) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
            oPacket.write(PlayerInteractionHandler.Action.VISIT.getCode());
            addAvatar(oPacket, slot, chr);
            // oPacket.write(slot);
            // encodeAvatarLook(oPacket, chr, false);
            // oPacket.writeMapleAsciiString(chr.getName());
            return oPacket.getPacket();
        }
    }

    // idk where these belong
    public static byte[] getPlayerShopChat(MapleCharacter c, String chat, boolean owner) {// used in MapleMiniGame
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        oPacket.write(PlayerInteractionHandler.Action.CHAT.getCode());
        oPacket.write(PlayerInteractionHandler.Action.CHAT_THING.getCode());
        oPacket.write(owner ? 0 : 1);
        oPacket.writeMapleAsciiString(c.getName() + " : " + chat);
        return oPacket.getPacket();
    }

    public static byte[] getPlayerShopNewVisitor(MapleCharacter c, int slot) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        oPacket.write(PlayerInteractionHandler.Action.VISIT.getCode());
        oPacket.write(slot);
        PacketHelper.encodeAvatarLook(oPacket, c, false);
        oPacket.writeMapleAsciiString(c.getName());
        return oPacket.getPacket();
    }

    public static byte[] getPlayerShopRemoveVisitor(int slot) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        oPacket.write(PlayerInteractionHandler.Action.EXIT.getCode());
        if (slot > 0) {
            oPacket.write(slot);
        }
        return oPacket.getPacket();
    }

    public static byte[] getPlayerShopItemUpdate(MaplePlayerShop shop) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        oPacket.write(PlayerInteractionHandler.Action.UPDATE_MERCHANT.getCode());
        oPacket.write(shop.getItems().size());
        for (MaplePlayerShopItem item : shop.getItems()) {
            oPacket.writeShort(item.getItem().getPerBundle());
            oPacket.writeShort(item.getItem().getQuantity());
            oPacket.writeInt(item.getPrice());
            PacketHelper.encodeItemSlotBase(oPacket, item.getItem(), true);
        }
        return oPacket.getPacket();
    }

    public static byte[] getPlayerShopChat(MapleCharacter c, String chat, byte slot) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        oPacket.write(PlayerInteractionHandler.Action.CHAT.getCode());
        oPacket.write(PlayerInteractionHandler.Action.CHAT_THING.getCode());
        oPacket.write(slot);
        oPacket.writeMapleAsciiString(c.getName() + " : " + chat);
        return oPacket.getPacket();
    }
}
