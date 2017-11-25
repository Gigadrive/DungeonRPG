package net.wrathofdungeons.dungeonrpg.skill.archer;

import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.projectile.DungeonProjectile;
import net.wrathofdungeons.dungeonrpg.projectile.DungeonProjectileType;
import net.wrathofdungeons.dungeonrpg.skill.Skill;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class ExplosionArrow implements Skill {
    @Override
    public String getName() {
        return "Explosion Arrow";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public HashMap<String, String> getEffects(int investedSkillPoints) {
        HashMap<String,String> effects = new HashMap<String, String>();

        int explosionRange = 4;
        int shotStrength = 1;

        if(investedSkillPoints == 2){
            explosionRange = 6;
            shotStrength = 1;
        } else if(investedSkillPoints == 3){
            explosionRange = 8;
            shotStrength = 2;
        } else if(investedSkillPoints == 4){
            explosionRange = 10;
            shotStrength = 2;
        } else if(investedSkillPoints == 5){
            explosionRange = 12;
            shotStrength = 3;
        }

        effects.put("Explosion Range",String.valueOf(explosionRange));
        effects.put("Shot Strength",shotStrength + "x");

        return effects;
    }

    @Override
    public int getIcon() {
        return 46;
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
        int explosionRange = Integer.parseInt(getEffects(investedSkillPoints).get("Explosion Range"));
        int shotStrength = Integer.parseInt(getEffects(investedSkillPoints).get("Shot Strength").replace("x",""));

        if(weapon != null){
            Arrow a = p.launchProjectile(Arrow.class);
            p.getWorld().playSound(p.getEyeLocation(), Sound.SHOOT_ARROW, 1F, 1F);
            p.getWorld().playSound(p.getEyeLocation(), Sound.FIREWORK_LAUNCH, 1F, 1F);
            a.setVelocity(p.getLocation().getDirection().multiply(shotStrength+2));

            double damage = 0;

            DungeonProjectile data = new DungeonProjectile(p, DungeonProjectileType.EXPLOSION_ARROW, p.getLocation(), explosionRange, damage, true);
            data.setEntity(a);
            data.setSkill(this);
            DungeonRPG.SHOT_PROJECTILE_DATA.put(a.getUniqueId().toString(),data);
        }
    }
}
