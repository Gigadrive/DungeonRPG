package net.wrathofdungeons.dungeonrpg.cmd;

import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.regions.Region;
import net.wrathofdungeons.dungeonrpg.regions.StoredLocation;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class LoadRegionCommand extends Command {
    public LoadRegionCommand(){
        super("loadregion", Rank.ADMIN);
    }

    @Override
    public void execute(Player p, String label, String[] args) {
        GameUser u = GameUser.getUser(p);

        if(!DungeonRPG.isTestServer()){
            p.sendMessage(ChatColor.RED + "That command is not available on this server.");
            return;
        }

        if(u.isInSetupMode()){
            if(DungeonRPG.SETUP_REGION == 0){
                if(args.length == 1){
                    if(Util.isValidInteger(args[0])){
                        int regionID = Integer.parseInt(args[0]);
                        Region region = Region.getRegion(regionID);

                        if(region != null){
                            DungeonRPG.SETUP_REGION = regionID;
                            p.sendMessage(ChatColor.GREEN + "Loading region..");

                            int i = 0;
                            for (StoredLocation l : region.getLocations()) {
                                Location loc = l.toBukkitLocation();

                                DungeonRPG.setLocationIndicator(loc,l.type);

                                i++;
                            }

                            if(i == 1){
                                p.sendMessage(ChatColor.GREEN + "Loaded " + i + " location!");
                            } else {
                                p.sendMessage(ChatColor.GREEN + "Loaded " + i + " locations!");
                            }
                        } else {
                            p.sendMessage(ChatColor.RED + "Unknown region.");
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "/" + label + " <Region ID>");
                    }
                } else {
                    p.sendMessage(ChatColor.RED + "/" + label + " <Region ID>");
                }
            } else {
                p.sendMessage(ChatColor.RED + "Region #" + DungeonRPG.SETUP_REGION + " is currently loaded. Please save it first.");
            }
        } else {
            p.sendMessage(ChatColor.RED + "You are not in setup mode.");
        }
    }
}
