package net.wrathofdungeons.dungeonrpg.mobs;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.ai.GoalController;
import net.citizensnpcs.api.ai.goals.WanderGoal;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.entity.EntityHumanNPC;
import net.citizensnpcs.npc.skin.Skin;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.minecraft.server.v1_8_R3.*;
import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.util.ParticleEffect;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.mobs.handler.TargetHandler;
import net.wrathofdungeons.dungeonrpg.mobs.nms.DungeonZombie;
import net.wrathofdungeons.dungeonrpg.mobs.nms.ZombieArcher;
import net.wrathofdungeons.dungeonrpg.mobs.skills.MobSkill;
import net.wrathofdungeons.dungeonrpg.regions.Region;
import net.wrathofdungeons.dungeonrpg.util.AttributeOperation;
import net.wrathofdungeons.dungeonrpg.util.WorldUtilities;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import static net.citizensnpcs.api.npc.NPC.*;
import static net.citizensnpcs.npc.skin.Skin.CACHED_SKIN_UUID_METADATA;
import static net.citizensnpcs.npc.skin.Skin.CACHED_SKIN_UUID_NAME_METADATA;
import static net.citizensnpcs.npc.skin.Skin.get;

public class CustomEntity {
    public static HashMap<LivingEntity,CustomEntity> STORAGE = new HashMap<LivingEntity,CustomEntity>();

    private int mobDataID;
    private int origin;
    private LivingEntity bukkitEntity;
    private NPC npc;

    private BukkitTask regenTask;

    private Hologram hologram;
    private TextLine healthLine;

    private static final UUID maxHealthUID = UUID.fromString("f8b0a945-2d6a-4bdb-9a6f-59c285bf1e5d");
    private static final UUID followRangeUID = UUID.fromString("1737400d-3c18-41ba-8314-49a158481e1e");
    private static final UUID knockbackResistanceUID = UUID.fromString("8742c557-fcd5-4079-a462-b58db99b0f2c");
    private static final UUID movementSpeedUID = UUID.fromString("206a89dc-ae78-4c4d-b42c-3b31db3f5a7c");
    private static final UUID attackDamageUID = UUID.fromString("7bbe3bb1-079d-4150-ac6f-669e71550776");

    private AttributeModifier speedModifier;
    public boolean damaged = false;
    public boolean playerMobSpeed = true;

    private WanderGoal wanderGoal;

