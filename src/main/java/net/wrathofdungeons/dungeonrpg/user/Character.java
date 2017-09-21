package net.wrathofdungeons.dungeonrpg.user;

import eu.the5zig.mod.server.api.Stat;
import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.MySQLManager;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.StatPointType;
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
    private int strength;
    private int stamina;
    private int intelligence;
    private int dexterity;
    private int agility;
    private int statpointsLeft;
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
                this.strength = rs.getInt("statpoints.str");
                this.stamina = rs.getInt("statpoints.sta");
                this.intelligence = rs.getInt("statpoints.int");
                this.dexterity = rs.getInt("statpoints.dex");
                this.agility = rs.getInt("statpoints.agi");
                this.statpointsLeft = rs.getInt("statpoints.left");
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

    public int getStatpointsPure(StatPointType type){
        switch(type){
            case STRENGTH: return strength;
            case STAMINA: return stamina;
            case INTELLIGENCE: return intelligence;
            case DEXTERITY: return dexterity;
            case AGILITY: return agility;
            default: return 0;
        }
    }

    public int getStatpointsArtificial(StatPointType type){
        // TODO: Calculate artificial stat points from equipment

        return 0;
    }

    public int getStatpointsTotal(StatPointType type){
        int total = getStatpointsPure(type)+getStatpointsArtificial(type);

        return total < 0 ? 0 : total;
    }

    public int getStatpointsLeft(){
        return statpointsLeft;
    }

    public void addStatpointsLeft(int i){
        this.statpointsLeft += i;
        if(this.statpointsLeft < 0) this.statpointsLeft = 0;
    }

    public void reduceStatpointsLeft(int i){
        if(this.statpointsLeft-i >= 0){
            this.statpointsLeft -= i;
        }
    }

    public void addStatpoint(StatPointType type){
        addStatpoints(type,1);
    }

    public void addStatpoints(StatPointType type, int statpoints){
        switch(type){
            case STRENGTH:
                strength += statpoints;
                break;
            case STAMINA:
                stamina += statpoints;
                break;
            case INTELLIGENCE:
                intelligence += statpoints;
                break;
            case DEXTERITY:
                dexterity += statpoints;
                break;
            case AGILITY:
                agility += statpoints;
                break;
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

                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("UPDATE `characters` SET `level` = ?, `exp` = ?, `statpoints.str` = ?, `statpoints.sta` = ?, `statpoints.int` = ?, `statpoints.dex` = ?, `statpoints.agi` = ?, `statpoints.left` = ?, `location.world` = ?, `location.x` = ?, `location.y` = ?, `location.z` = ?, `location.yaw` = ?, `location.pitch` = ?, `inventory` = ?, `lastLogin` = ? WHERE `id` = ?");
                ps.setInt(1,getLevel());
                ps.setDouble(2,getExp());
                ps.setInt(3,strength);
                ps.setInt(4,stamina);
                ps.setInt(5,intelligence);
                ps.setInt(6,dexterity);
                ps.setInt(7,agility);
                ps.setInt(8,statpointsLeft);
                ps.setString(9,p.getLocation().getWorld().getName());
                ps.setDouble(10,p.getLocation().getX());
                ps.setDouble(11,p.getLocation().getY());
                ps.setDouble(12,p.getLocation().getZ());
                ps.setFloat(13,p.getLocation().getYaw());
                ps.setFloat(14,p.getLocation().getPitch());
                ps.setString(15,getConvertedInventory(p).toString());
                ps.setTimestamp(16,lastLogin);
                ps.setInt(17,getId());
                ps.executeUpdate();
                ps.close();

                if(continueCharsel){
                    GameUser.getUser(p).setCurrentCharacter(null);
                    GameUser.getUser(p).bukkitReset();
                    GameUser.getUser(p).getSkillValues().reset();
                    GameUser.getUser(p).stopMPRegenTask();
                    GameUser.getUser(p).stopHPRegenTask();
                    GameUser.getUser(p).currentCombo = "";
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
