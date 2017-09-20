package net.wrathofdungeons.dungeonrpg.inv;

import net.wrathofdungeons.dungeonapi.util.ItemUtil;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.StatPointType;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.inventivetalent.menubuilder.inventory.InventoryMenuBuilder;

import java.util.ArrayList;

public class GameMenu {
    public static void openFor(Player p){
        GameUser u = GameUser.getUser(p);
        InventoryMenuBuilder inv = new InventoryMenuBuilder(Util.INVENTORY_1ROW);
        inv.withTitle("Game Menu");

        inv.withItem(2, b(u,StatPointType.STRENGTH,"Increases all damage dealt."),((player, action, item) -> c(p,StatPointType.STRENGTH)), ClickType.LEFT);
        inv.withItem(3,b(u,StatPointType.STAMINA,"Increases total health."),((player, action, item) -> c(p,StatPointType.STAMINA)), ClickType.LEFT);
        inv.withItem(4,b(u,StatPointType.INTELLIGENCE,"Increases mana regeneration."),((player, action, item) -> c(p,StatPointType.INTELLIGENCE)), ClickType.LEFT);
        inv.withItem(5,b(u,StatPointType.DEXTERITY,"Increases the chance to do critical hits."),((player, action, item) -> c(p,StatPointType.DEXTERITY)), ClickType.LEFT);
        inv.withItem(6,b(u,StatPointType.AGILITY,"Increases the chance to dodge enemy attacks."),((player, action, item) -> c(p,StatPointType.AGILITY)), ClickType.LEFT);

        inv.show(p);
    }

    private static void c(Player p, StatPointType type){
        GameUser u = GameUser.getUser(p);

        if(u.getCurrentCharacter().getStatpointsLeft() > 0){
            if(u.getCurrentCharacter().getStatpointsPure(type) < DungeonRPG.STATPOINTS_LIMIT){
                u.getCurrentCharacter().addStatpoint(type);
                u.getCurrentCharacter().reduceStatpointsLeft(1);
                p.closeInventory();
                openFor(p);
                p.playSound(p.getEyeLocation(), Sound.ORB_PICKUP,1f,2f);
            } else {
                p.sendMessage(ChatColor.RED + "You have reached the limit for that statpoint.");
            }
        } else {
            p.sendMessage(ChatColor.RED + "You don't have any statpoints left.");
        }
    }

    private static ItemStack b(GameUser u, StatPointType type, String description){
        ItemStack strength = new ItemStack(Material.ENCHANTED_BOOK);
        strength.setAmount(u.getCurrentCharacter().getStatpointsPure(type));
        ItemMeta strengthMeta = strength.getItemMeta();
        strengthMeta.setDisplayName(type.getColor() + type.getName());
        ArrayList<String> strengthLore = new ArrayList<String>();
        for(String s : Util.getWordWrapLore(description)){
            strengthLore.add(ChatColor.GOLD + s);
        }
        strengthLore.add(" ");
        strengthLore.add(ChatColor.GRAY + "Pure value: " + ChatColor.WHITE + u.getCurrentCharacter().getStatpointsPure(type));
        strengthLore.add(ChatColor.GRAY + "Additional value: " + ChatColor.WHITE + u.getCurrentCharacter().getStatpointsArtificial(type));
        strengthLore.add(ChatColor.GREEN + "Total value: " + ChatColor.YELLOW.toString() + ChatColor.BOLD + u.getCurrentCharacter().getStatpointsTotal(type));
        strengthMeta.setLore(strengthLore);
        strength.setItemMeta(strengthMeta);
        strength = ItemUtil.hideFlags(strength);

        return strength;
    }
}
