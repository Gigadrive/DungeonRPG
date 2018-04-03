package net.wrathofdungeons.dungeonrpg.mobs;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.goals.WanderGoal;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.minecraft.server.v1_9_R2.*;
import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.dungeon.Dungeon;
import net.wrathofdungeons.dungeonrpg.items.*;
import net.wrathofdungeons.dungeonrpg.items.awakening.AwakeningType;
import net.wrathofdungeons.dungeonrpg.mobs.handler.TargetHandler;
import net.wrathofdungeons.dungeonrpg.mobs.nms.DungeonHorse;
import net.wrathofdungeons.dungeonrpg.mobs.nms.DungeonZombie;
import net.wrathofdungeons.dungeonrpg.mobs.nms.ZombieArcher;
import net.wrathofdungeons.dungeonrpg.mobs.skills.MobSkill;
import net.wrathofdungeons.dungeonrpg.quests.Quest;
import net.wrathofdungeons.dungeonrpg.quests.QuestObjective;
import net.wrathofdungeons.dungeonrpg.quests.QuestProgressStatus;
import net.wrathofdungeons.dungeonrpg.quests.QuestStage;
import net.wrathofdungeons.dungeonrpg.regions.Region;
import net.wrathofdungeons.dungeonrpg.skill.PoisonData;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.util.AttributeOperation;
import net.wrathofdungeons.dungeonrpg.util.WorldUtilities;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import net.citizensnpcs.api.npc.NPC;

public class CustomEntity {
    public static HashMap<LivingEntity,CustomEntity> STORAGE = new HashMap<LivingEntity,CustomEntity>();

    private int mobDataID;
    private int origin;
    private LivingEntity bukkitEntity;
    private NPC npc;

    private double hp;
    private double maxHP;

    private BukkitTask regenTask;

    private Hologram hologram;
    private TextLine healthLine;

    private ArrayList<Entity> damagers;

    public static final UUID maxHealthUID = UUID.fromString("f8b0a945-2d6a-4bdb-9a6f-59c285bf1e5d");
    public static final UUID followRangeUID = UUID.fromString("1737400d-3c18-41ba-8314-49a158481e1e");
    public static final UUID knockbackResistanceUID = UUID.fromString("8742c557-fcd5-4079-a462-b58db99b0f2c");
    public static final UUID movementSpeedUID = UUID.fromString("206a89dc-ae78-4c4d-b42c-3b31db3f5a7c");
    public static final UUID attackDamageUID = UUID.fromString("7bbe3bb1-079d-4150-ac6f-669e71550776");

    private AttributeModifier speedModifier;
    public boolean damaged = false;
    public boolean playerMobSpeed = true;
    public long attackDelay = 0;

    private WanderGoal wanderGoal;
    private Player lastDamager;

    private ArrayList<BukkitTask> tasks;

    public boolean executingSkill = false;
    private PoisonData poisonData;

    public CustomEntity(MobData data){
        this.mobDataID = data.getId();
        this.damagers = new ArrayList<Entity>();

        this.maxHP = data.getHealth();
        this.hp = maxHP;
    }

    public double getHP() {
        return hp;
    }

    public double getMaxHP() {
        return maxHP;
    }

    public void setHP(double hp){
        this.hp = maxHP;
        checkHP();
    }

    public void addHP(double hp){
        this.hp += hp;
        checkHP();
    }

    public void reduceHP(double hp){
        this.hp -= hp;
        checkHP();
    }

    public void playDamageAnimation(){
        if(bukkitEntity != null){
            bukkitEntity.damage(0);
            DungeonRPG.showBloodEffect(bukkitEntity.getLocation());
            bukkitEntity.getWorld().playSound(bukkitEntity.getEyeLocation(), Sound.ENTITY_PLAYER_HURT,0.5f,1f);
            getData().playSound(bukkitEntity.getLocation());
        }
    }

