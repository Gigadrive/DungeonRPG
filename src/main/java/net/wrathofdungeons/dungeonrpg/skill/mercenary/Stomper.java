package net.wrathofdungeons.dungeonrpg.skill.mercenary;

import net.wrathofdungeons.dungeonrpg.skill.Skill;
import net.wrathofdungeons.dungeonrpg.skill.SkillType;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Stomper implements Skill {
    @Override
    public String getName() {
        return "Stomper";
    }

    @Override
    public RPGClass getRPGClass() {
        return RPGClass.MERCENARY;
    }

    @Override
    public SkillType getType() {
        return SkillType.ESCAPING_MOVE;
    }

    @Override
    public String getCombo() {
        return "RRR";
    }

    @Override
    public void execute(Player p) {
        GameUser u = GameUser.getUser(p);

        if(!u.getSkillValues().stomperActive){
            p.setVelocity(new Vector(0,3,0));

            u.getSkillValues().stomperActive = true;
            u.getSkillValues().stomperSkill = this;
        }
    }
}
