package net.wrathofdungeons.dungeonrpg.mobs.handler;

import com.google.common.collect.Sets;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.goals.TargetNearbyEntityGoal;
import net.citizensnpcs.api.npc.NPC;
import net.minecraft.server.v1_8_R3.*;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.mobs.MobType;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.util.UnsafeList;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TargetHandler {
    public static void giveTargets(LivingEntity entity, CustomEntity customEntity){
        if(entity.getType() != EntityType.PLAYER){
            EntityCreature c = (EntityCreature) ((EntityInsentient)((CraftEntity)entity).getHandle());

            try {
                Field bField = PathfinderGoalSelector.class.getDeclaredField("b");
                bField.setAccessible(true);
                Field cField = PathfinderGoalSelector.class.getDeclaredField("c");
                cField.setAccessible(true);
                bField.set(c.goalSelector, new UnsafeList<PathfinderGoalSelector>());
                bField.set(c.targetSelector, new UnsafeList<PathfinderGoalSelector>());
                cField.set(c.goalSelector, new UnsafeList<PathfinderGoalSelector>());
                cField.set(c.targetSelector, new UnsafeList<PathfinderGoalSelector>());

                c.goalSelector.a(0, new PathfinderGoalFloat(c));
                c.goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(c, 1.0D));
                if(customEntity.getData().getAiSettings().mayDoRandomStroll()) c.goalSelector.a(7, new PathfinderGoalRandomStroll(c, 1.0D));
                if(customEntity.getData().getAiSettings().mayLookAtPlayer()) c.goalSelector.a(8, new PathfinderGoalLookAtPlayer(c, EntityPlayer.class, 8.0F));
                if(customEntity.getData().getAiSettings().mayLookAround()) c.goalSelector.a(8, new PathfinderGoalRandomLookaround(c));

                if(customEntity.getData().getMobType() != MobType.PASSIVE){
                    ArrayList<Class<? extends Entity>> a = new ArrayList<Class<? extends Entity>>();
                    a.add(EntityAnimal.class);
                    a.add(EntityGolem.class);
                    a.add(EntityHuman.class);
                    a.add(EntityMonster.class);
                    a.add(EntityVillager.class);

                    for(Class<? extends Entity> cl : a){
                        c.goalSelector.a(2, new PathfinderGoalMeleeAttack(c, cl, 1.0D, false));
                        c.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(c, cl, false));
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
}
