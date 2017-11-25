package net.wrathofdungeons.dungeonrpg.skill.archer;

import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.StatPointType;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.projectile.DungeonProjectile;
import net.wrathofdungeons.dungeonrpg.projectile.DungeonProjectileType;
import net.wrathofdungeons.dungeonrpg.skill.Skill;
import net.wrathofdungeons.dungeonrpg.skill.SkillValues;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import net.wrathofdungeons.dungeonrpg.util.WorldUtilities;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class DartRain implements Skill {
    @Override
    public String getName() {
        return "Dart Rain";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public HashMap<String, String> getEffects(int investedSkillPoints) {
        HashMap<String,String> effects = new HashMap<String, String>();

        int arrowsAtOnce = 1;
        int totalShotArrows = 6;
        int shotStrength = 1;

        if(investedSkillPoints == 2){
            arrowsAtOnce = 1;
            totalShotArrows = 12*arrowsAtOnce;
            shotStrength = 1;
        } else if(investedSkillPoints == 3){
            arrowsAtOnce = 2;
            totalShotArrows = 12*arrowsAtOnce;
            shotStrength = 1;
        } else if(investedSkillPoints == 4){
            arrowsAtOnce = 2;
            totalShotArrows = 12*arrowsAtOnce;
            shotStrength = 2;
        } else if(investedSkillPoints == 5){
            arrowsAtOnce = 3;
            totalShotArrows = 16*arrowsAtOnce;
            shotStrength = 2;
        }

        effects.put("Arrows per shot",String.valueOf(arrowsAtOnce));
        effects.put("Total Arrows",String.valueOf(totalShotArrows));
        effects.put("Shot Strength",shotStrength + "x");

        return effects;
    }

    @Override
    public int getIcon() {
        return 262;
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
        return 1;
    }

    @Override
    public int getMaxInvestingPoints() {
        return 5;
    }

    @Override
    public int getBaseMPCost() {
        return 3;
    }

    @Override
    public void execute(Player p) {
        GameUser u = GameUser.getUser(p);
        CustomItem weapon = CustomItem.fromItemStack(p.getItemInHand());

        int investedSkillPoints = u.getCurrentCharacter().getVariables().getInvestedSkillPoints(this);

        int arrowsAtOnce = Integer.parseInt(getEffects(investedSkillPoints).get("Arrows per shot"));
        int totalShotArrows = Integer.parseInt(getEffects(investedSkillPoints).get("Total Arrows"));
        int shotStrength = Integer.parseInt(getEffects(investedSkillPoints).get("Shot Strength").replace("x",""));

        if(weapon != null){
            SkillValues values = u.getSkillValues();

            final int arrowAmount = totalShotArrows/arrowsAtOnce;

            values.dartRainArrows = arrowAmount;
            DartRain skill = this;

            if(values.dartRainTask == null){
                values.dartRainTask = new BukkitRunnable(){
                    @Override
                    public void run() {
                        if(values.dartRainArrows == 0){
                            cancel();
                            values.dartRainTask = null;
                        } else {
                            double damage = 0;

                            ArrayList<Arrow> arrows = new ArrayList<Arrow>();
                            double tilt = 0.15;

                            Vector originalVector = p.getLocation().getDirection().multiply(shotStrength+1);

                            if(arrowsAtOnce == 1){
                                Arrow a = p.launchProjectile(Arrow.class);

                                /*Arrow a = p.getWorld().spawnArrow(p.getLocation(),p.getLocation().getDirection().multiply(shotStrength+1),0.6f,12);
                                a.setShooter(p);*/
                                a.setVelocity(originalVector);
                                p.getWorld().playSound(p.getEyeLocation(), Sound.SHOOT_ARROW,1f,1f);
                                DungeonProjectile data = new DungeonProjectile(p, DungeonProjectileType.DART_RAIN, p.getLocation(), 0, damage, true);
                                data.setSkill(skill);
                                DungeonRPG.SHOT_PROJECTILE_DATA.put(a.getUniqueId().toString(),data);
                            } else if(arrowsAtOnce == 2){
                                Arrow first = p.launchProjectile(Arrow.class);
                                first.setVelocity(WorldUtilities.rotateVector(originalVector.clone(),-tilt));

                                Arrow second = p.launchProjectile(Arrow.class);
                                second.setVelocity(WorldUtilities.rotateVector(originalVector.clone(),+tilt));

                                arrows.add(first);
                                arrows.add(second);
                            } else if(arrowsAtOnce == 3){
                                Arrow third = p.launchProjectile(Arrow.class);
                                third.setVelocity(originalVector);

                                Arrow first = p.launchProjectile(Arrow.class);
                                first.setVelocity(WorldUtilities.rotateVector(originalVector.clone(),-tilt));

                                Arrow second = p.launchProjectile(Arrow.class);
                                second.setVelocity(WorldUtilities.rotateVector(originalVector.clone(),+tilt));

                                arrows.add(first);
                                arrows.add(second);
                                arrows.add(third);
                            }

                            p.getWorld().playSound(p.getEyeLocation(), Sound.SHOOT_ARROW,1f,1f);

                            for(Arrow a : arrows){
                                DungeonProjectile data = new DungeonProjectile(p, DungeonProjectileType.DART_RAIN, p.getLocation(), 0, damage, true);
                                data.setSkill(skill);
                                DungeonRPG.SHOT_PROJECTILE_DATA.put(a.getUniqueId().toString(),data);
                            }

                            /*for(int i = 0; i <= arrowsAtOnce; i++){
                                Arrow a = p.launchProjectile(Arrow.class);

                                a.setVelocity(a.getVelocity().multiply(shotStrength+1));
                                p.getWorld().playSound(p.getEyeLocation(), Sound.SHOOT_ARROW,1f,1f);
                                DungeonProjectile data = new DungeonProjectile(p, DungeonProjectileType.DART_RAIN, p.getLocation(), 0, damage, true);
                                data.setSkill(skill);
                                DungeonRPG.SHOT_PROJECTILE_DATA.put(a.getUniqueId().toString(),data);
                            }*/

                            p.getWorld().playEffect(p.getLocation(), Effect.MOBSPAWNER_FLAMES, 1);

                            values.dartRainArrows--;
                        }
                    }
                }.runTaskTimer(DungeonRPG.getInstance(),1,20/arrowAmount);
            }
        }
    }
}
