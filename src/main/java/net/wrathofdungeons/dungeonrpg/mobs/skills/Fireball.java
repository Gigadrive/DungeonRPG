package net.wrathofdungeons.dungeonrpg.mobs.skills;

import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.projectile.DungeonProjectile;
import net.wrathofdungeons.dungeonrpg.projectile.DungeonProjectileType;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

public class Fireball implements MobSkill {
    @Override
    public int getInterval() {
        return 9;
    }

    @Override
    public int getExecutionChanceTrue() {
        return 5;
    }

    @Override
    public int getExecutionChanceFalse() {
        return 1;
    }

    @Override
    public void execute(CustomEntity entity) {
        if(entity.hasTarget()){
            if(entity.getBukkitEntity() == null) return;
            entity.setCancelMovement(true);
            entity.executingSkill = true;

            new BukkitRunnable(){
                @Override
                public void run() {
                    if (entity.getBukkitEntity() == null) return;
                    entity.playAttackAnimation();

                    entity.getBukkitEntity().getWorld().playSound(entity.getBukkitEntity().getLocation(), Sound.BLOCK_FIRE_AMBIENT,1f,1f);

                    org.bukkit.entity.Fireball fireball = entity.getBukkitEntity().launchProjectile(org.bukkit.entity.Fireball.class);
                    fireball.setShooter(entity.getBukkitEntity());
                    fireball.setYield(0f);
                    fireball.setVelocity(fireball.getVelocity().multiply(3));

                    DungeonProjectile data = new DungeonProjectile(null, DungeonProjectileType.MOB_FIREBALL, fireball.getLocation(), 0, entity.getData().getAtk(), false);
                    data.setEntity(fireball);
                    DungeonRPG.SHOT_PROJECTILE_DATA.put(fireball.getUniqueId().toString(), data);
                }
            }.runTaskLater(DungeonRPG.getInstance(),20);

            new BukkitRunnable(){
                @Override
                public void run() {
                    entity.setCancelMovement(false);
                    entity.executingSkill = false;
                }
            }.runTaskLater(DungeonRPG.getInstance(),2*20);
        }
    }
}
