package net.wrathofdungeons.dungeonrpg.items;

import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.MySQLManager;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class ItemData {
    public static ArrayList<ItemData> STORAGE = new ArrayList<ItemData>();

    public static void init(){
        STORAGE.clear();

        DungeonAPI.async(() -> {
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
        });
    }

    private int id;
    private String name;
    private Material icon;
    private int durability;
    private ItemCategory category;
    private ItemRarity rarity;
    private String description;

    private int atkMin;
    private int atkMax;

    private int defMin;
    private int defMax;

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
                this.category = ItemCategory.valueOf(rs.getString("category"));
                this.rarity = ItemRarity.valueOf(rs.getString("rarity"));
                if(rs.getString("description") != null) this.description = ChatColor.translateAlternateColorCodes('&',rs.getString("description"));

                this.atkMin = rs.getInt("atk.min");
                this.atkMax = rs.getInt("atk.max");

                this.defMin = rs.getInt("def.min");
                this.defMax = rs.getInt("def.max");
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

    public ItemCategory getCategory() {
        return category;
    }

    public ItemRarity getRarity() {
        return rarity;
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
}
