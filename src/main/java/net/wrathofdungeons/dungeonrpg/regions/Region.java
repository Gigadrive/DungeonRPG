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
    public static ArrayList<Region> STORAGE = new ArrayList<Region>();

    public static void init(){
        STORAGE.clear();

        DungeonAPI.async(() -> {
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
        });
    }

    public static Region getRegion(int id){
        for(Region r : STORAGE){
            if(r.getID() == id) return r;
        }

        return null;
    }

    private int id;
    private int mobDataID;
    private int mobLimit;
    private ArrayList<RegionLocation> locations;

    private boolean mayActivateMobs = true;
    private BukkitTask activationTimer = null;

    public Region(int id){
        DungeonAPI.async(() -> {
            try {
                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `regions` WHERE `id` = ?");
                ps.setInt(1,id);
                ResultSet rs = ps.executeQuery();

                if(rs.first()){
                    this.id = rs.getInt("id");
                    this.mobDataID = rs.getInt("mobDataID");
                    this.mobLimit = rs.getInt("mobLimit");
                    String locationString = rs.getString("locations");
                    Gson gson = new Gson();
                    if(locationString != null){
                        this.locations = gson.fromJson(locationString, new TypeToken<ArrayList<RegionLocation>>(){}.getType());
                    } else {
                        this.locations = new ArrayList<RegionLocation>();
                    }

                    STORAGE.add(this);
                }

                MySQLManager.getInstance().closeResources(rs,ps);
            } catch(Exception e){
                e.printStackTrace();
            }
        });
    }

    public int getID() {
        return id;
    }

    public MobData getMobData(){
        return MobData.getData(mobDataID);
    }

    public int getMobLimit(){
        return mobLimit;
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
            return DungeonAPI.blocksFromTwoPointsMaximized(getLocations(RegionLocationType.MOB_ACTIVATION_1).get(0).toBukkitLocation(),getLocations(RegionLocationType.MOB_ACTIVATION_2).get(0).toBukkitLocation());
        } else {
            return null;
        }
    }

    public RegionLocationType getType(Location loc){
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
        if(mayActivateMobs && activationTimer == null){
            setMayActivateMobs(false);

            activationTimer = new BukkitRunnable(){
                @Override
                public void run() {
                    setMayActivateMobs(true);
                    cancel();
                    activationTimer = null;
                }
            }.runTaskLater(DungeonRPG.getInstance(),10*20);
        }
    }

    public BukkitTask getActivationTimer() {
        return activationTimer;
    }

    public void saveData(){
        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("UPDATE `regions` SET `locations` = ?  WHERE `id` = ?");
            ps.setString(1,new Gson().toJson(getLocations()));
            ps.setInt(2,getID());
            ps.executeUpdate();
            ps.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void reload(){
        STORAGE.remove(this);
        new Region(1);
    }
}
