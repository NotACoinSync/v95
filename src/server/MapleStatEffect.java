package server;

import client.*;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.player.SecondaryStat;
import client.player.buffs.twostate.*;
import constants.ItemConstants;
import constants.SkillConstants;
import constants.skills.*;
import constants.skills.resistance.BattleMage;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.*;
import java.util.Map.Entry;
import net.server.PlayerBuffValueHolder;
import provider.MapleData;
import provider.MapleDataTool;
import server.life.MapleMonster;
import server.life.MobSkill;
import server.life.MobStat;
import server.life.MobStatData;
import server.maps.MapleMap;
import server.maps.SummonMovementType;
import server.maps.objects.*;
import tools.*;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.LittleEndianWriter;
import tools.packets.CField.userpool.UserRemote;
import tools.packets.EffectPacket;
import tools.packets.PacketHelper;
import tools.packets.UserPool;
import tools.packets.WvsContext;

public class MapleStatEffect {

    private short watk = 0, matk = 0, wdef = 0, mdef = 0, acc = 0, avoid = 0, speed = 0, jump = 0;
    private short hp = 0, mp = 0;
    private double hpR = 0, mpR = 0;
    private short mpCon = 0, hpCon = 0;
    private int duration = -1;
    private boolean overTime, disease = false; // TODO: repeatEffect
    private int sourceid, sourceLevel = 0;
    private Integer skillLevel;
    private int moveTo = -1;
    private boolean skill;
    private List<Pair<SecondaryStat, BuffDataHolder>> statups = new ArrayList<>();
    private Map<MobStat, MobStatData> monsterStatus = new HashMap<>();
    private int x = 0, y = 0, mobCount = 1, moneyCon = 0, cooldown = 0, morphId = 0, ghost = 0, fatigue = 0, berserk, booster;
    private double prop = 0;
    private int itemCon = 0, itemConNo = 0;
    private int damage = 100, attackCount = 1, fixdamage = -1;
    private Point lt, rb;
    private byte bulletCount = 1, bulletConsume = 0;
    private int itemupbyitem;
    private int mesoupbyitem;
    private int prob; // probability bonus for drops (30 = 1.30x)
    public boolean itemEffect;
    public static List<Integer> UnhandledSkillId = new ArrayList<>();

    public void load(LittleEndianAccessor glea) {
        watk = glea.readShort();
        matk = glea.readShort();
        wdef = glea.readShort();
        mdef = glea.readShort();
        acc = glea.readShort();
        avoid = glea.readShort();
        speed = glea.readShort();
        jump = glea.readShort();
        hp = glea.readShort();
        mp = glea.readShort();
        hpR = glea.readDouble();
        mpR = glea.readDouble();
        mpCon = glea.readShort();
        hpCon = glea.readShort();
        duration = glea.readInt();
        overTime = glea.readBoolean();
        disease = glea.readBoolean();
        sourceid = glea.readInt();
        sourceLevel = glea.readInt();
        if (glea.readBoolean()) {
            skillLevel = glea.readInt();
        }
        moveTo = glea.readInt();
        skill = glea.readBoolean();
        int statupSize = glea.readInt();
        for (int i = 0; i < statupSize; i++) {
            statups.add(new Pair<>(SecondaryStat.getByShift(glea.readInt()), new BuffDataHolder(glea.readInt(), glea.readInt(), glea.readInt())));
        }
        statupSize = glea.readInt();
        for (int i = 0; i < statupSize; i++) {
            MobStat mobStat = MobStat.getByValue(glea.readInt());
            MobStatData data = new MobStatData();
            data.decode(glea);
            monsterStatus.put(mobStat, data);
        }
        x = glea.readInt();
        y = glea.readInt();
        mobCount = glea.readInt();
        moneyCon = glea.readInt();
        cooldown = glea.readInt();
        morphId = glea.readInt();
        ghost = glea.readInt();
        fatigue = glea.readInt();
        berserk = glea.readInt();
        booster = glea.readInt();
        prop = glea.readDouble();
        itemCon = glea.readInt();
        itemConNo = glea.readInt();
        damage = glea.readInt();
        attackCount = glea.readInt();
        fixdamage = glea.readInt();
        if (glea.readBoolean()) {
            lt = glea.readPos();
        }
        if (glea.readBoolean()) {
            rb = glea.readPos();
        }
        bulletCount = glea.readByte();
        bulletConsume = glea.readByte();
        itemupbyitem = glea.readInt();
        mesoupbyitem = glea.readInt();
        prob = glea.readInt();
        itemEffect = glea.readBoolean();
    }

    public void save(LittleEndianWriter oPacket) {
        oPacket.writeShort(watk);
        oPacket.writeShort(matk);
        oPacket.writeShort(wdef);
        oPacket.writeShort(mdef);
        oPacket.writeShort(acc);
        oPacket.writeShort(avoid);
        oPacket.writeShort(speed);
        oPacket.writeShort(jump);
        oPacket.writeShort(hp);
        oPacket.writeShort(mp);
        oPacket.writeDouble(hpR);
        oPacket.writeDouble(mpR);
        oPacket.writeShort(mpCon);
        oPacket.writeShort(hpCon);
        oPacket.writeInt(duration);
        oPacket.writeBoolean(overTime);
        oPacket.writeBoolean(disease);
        oPacket.writeInt(sourceid);
        oPacket.writeInt(sourceLevel);
        oPacket.writeBoolean(skillLevel != null);
        if (skillLevel != null) {
            oPacket.writeInt(skillLevel);
        }
        oPacket.writeInt(moveTo);
        oPacket.writeBoolean(skill);
        oPacket.writeInt(statups.size());
        for (Pair<SecondaryStat, BuffDataHolder> pair : statups) {
            oPacket.writeInt(pair.left.getShift());
            oPacket.writeInt(pair.right.getSourceID());
            oPacket.writeInt(pair.right.getSourceLevel());
            oPacket.writeInt(pair.right.getValue());
        }
        oPacket.writeInt(monsterStatus.size());
        for (Entry<MobStat, MobStatData> data : monsterStatus.entrySet()) {
            oPacket.writeInt(data.getKey().getShift());
            data.getValue().encode(oPacket);
        }
        oPacket.writeInt(x);
        oPacket.writeInt(y);
        oPacket.writeInt(mobCount);
        oPacket.writeInt(moneyCon);
        oPacket.writeInt(cooldown);
        oPacket.writeInt(morphId);
        oPacket.writeInt(ghost);
        oPacket.writeInt(fatigue);
        oPacket.writeInt(berserk);
        oPacket.writeInt(booster);
        oPacket.writeDouble(prop);
        oPacket.writeInt(itemCon);
        oPacket.writeInt(itemConNo);
        oPacket.writeInt(damage);
        oPacket.writeInt(attackCount);
        oPacket.writeInt(fixdamage);
        oPacket.writeBoolean(lt != null);
        if (lt != null) {
            oPacket.writePos(lt);
        }
        oPacket.writeBoolean(rb != null);
        if (rb != null) {
            oPacket.writePos(rb);
        }
        oPacket.write(bulletCount);
        oPacket.write(bulletConsume);
        oPacket.writeInt(itemupbyitem);
        oPacket.writeInt(mesoupbyitem);
        oPacket.writeInt(prob);
        oPacket.writeBoolean(itemEffect);
    }

    public static MapleStatEffect loadSkillEffectFromData(MapleData source, int skillid, boolean overtime, int level) {
        return loadFromData(source, skillid, true, false, overtime, level);
    }

    public static MapleStatEffect loadItemEffectFromData(MapleData source, int itemid, int level) {
        return loadFromData(source, itemid, false, true, false, level);
    }

    private void addLegalSecondStat(List<Pair<SecondaryStat, BuffDataHolder>> list, SecondaryStat buffstat, Integer value) {
        if (value != 0) {
            list.add(new Pair<>(buffstat, new BuffDataHolder(getSourceId(), getSourceLevel(), value)));
        }
    }

    public static MapleStatEffect loadDebuffEffectFromMobSkill(MobSkill source) {
        MapleStatEffect ret = new MapleStatEffect();
        ret.skill = true;
        ret.sourceid = source.getSkillId();
        ret.sourceLevel = source.getSkillLevel();
        ret.disease = true;
        ret.overTime = true;
        ret.x = source.getX();
        ret.y = source.getY();
        ret.duration = (int) source.getDuration();
        ret.cooldown = (int) source.getCoolTime();
        ret.rb = source.getRb();
        ret.lt = source.getLt();
        ArrayList<Pair<SecondaryStat, BuffDataHolder>> statups = new ArrayList<>();
        switch (ret.sourceid) {
            case 120:
                statups.add(new Pair<>(SecondaryStat.Seal, new BuffDataHolder(source.getSkillId(), source.getSkillLevel(), ret.x)));
                break;
            case 121:
                statups.add(new Pair<>(SecondaryStat.Darkness, new BuffDataHolder(source.getSkillId(), source.getSkillLevel(), ret.x)));
                break;
            case 122:
                statups.add(new Pair<>(SecondaryStat.Weakness, new BuffDataHolder(source.getSkillId(), source.getSkillLevel(), ret.x)));
                break;
            case 123:
                statups.add(new Pair<>(SecondaryStat.Stun, new BuffDataHolder(source.getSkillId(), source.getSkillLevel(), ret.x)));
                break;
            case 124:
                statups.add(new Pair<>(SecondaryStat.Curse, new BuffDataHolder(source.getSkillId(), source.getSkillLevel(), ret.x)));
                break;
            case 125:
                statups.add(new Pair<>(SecondaryStat.Poison, new BuffDataHolder(source.getSkillId(), source.getSkillLevel(), ret.x)));
                break;
            case 126:
                statups.add(new Pair<>(SecondaryStat.Slow, new BuffDataHolder(source.getSkillId(), source.getSkillLevel(), ret.x)));
                break;
            case 128:
                statups.add(new Pair<>(SecondaryStat.Attract, new BuffDataHolder(source.getSkillId(), source.getSkillLevel(), ret.x)));
                break;
            case 132:
                statups.add(new Pair<>(SecondaryStat.ReverseInput, new BuffDataHolder(source.getSkillId(), source.getSkillLevel(), ret.x)));
                break;
            case 133:
                statups.add(new Pair<>(SecondaryStat.Undead, new BuffDataHolder(source.getSkillId(), source.getSkillLevel(), ret.x)));
                break;
        }
        statups.trimToSize();
        ret.statups = statups;
        return ret;
    }

