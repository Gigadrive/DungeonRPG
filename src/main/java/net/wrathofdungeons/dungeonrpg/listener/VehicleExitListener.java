package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;

public class VehicleExitListener implements Listener {
    @EventHandler
    public void onExit(VehicleExitEvent e){
        if(e.getExited() instanceof Player){
            Player p = (Player)e.getExited();

            if(GameUser.isLoaded(p)){
                GameUser u = GameUser.getUser(p);

                if(u.getCurrentCharacter() != null){
                    if(u.currentMountEntity == e.getVehicle()) u.resetMount(false);
                }
            }
        }
    }
}
