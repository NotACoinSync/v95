package server.maps;

import client.*;
import client.enums.AdminResult;
import client.inventory.*;
import client.player.SecondaryStat;
import constants.FeatureSettings;
import constants.GameConstants;
import constants.ItemConstants;
import constants.MobConstants;
import java.awt.Point;
import java.awt.Rectangle;
import java.rmi.RemoteException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import net.channel.ChannelServer;
import net.server.channel.Channel;
import scripting.event.EventManager;
import scripting.map.MapScriptManager;
import server.ItemInformationProvider;
import server.MaplePortal;
import server.MapleStatEffect;
import server.TimerManager;
import server.events.gm.*;
import server.life.*;
import server.life.MapleLifeFactory.selfDestruction;
import server.life.MapleMonster.AttackerEntry;
import server.life.MapleMonster.AttackingMapleCharacter;
import server.maps.objects.*;
import server.partyquest.Pyramid;
import server.quest.MapleQuest;
import server.reactors.MapleReactor;
import server.reactors.ReactorHitInfo;
import server.reactors.ReactorHitType;
import tools.Pair;
import tools.Randomizer;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CField.Clock;
import tools.packets.CField.Coconut;
import tools.packets.CField.ContiMove;
import tools.packets.CField.DropPool;
import tools.packets.CField.MapLoadable;
import tools.packets.CField.MobPool;
import tools.packets.CField.NpcPool;
import tools.packets.CField.ReactorPool;
import tools.packets.CField.SnowBall;
import tools.packets.CField.SummonedPool;
import tools.packets.CField.TownPortalPool;
import tools.packets.CField.userpool.UserCommon;
import tools.packets.CField.userpool.UserRemote;
import tools.packets.DragonPacket;
import tools.packets.EffectPacket;
import tools.packets.Field;
import tools.packets.PacketHelper;
import tools.packets.PetPacket;
import tools.packets.UserPool;
import tools.packets.WvsContext;

public class MapleMap {

    private final Random rand = new Random();
    private static final int RANGE_DISTANCE = 2000000;
    private static final List<MapleMapObjectType> rangedMapobjectTypes = Arrays.asList(MapleMapObjectType.SHOP, MapleMapObjectType.ITEM, MapleMapObjectType.NPC, MapleMapObjectType.MONSTER, MapleMapObjectType.SUMMON, MapleMapObjectType.REACTOR, MapleMapObjectType.DRAGON, MapleMapObjectType.DOOR);
    private ConcurrentHashMap<Integer, MapleMapObject> mapobjects = new ConcurrentHashMap<>(8, 0.75f, 8);
    private Collection<SpawnPoint> monsterSpawn = Collections.synchronizedList(new LinkedList<SpawnPoint>());
    private Collection<SpawnPoint> customMonsterSpawn = Collections.synchronizedList(new LinkedList<SpawnPoint>());
    private AtomicInteger spawnedMonstersOnMap = new AtomicInteger(0);
    private ConcurrentLinkedQueue<MapleCharacter> characters = new ConcurrentLinkedQueue<>();
    private Map<Byte, MaplePortal> portals = new HashMap<>();
    private int mapid;
    private AtomicInteger runningOid = new AtomicInteger(10000000);
    private int channel;
    private boolean docked;
    private boolean tag = false;
    private MapleMapEffect mapEffect = null;
    private MapleOxQuiz ox;
    private boolean isOxQuiz = false;
    private boolean dropsOn = true;
    private ScheduledFuture<?> mapMonitor = null;
    private boolean allowSummons = true; // All maps should have this true at the beginning
    private long timeLimit;
    // Pink Bean
    private int PBStatus = 0;
    private Collection<MapleMonster> pinkbeanDeadMobs = null;
    // Horntail
    private Collection<MapleMonster> horntailDeadMobs = null;
    // Zakum
    private Collection<MapleMonster> zakumDeadMobs = null;
    // HPQ
    private int riceCakes = 0;
    private int bunnyDamage = 0;
    private long lastBunnyHit = 0;
    private short bunnyHitCombo = 0;
    // HolidayPQ
    private int snowmanDamage = 0;
    // events
    private boolean eventstarted = false, isMuted = false, isStunned = false, isSeduced = false;
    private MapleSnowball snowball0 = null;
    private MapleSnowball snowball1 = null;
    private MapleCoconut coconut;
    // Clockety
    private int clockHours;
    private int clockMinutes;
    private int clockSeconds;
    private ScheduledFuture<?> clockScheduler, clockTimer;
    // Instance map
    private UUID instanceID;
    //
    private Set<String> lastPlayers;
    private final MapleMapData mapData;
    // timers
    private Map<Integer, ScheduledFuture<?>> playerTimers = new HashMap<>();
    //
    private List<Integer> allowedMonsters = new ArrayList<>();
    //
    private long lastDropCheckTime = System.currentTimeMillis(), lastRespawnTime = System.currentTimeMillis(), lastAreaAffectedTime = System.currentTimeMillis();

