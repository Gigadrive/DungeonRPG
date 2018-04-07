package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonapi.util.BountifulAPI;
import net.wrathofdungeons.dungeonrpg.Duel;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.items.ItemData;
import net.wrathofdungeons.dungeonrpg.regions.Region;
import net.wrathofdungeons.dungeonrpg.regions.RegionLocationType;
import net.wrathofdungeons.dungeonrpg.regions.StoredLocation;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.util.WorldUtilities;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
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
                                for (StoredLocation loc : region.getLocations(RegionLocationType.PVP_RESPAWN)) {
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
                        if (!u.hasInInventory(ItemData.getData(DungeonRPG.RESURRECT_SCROLL), 1)) {
                            Location loc;

                            if (u.isInDungeon())
                                loc = DungeonRPG.getNearestTown(u.getCurrentDungeon().getType().getPortalEntranceLocation());
                            else
                                loc = DungeonRPG.getNearestTown(p);

                            u.getCurrentCharacter().getVariables().saveLocation = new StoredLocation();
                            u.getCurrentCharacter().getVariables().saveLocation.world = loc.getWorld().getName();
                            u.getCurrentCharacter().getVariables().saveLocation.x = loc.getX();
                            u.getCurrentCharacter().getVariables().saveLocation.y = loc.getY();
                            u.getCurrentCharacter().getVariables().saveLocation.z = loc.getZ();
                            u.getCurrentCharacter().getVariables().saveLocation.yaw = loc.getYaw();
                            u.getCurrentCharacter().getVariables().saveLocation.pitch = loc.getPitch();

                            u.getCurrentCharacter().getVariables().lastDeathLocation = new StoredLocation();
                            u.getCurrentCharacter().getVariables().lastDeathLocation.world = p.getLocation().getWorld().getName();
                            u.getCurrentCharacter().getVariables().lastDeathLocation.x = p.getLocation().getX();
                            u.getCurrentCharacter().getVariables().lastDeathLocation.y = p.getLocation().getY();
                            u.getCurrentCharacter().getVariables().lastDeathLocation.z = p.getLocation().getZ();
                            u.getCurrentCharacter().getVariables().lastDeathLocation.yaw = p.getLocation().getYaw();
                            u.getCurrentCharacter().getVariables().lastDeathLocation.pitch = p.getLocation().getPitch();

                            u.respawnCountdownCount = 30;
                            u.spawnSoulLight(p.getLocation().clone().add(0.0, 2.0, 0.0), u.respawnCountdownCount);
                            u.removeHoloPlate();

                            p.setGameMode(GameMode.SPECTATOR);

                            u.respawnCountdown = new BukkitRunnable() {
                                @Override
                                public void run() {
                                    if (u.respawnCountdownCount > 0) {
                                        if (u.respawnCountdownCount == 1) {
                                            BountifulAPI.sendTitle(p, 0, 30, 0, ChatColor.DARK_RED.toString() + ChatColor.BOLD.toString() + "YOU DIED!", ChatColor.RED + "Respawn in: " + ChatColor.GRAY + String.valueOf(u.respawnCountdownCount) + " second");
                                        } else {
                                            BountifulAPI.sendTitle(p, 0, 30, 0, ChatColor.DARK_RED.toString() + ChatColor.BOLD.toString() + "YOU DIED!", ChatColor.RED + "Respawn in: " + ChatColor.GRAY + String.valueOf(u.respawnCountdownCount) + " seconds");
                                        }

                                        u.respawnCountdownCount--;
                                        u.updateSoulLight(u.respawnCountdownCount);
                                    } else {
                                        p.teleport(u.getCurrentCharacter().getVariables().saveLocation.toBukkitLocation());
                                        p.setGameMode(DungeonRPG.PLAYER_DEFAULT_GAMEMODE);
                                        u.removeSoulLight();
                                        u.updateHoloPlate();
                                        cancel();
                                    }
                                }
                            }.runTaskTimer(DungeonRPG.getInstance(), 20, 20);
                        } else {
                            u.removeFromInventory(ItemData.getData(DungeonRPG.RESURRECT_SCROLL), 1);
                            p.sendMessage(ChatColor.AQUA + "You have been resurrected!");
                        }
                    }

                    if(respawn != null) p.teleport(respawn);

                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            u.setDying(false);
                        }
                    }.runTaskLater(DungeonRPG.getInstance(), 20);
                }
            }
        }
    }
}
