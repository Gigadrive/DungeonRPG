package net.wrathofdungeons.dungeonrpg.cmd;

import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SetSocketsCommand extends Command {
    public SetSocketsCommand(){
        super("setsockets", Rank.GM);
    }

    @Override
    public void execute(Player p, String label, String[] args) {
        GameUser u = GameUser.getUser(p);
        CustomItem item = CustomItem.fromItemStack(p.getItemInHand());

        if(item != null){
            if(args.length == 1){
                if(Util.isValidInteger(args[0])){
                    int sockets = Integer.parseInt(args[0]);

                    if(sockets <= item.getData().getRarity().getMaxSockets() && sockets > 0){
                        item.setSockets(sockets);

                        p.setItemInHand(item.build(p));

                        p.sendMessage(ChatColor.GREEN + "Success.");
                    } else {
                        p.sendMessage(ChatColor.RED + "Please enter a value between 1 and " + item.getData().getRarity().getMaxSockets() + ".");
                    }
                } else {
                    p.sendMessage(ChatColor.RED + "/" + label + " <Sockets>");
                }
            } else {
                p.sendMessage(ChatColor.RED + "/" + label + " <Sockets>");
            }
        } else {
            p.sendMessage(ChatColor.RED + "Please hold a weapon or a piece of armor in your main hand.");
        }
    }
}
