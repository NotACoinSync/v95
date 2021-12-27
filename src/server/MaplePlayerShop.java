package server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.ItemFactory;
import constants.ItemConstants;
import net.SendOpcode;
import server.maps.objects.MapleMapObjectType;
import server.maps.objects.PlayerShop;
import tools.packets.PacketHelper;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.WvsContext;
import tools.packets.CField.MiniRoomBaseDlg;
import tools.packets.CField.userpool.UserCommon;

public class MaplePlayerShop extends PlayerShop {

    private MapleCharacter owner;
    private MapleCharacter[] visitors = new MapleCharacter[3];
    private List<MaplePlayerShopItem> items = new ArrayList<>();
    private MapleCharacter[] slot = {null, null, null};
    private String description;
    private int boughtnumber = 0;
    private List<String> bannedList = new ArrayList<>();

    public MaplePlayerShop(MapleCharacter owner, String description) {
        this.setPosition(owner.getPosition());
        this.owner = owner;
        this.description = description;
    }

    public boolean hasFreeSlot() {
        return visitors[0] == null || visitors[1] == null || visitors[2] == null;
    }

    @Override
    public int getFreeSlot() {
        for (int i = 0; i < 3; i++) {
            if (visitors[i] == null) {
                return i;
            }
        }
        return -1;
    }

    public boolean isOwner(MapleCharacter c) {
        return owner.equals(c);
    }

    public void addVisitor(MapleCharacter visitor) {
        for (int i = 0; i < 3; i++) {
            if (visitors[i] == null) {
                visitors[i] = visitor;
                if (this.getSlot(0) == null) {
                    this.setSlot(visitor, 0);
                    this.broadcast(MiniRoomBaseDlg.getPlayerShopNewVisitor(visitor, 1));
                } else if (this.getSlot(1) == null) {
                    this.setSlot(visitor, 1);
                    this.broadcast(MiniRoomBaseDlg.getPlayerShopNewVisitor(visitor, 2));
                } else if (this.getSlot(2) == null) {
                    this.setSlot(visitor, 2);
                    this.broadcast(MiniRoomBaseDlg.getPlayerShopNewVisitor(visitor, 3));
                    visitor.getMap().announce(UserCommon.MiniRoomBalloon.addCharBox(owner, 1));
                }
                break;
            }
        }
    }

    public void removeVisitor(MapleCharacter visitor) {
        if (visitor == owner) {
            owner.getMap().removeMapObject(this);
            owner.setPlayerShop(null);
        }
        for (int i = 0; i < 3; i++) {
            if (visitors[i] != null && visitors[i].getId() == visitor.getId()) {
                int slot_ = visitor.getSlot();
                visitors[i] = null;
                this.setSlot(null, i);
                visitor.setSlot(-1);
                this.broadcast(MiniRoomBaseDlg.getPlayerShopRemoveVisitor(slot_ + 1));
                return;
            }
        }
    }

    public boolean isVisitor(MapleCharacter visitor) {
        return visitors[0] == visitor || visitors[1] == visitor || visitors[2] == visitor;
    }

    public void addItem(MaplePlayerShopItem item) {
        items.add(item);
    }

    public void removeItem(int item) {
        items.remove(item);
    }

