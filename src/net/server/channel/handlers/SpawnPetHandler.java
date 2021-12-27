package net.server.channel.handlers;

import java.awt.Point;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import client.MapleCharacter;
import client.MapleClient;
import client.SkillFactory;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import net.AbstractMaplePacketHandler;
import server.ItemInformationProvider;
import server.MapleInventoryManipulator;
import tools.DatabaseConnection;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.WvsContext;
import tools.packets.PetPacket;

public final class SpawnPetHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        slea.readInt();
        byte slot = slea.readByte();
        slea.readByte();
        boolean lead = slea.readByte() == 1;
        MaplePet pet = chr.getInventory(MapleInventoryType.CASH).getItem(slot).getPet();
        if (pet == null) {
            return;
        }
        pet.setPosition(slot);
        int petid = pet.getItemId();
        if (petid == 5000028 || petid == 5000047) { // Handles Dragon AND Robos
            if (chr.haveItem(petid + 1)) {
                chr.dropMessage(5, "You can't hatch your " + (petid == 5000028 ? "Dragon egg" : "Robo egg") + " if you already have a Baby " + (petid == 5000028 ? "Dragon." : "Robo."));
                c.announce(WvsContext.enableActions());
                return;
            } else {
                int evolveid = ItemInformationProvider.getInstance().getItemData(petid).evol1;
                int petId = MaplePet.createPet(evolveid);
                if (petId == 0) {
                    return;
                }
                try {
                    PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("DELETE FROM pets WHERE `petid` = ?");
                    ps.setInt(1, pet.getUniqueId());
                    ps.executeUpdate();
                    ps.close();
                } catch (SQLException ex) {
                    Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
                }
                long expiration = chr.getInventory(MapleInventoryType.CASH).getItem(slot).getExpiration();
                MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, petid, (short) 1, true, false);
                Item item = new Item(evolveid, (short) 1);
                item.setPetId(petId);
                item.setExpiration(expiration);
                MapleInventoryManipulator.addFromDrop(c, item, true);
                c.announce(WvsContext.enableActions());
                return;
            }
        }
        if (chr.getPetIndex(pet) != -1) {
            chr.unequipPet(pet, true);
        } else {
            if (chr.getSkillLevel(SkillFactory.getSkill(8)) == 0 && chr.getPet(0) != null) {
                chr.unequipPet(chr.getPet(0), false);
            }
            if (lead) {
                chr.shiftPetsRight();
            }
            Point pos = chr.getPosition();
            pos.y -= 12;
            pet.setPos(pos);
            pet.setFh(chr.getMap().getMapData().getFootholds().findBelow(pet.getPos()).getId());
            pet.setStance(0);
            pet.setSummoned(true);
            pet.saveToDb();
            chr.addPet(pet);
            chr.getMap().announce(c.getPlayer(), PetPacket.showPet(c.getPlayer(), pet, false, false), true);
            c.announce(PetPacket.petStatUpdate(c.getPlayer()));
            c.announce(WvsContext.enableActions());
            chr.startFullnessSchedule(ItemInformationProvider.getInstance().getItemData(pet.getItemId()).hungry, pet, chr.getPetIndex(pet));
        }
    }
}
