package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonapi.event.PlayerCoreDataLoadedEvent;
import net.wrathofdungeons.dungeonapi.user.User;
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
    }

    @EventHandler
    public void onDataLoaded(PlayerCoreDataLoadedEvent e){
        Player p = e.getPlayer();

        GameUser u = GameUser.TEMP.get(p);
        u.init(p);
    }
}
