package server.life;

import client.MapleClient;
import server.maps.objects.MapleMapObjectType;
import server.shops.MapleShopFactory;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.packets.CField.NpcPool;

public class MapleNPC extends AbstractLoadedMapleLife {

    public MapleNPCStats stats;

    public MapleNPC(int id, MapleNPCStats stats) {
        super(id);
        this.stats = stats;
    }

    public MapleNPC(int id) {
        super(id);
    }

    public MapleNPC(MapleNPC npc) {
        super(npc);
    }

    public MapleNPC() {
        super();
    }

    public boolean hasShop() {
        return MapleShopFactory.getInstance().getShopForNPC(getId()) != null;
    }

    public void sendShop(MapleClient c) {
        MapleShopFactory.getInstance().getShopForNPC(getId()).sendShop(c);
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        if (this.getId() > 9010010 && this.getId() < 9010014) {
            client.announce(NpcPool.spawnNPCRequestController(this, false));
        } else {
            client.announce(NpcPool.NpcEnterField(this));
            client.announce(NpcPool.spawnNPCRequestController(this, true));
        }
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        // setId(9390409);
        // client.announce(MaplePacketCreator.spawnNPC(this, false));
        // client.announce(MaplePacketCreator.removeNPC(getId()));
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.NPC;
    }

    public String getName() {
        return stats.getName();
    }

    @Override
    public MapleNPC clone() {
        MapleNPC clone = new MapleNPC(this);
        clone.setStance(getStance());
        clone.setPosition(getPosition());
        clone.stats = stats;
        return clone;
    }

    @Override
    public void save(MaplePacketLittleEndianWriter oPacket) {
        super.save(oPacket);
        stats.save(oPacket);
        oPacket.writeInt(getCy());
        oPacket.writeInt(getRx0());
        oPacket.writeInt(getRx1());
        oPacket.writeBoolean(isHidden());
    }

    @Override
    public void load(LittleEndianAccessor slea) {
        super.load(slea);
        stats = new MapleNPCStats();
        stats.load(slea);
        setCy(slea.readInt());
        setRx0(slea.readInt());
        setRx1(slea.readInt());
        setHide(slea.readBoolean());
    }
}