    private static MapleStatEffect loadFromData(MapleData source, int sourceid, boolean skill, boolean itemEffect, boolean overTime, int level) {
        MapleStatEffect ret = new MapleStatEffect();

        ret.duration = MapleDataTool.getInt("time", source, 0, level);
        ret.hp = (short) MapleDataTool.getInt("hp", source, 0, level);
        ret.mp = (short) MapleDataTool.getInt("mp", source, 0, level);
        if (skill) {
            ret.hpR = MapleDataTool.getInt("x", source, 0, level) / 100.0;
            ret.mpR = MapleDataTool.getInt("y", source, 0, level) / 100.0;
        } else {
            ret.hpR = MapleDataTool.getInt("hpR", source, 0, 1);
            ret.mpR = MapleDataTool.getInt("mpR", source, 0, 1);
        }
        ret.mpCon = (short) MapleDataTool.getInt("mpCon", source, 0, level);
        ret.hpCon = (short) MapleDataTool.getInt("hpCon", source, 0, level);
        int iprop = MapleDataTool.getInt("prop", source, 100, level);
        ret.prop = iprop / 100.0;
        ret.mobCount = MapleDataTool.getInt("mobCount", source, 1, level);
        ret.cooldown = MapleDataTool.getInt("cooltime", source, 0, level);
        ret.morphId = MapleDataTool.getInt("morph", source, 0, level);
        ret.ghost = MapleDataTool.getInt("ghost", source, 0, level);
        ret.fatigue = MapleDataTool.getInt("incFatigue", source, 0, level);
        ret.sourceid = sourceid;
        ret.skillLevel = ObjectParser.isInt(source.getName());
        if (ret.skillLevel == null) {
            ret.skillLevel = 0;
        }
        ret.skill = skill;
        ret.itemEffect = itemEffect;
        if (!ret.skill && ret.duration > -1) {
            ret.overTime = true;
        } else {
            ret.duration *= 1000; // items have their times stored in ms, of course
            ret.overTime = overTime;
        }
        ArrayList<Pair<SecondaryStat, BuffDataHolder>> statups = new ArrayList<>();
        ret.watk = (short) MapleDataTool.getInt("pad", source, 0, level);
        ret.wdef = (short) MapleDataTool.getInt("pdd", source, 0, level);
        ret.matk = (short) MapleDataTool.getInt("mad", source, 0, level);
        ret.mdef = (short) MapleDataTool.getInt("mdd", source, 0, level);
        ret.acc = (short) MapleDataTool.getIntConvert("acc", source, 0, level);
        ret.avoid = (short) MapleDataTool.getInt("eva", source, 0, level);
        ret.speed = (short) MapleDataTool.getInt("speed", source, 0, level);
        ret.jump = (short) MapleDataTool.getInt("jump", source, 0, level);
        ret.berserk = MapleDataTool.getInt("berserk", source, 0, level);
        ret.booster = MapleDataTool.getInt("booster", source, 0, level);
        ret.mesoupbyitem = MapleDataTool.getInt("mesoupbyitem", source, 0, level);
        ret.itemupbyitem = MapleDataTool.getInt("itemupbyitem", source, 0, level);
        if (ret.overTime && ret.getSummonMovementType() == null) {
            ret.addLegalSecondStat(statups, SecondaryStat.PAD, Integer.valueOf(ret.watk));
            ret.addLegalSecondStat(statups, SecondaryStat.PDD, Integer.valueOf(ret.wdef));
            ret.addLegalSecondStat(statups, SecondaryStat.MAD, Integer.valueOf(ret.matk));
            ret.addLegalSecondStat(statups, SecondaryStat.MDD, Integer.valueOf(ret.mdef));
            ret.addLegalSecondStat(statups, SecondaryStat.ACC, Integer.valueOf(ret.acc));
            ret.addLegalSecondStat(statups, SecondaryStat.EVA, Integer.valueOf(ret.avoid));
            ret.addLegalSecondStat(statups, SecondaryStat.Speed, Integer.valueOf(ret.speed));
            ret.addLegalSecondStat(statups, SecondaryStat.Jump, Integer.valueOf(ret.jump));
            ret.addLegalSecondStat(statups, SecondaryStat.DojangBerserk, ret.berserk);
            ret.addLegalSecondStat(statups, SecondaryStat.Booster, ret.booster);
            ret.addLegalSecondStat(statups, SecondaryStat.MesoUpByItem, ret.mesoupbyitem);
            ret.addLegalSecondStat(statups, SecondaryStat.ItemUpByItem, ret.itemupbyitem);
        }
        MapleData ltd = source.getChildByPath("lt");
        if (ltd != null) {
            ret.lt = (Point) ltd.getData();
            ret.rb = (Point) source.getChildByPath("rb").getData();
        }
        ret.x = MapleDataTool.getInt("x", source, 0, level);
        ret.y = MapleDataTool.getInt("y", source, 0, level);
        ret.damage = MapleDataTool.getIntConvert("damage", source, 100, level);
        ret.fixdamage = MapleDataTool.getIntConvert("fixdamage", source, -1, level);
        ret.attackCount = MapleDataTool.getIntConvert("attackCount", source, 1, level);
        ret.bulletCount = (byte) MapleDataTool.getIntConvert("bulletCount", source, 1, level);
        ret.bulletConsume = (byte) MapleDataTool.getIntConvert("bulletConsume", source, 0, level);
        ret.moneyCon = MapleDataTool.getIntConvert("moneyCon", source, 0, level);
        ret.itemCon = MapleDataTool.getInt("itemCon", source, 0, level);
        ret.itemConNo = MapleDataTool.getInt("itemConNo", source, 0, level);
        ret.moveTo = MapleDataTool.getInt("moveTo", source, -1, level);
        ret.prob = MapleDataTool.getInt("prob", source, level);
        Map<MobStat, MobStatData> monsterStatus = new ArrayMap<>();
        if (skill) {
            switch (sourceid) {
                // BEGINNER
                case Beginner.RECOVERY:
                case Noblesse.RECOVERY:
                case Legend.RECOVERY:
                case Evan.RECOVERY:
                    statups.add(new Pair<>(SecondaryStat.Regen, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    break;
                case Beginner.EchoOfHero:
                case Noblesse.EchoOfHero:
                case Legend.EchoOfHero:
                case Evan.EchoOfHero:
                    statups.add(new Pair<>(SecondaryStat.MaxLevelBuff, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    break;
                case Beginner.MONSTER_RIDER:
                case Noblesse.MONSTER_RIDER:
                case Legend.MONSTER_RIDER:
                case Evan.MONSTER_RIDER:
                case Corsair.BATTLE_SHIP:
                case Beginner.SPACESHIP:
                case Noblesse.SPACESHIP:
                case Beginner.YETI_MOUNT1:
                case Beginner.YETI_MOUNT2:
                case Noblesse.YETI_MOUNT1:
                case Noblesse.YETI_MOUNT2:
                case Legend.YETI_MOUNT1:
                case Legend.YETI_MOUNT2:
                case Beginner.WITCH_BROOMSTICK:
                case Noblesse.WITCH_BROOMSTICK:
                case Legend.WITCH_BROOMSTICK:
                case Beginner.BALROG_MOUNT:
                case Noblesse.BALROG_MOUNT:
                case Legend.BALROG_MOUNT:
                    statups.add(new Pair<>(SecondaryStat.RideVehicle, new BuffDataHolder(sourceid, ret.skillLevel, sourceid)));
                    break;
                case Beginner.BERSERK_FURY:
                case Noblesse.BERSERK_FURY:
                case Evan.BERSERK_FURY:
                    statups.add(new Pair<>(SecondaryStat.DojangBerserk, new BuffDataHolder(sourceid, ret.skillLevel, 1)));
                    break;
                case Beginner.INVINCIBLE_BARRIER:
                case Noblesse.INVINCIBLE_BARRIER:
                case Legend.INVICIBLE_BARRIER:
                case Evan.INVINCIBLE_BARRIER:
                    statups.add(new Pair<>(SecondaryStat.DojangInvincible, new BuffDataHolder(sourceid, ret.skillLevel, 1)));
                    break;
                case Fighter.POWER_GUARD:
                case Page.POWER_GUARD:
                    statups.add(new Pair<>(SecondaryStat.PowerGuard, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    break;
                case Spearman.HYPER_BODY:
                case SuperGM.HyperBody:
                    statups.add(new Pair<>(SecondaryStat.MaxHP, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    statups.add(new Pair<>(SecondaryStat.MaxMP, new BuffDataHolder(sourceid, ret.skillLevel, ret.y)));
                    break;
                case Crusader.COMBO:
                case DawnWarrior.COMBO:
                    statups.add(new Pair<>(SecondaryStat.ComboCounter, new BuffDataHolder(sourceid, ret.skillLevel, 1)));
                    break;
                case WhiteKnight.BW_FIRE_CHARGE:
                case WhiteKnight.BW_ICE_CHARGE:
                case WhiteKnight.BW_LIT_CHARGE:
                case WhiteKnight.SWORD_FIRE_CHARGE:
                case WhiteKnight.SWORD_ICE_CHARGE:
                case WhiteKnight.SWORD_LIT_CHARGE:
                case Paladin.BW_HOLY_CHARGE:
                case Paladin.SWORD_HOLY_CHARGE:
                case DawnWarrior.SOUL_CHARGE:
                case ThunderBreaker.LIGHTNING_CHARGE:
                    statups.add(new Pair<>(SecondaryStat.WeaponCharge, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    break;
                case DragonKnight.DRAGON_BLOOD:
                    statups.add(new Pair<>(SecondaryStat.DragonBlood, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    break;
                case DragonKnight.DRAGON_ROAR:
                    ret.hpR = -ret.x / 100.0;
                    break;
                case Hero.STANCE:
                case Paladin.STANCE:
                case DarkKnight.STANCE:
                case Aran.FREEZE_STANDING:
                    statups.add(new Pair<>(SecondaryStat.Stance, new BuffDataHolder(sourceid, ret.skillLevel, iprop)));
                    break;
                case DawnWarrior.FINAL_ATTACK:
                    statups.add(new Pair<>(SecondaryStat.SoulMasterFinal, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    break;
                case WindArcher.FINAL_ATTACK:
                    statups.add(new Pair<>(SecondaryStat.WindBreakerFinal, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    break;
                // MAGICIAN
                case Magician.MAGIC_GUARD:
                case BlazeWizard.MAGIC_GUARD:
                case Evan.MAGIC_GUARD:
                    statups.add(new Pair<>(SecondaryStat.MagicGuard, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    break;
                case Cleric.INVINCIBLE:
                    statups.add(new Pair<>(SecondaryStat.Invincible, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    break;
                case Priest.HOLY_SYMBOL:
                case SuperGM.HolySymbol:
                    statups.add(new Pair<>(SecondaryStat.HolySymbol, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    break;
                case FPArchMage.INFINITY:
                case ILArchMage.INFINITY:
                case Bishop.INFINITY:
                    statups.add(new Pair<>(SecondaryStat.Infinity, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    break;
                case FPArchMage.MANA_REFLECTION:
                case ILArchMage.MANA_REFLECTION:
                case Bishop.MANA_REFLECTION:
                    statups.add(new Pair<>(SecondaryStat.MagicReflection, new BuffDataHolder(sourceid, ret.skillLevel, 1)));
                    break;
                case Bishop.HOLY_SHIELD:
                    statups.add(new Pair<>(SecondaryStat.HolyShield, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    break;
                case BlazeWizard.ELEMENTAL_RESET:
                    statups.add(new Pair<>(SecondaryStat.ElementalReset, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    break;
                case Evan.ELEMENTAL_RESET:
                    statups.add(new Pair<>(SecondaryStat.ElementalReset, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    break;
                case Evan.MAGIC_SHIELD:
                    statups.add(new Pair<>(SecondaryStat.MagicShield, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    break;
                case Evan.MAGIC_RESISTANCE:
                    statups.add(new Pair<>(SecondaryStat.MagicResistance, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    break;
                // case Evan.BLESSING_OF_THE_ONYX:
                // break;
                case Evan.SLOW:
                    statups.add(new Pair<>(SecondaryStat.EvanSlow, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    break;
                // BOWMAN
                case Priest.MysticDoor:
                case Hunter.SOUL_ARROW:
                case Crossbowman.SOUL_ARROW:
                case WindArcher.SOUL_ARROW:
                    statups.add(new Pair<>(SecondaryStat.SoulArrow, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    break;
                case Ranger.PUPPET:
                case Sniper.PUPPET:
                case WindArcher.PUPPET:
                case Outlaw.OCTOPUS:
                case Corsair.WRATH_OF_THE_OCTOPI:
                    statups.add(new Pair<>(SecondaryStat.PUPPET, new BuffDataHolder(sourceid, ret.skillLevel, 1)));
                    break;
                case Bowmaster.CONCENTRATE:
                    statups.add(new Pair<>(SecondaryStat.Concentration, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    break;
                case Bowmaster.HAMSTRING:
                    statups.add(new Pair<>(SecondaryStat.HamString, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    monsterStatus.put(MobStat.Speed, new MobStatData(MobStat.Speed, ret.x, sourceid, ret.duration));
                    break;
                case Marksman.BLIND:
                    statups.add(new Pair<>(SecondaryStat.Blind, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    monsterStatus.put(MobStat.ACC, new MobStatData(MobStat.ACC, ret.x, sourceid, ret.duration));
                    break;
                case Bowmaster.SHARP_EYES:
                case Marksman.SHARP_EYES:
                    statups.add(new Pair<>(SecondaryStat.SharpEyes, new BuffDataHolder(sourceid, ret.skillLevel, (ret.x << 8 | ret.y))));
                    break;
                case WindArcher.WIND_WALK:
                    statups.add(new Pair<>(SecondaryStat.WindWalk, new BuffDataHolder(sourceid, ret.skillLevel, 1)));
                    break;
                // THIEF
                case Rogue.DARK_SIGHT:
                case NightWalker.DARK_SIGHT:
                    statups.add(new Pair<>(SecondaryStat.DarkSight, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    break;
                case Hermit.MESO_UP:
                    statups.add(new Pair<>(SecondaryStat.MesoUp, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    break;
                case Hermit.SHADOW_PARTNER:
                case NightWalker.SHADOW_PARTNER:
                    statups.add(new Pair<>(SecondaryStat.ShadowPartner, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    break;
                case ChiefBandit.MESO_GUARD:
                    statups.add(new Pair<>(SecondaryStat.MesoGuard, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    break;
                case ChiefBandit.PICKPOCKET:
                    statups.add(new Pair<>(SecondaryStat.PickPocket, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    break;
                case NightLord.ShadowStars:
                    statups.add(new Pair<>(SecondaryStat.SpiritJavelin, new BuffDataHolder(sourceid, ret.skillLevel, 0)));
                    break;
                case BladeLord.MIRROR_IMAGE:
                    statups.add(new Pair<>(SecondaryStat.MirrorImaging, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    break;
                // PIRATE
                case Pirate.DASH:
                case ThunderBreaker.DASH:
                case Beginner.SPACE_DASH:
                case Noblesse.SPACE_DASH:
                    statups.add(new Pair<>(SecondaryStat.Dash_Speed, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    statups.add(new Pair<>(SecondaryStat.Dash_Jump, new BuffDataHolder(sourceid, ret.skillLevel, ret.y)));
                    break;
                case Corsair.SPEED_INFUSION:
                case Buccaneer.SPEED_INFUSION:
                case ThunderBreaker.SPEED_INFUSION:
                    statups.add(new Pair<>(SecondaryStat.PartyBooster, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    break;
                case Outlaw.HOMING_BEACON:
                case Corsair.BULLSEYE:
                    statups.add(new Pair<>(SecondaryStat.GuidedBullet, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    break;
                case ThunderBreaker.SPARK:
                    statups.add(new Pair<>(SecondaryStat.Spark, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    break;
                case BladeSpecialist.TORNADO_SPIN:
                    statups.add(new Pair<>(SecondaryStat.Dash_Speed, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    statups.add(new Pair<>(SecondaryStat.Dash_Jump, new BuffDataHolder(sourceid, ret.skillLevel, ret.y)));
                    break;
                case BladeLord.OWL_SPIRIT:
                    statups.add(new Pair<>(SecondaryStat.SuddenDeath, new BuffDataHolder(sourceid, ret.x, ret.y)));
                    break;
                case BladeMaster.THORNS:
                    statups.add(new Pair<>(SecondaryStat.ThornsEffect, new BuffDataHolder(sourceid, ret.skillLevel, (ret.x << 8 | ret.y))));
                    break;
                case BladeMaster.FINAL_CUT:
                    ret.hpR = -ret.x / 100.0;
                    statups.add(new Pair<>(SecondaryStat.FinalCut, new BuffDataHolder(sourceid, ret.skillLevel, ret.y)));
                    break;
                case BattleMage.DarkAura:
                    statups.add(new Pair<>(SecondaryStat.DarkAura, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    break;
                case BattleMage.BlueAura:
                    statups.add(new Pair<>(SecondaryStat.BlueAura, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    break;
                case BattleMage.YellowAura:
                    statups.add(new Pair<>(SecondaryStat.YellowAura, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    break;
                // MULTIPLE
                case Aran.POLEARM_BOOSTER:
                case Fighter.AXE_BOOSTER:
                case Fighter.SWORD_BOOSTER:
                case Page.BW_BOOSTER:
                case Page.SWORD_BOOSTER:
                case Spearman.POLEARM_BOOSTER:
                case Spearman.SPEAR_BOOSTER:
                case Hunter.BOW_BOOSTER:
                case Crossbowman.CROSSBOW_BOOSTER:
                case Assassin.CLAW_BOOSTER:
                case Bandit.DAGGER_BOOSTER:
                case FPMage.SPELL_BOOSTER:
                case ILMage.SPELL_BOOSTER:
                case Brawler.KNUCKLER_BOOSTER:
                case Gunslinger.GUN_BOOSTER:
                case DawnWarrior.SWORD_BOOSTER:
                case BlazeWizard.SPELL_BOOSTER:
                case WindArcher.BOW_BOOSTER:
                case NightWalker.CLAW_BOOSTER:
                case ThunderBreaker.KNUCKLER_BOOSTER:
                case Evan.MAGIC_BOOSTER:
                case BladeRecruit.KATARA_BOOSTER:
                case BattleMage.StaffBoost:
                    statups.add(new Pair<>(SecondaryStat.Booster, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    break;
                case Hero.MAPLE_WARRIOR:
                case Paladin.MAPLE_WARRIOR:
                case DarkKnight.MAPLE_WARRIOR:
                case FPArchMage.MAPLE_WARRIOR:
                case ILArchMage.MAPLE_WARRIOR:
                case Bishop.MAPLE_WARRIOR:
                case Bowmaster.MAPLE_WARRIOR:
                case Marksman.MAPLE_WARRIOR:
                case NightLord.MAPLE_WARRIOR:
                case Shadower.MAPLE_WARRIOR:
                case Corsair.MAPLE_WARRIOR:
                case Buccaneer.MAPLE_WARRIOR:
                case Aran.MAPLE_WARRIOR:
                case Evan.MAPLE_WARRIOR:
                    statups.add(new Pair<>(SecondaryStat.BasicStatUp, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    break;
                // SUMMON
                case Ranger.SILVER_HAWK:
                case Sniper.GOLDEN_EAGLE:
                    statups.add(new Pair<>(SecondaryStat.SUMMON, new BuffDataHolder(sourceid, ret.skillLevel, 1)));
                    monsterStatus.put(MobStat.Stun, new MobStatData(MobStat.Stun, 1, sourceid, ret.duration));
                    break;
                case FPArchMage.ELQUINES:
                case Marksman.FROST_PREY:
                    statups.add(new Pair<>(SecondaryStat.SUMMON, new BuffDataHolder(sourceid, ret.skillLevel, 1)));
                    monsterStatus.put(MobStat.Freeze, new MobStatData(MobStat.Freeze, 1, sourceid, ret.duration));
                    break;
                case Priest.SUMMON_DRAGON:
                case Bowmaster.PHOENIX:
                case ILArchMage.IFRIT:
                case Bishop.BAHAMUT:
                case DarkKnight.BEHOLDER:
                case Outlaw.GAVIOTA:
                case DawnWarrior.SOUL:
                case BlazeWizard.FLAME:
                case WindArcher.STORM:
                case NightWalker.DARKNESS:
                case ThunderBreaker.LIGHTNING:
                case BlazeWizard.IFRIT:
                case BladeMaster.MIRRORED_TARGET:
                    statups.add(new Pair<>(SecondaryStat.SUMMON, new BuffDataHolder(sourceid, ret.skillLevel, 1)));
                    break;
                // ----------------------------- MONSTER STATUS ---------------------------------- //
                case Crusader.ARMOR_CRASH:
                case DragonKnight.POWER_CRASH:
                case WhiteKnight.MAGIC_CRASH:
                    monsterStatus.put(MobStat.SealSkill, new MobStatData(MobStat.SealSkill, 1, sourceid, ret.duration));
                    break;
                case Rogue.DISORDER:
                    monsterStatus.put(MobStat.PAD, new MobStatData(MobStat.PAD, ret.x, sourceid, ret.duration));
                    monsterStatus.put(MobStat.PDR, new MobStatData(MobStat.PDR, ret.y, sourceid, ret.duration));
                    break;
                case Corsair.HYPNOTIZE:
                    monsterStatus.put(MobStat.Dazzle, new MobStatData(MobStat.Dazzle, 1, sourceid, ret.duration));
                    break;
                case NightLord.NINJA_AMBUSH:
                case Shadower.NINJA_AMBUSH:
                    monsterStatus.put(MobStat.Ambush, new MobStatData(MobStat.PDR, ret.damage, sourceid, ret.duration));
                    break;
                case Page.THREATEN:
                    monsterStatus.put(MobStat.PAD, new MobStatData(MobStat.PAD, ret.x, sourceid, ret.duration));
                    monsterStatus.put(MobStat.PDR, new MobStatData(MobStat.PDR, ret.y, sourceid, ret.duration));
                    break;
                case Crusader.AXE_COMA:
                case Crusader.SWORD_COMA:
                case Crusader.SHOUT:
                case WhiteKnight.CHARGE_BLOW:
                case Hunter.ARROW_BOMB:
                case ChiefBandit.ASSAULTER:
                case Shadower.BOOMERANG_STEP:
                case Brawler.BACK_SPIN_BLOW:
                case Brawler.DOUBLE_UPPERCUT:
                case Marauder.ENERGY_BLAST:
                case ThunderBreaker.ENERGY_BLAST:
                case Buccaneer.DEMOLITION:
                case Buccaneer.SNATCH:
                case Buccaneer.BARRAGE:
                case Gunslinger.BLANK_SHOT:
                case DawnWarrior.COMA:
                case Aran.ROLLING_SPIN:
                case Evan.FIRE_BREATH:
                case Evan.BLAZE:
                case BladeLord.FLYING_ASSAULTER:
                    monsterStatus.put(MobStat.Stun, new MobStatData(MobStat.Stun, 1, sourceid, ret.duration));
                    break;
                case NightLord.TAUNT:
                case Shadower.TAUNT:
                    monsterStatus.put(MobStat.Darkness, new MobStatData(MobStat.Darkness, ret.x, sourceid, ret.duration));
                    monsterStatus.put(MobStat.MDR, new MobStatData(MobStat.Poison, ret.x, sourceid, ret.duration));
                    monsterStatus.put(MobStat.PDR, new MobStatData(MobStat.Poison, ret.x, sourceid, ret.duration));
                    break;
                case ILWizard.COLD_BEAM:
                case ILMage.ICE_STRIKE:
                case ILArchMage.BLIZZARD:
                case ILMage.ELEMENT_COMPOSITION:
                case Sniper.BLIZZARD:
                case Outlaw.ICE_SPLITTER:
                case FPArchMage.PARALYZE:
                case Aran.COMBO_TEMPEST:
                case Evan.ICE_BREATH:
                    ret.duration *= 2; // freezing skills are a little strange
                    monsterStatus.put(MobStat.Freeze, new MobStatData(MobStat.Freeze, 1, sourceid, ret.duration));
                    break;
                case FPWizard.SLOW:
                case ILWizard.SLOW:
                case BlazeWizard.SLOW:
                    monsterStatus.put(MobStat.Speed, new MobStatData(MobStat.Speed, ret.x, sourceid, ret.duration));
                    break;
                case FPWizard.POISON_BREATH:
                case FPMage.ELEMENT_COMPOSITION:
                    monsterStatus.put(MobStat.Poison, new MobStatData(MobStat.Poison, 1, sourceid, ret.duration));
                    break;
                case Priest.DOOM:
                    monsterStatus.put(MobStat.Doom, new MobStatData(MobStat.Doom, 1, sourceid, ret.duration));
                    break;
                case ILMage.SEAL:
                case FPMage.SEAL:
                    monsterStatus.put(MobStat.Seal, new MobStatData(MobStat.Seal, 1, sourceid, ret.duration));
                    break;
                case Hermit.SHADOW_WEB: // shadow web
                case NightWalker.SHADOW_WEB:
                    monsterStatus.put(MobStat.Web, new MobStatData(MobStat.Web, 1, sourceid, ret.duration));
                    break;
                case FPArchMage.FIRE_DEMON:
                case ILArchMage.ICE_DEMON:
                    monsterStatus.put(MobStat.Poison, new MobStatData(MobStat.Poison, 1, sourceid, ret.duration));
                    monsterStatus.put(MobStat.Freeze, new MobStatData(MobStat.Freeze, 1, sourceid, ret.duration));
                    break;
                case Evan.PHANTOM_IMPRINT:
                // monsterStatus.put(MonsterStatus.PHANTOM_IMPRINT, ret.x);
                // monsterStatus.put(MobStat.Poison, new MobStatData(MobStat.Poison, 1, sourceid, ret.duration));
                // ARAN
                case Aran.COMBO_ABILITY:
                    statups.add(new Pair<>(SecondaryStat.ComboAbilityBuff, new BuffDataHolder(sourceid, ret.skillLevel, 100)));
                    break;
                case Aran.COMBO_BARRIER:
                    statups.add(new Pair<>(SecondaryStat.ComboBarrier, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    break;
                case Aran.COMBO_DRAIN:
                    statups.add(new Pair<>(SecondaryStat.ComboDrain, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    break;
                case Aran.SMART_KNOCKBACK:
                    statups.add(new Pair<>(SecondaryStat.SmartKnockback, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    break;
                case Aran.BODY_PRESSURE:
                    statups.add(new Pair<>(SecondaryStat.BodyPressure, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
                    break;
                case Aran.SNOW_CHARGE:
                    statups.add(new Pair<>(SecondaryStat.WeaponCharge, new BuffDataHolder(sourceid, ret.skillLevel, ret.duration)));
                    break;
                default: {/*
                     if (!UnhandledSkillId.contains(sourceid)) {
                     UnhandledSkillId.add(sourceid);
                     }
                     StringBuilder text = new StringBuilder();
                     for (Integer i : UnhandledSkillId) {
                     text.append(", ").append(i);
                     }
                     System.out.println("skill ID unHandled: " + text);
                     */
                    break;
                }
            }
        }
        if (ret.isMorph()) {
            statups.add(new Pair<>(SecondaryStat.Morph, new BuffDataHolder(sourceid, ret.skillLevel, ret.getMorph())));
        }
        if (ret.ghost > 0 && !skill) {
            statups.add(new Pair<>(SecondaryStat.Ghost, new BuffDataHolder(sourceid, ret.skillLevel, ret.ghost)));
        }
        ret.monsterStatus = monsterStatus;
        statups.trimToSize();
        ret.statups = statups;
        return ret;
    }

    public void applyPassive(MapleCharacter applyto, MapleMapObject obj, int attack) {
        if (makeChanceResult()) {
            switch (sourceid) { // MP eater
                case FPWizard.MP_EATER:
                case ILWizard.MP_EATER:
                case Cleric.MP_EATER:
                    if (obj == null || obj.getType() != MapleMapObjectType.MONSTER) {
                        return;
                    }
                    MapleMonster mob = (MapleMonster) obj; // x is absorb percentage
                    if (!mob.isBoss()) {
                        int absorbMp = Math.min((int) (mob.getMaxMp() * (getX() / 100.0)), mob.getMp());
                        if (absorbMp > 0) {
                            mob.setMp(mob.getMp() - absorbMp);
                            applyto.addMP(absorbMp);
                            applyto.getClient().announce(EffectPacket.Local.SkillUse.encode(sourceid, applyto.getLevel(), skillLevel));
                            applyto.getMap().announce(applyto, EffectPacket.Remote.SkillUse.encode(applyto.getId(), applyto.getLevel(), sourceid, skillLevel), false);
                        }
                    }
                    break;
            }
        }
    }

    public boolean applyTo(MapleCharacter chr) {
        return applyTo(chr, chr, true, null);
    }

    public boolean applyTo(MapleCharacter chr, Point pos) {
        return applyTo(chr, chr, true, pos);
    }

    public boolean applyTo(MapleCharacter applyfrom, MapleCharacter applyto, boolean firstApply, Point pos) {
        return applyTo(applyfrom, applyto, firstApply, pos, duration, true);
    }

    public boolean applyTo(MapleCharacter applyfrom, MapleCharacter applyto, boolean firstApply, Point pos, long duration, boolean eliteEnhance) {
        if (skill && (sourceid == SuperGM.Hide)) {
            applyto.toggleHide(false);
            return true;
        }
        int hpchange = calcHPChange(applyfrom, firstApply);
        int mpchange = calcMPChange(applyfrom, firstApply);
        if (!skill) {
            // hp is normal pots, hpR is %.-
            if ((hp > 0 || hpR > 0.0) && hpchange > 0) {
                int actualAmount = 0;
                if (applyto.getHp() + hpchange > applyto.getMaxHp()) {
                    actualAmount = applyto.getMaxHp() - applyto.getHp();
                } else {
                    actualAmount = hpchange;
                }
                if (actualAmount > 0) {
                    applyto.gainRSSkillExp(RSSkill.Health, (long) (actualAmount * 0.05));
                }
            }
            if ((mp > 0 || mpR > 0.0) && mpchange > 0) {
                int actualAmount = 0;
                if (applyto.getMp() + mpchange > applyto.getMaxMp()) {
                    actualAmount = applyto.getMaxMp() - applyto.getMp();
                } else {
                    actualAmount = mpchange;
                }
                if (actualAmount > 0) {
                    applyto.gainRSSkillExp(RSSkill.Mana, (long) (actualAmount * 0.05));
                }
            }
        }
        if (firstApply) {
            if (itemConNo != 0) {
                MapleInventoryManipulator.removeById(applyto.getClient(), ItemInformationProvider.getInstance().getInventoryType(itemCon), itemCon, itemConNo, true, false);
            }
        }
        List<Pair<MapleStat, Integer>> hpmpupdate = new ArrayList<>(2);
        if (!firstApply && isResurrection()) {
            hpchange = applyto.getMaxHp();
            if (sourceid == Evan.SOUL_STONE) {
                double hp = hpchange;
                hp *= this.getX() / 100D;
                hpchange = (int) Math.round(hp);
            }
            applyto.setStance(0);
            applyto.getMap().announce(applyto, UserPool.UserLeaveField(applyto.getId()), false);
            applyto.getMap().announce(applyto, UserPool.UserEnterField(applyto), false);
        }
        if (isDispel() && makeChanceResult()) {
            applyto.dispelDebuffs();
        } else if (isHeroWill()) {
            applyto.cancelAllDebuffs();
        }
        if (isComboReset()) {
            applyto.setCombo((short) 0);
        }
        /*
         if (applyfrom.getMp() < getMpCon()) {
         AutobanFactory.MPCON.addPoint(applyfrom.getAutobanManager(), "mpCon
         hack for skill:" + sourceid + "; Player MP: " + applyto.getMp() + " MP
         Needed: " + getMpCon());
         }
         */
        if (hpchange != 0) {
            if (hpchange < 0 && (-hpchange) > applyto.getHp()) {
                return false;
            }
            int newHp = applyto.getHp() + hpchange;
            if (newHp < 1) {
                newHp = 1;
            }
            applyto.setHp(newHp);
            hpmpupdate.add(new Pair<>(MapleStat.HP, Integer.valueOf(applyto.getHp())));
        }
        int newMp = applyto.getMp() + mpchange;
        if (mpchange != 0) {
            if (mpchange < 0 && -mpchange > applyto.getMp()) {
                return false;
            }
            applyto.setMp(newMp);
            hpmpupdate.add(new Pair<>(MapleStat.MP, Integer.valueOf(applyto.getMp())));
        }
        applyto.getClient().announce(WvsContext.StatChanged(hpmpupdate, true, applyto));
        if (moveTo != -1) {
            if (moveTo != applyto.getMapId()) {
                MapleMap target;
                if (moveTo == 999999999) {
                    target = applyto.getMap().getReturnMap();
                } else {
                    target = applyto.getClient().getChannelServer().getMap(moveTo);
                    int targetid = target.getId() / 10000000;
                    if (targetid != 60 && applyto.getMapId() / 10000000 != 61 && targetid != applyto.getMapId() / 10000000 && targetid != 21 && targetid != 20 && targetid != 12 && (applyto.getMapId() / 10000000 != 10 && applyto.getMapId() / 10000000 != 12)) {
                        return false;
                    }
                }
                applyto.changeMap(target);
            } else {
                return false;
            }
        }
        if (isShadowClaw()) {
            int projectile = 0;
            MapleInventory use = applyto.getInventory(MapleInventoryType.USE);
            for (int i = 1; i <= use.getSlotLimit(); i++) { // impose order...
                Item item = use.getItem((short) i);
                if (item != null) {
                    if (ItemConstants.isThrowingStar(item.getItemId()) && item.getQuantity() >= 200) {
                        projectile = item.getItemId();
                        break;
                    }
                }
            }
            if (projectile == 0) {
                return false;
            } else {
                MapleInventoryManipulator.removeStarById(applyto.getClient(), MapleInventoryType.USE, projectile, 200, false, false);
            }
        }
        SummonMovementType summonMovementType = getSummonMovementType();
        if (overTime || isCygnusFA() || summonMovementType != null || sourceid == BladeMaster.FINAL_CUT) {// find how to properly implement final cut buff?
            applyBuffEffect(applyfrom, applyto, (int) duration, System.currentTimeMillis(), firstApply, eliteEnhance);
        }
        if (firstApply && (overTime || isHeal())) {
            applyPartyBuff(applyfrom);
        }
        if (firstApply && isMonsterBuff()) {
            applyMonsterBuff(applyfrom);
        }
        if (this.getFatigue() != 0) {
            applyto.getMount().setTiredness(applyto.getMount().getTiredness() + this.getFatigue());
        }
        if (summonMovementType != null && pos != null) {
            final MapleSummon tosummon = new MapleSummon(applyfrom, sourceid, pos, summonMovementType);
            applyfrom.getMap().spawnSummon(tosummon);
            applyfrom.addSummon(sourceid, tosummon);
            tosummon.addHP(x);
            if (isBeholder()) {
                tosummon.addHP(1);
            }
        }
        if (isMagicDoor() /*
                 && !FieldLimit.DOOR.check(applyto.getMap().getFieldLimit())
                 */) { // Magic Door
            int y = applyto.getFh() - 30;
            if (y == 0) {
                y = applyto.getPosition().y - 10;
            }
            Point doorPosition = new Point(applyto.getPosition().x, y);
            Point below = applyto.getMap().getGroundBelow(doorPosition);
            if (below != null) {
                doorPosition = below;
            } else {
                y = applyto.getFh();
                if (y == 0) {
                    y = applyto.getPosition().y;
                }
                doorPosition = new Point(applyto.getPosition().x, y);
            }
            MapleDoor door = new MapleDoor(applyto, sourceid, doorPosition);
            applyto.addDoor(door);
            applyto.getMap().spawnDoor(door);
            door = new MapleDoor(door); // The town door
            applyto.addDoor(door);
            door.getTown().spawnDoor(door);
            applyto.disableDoor();
        } else if (isAffectedArea()) {
            Rectangle bounds = calculateBoundingBox(pos != null ? pos : applyfrom.getPosition(), applyfrom.isFacingLeft());
            MapleAffectedArea affectedArea = new MapleAffectedArea(bounds, applyfrom, this);
            applyfrom.getMap().spawnAffectedArea(affectedArea, false);
        } else if (isTimeLeap()) {
            applyto.removeAllCooldownsExcept(Buccaneer.TIME_LEAP, true);
        }
        if (duration != -1 && ((sourceid >= 2022125 && sourceid <= 2022130) || sourceid == DarkKnight.HEX_OF_BEHOLDER)) {
            applyto.registerEffect(this, System.currentTimeMillis(), duration);
        }
        auraCheck(applyto);
        return true;
    }

    public void remove(MapleCharacter from, MapleCharacter applyto, boolean firstUse) {
        auraCheck(applyto);
        for (Pair<SecondaryStat, BuffDataHolder> p : getStatups()) {
            applyto.cancelEffectFromSecondaryStat(p.left);
        }
        if (firstUse) {
            removePartyBuff(applyto);
        }
    }

    private void auraCheck(MapleCharacter applyto) {
        int auras = 0, maxAuras = 2;
        for (PlayerBuffValueHolder buff : applyto.getAllBuffs()) {
            if (buff.sourceid == BattleMage.DarkAura || buff.sourceid == BattleMage.BlueAura || buff.sourceid == BattleMage.YellowAura) {
                auras++;
            }
        }
        if (auras > maxAuras) {
            PlayerBuffValueHolder aura = null;
            for (PlayerBuffValueHolder buff : applyto.getAllBuffs()) {
                if (buff.sourceid == BattleMage.DarkAura || buff.sourceid == BattleMage.BlueAura || buff.sourceid == BattleMage.YellowAura) {
                    Optional<Pair<SecondaryStat, BuffDataHolder>> check = getStatups().stream().findFirst();
                    if (check.get().right.getSourceID() != buff.sourceid) {
                        aura = buff;
                        break;
                    }
                }
            }
            if (aura != null) {
                applyto.cancelEffectFromSecondaryStat(aura.getEffect().getStatups().stream().findFirst().get().left);
            }
        }
    }

    private void applyPartyBuff(MapleCharacter applyfrom) {
        if (isPartyBuff() && (applyfrom.isInParty() || isGmBuff())) {
            Rectangle bounds = calculateBoundingBox(applyfrom.getPosition(), applyfrom.isFacingLeft());
            List<MapleMapObject> affecteds = applyfrom.getMap().getMapObjectsInRect(bounds, Arrays.asList(MapleMapObjectType.PLAYER));
            List<MapleCharacter> affectedp = new ArrayList<>(affecteds.size());
            int maxPlayers = this.getSourceId() == Evan.SOUL_STONE ? Randomizer.nextInt(getY()) + 1 : this.getSourceId() == Bishop.RESURRECTION ? 1 : 6;
            for (MapleMapObject affectedmo : affecteds) {
                MapleCharacter affected = (MapleCharacter) affectedmo;
                if (affected == null) {
                    continue;
                }
                if (applyfrom == null) {
                    continue;
                }
                if (affected.getId() != applyfrom.getId() && (isGmBuff() || (applyfrom.getPartyId() != -1 && applyfrom.getPartyId() == affected.getPartyId()))) {
                    if ((isResurrection() && !affected.isAlive()) || (!isResurrection() && affected.isAlive())) {
                        affectedp.add(affected);
                    }
                }
            }
            int totalAffected = 0;
            for (MapleCharacter affected : affectedp) {
                if (totalAffected++ >= maxPlayers) {
                    continue;
                }
                applyTo(applyfrom, affected, false, null);
                applyfrom.announce(EffectPacket.Local.SkillUse.encode(sourceid, applyfrom.getLevel(), skillLevel));
                applyfrom.getMap().announce(applyfrom, EffectPacket.Remote.SkillUse.encode(applyfrom.getId(), applyfrom.getLevel(), sourceid, skillLevel), false);
            }
        }
    }

    private void removePartyBuff(MapleCharacter from) {
        if (isPartyBuff() && (from.isInParty() || isGmBuff())) {
            Rectangle bounds = calculateBoundingBox(from.getPosition(), from.isFacingLeft());
            List<MapleMapObject> affecteds = from.getMap().getMapObjectsInRect(bounds, Arrays.asList(MapleMapObjectType.PLAYER));
            List<MapleCharacter> affectedp = new ArrayList<>(affecteds.size());
            for (MapleMapObject affectedmo : affecteds) {
                MapleCharacter affected = (MapleCharacter) affectedmo;
                if (affected == null) {
                    continue;
                }
                if (affected.getId() != from.getId() && (isGmBuff() || from.getParty().getId() == affected.getParty().getId())) {
                    if ((isResurrection() && !affected.isAlive()) || (!isResurrection() && affected.isAlive())) {
                        affectedp.add(affected);
                    }
                }
            }
            for (MapleCharacter affected : affectedp) {
                remove(from, affected, false);
            }
        }
    }

    private void applyMonsterBuff(MapleCharacter applyfrom) {
        Rectangle bounds = calculateBoundingBox(applyfrom.getPosition(), applyfrom.isFacingLeft());
        List<MapleMapObject> affected = applyfrom.getMap().getMapObjectsInRect(bounds, Arrays.asList(MapleMapObjectType.MONSTER));
        Skill skill_ = SkillFactory.getSkill(sourceid);
        int i = 0;
        for (MapleMapObject mo : affected) {
            i++;
            MapleMonster monster = (MapleMonster) mo;
            if (isDispel()) {
                if (i >= mobCount) {
                    continue;
                }
                monster.debuffMob(skill_.getId());
            } else {
                if (sourceid == Page.THREATEN) {// Custom, make threaten switch controller
                    MapleCharacter controller = monster.getController();
                    if (controller == null || controller.getId() != applyfrom.getId()) {
                        monster.switchController(applyfrom, true);
                    } else {
                        applyfrom.controlMonster(monster, true);
                        monster.setControllerHasAggro(true);
                        monster.setControllerKnowsAboutAggro(false);
                    }
                }
                if (i >= mobCount) {
                    continue;
                }
                if (makeChanceResult()) {
                    monster.applyStatus(applyfrom, getMonsterStati(), skill_, false, isPoison());
                    if (isCrash()) {
                        monster.debuffMob(skill_.getId());
                    }
                }
            }
            if (i >= mobCount && sourceid != Page.THREATEN) {
                break;
            }
        }
    }

    public Rectangle calculateBoundingBox(Point posFrom, boolean facingLeft) {
        Point mylt;
        Point myrb;
        if (facingLeft) {
            mylt = new Point(lt.x + posFrom.x, lt.y + posFrom.y);
            myrb = new Point(rb.x + posFrom.x, rb.y + posFrom.y);
        } else {
            myrb = new Point(-lt.x + posFrom.x, rb.y + posFrom.y);
            mylt = new Point(-rb.x + posFrom.x, lt.y + posFrom.y);
        }
        Rectangle bounds = new Rectangle(mylt.x, mylt.y, myrb.x - mylt.x, myrb.y - mylt.y);
        return bounds;
    }

    public void silentApplyBuff(MapleCharacter chr, long starttime, long duration) {
        silentApplyBuff(chr, starttime, duration, false);
    }

    public void silentApplyBuff(MapleCharacter chr, long starttime, long duration, boolean eliteEnhance) {
        this.applyBuffEffect(chr, chr, duration, starttime, false, eliteEnhance);
        SummonMovementType summonMovementType = getSummonMovementType();
        if (summonMovementType != null) {
            final MapleSummon tosummon = new MapleSummon(chr, sourceid, chr.getPosition(), summonMovementType);
            if (!tosummon.isStationary()) {
                chr.addSummon(sourceid, tosummon);
                tosummon.addHP(x);
            }
        }
    }

    public final void applyComboBuff(final MapleCharacter applyto, int combo) {
        final List<Pair<SecondaryStat, BuffDataHolder>> stat = Collections.singletonList(new Pair<>(SecondaryStat.ComboAbilityBuff, new BuffDataHolder(sourceid, 0, combo)));
        applyto.getClient().announce(WvsContext.setTemporaryStat(applyto, sourceid, 99999, stat));
        final long starttime = System.currentTimeMillis();
        applyto.registerEffect(this, starttime, -1);
    }

    public void applyBuffEffect(MapleCharacter chr, int duration) {
        applyBuffEffect(chr, chr, duration, false);
    }

    public void applyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, boolean primary) {
        applyBuffEffect(applyfrom, applyto, duration, primary);
    }

    public void applyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, int duration, boolean primary) {
        applyBuffEffect(applyfrom, applyto, duration, System.currentTimeMillis(), primary, true);
    }

    public void applyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, long duration, long starttime, boolean primary, boolean eliteEnhance) {
        if (!isMonsterRiding()) {
            applyto.cancelEffect(this, true, -1);
        }
        List<Pair<SecondaryStat, BuffDataHolder>> localstatups = statups;
        // Duration sent in packets, we reduce this later for times when you cc so on the players screen it visually expires on same time.
        int packetDuration = (int) duration;
        // Buff Duration is the duration used to check if the buff should expire server-side.
        // Since starttime never changes we can't re-use packetDuration and have to keep the original full duration of the buff.
        int buffDuration = packetDuration;
        // System.out.println("original: " + duration);
        int localsourceid = sourceid;
        int seconds = packetDuration / 1000;
        MapleMount givemount = null;
        if (isMonsterRiding()) {
            int ridingLevel = 0;
            Item mount = applyfrom.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -18);
            if (mount != null) {
                ridingLevel = mount.getItemId();
            }
            switch (sourceid) {
                case Corsair.BATTLE_SHIP:
                    ridingLevel = 1932000;
                    break;
                case Beginner.SPACESHIP:
                case Noblesse.SPACESHIP:
                    ridingLevel = 1932000 + applyto.getSkillLevel(sourceid);
                    break;
                case Beginner.YETI_MOUNT1:
                case Noblesse.YETI_MOUNT1:
                case Legend.YETI_MOUNT1:
                    ridingLevel = 1932003;
                    break;
                case Beginner.YETI_MOUNT2:
                case Noblesse.YETI_MOUNT2:
                case Legend.YETI_MOUNT2:
                    ridingLevel = 1932004;
                    break;
                case Beginner.WITCH_BROOMSTICK:
                case Noblesse.WITCH_BROOMSTICK:
                case Legend.WITCH_BROOMSTICK:
                    ridingLevel = 1932005;
                    break;
                case Beginner.BALROG_MOUNT:
                case Noblesse.BALROG_MOUNT:
                case Legend.BALROG_MOUNT:
                    ridingLevel = 1932010;
                    break;
                default:
                    if (applyto.getMount() == null) {
                        applyto.mount(ridingLevel, sourceid);
                    }
                    applyto.getMount().startSchedule();
                    break;
            }
            switch (sourceid) {
                case Corsair.BATTLE_SHIP:
                    givemount = new MapleMount(applyto, 1932000, sourceid);
                    break;
                case Beginner.SPACESHIP:
                case Noblesse.SPACESHIP:
                    givemount = new MapleMount(applyto, 1932000 + applyto.getSkillLevel(sourceid), sourceid);
                    break;
                case Beginner.YETI_MOUNT1:
                case Noblesse.YETI_MOUNT1:
                case Legend.YETI_MOUNT1:
                    givemount = new MapleMount(applyto, 1932003, sourceid);
                    break;
                case Beginner.YETI_MOUNT2:
                case Noblesse.YETI_MOUNT2:
                case Legend.YETI_MOUNT2:
                    givemount = new MapleMount(applyto, 1932004, sourceid);
                    break;
                case Beginner.WITCH_BROOMSTICK:
                case Noblesse.WITCH_BROOMSTICK:
                case Legend.WITCH_BROOMSTICK:
                    givemount = new MapleMount(applyto, 1932005, sourceid);
                    break;
                case Beginner.BALROG_MOUNT:
                case Noblesse.BALROG_MOUNT:
                case Legend.BALROG_MOUNT:
                    givemount = new MapleMount(applyto, 1932010, sourceid);
                    break;
                default:
                    givemount = applyto.getMount();
                    break;
            }
            packetDuration = sourceid;
            buffDuration = sourceid;
            localsourceid = ridingLevel;
            localstatups = Collections.singletonList(new Pair<>(SecondaryStat.RideVehicle, new BuffDataHolder(0, 0, 0)));// first 0 was sourceid
        } else if (isSkillMorph()) {
            localstatups = Collections.singletonList(new Pair<>(SecondaryStat.Morph, new BuffDataHolder(sourceid, 0, getMorph(applyto))));
        }
        if (primary) {
            packetDuration = alchemistModifyVal(applyfrom, packetDuration, false);
            buffDuration = alchemistModifyVal(applyfrom, buffDuration, false);
            applyto.getMap().announce(applyto, EffectPacket.Remote.SkillUse.encode(applyto.getId(), applyto.getLevel(), sourceid, skillLevel), false);
        }
        packetDuration -= (System.currentTimeMillis() - starttime);
        // System.out.println("buffDuration: " + packetDuration);
        if (packetDuration <= 0) {
            return;
        }
        if (!isDisease() && !SkillConstants.isEliteExempted(sourceid) && eliteEnhance) {
            if (sourceid != FPArchMage.BIG_BANG && sourceid != ILArchMage.BIG_BANG && sourceid != Bishop.BIG_BANG) {
                if (applyto.getClient().checkEliteStatus()) {
                    if (packetDuration * 2 > Integer.MAX_VALUE || packetDuration * 2 < 0) {
                        packetDuration = Integer.MAX_VALUE;
                        buffDuration = Integer.MAX_VALUE;
                    } else {
                        packetDuration *= 2;
                        buffDuration *= 2;
                    }
                    seconds = packetDuration / 1000;
                }
            } else {
                // localDuration = Math.abs(localDuration);
                // use to be -1000 causing it to be perm, Math.abs set it to 1000 but its too short for it to be at all useful.
                packetDuration = 5000;
                seconds = packetDuration / 1000;
            }
        }
        for (Pair<SecondaryStat, BuffDataHolder> p : localstatups) {
            switch (p.left) {
                case EnergyCharged: {
                    TemporaryStatBase pStat = applyto.secondaryStat.getTemporaryState(TSIndex.EnergyCharged.getIndex());
                    pStat.nOption = localsourceid;
                    pStat.rOption = packetDuration;
                    pStat.tLastUpdated = System.currentTimeMillis();
                    applyto.secondaryStat.setTemporaryState(TSIndex.EnergyCharged.getIndex(), pStat);
                    break;
                }
                case Dash_Speed: {
                    TwoStateTemporaryStat pStat = (TwoStateTemporaryStat) applyto.secondaryStat.getTemporaryState(TSIndex.DashSpeed.getIndex());
                    pStat.nOption = p.right.getValue();
                    pStat.rOption = localsourceid;
                    pStat.tLastUpdated = System.currentTimeMillis();
                    pStat.usExpireTerm = (int) System.currentTimeMillis();
                    applyto.secondaryStat.setTemporaryState(TSIndex.DashSpeed.getIndex(), pStat);
                    break;
                }
                case Dash_Jump: {
                    TwoStateTemporaryStat pStat = (TwoStateTemporaryStat) applyto.secondaryStat.getTemporaryState(TSIndex.DashJump.getIndex());
                    pStat.nOption = p.right.getValue();
                    pStat.rOption = localsourceid;
                    pStat.tLastUpdated = System.currentTimeMillis();
                    pStat.usExpireTerm = (int) System.currentTimeMillis();
                    applyto.secondaryStat.setTemporaryState(TSIndex.DashJump.getIndex(), pStat);
                    break;
                }
                case RideVehicle: {
                    // doesn't seem to work
                    TemporaryStatBase pStat = applyto.secondaryStat.getTemporaryState(TSIndex.RideVehicle.getIndex());
                    pStat.nOption = localsourceid;
                    pStat.rOption = packetDuration;
                    pStat.tLastUpdated = System.currentTimeMillis();
                    applyto.secondaryStat.setTemporaryState(TSIndex.RideVehicle.getIndex(), pStat);
                    break;
                }
                case PartyBooster: {
                    PartyBooster pStat = (PartyBooster) applyto.secondaryStat.getTemporaryState(TSIndex.PartyBooster.getIndex());
                    pStat.nOption = p.right.getValue();
                    pStat.rOption = localsourceid;
                    pStat.tLastUpdated = System.currentTimeMillis();
                    pStat.usExpireTerm = (int) System.currentTimeMillis();
                    applyto.secondaryStat.setTemporaryState(TSIndex.PartyBooster.getIndex(), pStat);
                    break;
                }
                case GuidedBullet: {
                    GuidedBullet pStat = (GuidedBullet) applyto.secondaryStat.getTemporaryState(TSIndex.GuidedBullet.getIndex());
                    pStat.nOption = p.right.getSourceID();
                    pStat.rOption = p.right.getSourceLevel();
                    pStat.tLastUpdated = System.currentTimeMillis();
                    applyto.secondaryStat.setTemporaryState(TSIndex.GuidedBullet.getIndex(), pStat);
                    break;
                }
                case Undead: {
                    TwoStateTemporaryStat pStat = (TwoStateTemporaryStat) applyto.secondaryStat.getTemporaryState(TSIndex.Undead.getIndex());
                    pStat.nOption = localsourceid;
                    pStat.rOption = packetDuration;
                    pStat.tLastUpdated = System.currentTimeMillis();
                    applyto.secondaryStat.setTemporaryState(TSIndex.Undead.getIndex(), pStat);
                    break;
                }
                case Morph:
                    // fix morph
                    break;
                default:
                    break;
            }
        }
        if (localstatups.size() > 0) {
            byte[] buff = null;
            byte[] mbuff = null;
            if (isDisease()) {
                buff = WvsContext.setTemporaryStat(applyto, (sourceLevel << 16 | sourceid), packetDuration, localstatups);
                mbuff = UserRemote.setTemporaryStat(applyto, localstatups);
            } else if (getSummonMovementType() == null) {
                buff = WvsContext.setTemporaryStat(applyto, (skill ? sourceid : -sourceid), packetDuration, localstatups);
            }
            if (isDash()) {
                buff = WvsContext.setTemporaryStat(applyto, localsourceid, packetDuration, localstatups);
                mbuff = UserRemote.setTemporaryPirateStat(applyto.getId(), sourceid, seconds, localstatups);
            } else if (isInfusion()) {
                buff = WvsContext.setTemporaryStat(applyto, localsourceid, packetDuration, localstatups);
                mbuff = UserRemote.setTemporaryPirateStat(applyto.getId(), sourceid, seconds, localstatups);
            } else if (isWindWalk()) {
                List<Pair<SecondaryStat, BuffDataHolder>> wwstat = Collections.singletonList(new Pair<>(SecondaryStat.WindWalk, new BuffDataHolder(0, 0, 0)));
                mbuff = UserRemote.setTemporaryStat(applyto, wwstat);
            } else if (isDs()) {
                List<Pair<SecondaryStat, BuffDataHolder>> dsstat = Collections.singletonList(new Pair<>(SecondaryStat.DarkSight, new BuffDataHolder(0, 0, 0)));
                mbuff = UserRemote.setTemporaryStat(applyto, dsstat);
            } else if (isCombo()) {
                mbuff = UserRemote.setTemporaryStat(applyto, statups);
            } else if (isMonsterRiding()) {
                applyto.getMount().setItemId(givemount.getItemId());
                applyto.getMount().setActive(true);
                applyto.getMount().setExp(givemount.getExp());
                applyto.getMount().setLevel(givemount.getLevel());
                applyto.getMount().setTiredness(givemount.getTiredness());
                buff = WvsContext.setTemporaryStat(applyto, localsourceid, packetDuration, localstatups);
                mbuff = UserRemote.showMonsterRiding(applyto.getId(), applyto.getMount());
                packetDuration = (int) duration;
                if (sourceid == Corsair.BATTLE_SHIP) {// hp
                    if (applyto.getBattleshipHp() == 0) {
                        applyto.resetBattleshipHp();
                    }
                }
            } else if (isShadowPartner()) {
                List<Pair<SecondaryStat, BuffDataHolder>> stat = Collections.singletonList(new Pair<>(SecondaryStat.ShadowPartner, new BuffDataHolder(0, 0, 0)));
                mbuff = UserRemote.setTemporaryStat(applyto, stat);
            } else if (isSoulArrow()) {
                List<Pair<SecondaryStat, BuffDataHolder>> stat = Collections.singletonList(new Pair<>(SecondaryStat.SoulArrow, new BuffDataHolder(0, 0, 0)));
                mbuff = UserRemote.setTemporaryStat(applyto, stat);
            } else if (isEnrage()) {
                applyto.handleOrbconsume();
            } else if (isMorph()) {
                List<Pair<SecondaryStat, BuffDataHolder>> stat = Collections.singletonList(new Pair<>(SecondaryStat.Morph, new BuffDataHolder(sourceid, 0, getMorph(applyto))));
                mbuff = UserRemote.setTemporaryStat(applyto, stat);
            }
            // System.out.println("registering duration: " + buffDuration);
            applyto.registerEffect(this, starttime, buffDuration);
            if (buff != null) {
                if (!hasNoIcon()) { // Thanks flav for such a simple release! :)
                    applyto.getClient().announce(buff);
                }
            }
            if (mbuff != null) {
                applyto.getMap().announce(applyto, mbuff, false);
            }
            if (sourceid == Corsair.BATTLE_SHIP) {
                applyto.announce(PacketHelper.skillCooldown(5221999, applyto.getBattleshipHp() / 10));
            }
        }
    }

    private int calcHPChange(MapleCharacter applyfrom, boolean firstApply) {
        int hpchange = 0;
        if (hp != 0) {
            if (!skill) {
                if (firstApply) {
                    hpchange += alchemistModifyVal(applyfrom, hp, true);
                } else {
                    hpchange += hp;
                }
            } else {
                hpchange += makeHealHP(hp / 100.0, applyfrom.getTotalMagic(), 3, 5);
            }
        }
        if (hpR != 0) {
            hpchange += (int) (applyfrom.getCurrentMaxHp() * hpR);
        }
        if (firstApply) {
            if (hpCon != 0) {
                hpchange -= hpCon;
            }
        }
        if (isChakra()) {
            hpchange += makeHealHP(getY() / 100.0, applyfrom.getTotalLuk(), 2.3, 3.5);
        } else if (sourceid == SuperGM.HealAndDispel) {
            hpchange += (applyfrom.getMaxHp() - applyfrom.getHp());
        }
        return hpchange;
    }

    private int makeHealHP(double rate, double stat, double lowerfactor, double upperfactor) {
        return (int) ((Math.random() * ((int) (stat * upperfactor * rate) - (int) (stat * lowerfactor * rate) + 1)) + (int) (stat * lowerfactor * rate));
    }

    private int calcMPChange(MapleCharacter applyfrom, boolean firstApply) {
        int mpchange = 0;
        if (mp != 0) {
            if (firstApply) {
                mpchange += alchemistModifyVal(applyfrom, mp, true);
            } else {
                mpchange += mp;
            }
        }
        if (mpR != 0) {
            mpchange += (int) (applyfrom.getCurrentMaxMp() * mpR);
        }
        if (firstApply) {
            if (mpCon != 0) {
                double mod = 1.0;
                boolean isAFpMage = applyfrom.getJob().isA(MapleJob.FP_MAGE);
                boolean isCygnus = applyfrom.getJob().isA(MapleJob.BLAZEWIZARD2);
                boolean isEvan = applyfrom.getJob().isA(MapleJob.EVAN7);
                if (isAFpMage || isCygnus || isEvan || applyfrom.getJob().isA(MapleJob.IL_MAGE)) {
                    Skill amp = isAFpMage ? SkillFactory.getSkill(FPMage.ELEMENT_AMPLIFICATION) : (isCygnus ? SkillFactory.getSkill(BlazeWizard.ELEMENT_AMPLIFICATION) : (isEvan ? SkillFactory.getSkill(Evan.MAGIC_AMPLIFICATION) : SkillFactory.getSkill(ILMage.ELEMENT_AMPLIFICATION)));
                    int ampLevel = applyfrom.getSkillLevel(amp);
                    if (ampLevel > 0) {
                        mod = amp.getEffect(ampLevel).getX() / 100.0;
                    }
                }
                mpchange -= mpCon * mod;
                if (applyfrom.getBuffedValue(SecondaryStat.Infinity) != null) {
                    mpchange = 0;
                } else if (applyfrom.getBuffedValue(SecondaryStat.Concentration) != null) {
                    mpchange -= (int) (mpchange * (applyfrom.getBuffedValue(SecondaryStat.Concentration).doubleValue() / 100));
                }
            }
        }
        if (sourceid == SuperGM.HealAndDispel) {
            mpchange += (applyfrom.getMaxMp() - applyfrom.getMp());
        }
        return mpchange;
    }

    private int alchemistModifyVal(MapleCharacter chr, int val, boolean withX) {
        if (!skill && (chr.getJob().isA(MapleJob.HERMIT) || chr.getJob().isA(MapleJob.NIGHTWALKER3))) {
            MapleStatEffect alchemistEffect = getAlchemistEffect(chr);
            if (alchemistEffect != null) {
                return (int) (val * ((withX ? alchemistEffect.getX() : alchemistEffect.getY()) / 100.0));
            }
        }
        return val;
    }

    private MapleStatEffect getAlchemistEffect(MapleCharacter chr) {
        int id = Hermit.ALCHEMIST;
        if (chr.isCygnus()) {
            id = NightWalker.ALCHEMIST;
        }
        int alchemistLevel = chr.getSkillLevel(SkillFactory.getSkill(id));
        return alchemistLevel == 0 ? null : SkillFactory.getSkill(id).getEffect(alchemistLevel);
    }

    private boolean isGmBuff() {
        switch (sourceid) {
            case Beginner.EchoOfHero:
            case Noblesse.EchoOfHero:
            case Legend.EchoOfHero:
            case Evan.EchoOfHero:
            case SuperGM.HealAndDispel:
            case SuperGM.Haste:
            case SuperGM.HolySymbol:
            case SuperGM.Bless:
            case SuperGM.Resurrection:
            case SuperGM.HyperBody:
                return true;
            default:
                return false;
        }
    }

    private boolean isMonsterBuff() {
        if (!skill) {
            return false;
        }
        switch (sourceid) {
            case Page.THREATEN:
            case FPWizard.SLOW:
            case ILWizard.SLOW:
            case FPMage.SEAL:
            case ILMage.SEAL:
            case Priest.DOOM:
            case Hermit.SHADOW_WEB:
            case NightLord.NINJA_AMBUSH:
            case Shadower.NINJA_AMBUSH:
            case BlazeWizard.SLOW:
            case BlazeWizard.SEAL:
            case NightWalker.SHADOW_WEB:
            case Crusader.ARMOR_CRASH:
            case DragonKnight.POWER_CRASH:
            case WhiteKnight.MAGIC_CRASH:
            case Priest.DISPEL:
            case SuperGM.HealAndDispel:
                return true;
        }
        return false;
    }

    private boolean isPartyBuff() {
        if (lt == null || rb == null) {
            return false;
        }
        if ((sourceid >= 1211003 && sourceid <= 1211008) || sourceid == Paladin.SWORD_HOLY_CHARGE || sourceid == Paladin.BW_HOLY_CHARGE || sourceid == DawnWarrior.SOUL_CHARGE) {// wk charges have lt and rb set but are neither player nor monster buffs
            return false;
        }
        return true;
    }

    private boolean isHeal() {
        return sourceid == Cleric.Heal || sourceid == SuperGM.HealAndDispel;
    }

    private boolean isResurrection() {
        return sourceid == Bishop.RESURRECTION
                || sourceid == Evan.SOUL_STONE
                || sourceid == SuperGM.Resurrection;
    }

    private boolean isTimeLeap() {
        return sourceid == Buccaneer.TIME_LEAP;
    }

    public boolean isDragonBlood() {
        return skill && sourceid == DragonKnight.DRAGON_BLOOD;
    }

    public boolean isBerserk() {
        return skill && sourceid == DarkKnight.BERSERK;
    }

    public boolean isRecovery() {
        return sourceid == Beginner.RECOVERY || sourceid == Noblesse.RECOVERY || sourceid == Legend.RECOVERY;
    }

    private boolean isWindWalk() {
        return sourceid == WindArcher.WIND_WALK;
    }

    private boolean isDs() {
        return skill && (sourceid == Rogue.DARK_SIGHT || sourceid == NightWalker.DARK_SIGHT);
    }

    private boolean isCombo() {
        return skill && (sourceid == Crusader.COMBO || sourceid == DawnWarrior.COMBO);
    }

    private boolean isEnrage() {
        return skill && sourceid == Hero.ENRAGE;
    }

    public boolean isBeholder() {
        return skill && sourceid == DarkKnight.BEHOLDER;
    }

    private boolean isShadowPartner() {
        return skill && (sourceid == Hermit.SHADOW_PARTNER || sourceid == NightWalker.SHADOW_PARTNER);
    }

    private boolean isChakra() {
        return skill && sourceid == ChiefBandit.CHAKRA;
    }

    public boolean isMonsterRiding() {
        return skill && (sourceid % 10000000 == 1004 || sourceid % 10000000 == 11004/*
                 Evan
                 */ || sourceid == Corsair.BATTLE_SHIP || sourceid == Beginner.SPACESHIP || sourceid == Noblesse.SPACESHIP || sourceid == Beginner.YETI_MOUNT1 || sourceid == Beginner.YETI_MOUNT2 || sourceid == Beginner.WITCH_BROOMSTICK || sourceid == Beginner.BALROG_MOUNT || sourceid == Noblesse.YETI_MOUNT1 || sourceid == Noblesse.YETI_MOUNT2 || sourceid == Noblesse.WITCH_BROOMSTICK || sourceid == Noblesse.BALROG_MOUNT || sourceid == Legend.YETI_MOUNT1 || sourceid == Legend.YETI_MOUNT2 || sourceid == Legend.WITCH_BROOMSTICK || sourceid == Legend.BALROG_MOUNT);
    }

    public boolean isMagicDoor() {
        return skill && sourceid == Priest.MysticDoor;
    }

    public boolean isPoison() {
        return skill
                && (sourceid == FPMage.POISON_MIST
                || sourceid == FPWizard.POISON_BREATH
                || sourceid == FPMage.ELEMENT_COMPOSITION
                || sourceid == NightWalker.POISON_BOMB
                || sourceid == BlazeWizard.FLAME_GEAR);
    }

    public boolean isMorph() {
        return morphId > 0;
    }

    public boolean isMorphWithoutAttack() {
        return morphId > 0 && morphId < 100; // Every morph item I have found has been under 100, pirate skill transforms start at 1000.
    }

    private boolean isAffectedArea() {
        return skill
                && (sourceid == FPMage.POISON_MIST
                || sourceid == BlazeWizard.FLAME_GEAR
                || sourceid == NightWalker.POISON_BOMB
                || sourceid == Evan.RecoveryAura
                || sourceid == Shadower.SmokeScreen
                || sourceid == BattleMage.PartyShield);
    }

    private boolean isSoulArrow() {
        return skill
                && (sourceid == Hunter.SOUL_ARROW
                || sourceid == Crossbowman.SOUL_ARROW
                || sourceid == WindArcher.SOUL_ARROW);
    }

    private boolean isShadowClaw() {
        return skill && sourceid == NightLord.ShadowStars;
    }

    private boolean isCrash() {
        return skill
                && (sourceid == DragonKnight.POWER_CRASH
                || sourceid == Crusader.ARMOR_CRASH
                || sourceid == WhiteKnight.MAGIC_CRASH);
    }

    private boolean isDispel() {
        return skill
                && (sourceid == Priest.DISPEL
                || sourceid == SuperGM.HealAndDispel);
    }

    private boolean isHeroWill() {
        if (skill) {
            switch (sourceid) {
                case Hero.HEROS_WILL:
                case Paladin.HEROS_WILL:
                case DarkKnight.HEROS_WILL:
                case FPArchMage.HEROS_WILL:
                case ILArchMage.HEROS_WILL:
                case Bishop.HEROS_WILL:
                case Bowmaster.HEROS_WILL:
                case Marksman.HEROS_WILL:
                case NightLord.HEROS_WILL:
                case Shadower.HEROS_WILL:
                case Buccaneer.PIRATES_RAGE:
                case Aran.HEROS_WILL:
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

    private boolean isDash() {
        return skill
                && (sourceid == Pirate.DASH
                || sourceid == ThunderBreaker.DASH
                || sourceid == Beginner.SPACE_DASH
                || sourceid == Noblesse.SPACE_DASH);
    }

    private boolean isSkillMorph() {
        return skill
                && (sourceid == Buccaneer.SUPER_TRANSFORMATION
                || sourceid == Marauder.TRANSFORMATION
                || sourceid == WindArcher.EAGLE_EYE
                || sourceid == ThunderBreaker.TRANSFORMATION);
    }

    private boolean isInfusion() {
        return skill
                && (sourceid == Buccaneer.SPEED_INFUSION
                || sourceid == Corsair.SPEED_INFUSION
                || sourceid == ThunderBreaker.SPEED_INFUSION);
    }

    private boolean isCygnusFA() {
        return skill
                && (sourceid == DawnWarrior.FINAL_ATTACK
                || sourceid == WindArcher.FINAL_ATTACK);
    }

    private boolean isComboReset() {
        return sourceid == Aran.COMBO_BARRIER || sourceid == Aran.COMBO_DRAIN;
    }

    private int getFatigue() {
        return fatigue;
    }

    public int getMorph() {
        return morphId;
    }

    private int getMorph(MapleCharacter chr) {
        if (morphId >= 1000) {
            return morphId + (100 * chr.getGender());
        }
        if (morphId % 10 == 0) {
            return morphId + chr.getGender();
        }
        return morphId + (100 * chr.getGender());
    }

    private SummonMovementType getSummonMovementType() {
        if (!skill) {
            return null;
        }
        switch (sourceid) {
            case Ranger.PUPPET:
            case Sniper.PUPPET:
            case WindArcher.PUPPET:
            case Outlaw.OCTOPUS:
            case Corsair.WRATH_OF_THE_OCTOPI:
            case BladeMaster.MIRRORED_TARGET:
                // case WildHunter.MineDummySummoned:
                // case WildHunter.Trap:
                // case Mechanic.TeslaCoil:
                // case Mechanic.VelocityControler:
                // case Mechanic.HealingRobot_H_LX:
                // case Mechanic.SG88:
                // case Mechanic.AR01:
                // case Mechanic.RoboRoboDummy:
                return SummonMovementType.STATIONARY;
            case Ranger.SILVER_HAWK:
            case Sniper.GOLDEN_EAGLE:
            case Priest.SUMMON_DRAGON:
            case Marksman.FROST_PREY:
            case Bowmaster.PHOENIX:
            case Outlaw.GAVIOTA:
                return SummonMovementType.CIRCLE_FOLLOW;
            case DarkKnight.BEHOLDER:
            case FPArchMage.ELQUINES:
            case ILArchMage.IFRIT:
            case Bishop.BAHAMUT:
            case DawnWarrior.SOUL:
            case BlazeWizard.FLAME:
            case BlazeWizard.IFRIT:
            case WindArcher.STORM:
            case NightWalker.DARKNESS:
            case ThunderBreaker.LIGHTNING:
                // case Mechanic.Satelite:
                // case Mechanic.Satelite2:
                // case Mechanic.Satelite3:
                // case Mechanic.RoboRobo:
                return SummonMovementType.FOLLOW;
            // case Valkyrie.Gabiota:
            // return SummonMovementType.FLY_RANDOM;
            //
            // case BMage.Revive:
            // return SummonMovementType.WALK_RANDOM;
            //
        }
        return null;
    }

    public boolean hasNoIcon() {
        return hasNoIcon(sourceid);
    }

    public static boolean hasNoIcon(int sourceid) {
        return (sourceid == 3111002 || sourceid == 3211002 || + // puppet, puppet
                sourceid == 3211005 || sourceid == 2311002 || + // golden eagle, mystic door
                sourceid == 2121005 || sourceid == 2221005 || + // elquines, ifrit
                sourceid == 2321003 || sourceid == 3121006 || + // bahamut, phoenix
                sourceid == 3221005 || sourceid == 3111005 || + // frostprey, silver hawk
                sourceid == 2311006 || sourceid == 5220002 || + // summon dragon, wrath of the octopi
                sourceid == 5211001 || sourceid == 5211002 || +sourceid == BladeMaster.MIRRORED_TARGET); // octopus, gaviota
    }

    public boolean isSkill() {
        return skill;
    }

    public int getSourceId() {
        return sourceid;
    }

    public int getSourceLevel() {
        return sourceLevel;
    }

    public boolean makeChanceResult() {
        return prop == 1.0 || Math.random() < prop;
    }

    public boolean isDisease() {
        return disease;
    }

    public short getHp() {
        return hp;
    }

    public short getMp() {
        return mp;
    }

    public short getHpCon() {
        return hpCon;
    }

    public short getMpCon() {
        return mpCon;
    }

    public short getMatk() {
        return matk;
    }

    public short getWatk() {
        return watk;
    }

    public int getDuration() {
        return duration;
    }

    public List<Pair<SecondaryStat, BuffDataHolder>> getStatups() {
        return statups;
    }

    public boolean sameSource(MapleStatEffect effect) {
        return this.sourceid == effect.sourceid && this.skill == effect.skill;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getDamage() {
        return damage;
    }

    public int getAttackCount() {
        return attackCount;
    }

    public int getMobCount() {
        return mobCount;
    }

    public int getFixDamage() {
        return fixdamage;
    }

    public byte getBulletCount() {
        return bulletCount;
    }

    public byte getBulletConsume() {
        return bulletConsume;
    }

    public int getMoneyCon() {
        return moneyCon;
    }

    public int getCooldown() {
        return cooldown;
    }

    /**
     @return If > 0 buff gives bonus item drop rate.
     */
    public int getItemupbyitem() {
        return itemupbyitem;
    }

    /**
     @return If > 0 buff gives bonus meso rate.
     */
    public int getMesoupbyitem() {
        return mesoupbyitem;
    }

    /**
     Used to get Meso & Drop bonus from {@link getItemupbyitem()} and
     {@link getMesoupbyitem()}

     @return Meso & Drop bonus, EG: 30 = 1.30x
     */
    public int getProb() {
        return prob;
    }

    public Map<MobStat, MobStatData> getMonsterStati() {
        return monsterStatus;
    }

    public Integer getSkilLevel() {
        return skillLevel;
    }

    public short getWdef() {
        return wdef;
    }

    public short getMdef() {
        return mdef;
    }

    public short getAcc() {
        return acc;
    }

    public short getAvoid() {
        return avoid;
    }
}
