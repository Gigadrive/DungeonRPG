package net.wrathofdungeons.dungeonrpg.skill.assassin;

import net.wrathofdungeons.dungeonrpg.skill.Skill;
import net.wrathofdungeons.dungeonrpg.skill.SkillType;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import org.bukkit.entity.Player;

public class StabbingStorm implements Skill {
    @Override
    public String getName() {
        return "Stabbing Storm";
    }

    @Override
    public RPGClass getRPGClass() {
        return RPGClass.ASSASSIN;
    }

    @Override
    public SkillType getType() {
        return SkillType.FAST_ATTACK;
    }

    @Override
    public String getCombo() {
        return "RLR";
    }

    @Override
    public void execute(Player p) {

    }
}
