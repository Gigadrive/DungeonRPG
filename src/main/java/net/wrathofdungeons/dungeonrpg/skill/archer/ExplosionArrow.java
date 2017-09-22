package net.wrathofdungeons.dungeonrpg.skill.archer;

import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.StatPointType;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.projectile.DungeonProjectile;
import net.wrathofdungeons.dungeonrpg.projectile.DungeonProjectileType;
import net.wrathofdungeons.dungeonrpg.skill.Skill;
import net.wrathofdungeons.dungeonrpg.skill.SkillType;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;

public class ExplosionArrow implements Skill {
    @Override
    public String getName() {
        return "Explosion Arrow";
    }

    @Override
    public RPGClass getRPGClass() {
        return RPGClass.ARCHER;
    }

    @Override
    public SkillType getType() {
        return SkillType.AOE_ATTACK;
    }

    @Override
    public String getCombo() {
        return "LLR";
    }

    @Override
    public void execute(Player p) {
        GameUser u = GameUser.getUser(p);
        CustomItem weapon = CustomItem.fromItemStack(p.getItemInHand());

        if(weapon != null){
            Arrow a = p.launchProjectile(Arrow.class);
            p.getWorld().playSound(p.getEyeLocation(), Sound.SHOOT_ARROW, 1F, 1F);
            p.getWorld().playSound(p.getEyeLocation(), Sound.FIREWORK_LAUNCH, 1F, 1F);
            a.setVelocity(p.getLocation().getDirection().multiply(4.0D));

            double damage = 0;

            DungeonProjectile data = new DungeonProjectile(p, DungeonProjectileType.EXPLOSION_ARROW, p.getLocation(), 0, damage, true);
            data.setEntity(a);
            data.setSkill(this);
            DungeonRPG.SHOT_PROJECTILE_DATA.put(a.getUniqueId().toString(),data);
        }
    }
}
