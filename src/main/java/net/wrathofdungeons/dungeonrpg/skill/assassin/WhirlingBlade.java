package net.wrathofdungeons.dungeonrpg.skill.assassin;

import net.wrathofdungeons.dungeonapi.util.ParticleEffect;
import net.wrathofdungeons.dungeonrpg.Duel;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.damage.DamageData;
import net.wrathofdungeons.dungeonrpg.damage.DamageHandler;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.projectile.DungeonProjectile;
import net.wrathofdungeons.dungeonrpg.projectile.DungeonProjectileType;
import net.wrathofdungeons.dungeonrpg.skill.Skill;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import net.wrathofdungeons.dungeonrpg.util.WorldUtilities;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;

public class WhirlingBlade implements Skill {
    @Override
    public String getName() {
        return "Whirling Blade";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public HashMap<String, String> getEffects(int investedSkillPoints) {
        HashMap<String,String> effects = new HashMap<String, String>();

        int range = 1;
        double damage = 1;
        double speed = 1;

        if(investedSkillPoints == 1){
            range = 3;
            damage = 1;
            speed = 1;
        } else if(investedSkillPoints == 2){
            range = 4;
            damage = 1.2;
            speed = 1.25;
        } else if(investedSkillPoints == 3){
            range = 5;
            damage = 1.4;
            speed = 1.5;
        } else if(investedSkillPoints == 4){
            range = 6;
            damage = 1.6;
            speed = 1.75;
        } else if(investedSkillPoints == 5){
            range = 7;
            damage = 1.8;
            speed = 2;
        }

        effects.put("Range",String.valueOf(range));
        effects.put("Damage",damage + "x");
        effects.put("Speed",speed + "x");

        return effects;
    }

    @Override
    public int getIcon() {
        return 256;
    }

    @Override
    public int getIconDurability() {
        return 0;
    }

    @Override
    public RPGClass getRPGClass() {
        return RPGClass.ASSASSIN;
    }

    @Override
    public int getMinLevel() {
        return 30;
    }

    @Override
    public int getMaxInvestingPoints() {
        return 5;
    }

    @Override
    public int getBaseMPCost() {
        return 7;
    }

    @Override
    public void execute(Player p) {
        GameUser u = GameUser.getUser(p);
        CustomItem weapon = CustomItem.fromItemStack(p.getItemInHand());

        int investedSkillPoints = u.getCurrentCharacter().getVariables().getInvestedSkillPoints(this);
        int range = Integer.parseInt(getEffects(investedSkillPoints).get("Range"));
        double damage = Double.parseDouble(getEffects(investedSkillPoints).get("Damage").replace("x",""));
        double speed = Double.parseDouble(getEffects(investedSkillPoints).get("Speed").replace("x",""));
        final double damageRange = 2.5;
        final Location playerLoc = p.getEyeLocation().clone().add(0,0.5,0);
        final WhirlingBlade whirlingBlade = this;

        if(weapon != null){
            ArrayList<Entity> entities = new ArrayList<Entity>();
            ArrayList<Location> locations = WorldUtilities.getParticleCircle(playerLoc,range,range*3);

            int i = 0;
            for(Location loc : locations){
                u.getSkillValues().skillTasks.add(new BukkitRunnable(){
                    @Override
                    public void run(){
                        if(investedSkillPoints == 1 || investedSkillPoints == 2){
                            ParticleEffect.CRIT.display(0f,0f,0f,0f,1,loc,600);
                        } else if(investedSkillPoints == 3 || investedSkillPoints == 4){
                            ParticleEffect.CRIT.display(0f,0f,0f,0f,1,loc.clone().add(0,0.5,0),600);
                            ParticleEffect.CRIT.display(0f,0f,0f,0f,1,loc.clone().add(0,-0.5,0),600);
                        } else if(investedSkillPoints == 5){
                            ParticleEffect.CRIT.display(0f,0f,0f,0f,1,loc.clone().add(0,1,0),600);
                            ParticleEffect.CRIT.display(0f,0f,0f,0f,1,loc,600);
                            ParticleEffect.CRIT.display(0f,0f,0f,0f,1,loc.clone().add(0,-1,0),600);
                        }

                        loc.getWorld().playSound(loc,Sound.ENTITY_SHEEP_SHEAR,1f,1f);

                        for(Entity entity : loc.getWorld().getNearbyEntities(loc,damageRange,damageRange,damageRange)){
                            if(!entities.contains(entity)){
                                if(entity instanceof LivingEntity){
                                    LivingEntity l = (LivingEntity)entity;

                                    if(l instanceof Player && GameUser.isLoaded((Player)l)){
                                        Player p2 = (Player)l;
                                        GameUser u2 = GameUser.getUser(p2);

                                        if(DungeonRPG.mayAttack(p,p2)){
                                            entities.add(l);
                                            DungeonRPG.showBloodEffect(l.getLocation());
                                            u2.giveNormalKnockback(playerLoc);
                                            DamageData damageData = DamageHandler.calculatePlayerToPlayerDamage(u,u2,whirlingBlade);
                                            DamageHandler.spawnDamageIndicator(p,damageData,p2.getLocation());
                                            u2.damage(damageData.getDamage(),p);
                                        }
                                    } else {
                                        CustomEntity c = CustomEntity.fromEntity(l);

                                        if(c != null){
                                            entities.add(l);
                                            DungeonRPG.showBloodEffect(l.getLocation());
                                            c.getData().playSound(l.getLocation());
                                            c.giveNormalKnockback(playerLoc);
                                            DamageData damageData = DamageHandler.calculatePlayerToMobDamage(u,c,whirlingBlade);
                                            DamageHandler.spawnDamageIndicator(p,damageData,c.getBukkitEntity().getLocation());
                                            c.damage(damageData.getDamage(),p);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }.runTaskLater(DungeonRPG.getInstance(),i == 0 ? 0 : (long)(i + (5/speed))));

                i++;
            }
        }
    }
}
