package net.wrathofdungeons.dungeonrpg.skill;

public enum SkillType {
    FAST_ATTACK(1,3),
    ESCAPING_MOVE(15,2),
    AOE_ATTACK(30,7),
    SUPPORTING_SKILL(45,4);

    private int minLevel;
    private int manaCost;

    SkillType(int minLevel, int manaCost){
        this.minLevel = minLevel;
        this.manaCost = manaCost;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public int getManaCost() {
        return manaCost;
    }
}
