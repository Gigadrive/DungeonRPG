package net.wrathofdungeons.dungeonrpg.mobs.handler;

import com.google.common.collect.Sets;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.goals.TargetNearbyEntityGoal;
import net.citizensnpcs.api.npc.NPC;
import net.minecraft.server.v1_8_R3.*;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.mobs.MobAIType;
import net.wrathofdungeons.dungeonrpg.mobs.MobType;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.util.UnsafeList;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class TargetHandler {
    public static void giveTargets(LivingEntity entity, CustomEntity customEntity){
        if(entity.getType() != EntityType.PLAYER){
            EntityInsentient c = (EntityInsentient) (((CraftEntity)entity).getHandle());
            EntitySlime s = null;
            if(c instanceof EntitySlime) s = (EntitySlime)c;

            try {
                Field bField = PathfinderGoalSelector.class.getDeclaredField("b");
                bField.setAccessible(true);
                Field cField = PathfinderGoalSelector.class.getDeclaredField("c");
                cField.setAccessible(true);
                if(s == null) bField.set(c.goalSelector, new UnsafeList<PathfinderGoalSelector>());
                bField.set(c.targetSelector, new UnsafeList<PathfinderGoalSelector>());
                if(s == null) cField.set(c.goalSelector, new UnsafeList<PathfinderGoalSelector>());
                cField.set(c.targetSelector, new UnsafeList<PathfinderGoalSelector>());

                c.goalSelector.a(0, new PathfinderGoalFloat(c));
                if(c instanceof EntityCreature) c.goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction((EntityCreature)c, 1.0D));
                if(c instanceof EntityCreature) if(customEntity.getData().getAiSettings().mayDoRandomStroll()) c.goalSelector.a(7, new PathfinderGoalRandomStroll((EntityCreature)c, 1.0D));
                if(customEntity.getData().getAiSettings().mayLookAtPlayer()) c.goalSelector.a(8, new PathfinderGoalLookAtPlayer(c, EntityPlayer.class, 8.0F));
                if(customEntity.getData().getAiSettings().mayLookAround()) c.goalSelector.a(8, new PathfinderGoalRandomLookaround(c));
                if(customEntity.getData().getAiSettings().getType() == MobAIType.RANGED && c instanceof IRangedEntity) c.goalSelector.a(4, new PathfinderGoalArrowAttack((IRangedEntity)c, 0.25F, 60, 10.0F));

                if(customEntity.getData().getMobType() != MobType.PASSIVE){
                    ArrayList<Class<? extends Entity>> a = new ArrayList<Class<? extends Entity>>();
                    a.add(EntityAnimal.class);
                    a.add(EntityGolem.class);
                    a.add(EntityHuman.class);
                    a.add(EntityMonster.class);
                    a.add(EntityVillager.class);

                    for(Class<? extends Entity> cl : a){
                        if(c instanceof EntityCreature) if(customEntity.getData().getAiSettings().getType() == MobAIType.MELEE) c.goalSelector.a(2, new PathfinderGoalMeleeAttack((EntityCreature)c, cl, 1.0D, false));
                        if(c instanceof EntityCreature) c.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget((EntityCreature)c, cl, false));
                    }
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        } else {
            NPC npc = customEntity.toCitizensNPC();

            if(npc != null){
                TargetNearbyEntityGoal.Builder builder = TargetNearbyEntityGoal.builder(npc);
                builder.aggressive(true);
                builder.radius(20);
                Set<EntityType> entityTypes = new HashSet<>();
                for(EntityType e : EntityType.values()){
                    if(e.isAlive()) entityTypes.add(e);
                }
                builder.targets(entityTypes);

                if(customEntity.getData().getMobType() != MobType.PASSIVE) npc.getDefaultGoalController().addGoal(builder.build(),10);
            } else {
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        giveTargets(entity,customEntity);
                    }
                }.runTaskLater(DungeonRPG.getInstance(),5);
            }
        }
    }

    public static void registerEntity(String name, int id, Class<? extends EntityInsentient> nmsClass, Class<? extends EntityInsentient> customClass){
        try {
            List<Map<?, ?>> dataMap = new ArrayList<Map<?, ?>>();

            for(Field f : EntityTypes.class.getDeclaredFields()){
                if(f.getType().getSimpleName().equals(Map.class.getSimpleName())){
                    f.setAccessible(true);
                    dataMap.add((Map<?, ?>) f.get(null));
                }
            }

            if(dataMap.get(2).containsKey(id)){
                dataMap.get(0).remove(name);
                dataMap.get(2).remove(id);
            }

            Method method = EntityTypes.class.getDeclaredMethod("a", Class.class, String.class, int.class);
            method.setAccessible(true);
            method.invoke(null, customClass, name, id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
