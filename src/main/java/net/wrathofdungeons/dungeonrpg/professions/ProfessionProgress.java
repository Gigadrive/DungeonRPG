package net.wrathofdungeons.dungeonrpg.professions;

public class ProfessionProgress {
    private Profession profession;
    private int level = 1;
    private double exp = 0;
    private boolean started = false;

    public ProfessionProgress(Profession profession){
        this.profession = profession;
    }

    public Profession getProfession() {
        return profession;
    }

    public int getLevel() {
        return level;
    }

    public double getExp() {
        return exp;
    }

    public void setExp(double exp) {
        this.exp = exp;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }
}
