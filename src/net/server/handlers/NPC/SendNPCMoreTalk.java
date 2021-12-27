package net.server.handlers.NPC;

import client.MapleCharacter;
import client.MapleClient;
import client.autoban.AutobanFactory;
import net.AbstractMaplePacketHandler;
import scripting.npc.NPCConversationManager;
import scripting.npc.NPCScriptManager;
import scripting.npc.NpcTalkData;
import scripting.npc.ScriptMessageType;
import scripting.quest.QuestScriptManager;
import tools.data.input.SeekableLittleEndianAccessor;

public final class SendNPCMoreTalk extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (player == null) {
            c.disconnect(true, false);
            return;
        }
        try {
            NPCConversationManager manager = c.getQM() != null ? c.getQM() : c.getCM();
            byte lastMsg = iPacket.readByte(); // 00 (last msg type I think)
            if (lastMsg == 6) {
                String text = iPacket.readMapleAsciiString();
                if (manager != null) {
                    manager.setGetText(text);
                    NPCScriptManager.getInstance().action(c, (byte) 1, lastMsg, -1);
                }
            } else if (lastMsg == 3) {
                byte action = iPacket.readByte(); // 00 = end chat, 01 == follow
                if (action != 0) {
                    String returnText = iPacket.readMapleAsciiString();
                    if (c.getQM() != null) {
                        c.getQM().setGetText(returnText);
                        if (c.getQM().isStart()) {
                            QuestScriptManager.getInstance().start(c, action, lastMsg, -1);
                        } else {
                            QuestScriptManager.getInstance().end(c, action, lastMsg, -1);
                        }
                    } else {
                        manager.setGetText(returnText);
                        NPCScriptManager.getInstance().action(c, action, lastMsg, -1);
                    }
                } else if (c.getQM() != null) {
                    player.dispose();
                } else {
                    player.dispose();
                }
            } else {
                byte mode = iPacket.readByte(); // 00 = end chat, 01 == follow
                int sel = 0;
                if (iPacket.available() >= 4) {
                    sel = iPacket.readInt();
                } else if (iPacket.available() > 0) {
                    sel = iPacket.readByte();
                }
                int selection = -1;
                String data = "";
                if (c.getQM() != null) {
                    data += "Quest: " + c.getQM().getQuest();
                } else if (c.getCM() != null) {
                    data += "NPC: " + c.getCM().getNpc() + "(" + c.getCM().getScriptName() + ") Text: " + c.getCM().getText();
                }
                if (mode == -1) {
                    player.dispose();
                    return;
                }
                if (manager != null) {
                    NpcTalkData talkData = manager.getTalkData();
                    if (talkData != null) {
                        ScriptMessageType messageType = talkData.messageType;
                        if (messageType != null) {
                            if (messageType.getMsgType() != lastMsg) {
                                AutobanFactory.PACKET_EDIT.alert(player, "Last message type doesn't match. Sent: " + ScriptMessageType.getType(lastMsg) + " Expected: " + messageType + " Npc: " + c.getCM().getNpc());
                            }
                            if (mode == 1) {
                                if (!talkData.next && messageType.equals(ScriptMessageType.Say)) {
                                    player.dispose();
                                    return;
                                }
                            } else if (mode == 0 && messageType.equals(ScriptMessageType.AskMenu)) {
                                if (manager.getScriptName() != null && manager.getScriptName().equals("ironman")) {
                                    if (player.getHardMode() == 0 && player.getIronMan() == 0) {
                                        player.setHardMode(-1);
                                        player.setIronMan(-1);
                                    }
                                }
                                player.dispose();
                                return;
                            }
                        }
                        if (talkData.max != 0 && sel > talkData.max) {
                            AutobanFactory.PACKET_EDIT.alert(player, "Invalid selection max: " + talkData.max + " input: " + sel + " Npc: " + c.getCM().getNpc());
                            sel = talkData.max;
                        }
                        if (sel < talkData.min) {
                            AutobanFactory.PACKET_EDIT.alert(player, "Invalid selection min: " + talkData.min + " input: " + sel + " Npc: " + c.getCM().getNpc());
                            sel = talkData.min;
                        }
                        if (!talkData.validSelections.isEmpty()) {
                            if (!talkData.validSelections.contains(sel)) {
                                AutobanFactory.PACKET_EDIT.alert(player, "Invalid selection: " + sel + " Valid: " + talkData.validSelections.toString() + " Npc: " + c.getCM().getNpc());
                            }
                        }
                    }
                }
                if (lastMsg == ScriptMessageType.AskNumber.getMsgType()) {
                    /*if(sel == 0){
						AutobanFactory.PACKET_EDIT.alert(player, "Tried to input invalid selection 0");
						// Logger.log(LogType.INFO, LogFile.ANTICHEAT, player.getName() + " tried to input invalid selection 0");
						selection = 1;
					}*/
                    if (c.getCM() != null) {
                        if (c.getCM().getTalkData() != null) {
                            NpcTalkData talkData = c.getCM().getTalkData();
                            if (sel < talkData.min || sel > talkData.max) {
                                AutobanFactory.PACKET_EDIT.alert(player, "Tried to input invalid selection " + sel + " when min is: " + talkData.min + " and max is: " + talkData.max + " Npc: " + c.getCM().getNpc());
                                // Logger.log(LogType.INFO, LogFile.ANTICHEAT, player.getName() + " tried to input invalid selection " + sel + " when min is: " + talkData.min + " and max is: " + talkData.max);
                                selection = talkData.def;
                            }
                        }
                    }
                }
                if (sel < 0) {
                    // Logger.log(LogType.INFO, LogFile.ANTICHEAT, player.getName() + " tried negative selection: " + sel);
                    AutobanFactory.PACKET_EDIT.alert(player, "Tried negative selection: " + sel + " Npc: " + c.getCM().getNpc());
                    selection = 0;
                } else {
                    selection = sel;
                }
                data += " Selection: " + selection;
                if (c.getQM() != null) {
                    if (c.getQM().isStart()) {// lastMsg = type
                        QuestScriptManager.getInstance().start(c, mode, lastMsg, selection);
                    } else {
                        QuestScriptManager.getInstance().end(c, mode, lastMsg, selection);
                    }
                } else if (c.getCM() != null) {
                    NPCScriptManager.getInstance().action(c, mode, lastMsg, selection);
                }
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }
}
