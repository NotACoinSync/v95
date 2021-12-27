package net.server.channel.handlers;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleStat;
import java.util.ArrayList;
import java.util.List;
import server.life.MobSkillFactory;
import server.movement.Elem;
import server.movement.MovePath;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CField.userpool.UserRemote;

public final class MovePlayerHandler extends AbstractMovementPacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (c.getPlayer() == null) {
            return;
        }
        slea.skip(29);
        // slea.readLong();
        final MovePath res = new MovePath();
        res.decode(slea);
        if (res != null) {
            if ((c.getPlayer().getMap().isStunned() || c.getPlayer().getMap().isSeduced()) && !c.getPlayer().isGM() && (c.getPlayer().isStunned() || c.getPlayer().isSeduced())) {
                if (c.getPlayer().isStunned()) {
                    MobSkillFactory.getMobSkill(123, 3).getEffect().applyTo(c.getPlayer());
                } else if (c.getPlayer().isSeduced()) {
                    MobSkillFactory.getMobSkill(128, 1).getEffect().applyTo(c.getPlayer());
                }// return;
            }
            updatePosition(res, c.getPlayer(), 0);
            c.getPlayer().getMap().movePlayer(c.getPlayer(), c.getPlayer().getPosition());
            if (c.getPlayer().bMoveAction != -1) {
                for (Elem elem : res.lElem) {
                    elem.bMoveAction = c.getPlayer().bMoveAction;
                }
            }
            if (c.getPlayer().isHidden()) {
                c.getPlayer().getMap().announceGM(c.getPlayer(), UserRemote.Move(c.getPlayer().getId(), res), false);
            } else {
                c.getPlayer().getMap().announce(c.getPlayer(), UserRemote.Move(c.getPlayer().getId(), res), false);
            }
            // Let's play Tag!
            if (c.getPlayer().getMap().getMapTag() && c.getPlayer().getTag() && c.getPlayer().isGM()) {
                List<MapleCharacter> chars = new ArrayList<>(c.getPlayer().getMap().getCharacters());
                for (MapleCharacter chr : chars) {
                    if (Math.abs(chr.getPosition().getX() - c.getPlayer().getPosition().getX()) < 75 && Math.abs(chr.getPosition().getY() - c.getPlayer().getPosition().getY()) < 75) {
                        if (chr.isGM()) { // 'cuz lol GMs
                            chr.setHp(chr.getMaxHp());
                            chr.updateSingleStat(MapleStat.HP, chr.getMaxHp());
                            chr.setMp(chr.getMaxMp());
                            chr.updateSingleStat(MapleStat.MP, chr.getMaxMp());
                        } else {
                            chr.setHpMp(0);
                        }
                    }
                }
            }
        }
    }
}
