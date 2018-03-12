package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.util.WorldUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.SheepDyeWoolEvent;
import org.bukkit.event.entity.SheepRegrowWoolEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class CreatureListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpawn(CreatureSpawnEvent e){
        if(e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM){
            e.setCancelled(true);
        }

        new BukkitRunnable(){
            @Override
            public void run() {
                if(e.getEntity() != null && e.getEntity().isValid())
                    if(!WorldUtilities.isValidEntity(e.getEntity()))
                        e.getEntity().remove();
            }
        }.runTaskLater(DungeonRPG.getInstance(),10);
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
