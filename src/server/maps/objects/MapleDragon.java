package server.maps.objects;

import client.MapleCharacter;
import client.MapleClient;
import tools.packets.DragonPacket;

public class MapleDragon extends AbstractAnimatedMapleMapObject {

    public int ownerid;
    public int jobid;

    public MapleDragon(MapleCharacter chr) {
        super();
        this.ownerid = chr.getId();
        this.jobid = chr.getJob().getId();
        this.setPosition(chr.getPosition());
        this.setStance(chr.getStance());
        sendSpawnData(chr.getClient());
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.DRAGON;
    }

    @Override
    public void sendSpawnData(MapleClient c) {
        c.announce(DragonPacket.Created(this));
    }

    @Override
    public void sendDestroyData(MapleClient c) {
        c.announce(DragonPacket.Destroy(ownerid));
    }

    @Override
    public MapleDragon clone() {
        return null;
    }
}
