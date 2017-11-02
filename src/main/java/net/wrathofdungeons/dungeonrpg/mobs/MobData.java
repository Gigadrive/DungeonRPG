package net.wrathofdungeons.dungeonrpg.mobs;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.MySQLManager;
import net.wrathofdungeons.dungeonapi.util.GameProfileBuilder;
import net.wrathofdungeons.dungeonapi.util.ItemUtil;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.mobs.skills.MobSkill;
import net.wrathofdungeons.dungeonrpg.mobs.skills.MobSkillStorage;
import net.wrathofdungeons.dungeonrpg.regions.RegionLocation;
import net.wrathofdungeons.dungeonrpg.util.FormularUtils;
import org.bukkit.*;
import org.bukkit.entity.*;
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

    int i = 0;

    private int id;
    private String name;
    private int level;
    private MobType mobType;
    private int health;
    private int atk;
    private int xp;
    private int regen;
    private EntityType entityType;
    private int[] skins;
    private ItemStack helmet;
    private ItemStack chestplate;
    private ItemStack leggings;
    private ItemStack boots;
    private ItemStack weapon;
    private int materialDrop;
    private ArrayList<PredefinedItemDrop> predefinedItemDrops;
    private boolean adult;
    private MobClass mobClass;
    private double speed;
    private ArrayList<MobSkill> skills;
    private MobSoundData soundData;

    private AISettings aiSettings;
    private EntitySettings entitySettings;

    private String skinName;
    private ArrayList<Skin> mineSkins;

    public MobData(int id){
        this.id = id;
        Gson gson = new Gson();

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
                if(rs.getString("skins") != null){
                    skins = gson.fromJson(rs.getString("skins"),int[].class);
                } else {
                    skins = new int[]{};
                }

                if((skins == null || skins.length == 0) && entityType != EntityType.PLAYER){
                    this.helmet = Util.parseItemStack(rs.getString("helmet"));
                    if(helmet != null) helmet = ItemUtil.setUnbreakable(helmet,true);
                }

                this.chestplate = Util.parseItemStack(rs.getString("chestplate"));
                if(chestplate != null) chestplate = ItemUtil.setUnbreakable(chestplate,true);
                this.leggings = Util.parseItemStack(rs.getString("leggings"));
                if(leggings != null) leggings = ItemUtil.setUnbreakable(leggings,true);
                this.boots = Util.parseItemStack(rs.getString("boots"));
                if(boots != null) boots = ItemUtil.setUnbreakable(boots,true);
                this.weapon = Util.parseItemStack(rs.getString("weapon"));
                this.materialDrop = rs.getInt("drop.material");
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

                String skillsString = rs.getString("skills");
                if(skillsString != null){
                    skills = new ArrayList<MobSkill>();

                    for(String s : (ArrayList<String>)new Gson().fromJson(skillsString, new TypeToken<ArrayList<String>>(){}.getType())){
                        if(MobSkillStorage.getSkill(s) != null){
                            skills.add(MobSkillStorage.getSkill(s));
                        }
                    }
                } else {
                    skills = new ArrayList<MobSkill>();
                }

                String predefinedItemDropsString = rs.getString("drop.predefined");
                if(predefinedItemDropsString != null){
                    predefinedItemDrops = new Gson().fromJson(predefinedItemDropsString,new TypeToken<ArrayList<PredefinedItemDrop>>(){}.getType());
                } else {
                    predefinedItemDrops = new ArrayList<PredefinedItemDrop>();
                }

                this.soundData.volume = rs.getFloat("sound.volume");
                this.soundData.pitch = rs.getFloat("sound.pitch");
                this.speed = rs.getDouble("speed");

                this.aiSettings = new AISettings();
                this.aiSettings.setType(MobAIType.valueOf(rs.getString("ai.type")));
                this.aiSettings.setRandomStroll(rs.getBoolean("ai.randomStroll"));
                this.aiSettings.setLookAtPlayer(rs.getBoolean("ai.lookAtPlayer"));
                this.aiSettings.setLookAround(rs.getBoolean("ai.lookAround"));

                this.entitySettings = new EntitySettings();
                this.entitySettings.setZombieVillager(rs.getBoolean("settings.zombieVillager"));
                this.entitySettings.setVillagerProfession(Villager.Profession.valueOf(rs.getString("settings.villagerProfession")));
                this.entitySettings.setSheepColor(DyeColor.valueOf(rs.getString("settings.sheepColor")));
                this.entitySettings.setCatType(Ocelot.Type.valueOf(rs.getString("settings.catType")));
                this.entitySettings.setCreeperPowered(rs.getBoolean("settings.creeperPowered"));
                this.entitySettings.setSkeletonType(Skeleton.SkeletonType.valueOf(rs.getString("settings.skeletonType")));
                this.entitySettings.setSlimeSize(rs.getInt("settings.slimeSize"));
                this.entitySettings.setEndermanBlock(Material.getMaterial(rs.getInt("settings.endermanBlock")));
                this.entitySettings.setElderGuardian(rs.getBoolean("settings.elderGuardian"));
                this.entitySettings.setHorseColor(Horse.Color.valueOf(rs.getString("settings.horseColor")));
                this.entitySettings.setHasHorseChest(rs.getBoolean("settings.horseChest"));
                this.entitySettings.setHorseStyle(Horse.Style.valueOf(rs.getString("settings.horseStyle")));
                this.entitySettings.setHorseVariant(Horse.Variant.valueOf(rs.getString("settings.horseVariant")));
                this.entitySettings.setHasHorseSaddle(rs.getBoolean("settings.horseSaddle"));
                this.entitySettings.setHorseArmor(HorseArmor.valueOf(rs.getString("settings.horseArmor")));
                this.entitySettings.setRabbitType(Rabbit.Type.valueOf(rs.getString("settings.rabbitType")));

                updateSkinName();

                if(this.skins != null && this.skins.length > 0){
                    for(int skinID : this.skins){
                        DungeonRPG.getMineskinClient().getSkin(skinID, new SkinCallback() {
                            @Override
                            public void done(Skin skin) {
                                mineSkins.add(skin);

                                if(i == skins.length-1){
                                    h();
                                } else {
                                    i++;
                                }
                            }
                        });
                    }

                    /*String skinURL = this.skin;
                    if(!skinURL.startsWith("http://textures.minecraft.net/texture/")) skinURL = "http://textures.minecraft.net/texture/" + skinURL;

                    //this.gameProfile = GameProfileBuilder.getProfile(UUID.randomUUID(), ChatColor.GREEN.toString(), skinURL);
                    this.gameProfile = new GameProfile(UUID.randomUUID(),ChatColor.GREEN.toString());
                    this.gameProfile.getProperties().put("textures",new Property("textures",new String(Base64Coder.encodeString("{\"SKIN\":{\"url\":\"" + skinURL + "\"}}").getBytes())));
                    //if(getSkinSignature() != null) this.gameProfile.getProperties().put("textures",new Property("signature",getSkinSignature()));*/
                }

                if(this.health == -1) this.health = (int)FormularUtils.getMobHP(mobClass,level);
                if(this.atk == -1) this.atk = (int)FormularUtils.getMobATK(mobClass,level);
                if(this.xp == -1) this.xp = (int)FormularUtils.getMobEXP(mobClass,level);
                if(this.regen == -1) this.regen = (int)FormularUtils.getMobRegen(mobClass,level);

                STORAGE.add(this);
            }

            MySQLManager.getInstance().closeResources(rs,ps);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private void h(){
        if((mineSkins != null && mineSkins.size() > 0) && entityType != EntityType.PLAYER) helmet = ItemUtil.profiledSkullCustom(mineSkins.get(0).data.texture.url);
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

    public int[] getSkinIDs(){
        return skins;
    }

    public ArrayList<Skin> getSkins() {
        return mineSkins;
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

    public int getMaterialDrop() {
        return materialDrop;
    }

    public ArrayList<PredefinedItemDrop> getPredefinedItemDrops() {
        return predefinedItemDrops;
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

    public ArrayList<MobSkill> getSkills() {
        return skills;
    }

    public boolean isAdult() {
        return adult;
    }

    public AISettings getAiSettings() {
        return aiSettings;
    }

    public EntitySettings getEntitySettings() {
        return entitySettings;
    }
}
