package net.wrathofdungeons.dungeonrpg.items;

import net.wrathofdungeons.dungeonrpg.mobs.MobClass;

public enum ItemSource {

    MOB_DROP_LOWCLASS(MobClass.LOW),
    MOB_DROP_MIDCLASS(MobClass.MID),
    MOB_DROP_HIGHCLASS(MobClass.HIGH),
    MOB_DROP_BOSS(MobClass.BOSS),
    LOOT_CHESTS(null);

    public MobClass mobClass;

    ItemSource(MobClass mobClass){
        this.mobClass = mobClass;
    }

}