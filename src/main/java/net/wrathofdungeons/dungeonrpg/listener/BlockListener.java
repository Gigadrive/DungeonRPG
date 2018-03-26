package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.MySQLManager;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.items.ItemData;
import net.wrathofdungeons.dungeonrpg.professions.Ore;
import net.wrathofdungeons.dungeonrpg.professions.Profession;
import net.wrathofdungeons.dungeonrpg.regions.Region;
import net.wrathofdungeons.dungeonrpg.regions.StoredLocation;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.util.WorldUtilities;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.util.Vector;

import java.sql.PreparedStatement;

public class BlockListener implements Listener {
    @EventHandler
    public void onPlace(BlockPlaceEvent e){
        if(e.getPlayer().getGameMode() != GameMode.CREATIVE) e.setCancelled(true);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e){
        Player p = e.getPlayer();

        if(GameUser.isLoaded(p)){
            GameUser u = GameUser.getUser(p);

            if(u.isInSetupMode()){
                if(DungeonRPG.SETUP_REGION > 0){
                    Region region = Region.getRegion(DungeonRPG.SETUP_REGION);

                    if(region != null){
                        StoredLocation toRemove = null;

                        for (StoredLocation l : region.getLocations()) {
                            if(Util.isLocationEqual(l.toBukkitLocation(),e.getBlock().getLocation())) toRemove = l;
                        }

                        if(toRemove != null){
                            p.sendMessage(ChatColor.GREEN + "Location removed.");
                            region.getLocations().remove(toRemove);
                        } else {
                            e.setCancelled(true);
                        }
                    } else {
                        e.setCancelled(true);
                    }
                } else {
                    Ore ore = Ore.getOre(e.getBlock().getLocation());

                    if(ore != null){
                        e.setCancelled(true);

                        DungeonAPI.async(() -> {
                            try {
                                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("DELETE FROM `ores` WHERE `id` = ?");
                                ps.setInt(1,ore.getId());
                                ps.executeUpdate();
                                ps.close();

                                ore.getLocation().getBlock().setType(ore.getLevel().getBlock());
                                Ore.STORAGE.remove(ore.getId());
                                p.sendMessage(ChatColor.YELLOW + "Ore deleted.");
                            } catch(Exception e1){
                                e1.printStackTrace();
                                p.sendMessage(ChatColor.RED + "An error occurred.");
                            }
                        });
                    } else {
                        e.setCancelled(true);
                    }
                }
            } else {
                CustomItem item = CustomItem.fromItemStack(p.getItemInHand());

                if(item != null){
                    Ore ore = Ore.getOre(e.getBlock().getLocation());
                    e.setCancelled(true);

                    if(ore != null){
                        if(ore.isAvailable()){
                            if(item.getModifiedPickaxeStrength() >= ore.getLevel().getRequiredPickaxeStrength()){
                                e.getBlock().setType(Material.COBBLESTONE);
                                ore.setAvailable(false);
                                ore.timer += Util.randomInteger(12,24)*60;

                                int currentValue = 0;
                                if(u.getCurrentCharacter().getVariables().statisticsManager.oresMined.containsKey(ore.getLevel())){
                                    currentValue = u.getCurrentCharacter().getVariables().statisticsManager.oresMined.get(ore.getLevel());
                                    u.getCurrentCharacter().getVariables().statisticsManager.oresMined.remove(ore.getLevel());
                                }

                                u.getCurrentCharacter().getVariables().statisticsManager.oresMined.put(ore.getLevel(),currentValue+1);

                                CustomItem drop = null;
                                int tries = 0;

                                while(drop == null && tries < 40){
                                    for(String sd : ore.getLevel().getDrops()){
                                        if(sd == null || sd.isEmpty()) continue;

                                        String[] s = sd.split(":");

                                        if(s.length == 3){
                                            if(Util.isValidInteger(s[0]) && Util.isValidInteger(s[1]) && Util.isValidInteger(s[2])){
                                                int itemID = Integer.parseInt(s[0]);
                                                int chance = Integer.parseInt(s[1]);
                                                int exp = Integer.parseInt(s[2]);

                                                if(ItemData.getData(itemID) != null && chance > -1 && exp > -1){
                                                    if(Util.getChanceBoolean(1,chance)){
                                                        drop = new CustomItem(itemID);
                                                        u.getCurrentCharacter().getVariables().getProfessionProgress(Profession.MINING).giveExp(p,exp);

                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    tries++;
                                }

                                if(drop != null){
                                    //p.sendMessage(ChatColor.GREEN + "That ore contained: " + ChatColor.YELLOW + drop.getAmount() + "x " + ChatColor.stripColor(drop.getData().getName()));

                                    if(u.getEmptySlotsInInventory() > 0){
                                        p.sendMessage(ChatColor.GREEN + "That ore contained: " + ChatColor.YELLOW + drop.getAmount() + "x " + ChatColor.stripColor(drop.getData().getName()));
                                        p.getInventory().addItem(drop.build(p));
                                        u.playItemPickupSound();
                                    } else {
                                        WorldUtilities.dropItem(e.getBlock().getLocation(),drop,p).setVelocity(new Vector(0,.25,0));
                                        p.sendMessage(ChatColor.GRAY + "Since your inventory is full, the item has been dropped on the ground.");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "That ore didn't contain a drop.");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "Your pickaxe isn't strong enough to break this block.");
                            }
                        }
                    }
                } else {
                    e.setCancelled(true);
                }
            }
        } else {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onSpread(BlockSpreadEvent e){
        e.setCancelled(true);
    }
}
