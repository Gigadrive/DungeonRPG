package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.mobs.MobType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

public class TargetListener implements Listener {
    @EventHandler
    public void onTarget(EntityTargetLivingEntityEvent e){
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
            }
        }
    }
}
