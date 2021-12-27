package net.server.channel.handlers;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import server.life.MapleMonster;
import server.maps.MapleMap;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CField.MobPool;
import tools.packets.WvsContext;

public final class MobDamageMobFriendlyHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int attacker = slea.readInt();
        slea.readInt();
        int damaged = slea.readInt();
        MapleMonster monster = c.getPlayer().getMap().getMonsterByOid(damaged);
        if (monster == null || c.getPlayer().getMap().getMonsterByOid(attacker) == null) {
            return;
        }
        int damage = Randomizer.nextInt(((monster.getMaxHp() / 13 + monster.getPADamage() * 10)) * 2 + 500) / 10; // Beng's formula.
        // int damage = monster.getStats().getPADamage() + monster.getStats().getPDDamage() - 1;
        if (monster.getId() == 9300061) {
            if (monster.getHp() - damage < 1) {
                monster.getMap().announce(WvsContext.BroadcastMsg.encode(6, "The Moon Bunny went home because he was sick."));
                c.getPlayer().getEventInstance().getMapInstance(monster.getMap().getId()).killFriendlies(monster);
            }
            MapleMap map = c.getPlayer().getEventInstance().getMapInstance(monster.getMap().getId());
            map.addBunnyHit();
        } else if (monster.getId() >= 9400322 && monster.getId() <= 9400336) { // Snowman
            if (monster.getHp() - damage < 1) {
                monster.getMap().announce(WvsContext.BroadcastMsg.encode(6, "The snowman has melted."));
                c.getPlayer().getEventInstance().getMapInstance(monster.getMap().getId()).killFriendlies(monster);
            }
            MapleMap map = c.getPlayer().getEventInstance().getMapInstance(monster.getMap().getId());
            map.addSnowmanHit();
        } else if (monster.getId() == 9300102) {// Watchhog
            if (monster.getHp() - damage < 1) {
                c.getPlayer().getEventInstance().getMapInstance(monster.getMap().getId()).killFriendlies(monster);
            }
        } else if (monster.getId() == 9300389) {// Safe Guard
            if (monster.getHp() - damage < 1) {
                c.getPlayer().getEventInstance().getMapInstance(monster.getMap().getId()).killFriendlies(monster);
            }
        } else {
            Logger.log(LogType.INFO, LogFile.GENERAL_ERROR, "No handling for MobDamageMobFriendlyHandler for mob: " + monster.getId());
        }
        c.getPlayer().getMap().announce(MobPool.MobDamageMobFriendly(monster, damage), monster.getPosition());
        c.announce(WvsContext.enableActions());
    }
}
