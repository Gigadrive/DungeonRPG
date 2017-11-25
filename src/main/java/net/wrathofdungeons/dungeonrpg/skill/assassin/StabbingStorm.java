package net.wrathofdungeons.dungeonrpg.skill.assassin;

import net.wrathofdungeons.dungeonrpg.skill.Skill;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class StabbingStorm implements Skill {
    @Override
    public String getName() {
        return "Stabbing Storm";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public HashMap<String, String> getEffects(int investedSkillPoints) {
        HashMap<String,String> effects = new HashMap<String, String>();

        return effects;
    }

    @Override
    public int getIcon() {
        return 359;
    }

    @Override
    public int getIconDurability() {
        return 0;
    }

    @Override
    public RPGClass getRPGClass() {
        return RPGClass.ASSASSIN;
    }

    @Override
    public int getMinLevel() {
        return 1;
    }

    @Override
    public int getMaxInvestingPoints() {
        return 5;
    }

    @Override
    public int getBaseMPCost() {
        return 3;
    }

    @Override
    public void execute(Player p) {

    }
}
