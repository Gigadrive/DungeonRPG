package net.wrathofdungeons.dungeonrpg.skill.archer;

import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.damage.SkillData;
import net.wrathofdungeons.dungeonrpg.projectile.DungeonProjectile;
import net.wrathofdungeons.dungeonrpg.projectile.DungeonProjectileType;
import net.wrathofdungeons.dungeonrpg.skill.PoisonData;
import net.wrathofdungeons.dungeonrpg.skill.Skill;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class PoisonArrow implements Skill {
    @Override
    public String getName() {
        return "Poison Arrow";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public HashMap<String, String> getEffects(int investedSkillPoints) {
        HashMap<String,String> effects = new HashMap<String, String>();

        int time = investedSkillPoints+1;
        double damage = 1;

        if(investedSkillPoints == 1){
            damage = 1;
        } else if(investedSkillPoints == 2){
            damage = 1.2;
        } else if(investedSkillPoints == 3){
            damage = 1.4;
        } else if(investedSkillPoints == 4){
            damage = 1.6;
        } else if(investedSkillPoints == 5){
            damage = 1.8;
        } else if(investedSkillPoints == 6){
            damage = 2;
        } else if(investedSkillPoints == 7){
            damage = 2.4;
        } else if(investedSkillPoints == 8){
            damage = 2.8;
        } else if(investedSkillPoints == 9){
            damage = 3.2;
        } else if(investedSkillPoints == 10){
            damage = 3.4;
        }

        effects.put("Poison Time",String.valueOf(time) + " seconds");
        effects.put("Damage",damage + "x");

        return effects;
    }

    @Override
    public int getIcon() {
        return 373;
    }

    @Override
    public int getIconDurability() {
        return 8196;
    }

    @Override
    public RPGClass getRPGClass() {
        return RPGClass.ARCHER;
    }

    @Override
    public int getMinLevel() {
        return 9;
    }

    @Override
    public int getMaxInvestingPoints() {
        return 10;
    }

    @Override
    public int getBaseMPCost() {
        return 5;
    }

    @Override
    public void execute(Player p) {
        GameUser u = GameUser.getUser(p);

        int investedSkillPoints = u.getCurrentCharacter().getVariables().getInvestedSkillPoints(this);

        int time = Integer.parseInt(getEffects(investedSkillPoints).get("Poison Time").replace(" seconds",""));
        double damage = Double.parseDouble(getEffects(investedSkillPoints).get("Damage").replace("x",""));

        PoisonData poisonData = new PoisonData();

        poisonData.investedSkillPoints = investedSkillPoints;
        poisonData.givenByPlayer = p;

        Arrow arrow = p.launchProjectile(Arrow.class);
        arrow.setVelocity(p.getLocation().getDirection().multiply(4.5));
        p.getWorld().playSound(p.getEyeLocation(), Sound.ENTITY_ARROW_SHOOT, 1F, 1F);

        DungeonProjectile projectile = new DungeonProjectile(p, DungeonProjectileType.POISON_ARROW, p.getLocation(), 0, 0, true);
        projectile.setSkillData(new SkillData(this,investedSkillPoints));
        projectile.setPoisonData(poisonData);
        projectile.setEntity(arrow);

        DungeonRPG.SHOT_PROJECTILE_DATA.put(arrow.getUniqueId().toString(),projectile);
    }
}