    private void checkHP(){
        if(this.hp > this.maxHP) this.hp = this.maxHP;

        if(this.hp <= 0) die();

        updateHealthBar();
    }

    public boolean requiresNewDamageHandler(){
        if(bukkitEntity != null){
            switch(bukkitEntity.getType()){
                case HORSE:
                case SHEEP:
                case CHICKEN:
                case VILLAGER:
                case BAT:
                case COW:
                case SQUID:
                case MUSHROOM_COW:
                case MAGMA_CUBE:
                case SLIME:
                    return true;
            }
        }

        return false;
    }

    public PoisonData getPoisonData() {
        return poisonData;
    }

    public boolean isPoisoned(){
        return getPoisonData() != null;
    }

    public void setPoisonData(PoisonData poisonData){
        if(this.poisonData != null) this.poisonData.cancelTasks();

        this.poisonData = poisonData;
    }

    public void die(){
        if (getBukkitEntity() == null || getBukkitEntity().isDead() || !getBukkitEntity().isValid())
            return;

        getData().playDeathSound(getBukkitEntity().getLocation());
        MobData mob = getData();

        stopLogicTask();

        if(getOriginRegion() != null && getOriginRegion().getAdditionalData().isBoss && getOriginRegion().getAdditionalData().dungeonType != null){
            // IS DUNGEON BOSS

            Dungeon dungeon = Dungeon.fromWorld(getBukkitEntity().getLocation().getWorld());
            if(dungeon != null){
                for(CustomEntity entity : dungeon.getMobs())
                    if(entity != this)
                        entity.die();

                new BukkitRunnable(){
                    @Override
                    public void run() {
                        dungeon.unregister();
                    }
                }.runTaskLater(DungeonRPG.getInstance(),10*20);
            }
        }

        getBukkitEntity().damage(getBukkitEntity().getHealth());

        if(lastDamager != null && lastDamager.isOnline()){
            if(GameUser.isLoaded(lastDamager)){
                Player p = lastDamager;
                GameUser u = GameUser.getUser(lastDamager);

                if(u.getCurrentCharacter() != null){
                    int currentValue = 0;
                    if(u.getCurrentCharacter().getVariables().statisticsManager.mobsKilled.containsKey(getData().getId())){
                        currentValue = u.getCurrentCharacter().getVariables().statisticsManager.mobsKilled.get(getData().getId());
                        u.getCurrentCharacter().getVariables().statisticsManager.mobsKilled.remove(getData().getId());
                    }

                    u.getCurrentCharacter().getVariables().statisticsManager.mobsKilled.put(getData().getId(),currentValue+1);

                    // DROP GOLD
                    if(mob.getMobType() == MobType.AGGRO || mob.getMobType() == MobType.NEUTRAL) if(Util.getChanceBoolean(50+u.getCurrentCharacter().getTotalValue(AwakeningType.FORTUNE),190)) WorldUtilities.dropItem(bukkitEntity.getLocation(),new CustomItem(7),p);

                    // DROP PREDEFINED ITEMS
                    if(mob.getPredefinedItemDrops() != null && mob.getPredefinedItemDrops().size() > 0){
                        for(PredefinedItemDrop drop : mob.getPredefinedItemDrops()){
                            if(drop.chance > 0){
                                if(!Util.getChanceBoolean(1,drop.chance)) continue;
                            } else if(drop.chance == 0){
                                // 100% drop chance
                            } else {
                                continue;
                            }

                            if(drop.itemID > 0 && ItemData.getData(drop.itemID) != null){
                                WorldUtilities.dropItem(bukkitEntity.getLocation(),new CustomItem(drop.itemID),p);
                            }
                        }
                    }

                    // DROP WEAPONS [automated]
                    if(mob.getMobType() == MobType.AGGRO || mob.getMobType() == MobType.NEUTRAL){
                        int limit = 2;

                        int[] usableLvls = new int[]{mob.getLevel()-2, mob.getLevel()-1, mob.getLevel(), mob.getLevel()+1, mob.getLevel()+2};

                        for(ItemData data : ItemData.STORAGE){
                            if (data.mayDropFromMonsters() && data.getRarity().getSources() != null) {
                                for(ItemSource s : data.getRarity().getSources()){
                                    if(s.mobClass != null){
                                        if(s.mobClass == mob.getMobClass()){
                                            //if(data.getCategory() == ItemCategory.ARMOR || data.getCategory() == ItemCategory.WEAPON_BOW || data.getCategory() == ItemCategory.WEAPON_AXE || data.getCategory() == ItemCategory.WEAPON_STICK || data.getCategory() == ItemCategory.WEAPON_SHEARS){
                                            if(data.getCategory() == ItemCategory.ARMOR || data.getCategory() == ItemCategory.WEAPON_BOW || data.getCategory() == ItemCategory.WEAPON_AXE || data.getCategory() == ItemCategory.WEAPON_STICK){
                                                if(data.getRarity() != ItemRarity.NONE && data.getRarity() != ItemRarity.SPECIAL){
                                                    for(int i : usableLvls){
                                                        if(i == data.getNeededLevel()){
                                                            if(mob.getMobClass().getChance(data.getRarity()) != null){
                                                                if(Util.getChanceBoolean(mob.getMobClass().getChance(data.getRarity()).min, mob.getMobClass().getChance(data.getRarity()).max-u.getCurrentCharacter().getTotalValue(AwakeningType.LOOT_BONUS))){
                                                                    if(limit != 0){
                                                                        WorldUtilities.dropItem(bukkitEntity.getLocation(),new CustomItem(data),p);
                                                                        limit--;
                                                                    }
                                                                }

                                                                break;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // DROP CRYSTALS
                    if(mob.getMobType() == MobType.AGGRO || mob.getMobType() == MobType.NEUTRAL){
                        if (Util.getChanceBoolean(1 + u.getCurrentCharacter().getTotalValue(AwakeningType.LOOT_BONUS), 3420)) {
                            WorldUtilities.dropItem(bukkitEntity.getLocation(),new CustomItem(DungeonRPG.CRYSTAL_IDS[Util.randomInteger(0,DungeonRPG.CRYSTAL_IDS.length-1)]),p);
                        }
                    }

                    // DROP UPGRADING STONE
                    if(mob.getMobType() == MobType.AGGRO || mob.getMobType() == MobType.NEUTRAL){
                        if (Util.getChanceBoolean(1 + u.getCurrentCharacter().getTotalValue(AwakeningType.LOOT_BONUS), 4400)) {
                            WorldUtilities.dropItem(bukkitEntity.getLocation(),new CustomItem(DungeonRPG.UPGRADING_STONE),p);
                        }
                    }

                    // DROP FOOD
                    if(mob.getMobType() == MobType.AGGRO || mob.getMobType() == MobType.NEUTRAL){
                        int limit = 2;

                        if (Util.getChanceBoolean(1 + u.getCurrentCharacter().getTotalValue(AwakeningType.LOOT_BONUS), 275)) {
                            int[] usableLvls = new int[]{mob.getLevel()-2, mob.getLevel()-1, mob.getLevel(), mob.getLevel()+1, mob.getLevel()+2};

                            for(ItemData data : ItemData.STORAGE){
                                if (data.getCategory() == ItemCategory.FOOD && data.mayDropFromMonsters()) {
                                    for(int i : usableLvls){
                                        if(i == data.getNeededLevel()){
                                            if(limit != 0){
                                                WorldUtilities.dropItem(bukkitEntity.getLocation(),new CustomItem(data),p);
                                                limit--;
                                            }

                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // DROP MATERIAL
                    ItemData materialData = ItemData.getData(mob.getMaterialDrop());
                    if(materialData != null){
                        if (Util.getChanceBoolean(1 + u.getCurrentCharacter().getTotalValue(AwakeningType.LOOT_BONUS), 125)) {
                            WorldUtilities.dropItem(bukkitEntity.getLocation(),new CustomItem(materialData),p);
                        }
                    }

                    // GIVE EXP
                    double xp = Util.randomDouble(mob.getXp()/2, mob.getXp());

                    if(Util.getIntegerDifference(u.getCurrentCharacter().getLevel(), mob.getLevel()) >= DungeonRPG.PLAYER_MOB_LEVEL_DIFFERENCE){
                        xp /= Util.getIntegerDifference(u.getCurrentCharacter().getLevel(), mob.getLevel());
                    }

                    xp = xp+xp*(u.getCurrentCharacter().getTotalValue(AwakeningType.XP_BONUS)*0.01);

                    if(xp > 0){
                        xp = u.giveEXP(xp);
                    }

                    if(u.getSettingsManager().playKillSound() && (mob.getMobType() == MobType.AGGRO || mob.getMobType() == MobType.NEUTRAL)) p.playSound(p.getEyeLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);

                    final Hologram holo = HologramsAPI.createHologram(DungeonRPG.getInstance(), bukkitEntity.getLocation().clone().add(0,2,0));

                    holo.appendTextLine(ChatColor.GOLD + "+" + ((Double)xp).intValue() + " EXP");

                    holo.getVisibilityManager().setVisibleByDefault(false);
                    holo.getVisibilityManager().showTo(p);

                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            holo.delete();
                        }
                    }.runTaskLater(DungeonRPG.getInstance(),10);

                    for(Quest q : Quest.STORAGE.values()){
                        if(u.getCurrentCharacter().getStatus(q) == QuestProgressStatus.STARTED){
                            QuestStage stage = q.getStages()[u.getCurrentCharacter().getCurrentStage(q)];

                            if(stage != null){
                                for(QuestObjective o : stage.objectives){
                                    if(o.mobToKill == mob.getId()){
                                        if(u.getCurrentCharacter().getObjectiveProgress(q,o).killedMobs < o.mobToKillAmount){
                                            u.getCurrentCharacter().getObjectiveProgress(q,o).killedMobs++;

                                            p.sendMessage(ChatColor.GRAY + "[Kill " + mob.getName() + " " + ChatColor.WHITE + "(" + u.getCurrentCharacter().getObjectiveProgress(q,o).killedMobs + "/" + o.mobToKillAmount + ")" + ChatColor.GRAY + "]");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!getHologram().isDeleted())
            getHologram().delete();

        new BukkitRunnable() {
            @Override
            public void run() {
                remove();
            }
        }.runTaskLater(DungeonRPG.getInstance(), 20);
    }

    public void damage(double hp){
        damaged = true;

        playDamageAnimation();
        reduceHP(hp);
    }

    public void damage(double hp, Player source){
        damaged = true;
        if(!damagers.contains(source)) damagers.add(source);
        lastDamager = source;
        playDamageAnimation();
        reduceHP(hp);
    }

    public Region getOriginRegion(){
        return origin != 0 ? Region.getRegion(origin,false) : null;
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
        /*if(getData().getEntityType() == EntityType.PLAYER){
            return npc != null && npc.getEntity() instanceof LivingEntity ? (LivingEntity)npc.getEntity() : null;
        } else {
            return bukkitEntity;
        }*/
        return bukkitEntity;
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
                        addHP(getData().getRegen());

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

    public ArrayList<Entity> getDamagers() {
        return damagers;
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
                bukkitEntity.setVelocity(WorldUtilities.safenVelocity(bukkitEntity.getLocation().toVector().subtract(from.toVector()).setY(-1).multiply(0.5)));
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
        return WorldUtilities.getEntitySpeed(bukkitEntity);
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
            adjustSpeed();
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

    public void initNPCValues() {
        if (toCitizensNPC() != null) {
            npc.setProtected(false);
            npc.getNavigator().getDefaultParameters().baseSpeed((float) getData().getSpeed()).useNewPathfinder(false).stationaryTicks(5 * 20).avoidWater(true);
            npc.data().setPersistent(NPC.TARGETABLE_METADATA, true);
            npc.data().setPersistent(NPC.DEFAULT_PROTECTED_METADATA, false);
            npc.data().setPersistent(NPC.NAMEPLATE_VISIBLE_METADATA, false);
            if (getData().getAiSettings().mayDoRandomStroll()/* && getData().getMobType() == MobType.PASSIVE*/)
                addWanderGoal();
            npc.getDefaultGoalController().setPaused(false);
        }
    }

    public void spawn(Location loc){
        if(bukkitEntity == null && !STORAGE.containsValue(this) && npc == null){
            if(getData().getEntityType() == EntityType.PLAYER){
                DungeonRPG.IGNORE_SPAWN_NPC.add(npc);
                npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER,DungeonRPG.randomColor().toString());
                npc.spawn(loc);

                if(getData().getSkins() != null){
                    if(getData().getSkins().size() == 1){
                        WorldUtilities.applySkinToNPC(npc,getData().getSkins().get(0));
                    } else if(getData().getSkins().size() > 1){
                        WorldUtilities.applySkinToNPC(npc,getData().getSkins().get(Util.randomInteger(0,getData().getSkins().size()-1)));
                    }
                }

                initNPCValues();

                if (getData().getMobType() != MobType.PASSIVE)
                    startLogicTask();

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
                    } else if(getData().getEntityType() == EntityType.HORSE){
                        World mcWorld = ((CraftWorld)loc.getWorld()).getHandle();
                        DungeonHorse dungeonHorse = new DungeonHorse(mcWorld);
                        dungeonHorse.setPosition(loc.getX(),loc.getY(),loc.getZ());
                        mcWorld.addEntity(dungeonHorse, CreatureSpawnEvent.SpawnReason.CUSTOM);

                        bukkitEntity = (Horse)dungeonHorse.getBukkitEntity();
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
                    } else {
                        bukkitEntity = (LivingEntity)loc.getWorld().spawnEntity(loc,getData().getEntityType());
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
            z.setVillager(getData().getEntitySettings().isZombieVillager());

            if(getData().isAdult()){
                z.setBaby(false);
            } else {
                z.setBaby(true);
            }
        } else if(bukkitEntity instanceof Villager){
            Villager v = (Villager)bukkitEntity;

            v.setProfession(getData().getEntitySettings().getVillagerProfession());

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

            s.setColor(getData().getEntitySettings().getSheepColor());

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

            o.setCatType(getData().getEntitySettings().getCatType());

            if(getData().isAdult()){
                o.setAdult();
            } else {
                o.setBaby();
            }
        } else if(bukkitEntity instanceof Creeper){
            Creeper c = (Creeper)bukkitEntity;

            c.setPowered(getData().getEntitySettings().isCreeperPowered());
        } else if(bukkitEntity instanceof Skeleton){
            Skeleton s = (Skeleton)bukkitEntity;

            s.setSkeletonType(getData().getEntitySettings().getSkeletonType());
        } else if(bukkitEntity instanceof Spider){
            Spider s = (Spider)bukkitEntity;
        } else if(bukkitEntity instanceof Slime){
            Slime s = (Slime)bukkitEntity;

            s.setSize(getData().getEntitySettings().getSlimeSize());
        } else if(bukkitEntity instanceof Ghast){
            Ghast g = (Ghast)bukkitEntity;
        } else if(bukkitEntity instanceof Enderman){
            Enderman e = (Enderman)bukkitEntity;

            e.setCarriedMaterial(new MaterialData(getData().getEntitySettings().getEndermanBlock()));
        } else if(bukkitEntity instanceof CaveSpider){
            CaveSpider c = (CaveSpider)bukkitEntity;
        } else if(bukkitEntity instanceof Blaze){
            Blaze b = (Blaze)bukkitEntity;
        } else if(bukkitEntity instanceof MagmaCube){
            MagmaCube m = (MagmaCube)bukkitEntity;

            m.setSize(getData().getEntitySettings().getSlimeSize());
        } else if(bukkitEntity instanceof Bat){
            Bat b = (Bat)bukkitEntity;

            b.setAwake(true);
        } else if(bukkitEntity instanceof Witch){
            Witch w = (Witch)bukkitEntity;
        } else if(bukkitEntity instanceof Guardian){
            Guardian g = (Guardian)bukkitEntity;

            g.setElder(getData().getEntitySettings().isElderGuardian());
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

            h.setColor(getData().getEntitySettings().getHorseColor());
            h.setCarryingChest(getData().getEntitySettings().hasHorseChest());
            h.setStyle(getData().getEntitySettings().getHorseStyle());
            h.setVariant(getData().getEntitySettings().getHorseVariant());

            if(getData().getEntitySettings().hasHorseSaddle()){
                h.getInventory().setSaddle(new ItemStack(Material.SADDLE));
            } else {
                h.getInventory().setSaddle(new ItemStack(Material.AIR));
            }

            if(getData().getEntitySettings().getHorseArmor() == HorseArmor.NONE){
                h.getInventory().setArmor(new ItemStack(Material.AIR));
            } else if(getData().getEntitySettings().getHorseArmor() == HorseArmor.GOLD){
                h.getInventory().setArmor(new ItemStack(Material.GOLD_BARDING));
            } else if(getData().getEntitySettings().getHorseArmor() == HorseArmor.IRON){
                h.getInventory().setArmor(new ItemStack(Material.IRON_BARDING));
            } else if(getData().getEntitySettings().getHorseArmor() == HorseArmor.DIAMOND){
                h.getInventory().setArmor(new ItemStack(Material.DIAMOND_BARDING));
            }

            if(getData().isAdult()){
                h.setAdult();
            } else {
                h.setBaby();
            }
        } else if(bukkitEntity instanceof Rabbit){
            Rabbit r = (Rabbit)bukkitEntity;

            r.setRabbitType(getData().getEntitySettings().getRabbitType());

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
                if(bukkitEntity == null) return;
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
                    if(!executingSkill && Util.getChanceBoolean(skill.getExecutionChanceTrue(),skill.getExecutionChanceFalse())) skill.execute(c);
                }
            }.runTaskTimer(DungeonRPG.getInstance(),Util.randomInteger(0,skill.getInterval()*20),skill.getInterval()*20));
        }

        STORAGE.put(getBukkitEntity(), this);
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

    private LivingEntity target;

    public LivingEntity getTarget(){
        if (bukkitEntity != null) {
            if (getData().getEntityType() == EntityType.PLAYER) {
                return target != null && target.isValid() && !target.isDead() && target.getWorld() == getBukkitEntity().getWorld() ? target : null;
            } else if (bukkitEntity != null && bukkitEntity instanceof Creature) {
                return ((Creature) bukkitEntity).getTarget();
            }
        }

        return null;
    }

    public void findTarget() {
        if (getTarget() != null) return;
        setTarget(null);

        final int range = 20;
        final List<Entity> entitiesWithinRange = getBukkitEntity().getNearbyEntities(range, range, range);

        for (final Entity potential : entitiesWithinRange) {
            if (getData().getMobType() == MobType.NEUTRAL && !damagers.contains(potential))
                continue;

            if (!(potential instanceof LivingEntity))
                continue;

            LivingEntity livingEntity = (LivingEntity) potential;

            if (livingEntity instanceof Player && GameUser.isLoaded((Player) livingEntity)) {
                if (getData().getMobType() == MobType.AGGRO || getData().getMobType() == MobType.NEUTRAL) {
                    Player p = (Player) livingEntity;
                    GameUser u = GameUser.getUser(p);

                    if (u.getCurrentCharacter() != null) {
                        setTarget(livingEntity);
                        break;
                    }
                }
            } else if (CustomEntity.fromEntity(livingEntity) != null) {
                CustomEntity c = CustomEntity.fromEntity(livingEntity);

                if (DungeonRPG.mayAttack(getData().getMobType(), c.getData().getMobType())) {
                    setTarget(livingEntity);
                    break;
                }
            }
        }
    }

    public void faceEntity(LivingEntity entity) {
        if (getBukkitEntity() == null) return;

        if (!getBukkitEntity().getWorld().equals(entity.getWorld())) {
            return;
        }
        final Location loc = getBukkitEntity().getLocation();

        final double xDiff = entity.getLocation().getX() - loc.getX();
        final double yDiff = entity.getLocation().getY() - loc.getY();
        final double zDiff = entity.getLocation().getZ() - loc.getZ();

        final double distanceXZ = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
        final double distanceY = Math.sqrt(distanceXZ * distanceXZ + yDiff * yDiff);

        double yaw = (Math.acos(xDiff / distanceXZ) * 180 / Math.PI);
        final double pitch = (Math.acos(yDiff / distanceY) * 180 / Math.PI) - 90;
        if (zDiff < 0.0) {
            yaw = yaw + (Math.abs(180 - yaw) * 2);
        }

        net.citizensnpcs.util.NMS.look(getBukkitEntity(), (float) yaw - 90, (float) pitch);
    }

    public void faceForward() {
        if (getBukkitEntity() == null) return;

        net.citizensnpcs.util.NMS.look(getBukkitEntity(), getBukkitEntity().getLocation().getYaw(), 0);
    }

    public void faceAlignWithVehicle() {
        if (getBukkitEntity() == null) return;

        final org.bukkit.entity.Entity v = getBukkitEntity().getVehicle();
        net.citizensnpcs.util.NMS.look(getBukkitEntity(), v.getLocation().getYaw(), 0);
    }

    public void setTarget(LivingEntity livingEntity){
        if (bukkitEntity != null) {
            if (getData().getEntityType() == EntityType.PLAYER) {
                this.target = livingEntity;

                if (this.target == null) {
                    toCitizensNPC().getNavigator().cancelNavigation();

                    faceForward();

                    toCitizensNPC().getDefaultGoalController().setPaused(false);
                    initNPCValues();
                } else {
                    toCitizensNPC().getNavigator().setTarget(livingEntity, true);

                    if (!toCitizensNPC().getDefaultGoalController().isPaused())
                        toCitizensNPC().getDefaultGoalController().setPaused(true);
                }
            } else if (bukkitEntity instanceof Creature) {
                ((Creature) bukkitEntity).setTarget(livingEntity);
            }
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
            }

            if (bukkitEntity != null) {
                bukkitEntity.remove();
                //bukkitEntity = null;
            }
        }

        if(hologram != null){
            if(!hologram.isDeleted()) hologram.delete();
            hologram = null;
        }

        if(tasks != null) for(BukkitTask t : tasks) t.cancel();

        stopLogicTask();
        stopRegenTask();
        setPoisonData(null);
    }

    private String healthBarText(){
        int div = ((Double)((getHP()/getMaxHP())*10)).intValue();

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
        if (!damaged || getHologram() == null || getHologram().isDeleted()) return;

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

    private BukkitTask logicTask;

    public void startLogicTask() {
        stopLogicTask();

        final int delay = Util.randomInteger(2, 5) * 20;

        logicTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (getTarget() != null) {
                    double distance = toCitizensNPC() != null ? toCitizensNPC().getEntity().getLocation().distance(getTarget().getLocation()) : getBukkitEntity().getLocation().distance(getTarget().getLocation());

                    if (distance < 30) {
                        faceEntity(getTarget());
                    } else {
                        setTarget(null);
                    }
                }

                if (getTarget() == null)
                    findTarget();
            }
        }.runTaskTimer(DungeonRPG.getInstance(), delay, delay);
    }

    public void stopLogicTask() {
        if (logicTask != null) {
            logicTask.cancel();
            logicTask = null;
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
