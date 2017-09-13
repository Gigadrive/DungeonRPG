package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.regions.Region;
import net.wrathofdungeons.dungeonrpg.regions.RegionLocation;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockListener implements Listener {
    @EventHandler
    public void onPlace(BlockPlaceEvent e){
        if(e.getPlayer().getGameMode() != GameMode.CREATIVE) e.setCancelled(true);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e){
        Player p = e.getPlayer();

        if(GameUser.isLoaded(p)){
            GameUser u = GameUser.getUser(p);

            if(u.isInSetupMode()){
                if(DungeonRPG.SETUP_REGION > 0){
                    Region region = Region.getRegion(DungeonRPG.SETUP_REGION);

                    if(region != null){
                        RegionLocation toRemove = null;

                        for(RegionLocation l : region.getLocations()){
                            if(Util.isLocationEqual(l.toBukkitLocation(),e.getBlock().getLocation())) toRemove = l;
                        }

                        if(toRemove != null){
                            p.sendMessage(ChatColor.GREEN + "Location removed.");
                            region.getLocations().remove(toRemove);
                        } else {
                            e.setCancelled(true);
                        }
                    } else {
                        e.setCancelled(true);
                    }
                } else {
                    e.setCancelled(true);
                }
            } else {
                e.setCancelled(true);
            }
        } else {
            e.setCancelled(true);
        }
    }
}
