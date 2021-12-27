package net.server.channel.handlers;

import client.MapleCharacter;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import server.maps.objects.MapleMapObject;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.WvsContext;

public final class CharInfoRequestHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readInt();
        int cid = slea.readInt();
        MapleMapObject target = c.getPlayer().getMap().getMapObject(cid);
        if (target != null) {
            if (target instanceof MapleCharacter) {
                MapleCharacter chr = (MapleCharacter) target;
                if (chr.isGM() && !c.getPlayer().isGM()) {
                    c.announce(WvsContext.enableActions());
                } else {
                    c.announce(WvsContext.CharacterInfo(chr));
                }
            }
        }
    }
}
