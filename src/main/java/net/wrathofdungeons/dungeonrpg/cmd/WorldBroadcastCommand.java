package net.wrathofdungeons.dungeonrpg.cmd;

import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class WorldBroadcastCommand extends Command {
    public WorldBroadcastCommand(){
        super("w", Rank.GM);
    }

    @Override
    public void execute(Player p, String label, String[] args) {
        if(args.length == 0){
            p.sendMessage(ChatColor.RED + "/" + label + " <Message>");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                sb.append(" ").append(args[i]);
            }
            String message = sb.toString().substring(1);

            for(Player pp : Bukkit.getOnlinePlayers()){
                GameUser uu = GameUser.getUser(pp);

                if(uu.getCurrentCharacter() != null || pp == p){
                    pp.sendMessage(ChatColor.YELLOW + "[GM Info] " + message);
                }
            }
        }
    }
}
