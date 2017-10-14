package net.wrathofdungeons.dungeonrpg.items;

import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.MySQLManager;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class ItemData {
    public static ArrayList<ItemData> STORAGE = new ArrayList<ItemData>();

    public static void init(){
        STORAGE.clear();

        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `items`");
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                STORAGE.add(new ItemData(rs.getInt("id")));
            }

            MySQLManager.getInstance().closeResources(rs,ps);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private int id;
    private String name;
    private Material icon;
    private int durability;
    private Color leatherArmorColor;
    private ItemCategory category;
    private ItemRarity rarity;
    private int tpScrollRegion;
    private String description;

    private int atkMin;
    private int atkMax;

    private int defMin;
    private int defMax;

    private int foodRegeneration;
    private int foodDelay;

    private int neededLevel;
    private RPGClass neededClass;

    private boolean untradeable;

    public static ItemData getData(int id){
        for(ItemData d : STORAGE){
            if(d.getId() == id) return d;
        }

        return null;
    }

    public ItemData(int id){
        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `items` WHERE `id` = ?");
            ps.setInt(1,id);
            ResultSet rs = ps.executeQuery();

            if(rs.first()){
                this.id = id;
                if(rs.getString("name") != null) this.name = ChatColor.translateAlternateColorCodes('&',rs.getString("name"));
                this.icon = Material.getMaterial(rs.getInt("icon"));
                this.durability = rs.getInt("durability");
                if(rs.getString("leatherArmorColor") != null) this.leatherArmorColor = Color.fromRGB(Integer.valueOf(rs.getString("leatherArmorColor").substring(1,3),16),Integer.valueOf(rs.getString("leatherArmorColor").substring(3,5),16),Integer.valueOf(rs.getString("leatherArmorColor").substring(5,7),16));
                this.category = ItemCategory.valueOf(rs.getString("category"));
                this.rarity = ItemRarity.valueOf(rs.getString("rarity"));
                this.tpScrollRegion = rs.getInt("tpScroll.regionID");
                if(rs.getString("description") != null) this.description = ChatColor.translateAlternateColorCodes('&',rs.getString("description"));

                this.atkMin = rs.getInt("atk.min");
                this.atkMax = rs.getInt("atk.max");

                this.defMin = rs.getInt("def.min");
                this.defMax = rs.getInt("def.max");

                this.foodRegeneration = rs.getInt("foodRegeneration");
                this.foodDelay = rs.getInt("foodDelay");

                this.neededLevel = rs.getInt("neededLevel");
                this.neededClass = RPGClass.valueOf(rs.getString("neededClass"));

                this.untradeable = rs.getBoolean("untradeable");
            }

            MySQLManager.getInstance().closeResources(rs,ps);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Material getIcon() {
        return icon;
    }

    public int getDurability() {
        return durability;
    }

    public Color getLeatherArmorColor() {
        return leatherArmorColor;
    }

    public ItemCategory getCategory() {
        return category;
    }

    public ItemRarity getRarity() {
        return rarity;
    }

    public int getTpScrollRegion() {
        return tpScrollRegion;
    }

    public String getDescription() {
        return description;
    }

    public int getAtkMin() {
        return atkMin;
    }

    public int getAtkMax() {
        return atkMax;
    }

    public int getDefMin() {
        return defMin;
    }

    public int getDefMax() {
        return defMax;
    }

    public int getFoodRegeneration() {
        return foodRegeneration;
    }

    public int getFoodDelayInTicks() {
        return foodDelay;
    }

    public double getFoodDelayInSeconds() {
        return (double)foodDelay*20;
    }

    public int getNeededLevel() {
        return neededLevel;
    }

    public RPGClass getNeededClass() {
        return neededClass;
    }

    public boolean isUntradeable() {
        return untradeable;
    }

    public boolean isMatchingWeapon(RPGClass rpgClass){
        return (getCategory() == ItemCategory.WEAPON_BOW && rpgClass.matches(RPGClass.ARCHER)) || (getCategory() == ItemCategory.WEAPON_SHEARS && rpgClass.matches(RPGClass.ASSASSIN)) || (getCategory() == ItemCategory.WEAPON_AXE && rpgClass.matches(RPGClass.MERCENARY)) || (getCategory() == ItemCategory.WEAPON_STICK && rpgClass.matches(RPGClass.MAGICIAN));
    }
}
