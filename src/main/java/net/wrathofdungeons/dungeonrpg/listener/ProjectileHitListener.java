package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonapi.util.ParticleEffect;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.damage.DamageData;
import net.wrathofdungeons.dungeonrpg.damage.DamageHandler;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.projectile.DungeonProjectile;
import net.wrathofdungeons.dungeonrpg.projectile.DungeonProjectileType;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.util.WorldUtilities;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

public class ProjectileHitListener implements Listener {
    @EventHandler
    public void onHit(ProjectileHitEvent e){
        if(DungeonRPG.SHOT_PROJECTILE_DATA.containsKey(e.getEntity().getUniqueId().toString())){
            DungeonProjectile data = DungeonRPG.SHOT_PROJECTILE_DATA.get(e.getEntity().getUniqueId().toString());

            if (data.getType() == DungeonProjectileType.EXPLOSION_ARROW) {
                if (data.getPlayer() != null && data.getPlayer().isOnline()) {
                    Player p = data.getPlayer();

                    if (GameUser.isLoaded(p)) {
                        GameUser u = GameUser.getUser(p);

                        if (u.getCurrentCharacter() != null) {
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

                                        DamageHandler.spawnDamageIndicator(p,damageData,c.getBukkitEntity().getLocation());
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

                                            if(DungeonRPG.mayAttack(p,p2)){
                                                GameUser u2 = GameUser.getUser(p2);

                                                p2.damage(0);
                                                DamageData damageData = DamageHandler.calculatePlayerToPlayerDamage(u,u2,data.getSkill());
                                                DamageHandler.spawnDamageIndicator(p,damageData,p2.getLocation());
                                                u2.damage(damageData.getDamage(),p);
                                                DungeonRPG.showBloodEffect(livingEntity.getLocation());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (data.getType() == DungeonProjectileType.MOB_FIREBALL) {
                ProjectileSource source = ((Projectile) e.getEntity()).getShooter();
                ParticleEffect.EXPLOSION_HUGE.display(0.05f, 0.05f, 0.05f, 0.005f, 5, e.getEntity().getLocation(), 600);

                for (Entity ent : e.getEntity().getNearbyEntities(8, 8, 8)) {
                    if (ent instanceof LivingEntity) {
                        LivingEntity livingEntity = (LivingEntity) ent;
                        CustomEntity c = CustomEntity.fromEntity(livingEntity);
                        CustomEntity shooter = CustomEntity.fromEntity((LivingEntity) source);

                        if (shooter != null) {
                            if (c != null) {
                                if (shooter.getData().getMobType().mayAttack(c.getData().getMobType())) {
                                    livingEntity.setVelocity(WorldUtilities.safenVelocity(livingEntity.getLocation().toVector().subtract(e.getEntity().getLocation().toVector()).normalize().multiply(1.15).setY(1.25)));
                                }
                            } else {
                                if (livingEntity instanceof Player) {
                                    Player p2 = (Player) livingEntity;

                                    if (GameUser.isLoaded(p2)) {
                                        GameUser u2 = GameUser.getUser(p2);

                                        if (u2.getCurrentCharacter() != null) {
                                            livingEntity.setVelocity(WorldUtilities.safenVelocity(livingEntity.getLocation().toVector().subtract(e.getEntity().getLocation().toVector()).normalize().multiply(1.15).setY(1.25)));
                                        }
                                    }
                                }
                            }
                        }
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
