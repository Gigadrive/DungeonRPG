package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.regions.Region;
import net.wrathofdungeons.dungeonrpg.regions.RegionLocation;
import net.wrathofdungeons.dungeonrpg.regions.RegionLocationType;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;

public class PlayerMoveListener implements Listener {
    @EventHandler
    public void onMove(PlayerMoveEvent e){
        Player p = e.getPlayer();
        boolean freeze = false;

        if(GameUser.isLoaded(p)){
            GameUser u = GameUser.getUser(p);

            if(u.getCurrentCharacter() != null){
                u.updateHoloPlate();

                Location from = e.getFrom();
                Location to = e.getTo();
                double x = Math.floor(from.getX());
                double z = Math.floor(from.getZ());

                boolean newBlock = Math.floor(to.getX()) != x || Math.floor(to.getZ()) != z;

                if(newBlock) u.getCurrentCharacter().getVariables().statisticsManager.blocksWalked++;

                if(((Entity)p).isOnGround()) u.getSkillValues().leapIsInAir = false;

                if(u.mayActivateMobs){
                    if(newBlock){
                        for(Region region : Region.STORAGE){
                            if(region.getMobData() != null && region.getMobLimit() > 0 && region.mayActivateMobs() && region.isActive()){
                                if(region.isInRegion(p.getLocation())){
                                    region.startMobActivationTimer();

                                    if(region.getSpawnChance() > 0 && !Util.getChanceBoolean(1,region.getSpawnChance()))
                                        return;

                                    ArrayList<RegionLocation> a = region.getLocations(RegionLocationType.MOB_LOCATION,region.getMobLimit());
                                    Collections.shuffle(a);

                                    if(a.size() > 0){
                                        int j = 0;

                                        for(int i = region.getEntitiesSpawned().size(); i < region.getMobLimit(); i++){
                                            if(j >= a.size()) j = 0;
                                            RegionLocation l = a.get(j);

                                            CustomEntity entity = new CustomEntity(region.getMobData());
                                            entity.setOriginRegion(region);
                                            entity.spawn(l.toBukkitLocation());

                                            j++;
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
                        }.runTaskLater(DungeonRPG.getInstance(),2*20);
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

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e){
        Player p = e.getPlayer();

        if(GameUser.isLoaded(p)){
            GameUser u = GameUser.getUser(p);

            if(u.getCurrentCharacter() != null){
                if(e.isSneaking() && u.getSkillValues().stomperActive){
                    p.setVelocity(new Vector(0,-2.5,0));
                }
            }
        }
    }
}
