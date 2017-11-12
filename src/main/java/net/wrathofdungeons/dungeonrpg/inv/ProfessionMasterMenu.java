package net.wrathofdungeons.dungeonrpg.inv;

import net.wrathofdungeons.dungeonapi.util.ItemUtil;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.professions.Profession;
import net.wrathofdungeons.dungeonrpg.professions.ProfessionProgress;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.util.WorldUtilities;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.inventivetalent.menubuilder.inventory.InventoryMenuBuilder;

import java.util.ArrayList;

public class ProfessionMasterMenu {
    public static void openFor(Player p){
        if(GameUser.isLoaded(p)){
            GameUser u = GameUser.getUser(p);

            if(u.getCurrentCharacter() != null){
                int size = Util.INVENTORY_1ROW;
                InventoryMenuBuilder inv = new InventoryMenuBuilder(size);
                inv.withTitle("Profession Master");

                int slot = 0;
                for(Profession profession : Profession.values()){
                    ProfessionProgress progress = u.getCurrentCharacter().getVariables().getProfessionProgress(profession);
                    ItemStack i = new ItemStack(profession.getIcon(),progress.getLevel(),(short)profession.getIconDurability());
                    ItemMeta iM = i.getItemMeta();
                    iM.setDisplayName(ChatColor.YELLOW + profession.getName());
                    ArrayList<String> iL = new ArrayList<String>();
                    if(progress.isStarted()){

                    } else {
                        for(String s : Util.getWordWrapLore("You haven't started this profession.")) iL.add(ChatColor.RED + s);

                        iL.add(" ");

                        for(String s : Util.getWordWrapLore("The cost for starting this profession is:")) iL.add(ChatColor.YELLOW + s);

                        if(profession.getMinLevel() > 0) iL.add(ChatColor.GRAY + "Minimum Character Level: "  + ChatColor.WHITE + profession.getMinLevel());

                        if(profession.getGoldCost() >= 1){
                            iL.add(ChatColor.GRAY + "Money: ");
                            for(CustomItem item : WorldUtilities.convertNuggetAmount(profession.getGoldCost())) iL.add(ChatColor.WHITE + "    " + item.getAmount() + "x " + item.getData().getName());
                        }

                        iL.add(" ");

                        for(String s : Util.getWordWrapLore("Click to start this profession.")) iL.add(ChatColor.GOLD + s);
                    }
                    iM.setLore(iL);
                    i.setItemMeta(iM);
                    i = ItemUtil.hideFlags(i);

                    inv.withItem(slot,i,((player, action, item) -> {
                        if(progress.isStarted()){

                        } else {
                            if(profession.getGoldCost() <= 0 || u.getTotalMoneyInInventory() >= profession.getGoldCost()){
                                if(u.getCurrentCharacter().getLevel() >= profession.getMinLevel()){
                                    p.closeInventory();
                                    u.removeMoneyFromInventory(profession.getGoldCost());
                                    p.playSound(p.getEyeLocation(), Sound.LEVEL_UP,1f,1f);
                                    p.sendMessage(ChatColor.GREEN + "You sucessfully started " + ChatColor.YELLOW + profession.getName() + ChatColor.GREEN + "!");
                                    progress.setStarted(true);
                                    if(progress.getLevel() < 1){
                                        progress.setLevel(p,1);
                                        progress.setExp(p,0);
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "Your level is too low to start this profession.");
                                    p.closeInventory();
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "You don't have enough money to start this profession.");
                                p.closeInventory();
                            }
                        }
                    }),ClickType.LEFT);

                    slot++;
                }

                inv.withItem(size-1, ItemUtil.namedItem(Material.BARRIER, ChatColor.DARK_RED + "Close",null),((player, action, item) -> p.closeInventory()), ClickType.LEFT);

                inv.show(p);
            }
        }
    }
}
