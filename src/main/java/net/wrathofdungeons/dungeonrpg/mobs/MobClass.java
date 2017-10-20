package net.wrathofdungeons.dungeonrpg.mobs;

import net.wrathofdungeons.dungeonrpg.items.ItemRarity;

public enum MobClass {

    LOW,
    MID,
    HIGH,
    BOSS;

    public MobClassRarityChance getChance(ItemRarity rarity){
        if(this == LOW){
            if(rarity == ItemRarity.COMMON){
                return new MobClassRarityChance(1, 25);
            } else if(rarity == ItemRarity.RARE){
                return new MobClassRarityChance(1, 135);
            } else if(rarity == ItemRarity.EPIC){
                return new MobClassRarityChance(1, 260);
            } else if(rarity == ItemRarity.LEGENDARY){
                return new MobClassRarityChance(1, 780);
            } /*else if(rarity == ItemRarity.MYTHIC){
                return null;
            } */else {
                return null;
            }
        } else if(this == MID){
            if(rarity == ItemRarity.COMMON){
                return new MobClassRarityChance(1, 60);
            } else if(rarity == ItemRarity.RARE){
                return new MobClassRarityChance(1, 175);
            } else if(rarity == ItemRarity.EPIC){
                return new MobClassRarityChance(1, 320);
            } else if(rarity == ItemRarity.LEGENDARY){
                return new MobClassRarityChance(1, 990);
            } /*else if(rarity == ItemRarity.MYTHIC){
                return null;
            } */else {
                return null;
            }
        } else if(this == HIGH){
            if(rarity == ItemRarity.COMMON){
                return new MobClassRarityChance(1, 60);
            } else if(rarity == ItemRarity.RARE){
                return new MobClassRarityChance(1, 175);
            } else if(rarity == ItemRarity.EPIC){
                return new MobClassRarityChance(1, 320);
            } else if(rarity == ItemRarity.LEGENDARY){
                return new MobClassRarityChance(1, 990);
            } /*else if(rarity == ItemRarity.MYTHIC){
                return new MobClassRarityChance(20, Integer.MAX_VALUE);
            } */else {
                return null;
            }
        } else if(this == BOSS){
            if(rarity == ItemRarity.COMMON){
                return new MobClassRarityChance(15, 45);
            } else if(rarity == ItemRarity.RARE){
                return new MobClassRarityChance(25, 15);
            } else if(rarity == ItemRarity.EPIC){
                return new MobClassRarityChance(15, 10);
            } else if(rarity == ItemRarity.LEGENDARY){
                return new MobClassRarityChance(1, 259);
            } /*else if(rarity == ItemRarity.MYTHIC){
                return new MobClassRarityChance(20, Integer.MAX_VALUE/2);
            } */else {
                return null;
            }
        } else {
            return LOW.getChance(rarity);
        }
    }
}