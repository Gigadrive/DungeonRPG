package net.wrathofdungeons.dungeonrpg;

import net.wrathofdungeons.dungeonapi.user.User;
import net.wrathofdungeons.dungeonrpg.cmd.*;
import net.wrathofdungeons.dungeonrpg.inv.CharacterSelectionMenu;
import net.wrathofdungeons.dungeonrpg.items.ItemData;
import net.wrathofdungeons.dungeonrpg.listener.*;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.mobs.MobData;
import net.wrathofdungeons.dungeonrpg.projectile.DungeonProjectile;
import net.wrathofdungeons.dungeonrpg.regions.Region;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DungeonRPG extends JavaPlugin {
    private static DungeonRPG instance;
    public static final boolean ENABLE_BOWDRAWBACK = false;
    public static final boolean SHOW_HP_IN_ACTION_BAR = false;
    public static final int PLAYER_MOB_LEVEL_DIFFERENCE = 7;
    public static HashMap<String, DungeonProjectile> SHOT_PROJECTILE_DATA = new HashMap<String, DungeonProjectile>();
    public static ArrayList<Material> DISALLOWED_BLOCKS = new ArrayList<Material>();
    public static ArrayList<Material> DISALLOWED_ITEMS = new ArrayList<Material>();

    public void onEnable(){
        instance = this;
        saveDefaultConfig();

        registerListeners();
        registerCommands();

        ItemData.init();
        MobData.init();
        Region.init();

        DISALLOWED_BLOCKS.add(Material.CHEST);
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

        if(SHOW_HP_IN_ACTION_BAR){
            new BukkitRunnable(){
                @Override
                public void run() {
                    for(Player p : Bukkit.getOnlinePlayers()){
                        if(GameUser.isLoaded(p)){
                            GameUser u = GameUser.getUser(p);

                            if(u.getCurrentCharacter() != null){
                                u.updateHPBar();
                            }
                        }
                    }
                }
            }.runTaskTimer(this,20,20);
        }

        new BukkitRunnable(){
            @Override
            public void run() {
                for(CustomEntity e : CustomEntity.STORAGE.values()){
                    if(e.getHologram() != null && e.getBukkitEntity() != null){
                        e.getHologram().teleport(e.getSupposedHologramLocation());
                    }
                }
            }
        }.runTaskTimerAsynchronously(this,1,1);

        Bukkit.getServer().clearRecipes();
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

    public static void showBloodEffect(Location loc){
        loc.getWorld().playSound(loc, Sound.HURT_FLESH, 1F, 1F);
        loc.getWorld().playEffect(loc.add(0.0D, 0.8D, 0.0D), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
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
                            a.add((LivingEntity)e);
                        }
                    }
                }
            }
        }

        return a;
    }

    private void registerListeners(){
        Bukkit.getPluginManager().registerEvents(new BlockListener(),this);
        Bukkit.getPluginManager().registerEvents(new CharacterCreationListener(),this);
        Bukkit.getPluginManager().registerEvents(new ChunkListener(),this);
        Bukkit.getPluginManager().registerEvents(new CraftListener(),this);
        Bukkit.getPluginManager().registerEvents(new DamageListener(),this);
        Bukkit.getPluginManager().registerEvents(new DeathListener(),this);
        Bukkit.getPluginManager().registerEvents(new FoodListener(),this);
        Bukkit.getPluginManager().registerEvents(new FrameProtectListener(),this);
        Bukkit.getPluginManager().registerEvents(new InteractListener(),this);
        Bukkit.getPluginManager().registerEvents(new InventoryClickListener(),this);
        Bukkit.getPluginManager().registerEvents(new InventoryCloseListener(),this);
        Bukkit.getPluginManager().registerEvents(new PlayerDropListener(),this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(),this);
        Bukkit.getPluginManager().registerEvents(new PlayerMoveListener(),this);
        Bukkit.getPluginManager().registerEvents(new PlayerPickupListener(),this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(),this);
        Bukkit.getPluginManager().registerEvents(new ProjectileHitListener(),this);
        Bukkit.getPluginManager().registerEvents(new ShootBowListener(),this);
        Bukkit.getPluginManager().registerEvents(new WeatherChangeListener(),this);
    }

    private void registerCommands(){
        new CharSelCommand();
        new GiveItemCommand();
        new ItemInfoCommand();
        new SetupCommand();
        new SummonCommand();
    }

    public static DungeonRPG getInstance() {
        return instance;
    }
}
