package net.wrathofdungeons.dungeonrpg.mobs;

import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.MySQLManager;
import net.wrathofdungeons.dungeonapi.util.ItemUtil;
import net.wrathofdungeons.dungeonapi.util.Util;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class MobData {
    public static ArrayList<MobData> STORAGE = new ArrayList<MobData>();

    public static void init(){
        STORAGE.clear();

        DungeonAPI.async(() -> {
            try {
                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `mobs`");
                ResultSet rs = ps.executeQuery();

                while(rs.next()){
                    new MobData(rs.getInt("id"));
                }

                MySQLManager.getInstance().closeResources(rs,ps);
            } catch(Exception e){
                e.printStackTrace();
            }
        });
    }

    public static MobData getData(int id){
        for(MobData d : STORAGE){
            if(d.getId() == id) return d;
        }

        return null;
    }

    private int id;
    private String name;
    private int level;
    private MobType mobType;
    private int health;
    private int atk;
    private int xp;
    private EntityType entityType;
    private String skin;
    private ItemStack helmet;
    private ItemStack chestplate;
    private ItemStack leggings;
    private ItemStack boots;
    private ItemStack weapon;
    private boolean adult;
    private MobClass mobClass;
    private AISettings aiSettings;

    public MobData(int id){
        this.id = id;

        DungeonAPI.async(() -> {
            try {
                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `mobs` WHERE `id` = ?");
                ps.setInt(1,id);
                ResultSet rs = ps.executeQuery();

                if(rs.first()){
                    this.name = rs.getString("name");
                    this.level = rs.getInt("level");
                    this.mobType = MobType.valueOf(rs.getString("type"));
                    this.health = rs.getInt("health");
                    this.atk = rs.getInt("atk");
                    this.xp = rs.getInt("xp");
                    this.entityType = EntityType.valueOf(rs.getString("entityType"));
                    this.skin = rs.getString("skin");
                    if(skin != null && entityType != EntityType.PLAYER){
                        this.helmet = ItemUtil.profiledSkull(this.skin);
                    } else {
                        this.helmet = Util.parseItemStack(rs.getString("helmet"));
                    }
                    this.chestplate = Util.parseItemStack(rs.getString("chestplate"));
                    this.leggings = Util.parseItemStack(rs.getString("leggings"));
                    this.boots = Util.parseItemStack(rs.getString("boots"));
                    this.weapon = Util.parseItemStack(rs.getString("weapon"));
                    this.adult = rs.getBoolean("adult");
                    this.mobClass = MobClass.valueOf(rs.getString("class"));
                    this.aiSettings = new AISettings(rs.getBoolean("ai.randomStroll"));

                    STORAGE.add(this);
                }

                MySQLManager.getInstance().closeResources(rs,ps);
            } catch(Exception e){
                e.printStackTrace();
            }
        });
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public MobType getMobType() {
        return mobType;
    }

    public int getHealth() {
        return health;
    }

    public int getAtk() {
        return atk;
    }

    public int getXp() {
        return xp;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public String getSkin() {
        return skin;
    }

    public ItemStack getHelmet() {
        return helmet;
    }

    public ItemStack getChestplate() {
        return chestplate;
    }

    public ItemStack getLeggings() {
        return leggings;
    }

    public ItemStack getBoots() {
        return boots;
    }

    public ItemStack getWeapon() {
        return weapon;
    }

    public MobClass getMobClass() {
        return mobClass;
    }

    public boolean isAdult() {
        return adult;
    }

    public AISettings getAiSettings() {
        return aiSettings;
    }
}