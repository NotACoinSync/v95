package net.server.handlers.Item;

import java.rmi.RemoteException;
import java.util.List;

import client.MapleClient;
import client.autoban.AutobanFactory;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.ItemConstants;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import server.ItemInformationProvider;
import server.ItemInformationProvider.RewardItem;
import server.MapleInventoryManipulator;
import tools.Pair;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.WvsContext;
import tools.packets.EffectPacket;

public final class ItemRewardHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {// if Updated, UseCashItem itemType == 553
        byte slot = (byte) slea.readShort();
        int itemId = slea.readInt(); // will load from xml I don't care.
        Item itemSel = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);
        if (c.getPlayer().getInventory(MapleInventoryType.USE).countById(itemId) < 1) {
            AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Doesn't have reward item.");
            return;
        }
        if (itemSel == null || itemSel.getItemId() != itemId) {
            AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Item is null or item ids don't match.");
            return;
        }
        ItemInformationProvider ii = ItemInformationProvider.getInstance();

        Pair<Integer, List<RewardItem>> rewards = ii.getItemData(itemId).rewardItems;
        if (rewards != null) {
            for (RewardItem reward : rewards.getRight()) {
                if (!c.getPlayer().canHoldItem(new Item(reward.itemid, reward.quantity))) {
                    c.announce(WvsContext.OnMessage.getShowInventoryFull());
                    break;
                }
                if (Randomizer.nextInt(rewards.getLeft()) < reward.prob) {// Is it even possible to get an item with prob 1?
                    if (ItemConstants.getInventoryType(reward.itemid) == MapleInventoryType.EQUIP) {
                        final Item item = ii.getEquipById(reward.itemid);
                        if (reward.period != -1) {
                            item.setExpiration(System.currentTimeMillis() + (reward.period * 60 * 60 * 10));
                        }
                        MapleInventoryManipulator.addFromDrop(c, item, false);
                    } else {
                        MapleInventoryManipulator.addFromDrop(c, new Item(reward.itemid, reward.quantity), false);
                    }
                    MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, false);
                    if (reward.worldmsg != null) {
                        String msg = reward.worldmsg;
                        msg.replaceAll("/name", c.getPlayer().getName());
                        msg.replaceAll("/item", ii.getItemData(reward.itemid).name);
                        try {
                            ChannelServer.getInstance().getWorldInterface().broadcastPacket(WvsContext.BroadcastMsg.encode(6, msg));
                        } catch (RemoteException | NullPointerException ex) {
                            Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
                        }
                    }
                    if (reward.effect != null) {
                        c.announce(EffectPacket.Local.ShowEffectByPath(reward.effect));
                        c.getPlayer().getMap().announce(EffectPacket.Remote.ShowEffectByPath(c.getPlayer().getId(), reward.effect));
                    }
                    break;
                }
            }
        } else {
            if (ItemConstants.getInventoryType(itemId) == MapleInventoryType.EQUIP) {
                final Item item = ii.getEquipById(itemId);
                MapleInventoryManipulator.addFromDrop(c, item, false);
            } else {
                MapleInventoryManipulator.addFromDrop(c, new Item(itemId, (short) 1), false);
            }
        }
        c.announce(WvsContext.enableActions());
    }
}
