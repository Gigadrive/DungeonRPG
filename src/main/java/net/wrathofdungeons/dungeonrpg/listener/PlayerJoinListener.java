package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonapi.event.PlayerCoreDataLoadedEvent;
import net.wrathofdungeons.dungeonapi.user.User;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.event.FinalDataLoadedEvent;
import net.wrathofdungeons.dungeonrpg.inv.CharacterSelectionMenu;
import net.wrathofdungeons.dungeonrpg.user.Character;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        Player p = e.getPlayer();
        GameUser.load(p);
        e.setJoinMessage(null);
        DungeonRPG.updateVanishing();
        DungeonRPG.updateNames();
    }

    @EventHandler
    public void onCoreLoaded(PlayerCoreDataLoadedEvent e){
        Player p = e.getPlayer();

        GameUser u = GameUser.TEMP.get(p);
        u.init(p);
    }

    @EventHandler
    public void onLoaded(FinalDataLoadedEvent e){
        Player p = e.getPlayer();
        GameUser u = GameUser.getUser(p);

        DungeonRPG.updateVanishing();
        u.bukkitReset();

        Character character = null;
        for(Character c : u.getCharacters()){
            if(c.getVariables() != null && c.getVariables().autoJoin){
                character = c;
                break;
            }
        }

        if(u.getSettingsManager().autoJoin() && character != null){
            u.playCharacter(character);
        } else {
            p.teleport(DungeonRPG.getCharSelLocation());
            CharacterSelectionMenu.openSelection(p);
        }
    }
}
