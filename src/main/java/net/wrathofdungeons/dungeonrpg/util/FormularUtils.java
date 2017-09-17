package net.wrathofdungeons.dungeonrpg.util;

public class FormularUtils {
    // http://people.rit.edu/sms6462/mc/exp

    public static double getExpNeededForLevel(int level){
        return 3*(Math.pow(level, 2))+24*level+48;
    }

    public static double getBaseHPOnLevel(int level){
        return 5*(level+1)+32*(level+1);
    }

    public static double getBaseHP(net.wrathofdungeons.dungeonrpg.user.Character c){
        //return (11*Math.pow(c.getLevel(), 2)+48*c.getLevel());
        return getBaseHPOnLevel(c.getLevel());
    }
}
