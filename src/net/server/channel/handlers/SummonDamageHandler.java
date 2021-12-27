package net.server.channel.handlers;

import java.util.ArrayList;
import java.util.List;

import client.MapleCharacter;
import client.MapleClient;
import client.Skill;
import client.SkillFactory;
import net.AbstractMaplePacketHandler;
import server.MapleStatEffect;
import server.life.MapleMonster;
import server.maps.objects.MapleSummon;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CField.SummonedPool;

public final class SummonDamageHandler extends AbstractMaplePacketHandler {

    public final class SummonAttackEntry {

        private int monsterOid;
        private int damage;

        public SummonAttackEntry(int monsterOid, int damage) {
            this.monsterOid = monsterOid;
            this.damage = damage;
        }

        public int getMonsterOid() {
            return monsterOid;
        }

        public int getDamage() {
            return damage;
        }
    }

    @Override
    public void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        int summonedID = iPacket.readInt();
        MapleCharacter player = c.getPlayer();
        if (!player.isAlive()) {
            return;
        }
        MapleSummon summon = null;
        for (MapleSummon sum : player.getSummons().values()) {
            if (sum.getObjectId() == summonedID) {
                summon = sum;
            }
        }
        if (summon == null) {
            return;
        }
        Skill summonSkill = SkillFactory.getSkill(summon.getSkill());
        MapleStatEffect summonEffect = summonSkill.getEffect(summon.getSkillLevel());
        iPacket.skip(8);
        iPacket.readInt(); // get_update_time()
        iPacket.skip(8);
        List<SummonAttackEntry> allDamage = new ArrayList<>();
        byte direction = iPacket.readByte();
        iPacket.skip(8);
        int mobCount = iPacket.readByte();
        iPacket.readShort(); // x1
        iPacket.readShort(); // y1
        iPacket.readShort(); // x2
        iPacket.readShort(); // y2
        for (int x = 0; x < mobCount; x++) {
            int monsterOid = iPacket.readInt(); // attacked oid
            iPacket.skip(8);
            iPacket.readByte(); // nHitAction
            iPacket.readByte(); // nForeAction
            iPacket.readByte(); // nFrameIdx
            iPacket.readByte(); // GetCalcDamageStatIndex
            iPacket.readShort(); // x3
            iPacket.readShort(); // y3
            iPacket.readShort(); // x4
            iPacket.readShort(); // y4
            iPacket.readShort(); // tDelay
            int damage = iPacket.readInt();
            allDamage.add(new SummonAttackEntry(monsterOid, damage));
        }
        iPacket.readInt(); // dwSkillCRC
        player.getMap().announce(player, SummonedPool.Attack(player.getId(), summon.getSkill(), direction, allDamage), summon.getPosition());
        for (SummonAttackEntry attackEntry : allDamage) {
            int damage = attackEntry.getDamage();
            MapleMonster target = player.getMap().getMonsterByOid(attackEntry.getMonsterOid());
            if (target != null) {
                if (damage > 0 && summonEffect.getMonsterStati().size() > 0) {
                    if (summonEffect.makeChanceResult()) {
                        target.applyStatus(player, summonEffect.getMonsterStati(), summonSkill, summonEffect.isPoison(), false);
                    }
                }
                player.getMap().damageMonster(player, target, damage);
            }
        }
    }
}
