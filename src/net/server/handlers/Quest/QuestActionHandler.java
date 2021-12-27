package net.server.handlers.Quest;

import client.MapleCharacter;
import client.MapleClient;
import client.MessageType;
import java.awt.Point;
import net.AbstractMaplePacketHandler;
import scripting.quest.QuestScriptManager;
import server.quest.MapleQuest;
import tools.data.input.SeekableLittleEndianAccessor;

public final class QuestActionHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (player == null) {
            c.disconnect(true, false);
            return;
        }
        byte action = iPacket.readByte();
        short questId = iPacket.readShort();
        MapleQuest quest = MapleQuest.getInstance(questId);
        Point UserPos = new Point();
        switch (action) {
            case 1: {
                // Start Quest
                int npcTemplateID = iPacket.readInt();
                short x = iPacket.readShort();
                short y = iPacket.readShort();
                UserPos.setLocation(x, y);
                quest.start(player, npcTemplateID);
                break;
            }
            case 2: {
                // Complete Quest
                int npcTemplateID = iPacket.readInt();
                short x = iPacket.readShort();
                short y = iPacket.readShort();
                UserPos.setLocation(x, y);
                if (iPacket.available() >= 2) {
                    int selection = iPacket.readShort();
                    quest.complete(player, npcTemplateID, selection);
                } else {
                    quest.complete(player, npcTemplateID);
                }
                break;
            }
            case 3:
                // forfeit quest
                quest.forfeit(player);
                break;
            case 4: {
                // Start Script Linked Quest
                int npcTemplateID = iPacket.readInt();
                short x = iPacket.readShort();
                short y = iPacket.readShort();
                UserPos.setLocation(x, y);
                if (quest.canStart(player, npcTemplateID)) {
                    QuestScriptManager.getInstance().start(c, questId, npcTemplateID);
                } else if (c.getPlayer().getScriptDebug()) {
                    c.getPlayer().dropMessage(MessageType.MAPLETIP, "You can't start scripted Quest " + quest.getId() + " from " + npcTemplateID);
                }
                break;
            }
            case 5: {
                // Complete Script Linked Quest
                int npcTemplateID = iPacket.readInt();
                short x = iPacket.readShort();
                short y = iPacket.readShort();
                UserPos.setLocation(x, y);
                if (quest.canComplete(player, npcTemplateID)) {
                    QuestScriptManager.getInstance().end(c, questId, npcTemplateID);
                } else if (c.getPlayer().getScriptDebug()) {
                    c.getPlayer().dropMessage(MessageType.MAPLETIP, "You can't complete scripted Quest " + quest.getId() + " from " + npcTemplateID);
                }
                break;
            }
            default:
                break;
        }
    }
}
