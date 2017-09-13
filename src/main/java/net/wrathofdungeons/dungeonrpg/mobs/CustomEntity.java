package net.wrathofdungeons.dungeonrpg.mobs;

import net.wrathofdungeons.dungeonrpg.regions.Region;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.HashMap;

public class CustomEntity {
    public static HashMap<Entity,CustomEntity> STORAGE = new HashMap<Entity,CustomEntity>();

    private int mobDataID;
    private int origin;
    private Entity bukkitEntity;

    public CustomEntity(MobData data){
        this.mobDataID = data.getId();
    }

    public Region getOriginRegion(){
        if(this.origin > 0){
            return Region.getRegion(origin);
        } else {
            return null;
        }
    }

    public void setOriginRegion(Region region){
        if(region == null){
            this.origin = 0;
        } else {
            this.origin = region.getID();
        }
    }

    public MobData getData(){
        return MobData.getData(this.mobDataID);
    }

    public Entity getBukkitEntity() {
        return bukkitEntity;
    }

    public void spawn(Location loc){
        if(bukkitEntity == null && !STORAGE.containsValue(this)){
            bukkitEntity = loc.getWorld().spawnEntity(loc,getData().getEntityType());

            STORAGE.put(bukkitEntity,this);
        }
    }

    public static CustomEntity fromEntity(Entity e){
        if(STORAGE.containsKey(e)){
            return STORAGE.get(e);
        } else {
            return null;
        }
    }
}
