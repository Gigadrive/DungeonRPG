package net.wrathofdungeons.dungeonrpg.cmd;

import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class CreateLootChestCommand extends Command {
    public CreateLootChestCommand(){
        super("createlootchest", Rank.ADMIN);
    }

    @Override
    public void execute(Player p, String label, String[] args) {
        GameUser u = GameUser.getUser(p);

        if(u.isInSetupMode()){
            if(u.lootChestTier > 0 && u.lootChestLevel > 0){
                p.sendMessage(ChatColor.RED + "Cancelled binding.");
                u.lootChestTier = 0;
                u.lootChestLevel = 0;
            } else {
                if(args.length == 2){
                    if(Util.isValidInteger(args[0]) && Util.isValidInteger(args[1])){
                        int tier = Integer.parseInt(args[0]);
                        int level = Integer.parseInt(args[1]);

                        if(tier > 0 && level > 0){
                            u.lootChestTier = tier;
                            u.lootChestLevel = level;

                            p.sendMessage(ChatColor.YELLOW + "Click a block to bind a loot chest to it!");
                            p.sendMessage(ChatColor.YELLOW + "Use /" + label + " again to cancel.");
                            p.sendMessage(ChatColor.YELLOW + "Tier: " + tier);
                            p.sendMessage(ChatColor.YELLOW + "Level: " + level);
                        } else {
                            p.sendMessage(ChatColor.RED + "Value must be greather than 0!");
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "/" + label + " <Tier> <Level>");
                    }
                } else {
                    p.sendMessage(ChatColor.RED + "/" + label + " <Tier> <Level>");
                }
            }
        } else {
            p.sendMessage(ChatColor.RED + "You are not in setup mode.");
        }
    }
}
