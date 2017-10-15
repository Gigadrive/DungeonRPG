package net.wrathofdungeons.dungeonrpg.cmd;

import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonrpg.Trade;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class CharSelCommand extends Command {
    public CharSelCommand(){
        super(new String[]{"charsel","character","selectcharacter","class","selectclass"});
    }

    @Override
    public void execute(Player p, String label, String[] args) {
        GameUser u = GameUser.getUser(p);

        if(!u.isInSetupMode()){
            if(u.getCurrentCharacter() != null){
                Trade.clearRequests(p);
                u.saveData(true);
            } else {
                p.sendMessage(ChatColor.RED + "You are already in the character selection screen.");
            }
        } else {
            p.sendMessage(ChatColor.RED + "Please leave setup mode first.");
        }
    }
}
