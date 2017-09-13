package net.wrathofdungeons.dungeonrpg.inv;

import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonapi.util.ItemUtil;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.inventivetalent.menubuilder.inventory.InventoryMenuBuilder;
import net.wrathofdungeons.dungeonrpg.user.Character;

import java.util.ArrayList;

public class CharacterSelectionMenu {
    public static ArrayList<Player> CREATING = new ArrayList<Player>();

    public static void openSelection(Player p){
        if(CREATING.contains(p)) return;

        GameUser u = GameUser.getUser(p);
        InventoryMenuBuilder inv = new InventoryMenuBuilder(Util.INVENTORY_2ROWS);
        inv.withTitle("Select your character");

        if(u.getCharacters().size() >= 1){
            charC(p, inv,u.getCharacters().get(0),1);
        } else {
            charC(p, inv,null,1);
        }

        if(u.getCharacters().size() >= 2){
            charC(p, inv,u.getCharacters().get(1),3);
        } else {
            charC(p, inv,null,3);
        }

        if(u.getCharacters().size() >= 3){
            charC(p, inv,u.getCharacters().get(2),5);
        } else {
            charC(p, inv,null,5);
        }

        if(u.getCharacters().size() >= 4){
            charC(p, inv,u.getCharacters().get(3),7);
        } else {
            charC(p, inv,null,7);
        }

        if(u.getCharacters().size() >= 5){
            charC(p, inv,u.getCharacters().get(4),10);
        } else {
            if(!u.hasPermission(Rank.DONATOR)){
                inv.withItem(10, ItemUtil.namedItem(Material.STAINED_GLASS_PANE, ChatColor.RED + "Donators only",new String[]{ChatColor.GRAY + "Visit our website to buy a donator rank",ChatColor.GRAY + "and get access to more slots!"}, 14));
            } else {
                charC(p, inv,null,10);
            }
        }

        if(u.getCharacters().size() >= 6){
            charC(p, inv,u.getCharacters().get(5),12);
        } else {
            if(!u.hasPermission(Rank.DONATOR)){
                inv.withItem(12, ItemUtil.namedItem(Material.STAINED_GLASS_PANE, ChatColor.RED + "Donators only",new String[]{ChatColor.GRAY + "Visit our website to buy a donator rank",ChatColor.GRAY + "and get access to more slots!"}, 14));
            } else {
                charC(p, inv,null,12);
            }
        }

        if(u.getCharacters().size() >= 7){
            charC(p, inv,u.getCharacters().get(6),14);
        } else {
            if(!u.hasPermission(Rank.DONATOR)){
                inv.withItem(14, ItemUtil.namedItem(Material.STAINED_GLASS_PANE, ChatColor.RED + "Donators only",new String[]{ChatColor.GRAY + "Visit our website to buy a donator rank",ChatColor.GRAY + "and get access to more slots!"}, 14));
            } else {
                charC(p, inv,null,14);
            }
        }

        if(u.getCharacters().size() >= 8){
            charC(p, inv,u.getCharacters().get(7),16);
        } else {
            if(!u.hasPermission(Rank.DONATOR)){
                inv.withItem(16, ItemUtil.namedItem(Material.STAINED_GLASS_PANE, ChatColor.RED + "Donators only",new String[]{ChatColor.GRAY + "Visit our website to buy a donator rank",ChatColor.GRAY + "and get access to more slots!"}, 14));
            } else {
                charC(p, inv,null,16);
            }
        }

        inv.show(p);
    }

    private static void charC(Player p, InventoryMenuBuilder inv, Character c, int slot){
        GameUser u = GameUser.getUser(p);

        if(c == null){
            inv.withItem(slot,ItemUtil.namedItem(Material.STAINED_GLASS_PANE,ChatColor.AQUA + "Click to create a character",null, 3), ((player, action, item) -> openCreation(player)), ClickType.LEFT);
        } else {
            ItemStack item = new ItemStack(c.getRpgClass().getIcon());
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.setDisplayName(ChatColor.GREEN + c.getRpgClass().getName());
            ArrayList<String> itemLore = new ArrayList<String>();

            itemLore.add(ChatColor.GOLD + "Level: " + ChatColor.YELLOW + String.valueOf(c.getLevel()));
            itemLore.add(ChatColor.GOLD + "Class: " + ChatColor.YELLOW + String.valueOf(c.getRpgClass().getName()));
            itemLore.add(" ");
            itemLore.add(ChatColor.LIGHT_PURPLE + "Click to play!");
            itemMeta.setLore(itemLore);

            item.setItemMeta(itemMeta);

            inv.withItem(slot,ItemUtil.hideFlags(item),((player, action, item1) -> u.playCharacter(c)), ClickType.LEFT);
        }
    }

    public static void openCreation(Player p){
        if(CREATING.contains(p)) return;

        GameUser u = GameUser.getUser(p);
        InventoryMenuBuilder inv = new InventoryMenuBuilder(Util.INVENTORY_1ROW);
        inv.withTitle("Select your class");

        inv.withItem(1,ItemUtil.hideFlags(ItemUtil.namedItem(Material.IRON_AXE,ChatColor.AQUA + "Mercenary",null)), ((player, action, item) -> {
            CREATING.add(p);
            p.closeInventory();
            u.addCharacter(RPGClass.MERCENARY);
            p.sendMessage(ChatColor.GRAY + "Creating character..");
        }), ClickType.LEFT);

        inv.withItem(3,ItemUtil.hideFlags(ItemUtil.namedItem(Material.BOW,ChatColor.AQUA + "Archer",null)), ((player, action, item) -> {
            CREATING.add(p);
            p.closeInventory();
            u.addCharacter(RPGClass.ARCHER);
            p.sendMessage(ChatColor.GRAY + "Creating character..");
        }), ClickType.LEFT);

        inv.withItem(5,ItemUtil.hideFlags(ItemUtil.namedItem(Material.STICK,ChatColor.AQUA + "Magician",null)), ((player, action, item) -> {
            CREATING.add(p);
            p.closeInventory();
            u.addCharacter(RPGClass.MAGICIAN);
            p.sendMessage(ChatColor.GRAY + "Creating character..");
        }), ClickType.LEFT);

        inv.withItem(7,ItemUtil.hideFlags(ItemUtil.namedItem(Material.SHEARS,ChatColor.AQUA + "Assassin",null)), ((player, action, item) -> {
            CREATING.add(p);
            p.closeInventory();
            u.addCharacter(RPGClass.ASSASSIN);
            p.sendMessage(ChatColor.GRAY + "Creating character..");
        }), ClickType.LEFT);

        inv.show(p);
    }
}
