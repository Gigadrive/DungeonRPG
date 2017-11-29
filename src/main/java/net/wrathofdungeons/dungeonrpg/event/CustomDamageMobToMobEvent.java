package net.wrathofdungeons.dungeonrpg.event;

import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.projectile.DungeonProjectile;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CustomDamageMobToMobEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private CustomEntity cd;
    private CustomEntity c;
    private DungeonProjectile projectile;

    public CustomDamageMobToMobEvent(CustomEntity cd, CustomEntity c, DungeonProjectile projectile){
        this.cd = cd;
        this.c = c;
        this.projectile = projectile;
    }

    public CustomEntity getDamager() {
        return cd;
    }

    public CustomEntity getEntity() {
        return c;
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
