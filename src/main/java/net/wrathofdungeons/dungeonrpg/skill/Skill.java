package net.wrathofdungeons.dungeonrpg.skill;

import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import org.bukkit.entity.Player;

public interface Skill {
    public String getName();
    public RPGClass getRPGClass();
    public SkillType getType();
    public String getCombo();
    public void execute(Player p);
}
