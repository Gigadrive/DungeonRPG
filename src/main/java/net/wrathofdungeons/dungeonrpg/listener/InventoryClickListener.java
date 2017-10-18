package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.Trade;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class InventoryClickListener implements Listener {
    @EventHandler
    public void onClick(InventoryClickEvent e){
        Inventory inv = e.getInventory();

        boolean deny = false;

        switch(e.getAction()){
            case HOTBAR_SWAP:
                deny = true;
                break;
        }

        if(deny){
            e.setCancelled(true);
            return;
        }

        if(e.getWhoClicked() instanceof Player){
            Player p = (Player)e.getWhoClicked();

            if(GameUser.isLoaded(p)){
                GameUser u = GameUser.getUser(p);

                if(u.getCurrentCharacter() != null){
                    u.checkRequirements();

                    if(e.getCurrentItem() != null){
                        CustomItem item = CustomItem.fromItemStack(e.getCurrentItem());

                        if(item != null){
                            if(item.getData().getId() == 5 || ((DungeonRPG.ENABLE_BOWDRAWBACK) && item.getData().getId() == 6)){
                                e.setCancelled(true);
                                return;
                            }
                        }
                    }

                    if(inv.getName().equals("Trading")){
                        e.setCancelled(true);
                        Trade t = Trade.getTrade(p);

                        if(t != null){
                            if((e.getRawSlot() == 2 && t.isPlayer1(p)) || (e.getRawSlot() == 6 && t.isPlayer2(p))){
                                // wool

                                if((t.isPlayer1(p) && t.isPlayer1Ready()) || (t.isPlayer2(p) && t.isPlayer2Ready())){
                                    // cancel trade

                                    if(t.isPlayer1(p)) t.setPlayer1Ready(false);
                                    if(t.isPlayer2(p)) t.setPlayer2Ready(false);

                                    t.updateInventories();
                                } else if((t.isPlayer1(p) && !t.isPlayer1Ready()) || (t.isPlayer2(p) && !t.isPlayer2Ready())){
                                    // accept trade

                                    if(t.isPlayer1(p)) t.setPlayer1Ready(true);
                                    if(t.isPlayer2(p)) t.setPlayer2Ready(true);

                                    t.getPlayer1().playSound(t.getPlayer1().getEyeLocation(), Sound.ORB_PICKUP,1f,1f);
                                    t.getPlayer2().playSound(t.getPlayer2().getEyeLocation(), Sound.ORB_PICKUP,1f,1f);

                                    if(t.isPlayer1Ready() && t.isPlayer2Ready()){
                                        // finish trade

                                        if((GameUser.getUser(t.getPlayer1()).getEmptySlotsInInventory() >= t.getOffers2().size()) && (GameUser.getUser(t.getPlayer2()).getEmptySlotsInInventory() >= t.getOffers1().size())){
                                            for(ItemStack i : t.getOffers2()){
                                                CustomItem item = CustomItem.fromItemStack(i);
                                                t.getPlayer1().getInventory().addItem(item.build(t.getPlayer1()));
                                            }

                                            for(ItemStack i : t.getOffers1()){
                                                CustomItem item = CustomItem.fromItemStack(i);
                                                t.getPlayer2().getInventory().addItem(item.build(t.getPlayer2()));
                                            }

                                            t.setStatus(Trade.Status.CANCELLED);

                                            t.getPlayer1().closeInventory();
                                            t.getPlayer2().closeInventory();

                                            t.getPlayer1().sendMessage(ChatColor.GREEN + "The trade has been finished.");
                                            t.getPlayer2().sendMessage(ChatColor.GREEN + "The trade has been finished.");

                                            t.unregister();
                                        } else {
                                            // Cancel trade if one player does not have enough inventory space
                                            t.cancelTrade();
                                        }
                                    } else {
                                        t.updateInventories();
                                    }
                                }
                            } else if(((t.isPlayer1(p)) && ((e.getRawSlot() >= 9 && e.getRawSlot() <= 12) || (e.getRawSlot() >= 18 && e.getRawSlot() <= 21) || (e.getRawSlot() >= 27 && e.getRawSlot() <= 30) || (e.getRawSlot() >= 36 && e.getRawSlot() <= 39))) || ((t.isPlayer2(p)) && ((e.getRawSlot() >= 14 && e.getRawSlot() <= 17) || (e.getRawSlot() >= 23 && e.getRawSlot() <= 26) || (e.getRawSlot() >= 32 && e.getRawSlot() <= 35) || (e.getRawSlot() >= 41 && e.getRawSlot() <= 44)))){
                                // trading area
                                CustomItem item = CustomItem.fromItemStack(e.getCurrentItem());

                                if(item != null){
                                    p.getInventory().addItem(item.build(p));

                                    if(t.isPlayer1(p)){
                                        t.getOffers1().remove(Trade.getPlayer1SlotsAsList().indexOf(e.getRawSlot()));
                                        //t.getOffers1().remove(item.build(p));
                                        t.setPlayer1Ready(false);
                                        t.setPlayer2Ready(false);
                                    } else if(t.isPlayer2(p)){
                                        t.getOffers2().remove(Trade.getPlayer2SlotsAsList().indexOf(e.getRawSlot()));
                                        //t.getOffers2().remove(item.build(p));
                                        t.setPlayer1Ready(false);
                                        t.setPlayer2Ready(false);
                                    }

                                    t.updateInventories();
                                }
                            } else if(e.getRawSlot() >= 0+inv.getSize() && e.getRawSlot() <= 44+inv.getSize()){
                                // player inventory

                                CustomItem item = CustomItem.fromItemStack(e.getCurrentItem());
                                if(e.getClick() == ClickType.RIGHT) item = new CustomItem(CustomItem.fromItemStack(e.getCurrentItem()).getData(),1);

                                if(item != null){
                                    int currentOffers = t.isPlayer1(p) ? t.getOffers1().size() : t.getOffers2().size();

                                    ItemStack currentItem = null;

                                    if(t.isPlayer1(p)){
                                        for(ItemStack iStack : t.getOffers1()){
                                            CustomItem it = CustomItem.fromItemStack(iStack);

                                            if(it.isSameItem(item) && it.getAmount()+item.getAmount() < 64){
                                                currentItem = iStack;
                                            }
                                        }
                                    } else if(t.isPlayer2(p)){
                                        for(ItemStack iStack : t.getOffers2()){
                                            CustomItem it = CustomItem.fromItemStack(iStack);

                                            if(it.isSameItem(item) && it.getAmount()+item.getAmount() < 64){
                                                currentItem = iStack;
                                            }
                                        }
                                    }

                                    if(currentItem != null){
                                        if(t.isPlayer1(p)){
                                            t.getOffers1().remove(currentItem);
                                            currentItem.setAmount(currentItem.getAmount()+item.getAmount());
                                            t.getOffers1().add(currentItem);
                                            t.setPlayer1Ready(false);
                                            t.setPlayer2Ready(false);
                                        } else if(t.isPlayer2(p)){
                                            t.getOffers2().remove(currentItem);
                                            currentItem.setAmount(currentItem.getAmount()+item.getAmount());
                                            t.getOffers2().add(currentItem);
                                            t.setPlayer1Ready(false);
                                            t.setPlayer2Ready(false);
                                        }

                                        if(p.getInventory().getItem(e.getSlot()) != null && p.getInventory().getItem(e.getSlot()).getAmount() < item.getAmount()){
                                            if(p.getInventory().getItem(e.getSlot()).getAmount()-item.getAmount() <= 1){
                                                p.getInventory().setItem(e.getSlot(),new ItemStack(Material.AIR));
                                            } else {
                                                p.getInventory().getItem(e.getSlot()).setAmount(p.getInventory().getItem(e.getSlot()).getAmount()-item.getAmount());
                                            }
                                        }

                                        t.updateInventories();
                                    } else {
                                        if(currentOffers < Trade.MAX_OFFERS){
                                            if(!item.isUntradeable()){
                                                if(t.isPlayer1(p)){
                                                    p.getInventory().setItem(e.getSlot(),new ItemStack(Material.AIR));
                                                    t.getOffers1().add(item.build(p));
                                                    t.setPlayer1Ready(false);
                                                    t.setPlayer2Ready(false);
                                                } else if(t.isPlayer2(p)){
                                                    p.getInventory().setItem(e.getSlot(),new ItemStack(Material.AIR));
                                                    t.getOffers2().add(item.build(p));
                                                    t.setPlayer1Ready(false);
                                                    t.setPlayer2Ready(false);
                                                }

                                                t.updateInventories();
                                            } else {
                                                p.sendMessage(ChatColor.RED + "That item is untradeable.");
                                            }
                                        } else {
                                            p.sendMessage(ChatColor.RED + "You can only trade " + Trade.MAX_OFFERS + " items at once.");
                                        }
                                    }
                                }
                            } else {
                                // anything else (out of container?)
                            }
                        }
                    }
                }
            }
        }
    }
}
