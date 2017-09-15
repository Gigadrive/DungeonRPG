package net.wrathofdungeons.dungeonrpg.util;

import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

public class WorldUtilities {
    public static void dropItem(Location loc, CustomItem item){
        dropItem(loc,item,null);
    }

    public static void dropItem(Location loc, CustomItem item, Player p){
        Item i = loc.getWorld().dropItem(loc,item.build(p));

        if(p != null){
            // TODO: Assign drop to player
        }
    }
}
