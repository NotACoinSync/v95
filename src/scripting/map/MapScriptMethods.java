package scripting.map;

import client.MapleClient;
import client.MapleQuestStatus;
import scripting.AbstractPlayerInteraction;
import server.quest.MapleQuest;
import tools.packets.WvsContext;
import tools.packets.CField.userpool.UserLocal;
import tools.packets.EffectPacket;

public class MapScriptMethods extends AbstractPlayerInteraction {

    private String rewardstring = " title has been rewarded. Please see NPC Dalair to receive your Medal.";

    public MapScriptMethods(MapleClient c) {
        super(c);
    }

    @Override
    public void displayAranIntro() {
        switch (c.getPlayer().getMapId()) {
            case 914090010:
                lockUI();
                c.announce(EffectPacket.Local.ShowEffectByPath("Effect/Direction1.img/aranTutorial/Scene0"));
                break;
            case 914090011:
                c.announce(EffectPacket.Local.ShowEffectByPath("Effect/Direction1.img/aranTutorial/Scene1" + c.getPlayer().getGender()));
                break;
            case 914090012:
                c.announce(EffectPacket.Local.ShowEffectByPath("Effect/Direction1.img/aranTutorial/Scene2" + c.getPlayer().getGender()));
                break;
            case 914090013:
                c.announce(EffectPacket.Local.ShowEffectByPath("Effect/Direction1.img/aranTutorial/Scene3"));
                break;
            case 914090100:
                lockUI();
                c.announce(EffectPacket.Local.ShowEffectByPath("Effect/Direction1.img/aranTutorial/HandedPoleArm" + c.getPlayer().getGender()));
                break;
        }
    }

    public void startExplorerExperience() {
        switch (c.getPlayer().getMapId()) {
            case 1020100: // Swordman
                c.announce(EffectPacket.Local.ShowEffectByPath("Effect/Direction3.img/swordman/Scene" + c.getPlayer().getGender()));
                break;
            case 1020200: // Magician
                c.announce(EffectPacket.Local.ShowEffectByPath("Effect/Direction3.img/magician/Scene" + c.getPlayer().getGender()));
                break;
            case 1020300: // Archer
                c.announce(EffectPacket.Local.ShowEffectByPath("Effect/Direction3.img/archer/Scene" + c.getPlayer().getGender()));
                break;
            case 1020400: // Rogue
                c.announce(EffectPacket.Local.ShowEffectByPath("Effect/Direction3.img/rogue/Scene" + c.getPlayer().getGender()));
                break;            
            case 1020500: // Pirate
                c.announce(EffectPacket.Local.ShowEffectByPath("Effect/Direction3.img/pirate/Scene" + c.getPlayer().getGender()));
                break;
            default:
                break;
        }
    }

    public void goAdventure() {
        lockUI();
        c.announce(EffectPacket.Local.ShowEffectByPath("Effect/Direction3.img/goAdventure/Scene" + c.getPlayer().getGender()));
    }

    public void goLith() {
        lockUI();
        c.announce(EffectPacket.Local.ShowEffectByPath("Effect/Direction3.img/goLith/Scene" + c.getPlayer().getGender()));
    }

    public void explorerQuest(short questid, String questName) {
        MapleQuest quest = MapleQuest.getInstance(questid);
        if (!isQuestStarted(questid)) {
            if (!quest.forceStart(getPlayer(), 9000066)) {
                return;
            }
        }
        MapleQuestStatus q = getPlayer().getQuest(quest);
        if (!q.addMedalMap(getPlayer().getMapId())) {
            return;
        }
        String status = Integer.toString(q.getMedalProgress());
        String infoex = quest.getInfoEx();
        getPlayer().announce(WvsContext.OnMessage.updateQuest(q, q.getQuest().startQuestData.infoNumber));
        StringBuilder smp = new StringBuilder();
        StringBuilder etm = new StringBuilder();
        if (status.equals(infoex)) {
            etm.append("Earned the ").append(questName).append(" title!");
            smp.append("You have earned the <").append(questName).append(">").append(rewardstring);
            getPlayer().announce(WvsContext.QuestClear(quest.getId()));
        } else {
            getPlayer().announce(WvsContext.ScriptProgressMessage(status + "/" + infoex + " regions explored."));
            etm.append("Trying for the ").append(questName).append(" title.");
            smp.append("You made progress on the ").append(questName).append(" title. ").append(status).append("/").append(infoex);
        }
        getPlayer().announce(WvsContext.ScriptProgressMessage(etm.toString()));
        showInfoText(smp.toString());
    }

    public void touchTheSky() { // 29004
        MapleQuest quest = MapleQuest.getInstance(29004);
        if (!isQuestStarted(29004)) {
            if (!quest.forceStart(getPlayer(), 9000066)) {
                return;
            }
        }
        MapleQuestStatus q = getPlayer().getQuest(quest);
        if (!q.addMedalMap(getPlayer().getMapId())) {
            return;
        }
        String status = Integer.toString(q.getMedalProgress());
        getPlayer().announce(WvsContext.OnMessage.updateQuest(q, q.getQuest().startQuestData.infoNumber));
        getPlayer().announce(WvsContext.ScriptProgressMessage(status + "/5 Completed"));
        getPlayer().announce(WvsContext.ScriptProgressMessage("The One Who's Touched the Sky title in progress."));
        if (Integer.toString(q.getMedalProgress()).equals(quest.getInfoEx())) {
            showInfoText("The One Who's Touched the Sky" + rewardstring);
            getPlayer().announce(WvsContext.QuestClear(quest.getId()));
        } else {
            showInfoText("The One Who's Touched the Sky title in progress. " + status + "/5 Completed");
        }
    }
}
