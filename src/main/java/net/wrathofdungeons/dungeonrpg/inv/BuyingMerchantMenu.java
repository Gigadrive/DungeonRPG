package net.wrathofdungeons.dungeonrpg.inv;

import net.wrathofdungeons.dungeonapi.util.ItemUtil;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.util.WorldUtilities;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class BuyingMerchantMenu implements Listener {
    public static void openFor(Player p){
        Inventory inv = Bukkit.createInventory(null,9,"Buying Merchant");

        inv.setItem(8, ItemUtil.namedItem(Material.STAINED_CLAY, ChatColor.GREEN + "Sell",null,5));

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
                    if(inv.getName().equals("Buying Merchant")){
                        e.setCancelled(true);

                        if(e.getCurrentItem() != null && e.getCurrentItem().getType() != null && e.getCurrentItem().getItemMeta() != null && e.getCurrentItem().getItemMeta().getDisplayName() != null){
                            String dis = e.getCurrentItem().getItemMeta().getDisplayName();

                            if(dis.equals(ChatColor.GREEN + "Sell")){
                                ArrayList<CustomItem> toSell = new ArrayList<CustomItem>();

                                int[] i = new int[]{0,1,2,3,4,5,6,7};

                                for(int ii : i){
                                    if(inv.getItem(ii) != null){
                                        if(CustomItem.fromItemStack(inv.getItem(ii)) != null){
                                            toSell.add(CustomItem.fromItemStack(inv.getItem(ii)));
                                        }
                                    }
                                }

                                int sellprice = 0;
                                for(CustomItem item : toSell){
                                    sellprice += item.getSellprice();
                                }

                                CustomItem[] money = WorldUtilities.convertNuggetAmount(sellprice);
                                if(u.getEmptySlotsInInventory() >= money.length){
                                    for(CustomItem item : money){
                                        p.getInventory().addItem(item.build(p));
                                    }

                                    inv.clear();
                                    p.closeInventory();
                                    p.playSound(p.getEyeLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);
                                    if(toSell.size() == 1){
                                        if(sellprice == 1){
                                            p.sendMessage(ChatColor.GREEN + "You sold " + ChatColor.YELLOW + toSell.size() + " " + ChatColor.GREEN + "item for " + ChatColor.YELLOW + sellprice + ChatColor.GREEN + " golden nugget.");
                                        } else {
                                            p.sendMessage(ChatColor.GREEN + "You sold " + ChatColor.YELLOW + toSell.size() + " " + ChatColor.GREEN + "item for " + ChatColor.YELLOW + sellprice + ChatColor.GREEN + " golden nuggets.");
                                        }
                                    } else {
                                        if(sellprice == 1){
                                            p.sendMessage(ChatColor.GREEN + "You sold " + ChatColor.YELLOW + toSell.size() + " " + ChatColor.GREEN + "items for " + ChatColor.YELLOW + sellprice + ChatColor.GREEN + " golden nugget.");
                                        } else {
                                            p.sendMessage(ChatColor.GREEN + "You sold " + ChatColor.YELLOW + toSell.size() + " " + ChatColor.GREEN + "items for " + ChatColor.YELLOW + sellprice + ChatColor.GREEN + " golden nuggets.");
                                        }
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "Please empty space in your inventory.");
                                }
                            } else {
                                if(CustomItem.fromItemStack(e.getCurrentItem()) != null){
                                    CustomItem item = CustomItem.fromItemStack(e.getCurrentItem());

                                    if(e.getRawSlot() == 0 || e.getRawSlot() == 1 || e.getRawSlot() == 2 || e.getRawSlot() == 3 || e.getRawSlot() == 4 || e.getRawSlot() == 5 || e.getRawSlot() == 6 || e.getRawSlot() == 7){
                                        p.getInventory().addItem(item.build(p));
                                        inv.setItem(e.getSlot(), new ItemStack(Material.AIR));
                                    } else {
                                        if(item.getSellprice() != 0 && !item.isUntradeable()){
                                            if(inv.getItem(0) == null || inv.getItem(1) == null || inv.getItem(2) == null || inv.getItem(3) == null || inv.getItem(4) == null || inv.getItem(5) == null || inv.getItem(6) == null || inv.getItem(7) == null){
                                                ItemStack i = item.build(p);
                                                ItemMeta iM = i.getItemMeta();
                                                ArrayList<String> iL = new ArrayList<String>();
                                                if(iM.getLore() != null) iL.addAll(iM.getLore());
                                                iL.add(ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH.toString() + Util.SCOREBOARD_LINE_SEPERATOR);
                                                iL.add(ChatColor.WHITE + "Sell Price" + ": " + ChatColor.GRAY + item.getSellprice());
                                                iM.setLore(iL);
                                                i.setItemMeta(iM);

                                                inv.addItem(i);
                                                p.getInventory().setItem(e.getSlot(), new ItemStack(Material.AIR));
                                            } else {
                                                p.sendMessage(ChatColor.RED + "The merchant can only buy 8 items at once.");
                                            }
                                        } else {
                                            p.sendMessage(ChatColor.RED + "That item can't be sold.");
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
