package net.wrathofdungeons.dungeonrpg.mobs.nms;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.enchantments.Enchantment;

public class ZombieArcher extends EntityZombie implements IRangedEntity {
    public ZombieArcher(World world){
        super(world);
    }

    public void a(EntityLiving entityLiving, float f){
        EntityArrow entityarrow = new EntityArrow(this.world, this, entityLiving, 1.6F, (float) (14 - this.world.getDifficulty().a() * 4));

        entityarrow.b((double) (f * 2.0F) + this.random.nextGaussian() * 0.25D + (double) ((float) this.world.getDifficulty().a() * 0.11F));

        // CraftBukkit start
        org.bukkit.event.entity.EntityShootBowEvent event = org.bukkit.craftbukkit.v1_8_R3.event.CraftEventFactory.callEntityShootBowEvent(this, this.bA(), entityarrow, 0.8F);
        if (event.isCancelled()) {
            event.getProjectile().remove();
            return;
        }

        if (event.getProjectile() == entityarrow.getBukkitEntity()) {
            world.addEntity(entityarrow);
        }
        // CraftBukkit end

        this.makeSound("random.bow", 1.0F, 1.0F / (this.bc().nextFloat() * 0.4F + 0.8F));
        // this.world.addEntity(entityarrow); // CraftBukkit - moved up
    }
}
