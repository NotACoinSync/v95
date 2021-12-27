package server.maps.objects;

import java.awt.Point;

import client.MapleClient;

public interface MapleMapObject {

    public int getObjectId();

    public void setObjectId(int id);

    public MapleMapObjectType getType();

    public Point getPosition();

    public void setPosition(Point position);

    public void sendSpawnData(MapleClient client);

    public void sendDestroyData(MapleClient client);

    public void nullifyPosition();

    public void setVisibleTo(int chrid);

    public int getVisibleTo();

    public MapleMapObject clone();
}
