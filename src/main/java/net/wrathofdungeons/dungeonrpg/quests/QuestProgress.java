package net.wrathofdungeons.dungeonrpg.quests;

import java.util.ArrayList;

public class QuestProgress {
    public int questID;
    public int questStage;
    public ArrayList<QuestObjectiveProgress> objectiveProgress = new ArrayList<QuestObjectiveProgress>();
    public QuestProgressStatus status = QuestProgressStatus.NOT_STARTED;
}
