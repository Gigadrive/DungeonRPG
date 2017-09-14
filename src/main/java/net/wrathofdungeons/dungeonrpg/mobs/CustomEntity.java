package net.wrathofdungeons.dungeonrpg.mobs;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
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
    private TextLine healthLine;

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

            if(healthLine != null){
                return loc.clone().add(0,1.25,0);
            } else {
                return loc.clone().add(0,1,0);
            }
        }

        return null;
    }

    public void giveNormalKnockback(Location from){
        if(bukkitEntity != null){
            bukkitEntity.setVelocity(bukkitEntity.getLocation().toVector().subtract(from.toVector()).setY(-1).multiply(0.5));
        }
    }

    public void spawn(Location loc){
        if(bukkitEntity == null && !STORAGE.containsValue(this)){
            bukkitEntity = (LivingEntity)loc.getWorld().spawnEntity(loc,getData().getEntityType());
            DungeonAPI.nmsMakeSilent(bukkitEntity);

            bukkitEntity.setMaximumNoDamageTicks(0);
            bukkitEntity.setNoDamageTicks(0);
            bukkitEntity.setFireTicks(0);
            bukkitEntity.setRemoveWhenFarAway(true);
            bukkitEntity.setMaxHealth(getData().getHealth());
            bukkitEntity.setHealth(bukkitEntity.getMaxHealth());

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
            if(!hologram.isDeleted()) hologram.delete();
            hologram = null;
        }
    }

    private String healthBarText(){
        int div = ((Double)((bukkitEntity.getHealth()/bukkitEntity.getMaxHealth())*10)).intValue();

        String text = "";
        if(div == 10) text = ChatColor.DARK_RED + "[" + ChatColor.RED + "||||||||||" + ChatColor.DARK_RED + "]";
        if(div == 9) text = ChatColor.DARK_RED + "[" + ChatColor.RED + "|||||||||" + ChatColor.DARK_GRAY + "|" + ChatColor.DARK_RED + "]";
        if(div == 8) text = ChatColor.DARK_RED + "[" + ChatColor.RED + "||||||||" + ChatColor.DARK_GRAY + "||" + ChatColor.DARK_RED + "]";
        if(div == 7) text = ChatColor.DARK_RED + "[" + ChatColor.RED + "|||||||" + ChatColor.DARK_GRAY + "|||" + ChatColor.DARK_RED + "]";
        if(div == 6) text = ChatColor.DARK_RED + "[" + ChatColor.RED + "||||||" + ChatColor.DARK_GRAY + "||||" + ChatColor.DARK_RED + "]";
        if(div == 5) text = ChatColor.DARK_RED + "[" + ChatColor.RED + "|||||" + ChatColor.DARK_GRAY + "|||||" + ChatColor.DARK_RED + "]";
        if(div == 4) text = ChatColor.DARK_RED + "[" + ChatColor.RED + "||||" + ChatColor.DARK_GRAY + "||||||" + ChatColor.DARK_RED + "]";
        if(div == 3) text = ChatColor.DARK_RED + "[" + ChatColor.RED + "|||" + ChatColor.DARK_GRAY + "|||||||" + ChatColor.DARK_RED + "]";
        if(div == 2) text = ChatColor.DARK_RED + "[" + ChatColor.RED + "||" + ChatColor.DARK_GRAY + "||||||||" + ChatColor.DARK_RED + "]";
        if(div == 1) text = ChatColor.DARK_RED + "[" + ChatColor.RED + "|" + ChatColor.DARK_GRAY + "|||||||||" + ChatColor.DARK_RED + "]";
        if(div == 0) text = ChatColor.DARK_RED + "[" + ChatColor.DARK_GRAY + "||||||||||" + ChatColor.DARK_RED + "]";

        return text;
    }

    public void updateHealthBar(){
        if(getHologram() != null){
            if(getHologram().size() == 1){
                healthLine = getHologram().appendTextLine(healthBarText());
            } else {
                if(healthLine != null){
                    healthLine.setText(healthBarText());
                } else {
                    hologram.getLine(1).removeLine();
                    healthLine = hologram.appendTextLine(healthBarText());
                }
            }
        }
    }

    public static CustomEntity fromEntity(LivingEntity e){
        if(STORAGE.containsKey(e)){
            return STORAGE.get(e);
        } else {
            return null;
        }
    }
}
