package net.wrathofdungeons.dungeonrpg.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import net.wrathofdungeons.dungeonrpg.user.Character;

public class CharacterCreationDoneEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Character character;
    private Player p;

    public CharacterCreationDoneEvent(Player p, Character c){
        this.character = c;
        this.p = p;
    }

    public Player getPlayer() {
        return p;
    }

    public Character getCharacter() {
        return character;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
