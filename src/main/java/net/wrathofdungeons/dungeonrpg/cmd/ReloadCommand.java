package net.wrathofdungeons.dungeonrpg.cmd;

import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.items.ItemData;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.mobs.MobData;
import net.wrathofdungeons.dungeonrpg.regions.Region;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ConcurrentModificationException;

public class ReloadCommand extends Command {
    public ReloadCommand(){
        super("reload", Rank.ADMIN);
    }

    @Override
    public void execute(Player p, String label, String[] args) {
        GameUser u = GameUser.getUser(p);

        if(args.length == 1){
            String mode = args[0];

            if(mode.equalsIgnoreCase("mobs")){
                DungeonAPI.async(() -> {
                    p.sendMessage(ChatColor.GREEN + "Reloading mobs..");
                /*p.sendMessage(ChatColor.GREEN + "Despawning all current mobs..");

                try {
                    for(CustomEntity e : CustomEntity.STORAGE.values()) e.remove();
                } catch(ConcurrentModificationException e){}*/

                    p.sendMessage("Loading mob data from database..");

                    MobData.init();

                    p.sendMessage("Done!");
                });
            } else if(mode.equalsIgnoreCase("items")){
                DungeonAPI.async(() -> {
                    p.sendMessage(ChatColor.GREEN + "Reloading items..");

                    p.sendMessage("Loading item data from database..");

                    ItemData.init();

                    p.sendMessage("Done!");
                });
            } else if(mode.equalsIgnoreCase("regions")){
                if(DungeonRPG.SETUP_REGION == 0){
                    DungeonAPI.async(() -> {
                        p.sendMessage(ChatColor.GREEN + "Reloading regions..");

                        p.sendMessage("Loading region data from database..");

                        Region.init();

                        p.sendMessage("Done!");
                    });
                } else {
                    p.sendMessage(ChatColor.RED + "There is currently a region loaded in setup mode. Please use /saveregion before reloading or all progress will be lost.");
                }
            } else {
                p.sendMessage(ChatColor.RED + "/" + label + " <mobs|items|regions>");
            }
        } else {
            p.sendMessage(ChatColor.RED + "/" + label + " <mobs|items|regions>");
        }
    }
}
