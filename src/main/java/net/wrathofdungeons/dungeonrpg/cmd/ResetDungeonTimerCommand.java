package net.wrathofdungeons.dungeonrpg.cmd;

import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonrpg.dungeon.DungeonType;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ResetDungeonTimerCommand extends Command {
    public ResetDungeonTimerCommand(){
        super("resetdungeontimer", Rank.GM);
    }

    @Override
    public void execute(Player p, String label, String[] args) {
        GameUser u = GameUser.getUser(p);

        if(args.length == 2){
            Player p2 = Bukkit.getPlayer(args[0]);

            if(p2 != null){
                if(GameUser.isLoaded(p2)){
                    GameUser u2 = GameUser.getUser(p2);

                    if(u2.getCurrentCharacter() != null){
                        DungeonType type = null;
                        for(DungeonType t : DungeonType.values()) if(t.name().equalsIgnoreCase(args[1])) type = t;

                        if(type != null){
                            u2.getCurrentCharacter().getVariables().setLastDungeonEntry(type,null);
                            p.sendMessage(ChatColor.GREEN + "Success.");
                        } else {
                            p.sendMessage(ChatColor.RED + "Invalid dungeon type. Valid types are:");
                            for(DungeonType t : DungeonType.values())
                                p.sendMessage(ChatColor.RED + "- " + ChatColor.YELLOW + t.name());
                        }
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
            p.sendMessage(ChatColor.RED + "/" + label + " <Player> <Dungeon Type>");
        }
    }
}
