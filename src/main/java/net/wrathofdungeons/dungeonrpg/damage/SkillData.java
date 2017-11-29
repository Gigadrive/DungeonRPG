package net.wrathofdungeons.dungeonrpg.damage;

import net.wrathofdungeons.dungeonrpg.skill.Skill;

public class SkillData {
    public SkillData(Skill skill, int investedSkillPoints){
        this.skill = skill;
        this.investedSkillPoints = investedSkillPoints;
    }

    public Skill skill;
    public int investedSkillPoints;
}
