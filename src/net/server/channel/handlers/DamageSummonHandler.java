package net.server.channel.handlers;

import client.player.SecondaryStat;
import client.MapleCharacter;
import client.MapleClient;
import client.autoban.AutobanFactory;
import net.AbstractMaplePacketHandler;
import server.maps.objects.MapleMapObject;
import server.maps.objects.MapleSummon;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CField.SummonedPool;

public final class DamageSummonHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int objectid = slea.readInt();
        int unkByte = slea.readByte();
        int damage = slea.readInt();
        int monsterIdFrom = slea.readInt();
        slea.readBoolean();
        MapleCharacter player = c.getPlayer();
        MapleMapObject mmo = player.getMap().getMapObject(objectid);
        if (mmo != null && mmo instanceof MapleSummon) {
            MapleSummon summon = (MapleSummon) mmo;
            if (summon.getOwner().getId() == player.getId()) {
                if (summon != null) {
                    summon.addHP(-damage);
                    if (summon.getHP() <= 0) {
                        player.cancelEffectFromSecondaryStat(SecondaryStat.PUPPET);
                    }
                    player.getMap().announce(player, SummonedPool.Hit(player.getId(), summon.getSkill(), damage, unkByte, monsterIdFrom), summon.getPosition());
                }
            } else {
                AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to damage a summon that isn't his.");
            }
        } else {
            AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to damage a summon that doesn't exist.");
        }
    }
}
