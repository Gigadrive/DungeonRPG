package net.wrathofdungeons.dungeonrpg.mobs;

public class AISettings {
    private boolean randomStroll;

    public AISettings(boolean randomStroll){
        this.randomStroll = randomStroll;
    }

    public boolean mayDoRandomStroll() {
        return randomStroll;
    }
}
