package net.wrathofdungeons.dungeonrpg.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class FinalDataLoadedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player p;

    public FinalDataLoadedEvent(Player p){
        this.p = p;
    }

    public Player getPlayer() {
        return p;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
