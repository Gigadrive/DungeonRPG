package net.wrathofdungeons.dungeonrpg.listener;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.Particle;
import net.wrathofdungeons.dungeonapi.util.ParticleEffect;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.damage.DamageManager;
import net.wrathofdungeons.dungeonrpg.damage.DamageSource;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.items.ItemCategory;
import net.wrathofdungeons.dungeonrpg.projectile.DungeonProjectile;
import net.wrathofdungeons.dungeonrpg.projectile.DungeonProjectileType;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import org.apache.logging.log4j.core.Filter;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

public class InteractListener implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e){
        Player p = e.getPlayer();

        if(GameUser.isLoaded(p)){
            GameUser u = GameUser.getUser(p);

            if(e.getAction() == Action.PHYSICAL){
                if(e.getClickedBlock() != null && e.getClickedBlock().getType() == Material.SOIL){
                    e.setCancelled(true);
                }
            }

            if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (p.getInventory().getItemInHand() != null) {
                    if (DungeonRPG.DISALLOWED_ITEMS.contains(p.getInventory().getItemInHand().getType())) {
                        e.setCancelled(true);
                        e.setUseInteractedBlock(Event.Result.DENY);
                        e.setUseItemInHand(Event.Result.DENY);
                        p.updateInventory();
                    }
                }
            }

            if(p.getItemInHand() != null && CustomItem.fromItemStack(p.getItemInHand()) != null && e.getAction() != Action.PHYSICAL) {
                CustomItem item = CustomItem.fromItemStack(p.getItemInHand());

                if(item.getData().getCategory() == ItemCategory.WEAPON_BOW){
                    if(u.getCurrentCharacter().getRpgClass() != RPGClass.ARCHER && u.getCurrentCharacter().getRpgClass() != RPGClass.HUNTER && u.getCurrentCharacter().getRpgClass() != RPGClass.RANGER){
                        p.sendMessage(ChatColor.DARK_RED + "This weapon is for a different class.");
                        e.setCancelled(true);
                        return;
                    }

                    if(u.getCurrentCharacter().getLevel() < item.getData().getNeededLevel()){
                        p.sendMessage(ChatColor.DARK_RED + "This weapon is for level " + item.getData().getNeededLevel() + "+ only.");
                        e.setCancelled(true);
                        return;
                    }
                } else if(item.getData().getCategory() == ItemCategory.WEAPON_SHEARS){
                    if(u.getCurrentCharacter().getRpgClass() != RPGClass.ASSASSIN && u.getCurrentCharacter().getRpgClass() != RPGClass.NINJA && u.getCurrentCharacter().getRpgClass() != RPGClass.BLADEMASTER){
                        p.sendMessage(ChatColor.DARK_RED + "This weapon is for a different class.");
                        e.setCancelled(true);
                        return;
                    }

                    if(u.getCurrentCharacter().getLevel() < item.getData().getNeededLevel()){
                        p.sendMessage(ChatColor.DARK_RED + "This weapon is for level " + item.getData().getNeededLevel() + "+ only.");
                        e.setCancelled(true);
                        return;
                    }
                } else if(item.getData().getCategory() == ItemCategory.WEAPON_STICK){
                    if(u.getCurrentCharacter().getRpgClass() != RPGClass.MAGICIAN && u.getCurrentCharacter().getRpgClass() != RPGClass.WIZARD && u.getCurrentCharacter().getRpgClass() != RPGClass.ALCHEMIST){
                        p.sendMessage(ChatColor.DARK_RED + "This weapon is for a different class.");
                        e.setCancelled(true);
                        return;
                    }

                    if(u.getCurrentCharacter().getLevel() < item.getData().getNeededLevel()){
                        p.sendMessage(ChatColor.DARK_RED + "This weapon is for level " + item.getData().getNeededLevel() + "+ only.");
                        e.setCancelled(true);
                        return;
                    }
                } else if(item.getData().getCategory() == ItemCategory.WEAPON_AXE){
                    if(u.getCurrentCharacter().getRpgClass() != RPGClass.MERCENARY && u.getCurrentCharacter().getRpgClass() != RPGClass.SOLDIER && u.getCurrentCharacter().getRpgClass() != RPGClass.KNIGHT){
                        p.sendMessage(ChatColor.DARK_RED + "This weapon is for a different class.");
                        e.setCancelled(true);
                        return;
                    }

                    if(u.getCurrentCharacter().getLevel() < item.getData().getNeededLevel()){
                        p.sendMessage(ChatColor.DARK_RED + "This weapon is for level " + item.getData().getNeededLevel() + "+ only.");
                        e.setCancelled(true);
                        return;
                    }
                }
            }

            if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (DungeonRPG.DISALLOWED_BLOCKS.contains(e.getClickedBlock().getType())) {
                    if (p.getGameMode() != GameMode.CREATIVE) {
                        e.setCancelled(true);
                        e.setUseInteractedBlock(Event.Result.DENY);
                        e.setUseItemInHand(Event.Result.DENY);
                        return;
                    }
                }

