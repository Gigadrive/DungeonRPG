package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.damage.DamageManager;
import net.wrathofdungeons.dungeonrpg.damage.DamageSource;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.items.ItemCategory;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.projectile.DungeonProjectile;
import net.wrathofdungeons.dungeonrpg.projectile.DungeonProjectileType;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class DamageListener implements Listener {
    @EventHandler
    public void onDamage(EntityDamageEvent e){
        if(e.getEntity().getType() == EntityType.ZOMBIE || e.getEntity().getType() == EntityType.SKELETON){
            e.getEntity().setFireTicks(0);

            if(e.getCause() == EntityDamageEvent.DamageCause.FIRE || e.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK){
                e.setCancelled(true);
            }
        }

        if(e.getEntity() instanceof Player){
            Player p = (Player)e.getEntity();

            if(GameUser.isLoaded(p)){
                GameUser u = GameUser.getUser(p);

                if(u.getCurrentCharacter() != null){
                    if(e.getCause() == EntityDamageEvent.DamageCause.STARVATION || e.getCause() == EntityDamageEvent.DamageCause.FALL){
                        e.setCancelled(true);
                        return;
                    }

                    if(!e.isCancelled()){
                        u.damage(e.getDamage());
                        e.setDamage(0);
                    }
                } else {
                    e.setCancelled(true);
                }
            } else {
                e.setCancelled(true);
            }
        }

        if(!e.isCancelled() && (e.getEntity() instanceof LivingEntity && !(e.getEntity() instanceof ArmorStand)) && !(e.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION || e.getCause() == EntityDamageEvent.DamageCause.DROWNING || e.getCause() == EntityDamageEvent.DamageCause.STARVATION || e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || e.getCause() == EntityDamageEvent.DamageCause.PROJECTILE)){
            DungeonRPG.showBloodEffect(e.getEntity().getLocation());
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e){
        if(e.getEntity() instanceof LivingEntity){
            LivingEntity ent = (LivingEntity)e.getEntity();
            ent.setNoDamageTicks(0);
            CustomEntity c = CustomEntity.fromEntity(ent);

            if(c != null){
                c.updateHealthBar();

                boolean adjustKnockback = false;

                if(e.getDamager() instanceof Player){
                    Player p = (Player)e.getDamager();

                    if(GameUser.isLoaded(p)){
                        GameUser u = GameUser.getUser(p);

                        if(u.getCurrentCharacter() != null){
                            if(p.getItemInHand() != null && CustomItem.fromItemStack(p.getItemInHand()) != null){
                                CustomItem item = CustomItem.fromItemStack(p.getItemInHand());
                                long itemCooldown = 10; // TODO: Add attack speed

                                boolean wrongClass = false;

                                if(item.getData().getCategory() == ItemCategory.WEAPON_BOW && !(u.getCurrentCharacter().getRpgClass() == RPGClass.ARCHER || u.getCurrentCharacter().getRpgClass() == RPGClass.RANGER || u.getCurrentCharacter().getRpgClass() == RPGClass.HUNTER)){
                                    wrongClass = true;
                                }

                                if(item.getData().getCategory() == ItemCategory.WEAPON_STICK && !(u.getCurrentCharacter().getRpgClass() == RPGClass.MAGICIAN || u.getCurrentCharacter().getRpgClass() == RPGClass.WIZARD || u.getCurrentCharacter().getRpgClass() == RPGClass.ALCHEMIST)){
                                    wrongClass = true;
                                }

                                if(item.getData().getCategory() == ItemCategory.WEAPON_AXE && !(u.getCurrentCharacter().getRpgClass() == RPGClass.MERCENARY || u.getCurrentCharacter().getRpgClass() == RPGClass.KNIGHT || u.getCurrentCharacter().getRpgClass() == RPGClass.SOLDIER)){
                                    wrongClass = true;
                                }

                                if(item.getData().getCategory() == ItemCategory.WEAPON_SHEARS && !(u.getCurrentCharacter().getRpgClass() == RPGClass.ASSASSIN || u.getCurrentCharacter().getRpgClass() == RPGClass.BLADEMASTER || u.getCurrentCharacter().getRpgClass() == RPGClass.NINJA)){
                                    wrongClass = true;
                                }

                                if(item.getData().getNeededClass() != RPGClass.NONE){
                                    if(item.getData().getNeededClass() == RPGClass.MERCENARY){
                                        if(u.getCurrentCharacter().getRpgClass() != RPGClass.MERCENARY && u.getCurrentCharacter().getRpgClass() != RPGClass.KNIGHT && u.getCurrentCharacter().getRpgClass() != RPGClass.SOLDIER) wrongClass = true;
                                    } else if(item.getData().getNeededClass() == RPGClass.MAGICIAN){
                                        if(u.getCurrentCharacter().getRpgClass() != RPGClass.MAGICIAN && u.getCurrentCharacter().getRpgClass() != RPGClass.WIZARD && u.getCurrentCharacter().getRpgClass() != RPGClass.ALCHEMIST) wrongClass = true;
                                    } else if(item.getData().getNeededClass() == RPGClass.ASSASSIN){
                                        if(u.getCurrentCharacter().getRpgClass() != RPGClass.ASSASSIN && u.getCurrentCharacter().getRpgClass() != RPGClass.BLADEMASTER && u.getCurrentCharacter().getRpgClass() != RPGClass.NINJA) wrongClass = true;
                                    } else if(item.getData().getNeededClass() == RPGClass.ARCHER){
                                        if(u.getCurrentCharacter().getRpgClass() != RPGClass.ARCHER && u.getCurrentCharacter().getRpgClass() != RPGClass.RANGER && u.getCurrentCharacter().getRpgClass() != RPGClass.HUNTER) wrongClass = true;
                                    } else {
                                        if(u.getCurrentCharacter().getRpgClass() != item.getData().getNeededClass()) wrongClass = true;
                                    }
                                }

                                if(wrongClass){
                                    p.sendMessage(ChatColor.DARK_RED + "This weapon is for a different class.");
                                    e.setCancelled(true);
                                    return;
                                }

                                if(item.getData().getCategory() == ItemCategory.WEAPON_SHEARS || item.getData().getCategory() == ItemCategory.WEAPON_AXE){
                                    if(u.isInAttackCooldown()){
                                        e.setCancelled(true);
                                        return;
                                    } else {
                                        u.setAttackCooldown(true);
                                        e.setDamage(DamageManager.calculateDamage(p,ent, DamageSource.PVE, false, false, 0, true));
                                        DungeonRPG.showBloodEffect(e.getEntity().getLocation());

                                        new BukkitRunnable(){
                                            @Override
                                            public void run() {
                                                u.setAttackCooldown(false);
                                            }
                                        }.runTaskLater(DungeonRPG.getInstance(),itemCooldown);
                                    }
                                } else {
                                    e.setDamage(0);
                                    DungeonRPG.showBloodEffect(e.getEntity().getLocation());
                                    adjustKnockback = true;
                                }
                            } else {
                                e.setDamage(0);
                                DungeonRPG.showBloodEffect(e.getEntity().getLocation());
                                adjustKnockback = true;
                            }
                        }
                    }
                }

                if(e.getDamager() instanceof Arrow){
                    if(((Arrow)e.getDamager()).getShooter() == e.getEntity()){
                        e.setCancelled(true);
                        return;
                    }

                    if(!e.isCancelled()) {
                        if (DungeonRPG.SHOT_PROJECTILE_DATA.containsKey(e.getDamager().getUniqueId().toString())) {
                            DungeonProjectile data = DungeonRPG.SHOT_PROJECTILE_DATA.get(e.getDamager().getUniqueId().toString());

                            if(data.getType() == DungeonProjectileType.EXPLOSION_ARROW){
                                e.setCancelled(true);
                                return;
                            }

                            if(data.getType() == DungeonProjectileType.ARROW_RAIN){
                                e.setDamage(data.getDamage());
                                DungeonRPG.showBloodEffect(e.getEntity().getLocation());
                                // TODO: Spawn damage indicator
                            } else {
                                e.setDamage(DamageManager.calculateDamage(data.getPlayer(), (LivingEntity)e.getEntity(), DamageSource.PVE, false, false, 0, true, data.getForce()));
                                DungeonRPG.showBloodEffect(e.getEntity().getLocation());
                                adjustKnockback = true;
                            }

                            DungeonRPG.SHOT_PROJECTILE_DATA.remove(e.getDamager().getUniqueId().toString());
                        } else {
                            e.setCancelled(true);
                        }
                    }
                }

                if(adjustKnockback){

                }
            } else {
                if(e.getDamager() instanceof Arrow) {
                    if (((Arrow) e.getDamager()).getShooter() == e.getEntity()) {
                        e.setCancelled(true);
                        return;
                    }

                    if(e.getEntity() instanceof Player){
                        e.setCancelled(true);
                    }
                }
            }
        }
    }
}
