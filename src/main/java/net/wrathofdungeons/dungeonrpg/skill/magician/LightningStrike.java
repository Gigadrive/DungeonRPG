package net.wrathofdungeons.dungeonrpg.skill.magician;

import net.wrathofdungeons.dungeonrpg.skill.Skill;
import net.wrathofdungeons.dungeonrpg.skill.SkillType;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import org.bukkit.entity.Player;

public class LightningStrike implements Skill {
    @Override
    public String getName() {
        return "Lightning Strike";
    }

    @Override
    public RPGClass getRPGClass() {
        return RPGClass.MAGICIAN;
    }

    @Override
    public SkillType getType() {
        return SkillType.AOE_ATTACK;
    }

    @Override
    public String getCombo() {
        return "RRL";
    }

    @Override
    public void execute(Player p) {

    }
}