    private ArrayList<BukkitTask> tasks;

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
        if(getData().getEntityType() == EntityType.PLAYER){
            return npc != null && npc.getEntity() instanceof LivingEntity ? (LivingEntity)npc.getEntity() : null;
        } else {
            return bukkitEntity;
        }
    }

    public NPC toCitizensNPC(){
        return npc;
    }

    public Hologram getHologram() {
        return hologram;
    }

    public void setHologram(Hologram hologram) {
        this.hologram = hologram;
    }

    public BukkitTask getRegenTask() {
        return regenTask;
    }

    public void startRegenTask(){
        if(getData().getRegen() > 0 && regenTask == null){
            regenTask = new BukkitRunnable(){
                @Override
                public void run() {
                    if(bukkitEntity != null && bukkitEntity.isValid() && !bukkitEntity.isDead()){
                        double h = bukkitEntity.getHealth();

                        h += getData().getRegen();
                        if(h > bukkitEntity.getMaxHealth()) h = bukkitEntity.getMaxHealth();

                        bukkitEntity.setHealth(h);
                        if(damaged) updateHealthBar();
                    }
                }
            }.runTaskTimer(DungeonRPG.getInstance(),10*20,10*20);
        }
    }

    public void stopRegenTask(){
        if(regenTask != null){
            regenTask.cancel();
            regenTask = null;
        }
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

    public void giveNormalKnockback(Location from) {
        giveNormalKnockback(from, false);
    }

    public void giveNormalKnockback(Location from, boolean closerLocation){
        if(bukkitEntity != null){
            if(closerLocation){
                double distance = from.distance(bukkitEntity.getLocation());
                if(distance <= 1){
                    giveNormalKnockback(from,false);
                    return;
                }

                BlockIterator blocksToAdd = new BlockIterator(from, 0D, ((Double)distance).intValue());
                Location lastLoc = null;
                while (blocksToAdd.hasNext()) {
                    lastLoc = blocksToAdd.next().getLocation();
                }

                Location f = from;
                Location fl = from;
                Location finalLoc = from;

                int c = (int) Math.ceil(finalLoc.distance(lastLoc) / 2F) - 1;
                if (c > 0) {
                    Vector v = lastLoc.toVector().subtract(finalLoc.toVector()).normalize().multiply(2F);
                    Location l = finalLoc.clone();
                    for (int i = 0; i < c; i++) {
                        l.add(v);
                        f = fl;
                        fl = finalLoc;
                        finalLoc = l;
                    }
                }

                finalLoc = f;

                giveNormalKnockback(finalLoc,false);
            } else {
                bukkitEntity.setVelocity(bukkitEntity.getLocation().toVector().subtract(from.toVector()).setY(-1).multiply(0.5));
            }
        }
    }

    public void setBukkitEntity(LivingEntity bukkitEntity) {
        if(this.bukkitEntity != null) if(STORAGE.containsKey(this.bukkitEntity)) STORAGE.remove(this.bukkitEntity);

        this.bukkitEntity = bukkitEntity;
        STORAGE.put(bukkitEntity,this);
    }

    public void adjustSpeed(){
        adjustSpeed(true);
    }

    public void adjustSpeed(boolean removeSpeed){
        if(getBukkitEntity() != null){
            if(removeSpeed) removeSpeedAttribute();

            if(getData().getEntityType() == EntityType.PLAYER){
                ((Player)getBukkitEntity()).setWalkSpeed((float)getData().getSpeed());
            } else {
                EntityInsentient nmsEntity = (EntityInsentient) ((CraftLivingEntity)bukkitEntity).getHandle();

                AttributeInstance attributes = nmsEntity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED);

                double toAdd = getData().getSpeed()-getCurrentSpeedValue();

                speedModifier = new AttributeModifier(movementSpeedUID,"wod movement speed",toAdd, AttributeOperation.ADD);

                attributes.b(speedModifier);
            }
        }
    }

    public double getCurrentSpeedValue(){
        if(bukkitEntity != null){
            EntityInsentient nmsEntity = (EntityInsentient) ((CraftLivingEntity)bukkitEntity).getHandle();

            if(nmsEntity != null){
                AttributeInstance attributes = nmsEntity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED);

                if(attributes != null){
                    return attributes.getValue();
                } else {
                    return 0;
                }
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    @Deprecated
    public void removeSpeedAttribute(){
        if(getBukkitEntity() != null && speedModifier != null && getData().getEntityType() != EntityType.PLAYER){
            EntityInsentient nmsEntity = (EntityInsentient) ((CraftLivingEntity)bukkitEntity).getHandle();

            AttributeInstance attributes = nmsEntity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED);

            attributes.c(speedModifier);

            speedModifier = null;
        }
    }

    @Deprecated
    public void c(){
        EntityInsentient nmsEntity = (EntityInsentient) ((CraftLivingEntity)bukkitEntity).getHandle();

        AttributeInstance attributes = nmsEntity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED);
        speedModifier = new AttributeModifier(movementSpeedUID,"wod movement speed",-1, AttributeOperation.MULTIPLY_TOTAL);

        attributes.c(speedModifier);
        attributes.b(speedModifier);
    }

    public void setCancelMovement(boolean b){
        if(b){
            c();
        } else {
            removeSpeedAttribute();
        }
    }

    public WanderGoal getWanderGoal(){
        /*if(toCitizensNPC() != null){
            NPC npc = toCitizensNPC();

            if(npc.getDefaultGoalController() != null){
                while(npc.getDefaultGoalController().iterator().hasNext()){
                    GoalController.GoalEntry g = npc.getDefaultGoalController().iterator().next();

                    if(g.getGoal() instanceof WanderGoal){
                        return (WanderGoal)g.getGoal();
                    }
                }
            }
        }

        return null;*/
        return wanderGoal;
    }

    public boolean hasWanderGoal(){
        return getWanderGoal() != null;
    }

    public void addWanderGoal(){
        if(!hasWanderGoal()){
            if(toCitizensNPC() != null){
                wanderGoal = WanderGoal.createWithNPCAndRange(npc,5,5);
                toCitizensNPC().getDefaultGoalController().addGoal(wanderGoal,9);
            }
        }
    }

    public void removeWanderGoal(){
        if(hasWanderGoal()){
            if(toCitizensNPC() != null){
                toCitizensNPC().getDefaultGoalController().removeGoal(getWanderGoal());
            }
        }
    }

    public void spawn(Location loc){
        if(bukkitEntity == null && !STORAGE.containsValue(this) && npc == null){
            if(getData().getEntityType() == EntityType.PLAYER){
                DungeonRPG.IGNORE_SPAWN_NPC.add(npc);
                npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER,ChatColor.GREEN.toString());
                npc.spawn(loc);

                WorldUtilities.applySkinToNPC(npc,getData().getSkin(),getData().getSkinName());

                npc.setProtected(false);
                npc.getNavigator().getDefaultParameters().baseSpeed((float)getData().getSpeed()).useNewPathfinder();
                if(getData().getAiSettings().mayDoRandomStroll()/* && getData().getMobType() == MobType.PASSIVE*/) addWanderGoal();

                /*CitizensNPC cnpc = (CitizensNPC)npc;
                EntityHumanNPC.PlayerNPC np = (EntityHumanNPC.PlayerNPC) cnpc.getEntity();
                GameProfile originalGameProfile = np.getProfile();
                PropertyMap pm = originalGameProfile.getProperties();

                String p = "https://textures.minecraft.net/texture/";
                String url = getData().getSkin().startsWith(p) ? getData().getSkin() : p + getData().getSkin();

                if(pm == null){
                    throw new IllegalArgumentException("Profile doesn't contain a property map");
                }
                byte[] encoded =  Base64Coder.encodeString("{textures:{SKIN:{url:\"" + url + "\"}}}").getBytes();
                pm.put("textures", new Property("textures", new String(encoded)));

                if(url != null) npc.data().set(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA,url);*/

                new BukkitRunnable(){
                    public void run() {
                        DungeonRPG.IGNORE_SPAWN_NPC.remove(npc);
                    }
                }.runTaskLater(DungeonRPG.getInstance(),5);

                handle();
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        handle();
                    }
                }.runTaskLater(DungeonRPG.getInstance(),1);
            } else {
                if(getData().getAiSettings().getType() == MobAIType.MELEE){
                    if(getData().getEntityType() == EntityType.ZOMBIE){
                        World mcWorld = ((CraftWorld)loc.getWorld()).getHandle();
                        DungeonZombie dungeonZombie = new DungeonZombie(mcWorld);
                        dungeonZombie.setPosition(loc.getX(),loc.getY(),loc.getZ());
                        mcWorld.addEntity(dungeonZombie, CreatureSpawnEvent.SpawnReason.CUSTOM);

                        bukkitEntity = (Zombie)dungeonZombie.getBukkitEntity();
                    } else {
                        bukkitEntity = (LivingEntity)loc.getWorld().spawnEntity(loc,getData().getEntityType());
                    }
                } else if(getData().getAiSettings().getType() == MobAIType.RANGED){
                    if(getData().getEntityType() == EntityType.ZOMBIE){
                        World mcWorld = ((CraftWorld)loc.getWorld()).getHandle();
                        ZombieArcher zombieArcher = new ZombieArcher(mcWorld);
                        zombieArcher.setPosition(loc.getX(),loc.getY(),loc.getZ());
                        mcWorld.addEntity(zombieArcher, CreatureSpawnEvent.SpawnReason.CUSTOM);

                        bukkitEntity = (Zombie)zombieArcher.getBukkitEntity();
                    }
                }

                handle();
            }
        }
    }

    private void handle(){
        if(npc != null){
            bukkitEntity = (LivingEntity)npc.getEntity();
            if(bukkitEntity == null){
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        handle();
                    }
                }.runTaskLater(DungeonRPG.getInstance(),1);
                return;
            }
        } else {
            DungeonAPI.nmsMakeSilent(bukkitEntity);
        }

        bukkitEntity.setMaximumNoDamageTicks(0);
        bukkitEntity.setNoDamageTicks(0);
        bukkitEntity.setFireTicks(0);
        bukkitEntity.setRemoveWhenFarAway(false);
        bukkitEntity.setMaxHealth(getData().getHealth());
        bukkitEntity.setHealth(bukkitEntity.getMaxHealth());

        if(bukkitEntity instanceof Zombie){
            bukkitEntity.getEquipment().setHelmet(new ItemStack(Material.APPLE));

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
            bukkitEntity.getEquipment().setHelmet(new ItemStack(Material.APPLE));
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

        TargetHandler.giveTargets(bukkitEntity,this);
        adjustSpeed(false);

        new BukkitRunnable(){
            @Override
            public void run() {
                if(getData().getEntityType() == EntityType.PLAYER){
                    if(npc != null){
                        Equipment equipment = npc.getTrait(Equipment.class);
                        equipment.set(Equipment.EquipmentSlot.HAND,getData().getWeapon());
                        equipment.set(Equipment.EquipmentSlot.HELMET,getData().getHelmet());
                        equipment.set(Equipment.EquipmentSlot.CHESTPLATE,getData().getChestplate());
                        equipment.set(Equipment.EquipmentSlot.LEGGINGS,getData().getLeggings());
                        equipment.set(Equipment.EquipmentSlot.BOOTS,getData().getBoots());
                    }
                } else {
                    if(bukkitEntity.getEquipment() != null){
                        bukkitEntity.getEquipment().clear();

                        bukkitEntity.getEquipment().setItemInHand(new ItemStack(Material.AIR));
                        bukkitEntity.getEquipment().setHelmet(new ItemStack(Material.AIR));
                        bukkitEntity.getEquipment().setChestplate(new ItemStack(Material.AIR));
                        bukkitEntity.getEquipment().setLeggings(new ItemStack(Material.AIR));
                        bukkitEntity.getEquipment().setBoots(new ItemStack(Material.AIR));

                        bukkitEntity.getEquipment().setItemInHand(getData().getWeapon());
                        bukkitEntity.getEquipment().setHelmet(getData().getHelmet());
                        bukkitEntity.getEquipment().setChestplate(getData().getChestplate());
                        bukkitEntity.getEquipment().setLeggings(getData().getLeggings());
                        bukkitEntity.getEquipment().setBoots(getData().getBoots());
                    }
                }
            }
        }.runTaskLater(DungeonRPG.getInstance(),2);

        if(hologram == null){
            hologram = HologramsAPI.createHologram(DungeonRPG.getInstance(),getSupposedHologramLocation());
            hologram.appendTextLine(getData().getMobType().getColor() + getData().getName() + " " + ChatColor.GOLD + "- Lv. " + getData().getLevel());
        }

        startRegenTask();

        tasks = new ArrayList<BukkitTask>();

        final CustomEntity c = this;

        for(MobSkill skill : getData().getSkills()){
            tasks.add(new BukkitRunnable(){
                @Override
                public void run() {
                    if(Util.getChanceBoolean(skill.getExecutionChanceTrue(),skill.getExecutionChanceFalse())){
                        skill.execute(c);
                    }
                }
            }.runTaskTimer(DungeonRPG.getInstance(),skill.getInterval()*20,skill.getInterval()*20));
        }

        STORAGE.put(bukkitEntity,this);
    }

    public void playAttackAnimation(){
        if(getBukkitEntity() != null){
            if(getData().getEntityType() != EntityType.PLAYER){
                for(Player p : getBukkitEntity().getWorld().getPlayers()){
                    ((CraftPlayer)p).getHandle().playerConnection.sendPacket(new PacketPlayOutAnimation(((CraftEntity) bukkitEntity).getHandle(), 0));
                }
            }
        }
    }

    public LivingEntity getTarget(){
        if(bukkitEntity != null && bukkitEntity instanceof Creature){
            return ((Creature)bukkitEntity).getTarget();
        } else {
            return null;
        }
    }

    public boolean hasTarget(){
        return getTarget() != null;
    }

    public void remove(){
        if(getBukkitEntity() != null){
            STORAGE.remove(getBukkitEntity());

            if(npc != null){
                npc.destroy();
            } else {
                bukkitEntity.remove();
                bukkitEntity = null;
            }
        }

        if(hologram != null){
            if(!hologram.isDeleted()) hologram.delete();
            hologram = null;
        }

        if(tasks != null) for(BukkitTask t : tasks) t.cancel();

        stopRegenTask();
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
        updateHealthBar(true);
    }

    public void updateHealthBar(boolean delay){
        if(delay){
            updateHealthBar(false);

            new BukkitRunnable(){
                @Override
                public void run() {
                    updateHealthBar(false);
                }
            }.runTaskLater(DungeonRPG.getInstance(),10);
        } else {
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
    }

    public static CustomEntity fromEntity(LivingEntity e){
        if(STORAGE.containsKey(e)){
            return STORAGE.get(e);
        } else {
            return null;
        }
    }
}
