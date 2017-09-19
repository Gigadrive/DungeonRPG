package net.wrathofdungeons.dungeonrpg.event;

import net.wrathofdungeons.dungeonrpg.npc.CustomNPC;
import net.wrathofdungeons.dungeonrpg.user.Character;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CustomNPCInteractEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player p;
    private CustomNPC npc;

    public CustomNPCInteractEvent(Player p, CustomNPC npc){
        this.p = p;
        this.npc = npc;
    }

    public Player getPlayer() {
        return p;
    }

    public CustomNPC getNPC() {
        return npc;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
