package net.server.channel.handlers;

import client.MapleCharacter;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CField.userpool.UserLocal;
import tools.packets.CField.userpool.UserCommon;
import tools.packets.WvsContext;

public final class SetPassengerResultHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int passengerID = slea.readInt();
        MapleCharacter chr = c.getPlayer().getMap().getCharacterById(passengerID);
        if (chr == null) {
            c.announce(UserLocal.followCharacterFailed(UserLocal.FollowCharacterFailType.CANT_ACCEPT, 0));
            return;
        }
        c.announce(WvsContext.SetPassenserRequest(passengerID));
        
        if (!c.getPlayer().requestedFollow.contains(passengerID)) {
            if (c.getPlayer().getParty() == null) {
                c.announce(UserLocal.followCharacterFailed(UserLocal.FollowCharacterFailType.UNKNOWN, 0));
                return;
            } else {
                if (c.getPlayer().getParty().getMemberById(passengerID) == null) {
                    c.announce(UserLocal.followCharacterFailed(UserLocal.FollowCharacterFailType.UNKNOWN, 0));
                    return;
                }
            }
        }
        if (slea.readBoolean()) {
            if (chr.driver != -1) {
                c.announce(UserLocal.followCharacterFailed(UserLocal.FollowCharacterFailType.ALREADY_FOLLOWING, 0));
                chr.announce(UserLocal.followCharacterFailed(UserLocal.FollowCharacterFailType.UNKNOWN, 0));
                c.getPlayer().requestedFollow.remove(Integer.valueOf(passengerID));
                return;
            }
            c.getPlayer().requestedFollow.remove(Integer.valueOf(passengerID));
            c.getPlayer().passenger = passengerID;
            chr.driver = c.getPlayer().getId();
            chr.getMap().announce(UserCommon.followCharacter(passengerID, c.getPlayer().getId()));
            // chr.announce(UserCommon.followCharacter(passengerID, c.getPlayer().getId()));
            // c.announce(UserCommon.followCharacter(passengerID, c.getPlayer().getId()));
        } else {
            int result = slea.readInt();// 5 = cancelled, 1 = unavailable.
            System.out.println("result: " + result);
            if (result == 1) {
                System.out.println(c.getPlayer().passenger + ", " + c.getPlayer().driver);
                chr.getClient().announce(UserLocal.followCharacterFailed(UserLocal.FollowCharacterFailType.CANT_ACCEPT, 0));
            } else {
                chr.getClient().announce(UserLocal.followCharacterFailed(UserLocal.FollowCharacterFailType.DENIED, 0));
            }
        }
    }
}
