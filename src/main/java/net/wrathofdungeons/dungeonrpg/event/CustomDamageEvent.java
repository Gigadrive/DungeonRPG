package net.wrathofdungeons.dungeonrpg.event;

import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.user.Character;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CustomDamageEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private CustomEntity entity;
    private Player p;
    private boolean isPlayerAttacking;
    private boolean isProjectile;

    public CustomDamageEvent(Player p, CustomEntity entity, boolean isPlayerAttacking, boolean isProjectile){
        this.entity = entity;
        this.p = p;
        this.isPlayerAttacking = isPlayerAttacking;
        this.isProjectile = isProjectile;
    }

    public Player getPlayer() {
        return p;
    }

    public CustomEntity getEntity() {
        return entity;
    }

    public boolean isPlayerAttacking() {
        return isPlayerAttacking;
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
