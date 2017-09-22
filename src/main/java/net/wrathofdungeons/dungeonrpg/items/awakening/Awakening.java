package net.wrathofdungeons.dungeonrpg.items.awakening;

import net.wrathofdungeons.dungeonapi.util.Util;

public class Awakening {
    public AwakeningType type;
    public int value;
    public boolean isPercentage;

    public Awakening(AwakeningType type, int value, boolean isPercentage){
        this.type = type;
        this.value = value;
        this.isPercentage = isPercentage;
    }

    public String toString(){
        return type.getId() + ";" + value + ";" + Util.convertBooleanToInteger(isPercentage);
    }

    public static Awakening fromString(String s){
        String[] ss = s.split(";");

        if(ss.length == 3){
            if(Util.isValidInteger(ss[0])){
                int id = Integer.parseInt(ss[0]);

                if(Util.isValidInteger(ss[1])){
                    int value = Integer.parseInt(ss[1]);

                    if(Util.isValidInteger(ss[2])){
                        boolean isPercentage = Util.convertIntegerToBoolean(Integer.parseInt(ss[2]));
                        AwakeningType type = AwakeningType.fromID(id);

                        if(type != null){
                            return new Awakening(type,value,isPercentage);
                        } else {
                            return null;
                        }
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
