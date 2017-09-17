package net.wrathofdungeons.dungeonrpg.skill.mercenary;

import net.wrathofdungeons.dungeonrpg.skill.Skill;
import net.wrathofdungeons.dungeonrpg.skill.SkillType;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import org.bukkit.entity.Player;

public class Stomper implements Skill {
    @Override
    public String getName() {
        return "Stomper";
    }

    @Override
    public RPGClass getRPGClass() {
        return RPGClass.MERCENARY;
    }

    @Override
    public SkillType getType() {
        return SkillType.ESCAPING_MOVE;
    }

    @Override
    public String getCombo() {
        return "RRR";
    }

    @Override
    public void execute(Player p) {

    }
}
