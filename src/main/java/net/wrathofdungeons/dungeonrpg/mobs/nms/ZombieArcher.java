package net.wrathofdungeons.dungeonrpg.mobs.nms;

import net.minecraft.server.v1_9_R2.*;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftArrow;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;

public class ZombieArcher extends DungeonZombie implements IRangedEntity {
    public ZombieArcher(World world){
        super(world);
    }

    public void a(EntityLiving entityLiving, float f){
        /*EntityArrow entityarrow = new EntityArrow(this.world, this, entityLiving, 1.6F, (float) (14 - this.world.getDifficulty().a() * 4));
        entityLiving.a*/
        Arrow a = ((LivingEntity)entityLiving.getBukkitEntity()).launchProjectile(Arrow.class);

        //entityarrow.b((double) (f * 2.0F) + this.random.nextGaussian() * 0.25D + (double) ((float) this.world.getDifficulty().a() * 0.11F));
        //entityarrow.c(0);

        // CraftBukkit start
        org.bukkit.event.entity.EntityShootBowEvent event = org.bukkit.craftbukkit.v1_9_R2.event.CraftEventFactory.callEntityShootBowEvent(this, this.b(EnumHand.MAIN_HAND), ((CraftArrow)a).getHandle(), 0.8F);
        if (event.isCancelled()) {
            event.getProjectile().remove();
            return;
        }

        /*if (event.getProjectile() == entityarrow.getBukkitEntity()) {
            world.addEntity(entityarrow);
        }*/
        // CraftBukkit end

        //this.makeSound("random.bow", 1.0F, 1.0F / (this.bc().nextFloat() * 0.4F + 0.8F));
        // this.world.addEntity(entityarrow); // CraftBukkit - moved up
    }
}
