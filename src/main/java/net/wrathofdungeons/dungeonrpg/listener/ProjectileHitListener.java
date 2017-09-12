package net.wrathofdungeons.dungeonrpg.listener;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

public class ProjectileHitListener implements Listener {
    @EventHandler
    public void onHit(ProjectileHitEvent e){
        if(e.getEntity().getType() == EntityType.ARROW) e.getEntity().remove();
    }
}