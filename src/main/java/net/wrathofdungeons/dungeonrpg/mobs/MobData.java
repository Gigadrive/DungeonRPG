package net.wrathofdungeons.dungeonrpg.mobs;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.MySQLManager;
import net.wrathofdungeons.dungeonapi.util.GameProfileBuilder;
import net.wrathofdungeons.dungeonapi.util.ItemUtil;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;
import org.mineskin.MineskinClient;
import org.mineskin.data.Skin;
import org.mineskin.data.SkinCallback;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.UUID;

public class MobData {
    public static ArrayList<MobData> STORAGE = new ArrayList<MobData>();

    public static void init(){
        STORAGE.clear();

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
    private int regen;
    private EntityType entityType;
    private int skin;
    private ItemStack helmet;
    private ItemStack chestplate;
    private ItemStack leggings;
    private ItemStack boots;
    private ItemStack weapon;
    private boolean adult;
    private MobClass mobClass;
    private double speed;
    private MobSoundData soundData;
    private AISettings aiSettings;

    private String skinName;
    private Skin mineSkin;

    public MobData(int id){
        this.id = id;

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
                this.regen = rs.getInt("regen");
                this.entityType = EntityType.valueOf(rs.getString("entityType"));
                this.skin = rs.getInt("skin");
                if(skin != 0 && entityType != EntityType.PLAYER){
                    //
                } else {
                    this.helmet = Util.parseItemStack(rs.getString("helmet"));
                }
                this.chestplate = Util.parseItemStack(rs.getString("chestplate"));
                this.leggings = Util.parseItemStack(rs.getString("leggings"));
                this.boots = Util.parseItemStack(rs.getString("boots"));
                this.weapon = Util.parseItemStack(rs.getString("weapon"));
                this.adult = rs.getBoolean("adult");
                this.mobClass = MobClass.valueOf(rs.getString("class"));
                this.soundData = new MobSoundData();

                if(rs.getString("sound.name") != null){
                    for(Sound s : Sound.values()){
                        if(s.toString().equalsIgnoreCase(rs.getString("sound.name"))){
                            this.soundData.sound = s;
                            break;
                        }
                    }
                }

                this.soundData.volume = rs.getFloat("sound.volume");
                this.soundData.pitch = rs.getFloat("sound.pitch");
                this.speed = rs.getDouble("speed");

                this.aiSettings = new AISettings();
                this.aiSettings.setRandomStroll(rs.getBoolean("ai.randomStroll"));
                this.aiSettings.setLookAtPlayer(rs.getBoolean("ai.lookAtPlayer"));
                this.aiSettings.setLookAround(rs.getBoolean("ai.lookAround"));

                updateSkinName();

                if(this.skin != 0){
                    DungeonRPG.getMineskinClient().getSkin(this.skin, new SkinCallback() {
                        @Override
                        public void done(Skin skin) {
                            mineSkin = skin;
                            h();
                        }
                    });

                    /*String skinURL = this.skin;
                    if(!skinURL.startsWith("http://textures.minecraft.net/texture/")) skinURL = "http://textures.minecraft.net/texture/" + skinURL;

                    //this.gameProfile = GameProfileBuilder.getProfile(UUID.randomUUID(), ChatColor.GREEN.toString(), skinURL);
                    this.gameProfile = new GameProfile(UUID.randomUUID(),ChatColor.GREEN.toString());
                    this.gameProfile.getProperties().put("textures",new Property("textures",new String(Base64Coder.encodeString("{\"SKIN\":{\"url\":\"" + skinURL + "\"}}").getBytes())));
                    //if(getSkinSignature() != null) this.gameProfile.getProperties().put("textures",new Property("signature",getSkinSignature()));*/
                }

                STORAGE.add(this);
            }

            MySQLManager.getInstance().closeResources(rs,ps);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private void h(){
        if(mineSkin != null && entityType != EntityType.PLAYER) helmet = ItemUtil.profiledSkullCustom(mineSkin.data.texture.url);
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

    public int getRegen() {
        return regen;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public int getSkinID(){
        return skin;
    }

    public Skin getSkin() {
        return mineSkin;
    }

    public String getSkinName() {
        return skinName;
    }

    private void updateSkinName(){
        skinName = Util.randomString(1,16);
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

    public MobSoundData getSoundData() {
        return soundData;
    }

    public void playSound(Location loc){
        if(getSoundData() != null) getSoundData().play(loc);
    }

    public void playDeathSound(Location loc){
        if(getSoundData() != null) getSoundData().playDeath(loc);
    }

    public double getSpeed() {
        return speed;
    }

    public boolean isAdult() {
        return adult;
    }

    public AISettings getAiSettings() {
        return aiSettings;
    }
}
