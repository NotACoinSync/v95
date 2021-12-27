package server.quest.actions;

import client.MapleCharacter;
import client.MapleQuestStatus;
import provider.MapleData;
import provider.MapleDataTool;
import server.quest.MapleQuest;
import server.quest.MapleQuestActionType;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.LittleEndianWriter;
import tools.packets.CField.userpool.UserLocal;

public class NextQuestAction extends MapleQuestAction {

    int nextQuest;

    public NextQuestAction(MapleQuest quest, MapleData data) {
        super(MapleQuestActionType.NEXTQUEST, quest);
        processData(data);
    }

    public NextQuestAction(MapleQuest quest, LittleEndianAccessor lea) {
        super(MapleQuestActionType.NEXTQUEST, quest);
        processData(lea);
    }

    @Override
    public void processData(MapleData data) {
        nextQuest = MapleDataTool.getInt(data);
    }

    @Override
    public void processData(LittleEndianAccessor lea) {
        nextQuest = lea.readInt();
    }

    @Override
    public void writeData(LittleEndianWriter lew) {
        lew.writeInt(nextQuest);
    }

    @Override
    public void run(MapleCharacter chr, Integer extSelection) {
        MapleQuestStatus status = chr.getQuest(MapleQuest.getInstance(questID));
        chr.announce(UserLocal.QuestResult.updateQuestFinish((short) questID, status.getNpc(), (short) nextQuest));
    }
}
