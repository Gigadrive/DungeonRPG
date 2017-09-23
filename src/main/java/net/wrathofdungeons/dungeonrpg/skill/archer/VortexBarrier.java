package net.wrathofdungeons.dungeonrpg.skill.archer;

import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.util.ParticleEffect;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.skill.Skill;
import net.wrathofdungeons.dungeonrpg.skill.SkillType;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import net.wrathofdungeons.dungeonrpg.util.WorldUtilities;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class VortexBarrier implements Skill {
    @Override
    public String getName() {
        return "Vortex Barrier";
    }

    @Override
    public RPGClass getRPGClass() {
        return RPGClass.ARCHER;
    }

    @Override
    public SkillType getType() {
        return SkillType.SUPPORTING_SKILL;
    }

    @Override
    public String getCombo() {
        return "LRR";
    }

    @Override
    public void execute(Player p) {
        GameUser u = GameUser.getUser(p);
        CustomItem weapon = CustomItem.fromItemStack(p.getItemInHand());

        if(weapon != null){
            if(u.getSkillValues().vortexBarrierTask != null){
                u.getSkillValues().vortexBarrierTask.cancel();
                u.getSkillValues().vortexBarrierTask = null;
            }

            u.getSkillValues().vortexBarrierLoc = p.getLocation().clone();
            int range = 7;

            PotionEffect speed = new PotionEffect(PotionEffectType.SPEED,3*60*20,1,true,true);

            if(!p.hasPotionEffect(PotionEffectType.SPEED)){
                p.addPotionEffect(speed);
                p.sendMessage(ChatColor.GREEN + p.getName() + " has given you speed.");
            }

            for(Entity e : p.getNearbyEntities(7,7,7)){
                if(e instanceof LivingEntity){
                    LivingEntity ent = (LivingEntity)e;
                    CustomEntity c = CustomEntity.fromEntity(ent);

                    if(c != null){
                        ent.setVelocity(ent.getLocation().toVector().subtract(u.getSkillValues().vortexBarrierLoc.toVector()).normalize().multiply(0.75).setY(1.25));
                    } else {
                        if(ent instanceof Player){
                            Player p2 = (Player)ent;

                            if(false){
                                // TODO: handle duels
                            } else {
                                if(GameUser.isLoaded(p2)){
                                    GameUser u2 = GameUser.getUser(p2);

                                    if(u2.getCurrentCharacter() != null){
                                        if(!p2.hasPotionEffect(PotionEffectType.SPEED)){
                                            p2.addPotionEffect(speed);
                                            p2.sendMessage(ChatColor.GREEN + p.getName() + " has given you speed.");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            u.getSkillValues().vortexBarrierTask = new BukkitRunnable(){
                @Override
                public void run() {
                    if(u.getSkillValues().vortexBarrierCount == 5){
                        cancel();
                        u.getSkillValues().vortexBarrierCount = 0;
                        u.getSkillValues().vortexBarrierTask = null;
                        return;
                    }

                    for(Location loc : WorldUtilities.getParticleCircle(u.getSkillValues().vortexBarrierLoc.clone().add(0,u.getSkillValues().vortexBarrierCount,0),2+(u.getSkillValues().vortexBarrierCount/2),15)){
                        ParticleEffect.CLOUD.display(0f,0f,0f, 0f,2,loc,30);
                    }

                    u.getSkillValues().vortexBarrierCount++;
                }
            }.runTaskTimer(DungeonRPG.getInstance(),0,1);
        }
    }
}
