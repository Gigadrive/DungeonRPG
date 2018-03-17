package net.wrathofdungeons.dungeonrpg.listener;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.Duel;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.items.*;
import net.wrathofdungeons.dungeonrpg.items.awakening.AwakeningType;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.mobs.MobData;
import net.wrathofdungeons.dungeonrpg.mobs.MobType;
import net.wrathofdungeons.dungeonrpg.regions.Region;
import net.wrathofdungeons.dungeonrpg.regions.RegionLocation;
import net.wrathofdungeons.dungeonrpg.regions.RegionLocationType;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.util.WorldUtilities;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

public class DeathListener implements Listener {
    @EventHandler
    public void onEntityDeath(EntityDeathEvent e){
        e.setDroppedExp(0);
        if(e.getDrops() != null) e.getDrops().clear();
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e){
        Player p = e.getEntity();

        p.setHealth(p.getMaxHealth());

        if(GameUser.isLoaded(p)){
            GameUser u = GameUser.getUser(p);

            if(u.getCurrentCharacter() != null){
                if(!u.isDying()){
                    u.setDying(true);
                    u.setHP(u.getMaxHP());
                    u.setMP(u.getMaxMP());
                    u.setPoisonData(null);

                    for(PotionEffect pe : p.getActivePotionEffects()) p.removePotionEffect(pe.getType());

                    Location respawn = null;

                    Player killer = u.lastDamageSource != null && u.lastDamageSource instanceof Player && ((Player)u.lastDamageSource).isOnline() && ((Player)u.lastDamageSource).isValid() ? ((Player)u.lastDamageSource) : null;

                    if(Duel.isDueling(p)){
                        Duel d = Duel.getDuel(p);

                        if(d.isPlayer1(p)){
                            d.endGame(d.getPlayer2());
                        } else {
                            d.endGame(d.getPlayer1());
                        }
                    } else if(WorldUtilities.isPvPArena(p.getLocation())){
                        if(killer != null){
                            GameUser u2 = GameUser.getUser(killer);

                            if(u2.getCurrentCharacter() != null){
                                u2.getCurrentCharacter().getVariables().statisticsManager.playersDefeatedInArena++;
                                killer.sendMessage(ChatColor.GREEN + "You killed " + ChatColor.YELLOW + p.getName() + ChatColor.GREEN + "!");
                                p.sendMessage(ChatColor.RED + "You were killed by " + ChatColor.YELLOW + killer.getName() + ChatColor.RED + "!");
                            } else {
                                p.sendMessage(ChatColor.RED + "You died!");
                            }
                        } else {
                            p.sendMessage(ChatColor.RED + "You died!");
                        }

                        for(Region region : Region.getRegions(false)){
                            if(region.getLocations().size() > 0){
                                for(RegionLocation loc : region.getLocations(RegionLocationType.PVP_RESPAWN)){
                                    if(loc.world.equals(p.getWorld().getName())){
                                        Location bukkitLocation = loc.toBukkitLocation();

                                        if(respawn == null || p.getLocation().distance(bukkitLocation) < p.getLocation().distance(respawn)){
                                            respawn = bukkitLocation;
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        if(u.isInDungeon()){
                            respawn = DungeonRPG.getNearestTown(u.getCurrentDungeon().getType().getPortalEntranceLocation());
                            p.sendMessage(ChatColor.RED + "You died!");
                        } else {
                            respawn = DungeonRPG.getNearestTown(p);
                            p.sendMessage(ChatColor.RED + "You died!");
                        }
                    }

                    if(respawn != null) p.teleport(respawn);

                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            u.setDying(false);
                        }
                    }.runTaskLater(DungeonRPG.getInstance(),2*20);
                }
            }
        }
    }
}
