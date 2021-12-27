package tools.packets.CField;

import java.util.Map;
import java.util.Map.Entry;
import net.SendOpcode;
import server.life.MapleMonster;
import server.life.MobStat;
import server.life.MobStatData;
import server.movement.MovePath;
import tools.Randomizer;
import tools.data.output.LittleEndianWriter;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.packets.PacketHelper;

public class MobPool {

    /**
     Gets a spawn monster packet.

     @param life     The monster to spawn.
     @param newSpawn Is it a new spawn?

     @return The spawn monster packet.
     */
    public static byte[] spawnMonster(MapleMonster life, boolean newSpawn) {
        return spawnMonsterInternal(life, false, newSpawn, false, 0, false);
    }

    /**
     Gets a spawn monster packet.

     @param life     The monster to spawn.
     @param newSpawn Is it a new spawn?

     @return The spawn monster packet.
     */
    public static byte[] spawnHPQMonster(MapleMonster life, boolean newSpawn) {
        return spawnMonsterInternal(life, false, newSpawn, false, 0, false);
    }

    /**
     Gets a spawn monster packet.

     @param life     The monster to spawn.
     @param newSpawn Is it a new spawn?
     @param effect   The spawn effect.

     @return The spawn monster packet.
     */
    public static byte[] spawnMonster(MapleMonster life, boolean newSpawn, int effect) {
        return spawnMonsterInternal(life, false, newSpawn, false, effect, false);
    }

    /**
     Gets a control monster packet.

     @param life     The monster to give control to.
     @param newSpawn Is it a new spawn?
     @param aggro    Aggressive monster?

     @return The monster control packet.
     */
    public static byte[] controlMonster(MapleMonster life, boolean newSpawn, boolean aggro) {
        return spawnMonsterInternal(life, true, newSpawn, aggro, 0, false);
    }

