package net.wrathofdungeons.dungeonrpg.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class ExplodeListener implements Listener {
    @EventHandler
    public void onExplode(EntityExplodeEvent e){
        e.setYield(0f);
    }
}
