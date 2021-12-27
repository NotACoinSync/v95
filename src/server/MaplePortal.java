package server;

import java.awt.Point;
import client.MapleClient;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;

public interface MaplePortal {

    public final int MAP_PORTAL = 2;
    public final int DOOR_PORTAL = 6;
    public static boolean OPEN = true;
    public static boolean CLOSED = false;

    int getType();

    byte getId();

    Point getPosition();

    String getName();

    String getTarget();

    String getScriptName();

    void setScriptName(String newName);

    void setPortalStatus(boolean newStatus);

    boolean getPortalStatus();

    int getTargetMapId();

    void enterPortal(MapleClient c);

    void setPortalState(boolean state);

    boolean getPortalState();

    MaplePortal clone();

    public void save(MaplePacketLittleEndianWriter oPacket);

    public void load(LittleEndianAccessor slea);
}
