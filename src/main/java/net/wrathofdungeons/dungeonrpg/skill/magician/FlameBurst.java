package net.wrathofdungeons.dungeonrpg.skill.magician;

import net.wrathofdungeons.dungeonrpg.skill.Skill;
import net.wrathofdungeons.dungeonrpg.skill.SkillType;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import org.bukkit.entity.Player;

public class FlameBurst implements Skill {
    @Override
    public String getName() {
        return "Flame Burst";
    }

    @Override
    public RPGClass getRPGClass() {
        return RPGClass.MAGICIAN;
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