    /**
     Removes a monster invisibility.

     @param life

     @return
     */
    public static byte[] removeMonsterInvisibility(MapleMonster life) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SPAWN_MONSTER_CONTROL.getValue());
        oPacket.write(1);
        oPacket.writeInt(life.getObjectId());
        return oPacket.getPacket();
        // return spawnMonsterInternal(life, true, false, false, 0, false);
    }

    /**
     Makes a monster invisible for Ariant PQ.

     @param life

     @return
     */
    public static byte[] makeMonsterInvisible(MapleMonster life) {
        return spawnMonsterInternal(life, true, false, false, 0, true);
    }

    /**
     Internal function to handler monster spawning and controlling.

     @param life              The mob to perform operations with.
     @param requestController Requesting control of mob?
     @param newSpawn          New spawn (fade in?)
     @param aggro             Aggressive mob?
     @param effect            The spawn effect to use.

     @return The spawn/control packet.
     */
    private static byte[] spawnMonsterInternal(MapleMonster life, boolean requestController, boolean newSpawn, boolean aggro, int effect, boolean makeInvis) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        if (makeInvis) {
            oPacket.writeShort(SendOpcode.SPAWN_MONSTER_CONTROL.getValue());
            oPacket.write(0);
            oPacket.writeInt(life.getObjectId());
            return oPacket.getPacket();
        }
        if (requestController) {
            oPacket.writeShort(SendOpcode.SPAWN_MONSTER_CONTROL.getValue());
            oPacket.write(aggro ? 2 : 1);
        } else {
            oPacket.writeShort(SendOpcode.SPAWN_MONSTER.getValue());
        }
        oPacket.writeInt(life.getObjectId());
        oPacket.write(life.getController() == null ? 5 : 1);
        oPacket.writeInt(life.getId());
        encodeTemporary(oPacket, life);
        oPacket.writePos(life.getPosition());
        oPacket.write(life.getStance());
        oPacket.writeShort(0); // Origin FH //life.getStartFh()
        oPacket.writeShort(life.getFh());
        /**
         -4: Fake -3: Appear after linked mob is dead -2: Fade in 1: Smoke 3:
         King Slime spawn 4: Summoning rock thing, used for 3rd job? 6:
         Magical shit 7: Smoke shit 8: 'The Boss' 9/10: Grim phantom shit?
         11/12: Nothing? 13: Frankenstein 14: Angry ^ 15: Orb animation thing,
         ?? 16: ?? 19: Mushroom kingdom boss thing
         */
        if (effect != 0) {
            oPacket.write(effect);// nAppearType
            if (effect == -3 || effect >= 0) {
                oPacket.writeInt(effect == -3 ? life.getParentMob() : 0);
            }
        } else {
            oPacket.write(newSpawn ? -2 : -1);
        }
        oPacket.write(life.getTeam());// m_nTeamForMCarnival
        oPacket.writeInt(life.getItemEffect());// nEffectItemID
        oPacket.writeInt(0);// m_nPhase
        return oPacket.getPacket();
    }

    private static void encodeTemporary(LittleEndianWriter lew, MapleMonster monster) {// CMob::SetTemporaryStat
        encodeTemporary(lew, monster.getMobStats());
    }

    private static void encodeTemporary(LittleEndianWriter lew, Map<MobStat, MobStatData> stats) {// CMob::SetTemporaryStat
        int[] mask = new int[PacketHelper.MaxBuffStat];
        for (Entry<MobStat, MobStatData> entry : stats.entrySet()) {
            mask[entry.getKey().getSet()] |= entry.getKey().getMask();
        }
        for (int i = 3; i >= 0; i--) {
            lew.writeInt(mask[i]);
        }
        for (Entry<MobStat, MobStatData> entry : stats.entrySet()) {
            MobStat stat = entry.getKey();
            MobStatData data = entry.getValue();
            switch (stat) {
                case Burned: {
                    lew.writeInt(0);// size
                    break;
                }
                case Disable: {
                    lew.writeBoolean(false); // Invincible
                    lew.writeBoolean(false); // disable
                    break;
                }
                default: {
                    lew.writeShort(data.nOption);
                    if (data.mobSkill) {
                        lew.writeShort(data.rOption);
                        lew.writeShort(data.skillLevel);
                    } else {
                        lew.writeInt(data.rOption);
                    }
                    lew.writeShort((int) ((data.endTime - System.currentTimeMillis()) / 500));
                    if (stat.equals(MobStat.PCounter)) {
                        lew.writeInt(entry.getValue().pCounter); // wPCounter_
                        lew.writeInt(100);
                    } else if (stat.equals(MobStat.MCounter)) {
                        lew.writeInt(entry.getValue().pCounter); // wMCounter_
                        lew.writeInt(100);
                    }
                    break;
                }
            }
        }
    }

    /**
     Handles monsters not being targettable, such as Zakum's first body.

     @param life   The mob to spawn as non-targettable.
     @param effect The effect to show when spawning.

     @return The packet to spawn the mob as non-targettable.
     */
    public static byte[] spawnFakeMonster(MapleMonster life, int effect) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SPAWN_MONSTER_CONTROL.getValue());
        oPacket.write(1);
        oPacket.writeInt(life.getObjectId());
        oPacket.write(5);// nCalcDamageIndex
        oPacket.writeInt(life.getId());
        addTemporaryStat(oPacket);
        oPacket.writePos(life.getPosition());
        oPacket.write(life.getStance());
        oPacket.writeShort(0);// life.getStartFh()
        oPacket.writeShort(life.getFh());
        if (effect > 0) {
            oPacket.write(effect);
            oPacket.write(0);
            oPacket.writeShort(0);
        }
        oPacket.writeShort(-2);
        oPacket.write(life.getTeam());
        oPacket.writeInt(life.getItemEffect());
        oPacket.writeInt(0);// m_nPhase
        return oPacket.getPacket();
    }

    /**
     Makes a monster previously spawned as non-targettable, targettable.

     @param life The mob to make targettable.

     @return The packet to make the mob targettable.
     */
    public static byte[] makeMonsterReal(MapleMonster life) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SPAWN_MONSTER.getValue());
        oPacket.writeInt(life.getObjectId());// dwMobId
        oPacket.write(5);// nCalcDamageIndex
        oPacket.writeInt(life.getId());// mob template
        addTemporaryStat(oPacket);
        oPacket.writePos(life.getPosition());
        oPacket.write(life.getStance());
        oPacket.writeShort(0);// life.getStartFh()
        oPacket.writeShort(life.getFh());
        oPacket.writeShort(-1);
        oPacket.writeInt(life.getItemEffect());
        oPacket.writeInt(0);// m_nPhase
        return oPacket.getPacket();
    }

    /**
     Gets a stop control monster packet.

     @param oid The ObjectID of the monster to stop controlling.

     @return The stop control monster packet.
     */
    public static byte[] stopControllingMonster(int objectID) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SPAWN_MONSTER_CONTROL.getValue());
        oPacket.write(0);
        oPacket.writeInt(objectID);
        return oPacket.getPacket();
    }

    public static byte[] killMonster(int oid, boolean animation) {
        return killMonster(oid, animation ? 1 : 0);
    }

    /**
     Gets a packet telling the client that a monster was killed.

     @param oid       The objectID of the killed monster.
     @param animation 0 = dissapear, 1 = fade out, 2+ = special

     @return The kill monster packet.
     */
    public static byte[] killMonster(int objectID, int animation) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.KILL_MONSTER.getValue());
        oPacket.writeInt(objectID);
        oPacket.write(animation);
        if (animation == 4) {
            oPacket.writeInt(0);
        }
        return oPacket.getPacket();
    }

    private static void addTemporaryStat(LittleEndianWriter lew) {
        int[] mask = new int[PacketHelper.MaxBuffStat];
        // for(MapleBuffStat statup : statups){
        // mask[statup.getSet()] |= statup.getMask();
        // }
        for (int i = 3; i >= 0; i--) {
            lew.writeInt(mask[i]);
        }
    }

    public static byte[] moveMonster(int useskill, int skill, int skill_1, int skill_2, int skill_3, int skill_4, int objectID, MovePath moves) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MOVE_MONSTER.getValue());
        oPacket.writeInt(objectID);
        oPacket.write(0);// bNotForceLandingWhenDiscard
        oPacket.write(useskill);// bNotChangeAction
        oPacket.write(skill);// bNextAttackPossible
        oPacket.write(skill_1);// bLeft
        oPacket.write(skill_2);
        oPacket.write(skill_3);
        oPacket.write(skill_4);
        oPacket.skip(8);
        // oPacket.write(notForceLandingWhenDiscard);// bNotForceLandingWhenDiscard
        // oPacket.write(notChangeAction);// bNotChangeAction
        // oPacket.writeBoolean(nextAttackPossible);// bNextAttackPossible
        // oPacket.writeBoolean(left);// bLeft
        // oPacket.writeInt(data);// data?
        // oPacket.writeInt(0);// m_aMultiTargetForBall
        // oPacket.writeInt(0);// m_aRandTimeforAreaAttack
        moves.encode(oPacket);
        return oPacket.getPacket();
    }

    /**
     Gets a response to a move monster packet.

     @param objectid  The ObjectID of the monster being moved.
     @param moveid    The movement ID.
     @param currentMp The current MP of the monster.
     @param useSkills Can the monster use skills?

     @return The move response packet.
     */
    public static byte[] moveMonsterResponse(int objectid, short moveid, int currentMp, boolean useSkills) {
        return moveMonsterResponse(objectid, moveid, currentMp, useSkills, 0, 0);
    }

    /**
     Gets a response to a move monster packet.

     @param objectid   The ObjectID of the monster being moved.
     @param moveid     The movement ID.
     @param currentMp  The current MP of the monster.
     @param useSkills  Can the monster use skills?
     @param skillId    The skill ID for the monster to use.
     @param skillLevel The level of the skill to use.

     @return The move response packet.
     */
    public static byte[] moveMonsterResponse(int objectID, short moveid, int currentMp, boolean useSkills, int skillId, int skillLevel) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MOVE_MONSTER_RESPONSE.getValue());
        oPacket.writeInt(objectID);
        oPacket.writeShort(moveid);//
        oPacket.writeBoolean(useSkills);// bNextAttackPossible
        oPacket.writeShort(currentMp);// mp
        oPacket.write(skillId);// m_nSkillCommand
        oPacket.write(skillLevel);// m_nSLV
        return oPacket.getPacket();
    }

    public static byte[] StatSet(MapleMonster monster, Map<MobStat, MobStatData> stats) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.STAT_SET_MONSTER.getValue());
        oPacket.writeInt(monster.getObjectId());
        encodeTemporary(oPacket, stats);
        oPacket.writeShort(100);// delay
        oPacket.write(1);// m_nCalcDamageStatIndex
        boolean movementAffectingStat = false;
        for (MobStat stat : stats.keySet()) {
            if (stat.isMovementAffectingStat()) {
                movementAffectingStat = true;
            }
        }
        if (movementAffectingStat) {
            oPacket.write(1);// bStat or m_bDoomReservedSN
        }
        return oPacket.getPacket();
    }

    public static byte[] StatReset(MapleMonster monster, Map<MobStat, MobStatData> stats) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.STAT_RESET_MONSTER.getValue());
        oPacket.writeInt(monster.getObjectId());
        processStatReset(oPacket, stats);
        return oPacket.getPacket();
    }

    private static void processStatReset(LittleEndianWriter lew, Map<MobStat, MobStatData> stats) {// CMob::OnStatReset
        int[] mask = new int[PacketHelper.MaxBuffStat];
        boolean movementAffectingStat = false;
        for (Entry<MobStat, MobStatData> entry : stats.entrySet()) {
            mask[entry.getKey().getSet()] |= entry.getKey().getMask();
            if (entry.getKey().isMovementAffectingStat()) {
                movementAffectingStat = true;
            }
        }
        for (int i = 3; i >= 0; i--) {
            lew.writeInt(mask[i]);
        }
        // CMob::ProcessStatReset
        // MobStat::Reset
        for (Entry<MobStat, MobStatData> entry : stats.entrySet()) {
            if (entry.getKey().equals(MobStat.Burned)) {
                lew.writeInt(0);// size
                // for each size
                // character id
                // skill id
            }
        }
        // end of MobStat::Reset
        lew.write(0);// m_nCalcDamageStatIndex
        // if mob is alive
        if (movementAffectingStat) {
            lew.writeBoolean(false);// bStat
        }
    }

    public static byte[] MobCrcKeyChanged() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MOB_CRC_KEY_CHANGED.getValue());
        oPacket.writeInt(Randomizer.nextInt());
        return oPacket.getPacket();
    }

    public static byte[] MobDamageMobFriendly(MapleMonster mob, int damage) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.DAMAGED_MONSTER.getValue());
        oPacket.writeInt(mob.getObjectId());
        oPacket.write(1); // direction ?
        oPacket.writeInt(damage);
        int remainingHp = mob.getHp() - damage;
        if (remainingHp <= 1) {
            remainingHp = 0;
            mob.getMap().removeMapObject(mob);
        }
        mob.setHp(remainingHp);
        oPacket.writeInt(remainingHp);
        oPacket.writeInt(mob.getMaxHp());
        return oPacket.getPacket();
    }

    public static byte[] damageMonster(int objectID, int damage) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.DAMAGED_MONSTER.getValue());
        oPacket.writeInt(objectID);
        oPacket.write(0);
        oPacket.writeInt(damage);
        oPacket.writeInt(0);
        oPacket.writeInt(0);
        return oPacket.getPacket();
    }

    public static byte[] healMonster(int oid, int heal) {
        return damageMonster(oid, -heal);
    }

    public static byte[] SpecialEffectBySkill(int objectID, int skillID, int characterid, int delay) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MONSTER_SPECIAL_EFFECT_BY_SKILL.getValue());
        oPacket.writeInt(objectID);
        oPacket.writeInt(skillID);
        oPacket.writeInt(characterid);
        oPacket.writeShort(delay);// tDelay
        return oPacket.getPacket();
    }

    /**
     @param oid
     @param remhppercentage

     @return
     */
    public static byte[] HPIndicator(int objectID, int nHPpercentage) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SHOW_MONSTER_HP.getValue());
        oPacket.writeInt(objectID);
        oPacket.write(nHPpercentage);
        return oPacket.getPacket();
    }

    public static byte[] CatchEffect(int objectID, int nItemID, byte bSuccess) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CATCH_MONSTER.getValue());
        oPacket.writeInt(objectID);
        oPacket.writeInt(nItemID);
        oPacket.write(bSuccess);
        return oPacket.getPacket();
    }

    public static byte[] EffectByItem(int objectID, int nItemID, byte bSuccess) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.EFFECT_BY_ITEM.getValue());
        oPacket.writeInt(objectID);
        oPacket.writeInt(nItemID);
        oPacket.write(bSuccess);
        return oPacket.getPacket();
    }

    public static byte[] MobSpeaking(int objectID, int nSpeakInfo, int nSpeech) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SPEAKING_MONSTER.getValue());
        oPacket.writeInt(objectID);
        oPacket.writeInt(nSpeakInfo);
        oPacket.writeInt(nSpeech);
        return oPacket.getPacket();
    }

    public static byte[] IncMobChargeCount(int objectID, int nMobChargeCount, int bAttackReady) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.INC_CHARGE_COUNT_MONSTER.getValue());
        oPacket.writeInt(objectID);
        oPacket.writeInt(nMobChargeCount);
        oPacket.writeInt(bAttackReady);
        return oPacket.getPacket();
    }

    public static byte[] MobSkillDelay(int objectID, int tSkillDelayTime, int nSkillID, int nSLV, int nOption) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SKILL_DELAY_MONSTER.getValue());
        oPacket.writeInt(objectID);
        oPacket.writeInt(tSkillDelayTime);
        oPacket.writeInt(nSkillID);
        oPacket.writeInt(nSLV);
        oPacket.writeInt(nOption);
        return oPacket.getPacket();
    }

    public static byte[] EscortFullPath(int objectID) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.ESCORT_FULL_PATH_MONSTER.getValue());
        oPacket.writeInt(objectID);
        oPacket.writeInt(0);
        oPacket.writeInt(0);
        oPacket.writeInt(0);
        return oPacket.getPacket();
    }

    public static byte[] EscortStopSay(int objectID, int nFadeDelay, int nChatBalloon, int bWeather, boolean say, String sMsg, int nEscortStopAct) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.ESCORT_STOP_SAY_MONSTER.getValue());
        oPacket.writeInt(objectID);
        oPacket.writeInt(nFadeDelay / 4);
        oPacket.writeInt(nChatBalloon);
        oPacket.write(bWeather);
        oPacket.writeBoolean(say);
        if (say) {
            oPacket.writeMapleAsciiString(sMsg);
            oPacket.writeInt(nEscortStopAct); // nEscortStopAct
        }
        return oPacket.getPacket();
    }

    public static byte[] EscortReturnBefore(int objectID, int v3) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.ESCORT_RETURN_BEFORE_MONSTER.getValue());
        oPacket.writeInt(objectID);
        oPacket.writeInt(v3);
        return oPacket.getPacket();
    }

    public static byte[] NextAttack(int objectID) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.ESCORT_RETURN_BEFORE_MONSTER.getValue());
        oPacket.writeInt(objectID);
        oPacket.writeInt(0);
        return oPacket.getPacket();
    }

    public static byte[] MobAttackedByMob(int objectID, int nAttackIdx, int nDamage, int dwMobTemplateID, int bLeft) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.ESCORT_RETURN_BEFORE_MONSTER.getValue());
        oPacket.writeInt(objectID);
        oPacket.write(nAttackIdx);
        oPacket.writeInt(nDamage);
        if (nAttackIdx > -2) {
            oPacket.writeInt(dwMobTemplateID);
            oPacket.write(bLeft);
        }
        return oPacket.getPacket();
    }
}
