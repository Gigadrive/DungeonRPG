package net.wrathofdungeons.dungeonrpg.mobs.handler;

import com.google.common.collect.Sets;
import net.minecraft.server.v1_8_R3.*;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.mobs.MobType;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.util.UnsafeList;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class TargetHandler {
    public static void giveTargets(LivingEntity entity, CustomEntity customEntity){
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
    }
}
