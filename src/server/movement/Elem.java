package server.movement;

import tools.data.input.LittleEndianAccessor;
import tools.data.output.LittleEndianWriter;

public class Elem {

    byte type;
    public byte bMoveAction, bStat;
    public short x, y, vx, vy, fh, fhFallStart, xOffset, yOffset;
    public short tElapse;

    public void decode(LittleEndianAccessor iPacket) {
        type = iPacket.readByte();// nAttr
        // label_12, encode2, goto label_13
        // label_13, encode1, encode 2
        switch (type) {
            case 0:
            case 5:
            case 12:
            case 14:
            case 35:
            case 36:
                x = iPacket.readShort();
                y = iPacket.readShort();
                vx = iPacket.readShort();
                vy = iPacket.readShort();
                fh = iPacket.readShort();
                if (type == 12) {
                    fhFallStart = iPacket.readShort();
                }
                xOffset = iPacket.readShort();
                yOffset = iPacket.readShort();
                bMoveAction = iPacket.readByte();
                tElapse = iPacket.readShort();
                break;
            case 3:
            case 4:
            case 6:
            case 7:
            case 8:
            case 10:
                x = iPacket.readShort();
                y = iPacket.readShort();
                fh = iPacket.readShort();
                bMoveAction = iPacket.readByte();
                tElapse = iPacket.readShort();
                break;
            case 11:
                vx = iPacket.readShort();
                vy = iPacket.readShort();
                fhFallStart = iPacket.readShort();
                bMoveAction = iPacket.readByte();
                tElapse = iPacket.readShort();
                break;
            case 17:
                x = iPacket.readShort();
                y = iPacket.readShort();
                vx = iPacket.readShort();
                vy = iPacket.readShort();
                bMoveAction = iPacket.readByte();
                tElapse = iPacket.readShort();
                break;
            case 1:
            case 2:
            case 13:
            case 16:
            case 18:
            case 31:
            case 32:
            case 33:
            case 34:
                vx = iPacket.readShort();
                // LABEL_23
                vy = iPacket.readShort();
                // LABEL_18
                bMoveAction = iPacket.readByte();
                tElapse = iPacket.readShort();
                break;
            case 9:
                bStat = iPacket.readByte();
                break;
            default:
                bMoveAction = iPacket.readByte();
                tElapse = iPacket.readShort();
                break;
        }
    }

    public void encode(LittleEndianWriter oPacket) {
        oPacket.write(type);
        switch (type) {
            case 0:
            case 5:
            case 12:
            case 14:
            case 35:
            case 36:
                oPacket.writeShort(x);
                oPacket.writeShort(y);
                oPacket.writeShort(vx);
                oPacket.writeShort(vy);
                oPacket.writeShort(fh);
                if (type == 12) {
                    oPacket.writeShort(fhFallStart);
                }
                oPacket.writeShort(xOffset);
                oPacket.writeShort(yOffset);
                oPacket.write(bMoveAction);
                oPacket.writeShort(tElapse);
                break;
            case 3:
            case 4:
            case 6:
            case 7:
            case 8:
            case 10:
                oPacket.writeShort(x);
                oPacket.writeShort(y);
                oPacket.writeShort(fh);
                oPacket.write(bMoveAction);
                oPacket.writeShort(tElapse);
                break;
            case 11:
                oPacket.writeShort(vx);
                oPacket.writeShort(vy);
                oPacket.writeShort(fhFallStart);
                oPacket.write(bMoveAction);
                oPacket.writeShort(tElapse);
                break;
            case 17:
                oPacket.writeShort(x);
                oPacket.writeShort(y);
                oPacket.writeShort(vx);
                oPacket.writeShort(vy);
                oPacket.write(bMoveAction);
                oPacket.writeShort(tElapse);
                break;
            case 1:
            case 2:
            case 13:
            case 16:
            case 18:
            case 31:
            case 32:
            case 33:
            case 34:
                oPacket.writeShort(vx);
                oPacket.writeShort(vy);
                oPacket.write(bMoveAction);
                oPacket.writeShort(tElapse);
                break;
            case 9:
                oPacket.write(bStat);
                break;
            default:
                oPacket.write(bMoveAction);
                oPacket.writeShort(tElapse);
                break;
        }
    }
}
