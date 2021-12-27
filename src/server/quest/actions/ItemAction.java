package server.quest.actions;

import java.util.*;

import client.MapleCharacter;
import client.MapleJob;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import provider.MapleData;
import provider.MapleDataTool;
import server.ItemInformationProvider;
import server.MapleInventoryManipulator;
import server.quest.MapleQuest;
import server.quest.MapleQuestActionType;
import tools.Pair;
import tools.Randomizer;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.LittleEndianWriter;
import tools.packets.CField.userpool.UserLocal;
import tools.packets.EffectPacket;

/**
 * @author Tyler (Twdtwd)
 */
public class ItemAction extends MapleQuestAction {

    Map<Integer, ItemData> items = new HashMap<>();

    public ItemAction(MapleQuest quest, MapleData data) {
        super(MapleQuestActionType.ITEM, quest);
        processData(data);
    }

    public ItemAction(MapleQuest quest, LittleEndianAccessor lea) {
        super(MapleQuestActionType.ITEM, quest);
        processData(lea);
    }

    @Override
    public void processData(MapleData data) {
        for (MapleData iEntry : data.getChildren()) {
            int key = Integer.parseInt(iEntry.getName());
            int id = MapleDataTool.getInt(iEntry.getChildByPath("id"));
            int count = MapleDataTool.getInt(iEntry.getChildByPath("count"), 0);
            Integer prop = -5555;
            MapleData propData = iEntry.getChildByPath("prop");
            if (propData != null) {
                prop = MapleDataTool.getInt(propData);
            }
            int gender = 2;
            if (iEntry.getChildByPath("gender") != null) {
                gender = MapleDataTool.getInt(iEntry.getChildByPath("gender"));
            }
            int job = -1;
            if (iEntry.getChildByPath("job") != null) {
                job = MapleDataTool.getInt(iEntry.getChildByPath("job"));
            }
            items.put(id, new ItemData(key, id, count, prop, job, gender));
        }
    }

    @Override
    public void processData(LittleEndianAccessor lea) {
        int totalItems = lea.readInt();
        for (int i = 0; i < totalItems; i++) {
            int key = lea.readInt();
            int itemid = lea.readInt();
            int count = lea.readInt();
            int prop = lea.readInt();
            int job = lea.readInt();
            int gender = lea.readInt();
            items.put(itemid, new ItemData(key, itemid, count, prop, job, gender));
        }
    }

    @Override
    public void writeData(LittleEndianWriter lew) {
        lew.writeInt(items.size());
        for (ItemData item : items.values()) {
            lew.writeInt(item.getKey());
            lew.writeInt(item.getId());
            lew.writeInt(item.getCount());
            lew.writeInt(item.getProp() == null ? -5555 : item.getProp());
            lew.writeInt(item.getJob());
            lew.writeInt(item.getGender());
        }
    }

