package net.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import client.MapleCharacter;
import client.MapleClient;
import client.RSSkill;
import constants.ServerConstants;
import net.MTSAuction;
import net.server.channel.Channel;
import net.server.world.World;
import server.MapleWedding;
import server.TimerManager;
import tools.AutoJCE;
import tools.DatabaseConnection;
import tools.Pair;
import tools.Triple;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

public class Server implements Runnable {

    private List<Map<Integer, String>> channels = new LinkedList<>();
    private List<World> worlds = new ArrayList<>();
    private Properties subnetInfo = new Properties();
    private static Server instance = null;
    private List<Pair<Integer, String>> worldRecommendedList = new LinkedList<>();
    private boolean online = false;
    public static long uptime = System.currentTimeMillis();
    private List<MapleWedding> weddings = new LinkedList<>();
    public static int highestWeddingID = 0;
    public boolean shuttingDown = false;

    @Deprecated
    public static Server getInstance() {
        /*
         if(instance == null){
         instance = new Server();
         }
         */
        return null;
    }

    public boolean isOnline() {
        return online;
    }

    public List<Pair<Integer, String>> worldRecommendedList() {
        return worldRecommendedList;
    }

    public void removeChannel(int worldid, int channel) {
        channels.remove(channel);
        World world = worlds.get(worldid);
        if (world != null) {
            world.removeChannel(channel);
        }
    }

    public Channel getChannel(int world, int channel) {
        return worlds.get(world).getChannel(channel);
    }

    public MapleClient getClientGlobal(int id) {
        for (World world : getWorlds()) {
            for (MapleCharacter player : world.getAllCharacters()) {
                if (player.getAccountID() == id) {
                    return player.getClient();
                }
            }
        }
        return null;
    }

    public MapleClient getClientGlobal(String id) {
        for (World world : getWorlds()) {
            for (MapleCharacter player : world.getAllCharacters()) {
                if (player.getClient().getAccountName().equalsIgnoreCase(id)) {
                    return player.getClient();
                }
            }
        }
        return null;
    }

    public List<Channel> getChannelsFromWorld(int world) {
        return worlds.get(world).getChannels();
    }

    public List<Channel> getAllChannels() {
        List<Channel> channelz = new ArrayList<>();
        for (World world : worlds) {
            for (Channel ch : world.getChannels()) {
                channelz.add(ch);
            }
        }
        return channelz;
    }

    public String getIP(int world, int channel) {
        return channels.get(world).get(channel);
    }

