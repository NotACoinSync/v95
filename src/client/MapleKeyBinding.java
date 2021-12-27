package client;

public class MapleKeyBinding {

    private int type, action;

    public MapleKeyBinding(int type, int action) {
        this.type = type;
        this.action = action;
    }

    public int getType() {
        return type;
    }

    public int getAction() {
        return action;
    }
}
