package net.server.channel.handlers;

import client.MapleCharacter;
import client.MapleClient;
import client.autoban.AutobanFactory;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.PetDataFactory.PetData;
import net.AbstractMaplePacketHandler;
import server.ItemData;
import server.ItemInformationProvider;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.WvsContext;
import tools.packets.PetPacket;

public final class PetCommandHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        int petId = slea.readInt();
        byte petIndex = chr.getPetIndex(petId);
        MaplePet pet;
        if (petIndex == -1) {
            return;
        } else {
            pet = chr.getPet(petIndex);
        }
        // decode 1,
        // v4 = decode1
        /*int i = */
        slea.readInt();
        /*int b = */
        slea.readByte();
        int command = slea.readByte();
        // System.out.println("PetCommand: " + "i: " + i + " b: " + b + " command: " + command);
        ItemData data = ItemInformationProvider.getInstance().getItemData(pet.getItemId());
        PetData petData = data.petData.get("" + command);
        if (petData == null) {
            AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use invalid pet command: " + command);
            return;
        }
        boolean success = false;
        if (Randomizer.nextInt(101) <= petData.prob) {
            success = true;
            pet.gainCloseness(chr, petData.inc);
            Item petz = chr.getInventory(MapleInventoryType.CASH).getItem(pet.getPosition());
            chr.forceUpdateItem(petz);
        }
        chr.getMap().announce(c.getPlayer(), PetPacket.commandResponse(chr.getId(), petIndex, command, success), true);
        c.announce(WvsContext.enableActions());
    }
}
