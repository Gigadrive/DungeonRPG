package net.wrathofdungeons.dungeonrpg.mobs.skills;

import net.wrathofdungeons.dungeonapi.util.ParticleEffect;
import net.wrathofdungeons.dungeonrpg.Duel;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.util.WorldUtilities;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class VortexBarrier implements MobSkill {
    @Override
    public int getInterval() {
        return 16;
    }

    @Override
    public int getExecutionChanceTrue() {
        return 20;
    }

    @Override
    public int getExecutionChanceFalse() {
        return 1;
    }

    @Override
    public void execute(CustomEntity entity) {
        entity.setCancelMovement(true);
        entity.executingSkill = true;

        final Location loc = entity.getBukkitEntity().getLocation().clone();
        int range = 7;

        for(Entity e : entity.getBukkitEntity().getNearbyEntities(range,range,range)){
            if(e instanceof LivingEntity){
                LivingEntity ent = (LivingEntity)e;
                CustomEntity c = CustomEntity.fromEntity(ent);

                if(c != null){
                    if(entity.getData().getMobType().mayAttack(c.getData().getMobType())){
                        ent.setVelocity(ent.getLocation().toVector().subtract(entity.getBukkitEntity().getLocation().toVector()).normalize().multiply(0.75).setY(1.25));
                    }
                } else {
                    if(ent instanceof Player){
                        Player p = (Player)ent;

                        if(GameUser.isLoaded(p)){
                            GameUser u = GameUser.getUser(p);

                            if(u.getCurrentCharacter() != null) ent.setVelocity(ent.getLocation().toVector().subtract(entity.getBukkitEntity().getLocation().toVector()).normalize().multiply(0.75).setY(1.25));
                        }
                    }
                }
            }
        }

        for(int i = 0; i < 5; i++){
            final int j = i;

            new BukkitRunnable(){
                @Override
                public void run() {
                    for(Location loc : WorldUtilities.getParticleCircle(loc.clone().add(0,j,0),2+(j/2),15)){
                        ParticleEffect.CLOUD.display(0f,0f,0f, 0f,2,loc,30);
                    }
                }
            }.runTaskLater(DungeonRPG.getInstance(),j);
        }

        new BukkitRunnable(){
            @Override
            public void run() {
                entity.setCancelMovement(false);
                entity.executingSkill = false;
            }
        }.runTaskLater(DungeonRPG.getInstance(),20);
    }
}
