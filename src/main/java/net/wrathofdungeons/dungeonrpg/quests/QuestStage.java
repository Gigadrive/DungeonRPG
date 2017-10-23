package net.wrathofdungeons.dungeonrpg.quests;

import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.npc.CustomNPC;

public class QuestStage {
    public QuestObjective[] objectives = new QuestObjective[]{};
    public String hint;
    public CustomItem[] itemsToGet = new CustomItem[]{};

    public boolean isLastNPC(CustomNPC npc){
        if(objectives.length > 0){
            QuestObjective o = objectives[objectives.length-1];

            return o.type == QuestObjectiveType.TALK_TO_NPC && o.npcToTalkTo == npc.getId();
        }

        return false;
    }
}
