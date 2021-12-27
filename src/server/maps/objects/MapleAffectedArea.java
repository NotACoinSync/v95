package server.maps.objects;

import client.MapleCharacter;
import client.MapleClient;
import client.Skill;
import client.SkillFactory;
import constants.skills.*;
import constants.skills.resistance.*;
import java.awt.Point;
import java.awt.Rectangle;
import server.MapleStatEffect;
import server.life.MapleMonster;
import server.life.MobSkill;
import tools.packets.CField.AffectedAreaPool;

public class MapleAffectedArea extends AbstractMapleMapObject {

    private Rectangle affectedAreaPosition;
    public int ownerid = -1, skilllevel;
    private MapleMonster monster = null;
    private MapleStatEffect source;
    private MobSkill skill;
    private boolean isMobAffectedArea, isPoisonAffectedArea, isRecoveryAffectedArea, isSmokeAffectedArea, isItemAffectedArea;
    private int skillDelay;
    public int duration, info, phase;
    public long createTime = System.currentTimeMillis();

    // Automatically sets isPoisonMist to true since all mobs give poison mist.
    public MapleAffectedArea(Rectangle affectedAreaPosition, MapleMonster mob, MobSkill skill, int duration) {
        this.affectedAreaPosition = affectedAreaPosition;
        this.monster = mob;
        this.skill = skill;
        this.isMobAffectedArea = true;
        this.isRecoveryAffectedArea = false;
        this.isPoisonAffectedArea = true;
        this.isSmokeAffectedArea = false;
        this.isItemAffectedArea = false;
        this.skillDelay = 0;
        this.duration = duration;
        this.info = 0;
        this.phase = 0;
    }

    public MapleAffectedArea(Rectangle affectedAreaPosition, MapleCharacter owner, MapleStatEffect source) {
        this.affectedAreaPosition = affectedAreaPosition;
        this.ownerid = owner.getId();
        this.skilllevel = owner.getSkillLevel(SkillFactory.getSkill(source.getSourceId()));
        this.source = source;
        this.skillDelay = 8;
        this.isMobAffectedArea = false;
        this.isItemAffectedArea = false;
        this.duration = source.getDuration();
        this.info = 0;
        this.phase = 0;
        switch (source.getSourceId()) {
            case Evan.RecoveryAura:
                isRecoveryAffectedArea = true;
                break;
            case Shadower.SmokeScreen:
            case BattleMage.PartyShield:
                isSmokeAffectedArea = true;
                break;
            case FPMage.POISON_MIST:
            case BlazeWizard.FLAME_GEAR:
            case NightWalker.POISON_BOMB:
                isPoisonAffectedArea = true;
                break;
        }
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.AFFECTED_AREA;
    }

    @Override
    public Point getPosition() {
        return affectedAreaPosition.getLocation();
    }

    public Skill getSourceSkill() {
        return SkillFactory.getSkill(source.getSourceId());
    }

    public int getAffectedAreaType() {
        if (isMobAffectedArea()) {
            return 0;
        } else if (isPoisonAffectedArea()) {
            return 1;
        } else if (isSmokeAffectedArea()) {
            return 2;
        } else if (isItemAffectedArea()) {
            return 3;
        } else if (isRecoveryAffectedArea()) {
            return 4;
        } else {
            return 5;
        }
    }

    public boolean isMobAffectedArea() {
        return isMobAffectedArea;
    }

    public boolean isRecoveryAffectedArea() {
        return isRecoveryAffectedArea;
    }

    public boolean isPoisonAffectedArea() {
        return isPoisonAffectedArea;
    }

    public boolean isSmokeAffectedArea() {
        return isSmokeAffectedArea;
    }

    public boolean isItemAffectedArea() {
        return isItemAffectedArea;
    }

    public int getSkillDelay() {
        return skillDelay;
    }

    public int getInfo() {
        return info;
    }

    public int getPhase() {
        return phase;
    }

    public MapleMonster getMobOwner() {
        return monster;
    }

    public Rectangle getBox() {
        return affectedAreaPosition;
    }

    @Override
    public void setPosition(Point position) {
        throw new UnsupportedOperationException();
    }

    public final byte[] makeDestroyData() {
        return AffectedAreaPool.Removed(getObjectId());
    }

    public final byte[] makeSpawnData() {
        if (ownerid != -1) {
            return AffectedAreaPool.Created(getObjectId(), ownerid, getSourceSkill().getId(), skilllevel, this);
        }
        return AffectedAreaPool.Created(getObjectId(), monster.getId(), skill.getSkillId(), skill.getSkillLevel(), this);
    }

    public final byte[] makeFakeSpawnData(int level) {
        if (ownerid != -1) {
            return AffectedAreaPool.Created(getObjectId(), ownerid, getSourceSkill().getId(), level, this);
        }
        return AffectedAreaPool.Created(getObjectId(), monster.getId(), skill.getSkillId(), skill.getSkillLevel(), this);
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        client.announce(makeSpawnData());
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.announce(makeDestroyData());
    }

    public boolean makeChanceResult() {
        return source.makeChanceResult();
    }

    @Override
    public MapleAffectedArea clone() {
        return null;
    }
}
