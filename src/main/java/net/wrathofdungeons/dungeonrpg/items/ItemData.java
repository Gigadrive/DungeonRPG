package net.wrathofdungeons.dungeonrpg.items;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.MySQLManager;
import net.wrathofdungeons.dungeonapi.util.ItemUtil;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.items.awakening.Awakening;
import net.wrathofdungeons.dungeonrpg.items.awakening.AwakeningType;
import net.wrathofdungeons.dungeonrpg.items.crystals.CrystalType;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.net.HttpURLConnection;
import java.net.URL;
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
    private CrystalType crystalType;
    private String description;

    private int atkMin;
    private int atkMax;

    private int defMin;
    private int defMax;

    private int pickaxeStrength;
    private int mountSpeed;
    private int mountJumpStrength;

    private int foodRegeneration;
    private int foodDelay;

    private ArrayList<Awakening> additionalStats;

    private int neededLevel;
    private RPGClass neededClass;
    private int neededBlacksmithingLevel;
    private int neededCraftingLevel;
    private int neededMiningLevel;

    private boolean untradeable;
    private boolean hasArmorSkin;

    public static ItemData getData(int id){
        for(ItemData d : STORAGE){
            if(d.getId() == id) return d;
        }

        return null;
    }

    public ItemData(int id){
        Gson gson = DungeonAPI.GSON;

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
                if(rs.getString("crystalName") != null) this.crystalType = CrystalType.valueOf(rs.getString("crystalName"));
                if(rs.getString("description") != null) this.description = ChatColor.translateAlternateColorCodes('&',rs.getString("description"));

                this.atkMin = rs.getInt("atk.min");
                this.atkMax = rs.getInt("atk.max");

                this.defMin = rs.getInt("def.min");
                this.defMax = rs.getInt("def.max");

                this.pickaxeStrength = rs.getInt("pickaxe.strength");
                this.mountSpeed = rs.getInt("mount.speed");
                this.mountJumpStrength = rs.getInt("mount.jumpStrength");

                if(rs.getString("additionalStats") != null){
                    this.additionalStats = gson.fromJson(rs.getString("additionalStats"),new TypeToken<ArrayList<Awakening>>(){}.getType());
                } else {
                    this.additionalStats = new ArrayList<Awakening>();
                }

                this.foodRegeneration = rs.getInt("foodRegeneration");
                this.foodDelay = rs.getInt("foodDelay");

                this.neededLevel = rs.getInt("neededLevel");
                this.neededClass = RPGClass.valueOf(rs.getString("neededClass"));
                this.neededBlacksmithingLevel = rs.getInt("neededBlacksmithingLevel");
                this.neededCraftingLevel = rs.getInt("neededCraftingLevel");
                this.neededMiningLevel = rs.getInt("neededMiningLevel");

                this.untradeable = rs.getBoolean("untradeable");

                DungeonAPI.async(() -> reloadArmorSkin());
            }

            MySQLManager.getInstance().closeResources(rs,ps);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void reloadArmorSkin() {
        if (getCategory() != ItemCategory.ARMOR) {
            hasArmorSkin = false;
            return;
        }

        try {
            URL url = new URL(getArmorSkinURL());
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            int statusCode = http.getResponseCode();

            this.hasArmorSkin = statusCode == 200;
        } catch (Exception e) {
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

    public CrystalType getCrystalType() {
        return crystalType;
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

    public int getPickaxeStrength() {
        return pickaxeStrength;
    }

    public int getMountSpeed() {
        return mountSpeed;
    }

    public int getMountJumpStrength() {
        return mountJumpStrength;
    }

    public ArrayList<Awakening> getAdditionalStats() {
        return additionalStats;
    }

    public boolean hasAdditionalStat(AwakeningType type){
        return getAdditionalStat(type) != null;
    }

    public Awakening getAdditionalStat(AwakeningType type){
        for(Awakening a : getAdditionalStats()) if(a.type == type) return a;

        return null;
    }

    public int getAdditionalStatValue(AwakeningType type){
        if(hasAdditionalStat(type)){
            return getAdditionalStat(type).value;
        } else {
            return 0;
        }
    }

    public int getStackLimit(){
        if(getCategory() == ItemCategory.WEAPON_STICK){
            return 1;
        } else {
            return getIcon().getMaxStackSize();
        }
    }

    public int getFoodRegeneration() {
        return foodRegeneration;
    }

    public int getFoodDelayInTicks() {
        return foodDelay;
    }

    public double getFoodDelayInSeconds() {
        return Util.round((double) foodDelay * 20, 1);
    }

    public int getNeededLevel() {
        return neededLevel;
    }

    public RPGClass getNeededClass() {
        return neededClass;
    }

    public int getNeededBlacksmithingLevel() {
        return neededBlacksmithingLevel;
    }

    public int getNeededCraftingLevel() {
        return neededCraftingLevel;
    }

    public int getNeededMiningLevel() {
        return neededMiningLevel;
    }

    public boolean isUntradeable() {
        return untradeable;
    }

    public boolean hasArmorSkin() {
        return hasArmorSkin;
    }

    public String getArmorSkinURL() {
        return "https://skins.wrathofdungeons.net/armorSkinParts/" + getId() + ".png";
    }

    public boolean isMatchingWeapon(RPGClass rpgClass){
        return (getCategory() == ItemCategory.WEAPON_BOW && rpgClass.matches(RPGClass.ARCHER)) || (getCategory() == ItemCategory.WEAPON_SHEARS && rpgClass.matches(RPGClass.ASSASSIN)) || (getCategory() == ItemCategory.WEAPON_AXE && rpgClass.matches(RPGClass.MERCENARY)) || (getCategory() == ItemCategory.WEAPON_STICK && rpgClass.matches(RPGClass.MAGICIAN));
    }

    public ArmorType getArmorType() {
        if (getCategory() == ItemCategory.ARMOR) {
            ItemStack i = new ItemStack(getIcon(), 1, (short) getDurability());

            if (ItemUtil.isHelmet(i)) {
                return ArmorType.HELMET;
            } else if (ItemUtil.isChestplate(i)) {
                return ArmorType.CHESTPLATE;
            } else if (ItemUtil.isLeggings(i)) {
                return ArmorType.LEGGINGS;
            } else if (ItemUtil.isBoots(i)) {
                return ArmorType.BOOTS;
            }
        }

        return null;
    }
}
