package tools.packets.CField;

import net.SendOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

public class MapLoadable {

    /**
     * Changes the current background effect to either being rendered or not.
     * Data is still missing, so this is pretty binary at the moment in how it
     * behaves.
     *
     * @param nEffect whether or not the remove or add the specified layer.
     * @param nPageID the targeted layer for removal or addition.
     * @param tDuration the time it takes to transition the effect.
     * @return a packet to change the background effect of a specified layer.
     */
    public static byte[] SetBackEffect(boolean nEffect, int nFieldID, int nPageID, int tDuration) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SET_BACK_EFFECT.getValue());
        oPacket.writeBoolean(nEffect);
        oPacket.writeInt(nFieldID);
        oPacket.write(nPageID);
        oPacket.writeInt(tDuration);
        return oPacket.getPacket();
    }

    public static byte[] SetMapObjectVisible(int Objects) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SET_MAP_OBJECT_VISIBLE.getValue());
        oPacket.write(Objects);
        /*
            if (Objects > 0)
            {
              do
              {
                oPacket.writeMapleAsciiString(Objects.); // objName
                oPacket.write(1); // bVisible
                --Objects;
              }
              while (Objects > 0);
            }
         */
        return oPacket.getPacket();
    }

    public static byte[] ClearBackEffect() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CLEAR_BACK_EFFECT.getValue());
        return oPacket.getPacket();
    }
}
