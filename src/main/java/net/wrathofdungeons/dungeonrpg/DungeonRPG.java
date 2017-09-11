package net.wrathofdungeons.dungeonrpg;

import org.bukkit.plugin.java.JavaPlugin;

public class DungeonRPG extends JavaPlugin {
    private static DungeonRPG instance;

    public void onEnable(){
        instance = this;
    }

    public static DungeonRPG getInstance() {
        return instance;
    }
}
