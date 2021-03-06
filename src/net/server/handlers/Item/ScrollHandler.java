package net.server.handlers.Item;

import java.util.ArrayList;
import java.util.List;

import client.MapleClient;
import client.Skill;
import client.autoban.AutobanFactory;
import client.inventory.*;
import constants.SkillConstants;
import net.AbstractMaplePacketHandler;
import server.ItemData;
import server.ItemInformationProvider;
import server.MapleInventoryManipulator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.WvsContext;
import tools.packets.CField.userpool.UserCommon;

public final class ScrollHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        iPacket.readInt(); // whatever...
        short slot = iPacket.readShort();
        short dst = iPacket.readShort();
        short ws = iPacket.readShort();
        boolean enchantSkill = iPacket.readBoolean();
        boolean whiteScroll = ws == 2; // white scroll being used? // return (this->m_pCheckWhiteScroll.p->m_bChecked != 0) + 1;
        boolean legendarySpirit = false; // legendary spirit skill
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        Equip toScroll = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(dst);
        if (enchantSkill && dst >= 0) {// Prob has a byte somewhere that says if its legendary spirit
            for (Skill s : c.getPlayer().getSkills().keySet()) {
                if (SkillConstants.isLegendarySpirit(s.getId())) {
                    legendarySpirit = true;
                    toScroll = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(dst);
                }
            }
            if (!legendarySpirit) {
                AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to scroll with Legendary Spirit but doesn't have Legendary Spirit.");
                return;
            }
        }
        byte oldLevel = toScroll.getLevel();
        byte oldSlots = toScroll.getUpgradeSlots();
        MapleInventory useInventory = c.getPlayer().getInventory(MapleInventoryType.USE);
        Item scroll = useInventory.getItem(slot);
        if (scroll == null) {
            scroll = c.getPlayer().getInventory(MapleInventoryType.CASH).getItem(slot);
            if (scroll == null) {
                AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use a null scroll");
                c.announce(WvsContext.InventoryOperationFull());
                c.announce(WvsContext.enableActions());
                return;
            }
        }
        Item wscroll = null;
        if (toScroll.getUpgradeSlots() < 1 && !isCleanSlate(scroll.getItemId()) && !isFlaggedScroll(scroll.getItemId())) {
            c.getPlayer().dropMessage(1, "You may not use that scroll on this item.");
            c.announce(WvsContext.InventoryOperationFull());
            c.announce(WvsContext.enableActions());
            return;
        }
        List<Integer> scrollReqs = ii.getItemData(scroll.getItemId()).allowedItems;
        if (scrollReqs.size() > 0 && !scrollReqs.contains(toScroll.getItemId())) {
            c.getPlayer().dropMessage(1, "You may not use that scroll on this item.");
            c.announce(WvsContext.InventoryOperationFull());
            c.announce(WvsContext.enableActions());
            return;
        }
        if (whiteScroll) {
            wscroll = useInventory.findById(2340000);
            if (wscroll == null || wscroll.getItemId() != 2340000) {
                whiteScroll = false;
                AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Set white scroll to true but had no white scrolls.");
            }
        }
        ItemData toScrollData = ii.getItemData(toScroll.getItemId());
        if (!isChaosScroll(scroll.getItemId()) && !isCleanSlate(scroll.getItemId()) && !isFlaggedScroll(scroll.getItemId())) {
            if (!canScroll(scroll.getItemId(), toScroll.getItemId())) {
                c.getPlayer().dropMessage(1, "You may not use that scroll on this item.");
                c.announce(WvsContext.InventoryOperationFull());
                c.announce(WvsContext.enableActions());
                return;
            }
        }
        if (isFlaggedScroll(scroll.getItemId())) {
            if (toScroll.getFlag() == 4 || toScroll.getFlag() == 2) {
                c.getPlayer().dropMessage(1, "You have already used that scroll on this item successfully.");
                c.announce(WvsContext.InventoryOperationFull());
                c.announce(WvsContext.enableActions());
                return;
            }
            if (toScrollData.isCash) {
                c.getPlayer().dropMessage(1, "You may not use that scroll on this item.");
                AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to scroll a cash item.");
                c.announce(WvsContext.InventoryOperationFull());
                c.announce(WvsContext.enableActions());
                return;
            }
        }
        if (isCleanSlate(scroll.getItemId()) && !(toScroll.getLevel() + toScroll.getUpgradeSlots() < toScrollData.tuc + toScroll.getVicious())) {
            c.getPlayer().dropMessage(1, "You may not use that scroll on this item.");
            c.announce(WvsContext.InventoryOperationFull());
            c.announce(WvsContext.enableActions());
            return;
        }
        Equip scrolled = (Equip) ii.scrollEquipWithId(toScroll, scroll.getItemId(), whiteScroll, c.getPlayer().isGM());
        boolean success = false, cursed = false;
        if (scrolled == null) {
            cursed = true;
        } else if (scrolled.getLevel() > oldLevel || (isCleanSlate(scroll.getItemId()) && scrolled.getUpgradeSlots() == oldSlots + 1) || (isFlaggedScroll(scroll.getItemId()) && (scrolled.getFlag() == 4 || scrolled.getFlag() == 2))) {
            success = true;
        }
        MapleInventoryManipulator.removeItem(c, MapleInventoryType.USE, scroll.getPosition(), (short) 1, true, false);
        if (whiteScroll && !isCleanSlate(scroll.getItemId())) {
            MapleInventoryManipulator.removeItem(c, MapleInventoryType.USE, wscroll.getPosition(), (short) 1, true, false);
        }
        final List<ModifyInventory> mods = new ArrayList<>();
        if (cursed) {
            mods.add(new ModifyInventory(3, toScroll));
            if (dst < 0) {
                c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).removeItem(toScroll.getPosition());
            } else {
                c.getPlayer().getInventory(MapleInventoryType.EQUIP).removeItem(toScroll.getPosition());
            }
            ItemFactory.deleteItem(toScroll);
        } else {
            mods.add(new ModifyInventory(3, scrolled));
            mods.add(new ModifyInventory(0, scrolled));
        }
        c.announce(WvsContext.InventoryOperation(true, mods));
        c.getPlayer().getMap().announce(UserCommon.showItemUpgradeEffect(c.getPlayer().getId(), success, cursed, legendarySpirit, 0, whiteScroll, false));
        if (dst < 0 && (success || cursed)) {
            c.getPlayer().equipChanged();
        }
    }

    private boolean isCleanSlate(int scrollId) {
        return scrollId > 2048999 && scrollId < 2049004;
    }

    private boolean isChaosScroll(int scrollId) {
        return scrollId >= 2049100 && scrollId <= 2049103 || scrollId == 2049113 || scrollId == 2049114;
    }

    private boolean isFlaggedScroll(int scrollId) {
        return scrollId == 2040727 || scrollId == 2041058;
    }

    public boolean canScroll(int scrollid, int itemid) {
        return (scrollid / 100) % 100 == (itemid / 10000) % 100;
    }
}
