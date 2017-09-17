package net.wrathofdungeons.dungeonrpg.skill.magician;

import net.wrathofdungeons.dungeonrpg.skill.Skill;
import net.wrathofdungeons.dungeonrpg.skill.SkillType;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import org.bukkit.entity.Player;

public class MagicCure implements Skill {
    @Override
    public String getName() {
        return "Magic Cure";
    }

    @Override
    public RPGClass getRPGClass() {
        return RPGClass.MAGICIAN;
    }

    @Override
    public SkillType getType() {
        return SkillType.SUPPORTING_SKILL;
    }

    @Override
    public String getCombo() {
        return "RLL";
    }

    @Override
    public void execute(Player p) {

    }
}
