package net.wrathofdungeons.dungeonrpg.event;

import net.wrathofdungeons.dungeonrpg.user.Character;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public class EntityStopTargetEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Entity entity;
    private Player p;

    public EntityStopTargetEvent(Entity entity){
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
