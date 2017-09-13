package net.wrathofdungeons.dungeonrpg.regions;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.MySQLManager;
import net.wrathofdungeons.dungeonrpg.mobs.MobData;

import javax.naming.ldap.PagedResultsControl;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class Region {
    public static ArrayList<Region> STORAGE = new ArrayList<Region>();

    public static void init(){
        STORAGE.clear();

        DungeonAPI.async(() -> {
            try {
                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `regions`");
                ResultSet rs = ps.executeQuery();

                while(rs.next()){
                    new Region(rs.getInt("id"),rs.getString("locations"));
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

    public Region(int id, String locationString){
        this.id = id;
        Gson gson = new Gson();
        if(locationString != null){
            this.locations = gson.fromJson(locationString, new TypeToken<ArrayList<RegionLocation>>(){}.getType());
        } else {
            this.locations = new ArrayList<RegionLocation>();
        }

        STORAGE.add(this);
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
