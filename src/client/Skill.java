package client;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import server.MapleStatEffect;
import server.life.Element;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.LittleEndianWriter;

public class Skill {

    public int id;
    public List<MapleStatEffect> effects = new ArrayList<>();
    public Element element;
    public int animationTime, skillType, delay, masterLevel, combatOrders;
    public int job;
    public boolean action;
    public Map<Integer, Point> lt = new HashMap<>();
    public Map<Integer, Point> rb = new HashMap<>();
    public Map<Integer, Integer> range = new HashMap<>();

    public Skill(int id) {
        this.id = id;
        this.job = id / 10000;
    }

    public void save(LittleEndianWriter oPacket) {
        oPacket.writeInt(effects.size());
        for (MapleStatEffect mse : effects) {
            mse.save(oPacket);
        }
        oPacket.writeMapleAsciiString(element.name());
        oPacket.writeInt(animationTime);
        oPacket.writeInt(skillType);
        oPacket.writeInt(delay);
        oPacket.writeInt(masterLevel);
        oPacket.writeInt(job);
        oPacket.writeBoolean(action);
        oPacket.writeInt(lt.size());
        for (int i : lt.keySet()) {
            oPacket.writeInt(i);
            oPacket.writePos(lt.get(i));
        }
        oPacket.writeInt(rb.size());
        for (int i : rb.keySet()) {
            oPacket.writeInt(i);
            oPacket.writePos(rb.get(i));
        }
        oPacket.writeInt(range.size());
        for (int i : range.keySet()) {
            oPacket.writeInt(i);
            oPacket.writeInt(range.get(i));
        }
    }

    public void load(LittleEndianAccessor iPacket) {
        int size = iPacket.readInt();
        MapleStatEffect mse = new MapleStatEffect();
        mse.load(iPacket);
        effects.add(mse);

        element = Element.valueOf(iPacket.readMapleAsciiString());
        animationTime = iPacket.readInt();
        skillType = iPacket.readInt();
        delay = iPacket.readInt();
        masterLevel = iPacket.readInt();
        job = iPacket.readInt();
        action = iPacket.readBoolean();
        size = iPacket.readInt();
        for (int i = 0; i < size; i++) {
            lt.put(iPacket.readInt(), iPacket.readPos());
        }
        size = iPacket.readInt();
        for (int i = 0; i < size; i++) {
            rb.put(iPacket.readInt(), iPacket.readPos());
        }
        size = iPacket.readInt();
        for (int i = 0; i < size; i++) {
            range.put(iPacket.readInt(), iPacket.readInt());
        }
    }

    public int getId() {
        return id;
    }

    public MapleStatEffect getEffect(int level) {
        return effects.get(level - 1);
    }

    public int getMaxLevel() {
        return effects.size();
    }

    public int getMasterLevel() {
        return masterLevel;
    }

    public int getCombatOrders() {
        return combatOrders;
    }

    public boolean isFourthJob() {
        if (job == 2212) {
            return false;
        }
        if (id == 22170001 || id == 22171003 || id == 22171004 || id == 22181002 || id == 22181003) {
            return true;
        }
        return job % 10 == 2;
    }

    public Element getElement() {
        return element;
    }

    public int getAnimationTime() {
        return animationTime;
    }

    public int getDelay() {
        return delay;
    }

    public int getSkillType() {
        return skillType;
    }

    public boolean isBeginnerSkill() {
        return id % 10000000 < 10000;
    }

    public boolean getAction() {
        return action;
    }
}
