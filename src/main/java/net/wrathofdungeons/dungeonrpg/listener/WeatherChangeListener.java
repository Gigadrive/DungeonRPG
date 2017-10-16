package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class WeatherChangeListener implements Listener {
    @EventHandler
    public void onChange(WeatherChangeEvent e){
        e.setCancelled(true);
    }

    @EventHandler
    public void onLoad(WorldInitEvent e){
        System.out.println("INIT: " + e.getWorld().getName());

        new BukkitRunnable(){
            @Override
            public void run() {
                if(DungeonRPG.MAIN_WORLD == null){
                    DungeonRPG.MAIN_WORLD = e.getWorld();
                }

                DungeonRPG.prepareWorld(e.getWorld());
            }
        }.runTaskLater(DungeonRPG.getInstance(),3*20);
    }
}
