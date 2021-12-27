package tools.packets;

import java.rmi.RemoteException;

import client.MapleCharacter;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import constants.ItemConstants;
import net.SendOpcode;
import net.channel.ChannelServer;
import net.server.guild.MapleGuildSummary;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CField.userpool.UserCommon;

public class UserPool {

    /**
     Gets a packet spawning a player as a mapobject to other clients.

     @param character The character to spawn to other clients.

     @return The spawn player packet.
     */
    public static byte[] UserEnterField(MapleCharacter character) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SPAWN_PLAYER.getValue());
        oPacket.writeInt(character.getId());
        // CUserRemote::Init
        oPacket.write(character.getLevel());
        oPacket.writeMapleAsciiString(character.getName());
        if (character.getGuildId() < 1) {
            oPacket.writeMapleAsciiString("");
            oPacket.write(new byte[6]);
        } else {
            MapleGuildSummary gs = null;
            try {
                gs = ChannelServer.getInstance().getWorldInterface().getGuildSummary(character.getGuildId());
            } catch (RemoteException | NullPointerException ex) {
                Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
            }
            if (gs != null) {
                oPacket.writeMapleAsciiString(gs.getName());
                oPacket.writeShort(gs.getLogoBG());
                oPacket.write(gs.getLogoBGColor());
                oPacket.writeShort(gs.getLogo());
                oPacket.write(gs.getLogoColor());
            } else {
                oPacket.writeMapleAsciiString("");
                oPacket.write(new byte[6]);
            }
        }
        character.secondaryStat.encodeRemote(oPacket, character.getAllStatups());
        //character.secondaryStat.encodeRemote_BackUp(oPacket, character.getAllStatups());
        oPacket.writeShort(character.getJob().getId());
        PacketHelper.encodeAvatarLook(oPacket, character, false);
        oPacket.writeInt(0);// m_dwDriverID
        oPacket.writeInt(0);// m_dwPassenserID
        oPacket.writeInt(Math.min(250, character.getInventory(MapleInventoryType.CASH).countById(5110000)));
        oPacket.writeInt(character.getItemEffect());// nActiveEffectItemID
        oPacket.writeInt(0);// m_nCompletedSetItemID
        oPacket.writeInt(ItemConstants.getInventoryType(character.getChair()) == MapleInventoryType.SETUP ? character.getChair() : 0);// m_nPortableChairID
        oPacket.writePos(character.getPosition());
        oPacket.write(character.getStance());
        oPacket.writeShort(0);// chr.getFh()
        oPacket.write(0);// bShowAdminEffect
        MaplePet[] pet = character.getPets();
        oPacket.write(character.getPets().length); // end of pets
        for (int i = 0; i < 3; i++) {
            if (pet[i] != null) {
                PetPacket.addPetInfo(oPacket, pet[i], true);
            }
        }
        if (character.getMount() == null) {
            oPacket.writeInt(1); // mob level
            oPacket.writeLong(0); // mob exp + tiredness
        } else {
            oPacket.writeInt(character.getMount().getLevel());
            oPacket.writeInt(character.getMount().getExp());
            oPacket.writeInt(character.getMount().getTiredness());
        }
        if (character.getPlayerShop() != null && character.getPlayerShop().isOwner(character)) {
            if (character.getPlayerShop().hasFreeSlot()) {
                UserCommon.MiniRoomBalloon.addAnnounceBox(oPacket, character.getPlayerShop(), character.getPlayerShop().getVisitors().length);
            } else {
                UserCommon.MiniRoomBalloon.addAnnounceBox(oPacket, character.getPlayerShop(), 1);
            }
        } else if (character.getMiniGame() != null && character.getMiniGame().isOwner(character)) {
            if (character.getMiniGame().hasFreeSlot()) {
                UserCommon.MiniRoomBalloon.addAnnounceBox(oPacket, character.getMiniGame(), 1, 0, 1, 0);
            } else {
                UserCommon.MiniRoomBalloon.addAnnounceBox(oPacket, character.getMiniGame(), 1, 0, 2, 1);
            }
        } else {
            oPacket.write(0);
        }
        if (character.getChalkboard() != null) {
            oPacket.write(1);
            oPacket.writeMapleAsciiString(character.getChalkboard());
        } else {
            oPacket.write(0);
        }
        PacketHelper.encodeRingLook(oPacket, character.getCrushRings());
        PacketHelper.encodeRingLook(oPacket, character.getFriendshipRings());
        PacketHelper.encodeMarriageRingLook(oPacket, character);
        oPacket.write(0);// some effect shit. 0x1 CUser::LoadDarkForceEffect, 0x2 CDragon::CreateEffect, 0x4 CUser::LoadSwallowingEffect, other 0x8 0x10 0x20
        oPacket.write(0);// (boolean)new year card record add, int size, 1 int in the loop
        oPacket.writeInt(0);// m_nPhase       
        oPacket.write(character.getTeam()); 
        return oPacket.getPacket();
    }

    public static byte[] UserLeaveField(int characterId) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.REMOVE_PLAYER_FROM_MAP.getValue());
        oPacket.writeInt(characterId);
        return oPacket.getPacket();
    }
}
