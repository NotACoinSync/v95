package server.maps;

import client.MapleClient;
import tools.Randomizer;
import tools.packets.Field;

public class MapleMapEffect {

    private String msg;
    private int itemId;
    private boolean active = true;
    private int rand;

    public MapleMapEffect(String msg, int itemId) {
        this.msg = msg;
        this.itemId = itemId;
        this.rand = Randomizer.nextInt();
    }

    public int getItemID() {
        return itemId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (active ? 1231 : 1237);
        result = prime * result + itemId;
        result = prime * result + ((msg == null) ? 0 : msg.hashCode());
        result = prime * result + rand;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MapleMapEffect other = (MapleMapEffect) obj;
        if (active != other.active) {
            return false;
        }
        if (itemId != other.itemId) {
            return false;
        }
        if (msg == null) {
            if (other.msg != null) {
                return false;
            }
        } else if (!msg.equals(other.msg)) {
            return false;
        }
        if (rand != other.rand) {
            return false;
        }
        return true;
    }

    public final byte[] makeDestroyData() {
        return Field.BlowWeather.resetMapEffects();
    }

    public final byte[] makeStartData() {
        return Field.BlowWeather.startMapEffect(msg, itemId, active);
    }

    public void sendStartData(MapleClient client) {
        if (itemId == 5120010 && !client.isNightOverlayEnabled()) {
            return;
        }
        client.announce(makeStartData());
    }
}