    public MapleMap(MapleMapData mapData, int mapId) {
        this.mapData = mapData;
        this.mapid = mapId;
        lastPlayers = new HashSet<>();
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public void toggleDrops() {
        this.dropsOn = !dropsOn;
    }

    public List<MapleMapObject> getMapObjectsInRect(Rectangle box, List<MapleMapObjectType> types) {
        final List<MapleMapObject> ret = new LinkedList<>();
        for (MapleMapObject l : mapobjects.values()) {
            if (types.contains(l.getType())) {
                if (box.contains(l.getPosition())) {
                    ret.add(l);
                }
            }
        }
        return ret;
    }

    public int getId() {
        return mapid;
    }

    public MapleMap getPartyMap(int partyMapId) {
        return ChannelServer.getInstance().getChannel(channel).getMap(partyMapId);
    }

    public MapleMap getReturnMap() {
        return ChannelServer.getInstance().getChannel(channel).getMap(getMapData().getReturnMap());
    }

    public void setReactorState() {
        for (MapleMapObject object : mapobjects.values()) {
            if (object.getType() == MapleMapObjectType.REACTOR) {
                if (((MapleReactor) object).getCurrStateAsByte() < 1) {
                    ((MapleReactor) object).setState(1);
                }
            }
        }
    }

    public MapleMap getForcedReturnMap() {
        return ChannelServer.getInstance().getChannel(channel).getMap(getMapData().getForcedReturnMap());
    }

    public int getCurrentPartyId() {
        for (MapleCharacter player : this.getCharacters()) {
            if (player.getPartyId() != -1) {
                return player.getPartyId();
            }
        }
        return -1;
    }

    public void addMapObject(MapleMapObject mapObject) {
        int curOID = getUsableOID();
        mapObject.setObjectId(curOID);
        this.mapobjects.put(curOID, mapObject);
    }

    private void spawnAndAddRangedMapObject(MapleMapObject mapObject, DelayedPacketCreation packetBakery) {
        spawnAndAddRangedMapObject(mapObject, packetBakery, null);
    }

    private void spawnAndAddRangedMapObject(MapleMapObject mapObject, DelayedPacketCreation packetBakery, SpawnCondition condition) {
        spawnAndAddRangedMapObject(mapObject, packetBakery, condition, null);
    }

    private void spawnAndAddRangedMapObject(MapleCharacter player, MapleMapObject mapObject, DelayedPacketCreation packetBakery, SpawnCondition condition) {
        int curOID = getUsableOID();
        mapObject.setObjectId(curOID);
        mapObject.setVisibleTo(player.getId());
        this.mapobjects.put(curOID, mapObject);
        spawnAndAddRangedMapObject(player, mapObject, packetBakery, condition, null);
    }

    private void spawnAndAddRangedMapObject(MapleMapObject mapObject, DelayedPacketCreation packetBakery, SpawnCondition condition, MapleCharacter spawner) {
        int curOID = getUsableOID();
        mapObject.setObjectId(curOID);
        this.mapobjects.put(curOID, mapObject);
        for (MapleCharacter player : characters) {
            spawnAndAddRangedMapObject(player, mapObject, packetBakery, condition, spawner);
        }
    }

    private void spawnAndAddRangedMapObject(MapleCharacter player, MapleMapObject mapobject, DelayedPacketCreation packetbakery, SpawnCondition condition, MapleCharacter spawner) {
        if (condition == null || condition.canSpawn(player)) {
            if (spawner != null) {
                if (spawner.getId() == player.getId()) {
                    return;
                }
            }
            if (player.getPosition().distanceSq(mapobject.getPosition()) <= RANGE_DISTANCE) {
                packetbakery.sendPackets(player.getClient());
                player.addVisibleMapObject(mapobject);
            }
        }
    }

    private int getUsableOID() {
        if (runningOid.incrementAndGet() > 2000000000) {
            runningOid.set(10000000);
        }
        if (mapobjects.containsKey(runningOid.get())) {
            while (mapobjects.containsKey(runningOid.incrementAndGet()));
        }
        return runningOid.get();
    }

    public void removeMapObject(int objectID) {
        this.mapobjects.remove(objectID);
    }

    public void removeMapObject(final MapleMapObject obj) {
        removeMapObject(obj.getObjectId());
    }

    public Point calcPointBelow(Point initial) {
        MapleFoothold fh = getMapData().getFootholds().findBelow(initial);
        if (fh == null) {
            return null;
        }
        int dropY = fh.getY1();
        if (!fh.isWall() && fh.getY1() != fh.getY2()) {
            double s1 = Math.abs(fh.getY2() - fh.getY1());
            double s2 = Math.abs(fh.getX2() - fh.getX1());
            double s5 = Math.cos(Math.atan(s2 / s1)) * (Math.abs(initial.x - fh.getX1()) / Math.cos(Math.atan(s1 / s2)));
            if (fh.getY2() < fh.getY1()) {
                dropY = fh.getY1() - (int) s5;
            } else {
                dropY = fh.getY1() + (int) s5;
            }
        }
        return new Point(initial.x, dropY);
    }

    public void update() {
        long updateTime = System.currentTimeMillis();
        if (updateTime - lastDropCheckTime >= 5000L) {
            // System.out.println("drop test");
            List<MapleMapObject> delete = new ArrayList<>();
            for (MapleMapObject mmo : this.mapobjects.values()) {
                if (mmo.getType().equals(MapleMapObjectType.ITEM)) {
                    MapleMapItem mapitem = (MapleMapItem) mmo;
                    if ((mapitem.getDropTime() + mapitem.getDeleteTime()) <= updateTime) {
                        mapitem.itemLock.lock();
                        try {
                            if (mapitem.isPickedUp()) {
                                return;
                            }
                            MapleMap.this.announce(DropPool.LeaveField(mapitem.getObjectId(), 0, 0), mapitem.getPosition());
                            mapitem.setPickedUp(true);
                            if (mapitem.getItem() != null && mapitem.getItem().nSN >= 0 && mapitem.getItem().hasDBFlag(ItemDB.DELETE)) {
                                ItemFactory.deleteItem(mapitem.getItem());
                            }
                        } finally {
                            mapitem.itemLock.unlock();
                            delete.add(mmo);
                        }
                    }
                } else if (mmo.getType().equals(MapleMapObjectType.MONSTER)) {
                    MapleMonster monster = (MapleMonster) mmo;
                    monster.update();
                }
            }
            if (!delete.isEmpty()) {
                delete.forEach(mmo -> removeMapObject(mmo));
                delete.clear();
            }
            lastDropCheckTime = updateTime;
        }
        updateAffectedArea(updateTime);
        if (updateTime - lastRespawnTime >= 10000L) {
            respawn();
            lastRespawnTime = updateTime;
        }
    }

    public void updateAffectedArea(long updateTime) {
        if (updateTime - lastAreaAffectedTime >= 2500) {
            // System.out.println("updateAffectedArea " + (lastAreaAffectedTime - updateTime));
            List<MapleMapObject> remove = null;
            for (MapleMapObject mmo : this.mapobjects.values()) {
                if (mmo.getType().equals(MapleMapObjectType.AFFECTED_AREA)) {
                    MapleAffectedArea affectedArea = (MapleAffectedArea) mmo;
                    if (updateTime > affectedArea.createTime + affectedArea.duration) {
                        if (remove == null) {
                            remove = new ArrayList<>();
                        }
                        remove.add(mmo);
                        continue;
                    }
                    MapleCharacter owner = null;
                    if (affectedArea.ownerid != -1) {
                        owner = this.getCharacterById(affectedArea.ownerid);
                        if (owner == null) {
                            if (remove == null) {
                                remove = new ArrayList<>();
                            }
                            remove.add(mmo);
                            continue;
                        }
                    }
                    if (affectedArea.isPoisonAffectedArea() && !affectedArea.isMobAffectedArea()) {
                        List<MapleMapObject> affectedMonsters = getMapObjectsInBox(affectedArea.getBox(), Collections.singletonList(MapleMapObjectType.MONSTER));
                        for (MapleMapObject mo : affectedMonsters) {
                            if (affectedArea.makeChanceResult()) {
                                MobStatData data = new MobStatData(MobStat.Poison, 1, affectedArea.getSourceSkill().getId(), affectedArea.duration);
                                ((MapleMonster) mo).applyStatus(owner, data, affectedArea.getSourceSkill(), true, false);
                            }
                        }
                    } else if (affectedArea.isRecoveryAffectedArea()) {
                        List<MapleMapObject> players = getMapObjectsInBox(affectedArea.getBox(), Collections.singletonList(MapleMapObjectType.PLAYER));
                        for (MapleMapObject mo : players) {
                            if (affectedArea.makeChanceResult()) {
                                MapleCharacter player = (MapleCharacter) mo;
                                if (affectedArea.ownerid == player.getId() || owner.isInParty() && owner.getParty().containsMembers(player.getMPC())) {
                                    player.addMP((int) ((int) affectedArea.getSourceSkill().getEffect(owner.getSkillLevel(affectedArea.getSourceSkill().getId())).getX() * player.getMp() / 100D));
                                }
                            }
                        }
                    } else if (affectedArea.isSmokeAffectedArea()) {
                        List<MapleMapObject> players = getMapObjectsInBox(affectedArea.getBox(), Collections.singletonList(MapleMapObjectType.PLAYER));
                        for (MapleMapObject mo : players) {
                            if (affectedArea.makeChanceResult()) {
                                MapleCharacter player = (MapleCharacter) mo;
                                if (affectedArea.ownerid == player.getId() || owner.isInParty() && owner.getParty().containsMembers(player.getMPC())) {
                                    double critPercentage = affectedArea.getSourceSkill().getEffect(owner.getSkillLevel(affectedArea.getSourceSkill().getId())).getX() / 100D;
                                    player.playerStat.addCritDamage(critPercentage);
                                    // TODO: Damage Taken in Proportion to HP
                                }
                            }
                        }
                    }
                }
            }
            if (remove != null) {
                remove.forEach(mmo -> {
                    removeMapObject(mmo);
                    MapleMap.this.announce(((MapleAffectedArea) mmo).makeDestroyData());
                });
                remove.clear();
                remove = null;
            }
            lastAreaAffectedTime = System.currentTimeMillis();
        }
    }

    public void spawnAffectedArea(final MapleAffectedArea affectedArea, boolean fake) {
        addMapObject(affectedArea);
        MapleMap.this.announce(fake ? affectedArea.makeFakeSpawnData(30) : affectedArea.makeSpawnData());
    }

    public Point calcDropPos(Point initial, Point fallback) {
        Point ret = calcPointBelow(new Point(initial.x, initial.y - 85));
        if (ret == null) {
            return fallback;
        }
        if (ret.x >= this.getMapData().getFootholds().getMaxDropX() - 25) {
            ret.x = this.getMapData().getFootholds().getMaxDropX() - 75;
        }
        if (ret.x <= this.getMapData().getFootholds().getMinDropX() + 25) {
            ret.x = this.getMapData().getFootholds().getMinDropX() + 75;
        }
        return ret;
    }

    private void dropFromMonster(final MapleCharacter player, final MapleMonster monster) {
        Item idrop = null;
        byte d = 1;
        Point pos = new Point(0, monster.getPosition().y);
        final byte droptype = (byte) (monster.getStats().isExplosiveReward() ? 3 : monster.getStats().isFfaLoot() ? 2 : player.isInParty() ? 1 : 0);
        final int mobpos = monster.getPosition().x;
        if (monster.dropsDisabled() || !dropsOn) {
            if (player.getEventInstance() != null && player.getEventInstance().getName().startsWith("MD_")) {
                if (Randomizer.nextInt(101) <= 5) {
                    pos.x = (int) (mobpos + ((d % 2 == 0) ? (25 * (d + 1) / 2) : -(25 * (d / 2))));
                    idrop = new Item(2022162, (short) 0, (short) 1);
                    spawnDrop(idrop, calcDropPos(pos, monster.getPosition()), monster, player, droptype, (short) 0);
                    d++;
                }
                if (Randomizer.nextInt(101) <= 60) {
                    pos.x = (int) (mobpos + ((d % 2 == 0) ? (25 * (d + 1) / 2) : -(25 * (d / 2))));
                    idrop = new Item(2022174, (short) 0, (short) 1);
                    spawnDrop(idrop, calcDropPos(pos, monster.getPosition()), monster, player, droptype, (short) 0);
                    d++;
                }
                if (Randomizer.nextInt(101) <= 40) {
                    pos.x = (int) (mobpos + ((d % 2 == 0) ? (25 * (d + 1) / 2) : -(25 * (d / 2))));
                    idrop = new Item(2022177, (short) 0, (short) 1);
                    spawnDrop(idrop, calcDropPos(pos, monster.getPosition()), monster, player, droptype, (short) 0);
                    d++;
                }
            }
            return;
        }
        final ItemInformationProvider ii = ItemInformationProvider.getInstance();
        double chServerrate = player.getStats().getDropRate();
        Map<MobStat, MobStatData> mobStats = monster.getMobStats();
        if (mobStats.containsKey(MobStat.Showdown)) {
            chServerrate *= (mobStats.get(MobStat.Showdown).nOption / 100.0 + 1.0);
        }
        final MapleMonsterInformationProvider mi = MapleMonsterInformationProvider.getInstance();
        List<MonsterDropEntry> dropEntry = new ArrayList<>(mi.retrieveDrop(monster.getId()));
        Collections.shuffle(dropEntry);
        boolean meso = false;
        if (!FeatureSettings.MESO_DROPS) {
            meso = true;
        }
        for (final MonsterDropEntry de : dropEntry) {
            if (de.itemId == 0) {
                meso = true;
            }
            if (Randomizer.nextInt(999999) < de.chance * chServerrate) {
                if (droptype == 3) {
                    pos.x = (int) (mobpos + (d % 2 == 0 ? (40 * (d + 1) / 2) : -(40 * (d / 2))));
                } else {
                    pos.x = (int) (mobpos + ((d % 2 == 0) ? (25 * (d + 1) / 2) : -(25 * (d / 2))));
                }
                if (de.itemId == 0) { // meso
                    if (de.Maximum != de.Minimum) {
                        int mesos = Randomizer.nextInt(de.Maximum - de.Minimum) + de.Minimum;
                        if (mesos > 0) {
                            if (FeatureSettings.MESO_DROPS) {
                                spawnMesoDrop((int) (mesos * player.getStats().getMesoRate()), calcDropPos(pos, monster.getPosition()), monster, player, false, droptype);
                            }
                        }
                    } else {
                        meso = false;
                    }
                } else {
                    if (ItemConstants.getInventoryType(de.itemId) == MapleInventoryType.EQUIP) {
                        if (Randomizer.nextInt(250000) <= 1) {
                            idrop = ii.makeRare(ii.randomizeStats((Equip) ii.getEquipById(de.itemId)), 2);
                            idrop.addLog("Legendary");
                            idrop.setOwner("(Legendary)");
                            idrop.setPosition((short) 1);
                            try {
                                ChannelServer.getInstance().getWorldInterface().broadcastPacket(WvsContext.BroadcastMsg.ItemMegaphone(idrop, "MèoStory : Congratulations to " + player.getName() + " on finding a Legendary.", player.getClient().getChannel(), false));
                            } catch (RemoteException | NullPointerException ex) {
                                Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
                            }
                        } else if (Randomizer.nextInt(100000) <= 1) {
                            idrop = ii.makeRare(ii.randomizeStats((Equip) ii.getEquipById(de.itemId)), 1.5);
                            idrop.addLog("Rare");
                            idrop.setOwner("(Rare)");
                        } else if (Randomizer.nextInt(50000) <= 1) {
                            idrop = ii.makeRare(ii.randomizeStats((Equip) ii.getEquipById(de.itemId)), 0.5);
                            idrop.addLog("Shit");
                            idrop.setOwner("(Shit)");
                        } else {
                            idrop = ii.randomizeStats((Equip) ii.getEquipById(de.itemId));
                        }
                        if (Randomizer.nextInt(100) <= 5) {
                            ((Equip) idrop).setGrade((byte) 1);
                        }
                    } else {
                        try {
                            idrop = new Item(de.itemId, (short) 0, (short) (de.Maximum != 1 ? Randomizer.nextInt(de.Maximum - de.Minimum) + de.Minimum : 1));
                        } catch (IllegalArgumentException ex) {
                            Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, "Item: " + de.itemId + " Max: " + de.Maximum + " Min: " + de.Minimum);
                            continue;
                        }
                    }
                    if (de.dropType == 2) {
                        Set<Integer> gaveDrop = new HashSet<>();
                        for (AttackerEntry ae : monster.getAttackEntries()) {
                            for (AttackingMapleCharacter amc : ae.getAttackers()) {
                                if (!gaveDrop.contains(amc.getAttacker().getId())) {
                                    gaveDrop.add(amc.getAttacker().getId());
                                    if (idrop instanceof Equip) {
                                        spawnDropFor(amc.getAttacker(), ((Equip) idrop).copy(), calcDropPos(pos, monster.getPosition()), monster, (byte) 0, de.questid);
                                    } else {
                                        spawnDropFor(amc.getAttacker(), idrop.copy(), calcDropPos(pos, monster.getPosition()), monster, (byte) 0, de.questid);
                                    }
                                }
                            }
                        }
                    } else {
                        spawnDrop(idrop, calcDropPos(pos, monster.getPosition()), monster, player, droptype, de.questid);
                    }
                }
                d++;
            }
        }
        if (!MobConstants.isDropExempted(monster.getId()) && monster.getStats().getExp() > 0) {
            if (!meso) {
                if (Randomizer.nextInt(999999) < 600000 * chServerrate) {
                    pos.x = (int) (mobpos + ((d % 2 == 0) ? (25 * (d + 1) / 2) : -(25 * (d / 2))));
                    double mesoGain = (monster.getStats().getHp() / monster.getStats().getLevel());
                    mesoGain += ThreadLocalRandom.current().nextInt(monster.getStats().getLevel());
                    mesoGain *= player.getStats().getMesoRate();
                    mesoGain = Math.min(50000, mesoGain);// cap at 50k bags
                    spawnMesoDrop((int) Math.round(mesoGain), calcDropPos(pos, monster.getPosition()), monster, player, false, droptype);
                    d++;
                }
            }
            if (monster.getStats().getLevel() >= 120 && player.getReincarnations() > 0) {
                if (Randomizer.nextInt(999999) < 400 * chServerrate) {
                    pos.x = (int) (mobpos + ((d % 2 == 0) ? (25 * (d + 1) / 2) : -(25 * (d / 2))));
                    idrop = new Item(2022465, (short) 0, (short) 1);
                    spawnDrop(idrop, calcDropPos(pos, monster.getPosition()), monster, player, droptype, (short) 0);
                    d++;
                }
            }
            if (player.getReincarnations() > 0) {
                if (Randomizer.nextInt(999999) < 400 * chServerrate) {
                    pos.x = (int) (mobpos + ((d % 2 == 0) ? (25 * (d + 1) / 2) : -(25 * (d / 2))));
                    idrop = new Item(1912009, (short) 0, (short) 1);
                    spawnDrop(idrop, calcDropPos(pos, monster.getPosition()), monster, player, droptype, (short) 0);
                    d++;
                }
            }
        }
        dropEntry = null;
        final List<MonsterGlobalDropEntry> globalEntry = mi.getGlobalDrop();
        // Global Drops
        for (final MonsterGlobalDropEntry de : globalEntry) {
            if (Randomizer.nextInt(999999) < de.chance) {
                if (droptype == 3) {
                    pos.x = (int) (mobpos + (d % 2 == 0 ? (40 * (d + 1) / 2) : -(40 * (d / 2))));
                } else {
                    pos.x = (int) (mobpos + ((d % 2 == 0) ? (25 * (d + 1) / 2) : -(25 * (d / 2))));
                }
                if (de.dropType == 1) { // Within 21 levels of the player.
                    int level = player.getLevel();
                    if (!monster.isBoss() && (monster.getStats().getLevel() > MobConstants.HIGH_LEVEL_MOB || level > monster.getStats().getLevel() + 21 || level < monster.getStats().getLevel() - 21)) {
                        continue;
                    }
                }
                if (ItemConstants.isSpecialItem(de.itemId) && MobConstants.isDropExempted(monster.getId())) {
                    continue;
                } else {
                    if (ItemConstants.getInventoryType(de.itemId) == MapleInventoryType.EQUIP) {
                        idrop = ii.randomizeStats((Equip) ii.getEquipById(de.itemId));
                    } else {
                        idrop = new Item(de.itemId, (short) 0, (short) (de.Maximum != 1 ? Randomizer.nextInt(de.Maximum - de.Minimum) + de.Minimum : 1));
                    }
                    if (de.dropType == 2) {
                        Set<Integer> gaveDrop = new HashSet<>();
                        for (AttackerEntry ae : monster.getAttackEntries()) {
                            for (AttackingMapleCharacter amc : ae.getAttackers()) {
                                if (!gaveDrop.contains(amc.getAttacker().getId())) {
                                    gaveDrop.add(amc.getAttacker().getId());
                                    spawnDropFor(amc.getAttacker(), idrop, calcDropPos(pos, monster.getPosition()), monster, droptype, de.questid);
                                }
                            }
                        }
                    } else {
                        spawnDrop(idrop, calcDropPos(pos, monster.getPosition()), monster, player, droptype, de.questid);
                    }
                    d++;
                }
            }
        }
    }

    private void spawnDrop(final Item itemDrop, final Point dropPos, final MapleMonster monster, final MapleCharacter player, final byte droptype, final short questid) {
        final MapleMapItem mdrop = new MapleMapItem(itemDrop, dropPos, monster, player, droptype, false, questid);
        mdrop.setDropTime(System.currentTimeMillis());
        spawnAndAddRangedMapObject(mdrop, (MapleClient c) -> {
            if (questid <= 0 || (c.getPlayer().getQuestStatus(questid) == 1 && c.getPlayer().needQuestItem(questid, itemDrop.getItemId()))) {
                c.announce(DropPool.EnterField(mdrop, monster.getPosition(), dropPos, (byte) 1));
            }
        }, null);
        // Holiday PQ
        if ((player.getMapId() == 889100001 || player.getMapId() == 889100011 || player.getMapId() == 889100021) && (mdrop.getItemId() == 4032094 || mdrop.getItemId() == 4032095) && dropPos.getX() >= -397 && dropPos.getX() <= 22 && dropPos.getY() == 34) {
            TimerManager.getInstance().schedule("spawnDrop3", () -> {
                if (!mdrop.isPickedUp()) {
                    handleSnowVigor(itemDrop, dropPos, player);
                }
            }, 5 * 1000);
            mdrop.setDeleteTime(5 * 1000L);
        } else {
            mdrop.setDeleteTime(3 * 60 * 1000L);
        }
        activateItemReactors(mdrop, player.getClient());
    }

    private void spawnDropFor(final MapleCharacter player, final Item itemDrop, final Point dropPos, final MapleMonster monster, final byte droptype, final short questid) {
        final MapleMapItem mdrop = new MapleMapItem(itemDrop, dropPos, monster, player, droptype, false, questid);
        mdrop.setDropTime(System.currentTimeMillis());
        mdrop.setVisibleTo(player.getId());
        spawnAndAddRangedMapObject(player, mdrop, (MapleClient c) -> {
            if (questid <= 0 || (c.getPlayer().getQuestStatus(questid) == 1 && c.getPlayer().needQuestItem(questid, itemDrop.getItemId()))) {
                c.announce(DropPool.EnterField(mdrop, monster.getPosition(), dropPos, (byte) 1));
            }
        }, null);
        // Holiday PQ
        if ((player.getMapId() == 889100001 || player.getMapId() == 889100011 || player.getMapId() == 889100021) && (mdrop.getItemId() == 4032094 || mdrop.getItemId() == 4032095) && dropPos.getX() >= -397 && dropPos.getX() <= 22 && dropPos.getY() == 34) {
            TimerManager.getInstance().schedule("spawnDropsFor3", new Runnable() {

                @Override
                public void run() {
                    if (!mdrop.isPickedUp()) {
                        handleSnowVigor(itemDrop, dropPos, player);
                    }
                }
            }, 5 * 1000);
            mdrop.setDeleteTime(5 * 1000L);
        } else {
            mdrop.setDeleteTime(3 * 60 * 1000L);
        }
        activateItemReactors(mdrop, player.getClient());
    }

    public final void spawnMesoDrop(final int meso, final Point position, final MapleMapObject dropper, final MapleCharacter owner, final boolean playerDrop, final byte droptype) {
        spawnMesoDrop(meso, position, dropper, owner, playerDrop, droptype, 0);
    }

    public final void spawnMesoDrop(final int meso, final Point position, final MapleMapObject dropper, final MapleCharacter owner, final boolean playerDrop, final byte droptype, int delay) {
        if (meso > 0) {// Because of meso amount being -5 from level.
            final Point droppos = calcDropPos(position, position);
            final MapleMapItem mdrop = new MapleMapItem(meso, droppos, dropper, owner, droptype, playerDrop);
            mdrop.setDropTime(System.currentTimeMillis());
            spawnAndAddRangedMapObject(mdrop, (MapleClient c) -> {
                c.announce(DropPool.EnterField(mdrop, dropper.getPosition(), (byte) 1, delay));
            }, null);
            mdrop.setDeleteTime(3 * 60 * 1000L);
        }
    }

    public final void disappearingItemDrop(final MapleMapObject dropper, final MapleCharacter owner, final Item item, final Point pos) {
        final Point droppos = calcDropPos(pos, pos);
        final MapleMapItem drop = new MapleMapItem(item, droppos, dropper, owner, (byte) 1, false);
        announce(DropPool.EnterField(drop, dropper.getPosition(), droppos, (byte) 3), drop.getPosition());
    }

    public MapleMonster getMonsterById(int id) {
        for (MapleMapObject obj : mapobjects.values()) {
            if (obj.getType() == MapleMapObjectType.MONSTER) {
                if (((MapleMonster) obj).getId() == id) {
                    return (MapleMonster) obj;
                }
            }
        }
        return null;
    }

