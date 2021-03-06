package server.quest.requirements;

import java.util.ArrayList;
import java.util.List;

import client.MapleCharacter;
import client.inventory.MapleInventoryType;
import provider.MapleData;
import provider.MapleDataTool;
import server.quest.MapleQuest;
import server.quest.MapleQuestRequirementType;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.LittleEndianWriter;

/**
 *
 *
 * @since Aug 23, 2016
 */
public class EquipSelectNeedRequirement extends MapleQuestRequirement {

    List<Integer> items = new ArrayList<>();

    public EquipSelectNeedRequirement(MapleQuest quest, MapleData data) {
        super(MapleQuestRequirementType.EQUIP_SELECT_NEED);
        processData(data);
    }

    public EquipSelectNeedRequirement(MapleQuest quest, LittleEndianAccessor lea) {
        super(MapleQuestRequirementType.EQUIP_SELECT_NEED);
        processData(lea);
    }

    @Override
    public void processData(MapleData data) {
        for (MapleData itemEntry : data.getChildren()) {
            items.add(MapleDataTool.getInt(itemEntry));
        }
    }

    @Override
    public void processData(LittleEndianAccessor lea) {
        int total = lea.readInt();
        for (int i = 0; i < total; i++) {
            items.add(lea.readInt());
        }
    }

    @Override
    public void writeData(LittleEndianWriter lew) {
        lew.writeInt(items.size());
        for (int id : items) {
            lew.writeInt(id);
        }
    }

    @Override
    public boolean check(MapleCharacter chr, Integer npcid) {
        for (Integer item : items) {
            for (MapleInventoryType mit : MapleInventoryType.values()) {
                if (chr.getInventory(mit).countById(item) > 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
