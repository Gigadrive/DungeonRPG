package net.wrathofdungeons.dungeonrpg.cmd;

import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class KillCommand extends Command {
    public KillCommand() {
        super("kill");
    }

    @Override
    public void execute(Player p, String label, String[] args) {
        GameUser u = GameUser.getUser(p);

        if (args.length == 0) {
            if (u.getCurrentCharacter() != null)
                u.damage(u.getHP());
        } else {
            if (u.hasPermission(Rank.GM)) {
                Player p2 = Bukkit.getPlayer(args[0]);

                if (p2 != null && GameUser.isLoaded(p2)) {
                    GameUser u2 = GameUser.getUser(p2);

                    if (u2.getCurrentCharacter() != null) {
                        u2.damage(u2.getHP());
                        p.sendMessage(ChatColor.GREEN + "Success!");
                    } else {
                        p.sendMessage(ChatColor.RED + "The target must be in game.");
                    }
                } else {
                    p.sendMessage(ChatColor.RED + "That player is not online.");
                }
            } else {
                execute(p, label, new String[]{});
            }
        }
    }
}