    @Override
    public void run(MapleCharacter chr, Integer extSelection) {
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        Map<Integer, Integer> props = new HashMap<>();
        for (ItemData item : items.values()) {
            if (item.getProp() != -5555 && item.getProp() != -1 && canGetItem(item, chr)) {
                for (int i = 0; i < item.getProp(); i++) {
                    props.put(props.size(), item.getId());
                }
            }
        }
        int selection = 0;
        if (props.size() > 0) {
            selection = props.get(Randomizer.nextInt(props.size()));
        }
        int totalNegProps = 0;
        for (ItemData iEntry : items.values()) {
            if (iEntry.getProp() != -5555 && iEntry.getProp() == -1) {
                totalNegProps++;
            }
        }
        int totalUselessProps = items.size() - totalNegProps;
        for (ItemData iEntry : items.values()) {
            if (!canGetItem(iEntry, chr)) {
                continue;
            }
            if (iEntry.getProp() != -5555) {
                if (iEntry.getProp() == -1 && extSelection != null) {
                    if (extSelection != iEntry.getKey() - totalUselessProps) {
                        continue;
                    }
                } else if (iEntry.getId() != selection) {
                    continue;
                }
            }
            if (iEntry.getCount() <= 0) { // Remove Items
                MapleInventoryType type = ii.getInventoryType(iEntry.getId());
                int quantity = iEntry.getCount() * -1; // Invert
                if (iEntry.getCount() == 0) { // 0 is the default, so remove all.
                    quantity = chr.getInventory(type).countById(iEntry.getId());
                }
                if (type.equals(MapleInventoryType.EQUIP)) {
                    if (chr.getInventory(type).countById(iEntry.getId()) < quantity) {
                        // Not enough in the equip inventoty, so check Equipped...
                        if (chr.getInventory(MapleInventoryType.EQUIPPED).countById(iEntry.getId()) > quantity) {
                            // Found it equipped, so change the type to equipped.
                            type = MapleInventoryType.EQUIPPED;
                        }
                    }
                }
                MapleInventoryManipulator.removeById(chr.getClient(), type, iEntry.getId(), quantity, true, true);
                chr.announce(EffectPacket.Local.Quest(iEntry.getId(), (short) iEntry.getCount()));
            } else {
                if (chr.canHoldItem(new Item(iEntry.getId(), (short) iEntry.getCount()))) {
                    Item item = null;
                    if (ItemInformationProvider.getInstance().getInventoryType(iEntry.getId()) == MapleInventoryType.EQUIP) {
                        item = ItemInformationProvider.getInstance().getEquipById(iEntry.getId());
                    } else {
                        item = new Item(iEntry.getId(), (short) iEntry.getCount());
                    }
                    MapleInventoryManipulator.addFromDrop(chr.getClient(), item, false);
                    chr.announce(EffectPacket.Local.Quest(iEntry.getId(), (short) iEntry.getCount()));
                } else {
                    chr.dropMessage(1, "Inventory Full");
                }
            }
        }
    }

    @Override
    public boolean check(MapleCharacter chr, Integer extSelection) {
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        EnumMap<MapleInventoryType, Integer> props = new EnumMap<>(MapleInventoryType.class);
        List<Pair<Item, MapleInventoryType>> itemList = new ArrayList<>();
        for (ItemData item : items.values()) {
            if (!canGetItem(item, chr)) {
                continue;
            }
            MapleInventoryType type = ii.getInventoryType(item.getId());
            if (item.getProp() != -5555) {
                if (!props.containsKey(type)) {
                    props.put(type, item.getId());
                }
                continue;
            }
            if (item.getCount() > 0) {
                // Make sure they can hold the item.
                Item toItem = new Item(item.getId(), (short) 0, (short) item.getCount());
                itemList.add(new Pair<>(toItem, type));
            } else {
                // Make sure they actually have the item.
                int quantity = (item.getCount() == 0 ? 1 : 0 + item.getCount()) * -1;
                if (chr.getInventory(type).countById(item.getId()) < quantity) {
                    if (type.equals(MapleInventoryType.EQUIP) && chr.getInventory(MapleInventoryType.EQUIPPED).countById(item.getId()) > quantity) {
                        continue;
                    }
                    return false;
                }
            }
        }
        for (Integer itemID : props.values()) {
            MapleInventoryType type = ii.getInventoryType(itemID);
            Item toItem = new Item(itemID, (short) 0, (short) 1);
            itemList.add(new Pair<>(toItem, type));
        }
        if (!chr.canHoldItemsType(itemList)) {
            chr.dropMessage(1, "Please check if you have enough space in your inventory.");
            return false;
        }
        return true;
    }

    private boolean canGetItem(ItemData item, MapleCharacter chr) {
        if (item.getGender() != 2 && item.getGender() != chr.getGender()) {
            return false;
        }
        if (item.getJob() != -1) {
            if (item.getJob() != chr.getJob().getId()) {
                if (!MapleJob.checkJobMask(item.getJob(), chr.getJob())) {
                    return false;
                }
            }
        }
        return true;
    }

    private class ItemData {

        private final int key;
        private final int id, count, job, gender;
        private final Integer prop;

        public ItemData(int key, int id, int count, Integer prop, int job, int gender) {
            this.key = key;
            this.id = id;
            this.count = count;
            this.prop = prop;
            this.job = job;
            this.gender = gender;
        }

        public int getKey() {
            return key;
        }

        public int getId() {
            return id;
        }

        public int getCount() {
            return count;
        }

        public Integer getProp() {
            return prop;
        }

        public int getJob() {
            return job;
        }

        public int getGender() {
            return gender;
        }
    }
}
