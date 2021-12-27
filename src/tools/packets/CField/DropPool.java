package tools.packets.CField;

import java.awt.Point;

import net.SendOpcode;
import server.maps.MapleMapItem;
import tools.packets.PacketHelper;
import tools.data.output.MaplePacketLittleEndianWriter;

public class DropPool {

    // OwnType
    public static final int UserOwn = 0x0;
    public static final int PartyOwn = 0x1;
    public static final int NoOwn = 0x2;
    public static final int Explosive_NoOwn = 0x3;
    // EnterType
    public static final int JustShowing = 0x0;
    public static final int Create = 0x1;
    public static final int OnTheFoothold = 0x2;
    public static final int FadingOut = 0x3;
    // LeaveType
    public static final int ByTimeOut = 0x0;
    public static final int ByScreenScroll = 0x1;
    public static final int PickedUpByUser = 0x2;
    public static final int PickedUpByMob = 0x3;
    public static final int Explode = 0x4;
    public static final int PickedUpByPet = 0x5;
    public static final int PassConvex = 0x6;
    public static final int SkillPet = 0x7;

    public static byte[] EnterField(MapleMapItem drop, Point dropfrom, byte enterType) {
        return EnterField(drop, dropfrom, drop.getPosition(), enterType, 0);
    }

    public static byte[] EnterField(MapleMapItem drop, Point dropfrom, byte enterType, int delay) {
        return EnterField(drop, dropfrom, drop.getPosition(), enterType, delay);
    }

    public static byte[] EnterField(MapleMapItem drop, Point dropfrom, Point dropto, byte enterType) {
        return EnterField(drop, dropfrom, dropto, enterType, 0);
    }

    public static byte[] EnterField(MapleMapItem drop, Point dropfrom, Point dropto, byte enterType, int delay) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.DROP_ITEM_FROM_MAPOBJECT.getValue());
        oPacket.write(enterType);
        oPacket.writeInt(drop.getObjectId());
        oPacket.writeBoolean(drop.getMeso() > 0); // 1 mesos, 0 item, 2 and above all item meso bag,
        oPacket.writeInt(drop.getItemId()); // drop object ID
        oPacket.writeInt(drop.getDropType() > 0 ? drop.getOwner() : drop.getOwnerChrId()); // might need to set this to 0 if drop type > 1
        oPacket.write(drop.getDropType()); // 0 = timeout for non-owner, 1 = timeout for non-owner's party, 2 = FFA, 3 = explosive/FFA
        oPacket.writePos(dropto);
        oPacket.writeInt(drop.getDropType() == 0 ? drop.getOwner() : 0);
        if (enterType == JustShowing || enterType == Create || enterType == FadingOut || enterType == Explode) {
            oPacket.writePos(dropfrom);
            oPacket.writeShort(delay);
        }
        if (drop.getMeso() == 0) {
            PacketHelper.encodeExpirationTime(oPacket, drop.getItem().getExpiration());
        }
        oPacket.write(drop.isPlayerDrop() ? 0 : 1); // pet EQP pickup
        oPacket.write(0);
        return oPacket.getPacket();
    }

    public static byte[] LeaveField(int objectID, int animation, int characterID) {
        return LeaveField(objectID, animation, characterID, 0);
    }

    public static byte[] LeaveField(int objectID, int animation, int characterID, int delay) {
        return LeaveField(objectID, animation, characterID, 0, delay);
    }

    private static byte[] LeaveField(int objectID, int leaveType, int characterID, int slot, int delay) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.REMOVE_ITEM_FROM_MAP.getValue());
        oPacket.write(leaveType);
        oPacket.writeInt(objectID);
        if (leaveType == PickedUpByUser || leaveType == PickedUpByMob || leaveType == PickedUpByPet) {
            oPacket.writeInt(characterID);
        } else if (leaveType == Explode) {
            oPacket.writeShort(delay);
            return oPacket.getPacket();
        }
        if (leaveType == PickedUpByPet) {
            oPacket.writeInt(slot);
        }
        return oPacket.getPacket();
    }
}
