package net.wrathofdungeons.dungeonrpg.mobs;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.regions.Region;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.*;

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

                if(getData().isAdult()){
                    z.setBaby(false);
                } else {
                    z.setBaby(true);
                }
            } else if(bukkitEntity instanceof Villager){
                Villager v = (Villager)bukkitEntity;

                if(getData().isAdult()){
                    v.setAdult();
                } else {
                    v.setBaby();
                }
            } else if(bukkitEntity instanceof PigZombie){
                PigZombie p = (PigZombie)bukkitEntity;

                if(getData().isAdult()){
                    p.setBaby(false);
                } else {
                    p.setBaby(true);
                }
            } else if(bukkitEntity instanceof Pig){
                Pig p = (Pig)bukkitEntity;

                if(getData().isAdult()){
                    p.setAdult();
                } else {
                    p.setBaby();
                }
            } else if(bukkitEntity instanceof Sheep){
                Sheep s = (Sheep)bukkitEntity;

                if(getData().isAdult()){
                    s.setAdult();
                } else {
                    s.setBaby();
                }
            } else if(bukkitEntity instanceof Chicken){
                Chicken c = (Chicken)bukkitEntity;

                if(getData().isAdult()){
                    c.setAdult();
                } else {
                    c.setBaby();
                }
            } else if(bukkitEntity instanceof Ocelot){
                Ocelot o = (Ocelot)bukkitEntity;

                if(getData().isAdult()){
                    o.setAdult();
                } else {
                    o.setBaby();
                }
            } else if(bukkitEntity instanceof Creeper){
                Creeper c = (Creeper)bukkitEntity;
            } else if(bukkitEntity instanceof Skeleton){
                Skeleton s = (Skeleton)bukkitEntity;
            } else if(bukkitEntity instanceof Spider){
                Spider s = (Spider)bukkitEntity;
            } else if(bukkitEntity instanceof Slime){
                Slime s = (Slime)bukkitEntity;
            } else if(bukkitEntity instanceof Ghast){
                Ghast g = (Ghast)bukkitEntity;
            } else if(bukkitEntity instanceof Enderman){
                Enderman e = (Enderman)bukkitEntity;
            } else if(bukkitEntity instanceof CaveSpider){
                CaveSpider c = (CaveSpider)bukkitEntity;
            } else if(bukkitEntity instanceof Blaze){
                Blaze b = (Blaze)bukkitEntity;
            } else if(bukkitEntity instanceof MagmaCube){
                MagmaCube m = (MagmaCube)bukkitEntity;
            } else if(bukkitEntity instanceof Bat){
                Bat b = (Bat)bukkitEntity;
            } else if(bukkitEntity instanceof Witch){
                Witch w = (Witch)bukkitEntity;
            } else if(bukkitEntity instanceof Guardian){
                Guardian g = (Guardian)bukkitEntity;
            } else if(bukkitEntity instanceof Cow){
                Cow c = (Cow)bukkitEntity;

                if(getData().isAdult()){
                    c.setAdult();
                } else {
                    c.setBaby();
                }
            } else if(bukkitEntity instanceof Squid){
                Squid s = (Squid)bukkitEntity;
            } else if(bukkitEntity instanceof Wolf){
                Wolf w = (Wolf)bukkitEntity;

                if(getData().isAdult()){
                    w.setAdult();
                } else {
                    w.setBaby();
                }
            } else if(bukkitEntity instanceof MushroomCow){
                MushroomCow m = (MushroomCow)bukkitEntity;

                if(getData().isAdult()){
                    m.setAdult();
                } else {
                    m.setBaby();
                }
            } else if(bukkitEntity instanceof Horse){
                Horse h = (Horse)bukkitEntity;

                if(getData().isAdult()){
                    h.setAdult();
                } else {
                    h.setBaby();
                }
            } else if(bukkitEntity instanceof Rabbit){
                Rabbit r = (Rabbit)bukkitEntity;

                if(getData().isAdult()){
                    r.setAdult();
                } else {
                    r.setBaby();
                }
            }

            if(bukkitEntity.getEquipment() != null){
                bukkitEntity.getEquipment().clear();

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
