package net.wrathofdungeons.dungeonrpg.util;

import net.wrathofdungeons.dungeonapi.DungeonAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class SerializedBukkitLocation {
    public static SerializedBukkitLocation fromLocation(Location loc){
        return new SerializedBukkitLocation(loc);
    }

    public static SerializedBukkitLocation fromLocation(String world, double x, double y, double z){
        return new SerializedBukkitLocation(world,x,y,z);
    }

    public static SerializedBukkitLocation fromLocation(String world, double x, double y, double z, float yaw, float pitch){
        return new SerializedBukkitLocation(world,x,y,z,yaw,pitch);
    }

    public static SerializedBukkitLocation fromLocation(World world, double x, double y, double z){
        return new SerializedBukkitLocation(world.getName(),x,y,z);
    }

    public static SerializedBukkitLocation fromLocation(World world, double x, double y, double z, float yaw, float pitch){
        return new SerializedBukkitLocation(world.getName(),x,y,z,yaw,pitch);
    }

    public static SerializedBukkitLocation fromString(String json){
        return DungeonAPI.GSON.fromJson(json,SerializedBukkitLocation.class);
    }

    private String world;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public SerializedBukkitLocation(Location location){
        this.world = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    public SerializedBukkitLocation(String world, double x, double y, double z){
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public SerializedBukkitLocation(String world, double x, double y, double z, float yaw, float pitch){
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public String getWorld() {
        return world;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public void setWorld(World world) {
        this.world = world.getName();
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public Location toBukkitLocation(){
        World w = Bukkit.getWorld(world);

        return w != null ? new Location(w,x,y,z,yaw,pitch) : null;
    }

    @Override
    public String toString(){
        return DungeonAPI.GSON.toJson(this);
    }

}
