package net.server.channel.handlers;

import client.MapleCharacter;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import server.life.MapleMonster;
import tools.data.input.SeekableLittleEndianAccessor;

public final class AutoAggroHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        int oid = iPacket.readInt();
        MapleMonster monster = c.getPlayer().getMap().getMonsterByOid(oid);
        if (c.getPlayer().isHidden()) {
            return; // Don't auto aggro GM's in hide...
        }
        if (monster == null || monster.getHp() <= 0) {
            return;
        }
        if (monster.getController() != null) {
            MapleCharacter curController = c.getPlayer().getMap().getCharacterById(monster.getController().getId());
            if (!monster.isControllerHasAggro()) {
                if (curController == null) {// Controller isn't in the map, new guy.
                    monster.switchController(c.getPlayer(), true);
                } else {
                    monster.switchController(monster.getController(), true);
                }
            } else if (curController == null) {
                monster.switchController(c.getPlayer(), true);
            }
        } else {// current controller is null, set it to this person
            monster.switchController(c.getPlayer(), true);
        }
    }
}
