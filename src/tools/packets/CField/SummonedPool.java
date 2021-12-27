package tools.packets.CField;

import java.util.List;

import constants.skills.BladeMaster;
import net.SendOpcode;
import net.server.channel.handlers.SummonDamageHandler.SummonAttackEntry;
import server.maps.objects.MapleSummon;
import server.movement.MovePath;
import tools.packets.PacketHelper;
import tools.data.output.MaplePacketLittleEndianWriter;

public class SummonedPool {

    /**
     * Gets a packet to spawn a special map object.
     *
     * @param summon
     * @param skillLevel The level of the skill used.
     * @param animated Animated spawn?
     * @return The spawn packet for the map object.
     */
    public static byte[] Created(MapleSummon summon, boolean animated) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter(25);
        oPacket.writeShort(SendOpcode.SPAWN_SPECIAL_MAPOBJECT.getValue());
        oPacket.writeInt(summon.getOwner().getId());
        oPacket.writeInt(summon.getObjectId());
        oPacket.writeInt(summon.getSkill());
        oPacket.write(summon.getOwner().getLevel()); // nCharLevel
        oPacket.write(summon.getSkillLevel());
        oPacket.writePos(summon.getPosition());
        oPacket.write(0);// nMoveAction
        oPacket.writeShort(0);// nCurFoothold
        oPacket.write(summon.getMovementType().getValue()); // nMoveAbility
        oPacket.write(summon.getAssistType());// nAssistType
        oPacket.write(animated ? 0 : 1);// nEnterType
        boolean bShowAvatar = summon.getSkill() == BladeMaster.MIRRORED_TARGET;
        oPacket.writeBoolean(bShowAvatar);// bShowAvatar
        if (bShowAvatar) {
            PacketHelper.encodeAvatarLook(oPacket, summon.getOwner(), false);
        }
        return oPacket.getPacket();
    }

    /**
     * Gets a packet to remove a special map object.
     *
     * @param summon
     * @param animated Animated removal?
     * @return The packet removing the object.
     */
    public static byte[] Removed(MapleSummon summon, boolean animated) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter(11);
        oPacket.writeShort(SendOpcode.REMOVE_SPECIAL_MAPOBJECT.getValue());
        oPacket.writeInt(summon.getOwner().getId());
        oPacket.writeInt(summon.getObjectId());
        oPacket.write(animated ? 4 : 1); // ?
        return oPacket.getPacket();
    }

    public static byte[] Move(int cid, int oid, MovePath moves) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MOVE_SUMMON.getValue());
        oPacket.writeInt(cid);
        oPacket.writeInt(oid);
        moves.encode(oPacket);
        return oPacket.getPacket();
    }

    public static byte[] Attack(int cid, int summonSkillId, byte direction, List<SummonAttackEntry> allDamage) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        // b2 00 29 f7 00 00 9a a3 04 00 c8 04 01 94 a3 04 00 06 ff 2b 00
        oPacket.writeShort(SendOpcode.SUMMON_ATTACK.getValue());
        oPacket.writeInt(cid);
        oPacket.writeInt(summonSkillId);
        oPacket.write(direction);
        oPacket.write(4);
        oPacket.write(allDamage.size());
        for (SummonAttackEntry attackEntry : allDamage) {
            oPacket.writeInt(attackEntry.getMonsterOid()); // oid
            oPacket.write(6); // who knows
            oPacket.writeInt(attackEntry.getDamage()); // damage
        }
        return oPacket.getPacket();
    }

    public static byte[] Hit(int cid, int summonSkillId, int damage, int unknown, int monsterIdFrom) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.DAMAGE_SUMMON.getValue());
        oPacket.writeInt(cid);
        oPacket.writeInt(summonSkillId);
        oPacket.write(unknown);
        oPacket.writeInt(damage);
        oPacket.writeInt(monsterIdFrom);
        oPacket.write(0);
        return oPacket.getPacket();
    }

    public static byte[] Skill(int cid, int summonSkillId, int nAttackAction) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SUMMON_SKILL.getValue());
        oPacket.writeInt(cid);
        oPacket.writeInt(summonSkillId);
        oPacket.write(nAttackAction);
        return oPacket.getPacket();
    }
}
