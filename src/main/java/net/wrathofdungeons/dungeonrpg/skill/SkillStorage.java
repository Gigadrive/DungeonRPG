package net.wrathofdungeons.dungeonrpg.skill;

import java.util.ArrayList;

public class SkillStorage {
    private ArrayList<Skill> storage;
    private static SkillStorage instance;

    public SkillStorage(){
        this.storage = new ArrayList<Skill>();
        instance = this;
    }

    public static SkillStorage getInstance() {
        return instance;
    }

    public ArrayList<Skill> getSkills() {
        return storage;
    }

    public Skill getSkill(String s){
        for(Skill skill : getSkills()){
            if(skill.getClass().getSimpleName().equalsIgnoreCase(s)) return skill;
        }

        return null;
    }

    public void addSkill(Skill s){
        getSkills().add(s);
    }

    public void removeSkill(Skill s){
        getSkills().remove(s);
    }
}
