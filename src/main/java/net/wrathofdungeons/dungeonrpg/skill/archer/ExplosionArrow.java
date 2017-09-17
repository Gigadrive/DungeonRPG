package net.wrathofdungeons.dungeonrpg.skill.archer;

import net.wrathofdungeons.dungeonrpg.skill.Skill;
import net.wrathofdungeons.dungeonrpg.skill.SkillType;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
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

    }
}
