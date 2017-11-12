package net.wrathofdungeons.dungeonrpg.professions;

import net.wrathofdungeons.dungeonapi.MySQLManager;
import net.wrathofdungeons.dungeonapi.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.UUID;

public class Ore {
    public static HashMap<Integer,Ore> STORAGE = new HashMap<Integer,Ore>();

    public static void init(){
        STORAGE.clear();

        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `ores`");
            ResultSet rs = ps.executeQuery();

            while(rs.next()) new Ore(rs.getInt("id"));

            MySQLManager.getInstance().closeResources(rs,ps);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public static Ore getOre(int id){
        return STORAGE.getOrDefault(id,null);
    }

    public static Ore getOre(Location loc){
        for(Ore ore : STORAGE.values()){
            if(Util.isLocationEqual(loc,ore.getLocation())) return ore;
        }

        return null;
    }

    private int id;
    private Location location;
    private OreLevel level;
    private Timestamp timeAdded;
    private UUID addedBy;
    private boolean isAvailable = true;

    public int timer = 0;

    public Ore(int id){
        if(STORAGE.containsKey(id)) return;

        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `ores` WHERE `id` = ?");
            ps.setInt(1,id);

            ResultSet rs = ps.executeQuery();
            if(rs.first()){
                this.id = rs.getInt("id");
                this.location = new Location(Bukkit.getWorld(rs.getString("location.world")),rs.getInt("location.x"),rs.getInt("location.y"),rs.getInt("location.z"));
                this.level = OreLevel.fromID(rs.getInt("level"));
                this.timeAdded = rs.getTimestamp("time");
                this.addedBy = UUID.fromString(rs.getString("addedBy"));

                location.getBlock().setType(getLevel().getBlock());

                STORAGE.put(id,this);
            }

            MySQLManager.getInstance().closeResources(rs,ps);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public OreLevel getLevel() {
        return level;
    }

    public Timestamp getTimeAdded() {
        return timeAdded;
    }

    public UUID getAddedBy() {
        return addedBy;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }
}
