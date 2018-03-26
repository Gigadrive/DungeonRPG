package net.wrathofdungeons.dungeonrpg.cmd;

import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.regions.Region;
import net.wrathofdungeons.dungeonrpg.regions.RegionLocationType;
import net.wrathofdungeons.dungeonrpg.regions.StoredLocation;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SetLocationCommand extends Command {
    public SetLocationCommand(){
        super("setlocation", Rank.ADMIN);
    }

    @Override
    public void execute(Player p, String label, String[] args) {
        GameUser u = GameUser.getUser(p);

        if(!DungeonRPG.isTestServer()){
            p.sendMessage(ChatColor.RED + "That command is not available on this server.");
            return;
        }

        if(u.isInSetupMode()){
            if(DungeonRPG.SETUP_REGION > 0){
                Region region = Region.getRegion(DungeonRPG.SETUP_REGION);

                if(region != null){
                    if(args.length == 1){
                        RegionLocationType type = RegionLocationType.fromName(args[0]);

                        if(type != null){
                            if(!region.hasLocation(p.getLocation())){
                                StoredLocation loc = new StoredLocation();
                                loc.world = p.getLocation().getWorld().getName();
                                loc.x = p.getLocation().getX();
                                loc.y = p.getLocation().getY();
                                loc.z = p.getLocation().getZ();
                                loc.yaw = p.getLocation().getYaw();
                                loc.pitch = p.getLocation().getPitch();
                                loc.type = type;

                                region.getLocations().add(loc);
                                DungeonRPG.setLocationIndicator(p.getLocation(),type);

                                p.sendMessage(ChatColor.GREEN + "Location added!");
                            } else {
                                p.sendMessage(ChatColor.RED + "This location has already been assigned!");
                            }
                        } else {
                            p.sendMessage(ChatColor.RED + "Unknown Region Location Type! Valid types are:");

                            for(RegionLocationType t : RegionLocationType.values()){
                                p.sendMessage(ChatColor.RED + "- " + t.toString());
                            }
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "/" + label + " <Location Type>");
                    }
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
