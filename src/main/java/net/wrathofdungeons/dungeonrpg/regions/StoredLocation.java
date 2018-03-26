package net.wrathofdungeons.dungeonrpg.regions;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class StoredLocation {
    public RegionLocationType type;
    public String world;
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;

    public Location toBukkitLocation(){
        return new Location(Bukkit.getWorld(world),x,y,z,yaw,pitch);
    }
}
