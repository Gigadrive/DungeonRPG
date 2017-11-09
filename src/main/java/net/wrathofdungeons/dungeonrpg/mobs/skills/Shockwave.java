package net.wrathofdungeons.dungeonrpg.mobs.skills;

import net.wrathofdungeons.dungeonapi.util.ParticleEffect;
import net.wrathofdungeons.dungeonrpg.Duel;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.damage.DamageData;
import net.wrathofdungeons.dungeonrpg.damage.DamageHandler;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.mobs.MobType;
import net.wrathofdungeons.dungeonrpg.mobs.nms.DungeonHorse;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.util.WorldUtilities;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class Shockwave implements MobSkill {
    @Override
    public int getInterval() {
        return 18;
    }

    @Override
    public int getExecutionChanceTrue() {
        return 25;
    }

    @Override
    public int getExecutionChanceFalse() {
        return 10;
    }

    @Override
    public void execute(CustomEntity customEntity) {
        if(!customEntity.hasTarget()) return;

        customEntity.setCancelMovement(true);
        customEntity.executingSkill = true;
        customEntity.playDamageAnimation();

        ArrayList<Entity> entities = new ArrayList<Entity>();

        final int range = 4;
        final Shockwave shockwave = this;
        final Location startLocation = customEntity.getBukkitEntity().getLocation().clone();

        for(int i = 0; i <= range; i++){
            final int j = i+1;
            new BukkitRunnable(){
                @Override
                public void run() {
                    ArrayList<Location> particleCircle = WorldUtilities.getParticleCircle(startLocation.clone().add(0,0.5,0),j,j*5);
                    for(Location loc : particleCircle) {
                        for(Entity entity : loc.getWorld().getNearbyEntities(loc, 1, 1, 1)) {
                            if(!entities.contains(entity)){
                                if(entity instanceof LivingEntity){
                                    LivingEntity livingEntity = (LivingEntity) entity;
                                    CustomEntity c = CustomEntity.fromEntity(livingEntity);

                                    if(c != null){
                                        if(customEntity.getData().getMobType().mayAttack(c.getData().getMobType())){
                                            c.damage(c.getData().getAtk()); // TODO: Increase damage with shockwave
                                            livingEntity.setVelocity(livingEntity.getLocation().toVector().subtract(startLocation.toVector()).normalize().multiply(2).setY(1));
                                            DungeonRPG.showBloodEffect(livingEntity.getLocation());
                                            entities.add(livingEntity);
                                        }
                                    } else {
                                        if(livingEntity instanceof Player){
                                            Player p = (Player)livingEntity;

                                            if(GameUser.isLoaded(p)){
                                                GameUser u = GameUser.getUser(p);

                                                if(u.getCurrentCharacter() != null){
                                                    if(c.getData().getMobType() == MobType.AGGRO || (c.getData().getMobType() == MobType.NEUTRAL && c.getDamagers().contains(p))){
                                                        DamageData damageData = DamageHandler.calculateMobToPlayerDamage(u,c,shockwave);
                                                        u.damage(damageData.getDamage());
                                                        livingEntity.setVelocity(livingEntity.getLocation().toVector().subtract(startLocation.toVector()).normalize().multiply(2).setY(1));
                                                        DungeonRPG.showBloodEffect(livingEntity.getLocation());
                                                        entities.add(livingEntity);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        ParticleEffect.CRIT_MAGIC.display(0f, 0f, 0f, 0.005f, 1, loc, 600);
                    }
                }
            }.runTaskLater(DungeonRPG.getInstance(),i*2);
        }

        new BukkitRunnable(){
            @Override
            public void run() {
                customEntity.setCancelMovement(false);
                customEntity.executingSkill = false;
            }
        }.runTaskLater(DungeonRPG.getInstance(),20);
    }
}
