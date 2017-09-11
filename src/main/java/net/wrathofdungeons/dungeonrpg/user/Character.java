package net.wrathofdungeons.dungeonrpg.user;

import net.wrathofdungeons.dungeonapi.MySQLManager;

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
                this.creationTime = rs.getTimestamp("creationTime");
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

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public Timestamp getLastLogin() {
        return lastLogin;
    }

    public void saveData(){

    }
}