    public int countMonster(int id) {
        int count = 0;
        for (MapleMapObject m : getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER))) {
            MapleMonster mob = (MapleMonster) m;
            if (mob.getId() == id) {
                count++;
            }
        }
        return count;
    }

    public boolean damageMonster(final MapleCharacter player, final MapleMonster monster, final int damage) {
        if (monster.getId() == 8800000) {
            for (MapleMapObject object : player.getMap().getMapObjects()) {
                MapleMonster mons = player.getMap().getMonsterByOid(object.getObjectId());
                if (mons != null) {
                    if (mons.getId() >= 8800003 && mons.getId() <= 8800010) {
                        return true;
                    }
                }
            }
        }
        if (monster.isAlive()) {
            boolean killed = false;
            monster.monsterLock.lock();
            try {
                if (!monster.isAlive()) {
                    return false;
                }
                Pair<Integer, Integer> cool = monster.getStats().getCool();
                if (cool != null) {
                    Pyramid pq = (Pyramid) player.getPartyQuest();
                    if (pq != null) {
                        if (damage > 0) {
                            if (damage >= cool.getLeft()) {
                                if ((Math.random() * 100) < cool.getRight()) {
                                    pq.cool();
                                } else {
                                    pq.kill();
                                }
                            } else {
                                pq.kill();
                            }
                        } else {
                            pq.miss();
                        }
                        killed = true;
                    }
                }
                if (damage > 0) {
                    monster.damage(player, damage, true);
                    if (!monster.isAlive()) { // monster just died
                        // killMonster(monster, chr, true);
                        killed = true;
                    }
                } else if (monster.getId() >= 8810002 && monster.getId() <= 8810009) {
                    for (MapleMapObject object : player.getMap().getMapObjects()) {
                        MapleMonster mons = player.getMap().getMonsterByOid(object.getObjectId());
                        if (mons != null) {
                            if (monster.isAlive() && (monster.getId() >= 8810010 && monster.getId() <= 8810017)) {
                                if (mons.getId() == 8810018) {
                                    killMonster(mons, player, true);
                                }
                            }
                        }
                    }
                }
            } finally {
                monster.monsterLock.unlock();
            }
            if (monster.getStats().selfDestruction() != null && monster.getStats().selfDestruction().getHp() > -1) {// should work ;p
                if (monster.getHp() <= monster.getStats().selfDestruction().getHp()) {
                    killMonster(monster, player, true, false, monster.getStats().selfDestruction().getAction());
                    return true;
                }
            }
            if (killed) {
                killMonster(monster, player, true);
            }
            return true;
        }
        return false;
    }

    public List<MapleMonster> getMonsters() {
        List<MapleMonster> mobs = new ArrayList<>();
        for (MapleMapObject object : this.getMapObjects()) {
            if (!object.getType().equals(MapleMapObjectType.MONSTER)) {
                continue;
            }
            mobs.add((MapleMonster) object);
        }
        return mobs;
        // return getMapObjects().stream().filter(mo-> mo.getType().equals(MapleMapObjectType.MONSTER)).map(mo-> (MapleMonster) mo).collect(Collectors.toList());
    }

    public int getMonsterSizeOnTeam(int team) {
        int size = 0;
        for (MapleMonster mob : getMonsters()) {
            if (mob.getTeam() == team) {
                size++;
            }
        }
        return size;
    }

    public List<MapleMonster> getMonstersEvent(MapleCharacter player) {
        List<MapleMonster> eventMobs = new ArrayList<>();
        MapleMap eventMap = player.getEventInstance().getMapInstance(player.getMapId());
        for (MapleMapObject object : eventMap.getMapObjects()) {
            if (object.getType() == MapleMapObjectType.MONSTER) {
                eventMobs.add((MapleMonster) object);
            }
        }
        return eventMobs;
    }

    public void killMonster(final MapleMonster monster, final MapleCharacter player, final boolean withDrops) {
        killMonster(monster, player, withDrops, false, 1);
    }

    public void killMonster(final MapleMonster monster, final MapleCharacter player, final boolean withDrops, final boolean secondTime, int animation) {
        /*        if (monster.getId() == 9500365) {
		 spawnMonster(MapleLifeFactory.getMonster(9500365));
		 }*/ // TROLL
        try {
            if (player == null) {
                spawnedMonstersOnMap.decrementAndGet();
                monster.setHp(0);
                announce(MobPool.killMonster(monster.getObjectId(), animation), monster.getPosition());
                removeMapObject(monster);
                return;
            }
            if (monster.getId() == 8810018 && !secondTime) {
                TimerManager.getInstance().schedule("killMonster555", new Runnable() {

                    @Override
                    public void run() {
                        killMonster(monster, player, withDrops, true, 1);
                        killAllMonsters();
                    }
                }, 3000);
                return;
            }
            MapleCharacter dropOwner = monster.killBy(player);// Highest damager
            /*if (chr.getQuest(MapleQuest.getInstance(29400)).getStatus().equals(MapleQuestStatus.Status.STARTED)) {
			 if (chr.getLevel() >= 120 && monster.getStats().getLevel() >= 120) {
			 //FIX MEDAL SHET
			 } else if (monster.getStats().getLevel() >= chr.getLevel()) {
			 }
			 }*/
            int buff = monster.getBuffToGive();
            if (buff > -1) {
                ItemInformationProvider mii = ItemInformationProvider.getInstance();
                for (MapleMapObject mmo : this.getAllPlayer()) {
                    MapleCharacter character = (MapleCharacter) mmo;
                    if (character.isAlive()) {
                        MapleStatEffect statEffect = mii.getItemData(buff).itemEffect;
                        character.getClient().announce(EffectPacket.Local.SkillUse.encode(buff, character.getLevel(), 1));
                        announce(character, EffectPacket.Remote.SkillUse.encode(character.getId(), buff, character.getLevel(), 1), false);
                        statEffect.applyTo(character);
                    }
                }
            }
            if (monster.getId() == 8810018 && player.getMapId() == 240060200) {
                for (Channel cserv : ChannelServer.getInstance().getChannels()) {
                    for (MapleCharacter character : cserv.getPlayerStorage().getAllCharacters()) {
                        character.dropMessage("To the crew that have finally conquered Horned Tail after numerous attempts, I salute thee! You are the true heroes of Leafre!!");
                    }
                }
            } else if (monster.getId() == 8820001 && player.getMapId() == 270050100) {
                for (MapleCharacter fighter : player.getMap().getCharacters()) {
                    if (fighter.getQuest(MapleQuest.getInstance(3522)).getStatus() == MapleQuestStatus.Status.STARTED) {
                        fighter.getQuest(MapleQuest.getInstance(3522)).setStatus(MapleQuestStatus.Status.COMPLETED);
                    }
                }
                for (Channel cserv : ChannelServer.getInstance().getChannels()) {
                    for (MapleCharacter character : cserv.getPlayerStorage().getAllCharacters()) {
                        character.dropMessage("Oh, the exploration team who has defeated Pink Bean with undying fervor! You are the true victors of time!");
                    }
                }
            }
            spawnedMonstersOnMap.decrementAndGet();
            monster.setHp(0);
            announce(MobPool.killMonster(monster.getObjectId(), animation), monster.getPosition());
            // if (monster.getStats().selfDestruction() == null) {//FUU BOMBS D:
            removeMapObject(monster);
            // }
            if (player.getEventInstance() != null) {
                if (player.getEventInstance().getName().startsWith("MD_")) {
                    if (player.isInParty()) {
                        ChannelServer.getInstance().getWorldInterface().incrementMonsterKills(player.getPartyId());
                    }
                }
                if (monster.getId() == 9300049) { // Dark Nependeath
                    player.getEventInstance().getMapInstance(player.getMapId()).spawnMonsterOnGroundBelow(9300039, -842, 563);
                }
                if (monster.getId() == 9300039) { // Papa Pixie
                    if (player.getEventInstance() != null) {
                        player.getClient().getChannelServer().getEventSM().getEventManager("OrbisPQ").setProperty("finished", "2");
                    }
                }
                if (player.getMapId() >= 926100000 && player.getMapId() <= 926110600) { // Frankenroid
                    EventManager em = player.getClient().getChannelServer().getEventSM().getEventManager("Romeo");
                    if (player.getMapId() >= 926110000 && player.getMapId() <= 926110600) {
                        em = player.getClient().getChannelServer().getEventSM().getEventManager("Juliet");
                    }
                    if (monster.getId() == 9300137 || monster.getId() == 9300138) {
                        em.setProperty("stage7", "1");
                        player.getEventInstance().broadcastPlayerMsg(5, "The one you were protecting has been killed.");
                    } else if (monster.getId() == 9300139 || monster.getId() == 9300140) {
                        if (player.getMapId() == 926100401) {
                            spawnNpc(2112004, 282, 150, player.getMap());
                        } else {
                            spawnNpc(2112003, 282, 150, player.getMap());
                        }
                    }
                }
            }
            // Horntail
            if (monster.getId() == 8810000) {
                if (player.getEventInstance() != null) {
                    player.getClient().getChannelServer().getEventSM().getEventManager("HorntailFight").setProperty("preheadCheck", "2");
                }
            }
            if (monster.getId() == 8810001) {
                if (player.getEventInstance() != null) {
                    player.getClient().getChannelServer().getEventSM().getEventManager("HorntailFight").setProperty("preheadCheck", "4");
                }
            }
            if (monster.getId() >= 8810002 && monster.getId() <= 8810009) { // Why is Horntail a pain in the butt
                if (horntailDeadMobs == null) {
                    horntailDeadMobs = new LinkedHashSet<>();
                }
                horntailDeadMobs.add(monster);
                if (isHorntailMapDead()) {
                    killMonster(getMonsterById(8810018), player, true);
                    horntailDeadMobs.clear();
                    horntailDeadMobs = null;
                }
            }
            if (((monster.getId() > 8820002 && monster.getId() < 8820007) || (monster.getId() > 8820014 && monster.getId() < 8820019) || monster.getId() == 8820002) && getPinkBeanSpongeID(PBStatus).isAlive()) { // Pink Bean
                if (pinkbeanDeadMobs == null) {
                    pinkbeanDeadMobs = new LinkedHashSet<>();
                }
                pinkbeanDeadMobs.add(monster);
                if (isPinkBeanMapDead(PBStatus)) {
                    killMonster(getMonsterById(8820010 + PBStatus), player, false);
                    if (PBStatus == 4) {
                        killMonster(getMonsterById(8820000), player, false);
                        PBStatus = 0;
                    } else {
                        PBStatus++;
                    }
                    pinkbeanDeadMobs.clear();
                    pinkbeanDeadMobs = null;
                }
            }
            // Zakum
            if (monster.getId() > 8800002 && monster.getId() < 8800011) { // MAKE MONSTER REAL DOESN'T WORK WUT/M
                if (zakumDeadMobs == null) {
                    zakumDeadMobs = new LinkedHashSet<>();
                }
                zakumDeadMobs.add(monster);
                if (isZakumMapDead()) {
                    makeMonsterReal(getMonsterById(8800000));
                    zakumDeadMobs.clear();
                    zakumDeadMobs = null;
                }
            }
            MapleCharacter nonNull = dropOwner;
            if (nonNull == null) {
                nonNull = player;
            }
            if (nonNull.getEventInstance() != null) {
                if (nonNull.getEventInstance().getName().startsWith("BossPQ")) {
                    if (monster.isBoss()) {
                        killAllMonsters();
                    }
                }
            }
            if (withDrops) {
                if (nonNull.getEventInstance() == null || (nonNull.getEventInstance() != null && !nonNull.getEventInstance().getName().contains("BossPQ"))) {
                    dropFromMonster(nonNull, monster);
                }
            }
            int levelRange = nonNull.getLevel() - monster.getStats().getLevel();
            if (!MobConstants.isNXGainExemption(monster.getId())) {
                if (levelRange <= 15 && levelRange >= -15) {
                    nonNull.getCashShop().gainCash(GameConstants.MAIN_NX_TYPE, 5);
                } else {
                    nonNull.getCashShop().gainCash(GameConstants.MAIN_NX_TYPE, 1);
                }
            }
            if ((levelRange <= 10 && levelRange >= -10) || (nonNull.getLevel() >= 120 && monster.getStats().getLevel() >= 100)) {
                if (monster.getStats().getExp() > 4 && nonNull.getGuildId() >= 0) {
                    try {
                        ChannelServer.getInstance().getWorldInterface().addGuildCoins(nonNull.getGuildId(), 1L);
                    } catch (RemoteException | NullPointerException ex) {
                        Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
                    }
                }
            }
            nonNull.incrementCombatProgress();
            nonNull.addMonsterKillCount(monster.getLevel() > MobConstants.HIGH_LEVEL_MOB || monster.getStats().getLevel() >= nonNull.getLevel());
            List<ModifyInventory> mods = null;
            for (Item item : nonNull.getInventory(MapleInventoryType.EQUIPPED)) {
                Equip eqp = (Equip) item;
                if (eqp.getDurability() > 0) {
                    if (mods == null) {
                        mods = new ArrayList<>();
                    }
                    eqp.setDurability(eqp.getDurability() - 1);
                    mods.add(new ModifyInventory(3, eqp));
                    mods.add(new ModifyInventory(0, eqp));
                }
            }
            if (mods != null && !mods.isEmpty()) {
                nonNull.announce(WvsContext.InventoryOperation(true, mods));
            }
            if (monster.getOwner() != null) {
                MapleCharacter mc = getCharacterByName(monster.getOwner());
                if (mc != null && mc.getHp() <= 0) {
                    mc.setHp(mc.getMaxHp());
                    mc.updateSingleStat(MapleStat.HP, mc.getMaxHp());
                    mc.setStance(0);
                    announce(mc, UserPool.UserLeaveField(mc.getId()), false);
                    announce(mc, UserPool.UserEnterField(mc), false);
                }
            }
            if (nonNull.getSlayerTask() != null) {
                if (nonNull.getSlayerTask().getTargetID() == monster.getId()) {
                    if (!nonNull.getSlayerTask().isCompleted()) {
                        nonNull.gainRSSkillExp(RSSkill.Slayer, nonNull.getSlayerTask().getTargetLevel());
                        if (nonNull.getSlayerTask().incrementKills()) {
                            nonNull.dropMessage(MessageType.SYSTEM, "You have completed your Slayer Task.");
                        } else {
                            nonNull.dropMessage(MessageType.TITLE, "Slayer Task: " + nonNull.getSlayerTask().getKills() + "/" + nonNull.getSlayerTask().getRequiredKills());
                        }
                    }
                }
            }
        } catch (Throwable e) {
            Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
        }
    }

    public void killFriendlies(MapleMonster monster) {
        MapleCharacter chr = null;
        if (getAllPlayer().size() != 0) {
            chr = (MapleCharacter) getAllPlayer().get(0);
        }
        killMonster(monster, chr, false);
    }

    public void killMonster(int monsterId) {
        for (MapleMapObject mmo : getMapObjects()) {
            if (mmo instanceof MapleMonster) {
                if (((MapleMonster) mmo).getId() == monsterId) {
                    MapleCharacter chr = null;
                    if (getAllPlayer().size() != 0) {
                        chr = (MapleCharacter) getAllPlayer().get(0);
                    }
                    killMonster((MapleMonster) mmo, chr, false);
                }
            }
        }
    }

    public void monsterCloakingDevice() {
        for (MapleMapObject monsterMapObject : getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER))) {
            MapleMonster monster = (MapleMonster) monsterMapObject;
            announce(MobPool.makeMonsterInvisible(monster));
        }
    }

    public void softKillAllMonsters() {
        for (MapleMapObject monsterMapObject : getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER))) {
            MapleMonster monster = (MapleMonster) monsterMapObject;
            if (monster.getStats().isFriendly()) {
                continue;
            }
            spawnedMonstersOnMap.decrementAndGet();
            monster.setHp(0);
            removeMapObject(monster);
        }
    }

    public void killAllMonstersNotFriendly() {
        for (MapleMapObject monsterMapObject : getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER))) {
            MapleMonster monster = (MapleMonster) monsterMapObject;
            if (monster.getStats().isFriendly()) {
                continue;
            }
            spawnedMonstersOnMap.decrementAndGet();
            monster.setHp(0);
            announce(MobPool.killMonster(monster.getObjectId(), true), monster.getPosition());
            removeMapObject(monster);
        }
    }

    public void killAllMonsters() {
        for (MapleMapObject monsterMapObject : getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER))) {
            MapleMonster monster = (MapleMonster) monsterMapObject;
            spawnedMonstersOnMap.decrementAndGet();
            monster.setHp(0);
            announce(MobPool.killMonster(monster.getObjectId(), true), monster.getPosition());
            removeMapObject(monster);
        }
    }

    public List<MapleMapObject> getAllPlayer() {
        return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.PLAYER));
    }

    public List<MapleCharacter> getAllPlayers() {
        List<MapleCharacter> ret = new ArrayList<>();
        for (MapleCharacter player : characters) {
            ret.add(player);
        }
        return ret;
    }

    public void destroyReactor(MapleReactor reactor) {
        TimerManager tMan = TimerManager.getInstance();
        reactor.setAlive(false);
        // Nexon actually remove it from map entirely.
        // Should probably do that and also fully remove the reactor from the map
        // then respawn it later using update method
        if (reactor.getDelay() > 0) {
            tMan.schedule("destroyReactor", () -> {
                respawnReactor(reactor);
            }, reactor.getDelay());
        }
    }

    public void resetReactors() {
        for (MapleMapObject o : mapobjects.values()) {
            if (o.getType() == MapleMapObjectType.REACTOR) {
                final MapleReactor reactor = ((MapleReactor) o);
                reactor.setState(0);
                reactor.setAlive(true);
                announce(ReactorPool.ReactorChangeState(reactor, 0));
            }
        }
    }

    public void shuffleReactors() {
        List<Point> points = new ArrayList<>();
        for (MapleMapObject o : mapobjects.values()) {
            if (o.getType() == MapleMapObjectType.REACTOR) {
                points.add(((MapleReactor) o).getPosition());
            }
        }
        Collections.shuffle(points);
        for (MapleMapObject o : mapobjects.values()) {
            if (o.getType() == MapleMapObjectType.REACTOR) {
                ((MapleReactor) o).setPosition(points.remove(points.size() - 1));
            }
        }
    }

    public MapleReactor getReactorById(int Id) {
        for (MapleMapObject obj : mapobjects.values()) {
            if (obj.getType() == MapleMapObjectType.REACTOR) {
                if (((MapleReactor) obj).getId() == Id) {
                    return (MapleReactor) obj;
                }
            }
        }
        return null;
    }

    /**
     * Automagically finds a new controller for the given monster from the chars
     * on the map...
     *
     * @param monster
     */
    public void updateMonsterController(MapleMonster monster) {
        monster.monsterLock.lock();
        try {
            if (!monster.isAlive()) {
                return;
            }
            if (monster.getController() != null) {
                if (monster.getController().getMap() != this) {
                    monster.getController().stopControllingMonster(monster);
                } else {
                    return;
                }
            }
            int mincontrolled = -1;
            MapleCharacter newController = null;
            for (MapleCharacter chr : characters) {
                if (!chr.isHidden() && (chr.getControlledMonsters().size() < mincontrolled || mincontrolled == -1)) {
                    mincontrolled = chr.getControlledMonsters().size();
                    newController = chr;
                }
            }
            if (newController != null) {// was a new controller found? (if not no one is on the map)
                if (monster.isFirstAttack()) {
                    newController.controlMonster(monster, true);
                    monster.setControllerHasAggro(true);
                    monster.setControllerKnowsAboutAggro(true);
                } else {
                    newController.controlMonster(monster, false);
                }
            }
        } finally {
            monster.monsterLock.unlock();
        }
    }

    public Collection<MapleMapObject> getMapObjects() {
        return Collections.unmodifiableCollection(mapobjects.values());
    }

    public boolean containsNPC(int npcId) {
        if (npcId == 9000066) {
            return true;
        }
        for (MapleMapObject obj : mapobjects.values()) {
            if (obj.getType() == MapleMapObjectType.NPC) {
                if (((MapleNPC) obj).getId() == npcId) {
                    return true;
                }
            }
            if (obj.getType() == MapleMapObjectType.PLAYER_NPC) {
                if (((PlayerNPC) obj).getId() == npcId) {
                    return true;
                }
            }
        }
        return false;
    }

    public MapleMapObject getMapObject(int objectId) {
        return mapobjects.get(objectId);
    }

    /**
     * returns a monster with the given oid, if no such monster exists returns
     * null
     *
     * @param objectId
     * @return
     */
    public MapleMonster getMonsterByOid(int objectId) {
        MapleMapObject mmo = getMapObject(objectId);
        if (mmo == null) {
            return null;
        }
        if (mmo.getType() == MapleMapObjectType.MONSTER) {
            return (MapleMonster) mmo;
        }
        return null;
    }

    public MapleReactor getReactorByOid(int oid) {
        MapleMapObject mmo = getMapObject(oid);
        if (mmo == null) {
            return null;
        }
        return mmo.getType() == MapleMapObjectType.REACTOR ? (MapleReactor) mmo : null;
    }

    public MapleReactor getReactorByName(String name) {
        for (MapleMapObject obj : mapobjects.values()) {
            if (obj.getType() == MapleMapObjectType.REACTOR) {
                if (((MapleReactor) obj).getName().equals(name)) {
                    return (MapleReactor) obj;
                }
            }
        }
        return null;
    }

    public void spawnMonsterOnGroundBelow(int id, int x, int y) {
        spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(id), new Point(x, y));
    }

    public void spawnMonsterOnGroundBelow(int id, int x, int y, boolean force) {
        spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(id), new Point(x, y), force);
    }

    public void spawnMonsterOnGroundBelow(int id, Point pos) {
        spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(id), pos);
    }

    public void spawnMonsterOnGroundBelow(MapleMonster monster, Point pos) {
        spawnMonsterOnGroundBelow(monster, pos, false);
    }

    public void spawnMonsterOnGroundBelow(MapleMonster monster, Point pos, boolean force) {
        Point spos = new Point(pos.x, pos.y - 1);
        Point newPos = calcPointBelow(spos);
        if (newPos != null) {
            spos = newPos;
            spos.y -= 5;
        }
        monster.setPosition(spos);
        spawnMonster(monster, force);
    }

    public void addBunnyHit() {
        long thisBunnyHit = System.currentTimeMillis();
        if (thisBunnyHit - lastBunnyHit <= 1515) { // +1 combo when gets hit continuously
            bunnyHitCombo++;
        } else {
            bunnyHitCombo = 1;
        }
        lastBunnyHit = thisBunnyHit;
        if (bunnyDamage++ > 5) {
            announce(WvsContext.BroadcastMsg.encode(6, "The Moon Bunny is feeling sick. Please protect it so it can make delicious rice cakes."));
            bunnyDamage = 0;
        }
    }

    public void addSnowmanHit() {
        snowmanDamage++;
        if (snowmanDamage > 5) {
            announce(WvsContext.BroadcastMsg.encode(6, "The snowman is starting to melt. Please protect it and drop more Snow Vigor to help it grow."));
            snowmanDamage = 0;
        }
    }

    // NOTE: since only HPQ and Watchhog (w/e that is) use this method,
    // it is set to start the next scheduler only when the mob is there AND if there are players in map.
    // if you want it to keep triggering even if no player is in the map, refer to the NOTE below.
    private void monsterItemDrop(final MapleMonster monster, final Item item, final int delay) {
        ScheduledFuture<?> monsterItemDrop = TimerManager.getInstance().schedule("monsterItemDrop", () -> {
            if (getMonsterById(monster.getId()) != null && !getAllPlayers().isEmpty()) {
                int dropPeriod = delay;
                if (item.getItemId() == 4001101) { // rice cakes, HPQ
                    riceCakes++;
                    announce(WvsContext.BroadcastMsg.encode(6, "The Moon Bunny made rice cake number " + (riceCakes)));
                    // let's set the next drop delay
                    // faster = 4 secs, default = 20 secs (60 secs / 3), slowest = 30 secs
                    long hitduration = System.currentTimeMillis() - lastBunnyHit;
                    // the longer the bunny is left alone, the faster it drops rice cake.
                    // the more times bunny get hits continuously, the slower it drops rice cake.
                    if (hitduration >= 20000) { // 20 secs or above
                        dropPeriod = 4000; // 4 secs
                    } else if (hitduration >= 10000) { // 10 secs or above
                        dropPeriod = 8000; // 8 secs
                    } else if (hitduration >= 5000) { // 5 secs or above
                        dropPeriod = 12000; // 12 secs, i can make it 10 but i dont want to :p
                    } else if (bunnyHitCombo >= 30) {
                        dropPeriod = 30000; // 30 secs
                    } else if (bunnyHitCombo >= 20) {
                        dropPeriod = 24000; // 24 secs
                    }
                }
                // NOTE: move the next scheduler code to somewhere else if you want it to happen under different conditions.
                monsterItemDrop(monster, item, dropPeriod); // schedule the next one.
                spawnItemDrop(monster, getAllPlayers().get(0), item, monster.getPosition(), true, false);
            }
        }, delay);
        if (getMonsterById(monster.getId()) == null) {
            monsterItemDrop.cancel(true);
        }
    }

    public void spawnFakeMonsterOnGroundBelow(MapleMonster monster, Point pos) {
        Point spos = getGroundBelow(pos);
        monster.setPosition(spos);
        spawnFakeMonster(monster);
    }

    public Point getGroundBelow(Point pos) {
        int yIncrease = 3;
        Point spos = new Point(pos.x, pos.y - yIncrease); // Using -3 fixes issues with spawning pets causing a lot of issues.
        spos = calcPointBelow(spos);
        yIncrease = 0;
        while (spos == null) {
            spos = calcPointBelow(new Point(pos.x, pos.y - yIncrease));
            if (spos == null) {
                spos = calcPointBelow(new Point(pos.x, pos.y + yIncrease));
            }
            yIncrease++;
            if (yIncrease >= 1000) {
                return null;
            }
        }
        spos.y--;// shouldn't be null!
        return spos;
    }

    public void spawnRevives(final MapleMonster monster) {
        monster.setMap(this);
        spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {

            @Override
            public void sendPackets(MapleClient c) {
                c.announce(MobPool.spawnMonster(monster, false));
            }
        });
        updateMonsterController(monster);
        spawnedMonstersOnMap.incrementAndGet();
    }

    public void spawnMonster(final MapleMonster monster) {
        spawnMonster(monster, false);
    }

    int[] randomStatups = {150, 151, 152, 153, 154/*, 155*/, 156};

    public void spawnMonster(final MapleMonster monster, int effect) {
        spawnMonster(monster, false, effect);
    }

    public void spawnMonster(final MapleMonster monster, boolean force) {
        spawnMonster(monster, force, 0);
    }

    public void spawnMonster(final MapleMonster monster, boolean force, int effect) {
        // if(!force && mobCapacity != -1 && (mobCapacity * (!ServerConstants.day ? 0 : 1.25)) == spawnedMonstersOnMap.get()){
        // return;//Pyramid PQ??
        // }
        monster.setMap(this);
        if (monster.getId() == 8810024) {
            TimerManager.getInstance().schedule("spawnOnGround1", () -> {
                spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8810000), monster.getPosition());
            }, 6000); // 'cuz lol
        }
        if (monster.getId() == 8810025) {
            TimerManager.getInstance().schedule("spawnOnGround2", () -> {
                spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8810001), monster.getPosition());
            }, 6000); // 'cuz lol
        }
        if (monster.getId() == 8810026) {
            TimerManager.getInstance().schedule("spawnOnGround3", () -> {
                for (int i = 8810002; i < 8810010; i++) {
                    spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(i), monster.getPosition());
                }
            }, 6000); // 'cuz lol
        }
        if (!getAllPlayers().isEmpty()) {
            MapleCharacter player = getAllPlayers().get(0);
            if (monster.getEventInstance() == null && player.getEventInstance() != null) {
                player.getEventInstance().registerMonster(monster);
            }
        }
        spawnAndAddRangedMapObject(monster, (MapleClient c) -> {
            c.announce(MobPool.spawnMonster(monster, true, effect));
        }, null);
        updateMonsterController(monster);
        // if(!ServerConstants.day && monster.getStats().getLevel() > 10){
        // MobSkillFactory.getMobSkill(150, 1).applyEffect(null, monster, false, false);
        // MobSkillFactory.getMobSkill(151, 1).applyEffect(null, monster, false, false);
        /// MobSkillFactory.getMobSkill(152, 1).applyEffect(null, monster, false, false);
        /// MobSkillFactory.getMobSkill(153, 1).applyEffect(null, monster, false, false);
        // }
        if (monster.getStats().getLevel() > 10) {
            if (monster.getItemEffect() >= 5010028 && monster.getItemEffect() <= 5010030) {
                MobSkill mobskill = MobSkillFactory.getMobSkill(randomStatups[rand.nextInt(randomStatups.length)], 1);
                if (mobskill != null) {
                    mobskill.applyEffect(null, monster, false, false);
                }
            }
        }
        if (monster.getDropPeriodTime() > 0) { // 9300102 - Watchhog, 9300061 - Moon Bunny (HPQ)
            if (monster.getId() == 9300102) {
                monsterItemDrop(monster, new Item(4031507, (short) 0, (short) 1), monster.getDropPeriodTime());
            } else if (monster.getId() == 9300061) {// use lastBunnyDamage? And maybe properly fix dropPeriodTime?
                monsterItemDrop(monster, new Item(4001101, (short) 0, (short) 1), monster.getDropPeriodTime() / 3);
            } else {
                Logger.log(LogType.INFO, LogFile.GENERAL_ERROR, "Uncoded timed mob detected: " + monster.getId());
            }
        }
        spawnedMonstersOnMap.incrementAndGet();
        final selfDestruction selfDestruction = monster.getStats().selfDestruction();
        if (monster.getStats().removeAfter() > 0 || selfDestruction != null && selfDestruction.getHp() < 0) {
            if (selfDestruction == null) {
                TimerManager.getInstance().schedule("killMonster2", () -> {
                    killMonster(monster, null, false);
                }, monster.getStats().removeAfter() * 1000);
            } else {
                TimerManager.getInstance().schedule("killMonster1", () -> {
                    killMonster(monster, null, false, false, selfDestruction.getAction());
                }, selfDestruction.removeAfter() * 1000);
            }
        }
    }

    public void spawnMKDummy(final MapleCharacter owner, MapleMonster monster) {
        monster.setMap(this);
        spawnAndAddRangedMapObject(monster, (MapleClient c) -> {
            c.announce(MobPool.spawnMonster(monster, true));
        }, null, owner);
    }

    public void spawnDojoMonster(final MapleMonster monster) {
        Point[] pts = {new Point(140, 0), new Point(190, 7), new Point(187, 7)};
        spawnMonsterWithEffect(monster, 15, pts[Randomizer.nextInt(3)]);
    }

    public void spawnMonsterWithEffect(final MapleMonster monster, final int effect, Point pos) {
        monster.setMap(this);
        Point spos = new Point(pos.x, pos.y - 1);
        spos = calcPointBelow(spos);
        spos.y--;
        monster.setPosition(spos);
        if (mapid < 925020000 || mapid > 925030000) {
            monster.disableDrops();
        }
        spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {

            @Override
            public void sendPackets(MapleClient c) {
                c.announce(MobPool.spawnMonster(monster, true, effect));
            }
        });
        if (monster.hasBossHPBar()) {
            announce(monster.makeBossHPBarPacket(), monster.getPosition());
        }
        updateMonsterController(monster);
        spawnedMonstersOnMap.incrementAndGet();
    }

    public void spawnFakeMonster(final MapleMonster monster) {
        monster.setMap(this);
        monster.setFake(true);
        spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {

            @Override
            public void sendPackets(MapleClient c) {
                // c.announce(MaplePacketCreator.spawnMonster(monster, true, 0xfc));
                c.announce(MobPool.spawnFakeMonster(monster, 0));
            }
        });
        updateMonsterController(monster);
        spawnedMonstersOnMap.incrementAndGet();
    }

    public void makeMonsterReal(final MapleMonster monster) {
        if (monster != null) {
            monster.setFake(false);
            announce(MobPool.makeMonsterReal(monster));
            updateMonsterController(monster);
        }
    }

    public void spawnReactor(final MapleReactor reactor) {
        reactor.setMap(this);
        spawnAndAddRangedMapObject(reactor, new DelayedPacketCreation() {

            @Override
            public void sendPackets(MapleClient c) {
                c.announce(reactor.makeSpawnData());
            }
        }, null);
    }

    private void respawnReactor(final MapleReactor reactor) {
        reactor.setState(0);
        reactor.setAlive(true);
    }

    public List<MapleCharacter> getPlayersInRange(Rectangle box, List<MapleCharacter> playerList) {
        List<MapleCharacter> character = new LinkedList<>();
        for (MapleCharacter a : characters) {
            if (playerList.contains(a.getClient().getPlayer())) {
                if (box.contains(a.getPosition())) {
                    character.add(a);
                }
            }
        }
        return character;
    }

    public void spawnSummon(final MapleSummon summon) {
        spawnAndAddRangedMapObject(summon, new DelayedPacketCreation() {

            @Override
            public void sendPackets(MapleClient c) {
                if (summon != null) {
                    c.announce(SummonedPool.Created(summon, true));
                }
            }
        }, null);
    }

    public final void spawnItemDrop(final MapleMapObject dropper, final MapleCharacter owner, final Item item, Point pos, final boolean ffaDrop, final boolean playerDrop) {
        spawnItemDrop(dropper, owner, item, pos, ffaDrop, playerDrop, 0);
    }

    public final void spawnItemDrop(final MapleMapObject dropper, final MapleCharacter owner, final Item item, Point pos, final boolean ffaDrop, final boolean playerDrop, int delay) {
        final Point droppos = calcDropPos(pos, pos);
        final MapleMapItem drop = new MapleMapItem(item, droppos, dropper, owner, (byte) (ffaDrop ? 2 : 0), playerDrop);
        drop.setDropTime(System.currentTimeMillis());
        spawnAndAddRangedMapObject(drop, new DelayedPacketCreation() {

            @Override
            public void sendPackets(MapleClient c) {
                c.announce(DropPool.EnterField(drop, dropper.getPosition(), (byte) 1, delay));
            }
        }, null);
        // spawnAndAddRangedMapObject(drop, (MapleClient c)-> {
        // c.announce(MaplePacketCreator.EnterField(drop, dropper.getPosition(), droppos, (byte) 1));
        // }, null);
        announce(DropPool.EnterField(drop, dropper.getPosition(), (byte) 0, delay));
        // Holiday PQ
        if ((owner.getMapId() == 889100001 || owner.getMapId() == 889100011 || owner.getMapId() == 889100021) && (drop.getItemId() == 4032094 || drop.getItemId() == 4032095) && pos.getX() >= -397 && pos.getX() <= 22 && pos.getY() == 34) {
            TimerManager.getInstance().schedule("handleSnowVigor", new Runnable() {

                @Override
                public void run() {
                    if (!drop.isPickedUp()) {
                        handleSnowVigor(item, pos, owner);
                    }
                }
            }, 5 * 1000);
            drop.setDeleteTime(5 * 1000L);
        } else {
            drop.setDeleteTime(3 * 60 * 1000L);
        }
        if (!getMapData().getEverlast()) {
            TimerManager.getInstance().schedule("activeateItemReactors", () -> {
                activateItemReactors(drop, owner.getClient());
            }, 6000);
        }
    }

    public void activateItemReactors(MapleClient c) {
        this.getAllItems().forEach(item -> {
            activateItemReactors(item, c);
        });
    }

    private void activateItemReactors(final MapleMapItem drop, final MapleClient c) {
        getAllReactor().stream().forEach(reactor -> {
            reactor.hitReactor(ReactorHitType.ITEM_TRIGGER, new ReactorHitInfo(), c);
        });
    }

    public final List<MapleReactor> getAllReactor() {
        List<MapleReactor> reactors = new ArrayList<>();
        for (MapleMapObject object : this.getMapObjects()) {
            if (!object.getType().equals(MapleMapObjectType.REACTOR)) {
                continue;
            }
            reactors.add((MapleReactor) object);
        }
        return reactors;
        // return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.REACTOR));
    }

    public final List<MapleMapItem> getAllItems() {
        List<MapleMapItem> items = new ArrayList<>();
        for (MapleMapObject object : this.getMapObjects()) {
            if (!object.getType().equals(MapleMapObjectType.ITEM)) {
                continue;
            }
            items.add((MapleMapItem) object);
        }
        return items;
        // return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.REACTOR));
    }

    public final List<PlayerShop> getPlayerShops() {
        List<PlayerShop> reactors = new ArrayList<>();
        for (MapleMapObject object : this.getMapObjects()) {
            if (!object.getType().equals(MapleMapObjectType.SHOP) && !object.getType().equals(MapleMapObjectType.HIRED_MERCHANT)) {
                continue;
            }
            reactors.add((PlayerShop) object);
        }
        return reactors;
        // return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.REACTOR));
    }

    public boolean startMapEffect(String msg, int itemId) {
        return startMapEffect(msg, itemId, 30000);
    }

    public boolean startMapEffect(String msg, int itemId, long time) {
        if (mapEffect == null) {
            return true;
        }
        mapEffect = new MapleMapEffect(msg, itemId);
        announce(mapEffect.makeStartData());
        TimerManager.getInstance().schedule("startMapEffect", new Runnable() {

            @Override
            public void run() {
                announce(mapEffect.makeDestroyData());
                mapEffect = null;
            }
        }, time);
        return false;
    }

    /**
     * Hàm này hội tụ mọi thứ cần có của người chơi khi vào Map!
     *
     * @param player
     */
    public void addPlayer(final MapleCharacter player) {
        characters.add(player);
        player.setMapId(mapid);
        lastPlayers.add(player.getName());

        if (mapData.getOnFirstUserEnter().length() != 0
                && !player.hasEntered(mapData.getOnFirstUserEnter(), mapid)
                && MapScriptManager.getInstance().scriptExists(mapData.getOnFirstUserEnter(), true)) {
            if (getAllPlayers().isEmpty()) {
                player.enteredScript(mapData.getOnFirstUserEnter(), mapid);
                MapScriptManager.getInstance().getMapScript(player.getClient(), mapData.getOnFirstUserEnter(), true);
            }
        }
        if (mapData.getOnUserEnter().length() != 0) {
            if (mapData.getOnUserEnter().equals("cygnusTest") && (mapid < 913040000 || mapid > 913040006)) {
                player.saveLocation("INTRO");
            }
            // SCRIPT-TODO: Thêm Map.onFirstUserEnter (Tôi lười lắm :<)
            MapScriptManager.getInstance().getMapScript(player.getClient(), mapData.getOnUserEnter(), false);
        }

        if (FieldLimit.CANNOTUSEMOUNTS.check(mapData.getFieldLimit()) && player.getBuffedValue(SecondaryStat.RideVehicle) != null) {
            player.cancelEffectFromSecondaryStat(SecondaryStat.RideVehicle);
            player.cancelBuffStats(SecondaryStat.RideVehicle);
            // Những Map không cho phép sử dụng thú cưỡi
        }

        if (mapid == 923010000 && getMonsterById(9300102) == null) { // Hidden Street - Kenta's Breeding Room
            spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(9300102), new Point(77, 426));
            // Những Map sẽ spawn Quái Vật ra!
        }

        if (mapid == 910010200) { // Hidden Street - Pig Town (HenesysPQ)
            removePlayerTimer(player.getId());
            synchronized (playerTimers) {
                player.announce(Clock.Created(60 * 5, 2));
                playerTimers.put(player.getId(), TimerManager.getInstance().schedule("boat6", new Runnable() {

                    @Override
                    public void run() {
                        if (player.getMapId() == 910010200) {
                            player.changeMap(910010400); // Hidden Street - Shortcut
                        }
                    }
                }, 60 * 5 * 1000));
            }
        } else if (mapid == 200090060) { // Snow Island - To Rien
            removePlayerTimer(player.getId());
            synchronized (playerTimers) {
                player.announce(Clock.Created(60 * 3, 2));
                playerTimers.put(player.getId(), TimerManager.getInstance().schedule("boat6", new Runnable() {

                    @Override
                    public void run() {
                        if (player.getMapId() == 200090060) {
                            player.changeMap(140020300); // Snow Island - Penguin Port
                        }
                    }
                }, 60 * 3 * 1000));
            }
        } else if (mapid == 200090070) { // Snow Island - To Lith Harbor
            removePlayerTimer(player.getId());
            synchronized (playerTimers) {
                player.announce(Clock.Created(60 * 3, 2));
                playerTimers.put(player.getId(), TimerManager.getInstance().schedule("boat5", new Runnable() {

                    @Override
                    public void run() {
                        if (player.getMapId() == 200090070) {
                            player.changeMap(104000000, 3); // Lith Harbor - Lith Harbor
                        }
                    }
                }, 60 * 3 * 1000));
            }
        } else if (mapid == 200090030) { // Empress' Road - To Ereve
            removePlayerTimer(player.getId());
            synchronized (playerTimers) {
                player.getClient().announce(Clock.Created(60 * 3, 2));
                playerTimers.put(player.getId(), TimerManager.getInstance().schedule("boat4", new Runnable() {

                    @Override
                    public void run() {
                        if (player.getMapId() == 200090030) {
                            player.changeMap(130000210); // Empress' Road - Sky Ferry 
                        }
                    }
                }, 60 * 3 * 1000));
            }
        } else if (mapid == 200090031) { // Empress' Road - Victoria Bound
            removePlayerTimer(player.getId());
            synchronized (playerTimers) {
                player.getClient().announce(Clock.Created(60 * 3, 2));
                playerTimers.put(player.getId(), TimerManager.getInstance().schedule("boat3", new Runnable() {

                    @Override
                    public void run() {
                        if (player.getMapId() == 200090031) {
                            player.changeMap(101000400); // Sky Ferry : Sky Ferry 
                        }
                    }
                }, 60 * 3 * 1000));
            }
        } else if (mapid == 200090021) { // Empress' Road - To Orbis
            removePlayerTimer(player.getId());
            synchronized (playerTimers) {
                player.getClient().announce(Clock.Created(60 * 3, 2));
                playerTimers.put(player.getId(), TimerManager.getInstance().schedule("boat2", new Runnable() {

                    @Override
                    public void run() {
                        if (player.getMapId() == 200090021) {
                            player.changeMap(200000161); // Orbis - Station -To Ereve-
                        }
                    }
                }, 60 * 3 * 1000));
            }
        } else if (mapid == 200090020) { // Empress' Road - To Ereve
            removePlayerTimer(player.getId());
            synchronized (playerTimers) {
                player.getClient().announce(Clock.Created(60 * 3, 2));
                playerTimers.put(player.getId(), TimerManager.getInstance().schedule("boat1", new Runnable() {

                    @Override
                    public void run() {
                        if (player.getMapId() == 200090020) {
                            player.changeMap(130000210); // Empress' Road - Sky Ferry 
                        }
                    }
                }, 60 * 3 * 1000));
            }
        } else if (mapid == 200090080) { // Olaf's Voyage - To the Slumbering Dragon Island 
            removePlayerTimer(player.getId());
            synchronized (playerTimers) {
                player.getClient().announce(Clock.Created(60 * 10, 2));
                playerTimers.put(player.getId(), TimerManager.getInstance().schedule("dragon-boat1", new Runnable() {

                    @Override
                    public void run() {
                        if (player.getMapId() == 200090080) {
                            player.changeMap(914100000); // Slumbering Dragon Island - Temporary Harbor 
                        }
                    }
                }, 60 * 10 * 1000));
            }
        } else if (mapid == 200090090) {// Olaf's Voyage - To Lith Harbor
            removePlayerTimer(player.getId());
            synchronized (playerTimers) {
                player.getClient().announce(Clock.Created(60 * 10, 2));
                playerTimers.put(player.getId(), TimerManager.getInstance().schedule("dragon-boat2", new Runnable() {

                    @Override
                    public void run() {
                        if (player.getMapId() == 200090090) {
                            player.changeMap(104000000); // Lith Harbor - Lith Harbor
                        }
                    }
                }, 60 * 10 * 1000));
            }
        } else if (mapid == 103040400) { // Kerning Square - 7th Floor 8th Floor Area A
            if (player.getEventInstance() != null) {
                player.getEventInstance().movePlayer(player);
            }
        } else if (MapleMiniDungeon.isDungeonMap(mapid)) {
            final MapleMiniDungeon dungeon = MapleMiniDungeon.getDungeon(mapid);
            removePlayerTimer(player.getId());
            synchronized (playerTimers) {
                player.getClient().announce(Clock.Created(60 * 30, 2));
                playerTimers.put(player.getId(), TimerManager.getInstance().schedule("dungeonExit", new Runnable() {

                    @Override
                    public void run() {
                        if (MapleMiniDungeon.isDungeonMap(player.getMapId())) {
                            player.changeMap(dungeon.getBase());
                        }
                    }
                }, 60 * 30 * 1000));
            }
        }
        if (player.getEventInstance() != null
                && player.getEventInstance().isTimerStarted()) {
            player.getClient().announce(Clock.Created((int) (player.getEventInstance().getTimeLeft() / 1000), 2));
        }
        if (player.getFitness() != null
                && player.getFitness().isTimerStarted()) {
            player.getClient().announce(Clock.Created((int) (player.getFitness().getTimeLeft() / 1000), 2));
        }
        if (player.getOla() != null
                && player.getOla().isTimerStarted()) {
            player.getClient().announce(Clock.Created((int) (player.getOla().getTimeLeft() / 1000), 2));
        }
        if (getMapData().hasClock()) {
            Calendar cal = Calendar.getInstance();
            player.getClient().announce((Clock.Created(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND))));
        }
        if (clockHours > 0 || clockMinutes > 0 || clockSeconds > 0) {
            player.getClient().announce((Clock.Created(clockSeconds + (clockMinutes * 60) + (clockHours * 60 * 60), 2)));
        }

        MaplePet[] pets = player.getPets();
        for (int i = 0; i < player.getPets().length; i++) {
            if (pets[i] != null) {
                pets[i].setPos(getGroundBelow(player.getPosition()));
                player.announce(PetPacket.showPet(player, pets[i], false, false));
                if (!pets[i].getExceptionList().isEmpty()) {
                    player.announce(PetPacket.petExceptionListResult(player, pets[i]));
                }
            } else {
                break;
            }
        }

        if (player.isHidden()) {
            announceGM(player, UserPool.UserEnterField(player), false);
            player.announce(Field.AdminResult.getGMEffect(AdminResult.HIDE, (byte) 1));
            List<Pair<SecondaryStat, BuffDataHolder>> dsstat = Collections.singletonList(new Pair<SecondaryStat, BuffDataHolder>(SecondaryStat.DarkSight, new BuffDataHolder(0, 0, 0)));
            announceGM(player, UserRemote.setTemporaryStat(player, dsstat), false);
            if (player.getMount() != null && player.getMount().isActive()) {
                broadcastGMMessage(player, UserRemote.showMonsterRiding(player.getId(), player.getMount()));
            }
        } else {
            announce(player, UserPool.UserEnterField(player), false);
            if (player.getMount() != null && player.getMount().isActive()) {
                announce(player, UserRemote.showMonsterRiding(player.getId(), player.getMount()));
            }
        }

        sendObjectPlacement(player.getClient());

        if (isStartingEventMap() && !eventStarted()) {
            player.getMap().getPortal("join00").setPortalStatus(false);
        }

        if (hasForcedEquip()) {
            player.getClient().announce(PacketHelper.showForcedEquip(-1));
        }

        if (specialEquip()) {
            player.getClient().announce(Coconut.Score(0, 0));
            player.getClient().announce(PacketHelper.showForcedEquip(player.getTeam()));
        }

        this.mapobjects.put(player.getObjectId(), player);
        if (player.getPlayerShop() != null) {
            addMapObject(player.getPlayerShop());
        }

        final MapleDragon dragon = player.getDragon();
        if (dragon != null) {
            dragon.setPosition(player.getPosition());
            dragon.sendSpawnData(player.getClient());
            this.addMapObject(dragon);
            if (player.isHidden()) {
                this.broadcastGMMessage(player, DragonPacket.Created(dragon));
            } else {
                this.announce(player, DragonPacket.Created(dragon));
            }
        }

        MapleStatEffect summonStat = player.getStatForBuff(SecondaryStat.SUMMON);
        if (summonStat != null) {
            MapleSummon summon = player.getSummons().get(summonStat.getSourceId());
            summon.setPosition(player.getPosition());
            player.getMap().spawnSummon(summon);
            updateMapObjectVisibility(player, summon);
        }

        player.getClient().announce(Field.BlowWeather.resetMapEffects());
        if (mapEffect != null) {
            if (mapEffect.getItemID() == 5120010 && player.getClient().isNightOverlayEnabled()) {
                mapEffect.sendStartData(player.getClient());
            } else if (mapEffect.getItemID() != 5120010) {
                mapEffect.sendStartData(player.getClient());
            }
        }

        player.getClient().announce(WvsContext.ForcedStat.Reset());
        if (mapid == 914000200 || mapid == 914000210 || mapid == 914000220) {
            player.getClient().announce(WvsContext.ForcedStat.aranGodlyStats());
        }

        if (mapid == 109060000) { // Hidden Street - Snowball
            player.announce(SnowBall.State(true, 0, null, null));
        }

        if (hasBoat() == 2) {
            player.getClient().announce((ContiMove.State(1, false)));
        } else if (hasBoat() == 1 && (player.getMapId() != 200090000 || player.getMapId() != 200090010)) { // During the Ride
            player.getClient().announce(ContiMove.State(2, false));
        }

        player.receivePartyMembers();
    }

    public MaplePortal findClosestPortal(Point from) {
        MaplePortal closest = null;
        double shortestDistance = Double.POSITIVE_INFINITY;
        for (MaplePortal portal : portals.values()) {
            double distance = portal.getPosition().distanceSq(from);
            if (distance < shortestDistance) {
                closest = portal;
                shortestDistance = distance;
            }
        }
        return closest;
    }

    public MaplePortal getRandomSpawnpoint() {
        List<MaplePortal> spawnPoints = new ArrayList<>();
        for (MaplePortal portal : portals.values()) {
            if (portal.getType() >= 0 && portal.getType() <= 2) {
                spawnPoints.add(portal);
            }
        }
        MaplePortal portal = spawnPoints.get(new Random().nextInt(spawnPoints.size()));
        return portal != null ? portal : getPortal(0);
    }

    public void removePlayer(MapleCharacter player) {
        try {
            for (MapleMonster monster : getMonsters()) {
                if (monster.getOwner() != null && monster.getOwner().equals(player.getName())) {
                    killMonster(monster, null, false);
                }
            }
            if (player.passenger != -1) {
                MapleCharacter passenger = this.getCharacterById(player.passenger);
                if (passenger != null) {
                    passenger.driver = -1;
                }
                announce(UserCommon.removeFollow(player.passenger, null));
                player.passenger = -1;
            }
            if (player.driver != -1) {
                MapleCharacter driver = getCharacterById(player.driver);
                if (driver != null) {
                    driver.passenger = -1;
                }
                announce(UserCommon.removeFollow(player.driver, null));
                player.driver = -1;
            }
            characters.remove(player);
            removeMapObject(player.getObjectId());
            if (!player.isHidden()) {
                announce(UserPool.UserLeaveField(player.getId()));
            } else {
                announceGM(UserPool.UserLeaveField(player.getId()));
            }
            for (MapleMonster monster : player.getControlledMonsters()) {
                monster.setController(null);
                monster.setControllerHasAggro(false);
                monster.setControllerKnowsAboutAggro(false);
                updateMonsterController(monster);
            }
            removePlayerTimer(player.getId());
            player.leaveMap();
            player.cancelMapTimeLimitTask();
            for (MapleSummon summon : player.getSummons().values()) {
                if (summon.isStationary()) {
                    player.cancelBuffStats(SecondaryStat.PUPPET);
                } else {
                    removeMapObject(summon);
                }
            }
            if (player.getDragon() != null) {
                removeMapObject(player.getDragon());
                if (player.isHidden()) {
                    this.broadcastGMMessage(player, DragonPacket.Destroy(player.getId()));
                } else {
                    this.announce(player, DragonPacket.Destroy(player.getId()));
                }
            }
        } catch (final Throwable t) {
            Logger.log(LogType.ERROR, LogFile.ACCOUNT_STUCK, t);
        }
        // getMapObjects().stream().filter(mo-> mo.getType().equals(MapleMapObjectType.DOOR)).forEach(md-> md.sendDestroyData(chr.getClient()));
    }

    /**
     * Cancels and removes any player timer. Usually added in addPlayer. used to
     * prevent duplicate timers that could be made on the same player by them
     * re-entering the map.
     *
     * @param playerId
     */
    public void removePlayerTimer(int playerId) {
        synchronized (playerTimers) {
            ScheduledFuture<?> timer = playerTimers.get(playerId);
            if (timer != null) {
                timer.cancel(true);
                timer = null;
                playerTimers.remove(playerId);
            }
        }
    }

    public void announce(MapleCharacter player, final byte[] packet) {
        for (MapleCharacter character : characters) {
            if (character.getId() != player.getId()) {
                character.getClient().announce(packet);
            }
        }
    }

    public void broadcastGMMessage(MapleCharacter player, final byte[] packet) {
        for (MapleCharacter character : characters) {
            if (character.getId() != player.getId() && character.isGM()) {
                character.getClient().announce(packet);
            }
        }
    }

    public void announce(final byte[] packet) {
        announce(null, packet, Double.POSITIVE_INFINITY, null);
    }

    public void announceGM(final byte[] packet) {
        announceGM(null, packet, Double.POSITIVE_INFINITY, null);
    }

    /**
     * Nonranged. Repeat to source according to parameter.
     *
     * @param player
     * @param packet
     * @param repeatToSource
     */
    public void announce(MapleCharacter player, final byte[] packet, boolean repeatToSource) {
        announce(repeatToSource ? null : player, packet, Double.POSITIVE_INFINITY, player.getPosition());
    }

    /**
     * Ranged and repeat according to parameters.
     *
     * @param player
     * @param packet
     * @param repeatToSource
     * @param ranged
     */
    public void announce(MapleCharacter player, final byte[] packet, boolean repeatToSource, boolean ranged) {
        announce(repeatToSource ? null : player, packet, ranged ? RANGE_DISTANCE : Double.POSITIVE_INFINITY, player.getPosition());
    }

    /**
     * Always ranged from Point.
     *
     * @param packet
     * @param rangedFrom
     */
    public void announce(final byte[] packet, Point rangedFrom) {
        announce(null, packet, RANGE_DISTANCE, rangedFrom);
    }

    /**
     * Always ranged from point. Does not repeat to source.
     *
     * @param player
     * @param packet
     * @param rangedFrom
     */
    public void announce(MapleCharacter player, final byte[] packet, Point rangedFrom) {
        announce(player, packet, RANGE_DISTANCE, rangedFrom);
    }

    private void announce(MapleCharacter player, final byte[] packet, double rangeSq, Point rangedFrom) {
        for (MapleCharacter character : characters) {
            if (player == null || character.getId() != player.getId()) {
                if (rangeSq < Double.POSITIVE_INFINITY) {
                    if (rangedFrom.distanceSq(character.getPosition()) <= rangeSq) {
                        character.getClient().announce(packet);
                    }
                } else {
                    character.getClient().announce(packet);
                }
            }
        }
    }

    private boolean isNonRangedType(MapleMapObjectType type) {
        switch (type) {
            case NPC:
            case PLAYER:
            case HIRED_MERCHANT:
            case PLAYER_NPC:
            case DRAGON:
            case AFFECTED_AREA:
            case KITE:
                return true;
            default:
                return false;
        }
    }

    private void sendObjectPlacement(MapleClient client) {
        MapleCharacter player = client.getPlayer();
        for (MapleMapObject o : mapobjects.values()) {
            if (o.getVisibleTo() >= 0 && (player.getId() != o.getVisibleTo())) {
                continue;
            }
            if (o.getType() == MapleMapObjectType.SUMMON) {
                MapleSummon summon = (MapleSummon) o;
                if (summon.getOwner() == player) {
                    if (player.getSummons().isEmpty() || !player.getSummons().containsValue(summon)) {
                        mapobjects.remove(o);
                        continue;
                    }
                }
            }
            if (isNonRangedType(o.getType())) {
                o.sendSpawnData(client);
            }
        }
        if (player != null) {
            for (MapleMapObject o : getMapObjectsInRange(player.getPosition(), RANGE_DISTANCE, rangedMapobjectTypes)) {
                if (o.getVisibleTo() >= 0 && (player.getId() != o.getVisibleTo())) {
                    continue;
                }
                if (o.getType() == MapleMapObjectType.REACTOR) {
                    if (((MapleReactor) o).isAlive()) {
                        o.sendSpawnData(player.getClient());
                        player.addVisibleMapObject(o);
                    }
                } else {
                    o.sendSpawnData(player.getClient());
                    player.addVisibleMapObject(o);
                }
            }
        }
    }

    public List<MapleMapObject> getMapObjectsInRange(Point from, double rangeSq, List<MapleMapObjectType> types) {
        List<MapleMapObject> ret = new LinkedList<>();
        for (MapleMapObject l : mapobjects.values()) {
            if (types.contains(l.getType())) {
                if (from.distanceSq(l.getPosition()) <= rangeSq) {
                    ret.add(l);
                }
            }
        }
        return ret;
    }

    public List<MapleMapObject> getMapObjectsInBox(Rectangle box, List<MapleMapObjectType> types) {
        List<MapleMapObject> ret = new LinkedList<>();
        for (MapleMapObject l : mapobjects.values()) {
            if (types.contains(l.getType())) {
                if (box.contains(l.getPosition())) {
                    ret.add(l);
                }
            }
        }
        return ret;
    }

    public void addPortal(MaplePortal myPortal) {
        portals.put(myPortal.getId(), myPortal);
    }

    public MaplePortal getPortal(String portalname) {
        for (MaplePortal port : portals.values()) {
            if (port.getName().equals(portalname)) {
                return port;
            }
        }
        return null;
    }

    public MaplePortal getPortal(byte portalid) {
        return portals.get(portalid);
    }

    public MaplePortal getPortal(int portalid) {
        return portals.get((byte) portalid);
    }

    /**
     * @param monster
     * @param mobTime
     * @param team
     */
    public void addMonsterSpawn(MapleMonster monster, int mobTime, int team) {
        Point newpos = calcPointBelow(monster.getPosition());
        newpos.y -= 1;
        SpawnPoint sp = new SpawnPoint(monster, newpos, !monster.isMobile(), mobTime, getMapData().getMobInterval(), team);
        monsterSpawn.add(sp);
        if (sp.shouldSpawn() || mobTime == -1) {// -1 does not respawn and should not either but force ONE spawn
            // spawnMonster(sp.getMonster());
        }
    }

    /**
     * @param monster
     * @param mobTime
     * @param team
     */
    public void addCustomMonsterSpawn(MapleMonster monster, int mobTime, int team) {
        Point newpos = calcPointBelow(monster.getPosition());
        newpos.y -= 1;
        SpawnPoint sp = new SpawnPoint(monster, newpos, !monster.isMobile(), mobTime, getMapData().getMobInterval(), team);
        customMonsterSpawn.add(sp);
        if (sp.shouldSpawn() || mobTime == -1) {// -1 does not respawn and should not either but force ONE spawn
            spawnMonster(sp.getMonster());
        }
    }

    public Collection<MapleCharacter> getCharacters() {
        return Collections.unmodifiableCollection(this.characters);
    }

    public MapleCharacter getCharacterById(int id) {
        for (MapleCharacter c : this.characters) {
            if (c.getId() == id) {
                return c;
            }
        }
        return null;
    }

    private void updateMapObjectVisibility(MapleCharacter player, MapleMapObject mo) {
        if (!player.isMapObjectVisible(mo)) { // monster entered view range
            if (mo.getVisibleTo() >= 0 && (mo.getVisibleTo() != player.getId())) {
                return;
            }
            if (mo.getType() == MapleMapObjectType.SUMMON || mo.getPosition().distanceSq(player.getPosition()) <= RANGE_DISTANCE) {
                player.addVisibleMapObject(mo);
                mo.sendSpawnData(player.getClient());
            }
        } else if (mo.getType() != MapleMapObjectType.SUMMON && mo.getPosition().distanceSq(player.getPosition()) > RANGE_DISTANCE) {
            player.removeVisibleMapObject(mo);
            mo.sendDestroyData(player.getClient());
        }
    }

    public void moveMonster(MapleMonster monster, Point reportedPos) {
        monster.setPosition(reportedPos);
        for (MapleCharacter chr : characters) {
            updateMapObjectVisibility(chr, monster);
        }
    }

    public void movePlayer(MapleCharacter player, Point newPosition) {
        player.getAutobanManager().setAttacksInOneSpot(0);
        player.setPosition(newPosition);
        Collection<MapleMapObject> visibleObjects = player.getVisibleMapObjects();
        if (visibleObjects == null) {
            return;
        }
        Set<MapleMapObject> remove = new HashSet<>();
        Set<MapleMapObject> update = new HashSet<>();
        for (MapleMapObject mo : visibleObjects) {
            if (mo != null) {
                if (mapobjects.get(mo.getObjectId()) == mo) {
                    update.add(mo);
                } else {
                    remove.add(mo);
                }
            }
        }
        synchronized (player.getVisibleMapObjects()) {
            for (MapleMapObject mo : update) {
                updateMapObjectVisibility(player, mo);
            }
            for (MapleMapObject mo : remove) {
                player.removeVisibleMapObject(mo);
            }
            for (MapleMapObject mo : getMapObjectsInRange(player.getPosition(), RANGE_DISTANCE, rangedMapobjectTypes)) {
                if (!player.isMapObjectVisible(mo)) {
                    mo.sendSpawnData(player.getClient());
                    player.addVisibleMapObject(mo);
                }
            }
        }
    }

    public MaplePortal findClosestSpawnpoint(Point from) {
        MaplePortal closest = null;
        double shortestDistance = Double.POSITIVE_INFINITY;
        for (MaplePortal portal : portals.values()) {
            double distance = portal.getPosition().distanceSq(from);
            if (portal.getType() >= 0 && portal.getType() <= 2 && distance < shortestDistance && portal.getTargetMapId() == 999999999) {
                closest = portal;
                shortestDistance = distance;
            }
        }
        return closest;
    }

    public Collection<MaplePortal> getPortals() {
        return Collections.unmodifiableCollection(portals.values());
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void setMuted(boolean mute) {
        isMuted = mute;
    }

    public int getSpawnedMonstersOnMap() {
        return spawnedMonstersOnMap.get();
    }

    // not really costly to keep generating imo
    public void sendNightEffect(MapleCharacter player) {
        if (mapEffect == null) {
            mapEffect = new MapleMapEffect("", 5120010);
        }
        if (player.getClient().isNightOverlayEnabled()) {
            mapEffect.sendStartData(player.getClient());
        }
        /*for(Entry<Integer, Integer> types : backgroundTypes.entrySet()){
			if(types.getValue() >= 3){ // 3 is a special number
				mc.announce(MaplePacketCreator.changeBackgroundEffect(true, types.getKey(), 0));
			}
		}*/
 /*if(!player.getClient().isBackgroundEnabled()){
			sendBackgroundEffect(player, 0, -1, true);
			return;
		}else{
			sendBackgroundEffect(player, 0, -1, false);// Reenable them all..
		}
		int keyReq = 1;
		for(int key : getMapData().getBackgroundTypes().keySet()){
			if(key >= keyReq){
				for(int i = 0; i < 10; i++){
					player.announce(MaplePacketCreator.changeBackgroundEffect(true, i, 0));
				}
			}
		}*/
        // mc.getClient().announce(MaplePacketCreator.showEffect("Weather/Night"));
    }

    public void sendDayEffect(MapleCharacter player) { // TODO
        mapEffect = null;
        player.getClient().announce(Field.BlowWeather.resetMapEffects());
        // player.getClient().announce(MaplePacketCreator.showEffect("Weather/Day"));
        /*if(!player.getClient().isBackgroundEnabled()){
			sendBackgroundEffect(player, 0, -1, true);
			return;
		}else{
			sendBackgroundEffect(player, 0, -1, false);// Reenable them all..
		}
		for(Entry<Integer, Integer> types : getMapData().getBackgroundTypes().entrySet()){
			if(types.getValue() >= 0){ // Reset them all for day
				player.announce(MaplePacketCreator.changeBackgroundEffect(false, types.getKey(), 0));
			}
		}*/
    }

    public void sendBackgroundEffect(MapleCharacter player, int layer, int type, boolean remove) {
        for (Entry<Integer, Integer> types : getMapData().getBackgroundTypes().entrySet()) {
            if (types.getValue() >= layer) {
                if (type == -1 || (type == types.getKey())) {
                    player.announce(MapLoadable.SetBackEffect(remove, player.getMap().getId(), types.getKey(), 0));
                }
            }
        }
    }

    public void removeBackgroundEffect(MapleCharacter player) {
        player.announce(MapLoadable.ClearBackEffect());
    }

    public void broadcastNightEffect() {
        for (MapleCharacter player : characters) {
            sendNightEffect(player);
        }
    }

    public void broadcastDayEffect() {
        for (MapleCharacter player : characters) {
            sendDayEffect(player);
        }
    }

    public MapleCharacter getCharacterByName(String name) {
        for (MapleCharacter player : this.characters) {
            if (player.getName().toLowerCase().equals(name.toLowerCase())) {
                return player;
            }
        }
        return null;
    }

    public void instanceMapRespawn() {
        try {
            final int numShouldSpawn = (short) ((monsterSpawn.size() - spawnedMonstersOnMap.get()));// Fking lol'd
            if (numShouldSpawn > 0) {
                List<SpawnPoint> randomSpawn = new ArrayList<>(monsterSpawn);
                Collections.shuffle(randomSpawn);
                int spawned = 0;
                for (SpawnPoint spawnPoint : randomSpawn) {
                    if (!allowedMonsters.isEmpty() && !allowedMonsters.contains(spawnPoint.getMonsterId())) {
                        continue;
                    }
                    spawnMonster(spawnPoint.getMonster());
                    spawned++;
                    if (spawned >= numShouldSpawn) {
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
        }
    }

    public void customMapRespawn() {
        try {
            final int numShouldSpawn = (short) ((customMonsterSpawn.size() - spawnedMonstersOnMap.get()));// I lol'd too lmao
            if (numShouldSpawn > 0) {
                List<SpawnPoint> randomSpawn = new ArrayList<>(customMonsterSpawn);
                Collections.shuffle(randomSpawn);
                int spawned = 0;
                for (SpawnPoint spawnPoint : randomSpawn) {
                    if (!allowedMonsters.isEmpty() && !allowedMonsters.contains(spawnPoint.getMonsterId())) {
                        continue;
                    }
                    spawnMonster(spawnPoint.getMonster());
                    spawned++;
                    if (spawned >= numShouldSpawn) {
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
        }
    }

    public void respawn() {
        try {
            if (characters.isEmpty()) {
                return;
            }
            // if(getSpawnState()) return;//TODO: ??
            double cap = monsterSpawn.size();
            // ceil to round up, be generous
            short numShouldSpawn = (short) (Math.ceil(cap) - spawnedMonstersOnMap.get());// Fking lol'd
            if (numShouldSpawn > 0) {
                List<SpawnPoint> randomSpawn = new ArrayList<>(monsterSpawn);
                Collections.shuffle(randomSpawn);
                short spawned = 0;
                for (SpawnPoint spawnPoint : randomSpawn) {
                    if (!allowedMonsters.isEmpty() && !allowedMonsters.contains(spawnPoint.getMonsterId())) {
                        continue;
                    }
                    if (spawnPoint.shouldSpawn()) {
                        spawnMonster(spawnPoint.getMonster());
                        spawned++;
                    }
                    if (spawned >= numShouldSpawn) {
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
        }
    }

    private static interface DelayedPacketCreation {

        void sendPackets(MapleClient client);
    }

    private static interface SpawnCondition {

        boolean canSpawn(MapleCharacter player);
    }

    private int hasBoat() {
        return docked ? 2 : (getMapData().hasBoat() ? 1 : 0);
    }

    public void setDocked(boolean isDocked) {
        this.docked = isDocked;
    }

    public void announceGM(MapleCharacter player, final byte[] packet, boolean repeatToSource) {
        announceGM(repeatToSource ? null : player, packet, Double.POSITIVE_INFINITY, player.getPosition());
    }

    private void announceGM(MapleCharacter player, final byte[] packet, double rangeSq, Point rangedFrom) {
        for (MapleCharacter chr : characters) {
            if (chr != player && chr.isGM()) {
                if (rangeSq < Double.POSITIVE_INFINITY) {
                    if (rangedFrom.distanceSq(chr.getPosition()) <= rangeSq) {
                        chr.getClient().announce(packet);
                    }
                } else {
                    chr.getClient().announce(packet);
                }
            }
        }
    }

    public void broadcastNONGMMessage(MapleCharacter player, final byte[] packet, boolean repeatToSource) {
        for (MapleCharacter character : characters) {
            if (character != player && !character.isGM()) {
                character.getClient().announce(packet);
            }
        }
    }

    public MapleOxQuiz getOx() {
        return ox;
    }

    public void setOx(MapleOxQuiz set) {
        this.ox = set;
    }

    public void setOxQuiz(boolean b) {
        this.isOxQuiz = b;
    }

    public boolean isOxQuiz() {
        return isOxQuiz;
    }

    private boolean hasForcedEquip() {
        return getMapData().getFieldType() == 81 || getMapData().getFieldType() == 82;
    }

    public void clearDrops(MapleCharacter player) {
        List<MapleMapObject> items = player.getMap().getMapObjectsInRange(player.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.ITEM));
        for (MapleMapObject i : items) {
            player.getMap().removeMapObject(i);
            player.getMap().announce(DropPool.LeaveField(i.getObjectId(), 0, player.getId()));
        }
    }

    public void clearDrops() {
        for (MapleMapObject i : getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.ITEM))) {
            removeMapObject(i);
            this.announce(DropPool.LeaveField(i.getObjectId(), 0, 0));
        }
    }

    public long getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(long timeLimit) {
        this.timeLimit = timeLimit;
    }

    public int getTimeLeft() {
        return (int) ((timeLimit - System.currentTimeMillis()) / 1000);
    }

    public void addMapTimer(int time) {
        timeLimit = System.currentTimeMillis() + (time * 1000);
        announce(Clock.Created(time, 2));
        mapMonitor = TimerManager.getInstance().register("addMapTimer", new Runnable() {

            @Override
            public void run() {
                if (timeLimit != 0 && timeLimit < System.currentTimeMillis()) {
                    warpEveryone(getMapData().getForcedReturnMap());
                }
                if (getCharacters().isEmpty()) {
                    resetReactors();
                    killAllMonsters();
                    clearDrops();
                    timeLimit = 0;
                    if (mapid >= 922240100 && mapid <= 922240119) {
                        toggleHiddenNPC(9001108);
                    }
                    mapMonitor.cancel(true);
                    mapMonitor = null;
                }
            }
        }, 1000);
    }

    public void resetRiceCakes() {
        this.riceCakes = 0;
    }

    public void allowSummonState(boolean b) {
        allowSummons = b;
    }

    public void allowSpawnState(boolean b) {
        allowSummons = b;
    }

    public boolean getSummonState() {
        return allowSummons;
    }

    public boolean getSpawnState() {
        return allowSummons;
    }

    public void warpEveryone(int to) {
        List<MapleCharacter> players;
        players = new ArrayList<>(getCharacters());
        for (MapleCharacter chr : players) {
            chr.changeMap(to);
        }
    }

    // BEGIN EVENTS
    public void setSnowball(int team, MapleSnowball ball) {
        switch (team) {
            case 0:
                this.snowball0 = ball;
                break;
            case 1:
                this.snowball1 = ball;
                break;
            default:
                break;
        }
    }

    public MapleSnowball getSnowball(int team) {
        switch (team) {
            case 0:
                return snowball0;
            case 1:
                return snowball1;
            default:
                return null;
        }
    }

    private boolean specialEquip() {// Maybe I shouldn't use fieldType :\
        return getMapData().getFieldType() == 4 || getMapData().getFieldType() == 19;
    }

    public void setCoconut(MapleCoconut nut) {
        this.coconut = nut;
    }

    public MapleCoconut getCoconut() {
        return coconut;
    }

    public void warpOutByTeam(int team, int mapId) {
        List<MapleCharacter> chars = new ArrayList<>(getCharacters());
        for (MapleCharacter chr : chars) {
            if (chr != null) {
                if (chr.getTeam() == team) {
                    chr.changeMap(mapId);
                }
            }
        }
    }

    public void startEvent(final MapleCharacter player) {
        if (this.mapid == 109080000 && getCoconut() == null) {
            setCoconut(new MapleCoconut(this));
            coconut.startEvent();
        } else if (this.mapid == 109040000) {
            player.setFitness(new MapleFitness(player));
            player.getFitness().startFitness();
        } else if (this.mapid == 109030101 || this.mapid == 109030201 || this.mapid == 109030301 || this.mapid == 109030401) {
            player.setOla(new MapleOla(player));
            player.getOla().startOla();
        } else if (this.mapid == 109020001 && getOx() == null) {
            setOx(new MapleOxQuiz(this));
            getOx().sendQuestion();
            setOxQuiz(true);
        } else if (this.mapid == 109060000 && getSnowball(player.getTeam()) == null) {
            setSnowball(0, new MapleSnowball(0, this));
            setSnowball(1, new MapleSnowball(1, this));
            getSnowball(player.getTeam()).startEvent();
        } else if (this.mapid == 109010000) {
            player.setJewel(new MapleFindThatJewel(player));
            player.getJewel().startJewel();
        }
    }

    public boolean eventStarted() {
        return eventstarted;
    }

    public void startEvent() {
        this.eventstarted = true;
    }

    public void setEventStarted(boolean event) {
        this.eventstarted = event;
    }

    public String getEventNPC() {
        StringBuilder sb = new StringBuilder();
        sb.append("Talk to ");
        if (mapid == 60000) {
            sb.append("Paul!");
        } else if (mapid == 104000000) {
            sb.append("Jean!");
        } else if (mapid == 200000000) {
            sb.append("Martin!");
        } else if (mapid == 220000000) {
            sb.append("Tony!");
        } else {
            return null;
        }
        return sb.toString();
    }

    public boolean hasEventNPC() {
        return this.mapid == 60000 || this.mapid == 104000000 || this.mapid == 200000000 || this.mapid == 220000000;
    }

    public boolean isStartingEventMap() {
        return this.mapid == 109040000 || this.mapid == 109020001 || this.mapid == 109010000 || this.mapid == 109030001 || this.mapid == 109030101 || this.mapid == 109030201 || this.mapid == 109030301 || this.mapid == 109030401;
    }

    public boolean isEventMap() {
        return this.mapid >= 109010000 && this.mapid < 109050000 || this.mapid > 109050001 && this.mapid <= 109090000;
    }

    public void spawnNpc(int id, int x, int y, MapleMap map) {
        MapleNPC npc = MapleLifeFactory.getNPC(id);
        if (npc != null) {
            npc.setPosition(new java.awt.Point(x, y));
            npc.setCy(y);
            npc.setRx0(x + 50);
            npc.setRx1(x - 50);
            npc.setFh(map.getMapData().getFootholds().findBelow(new java.awt.Point(x, y)).getId());
            map.addMapObject(npc);
            map.announce(NpcPool.NpcEnterField(npc));
        }
    }

    public void toggleHiddenNPC(int id) {
        for (MapleMapObject obj : mapobjects.values()) {
            if (obj.getType() == MapleMapObjectType.NPC) {
                MapleNPC npc = (MapleNPC) obj;
                if (npc.getId() == id) {
                    npc.setHide(!npc.isHidden());
                    if (!npc.isHidden()) { // Should only be hidden upon changing maps
                        announce(NpcPool.NpcEnterField(npc));
                    }
                }
            }
        }
    }

    public final void removeNpc(final int npcId) {
        MapleNPC npc = getNPCById(npcId);
        announce(NpcPool.NpcLeaveField(npc.getObjectId()));
    }

    public MapleNPC getNPCById(int id) {
        for (MapleMapObject obj : mapobjects.values()) {
            if (obj.getType() == MapleMapObjectType.NPC) {
                if (((MapleNPC) obj).getId() == id) {
                    return (MapleNPC) obj;
                }
            }
        }
        return null;
    }

    public MapleNPC getNPCByObjectId(int objectId) {
        for (MapleMapObject obj : mapobjects.values()) {
            if (obj.getType() == MapleMapObjectType.NPC) {
                if (obj.getObjectId() == objectId) {
                    return (MapleNPC) obj;
                }
            }
        }
        return null;
    }

    public final void clearAndReset(final boolean respawn) {
        killAllMonsters();
        resetReactors();
        clearDrops();
        announce(NpcPool.NpcLeaveField(-1));
        for (final MaplePortal port : portals.values()) {
            port.setPortalState(true);
        }
        if (respawn) {
            instanceMapRespawn();
            customMapRespawn();
        }
    }

    public void environmentChange(String message, int type) {
        announce(Field.FieldEffect.environmentChange(message, type));
    }

    /**
     * Reactor moving Toggle, Status for Checking and All Reset
     *
     * @param message
     * @param type
     */
    public void environmentObstacleToggle(String message, int type) {
        announce(Field.FieldObstacleOnOff(message, type));
    }

    public void environmentObstacleStatus(String message, int type) {
        announce(Field.FieldObstacleOnOffStatus(message, type));
    }

    public void environmentObstacleAllReset() {
        announce(Field.FieldObstacleAllReset());
    }

    public void setPBStatus(int status) {
        PBStatus = status;
    }

    public MapleMonster getPinkBeanSpongeID(int pbnum) {
        return this.getMonsterById(8820010 + pbnum);
    }

    public boolean isPinkBeanMapDead(int status) {
        return pinkbeanDeadMobs.size() > status;
    }

    public Collection<MapleMonster> getPinkBeanDeadMobs() {
        return pinkbeanDeadMobs;
    }

    public boolean isHorntailMapDead() {
        return horntailDeadMobs.size() > 7;
    }

    public Collection<MapleMonster> getHorntailDeadMobs() {
        return horntailDeadMobs;
    }

    public boolean isZakumMapDead() {
        return zakumDeadMobs.size() > 7;
    }

    public Collection<MapleMonster> getZakumDeadMobs() {
        return zakumDeadMobs;
    }

    public boolean getMapTag() {
        return tag;
    }

    public void setMapTag(boolean tag) {
        this.tag = tag;
    }

    public boolean isStunned() {
        return isStunned;
    }

    public void setStunned(boolean stun) {
        this.isStunned = stun;
    }

    public boolean isSeduced() {
        return isSeduced;
    }

    public void setSeduced(boolean seduce) {
        this.isSeduced = seduce;
    }

    // Holiday PQ
    public void handleSnowVigor(Item itemDrop, Point dropPos, MapleCharacter player) {
        if (itemDrop.getItemId() == 4032094 || itemDrop.getItemId() == 4032095) {
            if (dropPos.getX() >= -397 && dropPos.getX() <= 22 && dropPos.getY() == 34) {
                TimerManager.getInstance().schedule("snowVigor", new Runnable() {

                    @Override
                    public void run() {
                        int curamt = player.getClient().getChannelServer().getStoredVar(player.getMapId() + player.getEventInstance().getEm().getIntProperty("leaderID"));
                        player.dropMessage("Current Snow Vigor Amount: " + curamt);
                        if (itemDrop.getItemId() == 4032094) {
                            player.getClient().getChannelServer().setStoredVar(player.getMapId() + player.getEventInstance().getEm().getIntProperty("leaderID"), curamt + 1);
                        } else {
                            player.getClient().getChannelServer().setStoredVar(player.getMapId() + player.getEventInstance().getEm().getIntProperty("leaderID"), curamt - 1);
                        }
                        int snowmanID = 9400322;
                        MapleMap snowmanMap = player.getEventInstance().getMapInstance(player.getMapId());
                        if (player.getMapId() == 889100011) {
                            snowmanID = 9400327;
                        } else if (player.getMapId() == 889100021) {
                            snowmanID = 9400332;
                        }
                        killSnowmanFriendlies(snowmanMap, snowmanID, curamt);
                        if (curamt < 10) {
                            if (snowmanMap.getMonsterById(snowmanID) == null) {
                                snowmanMap.spawnMonsterOnGroundBelow(snowmanID, -180, 34);
                            }
                        } else if (curamt < 20) {
                            // Spawn Lv. 2
                            if (snowmanMap.getMonsterById(snowmanID + 1) == null && snowmanMap.getMonsterById(snowmanID) == null) {
                                snowmanMap.spawnMonsterOnGroundBelow(snowmanID + 1, -180, 34);
                            }
                        } else if (curamt < 25) {
                            // Spawn Lv. 3
                            if (snowmanMap.getMonsterById(snowmanID + 2) == null && snowmanMap.getMonsterById(snowmanID + 1) == null && snowmanMap.getMonsterById(snowmanID) == null) {
                                snowmanMap.spawnMonsterOnGroundBelow(snowmanID + 2, -180, 34);
                            }
                        } else if (curamt < 30) {
                            // Spawn Lv. 4
                            if (snowmanMap.getMonsterById(snowmanID + 3) == null && snowmanMap.getMonsterById(snowmanID + 2) == null && snowmanMap.getMonsterById(snowmanID + 1) == null) {
                                snowmanMap.spawnMonsterOnGroundBelow(snowmanID + 3, -180, 34);
                            }
                        } else {
                            // Spawn Lv. 5
                            if (snowmanMap.getMonsterById(snowmanID + 4) == null && snowmanMap.getMonsterById(snowmanID + 3) == null && snowmanMap.getMonsterById(snowmanID + 2) == null && snowmanMap.getMonsterById(snowmanID) == null) {
                                snowmanMap.spawnMonsterOnGroundBelow(snowmanID + 4, -180, 34);
                            }
                        }
                    }
                }, 5 * 1000);
            }
        }
    }

    public void killSnowmanFriendlies(MapleMap snowmanMap, int snowmanID, int curamt) {
        if (curamt > 29) {
            if (snowmanMap.getMonsterById(snowmanID + 3) != null) {
                snowmanMap.killFriendlies(snowmanMap.getMonsterById(snowmanID + 3));
                TimerManager.getInstance().schedule("killFriendlies1", new Runnable() {

                    @Override
                    public void run() {
                        snowmanMap.killFriendlies(snowmanMap.getMonsterById(snowmanID + 2));
                    }
                }, 3000);
                TimerManager.getInstance().schedule("killFriendlies2", new Runnable() {

                    @Override
                    public void run() {
                        snowmanMap.killFriendlies(snowmanMap.getMonsterById(snowmanID + 2));
                    }
                }, 6000);
                TimerManager.getInstance().schedule("killFriendlies3", new Runnable() {

                    @Override
                    public void run() {
                        snowmanMap.killFriendlies(snowmanMap.getMonsterById(snowmanID + 1));
                    }
                }, 9000);
                TimerManager.getInstance().schedule("killFriendlies3", new Runnable() {

                    @Override
                    public void run() {
                        snowmanMap.killFriendlies(snowmanMap.getMonsterById(snowmanID));
                    }
                }, 12000);
            }
        } else if (curamt > 24) {
            if (snowmanMap.getMonsterById(snowmanID + 2) != null) {
                snowmanMap.killFriendlies(snowmanMap.getMonsterById(snowmanID + 2));
                TimerManager.getInstance().schedule("killFriendlies4", new Runnable() {

                    @Override
                    public void run() {
                        snowmanMap.killFriendlies(snowmanMap.getMonsterById(snowmanID + 1));
                    }
                }, 3000);
                TimerManager.getInstance().schedule("killFriendlies5", new Runnable() {

                    @Override
                    public void run() {
                        snowmanMap.killFriendlies(snowmanMap.getMonsterById(snowmanID));
                    }
                }, 6000);
            }
        } else if (curamt > 19) {
            if (snowmanMap.getMonsterById(snowmanID + 1) != null) {
                snowmanMap.killFriendlies(snowmanMap.getMonsterById(snowmanID + 1));
                TimerManager.getInstance().schedule("killFriendlies6", new Runnable() {

                    @Override
                    public void run() {
                        snowmanMap.killFriendlies(snowmanMap.getMonsterById(snowmanID));
                    }
                }, 3000);
            }
        } else if (curamt > 9) {
            if (snowmanMap.getMonsterById(snowmanID) != null) {
                snowmanMap.killFriendlies(snowmanMap.getMonsterById(snowmanID));
            }
        }
    }

    public Collection<SpawnPoint> getSpawnPoints() {
        return monsterSpawn;
    }

    public void addClock(int hours, int minutes, int seconds, ScheduledFuture<?> timer) {
        clockHours = hours;
        clockMinutes = minutes;
        clockSeconds = seconds;
        clockScheduler = timer;
        if (clockHours > 0 || clockMinutes > 0 || clockSeconds > 0) {
            announce((Clock.Created(clockSeconds + (clockMinutes * 60) + (clockHours * 60 * 60), 2)));
        }
        clockTimer = TimerManager.getInstance().register("addClock", () -> {
            if (clockMinutes == 0 && clockHours > 0) {
                clockHours--;
                clockMinutes += 60;
            }
            if (clockSeconds == 0 && clockMinutes > 0) {
                clockMinutes--;
                clockSeconds += 60;
            }
            if (clockSeconds > 0) {
                clockSeconds--;
            }
        }, 1000);
    }

    public void cancelClock() {
        if (clockScheduler != null) {
            clockScheduler.cancel(true);
            clockScheduler = null;
        }
        if (clockTimer != null) {
            clockTimer.cancel(true);
            clockTimer = null;
        }
        clockHours = 0;
        clockMinutes = 0;
        clockSeconds = 0;
        announce(Clock.Destroy());
    }

    public int getClockHours() {
        return clockHours;
    }

    public int getClockMinutes() {
        return clockMinutes;
    }

    public int getClockSeconds() {
        return clockSeconds;
    }

    public Random getRandom() {
        return rand;
    }

    public boolean random() {
        return rand.nextBoolean();
    }

    public UUID getInstanceID() {
        return instanceID;
    }

    public void setInstanceID(UUID instanceID) {
        this.instanceID = instanceID;
    }

    public boolean containsABoss() {
        Collection<SpawnPoint> spawnPoints = monsterSpawn;
        if (customMonsterSpawn.size() > 0) {
            spawnPoints = customMonsterSpawn;
        }
        return spawnPoints.stream().anyMatch(sp -> sp.getFakeMonster().isBoss());
    }

    public boolean containsAnAliveBoss() {
        return getMonsters().stream().anyMatch(MapleMonster::isBoss);
    }

    public int getHighestLevelMob(boolean bossCheck) {
        Collection<SpawnPoint> spawnPoints = monsterSpawn;
        if (customMonsterSpawn.size() > 0) {
            spawnPoints = customMonsterSpawn;
        }
        if (spawnPoints.size() == 0) {
            return 1;
        }
        if (bossCheck && containsABoss()) {
            OptionalInt op = spawnPoints.stream().filter(sp -> sp.getFakeMonster().isBoss()).collect(Collectors.toList()).stream().mapToInt(sp -> sp.getFakeMonster().getLevel()).max();
            if (op.isPresent()) {
                return op.getAsInt();
            } else {
                return 1;
            }
        }
        OptionalInt op = spawnPoints.stream().filter(sp -> !sp.getFakeMonster().isBoss()).mapToInt(sp -> sp.getFakeMonster().getLevel()).max();
        if (op.isPresent()) {
            return op.getAsInt();
        }
        return 1;
    }

    public Optional<SpawnPoint> getHighestLevelMonster(boolean bossCheck) {
        Collection<SpawnPoint> spawnPoints = monsterSpawn;
        if (customMonsterSpawn.size() > 0) {
            spawnPoints = customMonsterSpawn;
        }
        if (spawnPoints.size() == 0) {
            return Optional.empty();
        }
        return spawnPoints.stream().filter(sp -> sp.getFakeMonster().getLevel() == getHighestLevelMob(bossCheck)).findFirst();
    }

    public int getAverageLevelOfMobs(boolean bossCheck) {
        Collection<SpawnPoint> spawnPoints = monsterSpawn;
        if (customMonsterSpawn.size() > 0) {
            spawnPoints = customMonsterSpawn;
        }
        if (spawnPoints.size() == 0) {
            return 0;
        }
        if (bossCheck && containsABoss()) {
            return (int) Math.ceil(spawnPoints.stream().filter(sp -> sp.getFakeMonster().isBoss()).collect(Collectors.toList()).stream().mapToInt(sp -> sp.getFakeMonster().getLevel()).average().getAsDouble());
        }
        return (int) Math.ceil(spawnPoints.stream().filter(sp -> !sp.getFakeMonster().isBoss()).mapToInt(sp -> sp.getFakeMonster().getLevel()).average().getAsDouble());
    }

    public int getLowestLevelMob(boolean bossCheck) {
        Collection<SpawnPoint> spawnPoints = monsterSpawn;
        if (customMonsterSpawn.size() > 0) {
            spawnPoints = customMonsterSpawn;
        }
        if (spawnPoints.size() == 0) {
            return 0;
        }
        if (bossCheck && containsABoss()) {
            return spawnPoints.stream().filter(sp -> sp.getFakeMonster().isBoss()).collect(Collectors.toList()).stream().mapToInt(sp -> sp.getFakeMonster().getLevel()).min().getAsInt();
        }
        return spawnPoints.stream().filter(sp -> !sp.getFakeMonster().isBoss()).mapToInt(sp -> sp.getFakeMonster().getLevel()).min().getAsInt();
    }

    public void setSpawnPointMob(boolean custom, int spawn, int monsterId) {
        Collection<SpawnPoint> monsterSpawn = Collections.synchronizedList(new LinkedList<SpawnPoint>());
        int pos = 0;
        for (SpawnPoint sp : getSpawnPoints()) {
            if (pos == spawn) {
                SpawnPoint newSP = sp;
                newSP.setMonster(monsterId);
                monsterSpawn.add(newSP);
            } else {
                monsterSpawn.add(sp);
            }
            pos++;
        }
        if (custom) {
            this.monsterSpawn = monsterSpawn;
        } else {
            this.customMonsterSpawn = monsterSpawn;
        }
    }

    public Collection<SpawnPoint> getCurrentSpawnPoints() {
        if (customMonsterSpawn.size() > 0) {
            return customMonsterSpawn;
        }
        return monsterSpawn;
    }

    public int getMobCount() {
        return getMonsters().size();
    }

    public Point getPointOfCustomFirstMob() {
        if (customMonsterSpawn.isEmpty()) {
            return new Point(0, 0);
        }
        return customMonsterSpawn.stream().findFirst().get().getPosition();
    }

    public Set<String> getLastPlayers() {
        return lastPlayers;
    }

    public MapleMapData getMapData() {
        return mapData;
    }

    public void spawnDoor(final MapleDoor door) {
        spawnAndAddRangedMapObject(door, new DelayedPacketCreation() {

            @Override
            public void sendPackets(MapleClient c) {
                MapleCharacter owner = door.getOwnerInstance();
                if (owner == null) {
                    return;
                }
                c.announce(TownPortalPool.Created(owner.getId(), door.getTargetPosition(), false));
                c.announce(WvsContext.TownPortal.Created(door.getTown().getId(), door.getTarget().getId(), door.getSkillID(), door.getTargetPosition()));
                if (owner.isInParty()) {
                    // c.announce(CWvsContext.Party.partyPortal(door.getTown().getId(), door.getTarget().getId(), door.getSkillID(), door.getTargetPosition()));
                }
                c.announce(WvsContext.enableActions());
            }
        }, new SpawnCondition() {

            @Override
            public boolean canSpawn(MapleCharacter chr) {
                return chr.getMapId() == door.getTarget().getId();
            }
        });
    }

    public void addAllowedMonster(int monsterId) {
        allowedMonsters.add(monsterId);
    }

    public void clearAllowedMonsters() {
        allowedMonsters.clear();
    }

    public MapleMapEffect getMapEffect() {
        return this.mapEffect;
    }
}
