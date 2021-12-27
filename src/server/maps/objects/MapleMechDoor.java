package server.maps.objects;

import client.MapleCharacter;
import client.MapleClient;
import java.awt.Point;

/**

 @author Phu
 */
public class MapleMechDoor extends AbstractAnimatedMapleMapObject {

    private int owner, partyid, id;

    public MapleMechDoor(MapleCharacter owner, Point pos, int id) {
        super();
        this.owner = owner.getId();
        this.partyid = owner.getParty() == null ? 0 : owner.getParty().getId();
        setPosition(pos);
        this.id = id;
    }

    @Override
    public AbstractAnimatedMapleMapObject clone() {
        return null;
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        //client.getSession().write(CField.spawnMechDoor(this, false));
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        //client.getSession().write(CField.removeMechDoor(this, false));
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.DOOR;
    }

    public int getOwnerId() {
        return this.owner;
    }

    public int getPartyId() {
        return this.partyid;
    }

    public int getId() {
        return this.id;
    }

}
