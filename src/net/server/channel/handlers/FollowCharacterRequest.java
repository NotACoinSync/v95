package net.server.channel.handlers;

import client.MapleCharacter;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.WvsContext;
import tools.packets.CField.userpool.UserLocal;
import tools.packets.CField.userpool.UserLocal.FollowCharacterFailType;
import tools.packets.CField.userpool.UserCommon;

/**
 *
 *
 * @since Oct 19, 2017
 */
public final class FollowCharacterRequest extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int targetID = slea.readInt();
        // 00 01 if movement cancel
        slea.readShort();// no one cares
        if (targetID <= 0) {
            if (c.getPlayer().driver != -1) {
                MapleCharacter driver = c.getPlayer().getMap().getCharacterById(c.getPlayer().driver);
                if (driver != null) {
                    driver.passenger = -1;
                    c.getPlayer().driver = -1;
                }
                c.getPlayer().getMap().announce(UserCommon.removeFollow(c.getPlayer().getId(), null));
                c.getPlayer().driver = -1;
            }
            return;
        }
        MapleCharacter target = c.getPlayer().getMap().getCharacterById(targetID);
        if (target == null) {
            c.announce(UserLocal.followCharacterFailed(FollowCharacterFailType.CANT_ACCEPT, 0));
            return;
        }
        target.requestedFollow.add(c.getPlayer().getId());
        target.getClient().announce(WvsContext.SetPassengerRequest(c.getPlayer().getId()));
    }
}
