package net.wrathofdungeons.dungeonrpg.cmd;

import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ExpCommand extends Command {
    public ExpCommand(){
        super(new String[]{"xp","exp","givexp","giveexp"}, Rank.GM);
    }

    @Override
    public void execute(Player p, String label, String[] args) {
        GameUser u = GameUser.getUser(p);

        if(args.length == 1){
            execute(p,label,new String[]{args[0],p.getName()});
        } else if(args.length == 2){
            if(Util.isValidInteger(args[0])){
                int exp = Integer.parseInt(args[0]);
                String name = args[1];
                Player p2 = Bukkit.getPlayer(name);

                if(p2 != null){
                    if(GameUser.isLoaded(p2)){
                        GameUser u2 = GameUser.getUser(p2);

                        if(u2.getCurrentCharacter() != null){
                            p.sendMessage(ChatColor.GREEN + "Successfully gave " + exp + " exp to " + p2.getName() + ".");
                            u2.giveEXP(exp,true);
                        } else {
                            p.sendMessage(ChatColor.RED + "The target must be in game.");
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "The target must be in game.");
                    }
                } else {
                    p.sendMessage(ChatColor.RED + "That player is not online.");
                }
            } else {
                p.sendMessage(ChatColor.RED + "Please enter a valid number.");
            }
        } else {
            p.sendMessage(ChatColor.RED + "/" + label + " <EXP> [Player]");
        }
    }
}
