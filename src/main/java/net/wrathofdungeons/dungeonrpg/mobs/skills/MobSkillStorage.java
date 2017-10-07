package net.wrathofdungeons.dungeonrpg.mobs.skills;

import java.util.HashMap;

public class MobSkillStorage {
    private static HashMap<String,MobSkill> STORAGE = new HashMap<String, MobSkill>();
    private static boolean init = false;

    public static void init(){
        if(!init){
            addSkill("FIREBALL",new Fireball());

            init = true;
        }
    }

    public static MobSkill getSkill(String name){
        return STORAGE.get(name);
    }

    public static void addSkill(String name,MobSkill skill){
        if(!STORAGE.containsValue(skill)) STORAGE.put(name,skill);
    }

    public static void removeSkill(String name){
        STORAGE.remove(name);
    }
}
