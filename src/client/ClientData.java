package client;

/**
 * Information based on what the client is telling the server
 */
public class ClientData implements Data {

    private boolean onRope, onLadder;

    @Override
    public boolean isOnRope() {
        return onRope;
    }

    @Override
    public void setOnRope(boolean on) {
        this.onRope = on;
    }

    @Override
    public boolean isOnLadder() {
        return onLadder;
    }

    @Override
    public void setOnLadder(boolean on) {
        this.onLadder = on;
    }
}
