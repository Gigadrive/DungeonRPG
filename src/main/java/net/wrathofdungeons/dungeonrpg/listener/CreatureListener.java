package net.wrathofdungeons.dungeonrpg.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.SheepDyeWoolEvent;
import org.bukkit.event.entity.SheepRegrowWoolEvent;

public class CreatureListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpawn(CreatureSpawnEvent e){
        if(e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onRegrow(SheepRegrowWoolEvent e){
        e.setCancelled(true);
    }

    @EventHandler
    public void onColor(SheepDyeWoolEvent e){
        e.setCancelled(true);
    }
}
