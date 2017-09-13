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
import net.wrathofdungeons.dungeonrpg.regions.Region;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class DungeonRPG extends JavaPlugin {
    private static DungeonRPG instance;
    public static final boolean ENABLE_BOWDRAWBACK = false;

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
                        Location loc = e.getBukkitEntity().getLocation();
                        if(e.getBukkitEntity() instanceof LivingEntity) loc = ((LivingEntity)e.getBukkitEntity()).getEyeLocation();

                        e.getHologram().teleport(loc.clone().add(0,1,0));
                    }
                }
            }
        }.runTaskTimer(this,1,1);
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

    }

    private void registerListeners(){
        Bukkit.getPluginManager().registerEvents(new BlockListener(),this);
        Bukkit.getPluginManager().registerEvents(new CharacterCreationListener(),this);
        Bukkit.getPluginManager().registerEvents(new CraftListener(),this);
        Bukkit.getPluginManager().registerEvents(new DamageListener(),this);
        Bukkit.getPluginManager().registerEvents(new FoodListener(),this);
        Bukkit.getPluginManager().registerEvents(new InteractListener(),this);
        Bukkit.getPluginManager().registerEvents(new InventoryClickListener(),this);
        Bukkit.getPluginManager().registerEvents(new InventoryCloseListener(),this);
        Bukkit.getPluginManager().registerEvents(new PlayerDropListener(),this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(),this);
        Bukkit.getPluginManager().registerEvents(new PlayerMoveListener(),this);
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
