package net.server.handlers.Item;

import client.MapleCharacter;
import client.MapleClient;
import client.autoban.AutobanFactory;
import client.inventory.Item;
import net.AbstractMaplePacketHandler;
import scripting.item.ItemScriptManager;
import scripting.npc.NPCScriptManager;
import server.ItemInformationProvider;
import server.ItemInformationProvider.scriptedItem;
import tools.data.input.SeekableLittleEndianAccessor;

public final class SendScriptRunItemRequest extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (player == null) {
            c.disconnect(true, false);
            return;
        }
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        int timestamp = iPacket.readInt();
        player.getAutobanManager().setTimestamp(4, timestamp, 3);
        short slot = iPacket.readShort();
        int itemId = iPacket.readInt();
        scriptedItem info = ii.getItemData(itemId).scriptedItem;
        if (info == null || info.npc == 0) {
            AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use an invalid scripted item: " + itemId);
            return;
        }
        ItemScriptManager ism = ItemScriptManager.getInstance();
        Item toUse = player.getInventory(ii.getInventoryType(itemId)).getItem(slot);
        if (toUse != null && toUse.getQuantity() > 0) {
            if (toUse.getItemId() != itemId && (toUse.getItemId() / 10000 != 243 || toUse.getItemId() == 3994225)) {
                AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use mismatched item in SendScriptRunItemRequest");
                return;
            }
            ism.getItemScript(c, info.getScript());
            player.dispose();
            NPCScriptManager.getInstance().start(c, info.getNpc(), player);
        } else {
            AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use a null item in SendScriptRunItemRequest");
        }
    }
}
