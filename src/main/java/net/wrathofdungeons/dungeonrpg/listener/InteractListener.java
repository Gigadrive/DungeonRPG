package net.wrathofdungeons.dungeonrpg.listener;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.Particle;
import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.util.BountifulAPI;
import net.wrathofdungeons.dungeonapi.util.ParticleEffect;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.damage.DamageManager;
import net.wrathofdungeons.dungeonrpg.damage.DamageSource;
import net.wrathofdungeons.dungeonrpg.event.CustomNPCInteractEvent;
import net.wrathofdungeons.dungeonrpg.inv.GameMenu;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.items.ItemCategory;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.npc.CustomNPC;
import net.wrathofdungeons.dungeonrpg.projectile.DungeonProjectile;
import net.wrathofdungeons.dungeonrpg.projectile.DungeonProjectileType;
import net.wrathofdungeons.dungeonrpg.regions.Region;
import net.wrathofdungeons.dungeonrpg.regions.RegionLocation;
import net.wrathofdungeons.dungeonrpg.regions.RegionLocationType;
import net.wrathofdungeons.dungeonrpg.skill.Skill;
import net.wrathofdungeons.dungeonrpg.skill.SkillStorage;
import net.wrathofdungeons.dungeonrpg.skill.magician.Blinkpool;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import org.apache.logging.log4j.core.Filter;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Wool;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import static net.wrathofdungeons.dungeonrpg.regions.RegionLocationType.*;

public class InteractListener implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e){
        Player p = e.getPlayer();

