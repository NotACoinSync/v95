package net.server.channel.handlers;

import client.MapleCharacter;
import client.MapleClient;
import client.MessageType;
import client.autoban.AutobanFactory;
import client.inventory.*;
import constants.FeatureSettings;
import constants.ItemConstants;
import net.AbstractMaplePacketHandler;
import server.ItemData;
import server.ItemInformationProvider;
import server.MapleInventoryManipulator;
import server.MapleStorage;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CField.TrunkDlg;
import tools.packets.WvsContext;

public final class SendTrunkDlgRequest extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        byte mode = slea.readByte();
        if (chr.isIronMan()) {
            c.announce(WvsContext.enableActions());
            return;
        }
        if (!FeatureSettings.STORAGE) {
            chr.dropMessage(MessageType.POPUP, FeatureSettings.STORAGE_DISABLED);
            c.announce(WvsContext.enableActions());
            return;
        }
        final MapleStorage storage = chr.getStorage();
        switch (mode) {
            case TrunkDlg.getItemOutTrunk: {
                byte type = slea.readByte();
                byte POS = slea.readByte();

                if (POS < 0 || POS > storage.getSlots()) {
                    AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), c.getPlayer().getName() + " tried to packet edit with storage.");
                    c.disconnect(true, false);
                    return;
                }

                POS = storage.getSlot(MapleInventoryType.getByType(type), POS);
                Item item = storage.getItem(POS);

                if (item != null) {
                    ItemData data = ItemInformationProvider.getInstance().getItemData(item.getItemId());
                    if (data.pickupRestricted && chr.getItemQuantity(item.getItemId(), true) > 0) {
                        c.announce(TrunkDlg.encode(TrunkDlg.CouldNotRetrieved));
                        return;
                    }
                    if (chr.getMap().getId() == 910000000) {
                        if (chr.getMeso() < 1000) {
                            c.announce(TrunkDlg.encode(TrunkDlg.NotEnoughMesos_Get));
                            return;
                        } else {
                            chr.gainMeso(-1000, false);
                        }
                    }
                    if (c.getPlayer().canHoldItem(item)) {
                        Logger.log(LogType.INFO, LogFile.STORAGE, c.getAccountName(), c.getPlayer().getName() + " took out " + item.getQuantity() + " " + data.name + " (" + item.getItemId() + ")");
                        if ((item.getFlag() & ItemConstants.KARMA) == ItemConstants.KARMA) {
                            item.setFlag((byte) (item.getFlag() ^ ItemConstants.KARMA)); // items with scissors of karma used on them are reset once traded
                        } else if (item.getType() == 2 && (item.getFlag() & ItemConstants.SPIKES) == ItemConstants.SPIKES) {
                            item.setFlag((byte) (item.getFlag() ^ ItemConstants.SPIKES));
                        }
                        item = storage.takeOut(POS);
                        ItemFactory.updateItemOwner(c.getPlayer(), item, ItemFactory.INVENTORY);
                        MapleInventoryManipulator.addFromDrop(c, item, false);
                        storage.sendGetItem(c, ii.getInventoryType(item.getItemId()));
                        String itemName = ItemInformationProvider.getInstance().getItemData(item.getItemId()).name;
                        Logger.log(LogType.INFO, LogFile.STORAGE, c.getAccountName(), c.getPlayer().getName() + " stored " + item.getQuantity() + " " + itemName + " (" + item.getItemId() + ")");
                    } else {
                        c.announce(TrunkDlg.encode(TrunkDlg.InventoryFull));
                    }
                } else {
                    c.announce(TrunkDlg.encode(false, "Due to an error, the trade did not happen."));
                }
                break;
            }
            case TrunkDlg.putItemInTrunk: {
                short POS = slea.readShort();
                int ItemID = slea.readInt();
                short Count = slea.readShort();
                MapleInventoryType slotType = ii.getInventoryType(ItemID);
                MapleInventory Inv = chr.getInventory(slotType);
                if (POS < 1 || POS > Inv.getSlotLimit()) { // player inv starts at one
                    AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), c.getPlayer().getName() + " tried to packet edit with storage.");
                    c.disconnect(true, false);
                    return;
                }
                if (Count < 1 || chr.getItemQuantity(ItemID, false) < Count) {
                    return;
                }
                MapleInventoryType type = ii.getInventoryType(ItemID);
                Item item = chr.getInventory(type).getItem(POS);
                short Fee = (short) (chr.getMap().getId() == 910000000 ? -500 : -100);
                if (chr.getMeso() < Fee) {
                    c.announce(TrunkDlg.encode(TrunkDlg.NotEnoughMesos_Put));
                    return;
                }
                if (item != null) {
                    if (!storage.hasRoom(c, item)) {
                        c.announce(TrunkDlg.encode(TrunkDlg.StorageFull));
                        return;
                    }
                    Item itemClone = item.copy();
                    if (item.getItemId() == ItemID && (item.getQuantity() >= Count || ItemConstants.isRechargable(ItemID))) {
                        if (ItemConstants.isRechargable(ItemID)) {
                            Count = item.getQuantity();
                        }
                        chr.gainMeso(Fee, false, true, false);
                        item.setQuantity(Count);
                        if (item.getQuantity() == itemClone.getQuantity()) {
                            item.nSN = itemClone.nSN;
                            ItemFactory.updateItemOwner(c.getPlayer(), item, ItemFactory.STORAGE);
                        } else {
                            itemClone.addDBFlag(ItemDB.UPDATE);
                        }
                        MapleInventoryManipulator.removeItem(c, type, POS, Count, false, false);
                        storage.store(c, item);
                        storage.sendPutItem(c, ii.getInventoryType(ItemID));
                        String itemName = ItemInformationProvider.getInstance().getItemData(item.getItemId()).name;
                        Logger.log(LogType.INFO, LogFile.STORAGE, c.getAccountName(), c.getPlayer().getName() + " stored " + item.getQuantity() + " " + itemName + " (" + item.getItemId() + ")");
                    }
                } else {
                    c.announce(TrunkDlg.encode(false, "Due to an error, the trade did not happen."));
                }
                break;
            }
            case TrunkDlg.sortItemInTrunk: {
                c.announce(TrunkDlg.encode(TrunkDlg.Error_CannotBeMoved));
                break;
            }
            case TrunkDlg.moneyInTrunk: {
                int Money = slea.readInt();
                int Trunk_Money = storage.getMeso();
                int Player_Money = chr.getMeso();
                boolean putInStorage = false;
                if ((Money > 0 && Trunk_Money >= Money) || (Money < 0 && Player_Money >= -Money)) {
                    if (Money < 0 && (Trunk_Money - Money) < 0) { // SendPutMoneyRequest
                        Money = -2147483648 + Trunk_Money;
                        if (Money < Player_Money) {
                            c.announce(TrunkDlg.encode(TrunkDlg.NotEnoughMesos_Put));
                            return;
                        }
                        putInStorage = true;
                    } else if (Money > 0 && (Player_Money + Money) < 0) { // SendGetMoneyRequest
                        Money = 2147483647 - Player_Money;
                        if (Money > Trunk_Money) {
                            c.announce(TrunkDlg.encode(TrunkDlg.NotEnoughMesos_Get));
                            return;
                        }
                    }
                    storage.setMeso(Trunk_Money - Money);
                    chr.gainMeso(Money, false, true, false);
                    if (putInStorage) {
                        storage.sendPutMesos(c);
                    } else {
                        storage.sendGetMesos(c);
                    }
                    Logger.log(LogType.INFO, LogFile.STORAGE, c.getAccountName(), c.getPlayer().getName() + (Money > 0 ? " took out " : " stored ") + Math.abs(Money) + " mesos\r\n");
                } else {
                    c.announce(TrunkDlg.encode(false, "Due to an error, the trade did not happen."));
                }
                break;
            }
            case TrunkDlg.closeTrunk: {
                storage.close();
                break;
            }
            default: {
                c.announce(TrunkDlg.encode(false, "Due to an error, the trade did not happen."));
                break;
            }
        }
    }
}
