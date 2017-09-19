package net.wrathofdungeons.dungeonrpg.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SlimeSplitEvent;

public class SplitListener implements Listener {
    @EventHandler
    public void onSplit(SlimeSplitEvent e){
        e.setCancelled(true);
    }
}
