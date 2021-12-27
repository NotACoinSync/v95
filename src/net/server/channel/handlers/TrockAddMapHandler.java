package net.server.channel.handlers;

import client.MapleCharacter;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import server.maps.FieldLimit;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.WvsContext;

public final class TrockAddMapHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        byte type = slea.readByte();
        boolean vip = slea.readByte() == 1;
        switch (type) {
            case 0: {
                int mapId = slea.readInt();
                if (vip) {
                    chr.deleteFromVipTrocks(mapId);
                } else {
                    chr.deleteFromTrocks(mapId);
                }
                c.announce(WvsContext.MapTransferResult.UpdateFieldList(chr, true, vip));
                break;
            }
            case 1: {
                if (!FieldLimit.CANNOTVIPROCK.check(chr.getMap().getMapData().getFieldLimit())) {
                    if (vip) {
                        chr.addVipTrockMap();
                    } else {
                        chr.addTrockMap();
                    }
                    c.announce(WvsContext.MapTransferResult.UpdateFieldList(chr, false, vip));
                } else {
                    chr.message("You may not save this map.");
                }
                break;
            }
            default: {
                c.announce(WvsContext.MapTransferResult.encode(type, vip));
                break;
            }
        }
    }
}
