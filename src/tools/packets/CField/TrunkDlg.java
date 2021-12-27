package tools.packets.CField;

import client.DBChar;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.SendOpcode;
import tools.packets.PacketHelper;
import tools.data.output.MaplePacketLittleEndianWriter;

public class TrunkDlg {

    public static final byte InventoryFull = 0xA,
            NotEnoughMesos_Put = 0xB,
            NotEnoughMesos_Get = 0x10,
            CouldNotRetrieved = 0xC,
            PutItems_Out = 0x9, PutItems_In = 0xD,
            PutMesos_In = 0xF, PutMesos_Out = 0x13,
            StorageFull = 0x11,
            SetUpStorage = 0x16,
            Error_CannotBeMoved = 0x17,
            Error_SendMessage = 0x18;

    public static final byte getItemOutTrunk = 0x4,
            putItemInTrunk = 0x5,
            sortItemInTrunk = 0x6,
            moneyInTrunk = 0x7,
            closeTrunk = 0x8;

    public static byte[] encode(byte mode) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.STORAGE.getValue());
        oPacket.write(mode);
        return oPacket.getPacket();
    }

    public static byte[] encode(int NpcTemplateID, byte SlotCount, int Money, Map<MapleInventoryType, List<Item>> typeItems, Collection<Item> items) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.STORAGE.getValue());
        oPacket.write(SetUpStorage);
        oPacket.writeInt(NpcTemplateID);
        oPacket.write(SlotCount);
        long mask = 0;
        for (MapleInventoryType type : typeItems.keySet()) {
            mask |= type.getBitfieldEncoding();
        }
        oPacket.writeLong(mask);
        oPacket.writeInt(Money);
        for (int i = 1; i <= 5; i++) {
            MapleInventoryType type = MapleInventoryType.getByType((byte) i);
            List<Item> newItem = new LinkedList<>();
            for (Item item : items) {
                if (MapleInventoryType.getByItemId(item.getItemId()) == type) {
                    newItem.add(item);
                }
            }
            oPacket.write(newItem.size());
            for (Item item2 : newItem) {
                PacketHelper.encodeItemSlotBase(oPacket, item2, true);
            }
        }
        return oPacket.getPacket();
    }

    public static byte[] encode(int InOutType, byte SlotCount, MapleInventoryType type, Collection<Item> items) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.STORAGE.getValue());
        oPacket.write(InOutType);
        oPacket.write(SlotCount);
        oPacket.writeLong(type.getBitfieldEncoding());
        oPacket.write(items.size());
        for (Item item : items) {
            PacketHelper.encodeItemSlotBase(oPacket, item, true);
        }
        return oPacket.getPacket();
    }

    public static byte[] encode(int InOutType, byte SlotCount, int Money) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.STORAGE.getValue());
        oPacket.write(InOutType);
        oPacket.write(SlotCount);
        oPacket.writeLong(DBChar.Money);
        oPacket.writeInt(Money);
        return oPacket.getPacket();
    }

    public static byte[] encode(boolean throwException, String sMsg) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.STORAGE.getValue());
        oPacket.write(Error_SendMessage);
        oPacket.writeBoolean(throwException);
        if (throwException) {
            oPacket.writeMapleAsciiString(sMsg);
        }
        return oPacket.getPacket();
    }
}
