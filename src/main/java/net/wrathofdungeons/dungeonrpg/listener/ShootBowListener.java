package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;

public class ShootBowListener implements Listener {
    @EventHandler
    public void onShoot(EntityShootBowEvent e){
        if(e.getEntity() instanceof Player){
            Player p = (Player)e.getEntity();

            if(GameUser.isLoaded(p)){
                GameUser u = GameUser.getUser(p);

                u.updateArrows();
                p.updateInventory();
            }
        }
    }
}
