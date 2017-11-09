package net.wrathofdungeons.dungeonrpg.skill.mercenary;

import net.wrathofdungeons.dungeonapi.util.ParticleEffect;
import net.wrathofdungeons.dungeonrpg.Duel;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.damage.DamageData;
import net.wrathofdungeons.dungeonrpg.damage.DamageHandler;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.skill.Skill;
import net.wrathofdungeons.dungeonrpg.skill.SkillType;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import net.wrathofdungeons.dungeonrpg.util.WorldUtilities;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class Shockwave implements Skill {
    @Override
    public String getName() {
        return "Shockwave";
    }

    @Override
    public RPGClass getRPGClass() {
        return RPGClass.MERCENARY;
    }

    @Override
    public SkillType getType() {
        return SkillType.AOE_ATTACK;
    }

    @Override
    public String getCombo() {
        return "RRL";
    }

    @Override
    public void execute(Player p) {
        GameUser u = GameUser.getUser(p);

        ArrayList<Entity> entities = new ArrayList<Entity>();

        final int range = 4;
        final Shockwave shockwave = this;
        final Location startLocation = p.getLocation().clone();

        for(int i = 0; i <= range; i++){
            final int j = i+1;
            new BukkitRunnable(){
                @Override
                public void run() {
                    u.ignoreDamageCheck = true;

                    ArrayList<Location> particleCircle = WorldUtilities.getParticleCircle(startLocation.clone().add(0,0.5,0),j,j*5);
                    for(Location loc : particleCircle){
                        for(Entity entity : loc.getWorld().getNearbyEntities(loc,1,1,1)){
                            if(!entities.contains(entity)){
                                if(entity instanceof LivingEntity){
                                    LivingEntity livingEntity = (LivingEntity)entity;
                                    CustomEntity c = CustomEntity.fromEntity(livingEntity);

                                    if(c != null){
                                        entities.add(livingEntity);
                                        DungeonRPG.showBloodEffect(livingEntity.getLocation());
                                        c.getData().playSound(livingEntity.getLocation());
                                        livingEntity.setVelocity(livingEntity.getLocation().toVector().subtract(startLocation.toVector()).normalize().multiply(2).setY(1));
                                        DamageData damageData = DamageHandler.calculatePlayerToMobDamage(u,c,shockwave);
                                        c.damage(damageData.getDamage(),p);
                                        DamageHandler.spawnDamageIndicator(p,damageData,c.getBukkitEntity().getLocation());
                                    } else {
                                        if(livingEntity instanceof Player){
                                            Player p2 = (Player)livingEntity;

                                            if(GameUser.isLoaded(p2) && Duel.isDuelingWith(p,p2)){
                                                GameUser u2 = GameUser.getUser(p2);
                                                entities.add(livingEntity);
                                                DungeonRPG.showBloodEffect(livingEntity.getLocation());
                                                livingEntity.setVelocity(livingEntity.getLocation().toVector().subtract(startLocation.toVector()).normalize().multiply(2).setY(1));
                                                DamageData damageData = DamageHandler.calculatePlayerToPlayerDamage(u,u2,shockwave);
                                                u2.damage(damageData.getDamage(),p);
                                                DamageHandler.spawnDamageIndicator(p,damageData,p2.getLocation());
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        ParticleEffect.CRIT_MAGIC.display(0f,0f,0f,0.005f,1,loc,600);
                    }

                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            u.ignoreDamageCheck = false;
                        }
                    }.runTaskLater(DungeonRPG.getInstance(),1);
                }
            }.runTaskLater(DungeonRPG.getInstance(),i*2);
        }
    }
}
