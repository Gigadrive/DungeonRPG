package net.wrathofdungeons.dungeonrpg.regions;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class RegionLocation {
    public int id;
    public RegionLocationType type;
    public String world;
    public double x;
    public double y;
    public double z;

    public Location toBukkitLocation(){
        return new Location(Bukkit.getWorld(world),x,y,z);
    }
}
