package net.server.handlers.Item;

import client.MapleCharacter;
import client.MapleClient;
import client.autoban.AutobanFactory;
import net.AbstractMaplePacketHandler;
import scripting.npc.NPCScriptManager;
import server.ItemInformationProvider;
import tools.data.input.SeekableLittleEndianAccessor;

public final class SendUseGachaponRemoteRequest extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (player == null) {
            c.disconnect(false, false);
            return;
        }
        int ticketId = slea.readInt();
        int gachaponId = slea.readInt();
        if (ticketId != 5451000) {
            AutobanFactory.GENERAL.alert(player, " Tried to use RemoteGachaponHandler with item id: " + ticketId);
            c.disconnect(false, false);
            return;
        } else if (gachaponId < 0 || gachaponId > 11) {
            NPCScriptManager.getInstance().start(c, gachaponId, null);
            return;
        } else if (player.getInventory(ItemInformationProvider.getInstance().getInventoryType(ticketId)).countById(ticketId) < 1) {
            AutobanFactory.GENERAL.alert(c.getPlayer(), " Tried to use RemoteGachaponHandler without a ticket.");
            c.disconnect(false, false);
            return;
        }
        int npcId = 9100100;
        if (gachaponId != 8 && gachaponId != 9) {
            npcId += gachaponId;
        } else {
            npcId = gachaponId == 8 ? 9100109 : 9100117;
        }
        player.dispose();
        NPCScriptManager.getInstance().start(c, npcId, "gachaponRemote", null);
    }
}
