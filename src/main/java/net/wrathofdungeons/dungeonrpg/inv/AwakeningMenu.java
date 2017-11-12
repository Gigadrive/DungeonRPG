package net.wrathofdungeons.dungeonrpg.inv;

import net.wrathofdungeons.dungeonapi.util.ItemUtil;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.items.awakening.Awakening;
import net.wrathofdungeons.dungeonrpg.items.awakening.AwakeningCategory;
import net.wrathofdungeons.dungeonrpg.items.awakening.AwakeningType;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.util.WorldUtilities;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class AwakeningMenu implements Listener {
    public static void openFor(Player p){
        Inventory inv = Bukkit.createInventory(null,9,"Awakening Specialist");

        inv.setItem(8, ItemUtil.namedItem(Material.STAINED_CLAY, ChatColor.GREEN + "Awaken",null,5));

        p.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        if(e.getWhoClicked() instanceof Player){
            Player p = (Player)e.getWhoClicked();
            Inventory inv = e.getInventory();

            if(GameUser.isLoaded(p)){
                GameUser u = GameUser.getUser(p);

                if(u.getCurrentCharacter() != null){
                    if(inv.getName().equals("Awakening Specialist")){
                        e.setCancelled(true);

                        if(e.getCurrentItem() != null && e.getCurrentItem().getType() != null && e.getCurrentItem().getItemMeta() != null && e.getCurrentItem().getItemMeta().getDisplayName() != null){
                            String dis = e.getCurrentItem().getItemMeta().getDisplayName();

                            if(dis.equals(ChatColor.GREEN + "Awaken")){
                                ArrayList<CustomItem> toAwaken = new ArrayList<CustomItem>();

                                int[] i = new int[]{0,1,2,3,4,5,6,7};

                                for(int ii : i){
                                    if(inv.getItem(ii) != null){
                                        if(CustomItem.fromItemStack(inv.getItem(ii)) != null){
                                            toAwaken.add(CustomItem.fromItemStack(inv.getItem(ii)));
                                        }
                                    }
                                }

                                if(toAwaken.size() > 0){
                                    int awakeningPrice = 0;
                                    for(CustomItem item : toAwaken){
                                        awakeningPrice += item.getAwakeningPrice();
                                    }

                                    if(u.getTotalMoneyInInventory() >= awakeningPrice){
                                        if(u.removeMoneyFromInventory(awakeningPrice,toAwaken.size())){
                                            for(CustomItem item : toAwaken){
                                                ArrayList<AwakeningType> types = new ArrayList<AwakeningType>();
                                                for(AwakeningType t : AwakeningType.getAwakenings(AwakeningCategory.WEAPON_ARMOR)){
                                                    if(!item.hasAwakening(t)) types.add(t);
                                                }
                                                Collections.shuffle(types);

                                                if(types.size() > 0){
                                                    AwakeningType type = types.get(0);
                                                    int value = Util.randomInteger(type.getMinimum(),type.getMaximum());

                                                    boolean isPercentage;

                                                    if(!type.mayBePercentage() && type.mayBeStatic()){
                                                        isPercentage = false;
                                                    } else if(type.mayBePercentage() && !type.mayBeStatic()){
                                                        isPercentage = true;
                                                    } else {
                                                        isPercentage = Util.convertIntegerToBoolean(Util.randomInteger(0,1));
                                                    }

                                                    Awakening a = new Awakening(type,value,isPercentage);
                                                    item.addAwakening(a);
                                                }
                                            }

                                            boolean dropped = false;

                                            for(CustomItem c : toAwaken){
                                                if(u.getEmptySlotsInInventory() > 0){
                                                    p.getInventory().addItem(c.build(p));
                                                } else {
                                                    dropped = true;
                                                    WorldUtilities.dropItem(p.getLocation(),c,p);
                                                }
                                            }

                                            if(dropped){
                                                p.sendMessage(ChatColor.GRAY + "Some items were dropped to the ground, because your inventory is full.");
                                            }

                                            inv.clear();
                                            p.closeInventory();
                                            p.playSound(p.getEyeLocation(), Sound.ORB_PICKUP, 1F, 1F);

                                            u.getCurrentCharacter().getVariables().statisticsManager.itemsAwakened += toAwaken.size();

                                            if(toAwaken.size() == 1){
                                                if(awakeningPrice == 1){
                                                    p.sendMessage(ChatColor.GREEN + "You awakened " + ChatColor.YELLOW + toAwaken.size() + " " + ChatColor.GREEN + "item for " + ChatColor.YELLOW + awakeningPrice + ChatColor.GREEN + " golden nugget.");
                                                } else {
                                                    p.sendMessage(ChatColor.GREEN + "You awakened " + ChatColor.YELLOW + toAwaken.size() + " " + ChatColor.GREEN + "item for " + ChatColor.YELLOW + awakeningPrice + ChatColor.GREEN + " golden nuggets.");
                                                }
                                            } else {
                                                if(awakeningPrice == 1){
                                                    p.sendMessage(ChatColor.GREEN + "You awakened " + ChatColor.YELLOW + toAwaken.size() + " " + ChatColor.GREEN + "items for " + ChatColor.YELLOW + awakeningPrice + ChatColor.GREEN + " golden nugget.");
                                                } else {
                                                    p.sendMessage(ChatColor.GREEN + "You awakened " + ChatColor.YELLOW + toAwaken.size() + " " + ChatColor.GREEN + "items for " + ChatColor.YELLOW + awakeningPrice + ChatColor.GREEN + " golden nuggets.");
                                                }
                                            }
                                        } else {
                                            p.closeInventory();
                                            p.sendMessage(ChatColor.RED + "Awakening failed! Please make sure you have enough space in your inventory.");
                                        }
                                    } else {
                                        p.closeInventory();
                                        p.sendMessage(ChatColor.RED + "You don't have enough money to awaken these items.");
                                    }
                                }
                            } else {
                                if(CustomItem.fromItemStack(e.getCurrentItem()) != null){
                                    CustomItem item = CustomItem.fromItemStack(e.getCurrentItem());

                                    if(e.getRawSlot() == 0 || e.getRawSlot() == 1 || e.getRawSlot() == 2 || e.getRawSlot() == 3 || e.getRawSlot() == 4 || e.getRawSlot() == 5 || e.getRawSlot() == 6 || e.getRawSlot() == 7){
                                        p.getInventory().addItem(item.build(p));
                                        inv.setItem(e.getSlot(), new ItemStack(Material.AIR));
                                    } else {
                                        if(item.getAwakeningPrice() != 0){
                                            if(inv.getItem(0) == null || inv.getItem(1) == null || inv.getItem(2) == null || inv.getItem(3) == null || inv.getItem(4) == null || inv.getItem(5) == null || inv.getItem(6) == null || inv.getItem(7) == null){
                                                ItemStack i = item.build(p);
                                                ItemMeta iM = i.getItemMeta();
                                                ArrayList<String> iL = new ArrayList<String>();
                                                if(iM.getLore() != null) iL.addAll(iM.getLore());
                                                iL.add(ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH.toString() + Util.SCOREBOARD_LINE_SEPERATOR);
                                                iL.add(ChatColor.WHITE + "Awakening Price" + ": " + ChatColor.GRAY + item.getAwakeningPrice());
                                                iM.setLore(iL);
                                                i.setItemMeta(iM);

                                                inv.addItem(i);
                                                p.getInventory().setItem(e.getSlot(), new ItemStack(Material.AIR));
                                            } else {
                                                p.sendMessage(ChatColor.RED + "You can only awaken 8 items at once.");
                                            }
                                        } else {
                                            p.sendMessage(ChatColor.RED + "That item can't be awakened.");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
