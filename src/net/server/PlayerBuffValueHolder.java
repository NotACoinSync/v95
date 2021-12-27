package net.server;

import client.SkillFactory;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import server.ItemInformationProvider;
import server.MapleStatEffect;
import server.life.MobSkill;

public class PlayerBuffValueHolder implements Externalizable {

    public long startTime, duration;
    public int sourceid, sourcelevel;
    public Integer skillLevel;
    public boolean disease, skill, itemEffect;
    private MapleStatEffect effect;

    public PlayerBuffValueHolder() {
        super();
    }

    public PlayerBuffValueHolder(long startTime, long duration, MapleStatEffect effect) {
        this.startTime = startTime;
        this.duration = duration;
        this.effect = effect;
        this.sourceid = effect.getSourceId();
        this.sourcelevel = effect.getSourceLevel();
        this.skillLevel = effect.getSkilLevel();
        this.disease = effect.isDisease();
        this.skill = effect.isSkill();
        this.itemEffect = effect.itemEffect;
    }

    public MapleStatEffect getEffect() {
        if (effect != null) {
            return effect;
        }
        if (disease) {
            effect = MapleStatEffect.loadDebuffEffectFromMobSkill(new MobSkill(sourceid, sourcelevel));
        } else if (skill) {
            effect = SkillFactory.getSkill(sourceid).getEffect(skillLevel);
        } else if (itemEffect) {
            effect = ItemInformationProvider.getInstance().getItemData(sourceid).itemEffect;
        }
        return effect;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(startTime);
        out.writeLong(duration);
        out.writeInt(sourceid);
        out.writeInt(sourcelevel);
        if (skillLevel != null) {
            out.writeInt(skillLevel);
        } else {
            out.writeInt(1);
        }
        out.writeBoolean(disease);
        out.writeBoolean(skill);
        out.writeBoolean(itemEffect);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        startTime = in.readLong();
        duration = in.readLong();
        sourceid = in.readInt();
        sourcelevel = in.readInt();
        skillLevel = in.readInt();
        disease = in.readBoolean();
        skill = in.readBoolean();
        itemEffect = in.readBoolean();
    }
}
