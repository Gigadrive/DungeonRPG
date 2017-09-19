package net.wrathofdungeons.dungeonrpg.util;

import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.ArrayList;

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

    public static ArrayList<Location> getParticleCircle(Location center){
        return getParticleCircle(center,4);
    }

    public static ArrayList<Location> getParticleCircle(Location center, double radius){
        return getParticleCircle(center,radius,15);
    }

    public static ArrayList<Location> getParticleCircle(Location center, double radius, int amount){
        World world = center.getWorld();
        double increment = (2 * Math.PI) / amount;
        ArrayList<Location> locations = new ArrayList<Location>();
        for(int i = 0;i < amount; i++) {
            double angle = i * increment;
            double x = center.getX() + (radius * Math.cos(angle));
            double z = center.getZ() + (radius * Math.sin(angle));
            locations.add(new Location(world, x, center.getY(), z));
        }

        return locations;
    }
}
