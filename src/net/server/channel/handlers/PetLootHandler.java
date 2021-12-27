package net.server.channel.handlers;

import net.server.handlers.Player.SendDropPickUpRequest;
import java.awt.Point;
import java.util.Arrays;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.MaplePet;
import constants.ItemConstants;
import server.maps.MapleMapItem;
import server.maps.objects.MapleMapObject;
import server.maps.objects.MapleMapObjectType;
import tools.packets.PacketHelper;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.WvsContext;

public class PetLootHandler extends SendDropPickUpRequest {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        long petIndex = slea.readLong();// petLockerSN
        if (petIndex < 0) {
            c.announce(WvsContext.enableActions());
            return;
        }
        MaplePet pet = chr.getPet(chr.getPetIndex(petIndex));
        if (pet == null || !pet.isSummoned()) {
            return;
        }
        slea.readByte();
        slea.readInt();
        Point petPos = slea.readPos();
        int oid = slea.readInt();
        slea.readInt();// dwCliCrc
        slea.readBoolean();// bPickupOthers
        slea.readBoolean();// bSweepForDrop
        slea.readBoolean();// bLongRange
        MapleMapObject ob = chr.getMap().getMapObject(oid);
        if (ob == null) {
            c.announce(WvsContext.InventoryOperationFull());
            return;
        }
        if (ob instanceof MapleMapItem) {
            pickUpItem(c, pet, ob, (MapleMapItem) ob, petPos, false);
            if (chr.getItemQuantity(ItemConstants.PET_ITEM_VAC, false) >= 1 && c.getPetVac()) {
                for (MapleMapObject mpo : chr.getMap().getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.ITEM))) {
                    pickUpItem(c, pet, mpo, (MapleMapItem) mpo, petPos, true);
                }
            }
        }
        c.announce(WvsContext.enableActions());
    }
}
