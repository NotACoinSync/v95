package server.maps.objects;

import java.awt.Point;

import client.MapleCharacter;
import client.MapleClient;
import client.SkillFactory;
import constants.skills.*;
import server.maps.SummonMovementType;
import tools.packets.CField.SummonedPool;

public class MapleSummon extends AbstractAnimatedMapleMapObject {

    private MapleCharacter owner;
    private byte skillLevel;
    private int skill, hp;
    private SummonMovementType movementType;

    public MapleSummon(MapleCharacter owner, int skill, Point pos, SummonMovementType movementType) {
        this.owner = owner;
        this.skill = skill;
        this.skillLevel = owner.getSkillLevel(SkillFactory.getSkill(skill));
        if (skillLevel == 0) {
            throw new RuntimeException();
        }
        this.movementType = movementType;
        setPosition(pos);
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        if (this != null) {
            client.announce(SummonedPool.Created(this, false));
        }
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.announce(SummonedPool.Removed(this, true));
    }

    public MapleCharacter getOwner() {
        return owner;
    }

    public int getSkill() {
        return skill;
    }

    public int getHP() {
        return hp;
    }

    public void addHP(int delta) {
        this.hp += delta;
    }

    public SummonMovementType getMovementType() {
        return movementType;
    }

    public boolean isStationary() {
        return (skill == 3111002 || skill == 3211002 || skill == 5211001 || skill == 13111004);
    }

    public byte getSkillLevel() {
        return skillLevel;
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.SUMMON;
    }

    public final boolean isPuppet() {
        switch (skill) {
            case 3111002:
            case 3211002:
            case 13111004:
                return true;
        }
        return false;
    }

    @Override
    public MapleSummon clone() {
        return null;
    }

    /**
     * @Author ERIC MADE THIS
     */
    public int getAssistType() {
        switch (skill) {
            case DarkKnight.BEHOLDER:
                return AssistType.Heal;
            /* case Mechanic.Satelite:
			case Mechanic.Satelite2:
			case Mechanic.Satelite3:
			    bAssistType = AssistType.Attack_Ex;
			    break;
			case Mechanic.RoboRobo:
			    bAssistType = AssistType.Attack_Manual;
			    break;*/
            case Outlaw.OCTOPUS:
                /// case Captain.SupportOctopus:
                // case WildHunter.Trap:
                return AssistType.Attack;
            case Ranger.PUPPET:
            case Sniper.PUPPET:
            // case Dual5.DummyEffect:
            case WindArcher.PUPPET:
                // case WildHunter.MineDummySummoned:
                // case Mechanic.TeslaCoil:
                // case Mechanic.VelocityControler:
                // case Mechanic.HealingRobot_H_LX:
                /// case Mechanic.RoboRobo:
                // case Mechanic.SG88:
                // case Mechanic.AR01:
                // case Mechanic.RoboRoboDummy:
                return AssistType.None;
            default: {
                return AssistType.Attack;
            }
        }
    }

    /**
     * @Author ERIC MADE THIS
     */
    public class AssistType {

        public static final int None = 0, Attack = 1, Heal = 2, Attack_Ex = 3, Summon = 4, Attack_Manual = 5, Attack_Counter = 6;
    }
}
