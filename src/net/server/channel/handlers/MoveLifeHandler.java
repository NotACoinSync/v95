package net.server.channel.handlers;

import client.MapleClient;
import server.life.MapleMonster;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import server.maps.objects.MapleMapObject;
import server.maps.objects.MapleMapObjectType;
import server.movement.Elem;
import server.movement.MovePath;
import tools.Pair;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CField.MobPool;

public final class MoveLifeHandler extends AbstractMovementPacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        int objectid = iPacket.readInt();
        short moveid = iPacket.readShort();// bMoveAction
        if (c.getPlayer() == null || c.getPlayer().getMap() == null) {
            return;// somehow
        }
        MapleMapObject mmo = c.getPlayer().getMap().getMapObject(objectid);
        if (mmo == null || mmo.getType() == null || mmo.getType() != MapleMapObjectType.MONSTER) {
            return;
        }
        MapleMonster monster = (MapleMonster) mmo;
        byte skillByte = iPacket.readByte();// nextAttackPossible
        byte skill = iPacket.readByte();// action
        // data(int)
        int skill_1 = iPacket.readByte() & 0xFF;
        byte skill_2 = iPacket.readByte();
        byte skill_3 = iPacket.readByte();
        byte skill_4 = iPacket.readByte();
        int size = iPacket.readInt();// ?
        for (int i = 0; i < size; i++) {
            iPacket.readInt();
            iPacket.readInt();
        }
        size = iPacket.readInt();// ?
        for (int i = 0; i < size; i++) {
            iPacket.readInt();
        }
        iPacket.readByte();
        iPacket.readInt();
        iPacket.readInt();
        iPacket.readInt();
        iPacket.readInt();
        MobSkill toUse = null;
        if (skillByte == 1 && monster.getNoSkills() > 0) {
            int random = Randomizer.nextInt(monster.getNoSkills());
            Pair<Integer, Integer> skillToUse = monster.getSkills().get(random);
            toUse = MobSkillFactory.getMobSkill(skillToUse.getLeft(), skillToUse.getRight());
            int percHpLeft = (int) (((double) monster.getHp() / monster.getMaxHp()) * 100);
            if (toUse.getHP() < percHpLeft || !monster.canUseSkill(toUse)) {
                toUse = null;
            }
        }
        if ((skill_1 >= 100 && skill_1 <= 200) && monster.hasSkill(skill_1, skill_2)) {
            MobSkill skillData = MobSkillFactory.getMobSkill(skill_1, skill_2);
            if (skillData != null && monster.canUseSkill(skillData)) {
                skillData.applyEffect(c.getPlayer(), monster, true);
            }
        }
        monster.getPosition();
        MovePath res = new MovePath();
        res.decode(iPacket);
        if (monster.getController() != null && monster.getController().getId() != c.getPlayer().getId()) {
            if (monster.isAttackedBy(c.getPlayer())) {// aggro and controller change
                monster.switchController(c.getPlayer(), true);
            } else {
                return;
            }
        } else if (skill == -1 && monster.isControllerKnowsAboutAggro() && !monster.isMobile() && !monster.isFirstAttack()) {
            monster.setControllerHasAggro(false);
            monster.setControllerKnowsAboutAggro(false);
        }
        boolean aggro = monster.isControllerHasAggro();
        if (toUse != null) {
            c.announce(MobPool.moveMonsterResponse(objectid, moveid, monster.getMp(), aggro, toUse.getSkillId(), toUse.getSkillLevel()));
        } else {
            c.announce(MobPool.moveMonsterResponse(objectid, moveid, monster.getMp(), aggro));
        }
        if (aggro) {
            monster.setControllerKnowsAboutAggro(true);
        }
        if (res != null) {
            updatePosition(res, monster, -1);
            c.getPlayer().getMap().moveMonster(monster, monster.getPosition());
            if (c.getPlayer().bMoveAction != -1) {
                for (Elem elem : res.lElem) {
                    elem.bMoveAction = c.getPlayer().bMoveAction;
                }
            }
            c.getPlayer().getMap().announce(c.getPlayer(), MobPool.moveMonster(skillByte, skill, skill_1, skill_2, skill_3, skill_4, objectid, res), monster.getPosition());
        }
    }
}
