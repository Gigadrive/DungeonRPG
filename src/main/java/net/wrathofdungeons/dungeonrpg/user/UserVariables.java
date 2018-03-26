package net.wrathofdungeons.dungeonrpg.user;

import net.wrathofdungeons.dungeonrpg.dungeon.DungeonType;
import net.wrathofdungeons.dungeonrpg.professions.Profession;
import net.wrathofdungeons.dungeonrpg.professions.ProfessionProgress;
import net.wrathofdungeons.dungeonrpg.regions.StoredLocation;
import net.wrathofdungeons.dungeonrpg.skill.ClickComboType;
import net.wrathofdungeons.dungeonrpg.skill.Skill;
import net.wrathofdungeons.dungeonrpg.skill.SkillStorage;
import net.wrathofdungeons.dungeonrpg.util.FormularUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

public class UserVariables {
    public boolean hasDoneFirstSkill = false;
    public boolean hasSeenChatRangeInfo = false;
    public boolean hasSeenBindSkillInfo = false;

    public boolean autoJoin = false;

    public ArrayList<Integer> seenRegionTitles = new ArrayList<Integer>();

    public StatisticsManager statisticsManager = new StatisticsManager();

    private HashMap<ClickComboType,String> comboDefinitions = new HashMap<ClickComboType,String>();
    private HashMap<String,Integer> skillPoints = new HashMap<String,Integer>();
    private HashMap<Profession,ProfessionProgress> professionProgress = new HashMap<Profession,ProfessionProgress>();
    private HashMap<String,Integer> skillUses = new HashMap<String,Integer>();
    private HashMap<DungeonType,Timestamp> dungeonEntrances = new HashMap<DungeonType,Timestamp>();

    public StoredLocation saveLocation;
    public StoredLocation lastDeathLocation;

    public int getSkillUses(Skill skill){
        return skillUses.getOrDefault(skill.getClass().getSimpleName(),0);
    }

    public int getCurrentSkillUses(Skill skill){
        int uses = getSkillUses(skill);

        for (int i = 2; i <= getInvestedSkillPoints(skill); i++)
            uses -= FormularUtils.getNeededSkillUsesForLevel(i);

        if(uses < 0) uses = 0;
        return uses;
    }

    public void setSkillUses(Skill skill, int uses){
        if(uses < 0) uses = 0;
        if(skillUses.containsKey(skill.getClass().getSimpleName())) skillUses.remove(skill.getClass().getSimpleName());

        skillUses.put(skill.getClass().getSimpleName(),uses);
    }

    public void addSkillUses(Skill skill){
        addSkillUses(skill,1);
    }

    public void addSkillUses(Skill skill, int uses){
        setSkillUses(skill,getSkillUses(skill)+uses);
    }

    public int getInvestedSkillPoints(Skill skill){
        return skillPoints.getOrDefault(skill.getClass().getSimpleName(),0);
    }

    public void setInvestedSkillPoints(Skill skill, int points){
        if(points < 0) points = 0;
        if(skillPoints.containsKey(skill.getClass().getSimpleName())) skillPoints.remove(skill.getClass().getSimpleName());

        skillPoints.put(skill.getClass().getSimpleName(),points);
    }

    public void addInvestedSkillPoint(Skill skill){
        addInvestedSkillPoints(skill,1);
    }

    public void addInvestedSkillPoints(Skill skill, int points){
        setInvestedSkillPoints(skill,getInvestedSkillPoints(skill)+points);
    }

    public Skill getSkillFromCombo(ClickComboType type){
        String s = comboDefinitions.getOrDefault(type,null);

        if(s != null){
            return SkillStorage.getInstance().getSkill(s);
        } else {
            return null;
        }
    }

    public ClickComboType getComboFromSkill(Skill skill){
        for(ClickComboType type : comboDefinitions.keySet()){
            if(SkillStorage.getInstance().getSkill(comboDefinitions.get(type)).equals(skill)) return type;
        }

        return null;
    }

    public void setSkillForCombo(ClickComboType type, Skill skill){
        if(skill == null){
            resetSkillCombo(type);
        } else {
            if(comboDefinitions.containsKey(type)) comboDefinitions.remove(type);

            comboDefinitions.put(type,skill.getClass().getSimpleName());
        }
    }

    public void resetSkillCombo(Skill skill){
        if(getComboFromSkill(skill) != null) comboDefinitions.remove(getComboFromSkill(skill));
    }

    public void resetSkillCombo(ClickComboType type){
        if(getSkillFromCombo(type) != null) comboDefinitions.remove(type);
    }

    public Timestamp getLastDungeonEntry(DungeonType type){
        return dungeonEntrances.getOrDefault(type,null);
    }

    public void setLastDungeonEntry(DungeonType type, Timestamp timestamp){
        if(getLastDungeonEntry(type) != null) dungeonEntrances.remove(type);

        if(timestamp != null) dungeonEntrances.put(type,timestamp);
    }

    public ProfessionProgress getProfessionProgress(Profession profession){
        if(professionProgress.containsKey(profession)){
            return professionProgress.get(profession);
        } else {
            professionProgress.put(profession,new ProfessionProgress(profession));
            return professionProgress.get(profession);
        }
    }
}
