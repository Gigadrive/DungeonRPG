package net.wrathofdungeons.dungeonrpg.listener;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.Particle;
import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.MySQLManager;
import net.wrathofdungeons.dungeonapi.util.BountifulAPI;
import net.wrathofdungeons.dungeonapi.util.ParticleEffect;
import net.wrathofdungeons.dungeonapi.util.PlayerUtilities;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.damage.DamageHandler;
import net.wrathofdungeons.dungeonrpg.damage.DamageManager;
import net.wrathofdungeons.dungeonrpg.damage.DamageSource;
import net.wrathofdungeons.dungeonrpg.event.CustomNPCInteractEvent;
import net.wrathofdungeons.dungeonrpg.inv.GameMenu;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.items.ItemCategory;
import net.wrathofdungeons.dungeonrpg.items.ItemData;
import net.wrathofdungeons.dungeonrpg.items.ItemRarity;
import net.wrathofdungeons.dungeonrpg.lootchests.LootChest;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.mobs.MobClass;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Wool;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;

import static net.wrathofdungeons.dungeonrpg.regions.RegionLocationType.*;

public class InteractListener implements Listener {
    private ArrayList<String> TP_SCROLL_COOLDOWN = new ArrayList<String>();

    private void tpScroll(Player p, Location loc){
        if(TP_SCROLL_COOLDOWN.contains(p.getName())){
            p.sendMessage(ChatColor.RED + "Please wait a little while before teleporting again.");
        } else {
            if(p.getItemInHand() != null){
                if(p.getItemInHand().getAmount() == 1){
                    p.setItemInHand(new ItemStack(Material.AIR));
                } else {
                    p.getItemInHand().setAmount(p.getItemInHand().getAmount()-1);
                }
            }

            p.teleport(loc);

            TP_SCROLL_COOLDOWN.add(p.getName());

            new BukkitRunnable(){
                @Override
                public void run() {
                    TP_SCROLL_COOLDOWN.remove(p.getName());
                }
            }.runTaskLater(DungeonRPG.getInstance(),30*20);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e){
        Player p = e.getPlayer();

        if(GameUser.isLoaded(p)){
            GameUser u = GameUser.getUser(p);

            if(u.isInSetupMode()){
                if(e.getAction() == Action.RIGHT_CLICK_BLOCK){
                    if (DungeonRPG.DISALLOWED_BLOCKS.contains(e.getClickedBlock().getType())) {
                        e.setCancelled(true);
                        e.setUseInteractedBlock(Event.Result.DENY);
                        e.setUseItemInHand(Event.Result.DENY);
                    }

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

                        return;
                    }

                    LootChest c = LootChest.getChest(e.getClickedBlock().getLocation());
                    if(c != null){
                        p.sendMessage(ChatColor.YELLOW + "ID: " + c.getId());
                        p.sendMessage(ChatColor.YELLOW + "Level: " + c.getLevel());
                        p.sendMessage(ChatColor.YELLOW + "Tier: " + c.getTier().getId());
                        if(c.getAddedBy() != null) p.sendMessage(ChatColor.YELLOW + "Added By: " + PlayerUtilities.getNameFromUUID(c.getAddedBy()));
                    } else {
                        if(u.lootChestLevel > 0 && u.lootChestTier > 0){
                            try {
                                int chestID = 0;

                                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `lootchests` (`location.world`,`location.x`,`location.y`,`location.z`,`level`,`tier`,`addedBy`) VALUES(?,?,?,?,?,?,?);", Statement.RETURN_GENERATED_KEYS);
                                ps.setString(1,e.getClickedBlock().getLocation().getWorld().getName());
                                ps.setDouble(2,e.getClickedBlock().getLocation().getX());
                                ps.setDouble(3,e.getClickedBlock().getLocation().getY());
                                ps.setDouble(4,e.getClickedBlock().getLocation().getZ());
                                ps.setInt(5,u.lootChestLevel);
                                ps.setInt(6,u.lootChestTier);
                                ps.setString(7,p.getUniqueId().toString());
                                ps.executeUpdate();

                                ResultSet rs = ps.getGeneratedKeys();
                                if(rs.first()){
                                    chestID = rs.getInt(1);
                                }

                                MySQLManager.getInstance().closeResources(rs,ps);

                                if(chestID > 0){
                                    c = new LootChest(chestID);
                                    p.sendMessage(ChatColor.GREEN + "Loot Chest created! ID: " + c.getId());
                                    p.sendMessage(ChatColor.GREEN + "Level: " + c.getLevel());
                                    p.sendMessage(ChatColor.GREEN + "Tier: " + c.getTier());
                                } else {
                                    p.sendMessage(ChatColor.RED + "An error occurred.");
                                }
                            } catch(Exception e1){
                                e1.printStackTrace();
                            }

                            u.lootChestLevel = 0;
                            u.lootChestTier = 0;
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

                    if(e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR){
                        // TELEPORT SCROLLS

                        if(item.getData().getCategory() == ItemCategory.TELEPORT_SCROLL){
                            if(item.getData().getTpScrollRegion() > 0){
                                Region region = Region.getRegion(item.getData().getTpScrollRegion());

                                if(region != null){
                                    ArrayList<RegionLocation> locs = region.getLocations(TOWN_LOCATION,1);

                                    if(locs.size() > 0){
                                        tpScroll(p,locs.get(0).toBukkitLocation());
                                    } else {
                                        p.sendMessage(ChatColor.RED + "Teleport failed.");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "Teleport failed.");
                                }
                            }
                        } else if(item.getData().getCategory() == ItemCategory.FOOD){
                            e.setCancelled(true);
                            e.setUseInteractedBlock(Event.Result.DENY);
                            e.setUseItemInHand(Event.Result.DENY);

                            if(item.getData().getFoodRegeneration() > 0 && item.getData().getFoodDelayInTicks() >= 0){
                                if(u.getCurrentCharacter().getLevel() >= item.getData().getNeededLevel()){
                                    if(!u.isInFoodCooldown()){
                                        if(u.getHP() < u.getMaxHP()){
                                            u.consumeCurrentItem(1);
                                            u.addHP(item.getData().getFoodRegeneration());

                                            p.playSound(p.getEyeLocation(),Sound.EAT,1f,1f);
                                            ParticleEffect.HEART.display(0.005f,0.005f,0.005f,0.005f,30,p.getEyeLocation(),600);

                                            if(item.getData().getFoodDelayInTicks() > 0){
                                                u.setFoodCooldown(true);

                                                new BukkitRunnable(){
                                                    @Override
                                                    public void run() {
                                                        u.setFoodCooldown(false);
                                                    }
                                                }.runTaskLater(DungeonRPG.getInstance(),item.getData().getFoodDelayInTicks());
                                            }
                                        }
                                    } else {
                                        p.sendMessage(ChatColor.RED + "Please wait a little while before using that item again.");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.DARK_RED + "This item is for level " + item.getData().getNeededLevel() + "+ only.");
                                }
                            }
                        }
                    }

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
                        e.setCancelled(true);
                        e.setUseInteractedBlock(Event.Result.DENY);
                        e.setUseItemInHand(Event.Result.DENY);
                        if(e.getClickedBlock().getType() != Material.CHEST && e.getClickedBlock().getType() != Material.ENDER_CHEST) return;
                    }

                    if(e.getClickedBlock().getType() == Material.CHEST){
                        LootChest c = LootChest.getChest(e.getClickedBlock().getLocation());
                        if(c != null){
                            if(c.isSpawned()){
                                if(!c.isClaimed()){
                                    c.claim(p);
                                    c.getLocation().getWorld().playSound(c.getLocation(), Sound.CHEST_OPEN, 1f, 1f);
                                    Inventory inv = Bukkit.createInventory(null,Util.INVENTORY_3ROWS,"[" + c.getTier().getDisplay() + "] Loot Chest");

                                    ArrayList<CustomItem> toAdd = new ArrayList<CustomItem>();
                                    int added = 0;
                                    int nuggets = Util.randomInteger(c.getTier().getGoldNuggetAmount()/2,c.getTier().getGoldNuggetAmount());
                                    if(nuggets == 0) nuggets = 1;

                                    while(added < nuggets){
                                        int missing = nuggets-added;
                                        int a = missing == 1 ? 1 : Util.randomInteger(1,missing);

                                        added += a;
                                        toAdd.add(new CustomItem(7,added));
                                    }

                                    int items = c.getTier().getItemAmount() > 0 ? Util.randomInteger(c.getTier().getItemAmount()/2,c.getTier().getItemAmount()) : 0;
                                    if(items > 0){
                                        int[] usableLvls = new int[]{c.getLevel()-2, c.getLevel()-1, c.getLevel(), c.getLevel()+1, c.getLevel()+2};

                                        firstloop:
                                        for(int i = 0; i < items; i++){
                                            ArrayList<ItemData> allItems = new ArrayList<ItemData>();
                                            allItems.addAll(ItemData.STORAGE);
                                            Collections.shuffle(allItems);

                                            for(ItemData data : allItems){
                                                if(data.getCategory() == ItemCategory.ARMOR || data.getCategory() == ItemCategory.WEAPON_BOW || data.getCategory() == ItemCategory.WEAPON_AXE || data.getCategory() == ItemCategory.WEAPON_STICK) {
                                                    if (data.getRarity() != ItemRarity.NONE && data.getRarity() != ItemRarity.SPECIAL) {
                                                        a:
                                                        for (int ii : usableLvls) {
                                                            if(ii == data.getNeededLevel()) {
                                                                toAdd.add(new CustomItem(data.getId()));

                                                                continue firstloop;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    for(CustomItem i : toAdd){
                                        int slot = -1;
                                        while(slot == -1 || inv.getItem(slot) != null) slot = Util.randomInteger(0,inv.getSize()-1);

                                        inv.setItem(slot,i.build(p));
                                    }

                                    p.openInventory(inv);
                                } else {
                                    p.sendMessage(ChatColor.RED + "This loot chest has already been claimed by another player.");
                                }
                            }
                        }

                        return;
                    } else if(e.getClickedBlock().getType() == Material.ENDER_CHEST){
                        if(u.getCurrentCharacter().getBank() != null) p.openInventory(u.getCurrentCharacter().getBank());
                        return;
                    }
                }

                if(p.getItemInHand() != null && p.getItemInHand().getType() != null && p.getItemInHand().getItemMeta() != null && CustomItem.fromItemStack(p.getItemInHand()) != null) {
                    final CustomItem customItem = CustomItem.fromItemStack(p.getItemInHand());
                    long itemCooldown = customItem.getCooldownInTicks(); // TODO: Add attack speed

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

                                                //if(entity != null) target.damage(5,p);
                                                //if(entity != null) entity.damage(DamageHandler.calculatePlayerToMobDamage(u,entity,null),p);
                                                if(entity != null) DungeonRPG.callPlayerToMobDamage(p,entity,true);
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
                        } else if(customItem.getData().getCategory() == ItemCategory.WEAPON_AXE || customItem.getData().getCategory() == ItemCategory.WEAPON_SHEARS){
                            e.setCancelled(true);
                            e.setUseInteractedBlock(Event.Result.DENY);
                            e.setUseItemInHand(Event.Result.DENY);

                            if(u.isInAttackCooldown()) return;

                            u.setAttackCooldown(true);
                            new BukkitRunnable(){
                                @Override
                                public void run() {
                                    u.setAttackCooldown(false);
                                }
                            }.runTaskLater(DungeonRPG.getInstance(),customItem.getCooldownInTicks());

                            if (u.currentCombo.equals("")) {
                                for (LivingEntity target : DungeonRPG.getTargets(p, 4, 2.0)) {
                                    CustomEntity entity = CustomEntity.fromEntity(target);

                                    //if(entity != null) target.damage(5,p);
                                    if(entity != null) DungeonRPG.callPlayerToMobDamage(p,entity,false);
                                }
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
                            if(!DungeonRPG.ENABLE_BOWDRAWBACK){
                                if(u.getCurrentCharacter().getRpgClass() == RPGClass.ARCHER){
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
                } else {
                    if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
                        if(!u.isInAttackCooldown()){
                            long itemCooldown = 15;

                            for (LivingEntity target : DungeonRPG.getTargets(p, 4, 2.0)) {
                                CustomEntity entity = CustomEntity.fromEntity(target);

                                if(entity != null){
                                    DungeonRPG.callPlayerToMobDamage(p,entity,false);
                                    break; // Only attack first entity when not using a weapon
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
