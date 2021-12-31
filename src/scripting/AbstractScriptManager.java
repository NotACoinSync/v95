package scripting;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import client.MapleClient;

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
            try (Stream<String> stream = Files.lines(scriptFile.toPath())) {
                String lines = "load('nashorn:mozilla_compat.js');";
                lines += stream.collect(Collectors.joining(System.lineSeparator()));
                engine.eval(lines);
            } catch (IOException | ScriptException t) {
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