                // TODO: Add loot chests
            }

            if(p.getItemInHand() != null && p.getItemInHand().getType() != null && p.getItemInHand().getItemMeta() != null && CustomItem.fromItemStack(p.getItemInHand()) != null) {
                final CustomItem customItem = CustomItem.fromItemStack(p.getItemInHand());
                long itemCooldown = 20; // TODO: Add attack speed

                if (u.getCurrentCharacter() == null) return;
                if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
                    if (customItem.getData().getCategory() == ItemCategory.WEAPON_STICK) {
                        if (u.getCurrentCharacter().getRpgClass() == RPGClass.MAGICIAN) {
                            if (customItem.getData().getNeededLevel() <= u.getCurrentCharacter().getLevel()) {
                                if (!u.isInAttackCooldown()) {
                                    e.setCancelled(true);
                                    e.setUseInteractedBlock(Event.Result.DENY);
                                    e.setUseItemInHand(Event.Result.DENY);

                                    if (u.currentCombo.equals("")) {
                                        Location loc = p.getEyeLocation();
                                        int range = 15;
									/*BlockIterator blocksToAdd = new BlockIterator(loc, 0D, range);
									Location blockToAdd = null;
									while(blocksToAdd.hasNext()){
										if(blockToAdd != null){
											if(blockToAdd.getBlock().getType() != Material.AIR){
												break;
											}

											p.getLocation().getWorld().spigot().playEffect(blockToAdd, Effect.CLOUD, 0, 0, 0.0000000001F, 0.0000000001F, 0.0000000001F, 0.05F, 10, 60);
										}

										blockToAdd = blocksToAdd.next().getLocation();
									}*/

                                        BlockIterator blocksToAdd = new BlockIterator(loc, 0D, range);
                                        Location lastLoc = null;
                                        while (blocksToAdd.hasNext()) {
                                            lastLoc = blocksToAdd.next().getLocation();
                                        }

                                        int c = (int) Math.ceil(loc.distance(lastLoc) / 2F) - 1;
                                        if (c > 0) {
                                            Vector v = lastLoc.toVector().subtract(loc.toVector()).normalize().multiply(2F);
                                            Location l = loc.clone();
                                            for (int i = 0; i < c; i++) {
                                                l.add(v);
                                                ParticleEffect.CLOUD.display(0f,0f,0f,0.0005f,1,l,600);
                                            }
                                        }

                                        for (LivingEntity target : DungeonRPG.getTargets(p, range, 2.0)) {
                                            if (target.getType() != EntityType.PLAYER) {
                                                target.damage(DamageManager.calculateDamage(p, target, DamageSource.PVE, false, false), p);
                                                DungeonRPG.showBloodEffect(target.getLocation());
                                            }
                                        }

                                        u.setAttackCooldown(true);

                                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRPG.getInstance(), new Runnable() {
                                            public void run() {
                                                u.setAttackCooldown(false);
                                            }
                                        }, itemCooldown);
                                    }
                                }
                            } else {
                                p.sendMessage(ChatColor.DARK_RED + "This weapon is for level " + customItem.getData().getNeededLevel() + "+ only.");
                            }
                        } else {
                            p.sendMessage(ChatColor.DARK_RED + "This weapon is for a different class.");
                        }
                    }
                }

                if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if (customItem.getData().getId() == 5) {
                        e.setCancelled(true);
                        e.setUseInteractedBlock(Event.Result.DENY);
                        e.setUseItemInHand(Event.Result.DENY);

                        // TODO: Open character menu
                    }

                    if(customItem.getData().getCategory() == ItemCategory.WEAPON_BOW){
                        if(!DungeonRPG.ENABLE_BOWDRAWBACK && u.getCurrentCharacter().getRpgClass() == RPGClass.ARCHER){
                            if(customItem.getData().getNeededLevel() <= u.getCurrentCharacter().getLevel()){
                                if(!u.isInAttackCooldown()){
                                    e.setCancelled(true);
                                    e.setUseInteractedBlock(Event.Result.DENY);
                                    e.setUseItemInHand(Event.Result.DENY);

                                    if(u.currentCombo.equals("")){
                                        Location loc = p.getEyeLocation();

                                        double damage = 1;
                                        damage += Util.randomDouble(customItem.getData().getAtkMin(), customItem.getData().getAtkMax());

                                        /*int dexToAdd = u.currentCharacter.getAttributeValue(AttributeType.STRENGTH) + u.currentCharacter.getArtificialAttributeValue(AttributeType.STRENGTH);
                                        if((dexToAdd + "").startsWith("-")){
                                            for (int i = 0; i > dexToAdd; i--) {
                                                damage -= 0.25;
                                            }
                                        } else {
                                            for (int i = 0; i < dexToAdd; i++) {
                                                damage += 0.25;
                                            }
                                        }

                                        if(damage < 1) damage = 1;*/

                                        Arrow projectile = p.launchProjectile(Arrow.class);
                                        p.getWorld().playSound(p.getEyeLocation(), Sound.SHOOT_ARROW, 1F, 1F);
                                        projectile.setVelocity(p.getLocation().getDirection().multiply(2.0D));
                                        DungeonProjectile data = new DungeonProjectile(p, DungeonProjectileType.ARCHER_ARROW, projectile.getLocation(), 0, damage, false);
                                        DungeonRPG.SHOT_PROJECTILE_DATA.put(projectile.getUniqueId().toString(), data);

                                        u.setAttackCooldown(true);

                                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRPG.getInstance(), new Runnable(){
                                            public void run(){
                                                u.setAttackCooldown(false);
                                            }
                                        }, itemCooldown);
                                    }
                                }
                            } else {
                                p.sendMessage(ChatColor.DARK_RED + "This weapon is for level " + customItem.getData().getNeededLevel() + "+ only.");
                            }
                        } else {
                            p.sendMessage(ChatColor.DARK_RED + "This weapon is for a different class.");
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityInteract(EntityInteractEvent e){
        if(e.getEntity().getType() == EntityType.PLAYER) return;

        if(e.getBlock() != null && e.getBlock().getType() == Material.SOIL){
            e.setCancelled(true);
        }
    }
}
