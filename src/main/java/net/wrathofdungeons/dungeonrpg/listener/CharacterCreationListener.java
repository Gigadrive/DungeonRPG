package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonrpg.event.CharacterCreationDoneEvent;
import net.wrathofdungeons.dungeonrpg.inv.CharacterSelectionMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CharacterCreationListener implements Listener {
    @EventHandler
    public void onDone(CharacterCreationDoneEvent e){
        Player p = e.getPlayer();

        if(CharacterSelectionMenu.CREATING.contains(p)){
            CharacterSelectionMenu.CREATING.remove(p);
            CharacterSelectionMenu.openSelection(p);
        }
    }
}
