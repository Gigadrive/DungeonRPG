package net.wrathofdungeons.dungeonrpg.cmd;

import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class TestAmountCommand extends Command {
    public TestAmountCommand(){
        super("testamount", Rank.ADMIN);
    }

    @Override
    public void execute(Player p, String label, String[] args) {
        GameUser u = GameUser.getUser(p);

        if(args.length == 1){
            if(p.getItemInHand() != null){
                if(Util.isValidInteger(args[0])){
                    p.getItemInHand().setAmount(Integer.parseInt(args[0]));
                    p.sendMessage(ChatColor.GREEN + "Amount set to " + args[0] + ".");
                } else {
                    p.sendMessage(ChatColor.RED + "Please enter a valid amount.");
                }
            } else {
                p.sendMessage(ChatColor.RED + "Please hold an item in your hand.");
            }
        } else {
            p.sendMessage(ChatColor.RED + "/" + label + " <Amount>");
        }
    }
}
