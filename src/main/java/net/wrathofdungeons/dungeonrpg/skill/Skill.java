package net.wrathofdungeons.dungeonrpg.skill;

import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import org.bukkit.entity.Player;

import java.util.HashMap;

public interface Skill {
    public String getName();
    public String getDescription();
    public HashMap<String,String> getEffects(int investedSkillPoints);
    public int getIcon();
    public int getIconDurability();
    public RPGClass getRPGClass();
    public int getMinLevel();
    public int getMaxInvestingPoints();
    public int getBaseMPCost();
    public void execute(Player p);
}
