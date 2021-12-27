package net.server.handlers.Engage;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import net.server.Server;
import server.MapleInventoryManipulator;
import server.MapleWedding;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.WvsContext;

public final class MarriageActionHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        byte mode = slea.readByte();
        MapleCharacter player = c.getPlayer();
        switch (mode) {
            case 0: // Send
                sendRequest(c, slea.readMapleAsciiString(), slea.readInt());
                break;
            case 1: // Cancel proposal
                Logger.log(LogType.INFO, LogFile.GENERAL_INFO, "Cancel Ring Action: " + slea.toString()); // log packet and see if any bytes
                player.setEngagementRingID(0);// Other person denied, remove his engagement ring id.
                // 89 00 01
                break;
            case 2:// Accept/Deny Proposal
                // c = person proposed to
                // proposed = person who proposed to c.
                final boolean accepted = slea.readByte() > 0;
                String proposed = slea.readMapleAsciiString();
                final int id = slea.readInt();
                final MapleCharacter proposedChr = c.getChannelServer().getPlayerStorage().getCharacterByName(proposed);
                if (player.getMarriedTo() > 0 || proposedChr == null || proposedChr.getId() != id || proposedChr.getEngagementRingID() <= 0 || !proposedChr.haveItem(proposedChr.getEngagementRingID()) || proposedChr.getMarriedTo() > 0 || !proposedChr.isAlive() || proposedChr.getEventInstance() != null || !player.isAlive() || player.getEventInstance() != null) {
                    c.announce(WvsContext.enableActions());
                    return;
                }
                if (accepted) {
                    final int itemid = proposedChr.getEngagementRingID();
                    final int ringbox = itemid == 4031358 ? 2240000 : itemid == 4031360 ? 2240001 : itemid == 4031362 ? 2240002 : 2240003;
                    if (!c.getPlayer().canHoldItem(new Item(itemid, (short) 1)) || !proposedChr.canHoldItem(new Item(itemid, (short) 1))) {
                        c.announce(WvsContext.enableActions());
                        return;
                    }
                    try {
                        MapleWedding wedding = new MapleWedding(id, player.getId());
                        int marriageid = wedding.insertToDB();
                        MapleInventoryManipulator.removeById(proposedChr.getClient(), MapleInventoryType.USE, ringbox, 1, true, false);// Removes engagement ring box
                        proposedChr.setMarriedTo(player.getId()); // engage them
                        player.setMarriedTo(proposedChr.getId());
                        player.setEngagementRingID(itemid);
                        player.setMarriageID(marriageid);
                        proposedChr.setMarriageID(marriageid);
                        MapleInventoryManipulator.addFromDrop(c, new Item((itemid + 1), (short) 1), false);// Give ring to person
                        MapleInventoryManipulator.addFromDrop(proposedChr.getClient(), new Item((itemid + 1), (short) 1), false);// Give ring to person
                        MapleInventoryManipulator.addFromDrop(proposedChr.getClient(), new Item(itemid, (short) 1), false);// Adds empty engagement ring box
                        proposedChr.getClient().announce(WvsContext.MarriageResult.Result());
                        c.announce(WvsContext.MarriageResult.Result());
                        proposedChr.getClient().announce(WvsContext.MarriageResult.Done(proposedChr, false, marriageid));
                        c.announce(WvsContext.MarriageResult.Done(proposedChr, false, marriageid));
                        Server.getInstance().addWedding(wedding);
                    } catch (Exception e) {
                        Logger.log(LogType.ERROR, LogFile.EXCEPTION, e, "Error with engagement ");
                    }
                } else {
                    proposedChr.setEngagementRingID(0);
                    proposedChr.dropMessage(1, "She has politely declined your engagement request.");
                }
            case 3: // Drop Ring
                // 89 00 03 7E 83 3D 00
                /*
                 if(player.getEngagementRingID() > 0){
                 Item item =
                 c.getPlayer().getInventory(MapleInventoryType.ETC).findById(c.getPlayer().getEngagementRingID());
                 if(item != null){
                 c.getPlayer().getInventory(MapleInventoryType.ETC).removeItem(item.getPosition());
                 player.setMarriedTo(0);
                 player.setEngagementRingID(0);
                 }
                 }
                 */
                // Need to reset marriage id.
                // set maplewedding status to -1
                // remove engagementring/wedding ring
                // set those variables to 0
                // set marriedto to 0
                Logger.log(LogType.INFO, LogFile.GENERAL_INFO, "DropRing: " + slea.toString());// Check if any data.
                break;
            case 5:// Invite %s to Wedding
                String name = slea.readMapleAsciiString();
                int marriageID = slea.readInt();
                int slot = slea.readInt();
                int type = slea.readByte(); // TODO
                Item invitation = c.getPlayer().getInventory(MapleInventoryType.ETC).getItem((short) slot);
                if (invitation == null || (invitation.getItemId() != 4031377 && invitation.getItemId() != 4031395)) {
                    return;
                }
                String groom = c.getPlayer().getName(),
                 bride = MapleCharacter.getNameById(c.getPlayer().getMarriedTo());
                int guest = MapleCharacter.getIdByName(name);
                if (groom == null || bride == null || groom.equals(bride) || groom.equals("") || groom.equals("nobody") || bride.equals("") || bride.equals("nobody") || guest == 0) {
                    c.getPlayer().dropMessage(1, "Unable to find " + name + "! Are they online?");
                    c.announce(WvsContext.enableActions());
                    return;
                }
                MapleWedding wedding = Server.getInstance().getWeddingByID(marriageID);
                if (wedding == null) {
                    c.getPlayer().dropMessage(1, "Unable to find the wedding!");
                    c.announce(WvsContext.enableActions());
                    return;
                }
                MapleCharacter chr = ChannelServer.getInstance().getCharacterById(guest);
                if (chr != null) {
                    chr.getClient().announce(WvsContext.MarriageResult.Invite(groom, bride, type));
                    wedding.getInvited().add(guest);
                    MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, invitation.getItemId(), 1, true, false);
                    MapleInventoryManipulator.addFromDrop(chr.getClient(), new Item(wedding.isCathedral() ? 4031407 : 4031406, (short) 1), false);
                } else {
                    c.getPlayer().dropMessage(1, "Unable to find " + name + "! Are they online?");
                    c.announce(WvsContext.enableActions());
                    return;
                }
                break;
            case 6: // Open Wedding Invitation
                slot = slea.readInt();
                int invitationItemID = slea.readInt();
                invitation = c.getPlayer().getInventory(MapleInventoryType.ETC).getItem((short) slot);
                if (invitation == null || (invitation.getItemId() != 4031406 && invitation.getItemId() != 4031407)) {
                    return;
                }
                if (invitationItemID == invitation.getItemId()) {
                    return;
                }
                boolean cathedral = invitationItemID == 4031407;
                wedding = Server.getInstance().getWeddingByID(cathedral ? c.getChannelServer().getCurrentCathedralMarriageID() : c.getChannelServer().getCurrentChapelMarriageID());
                if (wedding != null) {
                    if (wedding.getStatus() == 1) {
                        // TODO: Warp them
                        c.announce(WvsContext.enableActions());
                        return;
                    } else {
                        // TODO: Give them more info(sendWeddingInvitation)
                        c.announce(WvsContext.enableActions());
                        return;
                    }
                }
                break;
            case 9: // Groom and Bride's Wishlist
                // TODO:
                Logger.log(LogType.INFO, LogFile.GENERAL_INFO, "Wishlist: " + slea.toString());
                int amount = slea.readShort();
                if (amount > 10) {
                    amount = 10;
                }
                String[] items = new String[10];
                for (int i = 0; i < amount; i++) {
                    items[i] = slea.readMapleAsciiString();
                }
                c.announce(WvsContext.MarriageRequest("", 0, true));
                break;
            default:
                Logger.log(LogType.INFO, LogFile.GENERAL_INFO, "Unhandled RingAction mode: " + mode);
                Logger.log(LogType.INFO, LogFile.GENERAL_INFO, slea.toString());// Check if any data.
                break;
        }
    }

    public void sendRequest(MapleClient c, String target, int itemid) {
        MapleCharacter player = c.getPlayer();
        final int newItemId = itemid == 2240000 ? 4031357 : (itemid == 2240001 ? 4031359 : (itemid == 2240002 ? 4031361 : (itemid == 2240003 ? 4031363 : (1112300 + (itemid - 2240004)))));
        final MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterByName(target);
        // TODO: get the correct packet bytes for these popups
        if (player.getMarriedTo() > 0) {
            player.dropMessage(1, "You're already married!");
            c.announce(WvsContext.enableActions());
            return;
        } else if (chr == null) {
            player.dropMessage(1, "Unable to find " + target);
            c.announce(WvsContext.enableActions());
            return;
        } else if (chr == player) {
            player.dropMessage(1, "You can't engage yourself.");
            c.announce(WvsContext.enableActions());
            return;
        } else if (chr.getMapId() != player.getMapId()) {
            player.dropMessage(1, "Make sure your partner is on the same map!");
            c.announce(WvsContext.enableActions());
            return;
        } else if (!player.haveItem(itemid) || itemid < 2240000 || itemid > 2240015) {
            c.announce(WvsContext.enableActions());
            return;
        } else if (chr.getMarriedTo() > 0 || chr.getMarriageRingID() > 0 || chr.getEngagementRingID() > 0) {
            player.dropMessage(1, "The player is already married!");
            c.announce(WvsContext.enableActions());
            return;
        } else if (!c.getPlayer().canHoldItem(new Item(newItemId, (short) 1))) {
            c.announce(WvsContext.enableActions());
            return;
        } else if (!chr.canHoldItem(new Item(newItemId, (short) 1))) {
            c.announce(WvsContext.enableActions());
            return;
        }
        player.setEngagementRingID(newItemId);
        chr.getClient().announce(WvsContext.MarriageRequest(player.getName(), player.getId(), false));
    }
}
