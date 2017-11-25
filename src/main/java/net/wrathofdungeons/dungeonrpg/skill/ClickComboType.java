package net.wrathofdungeons.dungeonrpg.skill;

import net.wrathofdungeons.dungeonrpg.user.RPGClass;

public enum ClickComboType {
    COMBO_1("RLR","LRL"),
    COMBO_2("RRR","LLL"),
    COMBO_3("RRL","LLR"),
    COMBO_4("RLL","LRR");

    private String comboString;
    private String alternateComboString;

    ClickComboType(String comboString, String alternateComboString){
        this.comboString = comboString;
        this.alternateComboString = alternateComboString;
    }

    public String getComboString() {
        return comboString;
    }

    public String getAlternateComboString() {
        return alternateComboString;
    }

    public static ClickComboType fromComboString(String s){
        for(ClickComboType t : values()) if(t.getComboString().equalsIgnoreCase(s)) return t;
        return null;
    }

    public static ClickComboType fromAlternateComboString(String s){
        for(ClickComboType t : values()) if(t.getAlternateComboString().equalsIgnoreCase(s)) return t;
        return null;
    }

    public String getMatchingComboString(RPGClass rpgClass){
        if(rpgClass.matches(RPGClass.ARCHER)){
            return getAlternateComboString();
        } else {
            return getComboString();
        }
    }
}
