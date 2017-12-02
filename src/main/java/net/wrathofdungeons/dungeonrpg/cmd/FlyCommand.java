package net.wrathofdungeons.dungeonrpg.cmd;

import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class FlyCommand extends Command {
    public FlyCommand(){
        super("fly", Rank.GM);
    }

    @Override
    public void execute(Player p, String label, String[] args) {
        GameUser u = GameUser.getUser(p);

        if(args.length == 0){
            p.setAllowFlight(!p.getAllowFlight());

            if(p.getAllowFlight()){
                p.sendMessage(ChatColor.GREEN + "Flight enabled.");
            } else {
                p.sendMessage(ChatColor.RED + "Flight disabled.");
            }
        } else {
            Player p2 = Bukkit.getPlayer(args[0]);

            if(p2 != null){
                p2.setAllowFlight(!p2.getAllowFlight());

                if(p2.getAllowFlight()){
                    p.sendMessage(ChatColor.GREEN + "Flight enabled for " + p2.getName() + ".");
                } else {
                    p.sendMessage(ChatColor.RED + "Flight disabled for " + p2.getName() + ".");
                }
            } else {
                p.sendMessage(ChatColor.RED + "That player is not online.");
            }
        }
    }
}
