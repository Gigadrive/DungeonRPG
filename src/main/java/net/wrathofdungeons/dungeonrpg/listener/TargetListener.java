package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonrpg.event.EntityStopTargetEvent;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.mobs.MobType;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

public class TargetListener implements Listener {
    @EventHandler
    public void onTarget(EntityTargetEvent e){
        if(e.getTarget() == null){
            EntityStopTargetEvent event = new EntityStopTargetEvent(e.getEntity());
            Bukkit.getPluginManager().callEvent(event);
        }
    }

    @EventHandler
    public void onStop(EntityStopTargetEvent e){
        if(e.getEntity() instanceof LivingEntity){
            LivingEntity entity = (LivingEntity)e.getEntity();
            CustomEntity c = CustomEntity.fromEntity(entity);

            if(c != null){
                if(!c.getData().getAiSettings().mayDoRandomStroll()) c.setCancelMovement(true);
            }
        }
    }

    @EventHandler
    public void onTarget(EntityTargetLivingEntityEvent e){
        if(e.getReason() == EntityTargetEvent.TargetReason.OWNER_ATTACKED_TARGET || e.getReason() == EntityTargetEvent.TargetReason.TARGET_ATTACKED_OWNER){
            e.setCancelled(true);
            return;
        }

        if(e.getEntity() instanceof LivingEntity){
            LivingEntity entity = (LivingEntity)e.getEntity();
            CustomEntity c = CustomEntity.fromEntity(entity);

            if(c != null){
                LivingEntity target = e.getTarget();
                CustomEntity ct = CustomEntity.fromEntity(target);

                if(ct == null){
                    if((c.getData().getMobType() == MobType.SUPPORTING || c.getData().getMobType() == MobType.PASSIVE) && target instanceof Player){
                        e.setCancelled(true);
                    }
                } else {

                }

                if(!e.isCancelled() && !c.getData().getAiSettings().mayDoRandomStroll()) c.setCancelMovement(false);
            }
        }
    }
}
