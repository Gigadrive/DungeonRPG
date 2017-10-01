package net.wrathofdungeons.dungeonrpg.mobs;

public class AISettings {
    private MobAIType type = MobAIType.MELEE;
    private boolean randomStroll;
    private boolean lookAtPlayer;
    private boolean lookAround;

    public MobAIType getType() {
        return type;
    }

    public void setType(MobAIType type) {
        this.type = type;
    }

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
