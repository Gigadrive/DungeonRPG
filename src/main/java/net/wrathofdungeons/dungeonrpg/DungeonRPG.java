package net.wrathofdungeons.dungeonrpg;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.EntityTarget;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.npc.NPC;
import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.MySQLManager;
import net.wrathofdungeons.dungeonapi.util.ParticleEffect;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.cmd.*;
import net.wrathofdungeons.dungeonrpg.dungeon.Dungeon;
import net.wrathofdungeons.dungeonrpg.dungeon.DungeonType;
import net.wrathofdungeons.dungeonrpg.event.CustomDamageEvent;
import net.wrathofdungeons.dungeonrpg.event.CustomDamageMobToMobEvent;
import net.wrathofdungeons.dungeonrpg.event.CustomDamagePlayerToPlayerEvent;
import net.wrathofdungeons.dungeonrpg.event.PlayerLandOnGroundEvent;
import net.wrathofdungeons.dungeonrpg.inv.*;
import net.wrathofdungeons.dungeonrpg.items.ItemData;
import net.wrathofdungeons.dungeonrpg.listener.*;
import net.wrathofdungeons.dungeonrpg.lootchests.LootChest;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.mobs.MobData;
import net.wrathofdungeons.dungeonrpg.mobs.MobType;
import net.wrathofdungeons.dungeonrpg.mobs.nms.EntityManager;
import net.wrathofdungeons.dungeonrpg.mobs.skills.MobSkillStorage;
import net.wrathofdungeons.dungeonrpg.npc.CustomNPC;
import net.wrathofdungeons.dungeonrpg.professions.CraftingRecipe;
import net.wrathofdungeons.dungeonrpg.professions.Ore;
import net.wrathofdungeons.dungeonrpg.projectile.DungeonProjectile;
import net.wrathofdungeons.dungeonrpg.projectile.DungeonProjectileType;
import net.wrathofdungeons.dungeonrpg.quests.Quest;
import net.wrathofdungeons.dungeonrpg.regions.Region;
import net.wrathofdungeons.dungeonrpg.regions.RegionLocation;
import net.wrathofdungeons.dungeonrpg.regions.RegionLocationType;
import net.wrathofdungeons.dungeonrpg.skill.Skill;
import net.wrathofdungeons.dungeonrpg.skill.SkillStorage;
import net.wrathofdungeons.dungeonrpg.skill.archer.*;
import net.wrathofdungeons.dungeonrpg.skill.assassin.DashAttack;
import net.wrathofdungeons.dungeonrpg.skill.assassin.StabbingStorm;
import net.wrathofdungeons.dungeonrpg.skill.assassin.WhirlingBlade;
import net.wrathofdungeons.dungeonrpg.skill.magician.Blinkpool;
import net.wrathofdungeons.dungeonrpg.skill.magician.ChargedExplosion;
import net.wrathofdungeons.dungeonrpg.skill.magician.FlameBurst;
import net.wrathofdungeons.dungeonrpg.skill.magician.MagicCure;
import net.wrathofdungeons.dungeonrpg.skill.mercenary.AxeBlast;
import net.wrathofdungeons.dungeonrpg.skill.mercenary.BlazingAxe;
import net.wrathofdungeons.dungeonrpg.skill.mercenary.Shockwave;
import net.wrathofdungeons.dungeonrpg.skill.mercenary.Stomper;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import net.wrathofdungeons.dungeonrpg.util.WorldUtilities;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.mineskin.MineskinClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class DungeonRPG extends JavaPlugin {
    private static DungeonRPG instance;
    public static final boolean ENABLE_BOWDRAWBACK = true;
    public static final boolean SHOW_HP_IN_ACTION_BAR = true;
    public static final int PLAYER_MOB_LEVEL_DIFFERENCE = 7;
    public static final int STATPOINTS_LIMIT = -1;
    public static final double PARTY_EXP_RANGE = 50;
    public static final boolean ENABLE_QUESTS = true;
    public static final int QUEST_NPC_TEXT_LINE_DELAY = 4;
    public static final boolean STORE_NBT_JSON = true;

    public static final int[] CRYSTAL_IDS = new int[]{11,12,13,795,799,786,789,792,796,800};
    public static final int UPGRADING_STONE = 645;
    public static final int ANCIENT_POWDER = 803;
    public static final GameMode PLAYER_DEFAULT_GAMEMODE = GameMode.ADVENTURE;
    public static final double UPGRADING_STONE_DIVIDING_VALUE = 25d;

    public static HashMap<String, DungeonProjectile> SHOT_PROJECTILE_DATA = new HashMap<String, DungeonProjectile>();
    public static ArrayList<Material> DISALLOWED_BLOCKS = new ArrayList<Material>();
    public static ArrayList<Material> DISALLOWED_ITEMS = new ArrayList<Material>();
    public static ArrayList<Material> SETUP_ADD_NO_Y = new ArrayList<Material>();
    public static ArrayList<NPC> IGNORE_SPAWN_NPC = new ArrayList<NPC>();
    public static World MAIN_WORLD = null;
    public static World DUNGEON_WORLD = null;
    public static final ArrayList<String> BROADCAST_LINES = new ArrayList<String>();
    private int broadcastCurrent = 0;

    public static int RESTART_COUNT = Util.randomInteger(2*60*60,3*60*60);
    public static ArrayList<String> WORLDS_LOADING = new ArrayList<String>();

    public static int SETUP_REGION = 0;
    private static MineskinClient mineskinClient;

    public static final Material ARMOR_SKIN_DISPLAY_ITEM = Material.WOOD_BUTTON;
    public static final int ARMOR_SKIN_DISPLAY_DURABILITY = 0;

    public static final boolean PREVENT_MINECRAFT_ARMOR = true;

    public static final long DUNGEON_ENTRANCE_INTERVAL = 1*60*60*1000; // 1 hour

    public static void reloadBroadcastLines(){
        BROADCAST_LINES.clear();

        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `broadcastMessages` ORDER BY RAND()");
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                BROADCAST_LINES.add(rs.getString("text"));
            }

            MySQLManager.getInstance().closeResources(rs,ps);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public static boolean isWorldReloading(World w) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (GameUser.isLoaded(p)) {
                GameUser u = GameUser.getUser(p);

                if (u.getReloadingWorld() != null && u.getReloadingWorld().equalsIgnoreCase(w.getName()))
                    return true;
            }
        }

        return false;
    }

    private static String skillIndicatorPrefix;

    public static String getSkillIndicatorPrefix(){
        if(skillIndicatorPrefix == null){
            skillIndicatorPrefix = ChatColor.AQUA.toString() + ChatColor.GRAY.toString();

            /*ArrayList<ChatColor> colors = new ArrayList<ChatColor>();
            for(ChatColor c : ChatColor.values()) colors.add(c);

            for(int i = 0; i < Util.randomInteger(3,7); i++){
                Collections.shuffle(colors);

                skillIndicatorPrefix += colors.toString();
            }*/
        }

        return skillIndicatorPrefix;
    }

    private static ArrayList<Hologram> craftingStationHolos = new ArrayList<Hologram>();

    public static void initCraftingStations(){
        for(Hologram h : craftingStationHolos) if(!h.isDeleted()) h.delete();
        craftingStationHolos.clear();

        for(Region region : Region.getRegions()){
            for(RegionLocation l : region.getLocations(RegionLocationType.CRAFTING_STATION)){
                l.toBukkitLocation().getBlock().setType(Material.WORKBENCH);

                Hologram h = HologramsAPI.createHologram(DungeonRPG.getInstance(),DungeonAPI.getBlockCenter(l.toBukkitLocation()).clone().add(0,2.5,0));
                h.appendTextLine(ChatColor.AQUA + "Crafting Station");
                h.appendTextLine(ChatColor.RED + "Requires Crafting Profession");

                craftingStationHolos.add(h);
            }
        }
    }

    public void onEnable(){
        mineskinClient = new MineskinClient();
        instance = this;
        saveDefaultConfig();

        File tmpFolder = new File(getTemporaryFolder());
        if (!tmpFolder.exists())
            if (!tmpFolder.mkdir())
                System.err.println("Failed to create temp folder!");

        registerListeners();
        registerCommands();

        new BukkitRunnable(){
            @Override
            public void run() {
                MAIN_WORLD = Bukkit.getWorlds().get(0);
                prepareWorld(MAIN_WORLD);
            }
        }.runTaskLater(this,5*20);

        DUNGEON_WORLD = new WorldCreator("Dungeons").createWorld();

        if(isTestServer()){
            for(DungeonType type : DungeonType.values()){
                String name = "dungeonTemplate_" + type.getWorldTemplateName();
                File file = new File(Bukkit.getWorldContainer() + "/" + name);
                if(file.exists() || file.isDirectory()) new WorldCreator(name).createWorld();
            }
        }

        MobSkillStorage.init();
        ItemData.init();
        MobData.init();
        Region.init();
        CustomNPC.init();
        LootChest.init();
        Ore.init();
        reloadBroadcastLines();
        Quest.init();
        CraftingRecipe.init();

        initCraftingStations();

        SkillStorage s = new SkillStorage();

        // ARCHER
        s.addSkill(new DartRain());
        s.addSkill(new PoisonArrow());
        s.addSkill(new Leap());
        s.addSkill(new ExplosionArrow());
        s.addSkill(new VortexBarrier());

        // ASSASSIN
        s.addSkill(new StabbingStorm());
        s.addSkill(new DashAttack());
        s.addSkill(new WhirlingBlade());

        // MAGICIAN
        s.addSkill(new FlameBurst());
        s.addSkill(new Blinkpool());
        s.addSkill(new ChargedExplosion());
        s.addSkill(new MagicCure());

        // MERCENARY
        s.addSkill(new AxeBlast());
        s.addSkill(new Stomper());
        s.addSkill(new Shockwave());
        s.addSkill(new BlazingAxe());

        DISALLOWED_BLOCKS.add(Material.CHEST);
        DISALLOWED_BLOCKS.add(Material.ENDER_CHEST);
        DISALLOWED_BLOCKS.add(Material.TRAPPED_CHEST);
        DISALLOWED_BLOCKS.add(Material.FURNACE);
        DISALLOWED_BLOCKS.add(Material.ENCHANTMENT_TABLE);
        DISALLOWED_BLOCKS.add(Material.BREWING_STAND);
        DISALLOWED_BLOCKS.add(Material.WORKBENCH);
        DISALLOWED_BLOCKS.add(Material.ANVIL);
        DISALLOWED_BLOCKS.add(Material.JUKEBOX);
        DISALLOWED_BLOCKS.add(Material.NOTE_BLOCK);
        DISALLOWED_BLOCKS.add(Material.ENDER_PORTAL_FRAME);
        DISALLOWED_BLOCKS.add(Material.BED_BLOCK);
        DISALLOWED_BLOCKS.add(Material.BEACON);
        DISALLOWED_BLOCKS.add(Material.DISPENSER);
        DISALLOWED_BLOCKS.add(Material.DROPPER);
        DISALLOWED_BLOCKS.add(Material.DAYLIGHT_DETECTOR);
        DISALLOWED_BLOCKS.add(Material.DAYLIGHT_DETECTOR_INVERTED);
        DISALLOWED_BLOCKS.add(Material.DIODE_BLOCK_OFF);
        DISALLOWED_BLOCKS.add(Material.DIODE_BLOCK_ON);
        DISALLOWED_BLOCKS.add(Material.REDSTONE_COMPARATOR_OFF);
        DISALLOWED_BLOCKS.add(Material.REDSTONE_COMPARATOR_ON);
        DISALLOWED_BLOCKS.add(Material.REDSTONE_ORE);
        DISALLOWED_BLOCKS.add(Material.HOPPER);
        DISALLOWED_BLOCKS.add(Material.TRAP_DOOR);
        DISALLOWED_BLOCKS.add(Material.IRON_TRAPDOOR);
        DISALLOWED_BLOCKS.add(Material.FENCE_GATE);
        DISALLOWED_BLOCKS.add(Material.SPRUCE_FENCE_GATE);
        DISALLOWED_BLOCKS.add(Material.BIRCH_FENCE_GATE);
        DISALLOWED_BLOCKS.add(Material.JUNGLE_FENCE_GATE);
        DISALLOWED_BLOCKS.add(Material.DARK_OAK_FENCE_GATE);
        DISALLOWED_BLOCKS.add(Material.ACACIA_FENCE_GATE);
        DISALLOWED_BLOCKS.add(Material.CAKE_BLOCK);

        DISALLOWED_ITEMS.add(Material.LEATHER_HELMET);
        DISALLOWED_ITEMS.add(Material.LEATHER_CHESTPLATE);
        DISALLOWED_ITEMS.add(Material.LEATHER_LEGGINGS);
        DISALLOWED_ITEMS.add(Material.LEATHER_BOOTS);
        DISALLOWED_ITEMS.add(Material.CHAINMAIL_HELMET);
        DISALLOWED_ITEMS.add(Material.CHAINMAIL_CHESTPLATE);
        DISALLOWED_ITEMS.add(Material.CHAINMAIL_LEGGINGS);
        DISALLOWED_ITEMS.add(Material.CHAINMAIL_BOOTS);
        DISALLOWED_ITEMS.add(Material.IRON_HELMET);
        DISALLOWED_ITEMS.add(Material.IRON_CHESTPLATE);
        DISALLOWED_ITEMS.add(Material.IRON_LEGGINGS);
        DISALLOWED_ITEMS.add(Material.IRON_BOOTS);
        DISALLOWED_ITEMS.add(Material.GOLD_HELMET);
        DISALLOWED_ITEMS.add(Material.GOLD_CHESTPLATE);
        DISALLOWED_ITEMS.add(Material.GOLD_LEGGINGS);
        DISALLOWED_ITEMS.add(Material.GOLD_BOOTS);
        DISALLOWED_ITEMS.add(Material.DIAMOND_HELMET);
        DISALLOWED_ITEMS.add(Material.DIAMOND_CHESTPLATE);
        DISALLOWED_ITEMS.add(Material.DIAMOND_LEGGINGS);
        DISALLOWED_ITEMS.add(Material.DIAMOND_BOOTS);
        DISALLOWED_ITEMS.add(Material.POTION);
        DISALLOWED_ITEMS.add(Material.APPLE);
        DISALLOWED_ITEMS.add(Material.MUSHROOM_SOUP);
        DISALLOWED_ITEMS.add(Material.BREAD);
        DISALLOWED_ITEMS.add(Material.PORK);
        DISALLOWED_ITEMS.add(Material.GRILLED_PORK);
        DISALLOWED_ITEMS.add(Material.GOLDEN_APPLE);
        DISALLOWED_ITEMS.add(Material.RAW_FISH);
        DISALLOWED_ITEMS.add(Material.COOKED_FISH);
        DISALLOWED_ITEMS.add(Material.CAKE);
        DISALLOWED_ITEMS.add(Material.CAKE_BLOCK);
        DISALLOWED_ITEMS.add(Material.COOKIE);
        DISALLOWED_ITEMS.add(Material.MELON);
        DISALLOWED_ITEMS.add(Material.RAW_BEEF);
        DISALLOWED_ITEMS.add(Material.COOKED_BEEF);
        DISALLOWED_ITEMS.add(Material.RAW_CHICKEN);
        DISALLOWED_ITEMS.add(Material.COOKED_CHICKEN);
        DISALLOWED_ITEMS.add(Material.ROTTEN_FLESH);
        DISALLOWED_ITEMS.add(Material.SPIDER_EYE);
        DISALLOWED_ITEMS.add(Material.CARROT_ITEM);
        DISALLOWED_ITEMS.add(Material.POTATO_ITEM);
        DISALLOWED_ITEMS.add(Material.BAKED_POTATO);
        DISALLOWED_ITEMS.add(Material.POISONOUS_POTATO);
        DISALLOWED_ITEMS.add(Material.PUMPKIN_PIE);
        DISALLOWED_ITEMS.add(Material.RABBIT);
        DISALLOWED_ITEMS.add(Material.COOKED_RABBIT);
        DISALLOWED_ITEMS.add(Material.RABBIT_STEW);
        DISALLOWED_ITEMS.add(Material.MUTTON);
        DISALLOWED_ITEMS.add(Material.COOKED_MUTTON);
        DISALLOWED_ITEMS.add(Material.MONSTER_EGG);
        DISALLOWED_ITEMS.add(Material.MONSTER_EGGS);
        DISALLOWED_ITEMS.add(Material.FIREWORK_CHARGE);
        DISALLOWED_ITEMS.add(Material.FIREBALL);
        DISALLOWED_ITEMS.add(Material.BOOK_AND_QUILL);
        DISALLOWED_ITEMS.add(Material.APPLE);
        DISALLOWED_ITEMS.add(Material.MINECART);
        DISALLOWED_ITEMS.add(Material.STORAGE_MINECART);
        DISALLOWED_ITEMS.add(Material.POWERED_MINECART);
        DISALLOWED_ITEMS.add(Material.EXPLOSIVE_MINECART);
        DISALLOWED_ITEMS.add(Material.HOPPER_MINECART);
        DISALLOWED_ITEMS.add(Material.COMMAND_MINECART);
        DISALLOWED_ITEMS.add(Material.SEEDS);
        DISALLOWED_ITEMS.add(Material.NAME_TAG);
        DISALLOWED_ITEMS.add(Material.LEASH);
        DISALLOWED_ITEMS.add(Material.SAPLING);

        SETUP_ADD_NO_Y.add(Material.LONG_GRASS);
        SETUP_ADD_NO_Y.add(Material.LADDER);
        SETUP_ADD_NO_Y.add(Material.TORCH);
        SETUP_ADD_NO_Y.add(Material.REDSTONE_TORCH_OFF);
        SETUP_ADD_NO_Y.add(Material.REDSTONE_TORCH_ON);
        SETUP_ADD_NO_Y.add(Material.STONE_BUTTON);
        SETUP_ADD_NO_Y.add(Material.WOOD_BUTTON);
        SETUP_ADD_NO_Y.add(Material.YELLOW_FLOWER);
        SETUP_ADD_NO_Y.add(Material.DEAD_BUSH);
        SETUP_ADD_NO_Y.add(Material.VINE);
        SETUP_ADD_NO_Y.add(Material.WATER_LILY);
        SETUP_ADD_NO_Y.add(Material.SNOW);
        SETUP_ADD_NO_Y.add(Material.CARPET);
        SETUP_ADD_NO_Y.add(Material.DOUBLE_PLANT);
        SETUP_ADD_NO_Y.add(Material.GOLD_PLATE);
        SETUP_ADD_NO_Y.add(Material.IRON_PLATE);
        SETUP_ADD_NO_Y.add(Material.STONE_PLATE);
        SETUP_ADD_NO_Y.add(Material.WOOD_PLATE);
        SETUP_ADD_NO_Y.add(Material.RAILS);
        SETUP_ADD_NO_Y.add(Material.ACTIVATOR_RAIL);
        SETUP_ADD_NO_Y.add(Material.DETECTOR_RAIL);
        SETUP_ADD_NO_Y.add(Material.POWERED_RAIL);
        SETUP_ADD_NO_Y.add(Material.SIGN);
        SETUP_ADD_NO_Y.add(Material.SIGN_POST);
        SETUP_ADD_NO_Y.add(Material.WALL_SIGN);
        SETUP_ADD_NO_Y.add(Material.getMaterial(38));

        // UPDATE PLAYER HP/MP BAR

        if(SHOW_HP_IN_ACTION_BAR){
            new BukkitRunnable(){
                @Override
                public void run() {
                    for(Player p : Bukkit.getOnlinePlayers()){
                        if(GameUser.isLoaded(p)){
                            GameUser u = GameUser.getUser(p);

                            if(u.getCurrentCharacter() != null)
                                u.updateActionBar();
                        }
                    }
                }
            }.runTaskTimerAsynchronously(this,20,20);
        }

        // TELEPORT MOB NAME PLATES

        new BukkitRunnable(){
            @Override
            public void run() {
                try {
                    for(CustomEntity e : CustomEntity.STORAGE.values()){
                        if(e.getBukkitEntity() != null && e.getBukkitEntity().isValid() && !e.getBukkitEntity().isDead()){
                            if(e.getHologram() != null && e.getBukkitEntity() != null){
                                e.getHologram().teleport(e.getSupposedHologramLocation());
                            }
                        }
                    }
                } catch(ConcurrentModificationException e){}
            }
        }.runTaskTimerAsynchronously(this,1,1);

        // REMOVE INVALID ENTITIES

        new BukkitRunnable(){
            @Override
            public void run() {
                try {
                    ArrayList<CustomEntity> toRemove = new ArrayList<CustomEntity>();

                    for(CustomEntity entity : CustomEntity.STORAGE.values()){
                        if(entity.getBukkitEntity() == null || !entity.getBukkitEntity().isValid() || entity.getBukkitEntity().isDead()){
                            toRemove.add(entity);
                        }
                    }

                    for(CustomEntity entity : toRemove) entity.remove();
                } catch(ConcurrentModificationException e){}
            }
        }.runTaskTimerAsynchronously(this,0,10*20);

        // SAVE PLAYER DATA

        new BukkitRunnable(){
            @Override
            public void run() {
                for(Player all : Bukkit.getOnlinePlayers()){
                    if(GameUser.isLoaded(all)){
                        GameUser a = GameUser.getUser(all);

                        if(a.getCurrentCharacter() != null){
                            a.getCurrentCharacter().saveData(false,false);
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(this,2*60*20,2*60*20);

        // STOMPER PARTICLES

        new BukkitRunnable(){
            @Override
            public void run() {
                for(Player p : Bukkit.getOnlinePlayers()){
                    if(GameUser.isLoaded(p)){
                        GameUser u = GameUser.getUser(p);
                        boolean o = ((Entity)p).isOnGround();

                        if(!u.onGround && o){
                            PlayerLandOnGroundEvent event = new PlayerLandOnGroundEvent(p);
                            Bukkit.getPluginManager().callEvent(event);
                        }

                        u.onGround = o;

                        if(u.getSkillValues() != null){
                            if(u.getSkillValues().stomperActive){
                                ParticleEffect.SMOKE_NORMAL.display(0f,0f,0f,0.005f,1,p.getLocation(),600);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(this,1,1);

        // ARROW TRAILS

        new BukkitRunnable(){
            @Override
            public void run() {
                for(DungeonProjectile proj : SHOT_PROJECTILE_DATA.values()){
                    if(proj.getEntity() != null && proj.getEntity().isValid()){
                        DungeonProjectileType type = proj.getType();

                        if(type == DungeonProjectileType.EXPLOSION_ARROW){
                            if(proj.getRange() == 4){
                                ParticleEffect.VILLAGER_HAPPY.display(0f,0f,0f,0.005f,5,proj.getEntity().getLocation(),600);
                            } else if(proj.getRange() == 6){
                                ParticleEffect.VILLAGER_HAPPY.display(0f,0f,0f,0.005f,5,proj.getEntity().getLocation(),600);
                            } else if(proj.getRange() == 8){
                                ParticleEffect.VILLAGER_HAPPY.display(0f,0f,0f,0.005f,5,proj.getEntity().getLocation(),600);
                                ParticleEffect.FIREWORKS_SPARK.display(0f,0f,0f,0.005f,5,proj.getEntity().getLocation(),600);
                            } else if(proj.getRange() == 10){
                                ParticleEffect.VILLAGER_HAPPY.display(0f,0f,0f,0.005f,5,proj.getEntity().getLocation(),600);
                                ParticleEffect.FIREWORKS_SPARK.display(0f,0f,0f,0.005f,5,proj.getEntity().getLocation(),600);
                            } else if(proj.getRange() == 12){
                                ParticleEffect.VILLAGER_HAPPY.display(0f,0f,0f,0.005f,5,proj.getEntity().getLocation(),600);
                                ParticleEffect.FIREWORKS_SPARK.display(0f,0f,0f,0.005f,5,proj.getEntity().getLocation(),600);
                            }
                        } else if(type == DungeonProjectileType.MOB_FIREBALL){
                            ParticleEffect.FLAME.display(0.05f,0.05f,0.05f,0.005f,15,proj.getEntity().getLocation(),600);
                            ParticleEffect.SMOKE_NORMAL.display(0.05f,0.05f,0.05f,0.005f,4,proj.getEntity().getLocation(),600);
                        } else if(type == DungeonProjectileType.POISON_ARROW){
                            ParticleEffect.REDSTONE.display(new ParticleEffect.OrdinaryColor(0,255,0),proj.getEntity().getLocation(),600);
                        }
                    }
                }
            }
        }.runTaskTimer(this,1,1);

        // HANDLE NPC TARGETS

        new BukkitRunnable(){
            @Override
            public void run() {
                for(NPC npc : CitizensAPI.getNPCRegistry().sorted()){
                    if(npc.getEntity() != null && npc.getEntity() instanceof LivingEntity){
                        CustomEntity c = CustomEntity.fromEntity((LivingEntity)npc.getEntity());
                        Navigator n = npc.getNavigator();

                        if(c != null && n != null){
                            MobType m = c.getData().getMobType();

                            if(n.getEntityTarget() != null){
                                EntityTarget entityTarget = n.getEntityTarget();

                                if(entityTarget != null){
                                    LivingEntity target = entityTarget.getTarget() != null && entityTarget.getTarget() instanceof LivingEntity ? (LivingEntity)entityTarget.getTarget() : null;

                                    if(target != null){
                                        CustomEntity ct = CustomEntity.fromEntity(target);

                                        if(ct != null){
                                            MobType mt = ct.getData().getMobType();

                                            if(m == MobType.AGGRO && mt == MobType.AGGRO){
                                                n.cancelNavigation();
                                                return;
                                            } else if(m == MobType.SUPPORTING && mt == MobType.SUPPORTING){
                                                n.cancelNavigation();
                                                return;
                                            } else if(m == MobType.PASSIVE || mt == MobType.PASSIVE){
                                                n.cancelNavigation();
                                                return;
                                            }

                                            c.addWanderGoal();
                                        } else {
                                            if(target instanceof Player && GameUser.isLoaded((Player)target)){
                                                if(m == MobType.PASSIVE){
                                                    n.cancelNavigation();
                                                    return;
                                                } else if(m == MobType.SUPPORTING){
                                                    n.cancelNavigation();
                                                    return;
                                                }

                                                c.addWanderGoal();
                                            } else {
                                                n.cancelNavigation();
                                            }
                                        }
                                    } else {
                                        if(c.hasWanderGoal() && !c.getData().getAiSettings().mayDoRandomStroll()) c.removeWanderGoal();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(this,1,1);

        // REMOVE ARROWS IN PLAYERS

        new BukkitRunnable(){
            @Override
            public void run() {
                for(Player p : Bukkit.getOnlinePlayers()){
                    ((CraftPlayer)p).getHandle().k(0);
                }

                for(NPC npc : CitizensAPI.getNPCRegistry().sorted()){
                    if(npc.getEntity() != null && npc.getEntity() instanceof Player){
                        ((CraftPlayer)npc.getEntity()).getHandle().k(0);
                    }
                }
            }
        }.runTaskTimer(this,1,1);

        // PLAYER MOB IDLING

        new BukkitRunnable(){
            @Override
            public void run() {
                for(CustomEntity c : CustomEntity.STORAGE.values()){
                    if(c.getData() == null) continue;

                    if(c.getData().getEntityType() == EntityType.PLAYER && c.getData().getAiSettings().mayDoRandomStroll()){
                        if(c.getBukkitEntity() != null && c.toCitizensNPC() != null){
                            if(Util.getChanceBoolean(50,15)){
                                if(c.playerMobSpeed){
                                    c.toCitizensNPC().getNavigator().getDefaultParameters().baseSpeed(0f);
                                    c.playerMobSpeed = false;
                                } else {
                                    c.toCitizensNPC().getNavigator().getDefaultParameters().baseSpeed((float)c.getData().getSpeed());
                                    c.playerMobSpeed = true;
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(this,2*20,2*20);

        // LOOTCHEST PARTICLES

        new BukkitRunnable(){
            @Override
            public void run() {
                for(LootChest chest : LootChest.STORAGE.values()){
                    if(chest.isSpawned()){
                        chest.getLocation().getWorld().playEffect(chest.getLocation(),Effect.MOBSPAWNER_FLAMES,1);
                    }
                }
            }
        }.runTaskTimer(this,10,10);

        // AUTO BROADCAST

        new BukkitRunnable(){
            @Override
            public void run() {
                if(BROADCAST_LINES.size() == 0) return;
                if(broadcastCurrent >= BROADCAST_LINES.size()) broadcastCurrent = 0;

                String l = ChatColor.translateAlternateColorCodes('&',BROADCAST_LINES.get(broadcastCurrent));

                for(Player p : Bukkit.getOnlinePlayers()){
                    if(GameUser.isLoaded(p)){
                        GameUser u = GameUser.getUser(p);

                        if(u.getCurrentCharacter() != null){
                            p.sendMessage(ChatColor.DARK_RED + "[INFO] " + ChatColor.WHITE + l);
                        }
                    }
                }

                broadcastCurrent++;
            }
        }.runTaskTimer(DungeonRPG.getInstance(),6*60*20,6*60*20);

        // QUEST PARTICLES

        new BukkitRunnable(){
            @Override
            public void run() {
                for(Player p : Bukkit.getOnlinePlayers()){
                    if(GameUser.isLoaded(p)){
                        GameUser u = GameUser.getUser(p);

                        if(u.getCurrentCharacter() != null){
                            for(Quest q : Quest.STORAGE.values()){
                                if(u.getCurrentCharacter().mayStartQuest(q)){
                                    CustomNPC giver = q.getGiverNPC();

                                    if(giver != null){
                                        ParticleEffect.VILLAGER_HAPPY.display(0.55f,1f,0.55f,1f,100,giver.getLocation(),p);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(DungeonRPG.getInstance(),10,10);

        // DAMAGE HANDLER

        new BukkitRunnable(){
            @Override
            public void run() {
                for(CustomEntity entity : CustomEntity.STORAGE.values()){
                    if(entity.getBukkitEntity() == null || !entity.requiresNewDamageHandler()) continue;

                    if(entity.attackDelay > 0){
                        entity.attackDelay--;
                        continue;
                    }

                    if(entity.hasTarget()){
                        if(entity.getBukkitEntity().getLocation().distance(entity.getTarget().getLocation()) > 2) continue;

                        if(entity.getTarget() instanceof Player){
                            Player p = (Player)entity.getTarget();

                            if(GameUser.isLoaded(p)){
                                callMobToPlayerDamage(entity,(Player)entity.getTarget(),null);
                                entity.attackDelay = 10;
                            }
                        } else {
                            CustomEntity mob = CustomEntity.fromEntity(entity.getTarget());

                            if(mob != null){
                                callMobToMobDamage(entity,mob,null);
                                entity.attackDelay = 10;
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(DungeonRPG.getInstance(),1,1);

        // FIRST SKILL REMINDER

        new BukkitRunnable(){
            @Override
            public void run() {
                for(Player p : Bukkit.getOnlinePlayers()){
                    if(GameUser.isLoaded(p)){
                        GameUser u = GameUser.getUser(p);

                        if(u.getCurrentCharacter() != null){
                            if(!u.getCurrentCharacter().getVariables().hasDoneFirstSkill){
                                if(u.getCurrentCharacter().getRpgClass().matches(RPGClass.ARCHER)){
                                    p.sendMessage(ChatColor.DARK_RED + "You can cast different skills by using various click combinations. Try to cast a skill by doing this click combination, while holding your weapon: " + ChatColor.RED + "Left - Right - Left" + ChatColor.DARK_RED + "!");
                                } else {
                                    p.sendMessage(ChatColor.DARK_RED + "You can cast different skills by using various click combinations. Try to cast a skill by doing this click combination, while holding your weapon: " + ChatColor.RED + "Right - Left - Right" + ChatColor.DARK_RED + "!");
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(DungeonRPG.getInstance(), 0,25*20);

        // SERVER RESTART

        new BukkitRunnable(){
            @Override
            public void run() {
                RESTART_COUNT--;

                if(RESTART_COUNT == 30*60){
                    for(Player p : Bukkit.getOnlinePlayers()){
                        if(!GameUser.isLoaded(p)) continue;
                        GameUser u = GameUser.getUser(p);

                        if(u.getCurrentCharacter() != null) p.sendMessage(ChatColor.GOLD + "This server will restart in 30 minutes.");
                    }
                } else if(RESTART_COUNT == 20*60){
                    for(Player p : Bukkit.getOnlinePlayers()){
                        if(!GameUser.isLoaded(p)) continue;
                        GameUser u = GameUser.getUser(p);

                        if(u.getCurrentCharacter() != null) p.sendMessage(ChatColor.GOLD + "This server will restart in 20 minutes.");
                    }
                } else if(RESTART_COUNT == 10*60){
                    for(Player p : Bukkit.getOnlinePlayers()){
                        if(!GameUser.isLoaded(p)) continue;
                        GameUser u = GameUser.getUser(p);

                        if(u.getCurrentCharacter() != null) p.sendMessage(ChatColor.GOLD + "This server will restart in 10 minutes.");
                    }
                } else if(RESTART_COUNT == 5*60){
                    for(Player p : Bukkit.getOnlinePlayers()){
                        if(!GameUser.isLoaded(p)) continue;
                        GameUser u = GameUser.getUser(p);

                        if(u.getCurrentCharacter() != null) p.sendMessage(ChatColor.GOLD + "This server will restart in 5 minutes.");
                    }
                } else if(RESTART_COUNT == 2*60){
                    for(Player p : Bukkit.getOnlinePlayers()){
                        if(!GameUser.isLoaded(p)) continue;
                        GameUser u = GameUser.getUser(p);

                        if(u.getCurrentCharacter() != null) p.sendMessage(ChatColor.GOLD + "This server will restart in 2 minutes.");
                    }
                } else if(RESTART_COUNT == 60){
                    for(Player p : Bukkit.getOnlinePlayers()){
                        if(!GameUser.isLoaded(p)) continue;
                        GameUser u = GameUser.getUser(p);

                        if(u.getCurrentCharacter() != null) p.sendMessage(ChatColor.GOLD + "This server will restart in 1 minute.");
                    }
                } else if(RESTART_COUNT == 30){
                    for(Player p : Bukkit.getOnlinePlayers()){
                        if(!GameUser.isLoaded(p)) continue;
                        GameUser u = GameUser.getUser(p);

                        if(u.getCurrentCharacter() != null) p.sendMessage(ChatColor.GOLD + "This server will restart in 30 seconds.");
                    }
                } else if(RESTART_COUNT == 20){
                    for(Player p : Bukkit.getOnlinePlayers()){
                        if(!GameUser.isLoaded(p)) continue;
                        GameUser u = GameUser.getUser(p);

                        if(u.getCurrentCharacter() != null) p.sendMessage(ChatColor.GOLD + "This server will restart in 20 seconds.");
                    }
                } else if(RESTART_COUNT == 10){
                    for(Player p : Bukkit.getOnlinePlayers()){
                        if(!GameUser.isLoaded(p)) continue;
                        GameUser u = GameUser.getUser(p);

                        if(u.getCurrentCharacter() != null) p.sendMessage(ChatColor.GOLD + "This server will restart in 10 seconds.");
                    }
                } else if(RESTART_COUNT == 5){
                    for(Player p : Bukkit.getOnlinePlayers()){
                        if(!GameUser.isLoaded(p)) continue;
                        GameUser u = GameUser.getUser(p);

                        if(u.getCurrentCharacter() != null) p.sendMessage(ChatColor.GOLD + "This server will restart in 5 seconds.");
                    }
                } else if(RESTART_COUNT == 4){
                    for(Player p : Bukkit.getOnlinePlayers()){
                        if(!GameUser.isLoaded(p)) continue;
                        GameUser u = GameUser.getUser(p);

                        if(u.getCurrentCharacter() != null) p.sendMessage(ChatColor.GOLD + "This server will restart in 4 seconds.");
                    }
                } else if(RESTART_COUNT == 3){
                    for(Player p : Bukkit.getOnlinePlayers()){
                        if(!GameUser.isLoaded(p)) continue;
                        GameUser u = GameUser.getUser(p);

                        if(u.getCurrentCharacter() != null) p.sendMessage(ChatColor.GOLD + "This server will restart in 3 seconds.");
                    }
                } else if(RESTART_COUNT == 2){
                    for(Player p : Bukkit.getOnlinePlayers()){
                        if(!GameUser.isLoaded(p)) continue;
                        GameUser u = GameUser.getUser(p);

                        if(u.getCurrentCharacter() != null) p.sendMessage(ChatColor.GOLD + "This server will restart in 2 seconds.");
                    }
                } else if(RESTART_COUNT == 1){
                    for(Player p : Bukkit.getOnlinePlayers()){
                        if(!GameUser.isLoaded(p)) continue;
                        GameUser u = GameUser.getUser(p);

                        if(u.getCurrentCharacter() != null) p.sendMessage(ChatColor.GOLD + "This server will restart in 1 seconds.");
                    }
                } else if(RESTART_COUNT == 0){
                    for(Player p : Bukkit.getOnlinePlayers()){
                        if(!GameUser.isLoaded(p)) continue;
                        GameUser u = GameUser.getUser(p);

                        DungeonAPI.executeBungeeCommand(p.getName(),"hub");
                    }

                    cancel();

                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            Bukkit.shutdown();
                        }
                    }.runTaskLater(DungeonRPG.getInstance(),2*20);
                }
            }
        }.runTaskTimer(DungeonRPG.getInstance(),0,20);

        // ORES RESET

        new BukkitRunnable(){
            @Override
            public void run() {
                for(Ore ore : Ore.STORAGE.values()){
                    if(!ore.isAvailable()){
                        if(ore.timer > 0){
                            ore.timer--;
                        } else {
                            ore.setAvailable(true);
                            ore.timer = 0;
                            ore.getLocation().getBlock().setType(ore.getLevel().getBlock());
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(DungeonRPG.getInstance(),20,20);

        // ORES PARTICLES

        new BukkitRunnable(){
            @Override
            public void run() {
                for(Ore ore : Ore.STORAGE.values()){
                    if(ore.isAvailable()){
                        ParticleEffect.VILLAGER_HAPPY.display(0.5f,0.5f,0.5f,1f,50,ore.getLocation(),600);
                    }
                }
            }
        }.runTaskTimer(DungeonRPG.getInstance(),2*20,2*20);

        // BAR RESET

        new BukkitRunnable(){
            @Override
            public void run() {
                for(Player p : Bukkit.getOnlinePlayers()){
                    if(GameUser.isLoaded(p)){
                        GameUser u = GameUser.getUser(p);

                        if(u.getCurrentCharacter() != null){
                            if(u.barTimer > 1){
                                u.barTimer--;
                            } else if(u.barTimer == 1){
                                u.barTimer = 0;
                                u.updateLevelBar();
                            }
                        } else {
                            if(u.barTimer >= 1){
                                u.barTimer = 0;
                                p.setExp(0f);
                                p.setLevel(0);
                            }
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(DungeonRPG.getInstance(),20,20);

        // UPDATE TAB

        new BukkitRunnable(){
            @Override
            public void run() {
                for(Player p : Bukkit.getOnlinePlayers()){
                    if(GameUser.isLoaded(p)){
                        GameUser u = GameUser.getUser(p);

                        if(u.getCurrentCharacter() != null) u.updateTabList();
                    }
                }
            }
        }.runTaskTimer(DungeonRPG.getInstance(),10*20,10*20);

        new BukkitRunnable(){
            @Override
            public void run() {
                WorldUtilities.pvpArenaResults.clear();
            }
        }.runTaskTimer(this,20*20,20*20);

        // DUNGEON CHECK

        new BukkitRunnable(){
            @Override
            public void run() {
                for(Dungeon dungeon : Dungeon.STORAGE){
                    if (dungeon.getWorld().getPlayers().size() == 0 || dungeon.getParty() == null || dungeon.getParty().getMembers().size() == 0 && !isWorldReloading(dungeon.getWorld())) {
                        dungeon.unregister();
                    }
                }
            }
        }.runTaskTimer(this,30*20,30*20);

        Bukkit.getServer().clearRecipes();
        EntityManager.registerEntities();
    }

    public static boolean isInGame(String name){
        return isInGame(Bukkit.getPlayer(name));
    }

    public static boolean isInGame(UUID uuid){
        return isInGame(Bukkit.getPlayer(uuid));
    }

    public static boolean isInGame(Player p){
        if(p != null){
            if(p.isOnline()){
                if(GameUser.isLoaded(p)){
                    GameUser u = GameUser.getUser(p);

                    return u.getCurrentCharacter() != null;
                }
            }
        }

        return false;
    }

    public void onDisable(){
        for(CustomNPC npc : CustomNPC.getUnsavedData()){
            npc.saveData(false);
        }
    }

    public static String getTemporaryFolder() {
        return Bukkit.getWorldContainer().getAbsolutePath().substring(0, Bukkit.getWorldContainer().getAbsolutePath().length() - 1) + "plugins/DungeonRPG/tmp/";
    }

    public static void copyWorldToNewWorld(final String originalName, String newName) throws Exception {
        if(originalName.equals(newName)) throw new IllegalArgumentException("Cannot copy to original folder");

        String originalPath = Bukkit.getWorldContainer().getAbsolutePath().substring(0,Bukkit.getWorldContainer().getAbsolutePath().length()-1) + originalName + "/";
        String newPath = Bukkit.getWorldContainer().getAbsolutePath().substring(0,Bukkit.getWorldContainer().getAbsolutePath().length()-1) + newName + "/";

        final File originalFile = new File(originalPath);

        File copy = new File(newPath);

        if(originalFile.exists() || originalFile.isDirectory()){
            FileUtils.copyDirectory(originalFile,copy);

            File worldDataFile = new File(copy.getAbsolutePath() + "/" + "uid.dat");
            if(worldDataFile.exists())
                worldDataFile.delete();

            WORLDS_LOADING.add(newName);
            DungeonRPG.prepareWorld(new WorldCreator(newName).createWorld());
            WORLDS_LOADING.remove(newName);
        } else {
            throw new FileNotFoundException("Original file " + originalName + " not found");
        }
    }

    public static MineskinClient getMineskinClient(){
        return mineskinClient;
    }

    public static int getMaxLevel(){
        return getInstance().getConfig().getInt("max-lvl");
    }

    public static Location getCharSelLocation(){
        return new Location(Bukkit.getWorld(getInstance().getConfig().getString("locations.charsel.world")), getInstance().getConfig().getDouble("locations.charsel.x"), getInstance().getConfig().getDouble("locations.charsel.y"), getInstance().getConfig().getDouble("locations.charsel.z"), getInstance().getConfig().getInt("locations.charsel.yaw"), getInstance().getConfig().getInt("locations.charsel.pitch"));
    }

    public static Location getStartLocation(){
        return new Location(Bukkit.getWorld(getInstance().getConfig().getString("locations.startLocation.world")), getInstance().getConfig().getDouble("locations.startLocation.x"), getInstance().getConfig().getDouble("locations.startLocation.y"), getInstance().getConfig().getDouble("locations.startLocation.z"), getInstance().getConfig().getInt("locations.startLocation.yaw"), getInstance().getConfig().getInt("locations.startLocation.pitch"));
    }

    public static void updateVanishing(){
        for(Player all : Bukkit.getOnlinePlayers()){
            if(GameUser.isLoaded(all)) GameUser.getUser(all).updateVanishing();
        }
    }

    public static boolean mayAttack(MobType t1, MobType t2){
        if(t1 == MobType.AGGRO){
            return t2 != MobType.PASSIVE && t2 != MobType.AGGRO;
        } else if(t1 == MobType.NEUTRAL){
            return t2 != MobType.PASSIVE && t2 != MobType.AGGRO;
        } else if(t1 == MobType.PASSIVE){
            return false;
        } else if(t1 == MobType.SUPPORTING){
            return t2 == MobType.AGGRO || t2 == MobType.NEUTRAL;
        }

        return true;
    }

    public static void updateNames(){
        for(Player all : Bukkit.getOnlinePlayers()){
            if(GameUser.isLoaded(all)){
                GameUser u = GameUser.getUser(all);

                u.updateName();
                u.updateHoloPlate();
            }
        }
    }

    public static void showBloodEffect(Location loc){
        loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_HURT, 1F, 1F);
        for(Player p : loc.getWorld().getPlayers()){
            if(GameUser.isLoaded(p)){
                GameUser u = GameUser.getUser(p);

                if(u.getSettingsManager().mayShowBlood()) p.playEffect(loc.add(0.0D, 0.8D, 0.0D), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
            }
        }
    }

    public static ArrayList<LivingEntity> getTargets(LivingEntity ent){
        return getTargets(ent, 10, 2.0);
    }

    public static ArrayList<LivingEntity> getTargets(LivingEntity ent, int range){
        return getTargets(ent, range, 2.0);
    }

    public static ArrayList<LivingEntity> getTargets(LivingEntity ent, int range, double maxDistance){
        ArrayList<LivingEntity> a = new ArrayList<LivingEntity>();

        List<Entity> nearby = ent.getNearbyEntities(range, range, range);
        BlockIterator itr = new BlockIterator(ent, range);

        while(itr.hasNext()){
            Location loc = itr.next().getLocation().add(0.5, 0.5, 0.5);

            for(Entity e : nearby){
                if(e instanceof LivingEntity){
                    if(e.getLocation().distance(loc) <= maxDistance){
                        if(!a.contains((LivingEntity)e)){
                            if(CustomNPC.fromEntity(e) == null) a.add((LivingEntity)e);
                        }
                    }
                }
            }
        }

        return a;
    }

    public static ChatColor randomColor(){
        return ChatColor.values()[Util.randomInteger(0,ChatColor.values().length-1)];
    }

    public static void callPlayerToMobDamage(Player p, CustomEntity mob, DungeonProjectile projectile){
        callPlayerToMobDamage(p,mob,projectile,null);
    }

    public static void callPlayerToMobDamage(Player p, CustomEntity mob, DungeonProjectile projectile, Skill skill){
        CustomDamageEvent event = new CustomDamageEvent(p,mob,true,projectile,skill);
        Bukkit.getPluginManager().callEvent(event);
    }

    public static void callMobToPlayerDamage(CustomEntity mob, Player p, DungeonProjectile projectile){
        CustomDamageEvent event = new CustomDamageEvent(p,mob,false,projectile,null);
        Bukkit.getPluginManager().callEvent(event);
    }

    public static void callMobToMobDamage(CustomEntity mob, CustomEntity mob2, DungeonProjectile projectile){
        CustomDamageMobToMobEvent event = new CustomDamageMobToMobEvent(mob,mob2,projectile);
        Bukkit.getPluginManager().callEvent(event);
    }

    public static void callPlayerToPlayerDamage(Player p, Player p2, DungeonProjectile projectile){
        CustomDamagePlayerToPlayerEvent event = new CustomDamagePlayerToPlayerEvent(p,p2,projectile);
        Bukkit.getPluginManager().callEvent(event);
    }

    public static boolean isTestServer(){
        return DungeonAPI.getServerName().startsWith("Test-");
    }

    public static String convertComboString(String s){
        switch(s){
            case "LLL": return "Left - Left - Left";
            case "LLR": return "Left - Left - Right";
            case "LRL": return "Left - Right - Left";
            case "LRR": return "Left - Right - Right";
            case "RRR": return "Right - Right - Right";
            case "RRL": return "Right - Right - Left";
            case "RLR": return "Right - Left - Right";
            case "RLL": return "Right - Left - Left";
            default: return s;
        }
    }

    private void registerListeners(){
        Bukkit.getPluginManager().registerEvents(new AnimationListener(),this);
        Bukkit.getPluginManager().registerEvents(new AwakeningMenu(),this);
        Bukkit.getPluginManager().registerEvents(new BlockListener(),this);
        Bukkit.getPluginManager().registerEvents(new BuyingMerchantMenu(),this);
        Bukkit.getPluginManager().registerEvents(new CharacterCreationListener(),this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(),this);
        Bukkit.getPluginManager().registerEvents(new ChunkListener(),this);
        Bukkit.getPluginManager().registerEvents(new CombustListener(),this);
        Bukkit.getPluginManager().registerEvents(new CraftingMenu(),this);
        Bukkit.getPluginManager().registerEvents(new CraftListener(),this);
        Bukkit.getPluginManager().registerEvents(new CreatureListener(),this);
        Bukkit.getPluginManager().registerEvents(new CrystalMenu(),this);
        Bukkit.getPluginManager().registerEvents(new CustomDamageListener(),this);
        Bukkit.getPluginManager().registerEvents(new DamageListener(),this);
        Bukkit.getPluginManager().registerEvents(new DeathListener(),this);
        Bukkit.getPluginManager().registerEvents(new ExplodeListener(),this);
        Bukkit.getPluginManager().registerEvents(new FoodListener(),this);
        Bukkit.getPluginManager().registerEvents(new FrameProtectListener(),this);
        Bukkit.getPluginManager().registerEvents(new InteractListener(),this);
        Bukkit.getPluginManager().registerEvents(new InventoryClickListener(),this);
        Bukkit.getPluginManager().registerEvents(new InventoryCloseListener(),this);
        Bukkit.getPluginManager().registerEvents(new LandOnGroundListener(),this);
        Bukkit.getPluginManager().registerEvents(new MerchantSetupMenu(),this);
        Bukkit.getPluginManager().registerEvents(new NPCInteractListener(),this);
        Bukkit.getPluginManager().registerEvents(new PlayerDropListener(),this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(),this);
        Bukkit.getPluginManager().registerEvents(new PlayerMoveListener(),this);
        Bukkit.getPluginManager().registerEvents(new PlayerPickupListener(),this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(),this);
        Bukkit.getPluginManager().registerEvents(new ProjectileHitListener(),this);
        Bukkit.getPluginManager().registerEvents(new ShootBowListener(),this);
        Bukkit.getPluginManager().registerEvents(new SkinListener(), this);
        Bukkit.getPluginManager().registerEvents(new SplitListener(),this);
        Bukkit.getPluginManager().registerEvents(new SwitchItemListener(),this);
        Bukkit.getPluginManager().registerEvents(new TargetListener(),this);
        Bukkit.getPluginManager().registerEvents(new VehicleExitListener(),this);
        Bukkit.getPluginManager().registerEvents(new WeatherChangeListener(),this);

        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(this, "TheChest");

        PluginMessageListener l = new PluginMessageListener();
        Bukkit.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", l);
        Bukkit.getServer().getMessenger().registerIncomingPluginChannel(this, "TheChest", l);
    }

    private void registerCommands(){
        new AwakeningCommand();
        new CreateNPCCommand();
        new CreateLootChestCommand();
        new CreateRegionCommand();
        new CreateQuestCommand();
        new CharSelCommand();
        new DuelCommand();
        new ExpCommand();
        new FlyCommand();
        new GiveItemCommand();
        new GuildCommand();
        new ItemInfoCommand();
        new LoadRegionCommand();
        new ModifyNPCCommand();
        new ModifyQuestCommand();
        new ModifyRecipeCommand();
        new ModifyRegionCommand();
        new PartyCommand();
        new ReloadCommand();
        new ResetDungeonTimerCommand();
        new SaveNPCsCommand();
        new SaveRecipesCommand();
        new SaveRegionCommand();
        new SaveQuestsCommand();
        new SetLocationCommand();
        new SetSocketsCommand();
        new SetupCommand();
        new SummonCommand();
        new SyncCommand();
        new TestAmountCommand();
        new TradeCommand();
        new WorldCommand();
        new WorldBroadcastCommand();
    }

    public static boolean mayAttack(Player p, Player p2){
        return p != p2 && GameUser.isLoaded(p) && GameUser.isLoaded(p2) && GameUser.getUser(p).getCurrentCharacter() != null && GameUser.getUser(p2).getCurrentCharacter() != null &&
                (Duel.isDuelingWith(p,p2) ||
                (WorldUtilities.isPvPArena(p.getLocation()) && WorldUtilities.isPvPArena(p2.getLocation())));
    }

    public static void setLocationIndicator(Location loc, RegionLocationType type){
        if(loc == null || type == null) return;

        switch(type){
            case MOB_LOCATION:
                loc.getBlock().setType(Material.WOOL);
                loc.getBlock().setData((byte)5);
                break;
            case TOWN_LOCATION:
                loc.getBlock().setType(Material.WOOL);
                loc.getBlock().setData((byte)3);
                break;
            case MOB_ACTIVATION_1:
                loc.getBlock().setType(Material.WOOL);
                loc.getBlock().setData((byte)14);
                break;
            case MOB_ACTIVATION_2:
                loc.getBlock().setType(Material.WOOL);
                loc.getBlock().setData((byte)13);
                break;
            case CRAFTING_STATION:
                loc.getBlock().setType(Material.WOOL);
                loc.getBlock().setData((byte)11);
                break;
            case PVP_ARENA:
                loc.getBlock().setType(Material.WOOL);
                loc.getBlock().setData((byte)4);
                break;
            case PVP_RESPAWN:
                loc.getBlock().setType(Material.WOOL);
                loc.getBlock().setData((byte)1);
                break;
            case DUNGEON_GATEWAY:
                loc.getBlock().setType(Material.WOOL);
                loc.getBlock().setData((byte)7);
                break;
            case DUNGEON_ENTRY_POINT:
                loc.getBlock().setType(Material.WOOL);
                loc.getBlock().setData((byte)7);
                break;
        }
    }

    public static void prepareWorld(World w){
        w.setTime(0);
        w.setStorm(false);
        w.setThundering(false);

        w.setDifficulty(Difficulty.EASY);
        w.setAutoSave(true);
        w.setPVP(true);

        w.setGameRuleValue("doMobSpawning","false");
        w.setGameRuleValue("doFireTick","false");
        w.setGameRuleValue("naturalRegeneration","false");
        w.setGameRuleValue("doDaylightCycle","true");
    }

    public static String pathSuffix(String path){
        return path.endsWith(File.separator) ? "" : File.separator;
    }

    public static ArrayList<RegionLocation> sortedTownLocations(){
        ArrayList<RegionLocation> potential = new ArrayList<RegionLocation>();
        ArrayList<Region> regions = new ArrayList<Region>();
        regions.addAll(Region.getRegions(false));

        Collections.sort(regions, new Comparator<Region>() {
            @Override
            public int compare(Region o1, Region o2) {
                return Integer.compare(o1.getID(),o2.getID());
            }
        });

        for(Region region : Region.getRegions(false)) potential.addAll(region.getLocations(RegionLocationType.TOWN_LOCATION));

        return potential;
    }

    public static Location getNearestTown(Player p){
        if(GameUser.isLoaded(p)){
            GameUser u = GameUser.getUser(p);

            if(u.getCurrentCharacter() != null){
                return getNearestTown(p.getLocation());
            } else {
                return getCharSelLocation();
            }
        } else {
            return getCharSelLocation();
        }
    }

    public static Location getNearestTown(Location location){
        ArrayList<Location> potential = new ArrayList<Location>();
        Location loc = null;

        for(Region region : Region.getRegions(false)){
            for(RegionLocation l : region.getLocations(RegionLocationType.TOWN_LOCATION)){
                potential.add(l.toBukkitLocation());
            }
        }

        for(Location l : potential){
            if(loc == null){
                loc = l;
            } else {
                if(l.getWorld().getName().equals(location.getWorld().getName())){
                    if(!loc.getWorld().getName().equals(location.getWorld().getName())){
                        loc = l;
                    } else {
                        if(l.distance(location) < loc.distance(location)){
                            loc = l;
                        }
                    }
                }
            }
        }

        if(loc == null){
            return MAIN_WORLD.getSpawnLocation();
        } else {
            return loc;
        }
    }

    public static DungeonRPG getInstance() {
        return instance;
    }
}
