package net.wrathofdungeons.dungeonrpg.inv;

import net.wrathofdungeons.dungeonapi.util.ItemUtil;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.items.StoredCustomItem;
import net.wrathofdungeons.dungeonrpg.professions.CraftingRecipe;
import net.wrathofdungeons.dungeonrpg.professions.Profession;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.util.WorldUtilities;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;

public class CraftingMenu implements Listener {
    public static final int[] CRAFTING_SLOTS = new int[]{11,12,13,14,15};
    private static final ItemStack noResult = ItemUtil.namedItem(Material.BARRIER, ChatColor.GRAY + "(No Result)", null);;

    public static void openFor(Player p){
        GameUser u = GameUser.getUser(p);
        Inventory inv = Bukkit.createInventory(null, Util.INVENTORY_5ROWS,"Crafting");

        ItemStack pl = ItemUtil.namedItem(Material.STAINED_GLASS_PANE, " ", null, 15);

        for(int i = 0; i < inv.getSize(); i++) inv.setItem(i,pl);
        for(int i : CRAFTING_SLOTS) inv.setItem(i,new ItemStack(Material.AIR));

        inv.setItem(31,noResult);

        p.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        if(e.getWhoClicked() instanceof Player){
            Player p = (Player) e.getWhoClicked();
            Inventory inv = e.getInventory();

            if(GameUser.isLoaded(p)){
                GameUser u = GameUser.getUser(p);

                if(u.getCurrentCharacter() != null){
                    if(inv.getName().equals("Crafting")){
                        e.setCancelled(true);
                        ItemStack current = e.getCurrentItem();

                        if(current != null){
                            CustomItem item = CustomItem.fromItemStack(current);

                            if(item != null){
                                boolean craftingSlot = false;
                                for(int i : CRAFTING_SLOTS) if(e.getRawSlot() == i) craftingSlot = true;

                                if(craftingSlot){
                                    p.getInventory().addItem(item.build(p));
                                    inv.setItem(e.getSlot(), new ItemStack(Material.AIR));

                                    // Update crafting result

                                    ArrayList<StoredCustomItem> a = new ArrayList<StoredCustomItem>();
                                    for(int l : CRAFTING_SLOTS) if(inv.getItem(l) != null && CustomItem.fromItemStack(inv.getItem(l)) != null) a.add(new StoredCustomItem(CustomItem.fromItemStack(inv.getItem(l)),inv.getItem(l).getAmount()));

                                    if(a.size() > 0){
                                        CraftingRecipe recipe = CraftingRecipe.getResult(a.toArray(new StoredCustomItem[]{}));

                                        if(recipe != null){
                                            StoredCustomItem result = recipe.getResult();
                                            result.update();

                                            inv.setItem(31,result.build(p));
                                        } else {
                                            inv.setItem(31,noResult);
                                        }
                                    } else {
                                        inv.setItem(31,noResult);
                                    }
                                } else if(e.getRawSlot() == 31){
                                    if(u.getEmptySlotsInInventory() == 0){
                                        WorldUtilities.dropItem(p.getLocation(),item,p);
                                    } else {
                                        p.getInventory().addItem(item.build(p));
                                    }

                                    inv.setItem(31,noResult);
                                    for(int k : CRAFTING_SLOTS) inv.setItem(k,new ItemStack(Material.AIR));

                                    u.getCurrentCharacter().getVariables().getProfessionProgress(Profession.CRAFTING).giveExp(p,Util.randomInteger(6,15));
                                } else {
                                    if(e.getCurrentItem() != null && e.getClick() == ClickType.RIGHT) item = CustomItem.fromItemStack(current).cloneWithAmount(1);
                                    ItemStack currentItem = null;

                                    for(int k : CRAFTING_SLOTS){
                                        ItemStack iStack = inv.getItem(k);

                                        if(iStack != null){
                                            CustomItem it = CustomItem.fromItemStack(iStack);

                                            if(it.isSameItem(item) && it.getAmount()+item.getAmount() <= it.getData().getStackLimit()){
                                                currentItem = iStack;
                                            } else if(it.getData().getId() == item.getData().getId()){
                                                p.sendMessage(ChatColor.RED + "That item has already been added to the crafting station.");
                                                return;
                                            }
                                        }
                                    }

                                    if(currentItem != null){
                                        int slot = WorldUtilities.getSlotFromItem(e.getInventory(),CustomItem.fromItemStack(currentItem));

                                        if(slot > -1){
                                            currentItem.setAmount(currentItem.getAmount()+item.getAmount());
                                            inv.setItem(slot,currentItem);

                                            if(current.getAmount()-item.getAmount() > 0){
                                                current.setAmount(current.getAmount()-item.getAmount());
                                                p.getInventory().setItem(e.getSlot(),current);
                                            } else {
                                                p.getInventory().setItem(e.getSlot(),new ItemStack(Material.AIR));
                                            }

                                            // Update crafting result

                                            ArrayList<StoredCustomItem> a = new ArrayList<StoredCustomItem>();
                                            for(int l : CRAFTING_SLOTS) if(inv.getItem(l) != null && CustomItem.fromItemStack(inv.getItem(l)) != null) a.add(new StoredCustomItem(CustomItem.fromItemStack(inv.getItem(l)),inv.getItem(l).getAmount()));

                                            if(a.size() > 0){
                                                CraftingRecipe recipe = CraftingRecipe.getResult(a.toArray(new StoredCustomItem[]{}));

                                                if(recipe != null){
                                                    StoredCustomItem result = recipe.getResult();
                                                    result.update();

                                                    inv.setItem(31,result.build(p));
                                                } else {
                                                    inv.setItem(31,noResult);
                                                }
                                            } else {
                                                inv.setItem(31,noResult);
                                            }

                                            // Update end
                                        }
                                    } else {
                                        for(int k : CRAFTING_SLOTS){
                                            ItemStack iStack = inv.getItem(k);

                                            if(iStack == null){
                                                inv.setItem(k,item.build(p));

                                                current = p.getInventory().getItem(e.getSlot()) != null ? p.getInventory().getItem(e.getSlot()) : e.getCurrentItem();
                                                if(current != null/* && current.getAmount() < item.getAmount()*/){
                                                    //p.sendMessage(String.valueOf(current.getAmount()-item.getAmount()));
                                                    if(current.getAmount()-item.getAmount() > 0){
                                                        current.setAmount(current.getAmount()-item.getAmount());
                                                        p.getInventory().setItem(e.getSlot(),current);
                                                    } else {
                                                        p.getInventory().setItem(e.getSlot(),new ItemStack(Material.AIR));
                                                    }
                                                }

                                                // Update crafting result

                                                ArrayList<StoredCustomItem> a = new ArrayList<StoredCustomItem>();
                                                for(int l : CRAFTING_SLOTS) if(inv.getItem(l) != null && CustomItem.fromItemStack(inv.getItem(l)) != null) a.add(new StoredCustomItem(CustomItem.fromItemStack(inv.getItem(l)),inv.getItem(l).getAmount()));

                                                if(a.size() > 0){
                                                    CraftingRecipe recipe = CraftingRecipe.getResult(a.toArray(new StoredCustomItem[]{}));

                                                    if(recipe != null){
                                                        StoredCustomItem result = recipe.getResult();
                                                        result.update();

                                                        inv.setItem(31,result.build(p));
                                                    } else {
                                                        inv.setItem(31,noResult);
                                                    }
                                                } else {
                                                    inv.setItem(31,noResult);
                                                }

                                                // Update end

                                                break;
                                            }
                                        }
                                    }

                                    /*for(int k : CRAFTING_SLOTS){
                                        if(inv.getItem(k) == null){
                                            ItemStack i = item.build(p);

                                            inv.addItem(i);
                                            p.getInventory().setItem(e.getSlot(), new ItemStack(Material.AIR));

                                            // Update crafting result

                                            ArrayList<StoredCustomItem> a = new ArrayList<StoredCustomItem>();
                                            for(int l : CRAFTING_SLOTS) if(inv.getItem(l) != null && CustomItem.fromItemStack(inv.getItem(l)) != null) a.add(new StoredCustomItem(CustomItem.fromItemStack(inv.getItem(l)),inv.getItem(l).getAmount()));

                                            if(a.size() > 0){
                                                CraftingRecipe recipe = CraftingRecipe.getResult(a.toArray(new StoredCustomItem[]{}));

                                                if(recipe != null){
                                                    StoredCustomItem result = recipe.getResult();
                                                    result.update();

                                                    inv.setItem(31,result.build(p));
                                                } else {
                                                    inv.setItem(31,noResult);
                                                }
                                            } else {
                                                inv.setItem(31,noResult);
                                            }

                                            // Update end

                                            break;
                                        } else {
                                            CustomItem c = CustomItem.fromItemStack(inv.getItem(k));

                                            if(c != null && c.getData() != null && c.getData().getId() == item.getData().getId() && ){
                                                p.sendMessage(ChatColor.RED + "That item has already been added to the crafting station.");
                                                break;
                                            }
                                        }
                                    }*/
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
