package net.server.channel.handlers;

import client.MapleCharacter;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import server.maps.objects.MapleDoor;
import server.maps.objects.MapleMapObject;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.WvsContext;

public final class DoorHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int chrid = slea.readInt();
        byte mode = slea.readByte(); // specifies if backwarp or not, 1 town to target, 0 target to town
        for (MapleMapObject obj : c.getPlayer().getMap().getMapObjects()) {
            if (obj instanceof MapleDoor) {
                MapleDoor door = (MapleDoor) obj;
                MapleCharacter owner = door.getOwnerInstance();
                if (owner != null && owner.getId() == chrid) {// if the door is the proper one.
                    if (c.getPlayer().getId() == owner.getId() || (owner.getPartyId() > 0 && owner.getPartyId() == c.getPlayer().getPartyId())) {// if in party, or owner.
                        if (mode == 0) {
                            c.getPlayer().changeMapPortalPosition(door.getTown(), door.getTownPortal());
                        } else {
                            c.getPlayer().changeMapPosition(door.getTarget(), door.getTargetPosition());
                        }
                        c.announce(WvsContext.enableActions());
                        break;
                    }
                }
            }
        }
    }
}
