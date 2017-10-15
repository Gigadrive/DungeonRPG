package net.wrathofdungeons.dungeonrpg.cmd;

import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.items.ItemData;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.mobs.MobData;
import net.wrathofdungeons.dungeonrpg.npc.CustomNPC;
import net.wrathofdungeons.dungeonrpg.regions.Region;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ConcurrentModificationException;

public class ReloadCommand extends Command {
    public ReloadCommand(){
        super("reload", Rank.ADMIN);
    }

    @Override
    public void execute(Player p, String label, String[] args) {
        GameUser u = GameUser.getUser(p);

        if(args.length == 1){
            String mode = args[0];

            if(mode.equalsIgnoreCase("mobs")){
                DungeonAPI.async(() -> {
                    p.sendMessage(ChatColor.GREEN + "Reloading mobs..");
                /*p.sendMessage(ChatColor.GREEN + "Despawning all current mobs..");

                try {
                    for(CustomEntity e : CustomEntity.STORAGE.values()) e.remove();
                } catch(ConcurrentModificationException e){}*/

                    p.sendMessage(ChatColor.GREEN + "Loading mob data from database..");

                    MobData.init();

                    p.sendMessage(ChatColor.GREEN + "Done!");
                });
            } else if(mode.equalsIgnoreCase("items")){
                DungeonAPI.async(() -> {
                    p.sendMessage(ChatColor.GREEN + "Reloading items..");

                    p.sendMessage(ChatColor.GREEN + "Loading item data from database..");

                    ItemData.init();

                    p.sendMessage(ChatColor.GREEN + "Reloading items for players..");

                    for(Player all : Bukkit.getOnlinePlayers()){
                        if(GameUser.isLoaded(all)){
                            GameUser.getUser(all).updateInventory();
                        }
                    }

                    p.sendMessage(ChatColor.GREEN + "Done!");
                });
            } else if(mode.equalsIgnoreCase("regions")){
                if(DungeonRPG.SETUP_REGION == 0){
                    DungeonAPI.async(() -> {
                        p.sendMessage(ChatColor.GREEN + "Reloading regions..");

                        p.sendMessage(ChatColor.GREEN + "Loading region data from database..");

                        Region.init();

                        p.sendMessage(ChatColor.GREEN + "Done!");
                    });
                } else {
                    p.sendMessage(ChatColor.RED + "There is currently a region loaded in setup mode. Please use /saveregion before reloading or all progress will be lost.");
                }
            } else if(mode.equalsIgnoreCase("npcs")){
                if(CustomNPC.getUnsavedData().size() == 0){
                    DungeonAPI.async(() -> {
                        p.sendMessage(ChatColor.GREEN + "Reloading NPCs..");

                        p.sendMessage(ChatColor.GREEN + "Loading NPC data from database..");

                        CustomNPC.init();

                        p.sendMessage(ChatColor.GREEN + "Done!");
                    });
                } else {
                    p.sendMessage(ChatColor.RED + "There is unsaved data left over. Use /savenpcs to save your unsaved data before reloading.");
                }
            } else if(mode.equalsIgnoreCase("broadcast")){
                DungeonAPI.async(() -> {
                    p.sendMessage(ChatColor.GREEN + "Reloading broadcast lines..");

                    p.sendMessage(ChatColor.GREEN + "Loading broadcast lines from database..");

                    DungeonRPG.reloadBroadcastLines();

                    p.sendMessage(ChatColor.GREEN + "Done!");
                });
            } else {
                p.sendMessage(ChatColor.RED + "/" + label + " <mobs|items|regions|npcs|broadcast>");
            }
        } else {
            p.sendMessage(ChatColor.RED + "/" + label + " <mobs|items|regions|npcs|broadcast>");
        }
    }
}
