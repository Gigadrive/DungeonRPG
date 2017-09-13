package net.wrathofdungeons.dungeonrpg.mobs;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.regions.Region;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;

import java.util.HashMap;

public class CustomEntity {
    public static HashMap<LivingEntity,CustomEntity> STORAGE = new HashMap<LivingEntity,CustomEntity>();

    private int mobDataID;
    private int origin;
    private LivingEntity bukkitEntity;
    private Hologram hologram;

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

    public LivingEntity getBukkitEntity() {
        return bukkitEntity;
    }

    public Hologram getHologram() {
        return hologram;
    }

    public void setHologram(Hologram hologram) {
        this.hologram = hologram;
    }

    public Location getSupposedHologramLocation(){
        if(bukkitEntity != null){
            Location loc = bukkitEntity.getEyeLocation();

            return loc.clone().add(0,1,0);
        }

        return null;
    }

    public void spawn(Location loc){
        if(bukkitEntity == null && !STORAGE.containsValue(this)){
            bukkitEntity = (LivingEntity)loc.getWorld().spawnEntity(loc,getData().getEntityType());
            DungeonAPI.nmsMakeSilent(bukkitEntity);

            bukkitEntity.setNoDamageTicks(0);
            bukkitEntity.setRemoveWhenFarAway(false);

            if(bukkitEntity instanceof Zombie){
                Zombie z = (Zombie)bukkitEntity;
                z.setVillager(false);
            }

            if(bukkitEntity instanceof Ageable){
                if(getData().isAdult()){
                    ((Ageable)bukkitEntity).setAdult();
                } else {
                    ((Ageable)bukkitEntity).setBaby();
                }
            }

            if(bukkitEntity.getEquipment() != null){
                bukkitEntity.getEquipment().setItemInHand(getData().getWeapon());
                bukkitEntity.getEquipment().setHelmet(getData().getHelmet());
                bukkitEntity.getEquipment().setChestplate(getData().getChestplate());
                bukkitEntity.getEquipment().setLeggings(getData().getLeggings());
                bukkitEntity.getEquipment().setBoots(getData().getBoots());
            }

            hologram = HologramsAPI.createHologram(DungeonRPG.getInstance(),getSupposedHologramLocation());
            hologram.appendTextLine(getData().getMobType().getColor() + getData().getName() + " " + ChatColor.GOLD + "- Lv. " + getData().getLevel());

            STORAGE.put(bukkitEntity,this);
        }
    }

    public void remove(){
        if(bukkitEntity != null){
            STORAGE.remove(bukkitEntity);
            bukkitEntity.remove();
            bukkitEntity = null;
        }

        if(hologram != null){
            hologram.delete();
            hologram = null;
        }
    }

    public void updateHealthBar(){
        // TODO: Show health in name tag
    }

    public static CustomEntity fromEntity(LivingEntity e){
        if(STORAGE.containsKey(e)){
            return STORAGE.get(e);
        } else {
            return null;
        }
    }
}
