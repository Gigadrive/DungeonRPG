package net.wrathofdungeons.dungeonrpg.cmd;

import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.MySQLManager;
import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.PreparedStatement;

public class SyncCommand extends Command {
    public SyncCommand(){
        super("sync", Rank.ADMIN);
    }

    private boolean sync = false;

    private void syncTable(Player p, String table){
        GameUser u = GameUser.getUser(p);

        if(!sync){
            sync = true;

            DungeonAPI.async(() -> {
                try {
                    for(Player a : Bukkit.getOnlinePlayers()) a.sendMessage(ChatColor.GRAY + "** " + ChatColor.YELLOW + p.getName() + " started a sync action (" + ChatColor.GREEN + table.toUpperCase() + ChatColor.YELLOW + ") " + ChatColor.GRAY + "**");

                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("TRUNCATE TABLE wrathofdungeons." + table);
                    ps.executeUpdate();
                    ps.close();

                    ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT wrathofdungeons." + table + " SELECT * FROM wrathofdungeons_test." + table);
                    ps.executeUpdate();
                    ps.close();

                    sync = false;
                    for(Player a : Bukkit.getOnlinePlayers()) a.sendMessage(ChatColor.GRAY + "** " + ChatColor.GREEN + p.getName() + "'s sync action finished! " + ChatColor.GRAY + "**");
                } catch(Exception e){
                    e.printStackTrace();
                    sync = false;
                    for(Player a : Bukkit.getOnlinePlayers()) a.sendMessage(ChatColor.GRAY + "** " + ChatColor.RED + p.getName() + "'s sync action failed! " + ChatColor.GRAY + "**");
                }
            });
        } else {
            p.sendMessage(ChatColor.RED + "There is already a sync running right now.");
        }
    }

    private void syncFile(Player p, String from, String to) throws Exception {
        File file = new File(to);
        if(file.exists() || file.isDirectory()) FileUtils.deleteDirectory(file);

        FileUtils.copyDirectory(new File(from),file);
    }

    @Override
    public void execute(Player p, String label, String[] args) {
        GameUser u = GameUser.getUser(p);

        if(!DungeonRPG.isTestServer()){
            p.sendMessage(ChatColor.RED + "That command is not available on this server.");
            return;
        }

        if(args.length == 1){
            if(args[0].equalsIgnoreCase("map")){
                if(!sync){
                    sync = true;

                    DungeonAPI.async(() -> {
                        try {
                            for(Player a : Bukkit.getOnlinePlayers()) a.sendMessage(ChatColor.GRAY + "** " + ChatColor.YELLOW + p.getName() + " started a sync action (" + ChatColor.GREEN + "MAP" + ChatColor.YELLOW + ") " + ChatColor.GRAY + "**");

                            /*syncFile(p,"/home/wod/wrapper/local/templates/Test/default/wod/","/home/wod/wrapper/local/templates/Game/default/wod/");
                            syncFile(p,"/home/wod/wrapper/local/templates/Test/default/Dungeons/","/home/wod/wrapper/local/templates/Game/default/Dungeons/");*/
                            String[] directories = new File("/home/wod/wrapper/local/templates/Test/default/").list(new FilenameFilter() {
                                @Override
                                public boolean accept(File current, String name) {
                                    return new File(current, name).isDirectory();
                                }
                            });

                            for(String directory : directories){
                                if(!directory.equalsIgnoreCase("plugins") && !directory.equalsIgnoreCase("CLOUD") && !directory.equalsIgnoreCase("logs")){
                                    syncFile(p,"/home/wod/wrapper/local/templates/Test/default/" + directory + "/","/home/wod/wrapper/local/templates/Game/default/" + directory + "/");
                                }
                            }

                            sync = false;
                            for(Player a : Bukkit.getOnlinePlayers()) a.sendMessage(ChatColor.GRAY + "** " + ChatColor.GREEN + p.getName() + "'s sync action finished! " + ChatColor.GRAY + "**");
                        } catch(Exception e){
                            e.printStackTrace();
                            sync = false;
                            for(Player a : Bukkit.getOnlinePlayers()) a.sendMessage(ChatColor.GRAY + "** " + ChatColor.RED + p.getName() + "'s sync action failed! " + ChatColor.GRAY + "**");
                        }
                    });
                } else {
                    p.sendMessage(ChatColor.RED + "There is already a sync running right now.");
                }
            } else if(args[0].equalsIgnoreCase("items")){
                syncTable(p,"items");
            } else if(args[0].equalsIgnoreCase("regions")){
                syncTable(p,"regions");
            } else if(args[0].equalsIgnoreCase("quests")){
                syncTable(p,"quests");
            } else if(args[0].equalsIgnoreCase("lootchests")){
                syncTable(p,"lootchests");
            } else if(args[0].equalsIgnoreCase("mobs")){
                syncTable(p,"mobs");
            } else if(args[0].equalsIgnoreCase("ores")){
                syncTable(p,"ores");
            } else if(args[0].equalsIgnoreCase("npcs")){
                syncTable(p,"npcs");
            } else if(args[0].equalsIgnoreCase("craftingRecipes")){
                syncTable(p,"craftingRecipes");
            } else if(args[0].equalsIgnoreCase("plugin")){
                if(!sync){
                    sync = true;

                    DungeonAPI.async(() -> {
                        try {
                            for(Player a : Bukkit.getOnlinePlayers()) a.sendMessage(ChatColor.GRAY + "** " + ChatColor.YELLOW + p.getName() + " started a sync action (" + ChatColor.GREEN + "PLUGIN" + ChatColor.YELLOW + ") " + ChatColor.GRAY + "**");

                            //syncFile(p,"/home/wod/wrapper/local/templates/Test/default/plugins/","/home/wod/wrapper/local/templates/Game/default/plugins/");
                            syncFile(p,"/home/wod/wrapper/local/templates/Test/default/plugins/","/home/wod/wrapper/local/templates/Game/default/plugins/");

                            sync = false;
                            for(Player a : Bukkit.getOnlinePlayers()) a.sendMessage(ChatColor.GRAY + "** " + ChatColor.GREEN + p.getName() + "'s sync action finished! " + ChatColor.GRAY + "**");
                        } catch(Exception e){
                            e.printStackTrace();
                            sync = false;
                            for(Player a : Bukkit.getOnlinePlayers()) a.sendMessage(ChatColor.GRAY + "** " + ChatColor.RED + p.getName() + "'s sync action failed! " + ChatColor.GRAY + "**");
                        }
                    });
                } else {
                    p.sendMessage(ChatColor.RED + "There is already a sync running right now.");
                }
            } else {
                p.sendMessage(ChatColor.RED + "/" + label + " <map|items|regions|quests|lootchests|mobs|ores|plugin|npcs>");
            }
        } else {
            p.sendMessage(ChatColor.RED + "/" + label + " <map|items|regions|quests|lootchests|mobs|ores|plugin|npcs>");
        }
    }
}