        if(GameUser.isLoaded(p)){
            GameUser u = GameUser.getUser(p);

            if(u.isInSetupMode()){
                if(e.getAction() == Action.RIGHT_CLICK_BLOCK){
                    if(p.getItemInHand() != null && p.getItemInHand().hasItemMeta() && p.getItemInHand().getItemMeta().hasDisplayName()){
                        String dis = p.getItemInHand().getItemMeta().getDisplayName();

                        if(DungeonRPG.SETUP_REGION > 0){
                            Location loc = e.getClickedBlock().getLocation().clone().add(0,1,0);

                            if(e.getClickedBlock() != null && e.getClickedBlock().getType() != null && DungeonRPG.SETUP_ADD_NO_Y.contains(e.getClickedBlock().getType())){
                                loc = e.getClickedBlock().getLocation();
                            }

                            Region region = Region.getRegion(DungeonRPG.SETUP_REGION);
                            if(region != null){
                                boolean b = true;

                                for(RegionLocation l : region.getLocations()){
                                    if(Util.isLocationEqual(loc,l.toBukkitLocation())) b = false;
                                }

                                if(b){
                                    RegionLocation rl = new RegionLocation();

                                    rl.world = loc.getWorld().getName();
                                    rl.x = loc.getBlockX();
                                    rl.y = loc.getBlockY();
                                    rl.z = loc.getBlockZ();

                                    if(dis.equals("Mob Spawn Setter")){
                                        rl.type = MOB_LOCATION;
                                        region.getLocations().add(rl);
                                        p.sendMessage(ChatColor.GREEN + "Location added!");
                                    } else if(dis.equals("Mob Activation Setter (1)")){
                                        rl.type = MOB_ACTIVATION_1;
                                        region.getLocations().add(rl);
                                        p.sendMessage(ChatColor.GREEN + "Location added!");
                                    } else if(dis.equals("Mob Activation Setter (2)")){
                                        rl.type = MOB_ACTIVATION_2;
                                        region.getLocations().add(rl);
                                        p.sendMessage(ChatColor.GREEN + "Location added!");
                                    }

                                    DungeonRPG.setLocationIndicator(loc,rl.type);
                                } else {
                                    p.sendMessage(ChatColor.RED + "That block already holds a location.");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "Unknown region.");
                            }
                        } else {
                            p.sendMessage(ChatColor.RED + "No region loaded.");
                        }
                    }
                }
            } else {
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

                                            u.ignoreFistCheck = true;

                                            for (LivingEntity target : DungeonRPG.getTargets(p, range, 2.0)) {
                                                CustomEntity entity = CustomEntity.fromEntity(target);

                                                if(entity != null) target.damage(5,p);
                                            }

                                            new BukkitRunnable(){
                                                @Override
                                                public void run() {
                                                    u.ignoreFistCheck = false;
                                                }
                                            }.runTask(DungeonRPG.getInstance());

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

                            GameMenu.openFor(p);
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
                                            data.setEntity(projectile);
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

                    if(u.canCastCombo && customItem.isMatchingWeapon(u.getCurrentCharacter().getRpgClass())){
                        /*u.canCastCombo = false;
                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRPG.getInstance(), new Runnable(){
                            public void run(){
                                u.canCastCombo = true;
                            }
                        }, 5L);*/

                        if(u.currentCombo.equals("") || u.currentCombo.isEmpty()){
                            if((e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) && (u.getCurrentCharacter().getRpgClass().matches(RPGClass.MERCENARY) || u.getCurrentCharacter().getRpgClass().matches(RPGClass.MAGICIAN) || u.getCurrentCharacter().getRpgClass().matches(RPGClass.ASSASSIN))){
                                u.currentCombo += "R";
                                u.comboDelay = 1;
                                u.updateClickComboBar();
                                p.playSound(p.getEyeLocation(), Sound.CLICK, 1F, 1F);
                                u.startComboResetTask();
                                return;
                            } else if((e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_AIR) && (u.getCurrentCharacter().getRpgClass().matches(RPGClass.ARCHER))){
                                u.currentCombo += "L";
                                u.comboDelay = 1;
                                u.updateClickComboBar();
                                p.playSound(p.getEyeLocation(), Sound.CLICK, 1F, 1F);
                                u.startComboResetTask();
                                return;
                            }
                        }

                        if(u.currentCombo.length() < 3 && u.currentCombo.length() >= 1){
                            if(e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR){
                                u.currentCombo = u.currentCombo + "R";
                                u.comboDelay = 1;
                                u.updateClickComboBar();
                                p.playSound(p.getEyeLocation(), Sound.CLICK, 1F, 1F);
                                if(u.currentCombo.length() != 3){
                                    u.startComboResetTask();
                                    return;
                                }

                                u.stopComboResetTask();
                            } else if(e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_AIR){
                                u.currentCombo = u.currentCombo + "L";
                                u.comboDelay = 1;
                                u.updateClickComboBar();
                                p.playSound(p.getEyeLocation(), Sound.CLICK, 1F, 1F);
                                if(u.currentCombo.length() != 3){
                                    u.startComboResetTask();
                                    return;
                                }

                                u.stopComboResetTask();
                            }

                            if(u.currentCombo.length() == 3){
                                Skill toCast = null;

                                for(Skill s : SkillStorage.getInstance().getSkills()){
                                    if(s.getRPGClass().matches(u.getCurrentCharacter().getRpgClass())){
                                        if(s.getCombo().equals(u.currentCombo)){
                                            toCast = s;
                                            break;
                                        }
                                    }
                                }

                                if(toCast != null){
                                    if(toCast.getType().getMinLevel() <= u.getCurrentCharacter().getLevel()){
                                        if(toCast.getType().getManaCost() <= u.getMP()){
                                            u.ignoreDamageCheck = true;
                                            boolean playAfter = toCast instanceof Blinkpool;

                                            if(!playAfter) p.playSound(p.getEyeLocation(),Sound.SUCCESSFUL_HIT,1f,0.5f);
                                            u.setMP(u.getMP()-toCast.getType().getManaCost());
                                            toCast.execute(p);

                                            if(!DungeonRPG.SHOW_HP_IN_ACTION_BAR) BountifulAPI.sendActionBar(p,ChatColor.GREEN + toCast.getName() + " " + ChatColor.GRAY + "[-" + toCast.getType().getManaCost() + " MP]");

                                            if(playAfter) p.playSound(p.getEyeLocation(),Sound.SUCCESSFUL_HIT,1f,0.5f);

                                            new BukkitRunnable(){
                                                @Override
                                                public void run() {
                                                    u.ignoreDamageCheck = false;
                                                }
                                            }.runTask(DungeonRPG.getInstance());
                                        } else {
                                            p.sendMessage(ChatColor.RED + "You don't have enough MP.");
                                        }
                                    } else {
                                        p.sendMessage(ChatColor.RED + "You haven't unlocked that skill yet.");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "That skill could not be found."); // shouldn't happen but print message if something goes wrong
                                }

                                u.currentCombo = "";
                                u.comboDelay = 0;
                                u.updateClickComboBar();
                                return;
                            }
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

    @EventHandler
    public void onRight(NPCRightClickEvent e){
        Player p = e.getClicker();
        NPC npc = e.getNPC();
        CustomNPC c = CustomNPC.fromCitizensNPC(npc);

        if(GameUser.isLoaded(p)){
            if(c != null){
                CustomNPCInteractEvent event = new CustomNPCInteractEvent(p,c);
                Bukkit.getPluginManager().callEvent(event);
            }
        }
    }

    @EventHandler
    public void onPush(NPCPushEvent e){
        e.setCancelled(true);
    }
}
