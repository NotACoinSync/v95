package tools.packets.CField;

import net.SendOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

public class ContiMove {
    
    public static class Move {
        
        public static byte[] StartShipMoveField(boolean show) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.CONTI_MOVE.getValue());
            oPacket.write(8);
            oPacket.write(show ? 0 : 2);
            return oPacket.getPacket();
        }    
        
        public static byte[] MoveField(boolean show) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.CONTI_MOVE.getValue());
            oPacket.write(0xA);
            oPacket.write(show ? 0 : 5);
            return oPacket.getPacket();
        }    
        
        public static byte[] EndShipMoveField(boolean show) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.CONTI_MOVE.getValue());
            oPacket.write(0xC);
            oPacket.write(show ? 0 : 6);
            return oPacket.getPacket();
        }    
    }

    public static byte[] State(int type, boolean AppearShip) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CONTI_STATE.getValue());
        oPacket.write(type);
        oPacket.writeBoolean(AppearShip);
        /*
        switch (type) {
            case 0:
            case 1:
            case 6:
                if ( !v2->m_ship.m_nShipKind )
                CShip::EnterShipMove(&v2->m_ship);
                break;
            case 2:
            case 5:
                if ( !v2->m_ship.m_nShipKind )
                CShip::LeaveShipMove(&v2->m_ship);
                break;
            case 3:
            case 4:
                if ( v2->m_ship.m_nShipKind == 1 && v4 == 1 )
                CShip::AppearShip(&v2->m_ship);
                if ( !v2->m_ship.m_nShipKind )
                CShip::LeaveShipMove(&v2->m_ship);
                break;
            default:
                return;
        }
        */
        return oPacket.getPacket();
    }
}
