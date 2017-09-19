package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonrpg.event.PlayerLandOnGroundEvent;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class LandOnGroundListener implements Listener {
    @EventHandler
    public void onLand(PlayerLandOnGroundEvent e){
        Player p = e.getPlayer();

        if(GameUser.isLoaded(p)){
            GameUser u = GameUser.getUser(p);

            if(u.getCurrentCharacter() != null){
                u.getSkillValues().leapIsInAir = false;
            }
        }
    }
}
