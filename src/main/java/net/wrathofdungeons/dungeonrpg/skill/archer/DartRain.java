package net.wrathofdungeons.dungeonrpg.skill.archer;

import net.wrathofdungeons.dungeonrpg.skill.Skill;
import net.wrathofdungeons.dungeonrpg.skill.SkillType;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import org.bukkit.entity.Player;

public class DartRain implements Skill {
    @Override
    public String getName() {
        return "Dart Rain";
    }

    @Override
    public RPGClass getRPGClass() {
        return RPGClass.ARCHER;
    }

    @Override
    public SkillType getType() {
        return SkillType.FAST_ATTACK;
    }

    @Override
    public String getCombo() {
        return "LRL";
    }

    @Override
    public void execute(Player p) {
        p.sendMessage("lol");
    }
}
