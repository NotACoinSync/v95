package server.life;

import java.util.HashMap;
import java.util.Map;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.LittleEndianWriter;
import tools.data.output.MaplePacketLittleEndianWriter;

public class MapleNPCStats {

    private String name;
    private Map<Integer, NPCScriptData> scriptData = new HashMap<>();
    public boolean imitate;
    public boolean move;

    public MapleNPCStats(String name) {
        this.name = name;
    }

    public MapleNPCStats() {
        super();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Integer, NPCScriptData> getScriptData() {
        return scriptData;
    }

    public void addScriptData(int key, NPCScriptData scriptData) {
        this.scriptData.put(key, scriptData);
    }

    public void save(MaplePacketLittleEndianWriter oPacket) {
        oPacket.writeMapleAsciiString(name);
        oPacket.write(scriptData.size());
        for (int key : scriptData.keySet()) {
            oPacket.writeInt(key);
            scriptData.get(key).save(oPacket);
        }
        oPacket.writeBoolean(imitate);
        oPacket.writeBoolean(move);
    }

    public void load(LittleEndianAccessor lea) {
        name = lea.readMapleAsciiString();
        int size = lea.readByte();
        for (int i = 0; i < size; i++) {
            int key = lea.readInt();
            NPCScriptData data = new NPCScriptData();
            data.load(lea);
            scriptData.put(key, data);
        }
        imitate = lea.readBoolean();
        move = lea.readBoolean();
    }

    public static class NPCScriptData {

        public String script;
        public int start, end;

        public NPCScriptData() {
            super();
        }

        public NPCScriptData(String script, int start, int end) {
            this.script = script;
            this.start = start;
            this.end = end;
        }

        public void save(LittleEndianWriter lew) {
            lew.writeMapleAsciiString(script);
            lew.writeInt(start);
            lew.writeInt(end);
        }

        public void load(LittleEndianAccessor lea) {
            script = lea.readMapleAsciiString();
            start = lea.readInt();
            end = lea.readInt();
        }
        /*
		 public void UpdateScript() {
		lock.lock();
		try {
		SystemTime st = SystemTime.GetLocalTime();
		long tDate = st.wDay + 100 * (st.wMonth + 100 * st.wYear);
		
		for (ScriptInfo scriptInfo: lScriptInfo) {
		    sScript = scriptInfo.sScript;
		
		    if (scriptInfo.tStartDate <= tDate) {
		        if (tDate <= scriptInfo.tEndDate)
		            break;
		    }
		}
		} finally {
		lock.unlock();
		}
		}
		
         */
    }
}
