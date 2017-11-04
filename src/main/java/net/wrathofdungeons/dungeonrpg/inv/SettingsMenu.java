package net.wrathofdungeons.dungeonrpg.inv;

import net.wrathofdungeons.dungeonapi.util.ItemUtil;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.inventivetalent.menubuilder.inventory.InventoryMenuBuilder;

public class SettingsMenu {
    public static void openFor(Player p){
        GameUser u = GameUser.getUser(p);
        InventoryMenuBuilder inv = new InventoryMenuBuilder(Util.INVENTORY_1ROW);
        inv.withTitle("Settings");

        if(u.getSettingsManager().allowsFriendRequests()){
            inv.withItem(0, ItemUtil.namedItem(Material.SKULL_ITEM, ChatColor.YELLOW + "Friend Requests",new String[]{ChatColor.DARK_GRAY + "> " + ChatColor.GREEN + "Activated"},3),((player, action, item) -> {
                u.getSettingsManager().setFriendRequests(false);
                SettingsMenu.openFor(p);
            }),ClickType.LEFT);
        } else {
            inv.withItem(0, ItemUtil.namedItem(Material.SKULL_ITEM, ChatColor.YELLOW + "Friend Requests",new String[]{ChatColor.DARK_GRAY + "> " + ChatColor.RED + "Deactivated"},3),((player, action, item) -> {
                u.getSettingsManager().setFriendRequests(true);
                SettingsMenu.openFor(p);
            }),ClickType.LEFT);
        }

        if(u.getSettingsManager().allowsPrivateMessages()){
            inv.withItem(1, ItemUtil.namedItem(Material.PAPER, ChatColor.YELLOW + "Private Messages",new String[]{ChatColor.DARK_GRAY + "> " + ChatColor.GREEN + "Activated"}),((player, action, item) -> {
                u.getSettingsManager().setPrivateMessages(false);
                SettingsMenu.openFor(p);
            }),ClickType.LEFT);
        } else {
            inv.withItem(1, ItemUtil.namedItem(Material.PAPER, ChatColor.YELLOW + "Private Messages",new String[]{ChatColor.DARK_GRAY + "> " + ChatColor.RED + "Deactivated"}),((player, action, item) -> {
                u.getSettingsManager().setPrivateMessages(true);
                SettingsMenu.openFor(p);
            }),ClickType.LEFT);
        }

        if(u.getSettingsManager().allowsPartyRequests()){
            inv.withItem(2, ItemUtil.namedItem(Material.FIREWORK, ChatColor.YELLOW + "Party Requests",new String[]{ChatColor.DARK_GRAY + "> " + ChatColor.GREEN + "Activated"}),((player, action, item) -> {
                u.getSettingsManager().setPartyRequests(false);
                SettingsMenu.openFor(p);
            }),ClickType.LEFT);
        } else {
            inv.withItem(2, ItemUtil.namedItem(Material.FIREWORK, ChatColor.YELLOW + "Party Requests",new String[]{ChatColor.DARK_GRAY + "> " + ChatColor.RED + "Deactivated"}),((player, action, item) -> {
                u.getSettingsManager().setPartyRequests(true);
                SettingsMenu.openFor(p);
            }),ClickType.LEFT);
        }

        if(u.getSettingsManager().allowsGuildRequests()){
            inv.withItem(3, ItemUtil.namedItem(Material.PAINTING, ChatColor.YELLOW + "Guild Requests",new String[]{ChatColor.DARK_GRAY + "> " + ChatColor.GREEN + "Activated"}),((player, action, item) -> {
                u.getSettingsManager().setGuildRequests(false);
                SettingsMenu.openFor(p);
            }),ClickType.LEFT);
        } else {
            inv.withItem(3, ItemUtil.namedItem(Material.PAINTING, ChatColor.YELLOW + "Guild Requests",new String[]{ChatColor.DARK_GRAY + "> " + ChatColor.RED + "Deactivated"}),((player, action, item) -> {
                u.getSettingsManager().setGuildRequests(true);
                SettingsMenu.openFor(p);
            }),ClickType.LEFT);
        }

        if(u.getSettingsManager().allowsDuelRequests()){
            inv.withItem(4, ItemUtil.namedItem(Material.DIAMOND_SWORD, ChatColor.YELLOW + "Duel Requests",new String[]{ChatColor.DARK_GRAY + "> " + ChatColor.GREEN + "Activated"}),((player, action, item) -> {
                u.getSettingsManager().setDuelRequests(false);
                SettingsMenu.openFor(p);
            }),ClickType.LEFT);
        } else {
            inv.withItem(4, ItemUtil.namedItem(Material.DIAMOND_SWORD, ChatColor.YELLOW + "Duel Requests",new String[]{ChatColor.DARK_GRAY + "> " + ChatColor.RED + "Deactivated"}),((player, action, item) -> {
                u.getSettingsManager().setDuelRequests(true);
                SettingsMenu.openFor(p);
            }),ClickType.LEFT);
        }

        if(u.getSettingsManager().allowsTradeRequests()){
            inv.withItem(5, ItemUtil.namedItem(Material.CHEST, ChatColor.YELLOW + "Trade Requests",new String[]{ChatColor.DARK_GRAY + "> " + ChatColor.GREEN + "Activated"}),((player, action, item) -> {
                u.getSettingsManager().setTradeRequests(false);
                SettingsMenu.openFor(p);
            }),ClickType.LEFT);
        } else {
            inv.withItem(5, ItemUtil.namedItem(Material.CHEST, ChatColor.YELLOW + "Trade Requests",new String[]{ChatColor.DARK_GRAY + "> " + ChatColor.RED + "Deactivated"}),((player, action, item) -> {
                u.getSettingsManager().setTradeRequests(true);
                SettingsMenu.openFor(p);
            }),ClickType.LEFT);
        }

        inv.withItem(8,ItemUtil.namedItem(Material.BARRIER,ChatColor.DARK_RED + "Close",null),((player, action, item) -> GameMenu.openFor(p)), ClickType.LEFT);

        inv.show(p);
    }
}
