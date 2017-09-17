package net.wrathofdungeons.dungeonrpg.skill.archer;

import net.wrathofdungeons.dungeonrpg.skill.Skill;
import net.wrathofdungeons.dungeonrpg.skill.SkillType;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import org.bukkit.entity.Player;

public class VortexBarrier implements Skill {
    @Override
    public String getName() {
        return "Vortex Barrier";
    }

    @Override
    public RPGClass getRPGClass() {
        return RPGClass.ARCHER;
    }

    @Override
    public SkillType getType() {
        return SkillType.SUPPORTING_SKILL;
    }

    @Override
    public String getCombo() {
        return "LRR";
    }

    @Override
    public void execute(Player p) {

    }
}
