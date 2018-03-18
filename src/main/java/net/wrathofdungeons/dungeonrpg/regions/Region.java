package net.wrathofdungeons.dungeonrpg.regions;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.MySQLManager;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.mobs.MobData;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.naming.ldap.PagedResultsControl;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Region {
    private static ArrayList<Region> STORAGE = new ArrayList<Region>();

    public static ArrayList<Region> getRegions(){
        return getRegions(true);
    }

    public static ArrayList<Region> getRegions(boolean skipCopy){
        ArrayList<Region> a = new ArrayList<Region>();

        for(Region region : STORAGE){
            if(skipCopy && region.isCopy()) continue;

            a.add(region);
        }

        return a;
    }

    public static ArrayList<Region> getRegions(String worldName){
        ArrayList<Region> a = new ArrayList<Region>();

        regionLoop:
        for(Region region : STORAGE){
            if(region.isCopy()) continue;

            for(RegionLocation location : region.getLocations()){
                if(location.world.equalsIgnoreCase(worldName)){
                    a.add(region);
                    continue regionLoop;
                }
            }
        }

        return a;
    }

    public static ArrayList<Region> getStorage() {
        return STORAGE;
    }

    public static void init(){
        STORAGE.clear();

        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `regions`");
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                new Region(rs.getInt("id"));
            }

            MySQLManager.getInstance().closeResources(rs,ps);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public static Region getRegion(int id){
        return getRegion(id,true);
    }

    public static Region getRegion(int id, boolean skipCopy){
        for(Region r : STORAGE){
            if(r.getID() == id){
                if(skipCopy && r.isCopy())
                    continue;

                return r;
            }
        }

        return null;
    }

    private int id;
    private int mobDataID;
    private int mobLimit;
    private ArrayList<RegionLocation> locations;
    private String entranceTitleTop;
    private String entranceTitleBottom;
    private int cooldown;
    private int spawnChance;
    private boolean active;
    private RegionData additionalData;

    private boolean mayActivateMobs = true;
    private BukkitTask activationTimer = null;

    private boolean copy = false;

    public Region(int id){
        this.id = id;

        l();
    }

    public Region(int id, boolean isCopy){
        this.id = id;
        this.copy = isCopy;

        l();
    }

    private void l(){
        if(!isCopy()){
            try {
                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `regions` WHERE `id` = ?");
                ps.setInt(1,id);
                ResultSet rs = ps.executeQuery();

                if(rs.first()){
                    this.id = rs.getInt("id");
                    this.mobDataID = rs.getInt("mobDataID");
                    this.mobLimit = rs.getInt("mobLimit");
                    this.entranceTitleTop = rs.getString("entranceTitle.top");
                    this.entranceTitleBottom = rs.getString("entranceTitle.bottom");
                    this.cooldown = rs.getInt("cooldown");
                    this.spawnChance = rs.getInt("spawnChance");
                    String locationString = rs.getString("locations");
                    this.active = rs.getBoolean("active");
                    Gson gson = DungeonAPI.GSON;
                    if(locationString != null){
                        this.locations = gson.fromJson(locationString, new TypeToken<ArrayList<RegionLocation>>(){}.getType());
                    } else {
                        this.locations = new ArrayList<RegionLocation>();
                    }

                    String dataString = rs.getString("additionalData");
                    if(dataString != null){
                        this.additionalData = gson.fromJson(dataString,RegionData.class);
                    } else {
                        this.additionalData = new RegionData();
                    }

                    STORAGE.add(this);
                }

                MySQLManager.getInstance().closeResources(rs,ps);
            } catch(Exception e){
                e.printStackTrace();
            }
        } else {
            STORAGE.add(this);
        }
    }

    public int getID() {
        return id;
    }

    public MobData getMobData(){
        return MobData.getData(mobDataID);
    }

    public void setMobLimit(int mobLimit) {
        this.mobLimit = mobLimit;
    }

    public void setMobDataID(int mobDataID) {
        this.mobDataID = mobDataID;
    }

    public void setMobData(MobData data) {
        this.mobDataID = data != null ? data.getId() : 0;
    }

    public int getMobLimit(){
        return mobLimit;
    }

    public void setEntranceTitleTop(String entranceTitleTop) {
        this.entranceTitleTop = entranceTitleTop;
    }

    public String getEntranceTitleTop() {
        return entranceTitleTop;
    }

    public void setEntranceTitleBottom(String entranceTitleBottom) {
        this.entranceTitleBottom = entranceTitleBottom;
    }

    public String getEntranceTitleBottom() {
        return entranceTitleBottom;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public int getSpawnChance() {
        return spawnChance;
    }

    public void setSpawnChance(int spawnChance) {
        this.spawnChance = spawnChance;
    }

    public boolean isCopy() {
        return copy;
    }

    public void setCopy(boolean copy) {
        this.copy = copy;
    }

    public ArrayList<RegionLocation> getLocations() {
        return locations;
    }

    public ArrayList<RegionLocation> getLocations(RegionLocationType type) {
        ArrayList<RegionLocation> a = new ArrayList<RegionLocation>();

        for(RegionLocation l : getLocations()){
            if(l.type == type) a.add(l);
        }

        return a;
    }

    public ArrayList<RegionLocation> getRandomizedLocations() {
        ArrayList<RegionLocation> a = new ArrayList<RegionLocation>();

        a.addAll(getLocations());

        Collections.shuffle(a);

        return a;
    }

    public ArrayList<RegionLocation> getRandomizedLocations(RegionLocationType type) {
        ArrayList<RegionLocation> a = new ArrayList<RegionLocation>();

        for(RegionLocation l : getLocations()){
            if(l.type == type) a.add(l);
        }

        Collections.shuffle(a);

        return a;
    }

    public ArrayList<CustomEntity> getEntitiesSpawned(){
        ArrayList<CustomEntity> a = new ArrayList<CustomEntity>();

        for(CustomEntity e : CustomEntity.STORAGE.values()){
            if(e.getOriginRegion() == this) a.add(e);
        }

        return a;
    }

    public ArrayList<RegionLocation> getLocations(RegionLocationType type, int limit) {
        ArrayList<RegionLocation> a = new ArrayList<RegionLocation>();

        Collections.shuffle(getLocations());

        for(RegionLocation l : getLocations()){
            if(limit == 0){
                break;
            } else {
                if(l.type == type){
                    a.add(l);
                    limit--;
                }
            }
        }

        return a;
    }

    public List<Block> getMobActivationRegion(){
        if(getLocations(RegionLocationType.MOB_ACTIVATION_1).size() > 0 && getLocations(RegionLocationType.MOB_ACTIVATION_2).size() > 0){
            return DungeonAPI.blocksFromTwoPointsMaximized(getLocations(RegionLocationType.MOB_ACTIVATION_1,1).get(0).toBukkitLocation(),getLocations(RegionLocationType.MOB_ACTIVATION_2,1).get(0).toBukkitLocation());
        } else {
            return null;
        }
    }

    public boolean isInRegion(Location loc){
        if(getLocations(RegionLocationType.MOB_ACTIVATION_1).size() > 0 && getLocations(RegionLocationType.MOB_ACTIVATION_2).size() > 0){
            RegionLocation loc1 = getLocations(RegionLocationType.MOB_ACTIVATION_1,1).get(0);
            RegionLocation loc2 = getLocations(RegionLocationType.MOB_ACTIVATION_2,1).get(0);

            return loc.getWorld().getName().equals(loc1.world) && loc.getWorld().getName().equals(loc2.world) && DungeonAPI.isInRectangleMaximized(loc,loc1.toBukkitLocation(),loc2.toBukkitLocation());
        } else {
            return false;
        }
    }

    public String getWorld(){
        return getLocations().size() == 0 ? null : getRandomizedLocations().get(0).world;
    }

    public RegionLocationType getType(Location loc){
        loc = loc.getBlock().getLocation();
        RegionLocationType type = null;

        for(RegionLocation l : getLocations()){
            if(Util.isLocationEqual(l.toBukkitLocation(),loc)) type = l.type;
        }

        return type;
    }

    public boolean hasLocation(Location loc){
        return getType(loc) != null;
    }

    public boolean mayActivateMobs(){
        return mayActivateMobs;
    }

    public void setMayActivateMobs(boolean b){
        this.mayActivateMobs = b;
    }

    public void startMobActivationTimer(){
        if(cooldown <= -1){
            setMayActivateMobs(false);

            return;
        }

        if(mayActivateMobs && activationTimer == null){
            setMayActivateMobs(false);

            if(cooldown >= 0){
                activationTimer = new BukkitRunnable(){
                    @Override
                    public void run() {
                        setMayActivateMobs(true);
                        cancel();
                        activationTimer = null;
                    }
                }.runTaskLater(DungeonRPG.getInstance(),(cooldown > 0 ? cooldown : 10)*20);
            }
        }
    }

    public BukkitTask getActivationTimer() {
        return activationTimer;
    }

    public void setLocations(ArrayList<RegionLocation> locations) {
        this.locations = locations;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void saveData(){
        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("UPDATE `regions` SET `locations` = ?, `active` = ?, `mobDataID` = ?, `mobLimit` = ?, `entranceTitle.top` = ?, `entranceTitle.bottom` = ?, `cooldown` = ?, `spawnChance` = ?, `additionalData` = ? WHERE `id` = ?");
            ps.setString(1,new Gson().toJson(getLocations()));
            ps.setBoolean(2,active);
            ps.setInt(3,mobDataID);
            ps.setInt(4,mobLimit);
            ps.setString(5,entranceTitleTop);
            ps.setString(6,entranceTitleBottom);
            ps.setInt(7,cooldown);
            ps.setInt(8,spawnChance);
            ps.setString(9,DungeonAPI.GSON.toJson(additionalData));
            ps.setInt(10,getID());
            ps.executeUpdate();
            ps.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public RegionData getAdditionalData() {
        if(additionalData == null) additionalData = new RegionData();

        return additionalData;
    }

    public void setAdditionalData(RegionData additionalData) {
        this.additionalData = additionalData;
    }

    public void reload(){
        STORAGE.remove(this);
        new Region(id);
    }

    public Region clone(String worldName){
        Region region = new Region(-1,true);

        region.setMobData(getMobData());
        region.setMobLimit(getMobLimit());

        ArrayList<RegionLocation> locations = new ArrayList<RegionLocation>();
        for(RegionLocation l : getLocations()){
            RegionLocation loc = new RegionLocation();

            loc.type = l.type;
            loc.world = worldName;
            loc.x = l.x;
            loc.y = l.y;
            loc.z = l.z;
            loc.yaw = l.yaw;
            loc.pitch = l.pitch;
            locations.add(loc);
        }

        region.setLocations(locations);
        region.setEntranceTitleTop(getEntranceTitleTop());
        region.setEntranceTitleBottom(getEntranceTitleBottom());
        region.setCooldown(getCooldown());
        region.setSpawnChance(getSpawnChance());
        region.setActive(isActive());
        region.setAdditionalData(getAdditionalData());
        region.setCopy(true);

        return region;
    }

    public static RegionLocationType getOverallType(Location loc){
        for(Region r : STORAGE) if(r.getType(loc) != null) return r.getType(loc);

        return null;
    }
}
