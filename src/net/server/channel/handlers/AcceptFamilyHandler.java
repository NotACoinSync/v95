package net.server.channel.handlers;

import java.rmi.RemoteException;

import client.MapleCharacter;
import client.MapleClient;
import constants.FeatureSettings;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.WvsContext;
import tools.packets.FamilyPackets;

/**
 * @author Jay Estrella
 */
public final class AcceptFamilyHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (!FeatureSettings.FAMILY) {
            c.announce(WvsContext.enableActions());
            return;
        }
        if (c.getPlayer().getFamilyId() >= 0) {
            // check if they are the parent.
            System.out.println("B");
            return;
        }
        int inviterID = slea.readInt();
        MapleCharacter inviter = ChannelServer.getInstance().getCharacterById(inviterID);
        if (inviter == null) {
            // send msg
            System.out.println("A");
            return;
        }
        try {
            if (ChannelServer.getInstance().getWorldInterface().joinFamily(inviter.getFamilyId(), inviter.getId(), c.getPlayer().getId())) {
                c.announce(FamilyPackets.sendFamilyMessage(0, 0));
                inviter.getClient().announce(FamilyPackets.sendFamilyJoinResponse(true, c.getPlayer().getName()));
            } else {
                System.out.println("Failed");
            }
        } catch (RemoteException | NullPointerException ex) {
            Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
        }
        /*if(!ServerConstants.USE_FAMILY_SYSTEM) return;
		// System.out.println(slea.toString());
		int inviterId = slea.readInt();
		// String inviterName = slea.readMapleAsciiString();
		MapleCharacter inviter = c.getWorldServer().getCharacterById(inviterId);
		if(inviter != null){
			inviter.getClient().announce(MaplePacketCreator.sendFamilyJoinResponse(true, c.getPlayer().getName()));
		}
		c.announce(MaplePacketCreator.sendFamilyMessage(0, 0));*/
    }
}
