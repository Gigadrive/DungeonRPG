package net.wrathofdungeons.dungeonrpg.items;

import net.wrathofdungeons.dungeonrpg.user.RPGClass;

public enum ItemCategory {
    WEAPON_AXE,
    WEAPON_SHEARS,
    WEAPON_BOW,
    WEAPON_STICK,
    MISC,
    ARMOR,
    TELEPORT_SCROLL,
    FOOD,
    MATERIAL,
    COLLECTIBLE,
    QUEST,
    PICKAXE,
    MOUNT,
    CRYSTAL;

    public boolean wrongClass(RPGClass clazz){
        return
                (this == WEAPON_BOW && !(clazz == RPGClass.ARCHER || clazz == RPGClass.RANGER || clazz == RPGClass.HUNTER)) ||
                (this == WEAPON_SHEARS && !(clazz == RPGClass.ASSASSIN || clazz == RPGClass.NINJA || clazz == RPGClass.BLADEMASTER)) ||
                (this == WEAPON_AXE && !(clazz == RPGClass.MERCENARY || clazz == RPGClass.SOLDIER || clazz == RPGClass.KNIGHT)) ||
                (this == WEAPON_STICK && !(clazz == RPGClass.MAGICIAN || clazz == RPGClass.WIZARD || clazz == RPGClass.ALCHEMIST));
    }
}
