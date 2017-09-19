package net.wrathofdungeons.dungeonrpg.skill.archer;

import net.wrathofdungeons.dungeonapi.util.ParticleEffect;
import net.wrathofdungeons.dungeonrpg.skill.Skill;
import net.wrathofdungeons.dungeonrpg.skill.SkillType;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class Leap implements Skill {
    @Override
    public String getName() {
        return "Leap";
    }

    @Override
    public RPGClass getRPGClass() {
        return RPGClass.ARCHER;
    }

    @Override
    public SkillType getType() {
        return SkillType.ESCAPING_MOVE;
    }

    @Override
    public String getCombo() {
        return "LLL";
    }

    @Override
    public void execute(Player p) {
        p.getWorld().playSound(p.getEyeLocation(), Sound.FIREWORK_LAUNCH, 1F, 1F);
        ParticleEffect.CLOUD.display(0.05F, 0.05F, 0.05F, 0.05F, 120, p.getLocation().add(0.0, -1.0, 0.0), 900);
        p.setVelocity(p.getLocation().getDirection().multiply(1.5).setY(1));
    }
}
