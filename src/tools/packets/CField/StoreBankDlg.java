package tools.packets.CField;

import client.DBChar;
import client.MapleCharacter;
import client.inventory.Item;
import client.inventory.ItemFactory;
import client.inventory.MapleInventoryType;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import net.SendOpcode;
import tools.packets.PacketHelper;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.logger.Logger;

public class StoreBankDlg {

    public static byte[] Message(int operation) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.FREDRICK_MESSAGE.getValue());
        oPacket.write(operation);
        // 30 = You have retrieved your items and mesos.
        // 31 = Unable to retrieve mesos and items due to\r\ntoo much money stored\r\nat the Store Bank.
        // 32 = Unable to retrieve mesos and items due to\r\none of the items\r\nthat can only be possessed one at a time.
        // 33 = Due to the lack of service fee, you were unable to \r\nretrieve mesos or items.
        // 34 = Unable to retrieve mesos and items\r\ndue to full inventory.
        return oPacket.getPacket();
    }

    public static byte[] Get(MapleCharacter chr) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.FREDRICK.getValue());
        oPacket.write(0x23);
        oPacket.writeInt(9030000); // Fredrick
        oPacket.write(16);// slot count
        long flag = DBChar.Money | DBChar.ItemSlotEquip | DBChar.ItemSlotConsume | DBChar.ItemSlotInstall | DBChar.ItemSlotEtc;
        oPacket.writeLong(flag);
        if ((flag & DBChar.Money) > 0) {
            oPacket.writeInt(chr.getMerchantMeso());
        }
        try {
            Map<MapleInventoryType, List<Item>> items = ItemFactory.MERCHANT.loadItems(chr.getId());
            for (byte mitT = MapleInventoryType.EQUIP.getType(); mitT <= MapleInventoryType.CASH.getType(); mitT++) {
                MapleInventoryType mit = MapleInventoryType.getByType(mitT);
                if ((flag & DBChar.getByInventoryType(mit)) > 0) {
                    List<Item> itemList = items.get(mit);
                    if (itemList == null) {
                        oPacket.write(0);
                        continue;
                    }
                    oPacket.write(itemList.size());
                    for (int i = 0; i < itemList.size(); i++) {
                        PacketHelper.encodeItemSlotBase(oPacket, itemList.get(i), true, true);
                    }
                }
            }
        } catch (SQLException e) {
            oPacket.skip(5);
            Logger.log(Logger.LogType.ERROR, Logger.LogFile.EXCEPTION, e);
        }
        return oPacket.getPacket();
    }

    public static byte[] SendGetAllRequest(int PassingDay, int Fee) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.FREDRICK.getValue());
        oPacket.write(0x24);
        oPacket.writeInt(PassingDay);
        oPacket.writeInt(Fee);
        // CStoreBankDlg::SendGetAllRequest
        return oPacket.getPacket();
    }

    public static byte[] Find(int TemplateID, int RoomID, int Channel) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.FREDRICK.getValue());
        oPacket.write(0x25);
        oPacket.writeInt(TemplateID);
        oPacket.writeInt(RoomID);
        oPacket.write(Channel);
        // CStoreBankDlg::SendGetAllRequest
        return oPacket.getPacket();
    }

    public static byte[] Error() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.FREDRICK.getValue());
        oPacket.write(0x26);
        return oPacket.getPacket();
    }
}
