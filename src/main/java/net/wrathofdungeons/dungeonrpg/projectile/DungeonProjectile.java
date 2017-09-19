package net.wrathofdungeons.dungeonrpg.projectile;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class DungeonProjectile {
    private Player p;
    private DungeonProjectileType type;
    private Location firedFrom;
    private int range;
    private double damage;
    private boolean isSkill;
    private double force;
    private Entity entity;

    public DungeonProjectile(Player p, DungeonProjectileType type, Location firedFrom, int range, double damage, boolean isSkill) {
        this.p = p;
        this.type = type;
        this.firedFrom = firedFrom;
        this.range = range;
        this.damage = damage;
        this.isSkill = isSkill;
        this.force = 1.0;
    }

    public DungeonProjectile(Player p, DungeonProjectileType type, Location firedFrom, int range, double damage, boolean isSkill, double force) {
        this.p = p;
        this.type = type;
        this.firedFrom = firedFrom;
        this.range = range;
        this.damage = damage;
        this.isSkill = isSkill;
        this.force = force;
    }

    public Player getPlayer(){
        return this.p;
    }

    public DungeonProjectileType getType(){
        return this.type;
    }

    public Location getFiredFrom(){
        return this.firedFrom;
    }

    public int getRange(){
        return this.range;
    }

    public double getDamage(){
        return this.damage;
    }

    public boolean isSkill(){
        return this.isSkill;
    }

    public double getForce(){
        return this.force;
    }

    public void setEntity(Entity e){
        this.entity = e;
    }

    public Entity getEntity(){
        return entity;
    }
}
