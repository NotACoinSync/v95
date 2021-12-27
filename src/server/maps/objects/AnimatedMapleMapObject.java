package server.maps.objects;

public interface AnimatedMapleMapObject extends MapleMapObject {

    int getStance();

    void setStance(int stance);

    boolean isFacingLeft();
}
