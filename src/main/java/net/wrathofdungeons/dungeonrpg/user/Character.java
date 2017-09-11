package net.wrathofdungeons.dungeonrpg.user;

import net.wrathofdungeons.dungeonapi.MySQLManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import javax.naming.ldap.PagedResultsControl;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.UUID;

public class Character {
    private int id;
    private UUID owner;
    private RPGClass rpgClass;
    private int level;
    private double exp;
    private Location storedLocation;
    private Timestamp creationTime;
    private Timestamp lastLogin;

    public Character(int id){
        this.id = id;

        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `characters` WHERE `id` = ?");
            ps.setInt(1,id);

            ResultSet rs = ps.executeQuery();
            if(rs.first()){
                this.owner = UUID.fromString(rs.getString("uuid"));
                this.rpgClass = RPGClass.valueOf(rs.getString("class"));
                this.level = rs.getInt("level");
                this.exp = rs.getDouble("exp");
                this.storedLocation = new Location(Bukkit.getWorld(rs.getString("location.world")),rs.getDouble("location.x"),rs.getDouble("location.y"),rs.getDouble("location.z"),rs.getFloat("location.yaw"),rs.getFloat("location.pitch"));
                this.creationTime = rs.getTimestamp("time");
                this.lastLogin = rs.getTimestamp("lastLogin");
            }

            MySQLManager.getInstance().closeResources(rs,ps);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public UUID getOwner() {
        return owner;
    }

    public RPGClass getRpgClass() {
        return rpgClass;
    }

    public int getLevel() {
        return level;
    }

    public double getExp() {
        return exp;
    }

    public Location getStoredLocation() {
        return storedLocation;
    }

    public void play(){

    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public Timestamp getLastLogin() {
        return lastLogin;
    }

    public void saveData(){

    }
}
