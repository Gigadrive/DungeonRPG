package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.regions.Region;
import net.wrathofdungeons.dungeonrpg.regions.RegionLocation;
import net.wrathofdungeons.dungeonrpg.regions.RegionLocationType;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class PlayerMoveListener implements Listener {
    @EventHandler
    public void onMove(PlayerMoveEvent e){
        Player p = e.getPlayer();
        boolean freeze = false;

        if(GameUser.isLoaded(p)){
            GameUser u = GameUser.getUser(p);

            if(u.getCurrentCharacter() != null){
                if(u.mayActivateMobs){
                    Location from = e.getFrom();
                    Location to = e.getTo();
                    double x = Math.floor(from.getX());
                    double z = Math.floor(from.getZ());

                    if(Math.floor(to.getX()) != x || Math.floor(to.getZ()) != z){
                        for(Region region : Region.STORAGE){
                            if(region.getMobData() != null && region.getMobLimit() > 0 && region.mayActivateMobs()){
                                if(region.isInRegion(p.getLocation())){
                                    region.startMobActivationTimer();

                                    for(int i = region.getEntitiesSpawned().size(); i < region.getMobLimit(); i++){
                                        ArrayList<RegionLocation> a = region.getLocations(RegionLocationType.MOB_LOCATION,region.getMobLimit());

                                        for(RegionLocation l : a){
                                            CustomEntity entity = new CustomEntity(region.getMobData());
                                            entity.setOriginRegion(region);
                                            entity.spawn(l.toBukkitLocation());
                                        }
                                    }

                                    break;
                                }
                            }
                        }

                        u.mayActivateMobs = false;

                        new BukkitRunnable(){
                            @Override
                            public void run() {
                                u.mayActivateMobs = true;
                            }
                        }.runTaskLater(DungeonRPG.getInstance(),5*20);
                    }
                }
            } else {
                if(!u.isInSetupMode()) freeze = true;
            }
        } else {
            freeze = true;
        }

        if(freeze){
            Location from = e.getFrom();
            Location to = e.getTo();
            double x = Math.floor(from.getX());
            double z = Math.floor(from.getZ());

            if(Math.floor(to.getX()) != x || Math.floor(to.getZ()) != z){
                x += .5;
                z += .5;
                e.getPlayer().teleport(new Location(from.getWorld(),x,from.getY(),z,from.getYaw(),from.getPitch()));
            }
        }
    }
}
