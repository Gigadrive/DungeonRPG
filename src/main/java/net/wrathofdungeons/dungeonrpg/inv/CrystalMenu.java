package net.wrathofdungeons.dungeonrpg.inv;

import net.wrathofdungeons.dungeonapi.util.ChatIcons;
import net.wrathofdungeons.dungeonapi.util.ItemUtil;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.items.ItemCategory;
import net.wrathofdungeons.dungeonrpg.items.ItemRarity;
import net.wrathofdungeons.dungeonrpg.items.awakening.Awakening;
import net.wrathofdungeons.dungeonrpg.items.crystals.Crystal;
import net.wrathofdungeons.dungeonrpg.items.crystals.CrystalType;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.util.WorldUtilities;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.inventivetalent.menubuilder.inventory.InventoryMenuBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class CrystalMenu implements Listener {
    public static final int[] ADD_SLOTS = new int[]{2,3,4,5,6,7};

    public static final int ADDSOCKET_ITEM_SLOT = 19;
    public static final int ADDSOCKET_POWDER_SLOT = 21;

    public static void openFor(Player p){
        GameUser u = GameUser.getUser(p);
        InventoryMenuBuilder inv = new InventoryMenuBuilder(Util.INVENTORY_2ROWS);
        inv.withTitle("Crystal Specialist");

        ArrayList<String> l1 = new ArrayList<String>();
        for(String s : Util.getWordWrapLore("Infuse your crystals to an item to get the matching effect."))
            l1.add(ChatColor.GRAY + s);

        ArrayList<String> l2 = new ArrayList<String>();
        for(String s : Util.getWordWrapLore("Combine 4 crystals of the same type to get a crystal of a higher tier."))
            l2.add(ChatColor.GRAY + s);

        ArrayList<String> l3 = new ArrayList<String>();
        for(String s : Util.getWordWrapLore("Remove crystals from your items (please note that you will not get the crystals back!)."))
            l3.add(ChatColor.GRAY + s);

        ArrayList<String> l4 = new ArrayList<String>();
        for(String s : Util.getWordWrapLore("Add Sockets to your items which can be infused with crystals to add special effects to your weapon or armor piece. The maximal amount of sockets addable depends on your item's tier."))
            l4.add(ChatColor.GRAY + s);

        inv.withItem(2, ItemUtil.hideFlags(ItemUtil.namedItem(Material.STAINED_CLAY, ChatColor.GREEN + "Add crystals to your items", l1.toArray(new String[]{}), 5)), ((player, action, item) -> {
            openAdd(p);
        }), InventoryMenuBuilder.ALL_CLICK_TYPES);

        inv.withItem(4, ItemUtil.hideFlags(ItemUtil.namedItem(Material.STAINED_CLAY, ChatColor.YELLOW + "Combine your crystals", l2.toArray(new String[]{}), 4)), ((player, action, item) -> {
            openCombine(p);
        }), InventoryMenuBuilder.ALL_CLICK_TYPES);

        inv.withItem(6, ItemUtil.hideFlags(ItemUtil.namedItem(Material.STAINED_CLAY, ChatColor.RED + "Remove Crystals from your items", l3.toArray(new String[]{}), 14)), ((player, action, item) -> {
            openRemove(p);
        }), InventoryMenuBuilder.ALL_CLICK_TYPES);

        inv.withItem(4+9, ItemUtil.hideFlags(ItemUtil.namedItem(Material.ANVIL, ChatColor.AQUA + "Add Sockets to your items", l4.toArray(new String[]{}))), ((player, action, item) -> {
            openAddSockets(p);
        }), InventoryMenuBuilder.ALL_CLICK_TYPES);

        inv.show(p);
    }

    public static void openAdd(Player p){
        GameUser u = GameUser.getUser(p);
        InventoryMenuBuilder inv = new InventoryMenuBuilder(Util.INVENTORY_1ROW);
        inv.withTitle("Add crystals");

        ItemStack right = new ItemStack(Material.SKULL_ITEM,1,(short)3);
        SkullMeta rightM = (SkullMeta)right.getItemMeta();
        rightM.setDisplayName(" ");
        rightM.setOwner("MHF_ArrowRight");
        right.setItemMeta(rightM);

        inv.withItem(1,right);
        inv.withItem(8,ItemUtil.namedItem(Material.STAINED_CLAY, ChatColor.GREEN + "Add Crystals",null,5));

        inv.show(p);
    }

    public static void openCombine(Player p){
        GameUser u = GameUser.getUser(p);
        InventoryMenuBuilder inv = new InventoryMenuBuilder(Util.INVENTORY_1ROW);
        inv.withTitle("Comine crystals");


        inv.show(p);
    }

    public static void openRemove(Player p){
        GameUser u = GameUser.getUser(p);
        InventoryMenuBuilder inv = new InventoryMenuBuilder(Util.INVENTORY_1ROW);
        inv.withTitle("Remove crystals");


        inv.show(p);
    }

    public static void openAddSockets(Player p){
        GameUser u = GameUser.getUser(p);
        InventoryMenuBuilder inv = new InventoryMenuBuilder(Util.INVENTORY_4ROWS);
        inv.withTitle("Add sockets");

        inv.withItem(10,ItemUtil.namedItem(Material.SIGN,ChatColor.GOLD + "Weapon / Armor Piece",null));
        inv.withItem(12,ItemUtil.namedItem(Material.SIGN,ChatColor.GOLD + "Ancient Powder",null));

        inv.withItem(16,ItemUtil.namedItem(Material.STAINED_CLAY, ChatColor.GREEN + "Add Socket",null,5));
        inv.withItem(25,ItemUtil.namedItem(Material.STAINED_CLAY, ChatColor.RED + "Cancel",null,14), ((player, action, item) -> player.closeInventory()), ClickType.LEFT);

        for(int i = 0; i < inv.getInventory().getSize(); i++) if(inv.getInventory().getItem(i) == null && i != ADDSOCKET_ITEM_SLOT && i != ADDSOCKET_POWDER_SLOT) inv.withItem(i,ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));

        inv.show(p);
    }

    private int evaluateAddCrystalsPrice(CustomItem item, ArrayList<CustomItem> crystals){
        int pricePerCrystal = 32;

        return (crystals.size()+item.getCrystals().size())*pricePerCrystal;
    }

    private int evaluateAddSocketPrice(CustomItem item){
        int socketsToAdd = 1;
        int pricePerSocket = 64;

        return (item.getSockets()+socketsToAdd)*pricePerSocket;
    }

    private void updateAdd(Player p, Inventory inv){
        updateAdd(p,inv,true);
    }

    private void updateAdd(Player p, Inventory inv, boolean removeAll){
        GameUser u = GameUser.getUser(p);
        boolean hasItem = inv.getItem(0) != null;

        if(hasItem){
            CustomItem item = CustomItem.fromItemStack(inv.getItem(0));
            if(removeAll) for(int i : ADD_SLOTS) inv.setItem(i,null);

            int freeSockets = item.getSockets()-item.getCrystals().size();

            if(freeSockets < ADD_SLOTS.length){
                ArrayList<Integer> a = new ArrayList<Integer>();
                for(int i : ADD_SLOTS) a.add(i);

                Collections.reverse(a);

                ArrayList<String> lore = new ArrayList<String>();
                for(String s : Util.getWordWrapLore("Talk to the Crystal Specialist to add more sockets to this item."))
                    lore.add(ChatColor.GRAY + s);

                int f = freeSockets > ADD_SLOTS.length-1 ? ADD_SLOTS.length-1 : freeSockets;

                for(int i = 0; i < ((ADD_SLOTS.length-f)); i++)
                    inv.setItem(a.get(i),ItemUtil.namedItem(Material.BARRIER,ChatColor.RED + "No Socket",lore.toArray(new String[]{})));
            }

            ArrayList<String> iL = new ArrayList<String>();

            ArrayList<CustomItem> crystals = new ArrayList<CustomItem>();
            for(int i : ADD_SLOTS){
                CustomItem it = CustomItem.fromItemStack(inv.getItem(i));

                if(it != null){
                    if(it.getData().getCategory() == ItemCategory.CRYSTAL){
                        crystals.add(it);
                    }
                }
            }

            if(crystals.size() > 0){
                for(String s : Util.getWordWrapLore("This will add the following effects to your item:"))
                    iL.add(ChatColor.GRAY + s);

                iL.add(" ");

                for(CustomItem crystal : crystals){
                    if(item.getData().getCategory() == ItemCategory.ARMOR){
                        for(Awakening a : crystal.getData().getCrystalType().getEffectsOnArmor()){
                            if(a.value > 0){
                                // IS POSITIVE
                                if(a.isPercentage){
                                    iL.add(ChatColor.LIGHT_PURPLE + a.type.getDisplayName() + ": " + ChatColor.GREEN + "+" + a.value + "%");
                                } else {
                                    iL.add(ChatColor.LIGHT_PURPLE + a.type.getDisplayName() + ": " + ChatColor.GREEN + "+" + a.value);
                                }
                            } else if(a.value == 0){
                                // IS NEUTRAL (shouldn't really happen)
                                if(a.isPercentage){
                                    iL.add(ChatColor.LIGHT_PURPLE + a.type.getDisplayName() + ": " + ChatColor.YELLOW + "+" + a.value + "%");
                                } else {
                                    iL.add(ChatColor.LIGHT_PURPLE + a.type.getDisplayName() + ": " + ChatColor.YELLOW + "+" + a.value);
                                }
                            } else {
                                // IS NEGATIVE
                                if(a.isPercentage){
                                    iL.add(ChatColor.LIGHT_PURPLE + a.type.getDisplayName() + ": " + ChatColor.RED + a.value + "%");
                                } else {
                                    iL.add(ChatColor.LIGHT_PURPLE + a.type.getDisplayName() + ": " + ChatColor.RED + a.value);
                                }
                            }
                        }
                    } else {
                        for(Awakening a : crystal.getData().getCrystalType().getEffectsOnWeapons()){
                            if(a.value > 0){
                                // IS POSITIVE
                                if(a.isPercentage){
                                    iL.add(ChatColor.LIGHT_PURPLE + a.type.getDisplayName() + ": " + ChatColor.GREEN + "+" + a.value + "%");
                                } else {
                                    iL.add(ChatColor.LIGHT_PURPLE + a.type.getDisplayName() + ": " + ChatColor.GREEN + "+" + a.value);
                                }
                            } else if(a.value == 0){
                                // IS NEUTRAL (shouldn't really happen)
                                if(a.isPercentage){
                                    iL.add(ChatColor.LIGHT_PURPLE + a.type.getDisplayName() + ": " + ChatColor.YELLOW + "+" + a.value + "%");
                                } else {
                                    iL.add(ChatColor.LIGHT_PURPLE + a.type.getDisplayName() + ": " + ChatColor.YELLOW + "+" + a.value);
                                }
                            } else {
                                // IS NEGATIVE
                                if(a.isPercentage){
                                    iL.add(ChatColor.LIGHT_PURPLE + a.type.getDisplayName() + ": " + ChatColor.RED + a.value + "%");
                                } else {
                                    iL.add(ChatColor.LIGHT_PURPLE + a.type.getDisplayName() + ": " + ChatColor.RED + a.value);
                                }
                            }
                        }
                    }
                }

                iL.add(" ");
                iL.add(ChatColor.GOLD + "Price:");

                int price = evaluateAddCrystalsPrice(item, crystals);
                if(u.getTotalMoneyInInventory() >= price){
                    iL.add(ChatColor.GOLD + "- " + ChatColor.DARK_GREEN + ChatIcons.CHECK_MARK + " " + ChatColor.WHITE + price + " " + ChatColor.GRAY + "Golden Nuggets");
                } else {
                    iL.add(ChatColor.GOLD + "- " + ChatColor.DARK_RED + ChatIcons.X + " " + ChatColor.WHITE + price + " " + ChatColor.GRAY + "Golden Nuggets");
                }
            }

            inv.setItem(8,ItemUtil.namedItem(Material.STAINED_CLAY, ChatColor.GREEN + "Add Crystals",iL.toArray(new String[]{}),5));
        } else {
            boolean drop = false;

            for(int i : ADD_SLOTS){
                ItemStack ii = inv.getItem(i);
                CustomItem cii = CustomItem.fromItemStack(ii);

                if(ii != null && cii != null){
                    if(u.getEmptySlotsInInventory() > 0){
                        p.getInventory().addItem(cii.build(p));
                    } else {
                        WorldUtilities.dropItem(p.getLocation(),cii,p);
                        drop = true;
                    }
                }

                if(removeAll) inv.setItem(i,null);
            }

            if(drop)
                p.sendMessage(ChatColor.GRAY + "Some items were dropped to the ground, because your inventory is full.");

            inv.setItem(8,ItemUtil.namedItem(Material.STAINED_CLAY, ChatColor.GREEN + "Add Crystals",null,5));
        }
    }

    private void updateAddSockets(Player p, Inventory inv){
        GameUser u = GameUser.getUser(p);

        boolean hasItem = inv.getItem(ADDSOCKET_ITEM_SLOT) != null;
        boolean hasPowder = inv.getItem(ADDSOCKET_POWDER_SLOT) != null;

        if(hasItem && hasPowder){
            CustomItem item = CustomItem.fromItemStack(inv.getItem(ADDSOCKET_ITEM_SLOT));
            int price = evaluateAddSocketPrice(item);

            ArrayList<String> iL = new ArrayList<String>();

            for(String s : Util.getWordWrapLore("Click to add 1 Socket to this weapon or armor piece, that can be infused with a crystal to add additional stats on your weapon or armor piece."))
                iL.add(ChatColor.GRAY + s);

            iL.add(" ");
            iL.add(ChatColor.GOLD + "Price:");

            if(u.getTotalMoneyInInventory() >= price){
                iL.add(ChatColor.GOLD + "- " + ChatColor.DARK_GREEN + ChatIcons.CHECK_MARK + " " + ChatColor.WHITE + price + " " + ChatColor.GRAY + "Golden Nuggets");
            } else {
                iL.add(ChatColor.GOLD + "- " + ChatColor.DARK_RED + ChatIcons.X + " " + ChatColor.WHITE + price + " " + ChatColor.GRAY + "Golden Nuggets");
            }

            inv.setItem(16,ItemUtil.namedItem(Material.STAINED_CLAY, ChatColor.GREEN + "Add Socket",iL.toArray(new String[]{}),5));
        } else {
            inv.setItem(16,ItemUtil.namedItem(Material.STAINED_CLAY, ChatColor.GREEN + "Add Socket",null,5));
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        if(e.getWhoClicked() instanceof Player){
            Player p = (Player)e.getWhoClicked();
            GameUser u = GameUser.getUser(p);
            Inventory inv = e.getInventory();

            if(inv.getName().equals("Add crystals")){
                e.setCancelled(true);

                boolean hasItem = e.getInventory().getItem(0) != null;

                if(e.getCurrentItem() != null && e.getCurrentItem().getType() != null && e.getCurrentItem().getItemMeta() != null && e.getCurrentItem().getItemMeta().getDisplayName() != null){
                    String dis = e.getCurrentItem().getItemMeta().getDisplayName();
                    CustomItem item = CustomItem.fromItemStack(e.getCurrentItem());

                    if(e.getClickedInventory() == e.getView().getBottomInventory()){
                        if(item != null){
                            if(item.getData().getCategory() == ItemCategory.CRYSTAL){
                                for(int i : ADD_SLOTS){
                                    if(inv.getItem(i) == null){
                                        if(e.getCurrentItem().getAmount() == 1){
                                            p.getInventory().setItem(e.getSlot(), new ItemStack(Material.AIR));
                                        } else {
                                            e.getCurrentItem().setAmount(e.getCurrentItem().getAmount()-1);
                                        }

                                        inv.setItem(i,item.cloneWithAmount(1).build(p));

                                        updateAdd(p,inv,false);

                                        break;
                                    }
                                }
                            } else {
                                if(item.getData().getRarity() != null && item.getData().getRarity() != ItemRarity.NONE && item.getData().getRarity().getMaxSockets() > 0){
                                    if(!hasItem){
                                        inv.setItem(0,CustomItem.fromItemStack(e.getCurrentItem()).build(p));
                                        p.getInventory().setItem(e.getSlot(), new ItemStack(Material.AIR));

                                        updateAdd(p,inv);
                                    } else {
                                        p.sendMessage(ChatColor.RED + "You can only add crystals to one item at once.");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "That item can't hold any crystals.");
                                }
                            }
                        }
                    } else {
                        for(int slot : ADD_SLOTS){
                            if(slot == e.getRawSlot()){
                                if(item != null){
                                    if(item.getData().getCategory() == ItemCategory.CRYSTAL){
                                        if(p.getInventory().addItem(item.build(p)).size() != 0)
                                            WorldUtilities.dropItem(p.getLocation(),item,p);

                                        inv.setItem(e.getSlot(), new ItemStack(Material.AIR));

                                        updateAdd(p,inv,false);
                                    }
                                }

                                return;
                            }
                        }

                        if(dis.equals(ChatColor.GREEN + "Add Crystals")){
                            if(hasItem){
                                CustomItem current = CustomItem.fromItemStack(inv.getItem(0));
                                boolean added = false;

                                ArrayList<CustomItem> crystals = new ArrayList<CustomItem>();

                                for(int slot : ADD_SLOTS){
                                    CustomItem c = CustomItem.fromItemStack(inv.getItem(slot));

                                    if(c != null) crystals.add(c);
                                }

                                int price = evaluateAddCrystalsPrice(current, crystals);

                                if(u.getTotalMoneyInInventory() >= price){
                                    for(int slot : ADD_SLOTS){
                                        ItemStack i = inv.getItem(slot);
                                        CustomItem c = CustomItem.fromItemStack(i);

                                        if(c != null){
                                            crystals.add(c);

                                            if(c.getData().getCategory() == ItemCategory.CRYSTAL && c.getData().getCrystalType() != null){
                                                added = true;

                                                Crystal crystal = new Crystal(c.getData().getCrystalType());
                                                current.getCrystals().add(crystal);

                                                inv.setItem(0,current.build(p));

                                                inv.setItem(slot,new ItemStack(Material.AIR));
                                            }
                                        }
                                    }

                                    if(!added){
                                        p.sendMessage(ChatColor.RED + "Failed to add any crystals to your item.");
                                    } else {
                                        u.removeMoneyFromInventory(price);
                                        updateAdd(p,inv);
                                        p.playSound(p.getEyeLocation(), Sound.ENTITY_PLAYER_LEVELUP,1f,1f);
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "You don't have enough money in your inventory to add these crystals to that item.");
                                }
                            }
                        } else {
                            if(e.getRawSlot() == 0){
                                if(hasItem){
                                    if(u.getEmptySlotsInInventory() > 0){
                                        p.getInventory().addItem(item.build(p));
                                    } else {
                                        WorldUtilities.dropItem(p.getLocation(),item,p);
                                    }

                                    inv.setItem(e.getSlot(), new ItemStack(Material.AIR));

                                    updateAdd(p,inv);
                                }
                            }
                        }
                    }
                }
            } else if(inv.getName().equals("Add sockets")){
                e.setCancelled(true);

                boolean hasItem = e.getInventory().getItem(ADDSOCKET_ITEM_SLOT) != null;
                boolean hasPowder = e.getInventory().getItem(ADDSOCKET_POWDER_SLOT) != null;

                if(e.getCurrentItem() != null && e.getCurrentItem().getType() != null && e.getCurrentItem().getItemMeta() != null && e.getCurrentItem().getItemMeta().getDisplayName() != null){
                    String dis = e.getCurrentItem().getItemMeta().getDisplayName();
                    CustomItem item = CustomItem.fromItemStack(e.getCurrentItem());

                    if(e.getClickedInventory() == e.getView().getBottomInventory()){
                        if(item != null){
                            if(item.getData().getId() == DungeonRPG.ANCIENT_POWDER){
                                if(!hasPowder){
                                    if(e.getCurrentItem().getAmount() == 1){
                                        p.getInventory().setItem(e.getSlot(), new ItemStack(Material.AIR));
                                    } else {
                                        e.getCurrentItem().setAmount(e.getCurrentItem().getAmount()-1);
                                    }

                                    inv.setItem(ADDSOCKET_POWDER_SLOT,item.cloneWithAmount(1).build(p));

                                    updateAddSockets(p,inv);
                                }
                            } else {
                                if(!hasItem){
                                    if(item.getData().getRarity() != null && item.getData().getRarity() != ItemRarity.NONE && item.getData().getRarity().getMaxSockets() > 0){
                                        if(item.getSockets() < item.getData().getRarity().getMaxSockets()){
                                            inv.setItem(ADDSOCKET_ITEM_SLOT,CustomItem.fromItemStack(e.getCurrentItem()).build(p));
                                            p.getInventory().setItem(e.getSlot(), new ItemStack(Material.AIR));

                                            updateAddSockets(p,inv);
                                        } else {
                                            p.sendMessage(ChatColor.RED + "That item can't have more than " + item.getData().getRarity().getMaxSockets() + " sockets.");
                                        }
                                    } else {
                                        p.sendMessage(ChatColor.RED + "You can't add sockets to that item.");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "You can only add sockets to one item at once.");
                                }
                            }
                        }
                    } else {
                        if(e.getSlot() == ADDSOCKET_ITEM_SLOT){
                            if(hasItem){
                                if(u.getEmptySlotsInInventory() > 0){
                                    p.getInventory().addItem(item.build(p));
                                } else {
                                    WorldUtilities.dropItem(p.getLocation(),item,p);
                                }

                                inv.setItem(e.getSlot(), new ItemStack(Material.AIR));

                                updateAddSockets(p,inv);
                            }
                        } else if(e.getSlot() == ADDSOCKET_POWDER_SLOT){
                            if(hasPowder){
                                if(u.getEmptySlotsInInventory() > 0){
                                    p.getInventory().addItem(item.build(p));
                                } else {
                                    WorldUtilities.dropItem(p.getLocation(),item,p);
                                }

                                inv.setItem(e.getSlot(), new ItemStack(Material.AIR));

                                updateAddSockets(p,inv);
                            }
                        } else if(dis.equals(ChatColor.GREEN + "Add Socket")){
                            if(hasItem){
                                if(hasPowder){
                                    CustomItem it = CustomItem.fromItemStack(inv.getItem(ADDSOCKET_ITEM_SLOT));

                                    if(it.getSockets() < it.getData().getRarity().getMaxSockets()){
                                        int price = evaluateAddSocketPrice(it);

                                        if(u.getTotalMoneyInInventory() >= price){
                                            u.removeMoneyFromInventory(price);
                                            inv.setItem(ADDSOCKET_POWDER_SLOT,new ItemStack(Material.AIR));

                                            it.setSockets(it.getSockets()+1);

                                            inv.setItem(ADDSOCKET_ITEM_SLOT,it.build(p));

                                            p.playSound(p.getEyeLocation(),Sound.ENTITY_PLAYER_LEVELUP,1f,1f);
                                        } else {
                                            p.sendMessage(ChatColor.RED + "You don't have enough money to add a socket to this weapon.");
                                        }
                                    } else {
                                        p.sendMessage(ChatColor.RED + "That item can't have more than " + it.getData().getRarity().getMaxSockets() + " sockets.");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "Please add 1 Ancient Powder.");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "Please add a weapon or armor piece.");
                            }
                        }
                    }
                }
            }
        }
    }
}
