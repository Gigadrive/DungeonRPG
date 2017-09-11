package net.wrathofdungeons.dungeonrpg.user;

import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.MySQLManager;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

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

                if(lastLogin == null) storedLocation = DungeonRPG.getStartLocation();
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

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public Timestamp getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Timestamp t){
        this.lastLogin = t;
    }

    public void saveData(Player p){
        DungeonAPI.async(() -> {
            try {
                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("UPDATE `characters` SET `level` = ?, `exp` = ?, `location.world` = ?, `location.x` = ?, `location.y` = ?, `location.z` = ?, `location.yaw` = ?, `location.pitch` = ?, `lastLogin` = ? WHERE `id` = ?");
                ps.setInt(1,getLevel());
                ps.setDouble(2,getExp());
                ps.setString(3,p.getLocation().getWorld().getName());
                ps.setDouble(4,p.getLocation().getX());
                ps.setDouble(5,p.getLocation().getY());
                ps.setDouble(6,p.getLocation().getZ());
                ps.setFloat(7,p.getLocation().getYaw());
                ps.setFloat(8,p.getLocation().getPitch());
                ps.setTimestamp(9,lastLogin);
                ps.setInt(10,getId());
                ps.executeUpdate();
                ps.close();
            } catch(Exception e){
                e.printStackTrace();
            }
        });
    }
}
