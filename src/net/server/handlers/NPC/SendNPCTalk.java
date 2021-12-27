package net.server.handlers.NPC;

import client.MapleCharacter;
import client.MapleClient;
import constants.FeatureSettings;
import net.AbstractMaplePacketHandler;
import scripting.npc.NPCScriptManager;
import server.life.MapleNPC;
import server.life.MapleNPCStats.NPCScriptData;
import server.maps.objects.MapleMapObject;
import server.maps.objects.PlayerNPC;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.WvsContext;

public final class SendNPCTalk extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (player == null) {
            c.disconnect(true, false);
            return;
        }
        if (!player.isAlive()) {
            c.announce(WvsContext.enableActions());
            return;
        }
        int objectId = iPacket.readInt();
        iPacket.readInt();
        MapleMapObject mmo = player.getMap().getMapObject(objectId);
        if (mmo == null) {
            return;
        }
        if (mmo instanceof MapleNPC) {
            MapleNPC npc = (MapleNPC) mmo;
            if (npc.getId() == 9010009 && FeatureSettings.PACKAGE_DELIVERER) {
                // c.announce(MaplePacketCreator.sendDuey((byte) 8, DueyHandler.loadItems(player)));
                c.announce(WvsContext.enableActions());
            } else if (!npc.hasShop()) {
                if (c.getCM() != null) {
                    if (c.getCM().getNpc() == npc.getId()) {
                        c.announce(WvsContext.enableActions());
                        return;
                    } else {
                        player.dispose();
                    }
                }
                if (c.getQM() != null) {
                    c.announce(WvsContext.enableActions());
                    return;
                }
                if (npc.getId() >= 9100100 && npc.getId() < 9100120) {
                    // Custom handling for gachapon scripts to reduce the amount of scripts needed.
                    NPCScriptManager.getInstance().start(c, npc.getId(), "gachapon", null);
                } else {
                    if (npc.stats != null) {
                        NPCScriptData data = npc.stats.getScriptData().get(0);
                        if (data != null && data.script != null) {
                            NPCScriptManager.getInstance().start(c, npc.getId(), data.script, null);
                            return;
                        }
                    }
                    try {
                        NPCScriptManager.getInstance().start(c, npc.getId(), null);
                    } catch (Exception ex) {
                    }
                }
            } else if (npc.hasShop()) {
                if (player.getShop() != null) {
                    return;
                }
                npc.sendShop(c);
            }
        } else if (mmo instanceof PlayerNPC) {
            NPCScriptManager.getInstance().start(c, ((PlayerNPC) mmo).getId(), null);
        } else {
            
        }
    }
}
