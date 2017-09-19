package net.wrathofdungeons.dungeonrpg.mobs;

public class AISettings {
    private boolean randomStroll;
    private boolean lookAtPlayer;
    private boolean lookAround;

    public boolean mayDoRandomStroll() {
        return randomStroll;
    }

    public void setRandomStroll(boolean randomStroll) {
        this.randomStroll = randomStroll;
    }

    public boolean mayLookAround() {
        return lookAround;
    }

    public void setLookAround(boolean lookAround) {
        this.lookAround = lookAround;
    }

    public boolean mayLookAtPlayer() {
        return lookAtPlayer;
    }

    public void setLookAtPlayer(boolean lookAtPlayer) {
        this.lookAtPlayer = lookAtPlayer;
    }
}
