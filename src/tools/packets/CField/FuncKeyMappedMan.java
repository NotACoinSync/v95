package tools.packets.CField;

import client.MapleKeyBinding;
import java.util.Map;
import net.SendOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

public class FuncKeyMappedMan {

    public static byte[] Init(Map<Integer, MapleKeyBinding> keybindings) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.KEYMAP.getValue());
        oPacket.write(0);
        for (int x = 0; x < 90; x++) {
            MapleKeyBinding binding = keybindings.get(Integer.valueOf(x));
            if (binding != null) {
                oPacket.write(binding.getType());
                oPacket.writeInt(binding.getAction());
            } else {
                oPacket.write(0);
                oPacket.writeInt(0);
            }
        }
        return oPacket.getPacket();
    }

    public static byte[] PetConsumeItemInit(int itemId) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.AUTO_HP_POT.getValue());
        oPacket.writeInt(itemId);
        return oPacket.getPacket();
    }

    public static byte[] PetConsumeMPItemInit(int itemId) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.AUTO_MP_POT.getValue());
        oPacket.writeInt(itemId);
        return oPacket.getPacket();
    }

    public static byte[] getQuickSlots(Map<Integer, MapleKeyBinding> keybindings, boolean defaultKeys) {// CQuickslotKeyMappedMan::OnInit
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.QUICKSLOT_SET.getValue());
        oPacket.writeBoolean(!defaultKeys);
        if (!defaultKeys) {
            for (int x = 93; x <= 100; x++) {
                MapleKeyBinding binding = keybindings.get(Integer.valueOf(x));
                if (binding != null) {
                    oPacket.writeInt(binding.getAction());
                } else {
                    oPacket.writeInt(0);
                }
            }
        }
        return oPacket.getPacket();
    }
}
