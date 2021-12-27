package net.server.channel.handlers;

import client.MapleClient;
import client.autoban.AutobanFactory;
import net.AbstractMaplePacketHandler;
import server.ItemInformationProvider;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CField.userpool.UserRemote;

public final class FaceExpressionHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int emote = slea.readInt();
        boolean usedCashItem = false;
        if (emote > 7 && emote < (5170000 - 5159992)) {
            usedCashItem = true;
            int emoteid = 5159992 + emote;
            if (c.getPlayer().getInventory(ItemInformationProvider.getInstance().getInventoryType(emoteid)).findById(emoteid) == null) {
                AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use Face Expression on invalid(or null item) emote: " + emoteid);
                return;
            }
        }
        c.getPlayer().getMap().announce(c.getPlayer(), UserRemote.Emotion(c.getPlayer().getId(), emote, usedCashItem), false);
    }
}
