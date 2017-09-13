package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ProjectileHitListener implements Listener {
    @EventHandler
    public void onHit(ProjectileHitEvent e){
        if(e.getEntity().getType() == EntityType.ARROW) e.getEntity().remove();

        new BukkitRunnable(){
            @Override
            public void run() {
                if(DungeonRPG.SHOT_PROJECTILE_DATA.containsKey(e.getEntity().getUniqueId().toString())) DungeonRPG.SHOT_PROJECTILE_DATA.remove(e.getEntity().getUniqueId().toString());
            }
        }.runTask(DungeonRPG.getInstance());
    }
}
