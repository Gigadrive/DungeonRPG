package net.wrathofdungeons.dungeonrpg.skill.mercenary;

import net.wrathofdungeons.dungeonrpg.skill.Skill;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;

public class Stomper implements Skill {
    @Override
    public String getName() {
        return "Stomper";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public HashMap<String, String> getEffects(int investedSkillPoints) {
        HashMap<String,String> effects = new HashMap<String, String>();

        int range = 4;

        if(investedSkillPoints == 2){
            range = 6;
        } else if(investedSkillPoints == 3){
            range = 8;
        } else if(investedSkillPoints == 4){
            range = 10;
        } else if(investedSkillPoints == 5){
            range = 12;
        }

        effects.put("Range",String.valueOf(range));

        return effects;
    }

    @Override
    public int getIcon() {
        return 3;
    }

    @Override
    public int getIconDurability() {
        return 1;
    }

    @Override
    public RPGClass getRPGClass() {
        return RPGClass.MERCENARY;
    }

    @Override
    public int getMinLevel() {
        return 15;
    }

    @Override
    public int getMaxInvestingPoints() {
        return 5;
    }

    @Override
    public int getBaseMPCost() {
        return 2;
    }

    @Override
    public void execute(Player p) {
        GameUser u = GameUser.getUser(p);
        int investedSkillPoints = u.getCurrentCharacter().getVariables().getInvestedSkillPoints(this);

        if(!u.getSkillValues().stomperActive){
            p.setVelocity(new Vector(0,3,0));

            u.getSkillValues().stomperActive = true;
            u.getSkillValues().stomperSkill = this;
        }
    }
}
