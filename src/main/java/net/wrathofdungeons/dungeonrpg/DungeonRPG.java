package net.wrathofdungeons.dungeonrpg;

import net.wrathofdungeons.dungeonapi.user.User;
import net.wrathofdungeons.dungeonrpg.cmd.CharSelCommand;
import net.wrathofdungeons.dungeonrpg.cmd.GiveItemCommand;
import net.wrathofdungeons.dungeonrpg.cmd.ItemInfoCommand;
import net.wrathofdungeons.dungeonrpg.cmd.SummonCommand;
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
    public static final int PLAYER_MOB_LEVEL_DIFFERENCE = 7;
    public static HashMap<UUID, DungeonProjectile> SHOT_PROJECTILE_DATA = new HashMap<UUID, DungeonProjectile>();
    public static ArrayList<Material> DISALLOWED_BLOCKS = new ArrayList<Material>();

    public void onEnable(){
        instance = this;
        saveDefaultConfig();

        registerListeners();
        registerCommands();

        ItemData.init();
        MobData.init();
        Region.init();

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
        new SummonCommand();
    }

    public static DungeonRPG getInstance() {
        return instance;
    }
}
