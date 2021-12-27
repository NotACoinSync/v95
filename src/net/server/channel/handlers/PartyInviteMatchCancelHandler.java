package net.server.channel.handlers;

import client.MapleClient;
import constants.ServerConstants;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

public final class PartyInviteMatchCancelHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (!ServerConstants.USE_PARTY_SEARCH) {
            return;
        }
    }
}
