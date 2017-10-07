package net.wrathofdungeons.dungeonrpg.mobs.skills;

import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;

public interface MobSkill {
    public int getInterval();
    public int getExecutionChanceTrue();
    public int getExecutionChanceFalse();
    public void execute(CustomEntity entity);
}