    @Override
    public void run() {
        System.setProperty("wzpath", "wz");
        System.out.println("MeoMS v" + ServerConstants.VERSION + " starting up.");
        AutoJCE.removeCryptographyRestrictions();
        DatabaseConnection.getConnection();
        Connection c = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = c.prepareStatement("UPDATE accounts SET loggedin = 0");
            ps.executeUpdate();
            ps.close();
            ps = c.prepareStatement("UPDATE characters SET HasMerchant = 0");
            ps.executeUpdate();
            ps.close();
        } catch (SQLException sqle) {
            Logger.log(LogType.ERROR, LogFile.EXCEPTION, sqle);
        }
        // tMan.register(tMan.purge(), 300000);//Purging ftw...
        TimerManager.getInstance().register("rankingWorker", new RankingWorker(), ServerConstants.RANKING_INTERVAL);
        TimerManager.getInstance().register("MTSAuction", new MTSAuction(), ServerConstants.MTS_AUCTION_INTERVAL);
        try (ResultSet rs = DatabaseConnection.getConnection().createStatement().executeQuery("SELECT id FROM weddings where id > 0")) {
            while (rs.next()) {
                MapleWedding wedding = new MapleWedding(rs.getInt("id"));
                wedding.loadFromDB();
                addWedding(wedding);
            }
        } catch (Exception ex) {
            Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
        }
        System.out.println("MeoMS is now online.");
        online = true;
        /*
         TimerManager.getInstance().register(()-> {// #1 player skill npcs
         List<Triple<RSSkill, String, Byte>> highestLevels = new
         ArrayList<Triple<RSSkill, String, Byte>>();
         List<Triple<RSSkill, String, Long>> highestExp = new
         ArrayList<Triple<RSSkill, String, Long>>();
         try(PreparedStatement ps =
         DatabaseConnection.getConnection().prepareStatement("SELECT name,
         rsSkillLevel, rsSkillExp FROM characters")){
         try(ResultSet rs = ps.executeQuery()){
         while(rs.next()){
         Map<RSSkill, Byte> level = new HashMap<RSSkill, Byte>();
         Map<RSSkill, Long> exp = new HashMap<RSSkill, Long>();
         String skillIn = rs.getString("rsSkillLevel");
         if(skillIn != null && skillIn.length() > 0){
         for(String info : skillIn.split(",")){
         String[] split = info.split("=");
         level.put(RSSkill.valueOf(split[0]), Byte.valueOf(split[1]));
         }
         }
         skillIn = rs.getString("rsSkillExp");
         if(skillIn != null && skillIn.length() > 0){
         for(String info : skillIn.split(",")){
         String[] split = info.split("=");
         exp.put(RSSkill.valueOf(split[0]), Long.parseLong(split[1]));
         }
         }
         for(RSSkill skill : level.keySet()){
         Triple<RSSkill, String, Byte> triple = null;
         int i = 0;
         for(i = 0; i < highestLevels.size(); i++){
         Triple<RSSkill, String, Byte> t = highestLevels.get(i);
         if(t.left.equals(skill)){
         triple = t;
         break;
         }
         }
         if(triple != null){
         byte lvl = level.get(skill);
         if(lvl >= triple.right){
         Triple<RSSkill, String, Long> triple2 = null;
         int i2 = 0;
         for(i2 = 0; i2 < highestExp.size(); i2++){
         Triple<RSSkill, String, Long> t = highestExp.get(i2);
         if(t.left.equals(skill)){
         triple2 = t;
         break;
         }
         }
         if(triple2 != null){
         Long exp2 = exp.get(skill);
         if(lvl == triple.right && exp2 < triple2.right || lvl < triple.right){
         continue;
         }
         triple.right = lvl;
         triple.mid = rs.getString("name");
         highestLevels.set(i, triple);
         highestExp.set(i2, new Triple<RSSkill, String, Long>(skill,
         rs.getString("name"), exp2));
         }else{
         triple.right = lvl;
         triple.mid = rs.getString("name");
         Long exp2 = exp.get(skill);
         if(exp2 != null){
         highestLevels.set(i, triple);
         highestExp.add(new Triple<RSSkill, String, Long>(skill,
         rs.getString("name"), exp2));
         }
         }
         }
         }else{
         triple = new Triple<RSSkill, String, Byte>(skill, rs.getString("name"),
         level.get(skill));
         highestLevels.add(triple);
         }
         }
         }
         }
         }catch(SQLException ex){
         FilePrinter.printError(FilePrinter.EXCEPTION, ex);
         }
         List<Triple<RSSkill, String, Byte>> updatedHighestLevels = new
         ArrayList<Triple<RSSkill, String, Byte>>();
         if(lastHighestLevels.size() > 0){
         for(int i = 0; i < highestLevels.size(); i++){
         Triple<RSSkill, String, Byte> t = highestLevels.get(i);
         for(Triple<RSSkill, String, Byte> last : lastHighestLevels){
         if(t.left.equals(last.left)){
         if(!t.mid.equals(last.mid)){
         updatedHighestLevels.add(t);
         }
         }
         }
         }
         lastHighestLevels = highestLevels;
         lastHighestExp = highestExp;
         }else{
         lastHighestLevels = highestLevels;
         lastHighestExp = highestExp;
         updatedHighestLevels = highestLevels;
         }
         Map<String, MapleCharacter> chrs = new HashMap<String,
         MapleCharacter>();
         for(Triple<RSSkill, String, Byte> t : updatedHighestLevels){
         for(World world : Server.getInstance().getWorlds()){
         for(Channel ch : world.getChannels()){
         int mapid = 100000204;
         MapleMap map = ch.getMap(mapid);
         for(MapleMapObject mpo : map.getMapObjects()){
         if(mpo instanceof PlayerNPC){
         PlayerNPC npc = (PlayerNPC) mpo;
         if(npc.getName().contains(t.left.name())){
         MapleCharacter chr = chrs.get(t.mid);
         if(chr == null){
         chr = world.getCharacterByName(t.mid);
         if(chr == null){
         try(PreparedStatement ps =
         DatabaseConnection.getConnection().prepareStatement("SELECT id FROM
         characters WHERE name = ?")){
         ps.setString(1, t.mid);
         try(ResultSet rs = ps.executeQuery()){
         if(rs.next()){
         chr = MapleCharacter.loadCharFromDB(rs.getInt("id"), null, false);
         }
         }
         }catch(SQLException ex){
         FilePrinter.printError(FilePrinter.EXCEPTION, ex);
         }
         }
         }
         if(chr != null){
         npc.setName(t.left.name() + " Master");
         npc.setGender((byte) chr.getGender());
         npc.setHair(chr.getHair());
         npc.setFace(chr.getFace());
         npc.setSkin((byte) chr.getSkinColor().getId());
         npc.setEquipsTo(chr);
         map.removeMapObject(mpo);
         map.addMapObject(npc);
         map.broadcastMessage(MaplePacketCreator.getPlayerNPC(npc));
         npc.updateMainStuffInDB();
         npc.saveEquipsToDB();
         break;
         }
         }
         }
         }
         }
         }
         }
         //Skill Ranking

         }, 1 * 60 * 60 * 1000);
         */
    }

    List<Triple<RSSkill, String, Byte>> lastHighestLevels = new ArrayList<Triple<RSSkill, String, Byte>>();
    List<Triple<RSSkill, String, Long>> lastHighestExp = new ArrayList<Triple<RSSkill, String, Long>>();

    public List<Triple<RSSkill, String, Byte>> getHighestRSLevels() {
        return lastHighestLevels;
    }

    public List<Triple<RSSkill, String, Long>> getHighestRSExp() {
        return lastHighestExp;
    }

    public void shutdown() {
        System.out.println("Server offline.");
        System.exit(0);// BOEIEND :D
    }

    public Properties getSubnetInfo() {
        return subnetInfo;
    }

    public Set<Integer> getChannelServer(int world) {
        return new HashSet<>(channels.get(world).keySet());
    }

    public byte getHighestChannelId() {
        byte highest = 0;
        for (Iterator<Integer> it = channels.get(0).keySet().iterator(); it.hasNext();) {
            Integer channel = it.next();
            if (channel != null && channel.intValue() > highest) {
                highest = channel.byteValue();
            }
        }
        return highest;
    }

    public void broadcastMessage(final byte[] packet) {
        for (World world : getWorlds()) {
            world.broadcastPacket(packet);
        }
    }

    public void broadcastGMMessage(final byte[] packet) {
        for (World world : getWorlds()) {
            world.broadcastGMPacket(packet);
        }
    }

    public boolean isGmOnline() {
        for (World world : getWorlds()) {
            for (Channel ch : world.getChannels()) {
                for (MapleCharacter player : ch.getPlayerStorage().getAllCharacters()) {
                    if (player.isGM()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public World getWorld(int id) {
        return worlds.get(id);
    }

    public List<World> getWorlds() {
        return worlds;
    }

    public MapleWedding getWeddingByID(int marriageID) {
        for (int i = 0; i < weddings.size(); i++) {
            MapleWedding wedding = weddings.get(i);
            if (wedding.getMarriageID() == marriageID) {
                wedding.index = i;
                return wedding;
            }
        }
        return null;
    }

    public MapleWedding getWedding(int player) {
        return getWedding(player, 0);
    }

    public MapleWedding getWedding(int player, int requiredStatus) {
        for (int i = 0; i < weddings.size(); i++) {
            MapleWedding wedding = weddings.get(i);
            if (wedding.getStatus() >= requiredStatus) {
                if (wedding.getPlayer1() == player || wedding.getPlayer2() == player) {
                    wedding.index = i;
                    return wedding;
                }
            }
        }
        return null;
    }

    public void addWedding(MapleWedding wedding) {
        if (wedding.index == -1) {
            weddings.add(wedding);
        } else {
            weddings.set(wedding.index, wedding);
        }
    }
}
