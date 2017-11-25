package net.wrathofdungeons.dungeonrpg.skill.mercenary;

import net.wrathofdungeons.dungeonrpg.skill.Skill;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class BlazingAxe implements Skill {
    @Override
    public String getName() {
        return "Blazing Axe";
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
        return 286;
    }

    @Override
    public int getIconDurability() {
        return 0;
    }

    @Override
    public RPGClass getRPGClass() {
        return RPGClass.MERCENARY;
    }

    @Override
    public int getMinLevel() {
        return 45;
    }

    @Override
    public int getMaxInvestingPoints() {
        return 5;
    }

    @Override
    public int getBaseMPCost() {
        return 4;
    }

    @Override
    public void execute(Player p) {

    }
}
