package net.wrathofdungeons.dungeonrpg.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CustomDamagePlayerToPlayerEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player p;
    private Player p2;
    private boolean isProjectile;

    public CustomDamagePlayerToPlayerEvent(Player p, Player p2, boolean isProjectile){
        this.p = p;
        this.p2 = p2;
        this.isProjectile = isProjectile;
    }

    public Player getDamager() {
        return p;
    }

    public Player getEntity() {
        return p2;
    }

    public boolean isProjectile() {
        return isProjectile;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
