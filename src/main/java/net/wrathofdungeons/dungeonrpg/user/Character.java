package net.wrathofdungeons.dungeonrpg.user;

import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.MySQLManager;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.inv.CharacterSelectionMenu;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.items.PlayerInventory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.naming.ldap.PagedResultsControl;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.UUID;

public class Character {
    private int id;
    private UUID owner;
    private RPGClass rpgClass;
    private int level;
    private double exp;
    private Location storedLocation;
    private PlayerInventory storedInventory;
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
                if(rs.getString("inventory") != null) this.storedInventory = PlayerInventory.fromString(rs.getString("inventory"));
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

    public void setLevel(int level){
        this.level = level;
    }

    public double getExp() {
        return exp;
    }

    public void setExp(double exp){
        this.exp = exp;
    }

    public Location getStoredLocation() {
        return storedLocation;
    }

    public PlayerInventory getStoredInventory() {
        return storedInventory;
    }

    public PlayerInventory getConvertedInventory(Player p){
        return PlayerInventory.fromInventory(p);
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public Timestamp getLastLogin() {
        return lastLogin;
    }

    public boolean mayGetEXP(){
        if(this.level >= DungeonRPG.getMaxLevel()){
            return false;
        } else {
            return true;
        }
    }

    public CustomItem[] getEquipment(Player p){
        ArrayList<CustomItem> a = new ArrayList<CustomItem>();

        if(p.getInventory().getHelmet() != null && CustomItem.fromItemStack(p.getInventory().getHelmet()) != null) a.add(CustomItem.fromItemStack(p.getInventory().getHelmet()));
        if(p.getInventory().getChestplate() != null && CustomItem.fromItemStack(p.getInventory().getChestplate()) != null) a.add(CustomItem.fromItemStack(p.getInventory().getChestplate()));
        if(p.getInventory().getLeggings() != null && CustomItem.fromItemStack(p.getInventory().getLeggings()) != null) a.add(CustomItem.fromItemStack(p.getInventory().getLeggings()));
        if(p.getInventory().getBoots() != null && CustomItem.fromItemStack(p.getInventory().getBoots()) != null) a.add(CustomItem.fromItemStack(p.getInventory().getBoots()));
        if(p.getInventory().getItemInHand() != null && CustomItem.fromItemStack(p.getInventory().getItemInHand()) != null) a.add(CustomItem.fromItemStack(p.getInventory().getItemInHand()));

        return a.toArray(new CustomItem[]{});
    }

    public void setLastLogin(Timestamp t){
        this.lastLogin = t;
    }

    public void saveData(Player p){
        saveData(p,false,true);
    }

    public void saveData(Player p,boolean continueCharsel){
        saveData(p,continueCharsel,true);
    }

    public void saveData(Player p, boolean continueCharsel, boolean async){
        if(async){
            DungeonAPI.async(() -> saveData(p,continueCharsel,false));
        } else {
            try {
                this.storedLocation = p.getLocation();
                this.storedInventory = getConvertedInventory(p);

                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("UPDATE `characters` SET `level` = ?, `exp` = ?, `location.world` = ?, `location.x` = ?, `location.y` = ?, `location.z` = ?, `location.yaw` = ?, `location.pitch` = ?, `inventory` = ?, `lastLogin` = ? WHERE `id` = ?");
                ps.setInt(1,getLevel());
                ps.setDouble(2,getExp());
                ps.setString(3,p.getLocation().getWorld().getName());
                ps.setDouble(4,p.getLocation().getX());
                ps.setDouble(5,p.getLocation().getY());
                ps.setDouble(6,p.getLocation().getZ());
                ps.setFloat(7,p.getLocation().getYaw());
                ps.setFloat(8,p.getLocation().getPitch());
                ps.setString(9,getConvertedInventory(p).toString());
                ps.setTimestamp(10,lastLogin);
                ps.setInt(11,getId());
                ps.executeUpdate();
                ps.close();

                if(continueCharsel){
                    GameUser.getUser(p).setCurrentCharacter(null);
                    GameUser.getUser(p).bukkitReset();
                    GameUser.getUser(p).getSkillValues().reset();
                    GameUser.getUser(p).stopMPRegenTask();
                    GameUser.getUser(p).stopHPRegenTask();
                    p.teleport(DungeonRPG.getCharSelLocation());
                    DungeonRPG.updateVanishing();

                    CharacterSelectionMenu.openSelection(p);
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
