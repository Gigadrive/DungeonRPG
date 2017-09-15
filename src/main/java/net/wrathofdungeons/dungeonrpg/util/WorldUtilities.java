package net.wrathofdungeons.dungeonrpg.util;

import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

public class WorldUtilities {
    public static void dropItem(Location loc, CustomItem item){
        dropItem(loc,item,null);
    }

    public static void dropItem(Location loc, CustomItem item, Player p){
        Item i = loc.getWorld().dropItem(loc,item.build(p));

        if(p != null){
            addAssignmentData(i,p);
        }
    }

    public static void addAssignmentData(Item i, Player p){
        i.setMetadata("assignedPlayer", new FixedMetadataValue(DungeonRPG.getInstance(),p.getName()));
        i.setMetadata("dropTime", new FixedMetadataValue(DungeonRPG.getInstance(),System.currentTimeMillis()));
    }
}
