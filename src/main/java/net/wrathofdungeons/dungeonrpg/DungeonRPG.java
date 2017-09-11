package net.wrathofdungeons.dungeonrpg;

import net.wrathofdungeons.dungeonrpg.listener.CharacterCreationListener;
import net.wrathofdungeons.dungeonrpg.listener.PlayerJoinListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

public class DungeonRPG extends JavaPlugin {
    private static DungeonRPG instance;

    public void onEnable(){
        instance = this;
        saveDefaultConfig();

        registerListeners();
        registerCommands();
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

    private void registerListeners(){
        Bukkit.getPluginManager().registerEvents(new CharacterCreationListener(),this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(),this);
    }

    private void registerCommands(){

    }

    public static DungeonRPG getInstance() {
        return instance;
    }
}
