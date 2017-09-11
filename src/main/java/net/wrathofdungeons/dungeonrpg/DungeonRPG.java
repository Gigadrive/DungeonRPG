package net.wrathofdungeons.dungeonrpg;

import net.wrathofdungeons.dungeonrpg.listener.PlayerJoinListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class DungeonRPG extends JavaPlugin {
    private static DungeonRPG instance;

    public void onEnable(){
        instance = this;

        registerListeners();
        registerCommands();
    }

    private void registerListeners(){
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(),this);
    }

    private void registerCommands(){

    }

    public static DungeonRPG getInstance() {
        return instance;
    }
}
