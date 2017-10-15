package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonrpg.Duel;
import net.wrathofdungeons.dungeonrpg.Trade;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        Player p = e.getPlayer();

        e.setQuitMessage(null);
        Trade.clearRequests(p);
        Duel.clearRequests(p);

        if(GameUser.isLoaded(p)){
            GameUser u = GameUser.getUser(p);

            if(u.getParty() != null) u.getParty().leaveParty(p);
            u.stopMPRegenTask();
            u.stopHPRegenTask();
        }
    }
}
