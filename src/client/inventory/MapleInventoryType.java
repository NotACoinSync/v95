package client.inventory;

import java.util.List;
import java.util.Map;

public enum MapleInventoryType {
    MESO(0),
    EQUIP(1),
    USE(2),
    SETUP(3),
    ETC(4),
    CASH(5),
    EQUIPPED(-1);

    final byte type;

    private MapleInventoryType(int type) {
        this.type = (byte) type;
    }

    public byte getType() {
        return type;
    }

    public short getBitfieldEncoding() {
        return (short) (2 << type);
    }

    public static long getBitfieldEncoding(Map<MapleInventoryType, List<Item>> typeItems) {
        long result = 0;
        for (MapleInventoryType types : typeItems.keySet()) {
            result += 2 << types.getType();
        }
        return result;
    }

    public static MapleInventoryType getByType(byte type) {
        for (MapleInventoryType l : MapleInventoryType.values()) {
            if (l.getType() == type) {
                return l;
            }
        }
        return null;
    }

    public static MapleInventoryType getByItemId(int itemid) {
        final byte type = (byte) (itemid / 1000000);
        if (type < 1 || type > 5) {
            return MapleInventoryType.MESO;
        }
        return MapleInventoryType.getByType(type);
    }

    public static MapleInventoryType getByWZName(String name) {
        switch (name) {
            case "Install":
                return SETUP;
            case "Consume":
                return USE;
            case "Etc":
                return ETC;
            case "Cash":
                return CASH;
            case "Pet":
                return CASH;
            default:
                break;
        }
        return MESO;
    }
}
