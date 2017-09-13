package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.items.ItemCategory;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import org.bukkit.ChatColor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

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
            }

            boolean adjustKnockback = false;

            if(e.getDamager() instanceof Player){
                Player p = (Player)e.getDamager();

                if(GameUser.isLoaded(p)){
                    GameUser u = GameUser.getUser(p);

                    if(u.getCurrentCharacter() != null){
                        if(p.getItemInHand() != null && CustomItem.fromItemStack(p.getItemInHand()) != null){
                            CustomItem item = CustomItem.fromItemStack(p.getItemInHand());

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

                            } else {
                                e.setDamage(0);
                                adjustKnockback = true;
                            }
                        }
                    }
                }
            }

            if(adjustKnockback){

            }
        }
    }
}
