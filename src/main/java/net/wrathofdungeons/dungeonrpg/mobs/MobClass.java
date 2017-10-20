package net.wrathofdungeons.dungeonrpg.mobs;

import net.wrathofdungeons.dungeonrpg.items.ItemRarity;

public enum MobClass {

    LOW,
    MID,
    HIGH,
    BOSS;

    public MobClassRarityChance getChance(ItemRarity rarity){
        if(rarity == ItemRarity.COMMON){
            return new MobClassRarityChance(1, 135);
        } else if(rarity == ItemRarity.RARE){
            return new MobClassRarityChance(1, 260);
        } else if(rarity == ItemRarity.EPIC){
            return new MobClassRarityChance(1, 780);
        } else if(rarity == ItemRarity.LEGENDARY){
            return new MobClassRarityChance(1, 2090);
        } /*else if(rarity == ItemRarity.MYTHIC){
                return null;
        } */else {
            return null;
        }
    }
}