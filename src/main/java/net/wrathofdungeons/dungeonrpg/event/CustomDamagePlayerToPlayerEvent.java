package net.wrathofdungeons.dungeonrpg.event;

import net.wrathofdungeons.dungeonrpg.projectile.DungeonProjectile;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CustomDamagePlayerToPlayerEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player p;
    private Player p2;
    private DungeonProjectile projectile;

    public CustomDamagePlayerToPlayerEvent(Player p, Player p2, DungeonProjectile projectile){
        this.p = p;
        this.p2 = p2;
        this.projectile = projectile;
    }

    public Player getDamager() {
        return p;
    }

    public Player getEntity() {
        return p2;
    }

    public DungeonProjectile getProjectile() {
        return projectile;
    }

    public boolean isProjectile() {
        return projectile != null;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
