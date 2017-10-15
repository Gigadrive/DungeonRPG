package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class DamageListener implements Listener {
    @EventHandler
    public void onDamage(EntityDamageEvent e){
        if(e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;

        /*if(e.getEntity().getType() == EntityType.ZOMBIE || e.getEntity().getType() == EntityType.SKELETON){
            e.getEntity().setFireTicks(0);

            if(e.getCause() == EntityDamageEvent.DamageCause.FIRE || e.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK){
                e.setCancelled(true);
            }
        }*/

        if(e.getEntity() instanceof LivingEntity){
            LivingEntity ent = (LivingEntity)e.getEntity();
            CustomEntity c = CustomEntity.fromEntity(ent);

            if(c != null){
                e.setCancelled(true);
                c.damage(e.getDamage());

                return;
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
            CustomEntity c = CustomEntity.fromEntity((LivingEntity)e.getEntity());

            if(c != null){
                if(e.getDamager() instanceof Projectile){
                    if(((Projectile) e.getDamager()).getShooter() != null && ((Projectile) e.getDamager()).getShooter() instanceof LivingEntity){
                        if(((Projectile) e.getDamager()).getShooter() == e.getEntity()){
                            e.setCancelled(true);
                            return;
                        }

                        CustomEntity cd = CustomEntity.fromEntity((LivingEntity)((Projectile) e.getDamager()).getShooter());

                        if(cd != null){
                            e.setCancelled(true);
                            e.getDamager().remove();
                            DungeonRPG.callMobToMobDamage(cd,c,true);
                        } else {
                            if(((Projectile) e.getDamager()).getShooter() instanceof Player){
                                Player p = (Player)((Projectile) e.getDamager()).getShooter();

                                if(GameUser.isLoaded(p)){
                                    GameUser u = GameUser.getUser(p);

                                    if(u.getCurrentCharacter() != null){
                                        e.setCancelled(true);
                                        e.getDamager().remove();
                                        DungeonRPG.callPlayerToMobDamage(p,c,true);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if(e.getDamager() instanceof LivingEntity){
                        LivingEntity l = (LivingEntity)e.getDamager();
                        CustomEntity cd = CustomEntity.fromEntity(l);

                        if(cd != null){
                            e.setCancelled(true);
                            DungeonRPG.callMobToMobDamage(cd,c,false);
                        } else {
                            e.setCancelled(true);
                        }
                    } else {
                        e.setCancelled(true);
                    }
                }
            } else {
                if(e.getEntity() instanceof Player){
                    Player p = (Player)e.getEntity();

                    if(GameUser.isLoaded(p)){
                        GameUser u = GameUser.getUser(p);

                        if(e.getDamager() instanceof Projectile){
                            if(((Projectile) e.getDamager()).getShooter() != null && ((Projectile) e.getDamager()).getShooter() instanceof LivingEntity && !(((Projectile) e.getDamager()).getShooter() instanceof Player)){
                                if(((Projectile) e.getDamager()).getShooter() == e.getEntity()){
                                    e.setCancelled(true);
                                    return;
                                }

                                CustomEntity cd = CustomEntity.fromEntity((LivingEntity) ((Projectile) e.getDamager()).getShooter());

                                if(cd != null){
                                    e.setCancelled(true);
                                    e.getDamager().remove();
                                    DungeonRPG.callMobToPlayerDamage(cd,p,true);
                                }
                            } else {
                                if(((Projectile) e.getDamager()).getShooter() instanceof Player){
                                    if(((Projectile) e.getDamager()).getShooter() == e.getEntity()){
                                        e.setCancelled(true);
                                        return;
                                    }

                                    Player p2 = (Player)((Projectile) e.getDamager()).getShooter();

                                    if(GameUser.isLoaded(p2)){
                                        GameUser u2 = GameUser.getUser(p2);

                                        if(u2.getCurrentCharacter() != null){
                                            e.setCancelled(true);
                                            e.getDamager().remove();
                                            DungeonRPG.callPlayerToPlayerDamage(p2,p,true);
                                        }
                                    }
                                }
                            }
                        } else {
                            if(e.getDamager() instanceof LivingEntity){
                                LivingEntity l = (LivingEntity)e.getDamager();
                                c = CustomEntity.fromEntity(l);

                                if(c != null){
                                    e.setCancelled(true);
                                    DungeonRPG.callMobToPlayerDamage(c,p,false);
                                } else {
                                    e.setCancelled(true);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /*@EventHandler
    public void onDamage(EntityDamageByEntityEvent e){
        Bukkit.broadcastMessage(String.valueOf(1));
        if(e.getEntity() instanceof LivingEntity){
            Bukkit.broadcastMessage(String.valueOf(2));
            LivingEntity ent = (LivingEntity)e.getEntity();
            ent.setNoDamageTicks(0);
            CustomEntity c = CustomEntity.fromEntity(ent);

            if(c != null){
                c.updateHealthBar();

                boolean adjustKnockback = false;
                boolean closerKnockbackLocation = false;

                if(e.getDamager() instanceof Player){
                    Player p = (Player)e.getDamager();

                    if(GameUser.isLoaded(p)){
                        GameUser u = GameUser.getUser(p);

                        if(!u.ignoreDamageCheck){
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
                                            //double damage = DamageManager.calculateDamage(p,ent, DamageSource.PVE, false, false, 0, true);
                                            double damage = DamageHandler.calculatePlayerToMobDamage(u,c,null);
                                            e.setDamage(damage);
                                            DungeonRPG.showBloodEffect(e.getEntity().getLocation());
                                            c.getData().playSound(e.getEntity().getLocation());
                                            adjustKnockback = true;

                                            int hpLeech = u.getCurrentCharacter().getTotalValue(AwakeningType.HP_LEECH);
                                            if(hpLeech > 0){
                                                u.addHP(damage*(hpLeech*0.01));
                                            }

                                            int mpLeech = u.getCurrentCharacter().getTotalValue(AwakeningType.MP_LEECH);
                                            if(mpLeech > 0){
                                                u.addMP(damage*(mpLeech*0.01));
                                            }

                                            new BukkitRunnable(){
                                                @Override
                                                public void run() {
                                                    u.setAttackCooldown(false);
                                                }
                                            }.runTaskLater(DungeonRPG.getInstance(),itemCooldown);
                                        }
                                    } else {
                                        if(u.ignoreFistCheck){
                                            //double damage = DamageManager.calculateDamage(p, ent, DamageSource.PVE, false, false);
                                            double damage = DamageHandler.calculatePlayerToMobDamage(u,c,null);
                                            e.setDamage(damage);
                                            DungeonRPG.showBloodEffect(e.getEntity().getLocation());
                                            c.getData().playSound(e.getEntity().getLocation());
                                            adjustKnockback = true;
                                            closerKnockbackLocation = true;

                                            int hpLeech = u.getCurrentCharacter().getTotalValue(AwakeningType.HP_LEECH);
                                            if(hpLeech > 0){
                                                u.addHP(damage*(hpLeech*0.01));
                                            }

                                            int mpLeech = u.getCurrentCharacter().getTotalValue(AwakeningType.MP_LEECH);
                                            if(mpLeech > 0){
                                                u.addMP(damage*(mpLeech*0.01));
                                            }
                                        } else {
                                            e.setCancelled(true);
                                            return;
                                        }
                                    }
                                } else {
                                    e.setCancelled(true);
                                    p.sendMessage(ChatColor.RED + "Please use a weapon.");
                                    return;
                                }
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
                            Player p = data.getPlayer();
                            GameUser u = GameUser.getUser(p);

                            if(data.getType() == DungeonProjectileType.EXPLOSION_ARROW){
                                e.setCancelled(true);
                                return;
                            }

                            if(data.getType() == DungeonProjectileType.DART_RAIN){
                                //double damage = data.getDamage();
                                double damage = DamageHandler.calculatePlayerToMobDamage(u,c,data.getSkill());
                                e.setDamage(damage);
                                DungeonRPG.showBloodEffect(e.getEntity().getLocation());
                                c.getData().playSound(e.getEntity().getLocation());
                            } else {
                                //e.setDamage(DamageManager.calculateDamage(data.getPlayer(), (LivingEntity)e.getEntity(), DamageSource.PVE, false, false, 0, true, data.getForce()));
                                e.setDamage(DamageHandler.calculatePlayerToMobDamage(u,c,data.getSkill()));
                                DungeonRPG.showBloodEffect(e.getEntity().getLocation());
                                c.getData().playSound(e.getEntity().getLocation());
                                adjustKnockback = true;
                            }

                            DungeonRPG.SHOT_PROJECTILE_DATA.remove(e.getDamager().getUniqueId().toString());
                        } else {
                            e.setCancelled(true);
                        }
                    }
                }

                if(adjustKnockback) c.giveNormalKnockback(e.getDamager().getLocation(),closerKnockbackLocation);

                if(!e.isCancelled()){
                    if(e.getDamager() instanceof Projectile){
                        LivingEntity shooter = (LivingEntity)((Projectile)e.getDamager()).getShooter();

                        if(c.getTarget() == null) c.setTarget(shooter);

                        c.getDamagers().add(shooter);
                    } else if(e.getDamager() instanceof LivingEntity) {
                        if(c.getTarget() == null) c.setTarget((LivingEntity)e.getDamager());
                        c.getDamagers().add(e.getDamager());
                    }
                }
            } else {
                Bukkit.broadcastMessage(String.valueOf(3));
                if(e.getDamager() instanceof Projectile) {
                    if (((Projectile) e.getDamager()).getShooter() == e.getEntity()) {
                        e.setCancelled(true);
                        return;
                    }

                    if(e.getEntity() instanceof Player){
                        Player p = (Player)e.getEntity();

                        if(GameUser.isLoaded(p)) {
                            GameUser u = GameUser.getUser(p);

                            if (u.getCurrentCharacter() == null) {
                                e.setCancelled(true);
                                return;
                            }

                            if (!e.isCancelled()) {
                                c = CustomEntity.fromEntity(((LivingEntity)((Projectile) e.getDamager()).getShooter()));

                                if(((Projectile) e.getDamager()).getShooter() instanceof LivingEntity && c != null){
                                    e.setDamage(0);
                                    double damage = DamageHandler.calculateMobToPlayerDamage(u,c);
                                    int thorns = u.getCurrentCharacter().getTotalValue(AwakeningType.THORNS);
                                    u.damage(damage,c.getBukkitEntity());

                                    if(thorns > 0){
                                        damage *= thorns*0.01;
                                        c.getBukkitEntity().damage(damage,p);
                                    }

                                    DungeonRPG.showBloodEffect(e.getEntity().getLocation());
                                }
                            }
                        }
                    }
                }

                if(e.getEntity() instanceof Player && !(e.getDamager() instanceof Player)){
                    Bukkit.broadcastMessage(String.valueOf(4));
                    Player p = (Player)e.getEntity();

                    if(GameUser.isLoaded(p)){
                        Bukkit.broadcastMessage(String.valueOf(5));
                        GameUser u = GameUser.getUser(p);

                        if(u.getCurrentCharacter() == null){
                            e.setCancelled(true);
                            return;
                        }

                        Bukkit.broadcastMessage(String.valueOf(6));

                        if(!e.isCancelled()){
                            Bukkit.broadcastMessage(String.valueOf(7));
                            if(e.getDamager() instanceof LivingEntity){
                                Bukkit.broadcastMessage(String.valueOf(8));
                                if(u.__associateDamageWithSystem){
                                    Bukkit.broadcastMessage(String.valueOf(9));
                                    c = CustomEntity.fromEntity((LivingEntity)e.getDamager());

                                    if(c != null){
                                        Bukkit.broadcastMessage(String.valueOf(10));
                                        e.setDamage(0);
                                        double damage = DamageHandler.calculateMobToPlayerDamage(u,c);
                                        int thorns = u.getCurrentCharacter().getTotalValue(AwakeningType.THORNS);
                                        u.damage(damage,(LivingEntity)e.getDamager());

                                        if(thorns > 0){
                                            damage *= thorns*0.01;
                                            c.getBukkitEntity().damage(damage,p);
                                        }

                                        DungeonRPG.showBloodEffect(e.getEntity().getLocation());
                                    } else {
                                        e.setCancelled(true);
                                    }
                                }
                            }
                        }
                    } else {
                        e.setCancelled(true);
                    }
                } else if(e.getEntity() instanceof Player && e.getDamager() instanceof Player){
                    Player p = (Player)e.getEntity();

                    if(GameUser.isLoaded(p) && CustomEntity.fromEntity((Player)e.getDamager()) != null){
                        GameUser u = GameUser.getUser(p);
                        c = CustomEntity.fromEntity((Player)e.getDamager());
                        e.setDamage(0);
                        double damage = DamageHandler.calculateMobToPlayerDamage(u,c);
                        int thorns = u.getCurrentCharacter().getTotalValue(AwakeningType.THORNS);
                        u.damage(damage,(LivingEntity)e.getDamager());

                        if(thorns > 0){
                            damage *= thorns*0.01;
                            c.getBukkitEntity().damage(damage,p);
                        }

                        DungeonRPG.showBloodEffect(e.getEntity().getLocation());
                    } else if(GameUser.isLoaded((Player)e.getDamager()) && CustomEntity.fromEntity(p) != null){
                        p = (Player)e.getDamager();
                        GameUser u = GameUser.getUser((Player)e.getEntity());

                        p.sendMessage("shouldn't be here..");
                    } else if(GameUser.isLoaded(p) && GameUser.isLoaded((Player)e.getDamager())) {
                        Player p2 = (Player)e.getDamager();
                        GameUser u = GameUser.getUser(p);
                        GameUser u2 = GameUser.getUser(p2);

                        // TODO: handle duels
                        e.setCancelled(true);
                    }
                }
            }
        }
    }*/
}
