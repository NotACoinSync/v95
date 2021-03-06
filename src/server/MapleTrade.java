package server;

import java.util.*;

import client.MapleCharacter;
import client.inventory.Item;
import client.inventory.ItemFactory;
import client.inventory.MapleInventoryType;
import constants.ItemConstants;
import tools.BigBrother;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CField.MiniRoomBaseDlg;
import tools.packets.WvsContext;

public class MapleTrade {

    private MapleTrade partner = null;
    private List<Item> items = new ArrayList<>();
    private List<Item> exchangeItems;
    private int meso = 0;
    private int exchangeMeso;
    boolean locked = false;
    private MapleCharacter chr;
    private byte number;
    private boolean fullTrade = false;

    public MapleTrade(byte number, MapleCharacter c) {
        chr = c;
        this.number = number;
    }

    private static int getFee(int meso) {
        int fee = 0;
        if (meso >= 100000000) {
            fee = (int) Math.round(0.06 * meso);
        } else if (meso >= 25000000) {
            fee = meso / 20;
        } else if (meso >= 10000000) {
            fee = meso / 25;
        } else if (meso >= 5000000) {
            fee = (int) Math.round(.03 * meso);
        } else if (meso >= 1000000) {
            fee = (int) Math.round(.018 * meso);
        } else if (meso >= 100000) {
            fee = meso / 125;
        }
        return fee;
    }

    private void lock() {
        locked = true;
        partner.getChr().getClient().announce(MiniRoomBaseDlg.Trade.getTradeConfirmation());
    }

    private void complete1() {
        exchangeItems = partner.getItems();
        exchangeMeso = partner.getMeso();
    }

    private void complete2() {
        items.clear();
        meso = 0;
        for (Item item : exchangeItems) {
            if ((item.getFlag() & ItemConstants.KARMA) == ItemConstants.KARMA) {
                item.setFlag((byte) (item.getFlag() ^ ItemConstants.KARMA)); // items with scissors of karma used on them are reset once traded
            } else if (item.getType() == 2 && (item.getFlag() & ItemConstants.SPIKES) == ItemConstants.SPIKES) {
                item.setFlag((byte) (item.getFlag() ^ ItemConstants.SPIKES));
            }
            MapleInventoryManipulator.addFromDrop(chr.getClient(), item, true);
            ItemFactory.updateItemOwner(chr, item, ItemFactory.INVENTORY);
        }
        if (exchangeMeso > 0) {
            chr.gainMeso(exchangeMeso - getFee(exchangeMeso), true, true, true);
        }
        exchangeMeso = 0;
        if (exchangeItems != null) {
            exchangeItems.clear();
        }
        chr.getClient().announce(MiniRoomBaseDlg.Trade.getTradeCompletion(number));
    }

    private void cancel() {
        for (Item item : items) {
            MapleInventoryManipulator.addFromDrop(chr.getClient(), item, true);
            ItemFactory.updateItemOwner(chr, item, ItemFactory.INVENTORY);
        }
        if (meso > 0) {
            chr.gainMeso(meso, true, true, true);
        }
        meso = 0;
        if (items != null) {
            items.clear();
        }
        exchangeMeso = 0;
        if (exchangeItems != null) {
            exchangeItems.clear();
        }
        chr.getClient().announce(MiniRoomBaseDlg.Trade.getTradeCancel(number));
    }

    private boolean isLocked() {
        return locked;
    }

    private int getMeso() {
        return meso;
    }

    public void setMeso(int meso) {
        if (locked) {
            throw new RuntimeException("Trade is locked.");
        }
        if (meso < 0) {
            Logger.log(LogType.INFO, LogFile.GENERAL_INFO, "[h4x] " + chr.getName() + " Trying to trade < 0 mesos");
            return;
        }
        if (chr.getMeso() >= meso) {
            chr.gainMeso(-meso, false, true, false);
            this.meso += meso;
            chr.getClient().announce(MiniRoomBaseDlg.Trade.getTradeMesoSet((byte) 0, this.meso));
            if (partner != null) {
                partner.getChr().getClient().announce(MiniRoomBaseDlg.Trade.getTradeMesoSet((byte) 1, this.meso));
            }
        } else {
        }
    }

    public void addItem(Item item) {
        items.add(item);
        chr.getClient().announce(MiniRoomBaseDlg.Trade.getTradeItemAdd((byte) 0, item));
        if (partner != null) {
            partner.getChr().getClient().announce(MiniRoomBaseDlg.Trade.getTradeItemAdd((byte) 1, item));
        }
    }

    public void chat(String message) {
        BigBrother.trade(chr, partner.getChr(), message);
        chr.getClient().announce(MiniRoomBaseDlg.Trade.getTradeChat(chr, message, true));
        if (partner != null) {
            partner.getChr().getClient().announce(MiniRoomBaseDlg.Trade.getTradeChat(chr, message, false));
        }
    }

    public MapleTrade getPartner() {
        return partner;
    }

