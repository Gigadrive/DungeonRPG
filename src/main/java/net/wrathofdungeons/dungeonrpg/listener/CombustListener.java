package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;

public class CombustListener implements Listener {
    @EventHandler
    public void onCombust(EntityCombustEvent e){
        if (e instanceof EntityCombustByEntityEvent || e instanceof EntityCombustByBlockEvent) {
            return;
        }

        if(e.getEntity() instanceof LivingEntity){
            LivingEntity entity = (LivingEntity)e.getEntity();
            CustomEntity c = CustomEntity.fromEntity(entity);

            if(c != null){
                if(e.getDuration() == 8){
                    e.setCancelled(true);
                }
            }
        }
    }
}
