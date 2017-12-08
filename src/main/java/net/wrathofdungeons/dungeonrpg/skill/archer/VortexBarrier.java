package net.wrathofdungeons.dungeonrpg.skill.archer;

import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.util.ParticleEffect;
import net.wrathofdungeons.dungeonrpg.Duel;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.mobs.MobType;
import net.wrathofdungeons.dungeonrpg.skill.Skill;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import net.wrathofdungeons.dungeonrpg.util.WorldUtilities;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class VortexBarrier implements Skill {
    @Override
    public String getName() {
        return "Vortex Barrier";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public HashMap<String, String> getEffects(int investedSkillPoints) {
        HashMap<String,String> effects = new HashMap<String, String>();

        if(investedSkillPoints == 1){
            effects.put("Range",String.valueOf(7));
        } else if(investedSkillPoints == 2){
            effects.put("Range",String.valueOf(7));
            effects.put("Buff","Speed I");
        } else if(investedSkillPoints == 3){
            effects.put("Range",String.valueOf(8));
            effects.put("Buff","Speed II");
        } else if(investedSkillPoints == 4){
            effects.put("Range",String.valueOf(8));
            effects.put("Buff","Speed III");
        }

        return effects;
    }

    @Override
    public int getIcon() {
        return 30;
    }

    @Override
    public int getIconDurability() {
        return 0;
    }

    @Override
    public RPGClass getRPGClass() {
        return RPGClass.ARCHER;
    }

    @Override
    public int getMinLevel() {
        return 45;
    }

    @Override
    public int getMaxInvestingPoints() {
        return 4;
    }

    @Override
    public int getBaseMPCost() {
        return 4;
    }

    @Override
    public void execute(Player p) {
        GameUser u = GameUser.getUser(p);
        CustomItem weapon = CustomItem.fromItemStack(p.getItemInHand());

        int investedSkillPoints = u.getCurrentCharacter().getVariables().getInvestedSkillPoints(this);

        int range = 7;
        PotionEffect speed = null;
        int buffTime = 3*60*20;

        if(investedSkillPoints == 1){
            range = 7;
        } else if(investedSkillPoints == 2){
            range = 7;
            speed = new PotionEffect(PotionEffectType.SPEED,buffTime,0,true,true);
        } else if(investedSkillPoints == 3){
            range = 8;
            speed = new PotionEffect(PotionEffectType.SPEED,buffTime,1,true,true);
        } else if(investedSkillPoints == 4){
            range = 8;
            speed = new PotionEffect(PotionEffectType.SPEED,buffTime,2,true,true);
        }

        if(weapon != null){
            if(u.getSkillValues().vortexBarrierTask != null){
                u.getSkillValues().vortexBarrierTask.cancel();
                u.getSkillValues().vortexBarrierTask = null;
            }

            final Location loc = p.getLocation().clone();

            if(speed != null && !p.hasPotionEffect(PotionEffectType.SPEED)){
                p.addPotionEffect(speed);
                p.sendMessage(ChatColor.GREEN + p.getName() + " has given you speed.");
            }

            for(Entity e : p.getNearbyEntities(range,range,range)){
                boolean launch = false;

                if(e instanceof LivingEntity){
                    LivingEntity ent = (LivingEntity)e;
                    CustomEntity c = CustomEntity.fromEntity(ent);

                    if(c != null){
                        launch = true;
                    } else {
                        if(ent instanceof Player){
                            Player p2 = (Player)ent;

                            if(GameUser.isLoaded(p2) && Duel.isDuelingWith(p,p2)){
                                GameUser u2 = GameUser.getUser(p2);

                                if(u2.getCurrentCharacter() != null) launch = true;
                            } else {
                                if(GameUser.isLoaded(p2)){
                                    GameUser u2 = GameUser.getUser(p2);

                                    if(u2.getCurrentCharacter() != null){
                                        if(speed != null && !p2.hasPotionEffect(PotionEffectType.SPEED)){
                                            p2.addPotionEffect(speed);
                                            p2.sendMessage(ChatColor.GREEN + p.getName() + " has given you speed.");
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if(e instanceof Projectile){
                    Projectile pr = (Projectile)e;

                    if(pr.getShooter() instanceof Player){
                        Player p2 = (Player)pr.getShooter();

                        if(GameUser.isLoaded(p2) && Duel.isDuelingWith(p,p2)){
                            GameUser u2 = GameUser.getUser(p2);

                            if(u2.getCurrentCharacter() != null) launch = true;
                        } else if(CustomEntity.fromEntity(p2) != null){
                            CustomEntity c = CustomEntity.fromEntity(p2);

                            if(c.getData().getMobType() == MobType.AGGRO || c.getData().getMobType() == MobType.NEUTRAL) launch = true;
                        }
                    } else if(pr.getShooter() instanceof LivingEntity){
                        LivingEntity ent = (LivingEntity)pr.getShooter();

                        if(CustomEntity.fromEntity(ent) != null){
                            CustomEntity c = CustomEntity.fromEntity(ent);

                            if(c.getData().getMobType() == MobType.AGGRO || c.getData().getMobType() == MobType.NEUTRAL) launch = true;
                        }
                    }
                }

                if(launch) e.setVelocity(e.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(0.75).setY(1.25));
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
        }
    }
}
