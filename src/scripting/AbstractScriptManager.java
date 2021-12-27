package scripting;

import java.io.File;
import java.io.FileReader;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import client.MapleClient;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 @author Matze
 */
public abstract class AbstractScriptManager {

    private ScriptEngineManager sem;

    protected AbstractScriptManager() {
        sem = new ScriptEngineManager();
    }

    protected ScriptEngine getInvocable(String path, MapleClient c) {
        path = "scripts/" + path;
        File scriptFile = new File(path);
        if (!scriptFile.exists()) {
            return null;
        }
        ScriptEngine engine = null;
        if (c != null) {
            engine = c.getScriptEngine(path);
        }
        if (engine == null) {
            engine = sem.getEngineByName("nashorn");
            if (c != null) {
                c.setScriptEngine(path, engine);
            }
            try (FileReader fr = new FileReader(scriptFile)) {
                engine.eval(fr);
            } catch (Exception t) {
                Logger.log(LogType.ERROR, LogFile.EXCEPTION, t, path.substring(12, path.length()) + "\r\n" + path);
                return null;
            }
        }
        return engine;
    }

    protected boolean scriptExist(String path) {
        File scriptFile = new File(path);
        return scriptFile.exists();
    }

    protected void resetContext(String path, MapleClient c) {
        c.removeScriptEngine("scripts/" + path);
    }
}
