package net.wrathofdungeons.dungeonrpg.mobs;

import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import org.bukkit.ChatColor;

public enum MobType {
    PASSIVE(ChatColor.GREEN),
    NEUTRAL(ChatColor.YELLOW),
    AGGRO(ChatColor.RED),
    SUPPORTING(ChatColor.AQUA);

    private ChatColor color;

    MobType(ChatColor color){
        this.color = color;
    }

    public ChatColor getColor() {
        return color;
    }

    public boolean mayAttack(MobType t){
        return DungeonRPG.mayAttack(this,t);
    }

    public static MobType fromColor(ChatColor color){
        for(MobType t : values()){
            if(t.getColor().toString().equals(color.toString())) return t;
        }

        return null;
    }
}
