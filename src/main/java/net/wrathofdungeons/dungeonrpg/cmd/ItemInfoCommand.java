package net.wrathofdungeons.dungeonrpg.cmd;

import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ItemInfoCommand extends Command {
    public ItemInfoCommand(){
        super("iteminfo", Rank.GM);
    }

    @Override
    public void execute(Player p, String label, String[] args) {
        if(p.getItemInHand() != null){
            CustomItem item = CustomItem.fromItemStack(p.getItemInHand());

            if(item != null){
                p.sendMessage(ChatColor.GREEN + "Data ID: " + item.getData().getId());
                p.sendMessage(ChatColor.GREEN + "JSON Data:" + ChatColor.GRAY + item.toString());
            } else {
                p.sendMessage(ChatColor.RED + "Unknown item.");
            }
        } else {
            p.sendMessage(ChatColor.RED + "You have to hold an item in your hand.");
        }
    }
}
