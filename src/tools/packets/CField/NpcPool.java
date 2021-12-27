package tools.packets.CField;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.SendOpcode;
import server.life.MapleNPC;
import server.maps.objects.PlayerNPC;
import server.movement.MovePath;
import tools.Pair;
import tools.data.output.MaplePacketLittleEndianWriter;

public class NpcPool {

    public static byte[] ImitatedNPCResult() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.IMITATED_NPC_RESULT.getValue());
        oPacket.write(0);
        return oPacket.getPacket();
    }

    public static byte[] NpcImitateData(PlayerNPC npc) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.IMITATED_NPC_DATA.getValue());
        oPacket.write(0x01);
        oPacket.writeInt(npc.getId());
        oPacket.writeMapleAsciiString(npc.getName());
        oPacket.write(npc.getGender());
        oPacket.write(npc.getSkin());
        oPacket.writeInt(npc.getFace());
        oPacket.write(0);
        oPacket.writeInt(npc.getHair());
        Map<Short, Integer> equip = npc.getEquips();
        Map<Short, Integer> myEquip = new LinkedHashMap<Short, Integer>();
        Map<Short, Integer> maskedEquip = new LinkedHashMap<Short, Integer>();
        for (short position : equip.keySet()) {
            short pos = (byte) (position * -1);
            if (pos < 100 && myEquip.get(pos) == null) {
                myEquip.put(pos, equip.get(position));
            } else if ((pos > 100 || pos == -128) && pos != 111) { // don't ask. o.o
                pos -= 100;
                if (myEquip.get(pos) != null) {
                    maskedEquip.put(pos, myEquip.get(pos));
                }
                myEquip.put(pos, equip.get(position));
            } else if (myEquip.get(pos) != null) {
                maskedEquip.put(pos, equip.get(position));
            }
        }
        for (Map.Entry<Short, Integer> entry : myEquip.entrySet()) {
            oPacket.write(entry.getKey());
            oPacket.writeInt(entry.getValue());
        }
        oPacket.write(0xFF);
        for (Map.Entry<Short, Integer> entry : maskedEquip.entrySet()) {
            oPacket.write(entry.getKey());
            oPacket.writeInt(entry.getValue());
        }
        oPacket.write(0xFF);
        Integer cWeapon = equip.get((byte) -111);
        if (cWeapon != null) {
            oPacket.writeInt(cWeapon);
        } else {
            oPacket.writeInt(0);
        }
        for (int i = 0; i < 3; i++) {
            oPacket.writeInt(0);
        }
        return oPacket.getPacket();
    }

    public static byte[] UpdateLimitedDisableInfo(List<Integer> disabledNpc) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.LIMITED_NPC_DISABLE_INFO.getValue());
        oPacket.write(disabledNpc.size());
        for (int nDisabledNpc : disabledNpc) {
            oPacket.writeInt(nDisabledNpc);
        }
        return oPacket.getPacket();
    }

    public static byte[] NpcEnterField(MapleNPC life) {
        return NpcEnterField(life, true);
    }

    public static byte[] NpcEnterField(MapleNPC life, boolean minimap) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SPAWN_NPC.getValue());
        oPacket.writeInt(life.getObjectId());
        oPacket.writeInt(life.getId());
        // CNpc::Init
        oPacket.writeShort(life.getPosition().x); // m_ptPosPrev.x
        oPacket.writeShort(life.getCy()); // m_ptPosPrev.y
        if (life.getF() == 1) { // m_nMoveAction
            oPacket.write(0);
        } else {
            oPacket.write(1);
        }
        oPacket.writeShort(life.getFh()); // dwSN
        oPacket.writeShort(life.getRx0()); // m_rgHorz.low
        oPacket.writeShort(life.getRx1()); // m_rgHorz.high
        oPacket.writeBoolean(minimap); // m_bEnabled
        return oPacket.getPacket();
    }

    public static byte[] NpcLeaveField(int dwNpcId) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.REMOVE_NPC.getValue());
        oPacket.writeInt(dwNpcId);
        return oPacket.getPacket();
    }

    public static byte[] spawnNPCRequestController(MapleNPC life, boolean MiniMap) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
        oPacket.write(1);
        oPacket.writeInt(life.getObjectId());
        oPacket.writeInt(life.getId());
        oPacket.writeShort(life.getPosition().x);
        oPacket.writeShort(life.getCy());
        if (life.getF() == 1) {
            oPacket.write(0);
        } else {
            oPacket.write(1);
        }
        oPacket.writeShort(life.getFh());
        oPacket.writeShort(life.getRx0());
        oPacket.writeShort(life.getRx1());
        oPacket.writeBoolean(MiniMap);
        return oPacket.getPacket();
    }

    public static byte[] spawnPlayerNPC(PlayerNPC npc) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
        oPacket.write(1);
        oPacket.writeInt(npc.getObjectId());
        oPacket.writeInt(npc.getId());
        oPacket.writeShort(npc.getPosition().x);
        oPacket.writeShort(npc.getCY());
        oPacket.write(npc.getDirection());
        oPacket.writeShort(npc.getFH());
        oPacket.writeShort(npc.getRX0());
        oPacket.writeShort(npc.getRX1());
        oPacket.write(1);
        return oPacket.getPacket();
    }
    
    public static byte[] Move(MapleNPC npc, MovePath moves) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MOVE_NPC.getValue());
        oPacket.writeInt(npc.getObjectId());
        oPacket.write(0); // m_nOneTimeAction
        oPacket.write(0); // nChatIdx        
        moves.encode(oPacket);
        return oPacket.getPacket();
    }

    public static byte[] UpdateLimitedInfo(MapleNPC npc, boolean bEnabled) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.UPDATE_LIMITED_INFO.getValue());
        oPacket.writeInt(npc.getObjectId());
        oPacket.writeBoolean(bEnabled); // m_bEnabled
        return oPacket.getPacket();
    }

    public static byte[] setNpcSpecialAction(int oid, String action) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.NPC_SPECIAL_ACTION.getValue());
        oPacket.writeInt(oid);
        oPacket.writeMapleAsciiString(action);
        return oPacket.getPacket();
    }

    /**
     * Makes any NPC in the game scriptable.
     *
     * @param npcId - The NPC's ID, found in WZ files/MCDB
     * @param description - If the NPC has quests, this will be the text of the
     * menu item
     * @return
     */
    public static byte[] setNPCScriptable(int npcId, String description) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SET_NPC_SCRIPTABLE.getValue());
        oPacket.write(1); // following structure is repeated n times
        oPacket.writeInt(npcId);
        oPacket.writeMapleAsciiString(description);
        oPacket.writeInt(0); // start time
        oPacket.writeInt(Integer.MAX_VALUE); // end time
        return oPacket.getPacket();
    }

    /**
     * Makes any NPC in the game scriptable.
     *
     * @param npcId - The NPC's ID, found in WZ files/MCDB
     * @param description - If the NPC has quests, this will be the text of the
     * menu item
     * @return
     */
    public static byte[] setNPCScriptable(Set<Pair<Integer, String>> npcs) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SET_NPC_SCRIPTABLE.getValue());
        for (Pair<Integer, String> npc : npcs) {
            oPacket.writeInt(npc.left);
            oPacket.writeMapleAsciiString(npc.right);
            oPacket.writeInt(0); // start time
            oPacket.writeInt(Integer.MAX_VALUE); // end time
        }
        return oPacket.getPacket();
    }
}
