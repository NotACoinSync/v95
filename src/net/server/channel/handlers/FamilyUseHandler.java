package net.server.channel.handlers;

import client.MapleCharacter;
import client.MapleClient;
import client.MessageType;
import constants.FeatureSettings;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.packets.WvsContext;

public final class FamilyUseHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (!FeatureSettings.FAMILY) {
            c.getPlayer().dropMessage(MessageType.POPUP, FeatureSettings.FAMILY_DISABLED);
            c.announce(WvsContext.enableActions());
            return;
        }
        int[] repCost = {3, 5, 7, 8, 10, 12, 15, 20, 25, 40, 50};
        final int type = slea.readInt();
        MapleCharacter victim;
        if (type == 0 || type == 1) {
            victim = c.getChannelServer().getPlayerStorage().getCharacterByName(slea.readMapleAsciiString());
            if (victim != null) {
                if (type == 0) {
                    c.getPlayer().changeMap(victim.getMap(), victim.getMap().getPortal(0));
                } else {
                    victim.changeMap(c.getPlayer().getMap(), c.getPlayer().getMap().getPortal(0));
                }
            } else {
                return;
            }
        } else {
            int erate = type == 3 ? 150 : (type == 4 || type == 6 || type == 8 || type == 10 ? 200 : 100);
            int drate = type == 2 ? 150 : (type == 4 || type == 5 || type == 7 || type == 9 ? 200 : 100);
            if (type > 8) {
            } else {
                c.announce(useRep(drate == 100 ? 2 : (erate == 100 ? 3 : 4), type, erate, drate, ((type > 5 || type == 4) ? 2 : 1) * 15 * 60 * 1000));
            }
        }
        // c.getPlayer().getFamily().getMember(c.getPlayer().getId()).gainReputation(repCost[type]);
    }
    
    private static byte[] useRep(int mode, int type, int erate, int drate, int time) {
        MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(0x60);// noty
        oPacket.write(mode);
        oPacket.writeInt(type);
        if (mode < 4) {
            oPacket.writeInt(erate);
            oPacket.writeInt(drate);
        }
        oPacket.write(0);
        oPacket.writeInt(time);
        return oPacket.getPacket();
    }
}
