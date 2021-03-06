package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.event.EntityStopTargetEvent;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.mobs.MobType;
import net.wrathofdungeons.dungeonrpg.npc.CustomNPC;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
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
                if(!c.getData().getAiSettings().mayDoRandomStroll()){
                    c.setCancelMovement(true);
                }
            }
        }
    }

    @EventHandler
    public void onTarget(EntityTargetLivingEntityEvent e){
        if(e.getEntity() == null || e.getTarget() == null) return;

        if(e.getReason() == EntityTargetEvent.TargetReason.OWNER_ATTACKED_TARGET || e.getReason() == EntityTargetEvent.TargetReason.TARGET_ATTACKED_OWNER){
            e.setCancelled(true);
            return;
        }

        if(e.getEntity() instanceof LivingEntity){
            LivingEntity entity = (LivingEntity)e.getEntity();
            CustomEntity c = CustomEntity.fromEntity(entity);

            if(c != null){
                MobType m = c.getData().getMobType();

                if(m == MobType.PASSIVE){
                    e.setCancelled(true);
                    return;
                }

                LivingEntity target = e.getTarget();
                CustomEntity ct = CustomEntity.fromEntity(target);

                if(ct == null){
                    if(CustomNPC.fromEntity(target) != null){
                        e.setCancelled(true);
                    } else {
                        if(target instanceof Player){
                            if(GameUser.isLoaded((Player)target)){
                                if(GameUser.getUser((Player)target).getCurrentCharacter() == null){
                                    e.setCancelled(true);
                                } else {
                                    if((c.getData().getMobType() == MobType.SUPPORTING || c.getData().getMobType() == MobType.PASSIVE)){
                                        e.setCancelled(true);
                                    } else if(m == MobType.NEUTRAL && !c.getDamagers().contains(target)){
                                        e.setCancelled(true);
                                    }
                                }
                            } else {
                                e.setCancelled(true);
                            }
                        } else {
                            e.setCancelled(true);
                        }
                    }
                } else {
                    MobType mt = ct.getData().getMobType();

                    if (m == MobType.NEUTRAL && !c.getDamagers().contains(target)) {
                        e.setCancelled(true);
                    } else if (!DungeonRPG.mayAttack(c.getData().getMobType(), mt)) {
                        e.setCancelled(true);
                    }
                }

                if(!e.isCancelled() && !c.getData().getAiSettings().mayDoRandomStroll()){
                    c.setCancelMovement(false);
                }
            }
        }
    }
}
