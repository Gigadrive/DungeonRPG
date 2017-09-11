package net.wrathofdungeons.dungeonrpg.cmd;

import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonapi.user.User;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.items.ItemData;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class GiveItemCommand extends Command {
    public GiveItemCommand(){
        super("giveitem", Rank.GM);
    }

    @Override
    public void execute(Player p, String label, String[] args) {
        if(args.length == 1){
            if(Util.isValidInteger(args[0])){
                int id = Integer.parseInt(args[0]);

                if(ItemData.getData(id) != null){
                    ItemData data = ItemData.getData(id);

                    if(GameUser.isLoaded(p) && GameUser.getUser(p).getCurrentCharacter() != null){
                        p.getInventory().addItem(new CustomItem(data).build(p));
                        p.sendMessage(ChatColor.GREEN + "Success.");
                    } else {
                        p.sendMessage(ChatColor.RED + "Player must be ingame!");
                    }
                } else {
                    p.sendMessage(ChatColor.RED + "Invalid Item ID!");
                }
            } else {
                p.sendMessage(ChatColor.RED + "Invalid Item ID!");
            }
        } else if(args.length == 2){
            if(Util.isValidInteger(args[0])){
                int id = Integer.parseInt(args[0]);
                Player p2 = Bukkit.getPlayer(args[1]);

                if(ItemData.getData(id) != null){
                    ItemData data = ItemData.getData(id);

                    if(p2 != null){
                        if(GameUser.isLoaded(p2) && GameUser.getUser(p2).getCurrentCharacter() != null){
                            p2.getInventory().addItem(new CustomItem(data).build(p2));
                            p.sendMessage(ChatColor.GREEN + "Success.");
                        } else {
                            p.sendMessage(ChatColor.RED + "Player must be ingame!");
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "Unknown player!");
                    }
                } else {
                    p.sendMessage(ChatColor.RED + "Invalid Item ID!");
                }
            } else {
                p.sendMessage(ChatColor.RED + "Invalid Item ID!");
            }
        } else {
            p.sendMessage(ChatColor.RED + "/" + label + " <Item-ID> [Player]");
        }
    }
}
