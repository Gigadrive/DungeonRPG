package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.inv.MerchantSetupMenu;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.items.ItemData;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.npc.MerchantOffer;
import net.wrathofdungeons.dungeonrpg.npc.MerchantOfferCost;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

import java.util.ArrayList;

public class ChatListener implements Listener {
    @EventHandler
    public void onChat(PlayerChatEvent e){
        Player p = e.getPlayer();
        String msg = e.getMessage();

        if(GameUser.isLoaded(p)){
            GameUser u = GameUser.getUser(p);

            if(u.getCurrentCharacter() != null){
                e.setCancelled(true);

                p.sendMessage(ChatColor.GRAY + "[" + u.getCurrentCharacter().getRpgClass().getName().substring(0,2) + u.getCurrentCharacter().getLevel() + "] " + u.getRank().getColor() + p.getName() + ": " + ChatColor.WHITE + msg);

                for(Entity entity : p.getNearbyEntities(60,60,60)){
                    if(entity instanceof Player){
                        if(CustomEntity.fromEntity((LivingEntity)entity) == null){
                            Player p2 = (Player)entity;

                            if(GameUser.isLoaded(p2)){
                                GameUser u2 = GameUser.getUser(p2);

                                if(u2.getCurrentCharacter() != null){
                                    double distance = p.getLocation().distance(p2.getLocation());

                                    ChatColor color = null;

                                    if(distance < 35){
                                        color = ChatColor.WHITE;
                                    } else if(distance > 35 && distance < 65){
                                        color = ChatColor.GRAY;
                                    } else if(distance > 65 && distance < 90){
                                        color = ChatColor.DARK_GRAY;
                                    }

                                    if(color != null){
                                        p2.sendMessage(ChatColor.GRAY + "[" + u.getCurrentCharacter().getRpgClass().getName().substring(0,2) + u.getCurrentCharacter().getLevel() + "] " + u.getRank().getColor() + p.getName() + ": " + color + msg);
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                e.setCancelled(true);

                if(u.isInSetupMode()){
                    if(u.merchantAddItem != null){
                        if(u.merchantAddItemHandle == null){
                            msg = msg.trim();
                            String[] s = msg.split(":");

                            if(!msg.equalsIgnoreCase("cancel")){
                                if(s.length == 1 || s.length == 2){
                                    if(Util.isValidInteger(s[0])){
                                        int itemID = Integer.parseInt(s[0]);

                                        if(ItemData.getData(itemID) != null){
                                            int amount = 1;

                                            if(s.length == 2){
                                                if(Util.isValidInteger(s[1])){
                                                    amount = Integer.parseInt(s[1]);
                                                } else {
                                                    p.sendMessage(ChatColor.RED + "Please enter a valid integer.");
                                                    return;
                                                }
                                            }

                                            u.merchantAddItemHandle = new CustomItem(itemID,amount);
                                            p.sendMessage(ChatColor.GOLD + "Enter the amount of gold nuggets players should be charged for this item.");
                                            p.sendMessage(ChatColor.GRAY + "(Type 0 to skip.)");
                                            p.sendMessage(ChatColor.GRAY + "(Type 'cancel' to cancel.)");
                                        } else {
                                            p.sendMessage(ChatColor.RED + "That item doesn't exist.");
                                        }
                                    } else {
                                        p.sendMessage(ChatColor.RED + "Please enter a valid integer.");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "Invalid format. Use ID[:AMOUNT]!");
                                }
                            } else {
                                u.merchantAddItem = null;
                                u.merchantAddItemHandle = null;
                                u.merchantAddItemCosts = null;
                                u.merchantAddMoneyCost = -1;
                                u.merchantAddItemSlot = -1;
                                p.sendMessage(ChatColor.RED + "Operation aborted.");
                            }
                        } else if(u.merchantAddMoneyCost == -1){
                            msg = msg.trim();

                            if(Util.isValidInteger(msg)){
                                int money = Integer.parseInt(msg);
                                if(money < 0) money = 0;

                                if(money == 0){
                                    p.sendMessage(ChatColor.GRAY + "Skipped money cost..");
                                } else {
                                    p.sendMessage(ChatColor.GREEN + "Money cost set to: " + money + " gold nugget(s).");
                                }

                                p.sendMessage(ChatColor.GOLD + "Enter item costs that should be charged additionally to the gold nugget cost.");
                                p.sendMessage(ChatColor.GOLD + "Use ID[:AMOUNT] as a format template.");
                                p.sendMessage(ChatColor.GRAY + "(Type 'skip' to skip.)");
                                p.sendMessage(ChatColor.GRAY + "(Type 'cancel' to cancel.)");

                                u.merchantAddMoneyCost = money;
                            } else {
                                if(msg.equalsIgnoreCase("cancel")){
                                    u.merchantAddItem = null;
                                    u.merchantAddItemHandle = null;
                                    u.merchantAddItemCosts = null;
                                    u.merchantAddMoneyCost = -1;
                                    u.merchantAddItemSlot = -1;
                                    p.sendMessage(ChatColor.RED + "Operation aborted.");
                                } else {
                                    p.sendMessage(ChatColor.RED + "Please enter a valid integer.");
                                }
                            }
                        } else {
                            msg = msg.trim();
                            String[] s = msg.split(":");
                            if(u.merchantAddItemCosts == null) u.merchantAddItemCosts = new ArrayList<CustomItem>();
                            int costAmount = u.merchantAddItemCosts.size();

                            if(msg.equalsIgnoreCase("cancel")){
                                u.merchantAddItem = null;
                                u.merchantAddItemHandle = null;
                                u.merchantAddItemCosts = null;
                                u.merchantAddMoneyCost = -1;
                                u.merchantAddItemSlot = -1;
                                p.sendMessage(ChatColor.RED + "Operation aborted.");
                            } else if(msg.equalsIgnoreCase("skip")){
                                if(costAmount > 0 || u.merchantAddMoneyCost > 0){
                                    MerchantOffer offer = new MerchantOffer();
                                    offer.moneyCost = u.merchantAddMoneyCost;
                                    offer.itemToBuy = u.merchantAddItemHandle.getData().getId();
                                    offer.amount = u.merchantAddItemHandle.getAmount();
                                    offer.slot = u.merchantAddItemSlot;
                                    offer.itemCost = new ArrayList<MerchantOfferCost>();

                                    for(CustomItem i : u.merchantAddItemCosts){
                                        MerchantOfferCost c = new MerchantOfferCost();
                                        c.item = i.getData().getId();
                                        c.amount = i.getAmount();
                                        offer.itemCost.add(c);
                                    }

                                    u.merchantAddItem.getOffers().add(offer);
                                    u.merchantAddItem.setHasUnsavedData(true);

                                    p.sendMessage(ChatColor.GREEN + "Offer added!");
                                    MerchantSetupMenu.open(p,u.merchantAddItem);

                                    u.merchantAddItem = null;
                                    u.merchantAddItemHandle = null;
                                    u.merchantAddItemCosts = null;
                                    u.merchantAddMoneyCost = -1;
                                    u.merchantAddItemSlot = -1;
                                } else {
                                    p.sendMessage(ChatColor.RED + "You have to add at least one cost!");
                                }
                            } else {
                                if(s.length == 1 || s.length == 2){
                                    if(Util.isValidInteger(s[0])){
                                        int itemID = Integer.parseInt(s[0]);

                                        if(ItemData.getData(itemID) != null){
                                            int amount = 1;

                                            if(s.length == 2){
                                                if(Util.isValidInteger(s[1])){
                                                    amount = Integer.parseInt(s[1]);
                                                } else {
                                                    p.sendMessage(ChatColor.RED + "Please enter a valid integer.");
                                                    return;
                                                }
                                            }

                                            u.merchantAddItemCosts.add(new CustomItem(itemID,amount));
                                            costAmount++;

                                            if(costAmount < 5){
                                                p.sendMessage(ChatColor.GOLD + "Items left to be added: " + String.valueOf(5-costAmount));
                                                p.sendMessage(ChatColor.GOLD + "Use ID[:AMOUNT] as a format template.");
                                                p.sendMessage(ChatColor.GRAY + "(Type 'skip' to skip.)");
                                                p.sendMessage(ChatColor.GRAY + "(Type 'cancel' to cancel.)");
                                            } else {
                                                MerchantOffer offer = new MerchantOffer();
                                                offer.moneyCost = u.merchantAddMoneyCost;
                                                offer.itemToBuy = u.merchantAddItemHandle.getData().getId();
                                                offer.amount = u.merchantAddItemHandle.getAmount();
                                                offer.slot = u.merchantAddItemSlot;
                                                offer.itemCost = new ArrayList<MerchantOfferCost>();

                                                for(CustomItem i : u.merchantAddItemCosts){
                                                    MerchantOfferCost c = new MerchantOfferCost();
                                                    c.item = i.getData().getId();
                                                    c.amount = i.getAmount();
                                                    offer.itemCost.add(c);
                                                }

                                                u.merchantAddItem.getOffers().add(offer);
                                                u.merchantAddItem.setHasUnsavedData(true);

                                                p.sendMessage(ChatColor.GREEN + "Offer added!");
                                                MerchantSetupMenu.open(p,u.merchantAddItem);

                                                u.merchantAddItem = null;
                                                u.merchantAddItemHandle = null;
                                                u.merchantAddItemCosts = null;
                                                u.merchantAddMoneyCost = -1;
                                                u.merchantAddItemSlot = -1;
                                            }
                                        } else {
                                            p.sendMessage(ChatColor.RED + "That item doesn't exist.");
                                        }
                                    } else {
                                        p.sendMessage(ChatColor.RED + "Please enter a valid integer.");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "Invalid format. Use ID[:AMOUNT]!");
                                }
                            }
                        }
                    }
                }
            }
        } else {
            e.setCancelled(true);
        }
    }
}
