package tools.packets;

import client.MapleCharacter;
import client.MapleStat;
import client.inventory.MaplePet;
import net.SendOpcode;
import server.movement.MovePath;
import tools.data.output.MaplePacketLittleEndianWriter;

public class PetPacket {

    public static byte[] showPet(MapleCharacter chr, MaplePet pet, boolean remove, boolean hunger) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SPAWN_PET.getValue());
        oPacket.writeInt(chr.getId());
        oPacket.write(chr.getPetIndex(pet));
        if (remove) {
            oPacket.write(0);
            oPacket.write(hunger ? 1 : 0);
        } else {
            addPetInfo(oPacket, pet, true);
        }
        return oPacket.getPacket();
    }
    
    public static byte[] movePet(int cid, byte slot, MovePath moves) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MOVE_PET.getValue());
        oPacket.writeInt(cid);
        oPacket.write(slot);
        moves.encode(oPacket);
        return oPacket.getPacket();
    }

    public static byte[] petChat(int cid, byte index, int n, int nAction, String text) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PET_CHAT.getValue());
        oPacket.writeInt(cid);
        oPacket.write(index);//
        oPacket.write(n);
        oPacket.write(nAction);
        oPacket.writeMapleAsciiString(text);
        oPacket.write(0);// bChatBalloon
        /*
            COutPacket::COutPacket(&oPacket, 125, 0);
            v6 = v2->m_pOwner->m_dwCharacterID;
            LOBYTE(v13) = 3;
            COutPacket::Encode4(&oPacket, v6);
            COutPacket::Encode1(&oPacket, n[0]);
            COutPacket::Encode1(&oPacket, nAction);
            v6 = 0;
            ZXString<char>::operator_((ZXString<char> *)&v6, &result);
            COutPacket::EncodeStr(&oPacket, (ZXString<char>)v6);
            CPet::UpdatePetAbility(v2);
            COutPacket::Encode1(&oPacket, v2->m_bChatBalloon);
         */
        return oPacket.getPacket();
    }

    public static byte[] changePetName(MapleCharacter chr, String newname, int slot) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PET_NAMECHANGE.getValue());
        oPacket.writeInt(chr.getId());
        oPacket.write(0);
        oPacket.writeMapleAsciiString(newname);
        oPacket.write(0);
        return oPacket.getPacket();
    }

    public static byte[] petExceptionListResult(MapleCharacter chr, MaplePet pet) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PET_EXCEPTION_LIST_RESULT.getValue());
        oPacket.writeInt(chr.getId());
        oPacket.write(chr.getPetIndex(pet));
        oPacket.writeLong(pet.getUniqueId());// liPetSN
        oPacket.write(pet.getExceptionList().size());
        for (int itemid : pet.getExceptionList()) {
            oPacket.writeInt(itemid);
        }
        return oPacket.getPacket();
    }   

    public static byte[] commandResponse(int cid, byte index, int animation, boolean success) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PET_COMMAND.getValue());
        oPacket.writeInt(cid);
        oPacket.write(index);
        oPacket.write(0);// v3, if set to 1 it requires a cc, map change, etc
        oPacket.write(animation);// n[0]
        oPacket.writeBoolean(success);// thisa
        oPacket.writeBoolean(false);// m_bChatBalloon
        return oPacket.getPacket();
    }
    
    public static byte[] petStatUpdate(MapleCharacter chr) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.STAT_CHANGED.getValue());
        int mask = 0;
        mask |= MapleStat.PETSN.getValue();
        oPacket.write(0);
        oPacket.writeInt(mask);
        MaplePet[] pets = chr.getPets();
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                oPacket.writeInt(pets[i].getUniqueId());
                oPacket.writeInt(0);
            } else {
                oPacket.writeLong(0);
            }
        }
        oPacket.write(0);
        return oPacket.getPacket();
    }

    public static void addPetInfo(final MaplePacketLittleEndianWriter oPacket, MaplePet pet, boolean showpet) {// CPet::Init
        oPacket.write(1);
        if (showpet) {
            oPacket.write(0);
        }
        oPacket.writeInt(pet.getItemId());
        oPacket.writeMapleAsciiString(pet.getName());
        oPacket.writeInt(pet.getUniqueId());
        oPacket.writeInt(0);// this is a long^
        oPacket.writePos(pet.getPos());
        oPacket.write(pet.getStance());
        oPacket.writeShort(pet.getFh());
        oPacket.write(0);// m_bNameTag
        oPacket.write(0);// m_bChatBalloon
    }
}