    /**
     * no warnings for now o.op
     *
     * @param c
     * @param item
     * @param quantity
     */
    public void buy(MapleClient c, int item, short quantity) {
        if (isVisitor(c.getPlayer())) {
            MaplePlayerShopItem pItem = items.get(item);
            Item newItem = pItem.getItem().copy();
            if ((newItem.getFlag() & ItemConstants.KARMA) == ItemConstants.KARMA) {
                newItem.setFlag((byte) (newItem.getFlag() ^ ItemConstants.KARMA));
            }
            if (newItem.getType() == 2 && (newItem.getFlag() & ItemConstants.SPIKES) == ItemConstants.SPIKES) {
                newItem.setFlag((byte) (newItem.getFlag() ^ ItemConstants.SPIKES));
            }
            if (quantity < 1 || pItem.getItem().getPerBundle() < 1 || !pItem.isExist() || pItem.getBundles() < quantity) {
                c.announce(WvsContext.enableActions());
                return;
            } else if (newItem.getType() == 1 && newItem.getQuantity() > 1) {
                c.announce(WvsContext.enableActions());
                return;
            } else if (!pItem.isExist()) {
                c.announce(WvsContext.enableActions());
                return;
            }
            if (newItem.getQuantity() == pItem.getPerBundle() * pItem.getBundles()) {
                newItem.nSN = pItem.getItem().nSN;
            }
            synchronized (c.getPlayer()) {
                if (c.getPlayer().getMeso() >= (long) pItem.getPrice() * quantity) {
                    if (c.getPlayer().canHoldItem(newItem)) {
                        c.getPlayer().gainMeso(-pItem.getPrice() * quantity, true);
                        owner.gainMeso(pItem.getPrice() * quantity, true);
                        MapleInventoryManipulator.addFromDrop(c, newItem, true);
                        ItemFactory.updateItemOwner(c.getPlayer(), newItem, ItemFactory.INVENTORY);
                        pItem.setBundles((short) (pItem.getBundles() - quantity));
                        if (pItem.getItem().getPerBundle() < 1) {
                            pItem.setDoesExist(false);
                            if (++boughtnumber == items.size()) {
                                owner.setPlayerShop(null);
                                owner.getMap().announce(UserCommon.MiniRoomBalloon.removeCharBox(owner));
                                this.removeVisitors();
                                owner.dropMessage(1, "Your items are sold out, and therefore your shop is closed.");
                            }
                        }
                    } else {
                        c.getPlayer().dropMessage(1, "Your inventory is full. Please open a slot before buying this item.");
                    }
                }
            }
        }
    }

    public void broadcastToVisitors(final byte[] packet) {
        for (int i = 0; i < 3; i++) {
            if (visitors[i] != null) {
                visitors[i].getClient().announce(packet);
            }
        }
    }

    public void removeVisitors() {
        try {
            for (int i = 0; i < 3; i++) {
                if (visitors[i] != null) {
                    visitors[i].getClient().announce(PacketHelper.shopErrorMessage(10, 1));
                    removeVisitor(visitors[i]);
                }
            }
        } catch (Exception e) {
            Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
        }
        if (owner != null) {
            removeVisitor(owner);
        }
    }

    public static byte[] shopErrorMessage(int error, int type) {
        MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        oPacket.write(0x0A);
        oPacket.write(type);
        oPacket.write(error);
        return oPacket.getPacket();
    }

    public void broadcast(final byte[] packet) {
        if (owner.getClient() != null && owner.getClient().getSession() != null) {
            owner.getClient().announce(packet);
        }
        broadcastToVisitors(packet);
    }

    public void chat(MapleClient c, String chat) {
        byte s = 0;
        for (MapleCharacter mc : getVisitors()) {
            s++;
            if (mc != null) {
                if (mc.getName().equalsIgnoreCase(c.getPlayer().getName())) {
                    break;
                }
            } else if (s == 3) {
                s = 0;
            }
        }
        broadcast(MiniRoomBaseDlg.getPlayerShopChat(c.getPlayer(), chat, s));
    }

    public void sendShop(MapleClient c) {
        c.announce(MiniRoomBaseDlg.PersonalShop.getPlayerShop(c, this, isOwner(c.getPlayer())));
    }

    @Override
    public String getOwnerName() {
        return owner.getName();
    }

    public MapleCharacter getOwner() {
        return owner;
    }

    public MapleCharacter[] getVisitors() {
        return visitors;
    }

    public MapleCharacter getSlot(int s) {
        return slot[s];
    }

    private void setSlot(MapleCharacter person, int s) {
        slot[s] = person;
        if (person != null) {
            person.setSlot(s);
        }
    }

    @Override
    public List<MaplePlayerShopItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void banPlayer(String name) {
        if (!bannedList.contains(name)) {
            bannedList.add(name);
        }
        for (int i = 0; i < 3; i++) {
            if (visitors[i] != null && visitors[i].getName().equals(name)) {
                visitors[i].getClient().announce(PacketHelper.shopErrorMessage(5, 1));
                removeVisitor(visitors[i]);
                return; // I'm guessing this was the intended action
            }
        }
    }

    public boolean isBanned(String name) {
        return bannedList.contains(name);
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.announce(UserCommon.MiniRoomBalloon.removeCharBox(owner));
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        client.announce(UserCommon.MiniRoomBalloon.addCharBox(owner, 4));
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.SHOP;
    }

    @Override
    public MaplePlayerShop clone() {
        return null;
    }

    @Override
    public int getOwnerId() {
        return owner.getId();
    }

    @Override
    public int getMapId() {
        return owner.getMapId();
    }

    @Override
    public int getChannel() {
        return owner.getClient().getChannel();
    }
}
