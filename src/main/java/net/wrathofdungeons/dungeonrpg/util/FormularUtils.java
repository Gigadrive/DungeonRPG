package net.wrathofdungeons.dungeonrpg.util;

import net.wrathofdungeons.dungeonrpg.mobs.MobClass;
import net.wrathofdungeons.dungeonrpg.professions.Profession;

public class FormularUtils {
    // http://people.rit.edu/sms6462/mc/exp

    public static double getExpNeededForLevel(int level){
        //return 3*(Math.pow(level, 2))+24*level+48;
        return 6*Math.pow(level,2.25)+36*level+64;
    }

    public static double getBaseHPOnLevel(int level){
        //return 5*(level+1)+32*(level+1);
        //return 16*((level+1)/2)+2*24;
        return 16*(level+1)+3*24;
    }

    public static double getBaseHP(net.wrathofdungeons.dungeonrpg.user.Character c){
        //return (11*Math.pow(c.getLevel(), 2)+48*c.getLevel());
        return getBaseHPOnLevel(c.getLevel());
    }

    public static double getMobHP(MobClass mobClass, int level){
        if(mobClass == MobClass.LOW){
            //return (level*1.5)+48*((level+1)/2); // 100%
            return (Math.pow(level,2.25))+18*((level+2)); // 100%
        } else if(mobClass == MobClass.MID){
            return getMobHP(MobClass.LOW,level)*1.2; // 120%
        } else if(mobClass == MobClass.HIGH){
            return getMobHP(MobClass.LOW,level)*1.5; // 150%
        } else if(mobClass == MobClass.BOSS){
            return getMobHP(MobClass.LOW,level)*16; // 1600%
        } else {
            return getMobHP(MobClass.LOW,level);
        }
    }

    public static double getMobATK(MobClass mobClass, int level){
        if(mobClass == MobClass.LOW){
            return 16*((level+1)/2)+2; // 100%
        } else if(mobClass == MobClass.MID){
            return getMobATK(MobClass.LOW,level)*1.2; // 120%
        } else if(mobClass == MobClass.HIGH){
            return getMobATK(MobClass.LOW,level)*1.5; // 150%
        } else if(mobClass == MobClass.BOSS){
            return getMobATK(MobClass.LOW,level)*1.75; // 175%
        } else {
            return getMobATK(MobClass.LOW,level);
        }
    }

    public static double getMobEXP(MobClass mobClass, int level){
        if(mobClass == MobClass.LOW){
            return getMobEXP(MobClass.MID,level)*0.8; // 80%
        } else if(mobClass == MobClass.MID){
            //return level*3;
            return 0.5*Math.pow(level,1.5)+32; // 100%
        } else if(mobClass == MobClass.HIGH){
            return getMobEXP(MobClass.MID,level)*1.2; // 120%
        } else if(mobClass == MobClass.BOSS){
            return getMobEXP(MobClass.MID,level)*2.1; // 210%
        } else {
            return getMobEXP(MobClass.MID,level);
        }
    }

    public static double getNeededProfessionEXP(Profession profession, int level){
        if(profession == Profession.BLACKSMITHING){
            return 4*Math.pow(level,2.25)*level+64;
        } else if(profession == Profession.CRAFTING){
            return getNeededProfessionEXP(Profession.BLACKSMITHING,level);
        } else if(profession == Profession.MINING){
            return getNeededProfessionEXP(Profession.BLACKSMITHING,level);
        } else {
            return getNeededProfessionEXP(Profession.BLACKSMITHING,level);
        }
    }

    public static int getNeededSkillUsesForLevel(int level){
        return ((int) ((Math.pow(level, 2.45)) * level + 94));
    }

    public static int calculateSkillPointsForLevel(int level){
        return level;
    }

    public static double getMobRegen(MobClass mobClass, int level){
        return level-1; // TODO: Add proper calculation
    }
}
