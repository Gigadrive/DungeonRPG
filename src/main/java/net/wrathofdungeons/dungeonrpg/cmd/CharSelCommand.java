package net.wrathofdungeons.dungeonrpg.cmd;

import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.inv.CharacterSelectionMenu;
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

        if(u.getCurrentCharacter() != null){
            u.saveData();
            u.setCurrentCharacter(null);
            u.bukkitReset();
            p.teleport(DungeonRPG.getCharSelLocation());
            DungeonRPG.updateVanishing();

            CharacterSelectionMenu.openSelection(p);
        } else {
            p.sendMessage(ChatColor.RED + "You are already in the character selection screen.");
        }
    }
}
