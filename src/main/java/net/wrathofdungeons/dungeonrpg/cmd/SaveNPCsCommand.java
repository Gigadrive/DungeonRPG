package net.wrathofdungeons.dungeonrpg.cmd;

import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.npc.CustomNPC;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class SaveNPCsCommand extends Command {
    public SaveNPCsCommand(){
        super("savenpcs", Rank.ADMIN);
    }

    @Override
    public void execute(Player p, String label, String[] args) {
        GameUser u = GameUser.getUser(p);

        if(!DungeonRPG.isTestServer()){
            p.sendMessage(ChatColor.RED + "That command is not available on this server.");
            return;
        }

        ArrayList<CustomNPC> toSave = CustomNPC.getUnsavedData();

        if(toSave.size() > 0){
            DungeonAPI.async(() -> {
                p.sendMessage(ChatColor.GREEN + "Saving data..");

                for(CustomNPC npc : toSave){
                    npc.saveData(false);
                }

                p.sendMessage(ChatColor.GREEN + "Done!");
            });
        } else {
            p.sendMessage(ChatColor.RED + "There is no data left to save.");
        }
    }
}
