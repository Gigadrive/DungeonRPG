package net.wrathofdungeons.dungeonrpg.cmd;

import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.regions.Region;
import net.wrathofdungeons.dungeonrpg.regions.RegionLocation;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class SaveRegionCommand extends Command {
    public SaveRegionCommand(){
        super("saveregion", Rank.ADMIN);
    }

    @Override
    public void execute(Player p, String label, String[] args) {
        GameUser u = GameUser.getUser(p);

        if(u.isInSetupMode()){
            if(DungeonRPG.SETUP_REGION > 0){
                Region region = Region.getRegion(DungeonRPG.SETUP_REGION);

                if(region != null){
                    p.sendMessage(ChatColor.RED + "Saving region #" + DungeonRPG.SETUP_REGION + ".");
                    region.saveData();

                    for(RegionLocation loc : region.getLocations()){
                        Location location = loc.toBukkitLocation();

                        location.getBlock().setType(Material.AIR);
                    }

                    DungeonRPG.SETUP_REGION = 0;
                    p.sendMessage(ChatColor.RED + "Region saved & unloaded.");
                } else {
                    p.sendMessage(ChatColor.RED + "An error occurred.");
                }
            } else {
                p.sendMessage(ChatColor.RED + "There is no region currently loaded. Use /loadregion to load a region.");
            }
        } else {
            p.sendMessage(ChatColor.RED + "You are not in setup mode.");
        }
    }
}
