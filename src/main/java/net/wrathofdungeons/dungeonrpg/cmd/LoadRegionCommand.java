package net.wrathofdungeons.dungeonrpg.cmd;

import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.regions.Region;
import net.wrathofdungeons.dungeonrpg.regions.RegionLocation;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.material.Wool;

public class LoadRegionCommand extends Command {
    public LoadRegionCommand(){
        super("loadregion", Rank.ADMIN);
    }

    @Override
    public void execute(Player p, String label, String[] args) {
        GameUser u = GameUser.getUser(p);

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
                            for(RegionLocation l : region.getLocations()){
                                Location loc = l.toBukkitLocation();
                                Block b = loc.getBlock();
                                b.setType(Material.WOOL);

                                switch(l.type){
                                    case MOB_LOCATION:
                                        b.setData((byte)5);
                                        break;
                                    case TOWN_LOCATION:
                                        b.setData((byte)3);
                                        break;
                                    case MOB_ACTIVATION_1:
                                        b.setData((byte)14);
                                        break;
                                    case MOB_ACTIVATION_2:
                                        b.setData((byte)13);
                                        break;
                                }

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
