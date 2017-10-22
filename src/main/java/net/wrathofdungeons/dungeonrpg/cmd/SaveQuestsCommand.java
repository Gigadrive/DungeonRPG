package net.wrathofdungeons.dungeonrpg.cmd;

import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonrpg.npc.CustomNPC;
import net.wrathofdungeons.dungeonrpg.quests.Quest;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class SaveQuestsCommand extends Command {
    public SaveQuestsCommand(){
        super("savequests", Rank.ADMIN);
    }

    @Override
    public void execute(Player p, String label, String[] args) {
        GameUser u = GameUser.getUser(p);
        ArrayList<Quest> toSave = Quest.getUnsavedData();

        if(toSave.size() > 0){
            DungeonAPI.async(() -> {
                p.sendMessage(ChatColor.GREEN + "Saving data..");

                for(Quest q : toSave){
                    q.saveData(false);
                }

                p.sendMessage(ChatColor.GREEN + "Done!");
            });
        } else {
            p.sendMessage(ChatColor.RED + "There is no data left to save.");
        }
    }
}
