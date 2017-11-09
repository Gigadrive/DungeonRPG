package net.wrathofdungeons.dungeonrpg.user;

import net.wrathofdungeons.dungeonrpg.professions.Profession;
import net.wrathofdungeons.dungeonrpg.professions.ProfessionProgress;

import java.util.HashMap;

public class UserVariables {
    public boolean hasDoneFirstSkill = false;
    public boolean hasSeenChatRangeInfo = false;

    public StatisticsManager statisticsManager = new StatisticsManager();

    private HashMap<Profession,ProfessionProgress> professionProgress = new HashMap<Profession,ProfessionProgress>();

    public ProfessionProgress getProfessionProgress(Profession profession){
        if(professionProgress.containsKey(profession)){
            return professionProgress.get(profession);
        } else {
            professionProgress.put(profession,new ProfessionProgress(profession));
            return professionProgress.get(profession);
        }
    }
}
