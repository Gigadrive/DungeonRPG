package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.util.ParticleEffect;
import net.wrathofdungeons.dungeonrpg.Duel;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.damage.DamageData;
import net.wrathofdungeons.dungeonrpg.damage.DamageHandler;
import net.wrathofdungeons.dungeonrpg.items.awakening.AwakeningType;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.projectile.DungeonProjectile;
import net.wrathofdungeons.dungeonrpg.projectile.DungeonProjectileType;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ProjectileHitListener implements Listener {
    @EventHandler
    public void onHit(ProjectileHitEvent e){
        if(DungeonRPG.SHOT_PROJECTILE_DATA.containsKey(e.getEntity().getUniqueId().toString())){
            DungeonProjectile data = DungeonRPG.SHOT_PROJECTILE_DATA.get(e.getEntity().getUniqueId().toString());

            if(data.getPlayer() != null && data.getPlayer().isOnline()){
                Player p = data.getPlayer();

                if(GameUser.isLoaded(p)){
                    GameUser u = GameUser.getUser(p);

                    if(u.getCurrentCharacter() != null){
                        DungeonAPI.async(() -> {
                            if(data.getType() == DungeonProjectileType.EXPLOSION_ARROW){
                                if(data.getRange() == 4){
                                    ParticleEffect.EXPLOSION_NORMAL.display(0.005f,0.005f,0.005f,0.005f,3,e.getEntity().getLocation(),600);
                                    e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.ENTITY_GENERIC_EXPLODE,1f,1f);
                                } else if(data.getRange() == 6){
                                    ParticleEffect.EXPLOSION_NORMAL.display(0.05f,0.05f,0.05f,0.005f,8,e.getEntity().getLocation(),600);
                                    e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.ENTITY_GENERIC_EXPLODE,1f,1f);
                                } else if(data.getRange() == 8){
                                    ParticleEffect.EXPLOSION_LARGE.display(0.005f,0.005f,0.005f,0.005f,3,e.getEntity().getLocation(),600);
                                    e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.ENTITY_GENERIC_EXPLODE,1f,1f);
                                } else if(data.getRange() == 10){
                                    ParticleEffect.EXPLOSION_LARGE.display(0.05f,0.05f,0.05f,0.005f,8,e.getEntity().getLocation(),600);
                                    e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.ENTITY_GENERIC_EXPLODE,1f,1f);
                                } else if(data.getRange() == 12){
                                    ParticleEffect.EXPLOSION_HUGE.display(0.05f,0.05f,0.05f,0.005f,12,e.getEntity().getLocation(),600);
                                    e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.ENTITY_GENERIC_EXPLODE,1f,1f);
                                }

                                for(Entity ent : e.getEntity().getNearbyEntities(data.getRange(),data.getRange(),data.getRange())){
                                    if(ent instanceof LivingEntity){
                                        LivingEntity livingEntity = (LivingEntity)ent;
                                        CustomEntity c = CustomEntity.fromEntity(livingEntity);

                                        if(c != null){
                                            u.ignoreDamageCheck = true;
                                            u.ignoreFistCheck = true;
                                            //livingEntity.damage(data.getDamage(),p);
                                            //livingEntity.damage(DamageHandler.calculatePlayerToMobDamage(u,c,data.getSkill()),p);
                                            DamageData damageData = DamageHandler.calculatePlayerToMobDamage(u,c,data.getSkill());
                                            double damage = damageData.getDamage();
                                            int hpLeech = u.getCurrentCharacter().getTotalValue(AwakeningType.HP_LEECH);
                                            if(hpLeech > 0){
                                                u.addHP(damage*(hpLeech*0.01));
                                            }

                                            int mpLeech = u.getCurrentCharacter().getTotalValue(AwakeningType.MP_LEECH);
                                            if(mpLeech > 0){
                                                u.addMP(damage*(mpLeech*0.01));
                                            }

                                            DungeonAPI.sync(() -> DamageHandler.spawnDamageIndicator(p,damageData,c.getBukkitEntity().getLocation()));
                                            c.damage(damage,p);
                                            DungeonRPG.showBloodEffect(livingEntity.getLocation());
                                            c.getData().playSound(livingEntity.getLocation());

                                            new BukkitRunnable(){
                                                @Override
                                                public void run() {
                                                    u.ignoreDamageCheck = false;
                                                    u.ignoreFistCheck = false;
                                                }
                                            }.runTaskLater(DungeonRPG.getInstance(),1);
                                        } else {
                                            if(ent.getType() == EntityType.PLAYER){
                                                Player p2 = (Player)ent;

                                                if(GameUser.isLoaded(p2) && Duel.isDuelingWith(p,p2)){
                                                    GameUser u2 = GameUser.getUser(p2);

                                                    DamageData damageData = DamageHandler.calculatePlayerToPlayerDamage(u,u2,data.getSkill());
                                                    DungeonAPI.sync(() -> DamageHandler.spawnDamageIndicator(p,damageData,p2.getLocation()));
                                                    u2.damage(damageData.getDamage(),p);
                                                    DungeonRPG.showBloodEffect(livingEntity.getLocation());
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            }
        }

        if(e.getEntity().getType() == EntityType.ARROW) e.getEntity().remove();

        new BukkitRunnable(){
            @Override
            public void run() {
                DungeonRPG.SHOT_PROJECTILE_DATA.remove(e.getEntity().getUniqueId().toString());
            }
        }.runTask(DungeonRPG.getInstance());
    }
}
