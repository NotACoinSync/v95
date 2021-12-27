package net.server.handlers.Guild;

import client.MapleCharacter;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.WvsContext;

public final class DenyGuildRequestHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        iPacket.readByte(); // debug info
        MapleCharacter playerFrom = c.getChannelServer().getPlayerStorage().getCharacterByName(iPacket.readMapleAsciiString());
        if (playerFrom != null) {
            playerFrom.getClient().announce(WvsContext.GuildResult.denyGuildInvitation(c.getPlayer().getName()));
        } else {
            c.disconnect(true, false);
        }
    }
}
