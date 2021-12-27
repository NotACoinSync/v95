package net.server;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import client.MapleCharacter;
import client.MapleClient;
import tools.ObjectParser;

public class PlayerStorage {

    private final Map<Integer, MapleCharacter> storage = new ConcurrentHashMap<>();

    public void addPlayer(MapleCharacter chr) {
        storage.put(chr.getId(), chr);
    }

    public MapleCharacter removePlayer(int chr) {
        return storage.remove(chr);
    }

    public MapleCharacter getCharacterByName(String name) {
        Integer in = ObjectParser.isInt(name);
        for (MapleCharacter chr : storage.values()) {
            if (chr.getName().toLowerCase().equals(name.toLowerCase())) {
                return chr;
            }
            if (in != null && chr.getId() == in) {
                return chr;
            }
        }
        return null;
    }

    public MapleCharacter getCharacterById(int id) {
        return storage.get(id);
    }

    public Collection<MapleCharacter> getAllCharacters() {
        return storage.values();
    }

    public final MapleCharacter getFinalMC(int id) {
        final MapleCharacter mc = getCharacterById(id);
        return mc;
    }

    public final void disconnectAll() {
        storage.values().forEach(mc -> {
            if (mc != null) {
                final MapleClient client = mc.getClient();
                if (client != null) {
                    client.disconnect(true, false);
                    client.updateLoginState(MapleClient.LOGIN_NOTLOGGEDIN);
                    if (client.getSession() != null) {// Discord bot
                        client.getSession().attr(MapleClient.CLIENT_KEY).set(null); // prevents double dcing during login
                        client.getSession().close();
                    }
                }
            }
        });
        storage.clear();
    }

    public int getSize() {
        return storage.size();
    }
}