    public void setPartner(MapleTrade partner) {
        if (locked) {
            return;
        }
        this.partner = partner;
    }

    public MapleCharacter getChr() {
        return chr;
    }

    public List<Item> getItems() {
        return new LinkedList<>(items);
    }

    public int getExchangeMesos() {
        return exchangeMeso;
    }

    private boolean fitsInInventory() {
        ItemInformationProvider mii = ItemInformationProvider.getInstance();
        Map<MapleInventoryType, Integer> neededSlots = new LinkedHashMap<>();
        for (Item item : exchangeItems) {
            MapleInventoryType type = mii.getInventoryType(item.getItemId());
            if (neededSlots.get(type) == null) {
                neededSlots.put(type, 1);
            } else {
                neededSlots.put(type, neededSlots.get(type) + 1);
            }
        }
        for (Map.Entry<MapleInventoryType, Integer> entry : neededSlots.entrySet()) {
            if (chr.getInventory(entry.getKey()).isFull(entry.getValue() - 1)) {
                return false;
            }
        }
        return true;
    }

    public static void completeTrade(MapleCharacter c) {
        c.getTrade().lock();
        MapleTrade local = c.getTrade();
        MapleTrade partner = local.getPartner();
        if (partner.isLocked()) {
            local.complete1();
            partner.complete1();
            if (!local.fitsInInventory() || !partner.fitsInInventory()) {
                cancelTrade(c);
                c.message("There is not enough inventory space to complete the trade.");
                partner.getChr().message("There is not enough inventory space to complete the trade.");
                return;
            }
            if (local.getChr().getLevel() < 15) {
                if (local.getChr().getMesosTraded() + local.exchangeMeso > 1000000) {
                    cancelTrade(c);
                    local.getChr().getClient().announce(WvsContext.BroadcastMsg.encode(1, "Characters under level 15 may not trade more than 1 million mesos per day."));
                    return;
                } else {
                    local.getChr().addMesosTraded(local.exchangeMeso);
                }
            } else if (c.getTrade().getChr().getLevel() < 15) {
                if (c.getMesosTraded() + c.getTrade().exchangeMeso > 1000000) {
                    cancelTrade(c);
                    c.getClient().announce(WvsContext.BroadcastMsg.encode(1, "Characters under level 15 may not trade more than 1 million mesos per day."));
                    return;
                } else {
                    c.addMesosTraded(local.exchangeMeso);
                }
            }
            BigBrother.logTrade(local, partner);
            local.complete2();
            partner.complete2();
            partner.getChr().setTrade(null);
            c.setTrade(null);
        }
    }

    public static void cancelTrade(MapleCharacter c) {
        c.getTrade().cancel();
        if (c.getTrade().getPartner() != null) {
            c.getTrade().getPartner().cancel();
            c.getTrade().getPartner().getChr().setTrade(null);
        }
        c.setTrade(null);
    }

    public static void startTrade(MapleCharacter c) {
        if (c.getTrade() == null) {
            c.setTrade(new MapleTrade((byte) 0, c));
            c.getClient().announce(MiniRoomBaseDlg.Trade.getTradeStart(c.getClient(), c.getTrade(), (byte) 0));
        } else {
            c.message("You are already in a trade.");
        }
    }

    public static void inviteTrade(MapleCharacter c1, MapleCharacter c2) {
        if (c2.getTrade() == null) {
            c2.setTrade(new MapleTrade((byte) 1, c2));
            c2.getTrade().setPartner(c1.getTrade());
            c1.getTrade().setPartner(c2.getTrade());
            c2.getClient().announce(MiniRoomBaseDlg.Trade.getTradeInvite(c1));
        } else {
            c1.message("The other player is already trading with someone else.");
            cancelTrade(c1);
        }
    }

    public static void visitTrade(MapleCharacter c1, MapleCharacter c2) {
        if (c1.getTrade() != null && c1.getTrade().getPartner() == c2.getTrade() && c2.getTrade() != null && c2.getTrade().getPartner() == c1.getTrade()) {
            c2.getClient().announce(MiniRoomBaseDlg.Trade.getTradePartnerAdd(c1));
            c1.getClient().announce(MiniRoomBaseDlg.Trade.getTradeStart(c1.getClient(), c1.getTrade(), (byte) 1));
            c1.getTrade().setFullTrade(true);
            c2.getTrade().setFullTrade(true);
        } else {
            c1.message("The other player has already closed the trade.");
        }
    }

    public static void declineTrade(MapleCharacter c) {
        MapleTrade trade = c.getTrade();
        if (trade != null) {
            if (trade.getPartner() != null) {
                MapleCharacter other = trade.getPartner().getChr();
                other.getTrade().cancel();
                other.setTrade(null);
                other.message(c.getName() + " has declined your trade request.");
            }
            trade.cancel();
            c.setTrade(null);
        }
    }

    public boolean isFullTrade() {
        return fullTrade;
    }

    public void setFullTrade(boolean fullTrade) {
        this.fullTrade = fullTrade;
    }
}
