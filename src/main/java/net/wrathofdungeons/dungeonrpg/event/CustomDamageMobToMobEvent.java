package net.wrathofdungeons.dungeonrpg.event;

import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CustomDamageMobToMobEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private CustomEntity cd;
    private CustomEntity c;
    private boolean isProjectile;

    public CustomDamageMobToMobEvent(CustomEntity cd, CustomEntity c, boolean isProjectile){
        this.cd = cd;
        this.c = c;
        this.isProjectile = isProjectile;
    }

    public CustomEntity getDamager() {
        return cd;
    }

    public CustomEntity getEntity() {
        return c;
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
