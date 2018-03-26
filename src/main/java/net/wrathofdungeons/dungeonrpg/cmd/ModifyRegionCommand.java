package net.wrathofdungeons.dungeonrpg.cmd;

import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.dungeon.DungeonType;
import net.wrathofdungeons.dungeonrpg.mobs.MobData;
import net.wrathofdungeons.dungeonrpg.regions.Region;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ModifyRegionCommand extends Command {
    public ModifyRegionCommand(){
        super(new String[]{"modifyregion","region"}, Rank.ADMIN);
    }

    public void sendUsage(Player p, String label){
        p.sendMessage(ChatColor.RED + "/createregion");
        p.sendMessage(ChatColor.RED + "/" + label + " list");
        p.sendMessage(ChatColor.RED + "/" + label + " <ID> info");
        p.sendMessage(ChatColor.RED + "/" + label + " <ID> toggle");
        p.sendMessage(ChatColor.RED + "/" + label + " <ID> teleport");
        p.sendMessage(ChatColor.RED + "/" + label + " <ID> mob <Level>");
        p.sendMessage(ChatColor.RED + "/" + label + " <ID> moblimit <Stage Index>");
        p.sendMessage(ChatColor.RED + "/" + label + " <ID> cooldown <seconds>");
        p.sendMessage(ChatColor.RED + "/" + label + " <ID> spawnChance <Value>");
        p.sendMessage(ChatColor.RED + "/" + label + " <ID> dungeonType <Dungeon Type>");
        p.sendMessage(ChatColor.RED + "/" + label + " <ID> entranceTitleTop [Title]");
        p.sendMessage(ChatColor.RED + "/" + label + " <ID> entranceTitleBottom [Title]");
    }

    @Override
    public void execute(Player p, String label, String[] args) {
        GameUser u = GameUser.getUser(p);

        if(!DungeonRPG.isTestServer()){
            p.sendMessage(ChatColor.RED + "That command is not available on this server.");
            return;
        }

        if(u.isInSetupMode()){
            if(args.length == 1){
                if(args[0].equalsIgnoreCase("list")){
                    if(Region.getRegions().size() > 0){
                        for(Region region : Region.getRegions()){
                            if(region.getMobData() != null){
                                if(region.getLocations().size() == 1){
                                    p.sendMessage(ChatColor.YELLOW + "#" + region.getID() + ChatColor.GREEN + " - " + ChatColor.YELLOW + region.getMobLimit() + "x " + region.getMobData().getName() + ChatColor.GREEN + " - " + ChatColor.YELLOW + region.getLocations().size() + " location");
                                } else {
                                    p.sendMessage(ChatColor.YELLOW + "#" + region.getID() + ChatColor.GREEN + " - " + ChatColor.YELLOW + region.getMobLimit() + "x " + region.getMobData().getName() + ChatColor.GREEN + " - " + ChatColor.YELLOW + region.getLocations().size() + " locations");
                                }
                            } else {
                                if(region.getLocations().size() == 1){
                                    p.sendMessage(ChatColor.YELLOW + "#" + region.getID() + ChatColor.GREEN + " - " + ChatColor.YELLOW + region.getLocations().size() + " location");
                                } else {
                                    p.sendMessage(ChatColor.YELLOW + "#" + region.getID() + ChatColor.GREEN + " - " + ChatColor.YELLOW + region.getLocations().size() + " locations");
                                }
                            }
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "There are no regions to be listed.");
                    }
                } else {
                    sendUsage(p,label);
                }
            } else if(args.length == 2){
                if(Util.isValidInteger(args[0])){
                    Region region = Region.getRegion(Integer.parseInt(args[0]),true);

                    if(region != null){
                        if(args[1].equalsIgnoreCase("info")){
                            p.sendMessage(ChatColor.YELLOW + "Region Info: " + ChatColor.GREEN + "#" + region.getID());
                            p.sendMessage(ChatColor.YELLOW + "Locations: " + ChatColor.GREEN + region.getLocations().size());

                            if(region.getMobData() != null){
                                p.sendMessage(ChatColor.YELLOW + "Mob Data: " + ChatColor.GREEN + region.getMobData().getName());
                                p.sendMessage(ChatColor.YELLOW + "Mob Spawn Limit: " + ChatColor.GREEN + region.getMobLimit());
                                p.sendMessage(ChatColor.YELLOW + "Cooldown: " + ChatColor.GREEN + region.getCooldown() + " seconds");
                                p.sendMessage(ChatColor.YELLOW + "Spawn Chance: " + ChatColor.GREEN + region.getSpawnChance());
                            }
                        } else if(args[1].equalsIgnoreCase("toggle")){
                            if(region.isActive()){
                                region.setActive(false);
                                p.sendMessage(ChatColor.GREEN + "Mobs from this region will no longer spawn.");

                                DungeonAPI.async(() -> region.saveData());
                            } else {
                                region.setActive(true);
                                p.sendMessage(ChatColor.GREEN + "Mobs from this region will now spawn again.");

                                DungeonAPI.async(() -> region.saveData());
                            }
                        } else if(args[1].equalsIgnoreCase("teleport")){
                            if(region.getLocations().size() > 0){
                                p.teleport(region.getRandomizedLocations().get(0).toBukkitLocation());
                                p.sendMessage(ChatColor.GREEN + "Teleporting to a random location..");
                            } else {
                                p.sendMessage(ChatColor.RED + "That region doesn't have any location to teleport to.");
                            }
                        } else if(args[1].equalsIgnoreCase("entranceTitleTop")){
                            region.setEntranceTitleTop(null);
                            DungeonAPI.async(() -> region.saveData());

                            p.sendMessage(ChatColor.GREEN + "Top of entrance title has been reset.");
                        } else if(args[1].equalsIgnoreCase("entranceTitleBottom")){
                            region.setEntranceTitleBottom(null);
                            DungeonAPI.async(() -> region.saveData());

                            p.sendMessage(ChatColor.GREEN + "Bottom of entrance title has been reset.");
                        } else {
                            sendUsage(p,label);
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "Please enter a valid region ID.");
                    }
                } else {
                    p.sendMessage(ChatColor.RED + "Please enter a valid region ID.");
                }
            } else if(args.length == 3){
                if(Util.isValidInteger(args[0])){
                    Region region = Region.getRegion(Integer.parseInt(args[0]),true);

                    if(region != null){
                        if(args[1].equalsIgnoreCase("mob")){
                            if(Util.isValidInteger(args[2])){
                                MobData mobData = MobData.getData(Integer.parseInt(args[2]));

                                if(mobData != null){
                                    region.setMobData(mobData);
                                    p.sendMessage(ChatColor.GREEN + "Mob Data set to: " + mobData.getName());

                                    DungeonAPI.async(() -> region.saveData());
                                } else {
                                    p.sendMessage(ChatColor.RED + "Please enter a valid mob ID.");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "Please enter a valid mob ID.");
                            }
                        } else if(args[1].equalsIgnoreCase("moblimit")){
                            if(Util.isValidInteger(args[2])){
                                int limit = Integer.parseInt(args[2]);

                                if(limit >= 0){
                                    region.setMobLimit(limit);
                                    p.sendMessage(ChatColor.GREEN + "Mob Limit set to: " + limit);

                                    DungeonAPI.async(() -> region.saveData());
                                } else {
                                    p.sendMessage(ChatColor.RED + "Please enter a valid mob limit (must be at least 0).");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "Please enter a valid mob limit (must be at least 0).");
                            }
                        } else if(args[1].equalsIgnoreCase("cooldown")){
                            if(Util.isValidInteger(args[2])){
                                int cooldown = Integer.parseInt(args[2]);

                                if(cooldown >= 0){
                                    region.setCooldown(cooldown);
                                    p.sendMessage(ChatColor.GREEN + "Cooldown set to: " + cooldown);

                                    DungeonAPI.async(() -> region.saveData());
                                } else {
                                    p.sendMessage(ChatColor.RED + "Please enter a valid cooldown (must be at least 0).");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "Please enter a valid cooldown (must be at least 0).");
                            }
                        } else if(args[1].equalsIgnoreCase("spawnChance")){
                            if(Util.isValidInteger(args[2])){
                                int spawnChance = Integer.parseInt(args[2]);

                                if(spawnChance >= 0){
                                    region.setSpawnChance(spawnChance);
                                    p.sendMessage(ChatColor.GREEN + "Spawn chance set to: " + spawnChance);

                                    DungeonAPI.async(() -> region.saveData());
                                } else {
                                    p.sendMessage(ChatColor.RED + "Please enter a valid spawn chance (must be at least 0).");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "Please enter a valid spawn chance (must be at least 0).");
                            }
                        } else if(args[1].equalsIgnoreCase("dungeonType")){
                            DungeonType type = null;

                            for(DungeonType d : DungeonType.values()) if(d.name().equalsIgnoreCase(args[2])) type = d;

                            if(type != null){
                                region.getAdditionalData().dungeonType = type;
                                p.sendMessage(ChatColor.GREEN + "Success!");
                                region.saveData();
                            } else {
                                p.sendMessage(ChatColor.RED + "Invalid Dungeon type!");
                            }
                        } else if(args[1].equalsIgnoreCase("boss")){
                            if(Util.isValidInteger(args[2])){
                                region.getAdditionalData().isBoss = Util.convertIntegerToBoolean(Integer.parseInt(args[2]));
                                region.saveData();
                                p.sendMessage(ChatColor.GREEN + "Success!");
                            } else {
                                p.sendMessage(ChatColor.RED + "Please enter a integer.");
                            }
                        } else if(args[1].equalsIgnoreCase("entranceTitleTop")){
                            StringBuilder sb = new StringBuilder();
                            for (int i = 2; i < args.length; i++) {
                                sb.append(args[i]).append(" ");
                            }
                            String message = sb.toString().trim();

                            region.setEntranceTitleTop(message);
                            DungeonAPI.async(() -> region.saveData());

                            p.sendMessage(ChatColor.GREEN + "Top of entrance title has been set to " + message + ".");
                        } else if(args[1].equalsIgnoreCase("entranceTitleBottom")){
                            StringBuilder sb = new StringBuilder();
                            for (int i = 2; i < args.length; i++) {
                                sb.append(args[i]).append(" ");
                            }
                            String message = sb.toString().trim();

                            region.setEntranceTitleBottom(message);
                            DungeonAPI.async(() -> region.saveData());

                            p.sendMessage(ChatColor.GREEN + "Bottom of entrance title has been set to " + message + ".");
                        } else {
                            sendUsage(p,label);
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "Please enter a valid region ID.");
                    }
                } else {
                    p.sendMessage(ChatColor.RED + "Please enter a valid region ID.");
                }
            } else if(args.length > 3) {
                if(Util.isValidInteger(args[0])){
                    Region region = Region.getRegion(Integer.parseInt(args[0]),true);

                    if(region != null){
                        if(args[1].equalsIgnoreCase("entranceTitleTop")){
                            StringBuilder sb = new StringBuilder();
                            for (int i = 2; i < args.length; i++) {
                                sb.append(args[i]).append(" ");
                            }
                            String message = sb.toString().trim();

                            region.setEntranceTitleTop(message);
                            DungeonAPI.async(() -> region.saveData());

                            p.sendMessage(ChatColor.GREEN + "Top of entrance title has been set to " + message + ".");
                        } else if(args[1].equalsIgnoreCase("entranceTitleBottom")){
                            StringBuilder sb = new StringBuilder();
                            for (int i = 2; i < args.length; i++) {
                                sb.append(args[i]).append(" ");
                            }
                            String message = sb.toString().trim();

                            region.setEntranceTitleBottom(message);
                            DungeonAPI.async(() -> region.saveData());

                            p.sendMessage(ChatColor.GREEN + "Bottom of entrance title has been set to " + message + ".");
                        } else {
                            sendUsage(p,label);
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "Please enter a valid region ID.");
                    }
                } else {
                    p.sendMessage(ChatColor.RED + "Please enter a valid region ID.");
                }
            } else {
                sendUsage(p,label);
            }
        } else {
            p.sendMessage(ChatColor.RED + "You are not in setup mode.");
        }
    }
}
