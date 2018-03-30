package net.wrathofdungeons.dungeonrpg.items.awakening;

import java.util.ArrayList;

public enum AwakeningType {
    STR_BONUS(1, "STR Bonus", "STR", AwakeningValueType.STATIC, -12, +12, AwakeningCategory.WEAPON_ARMOR),
    STA_BONUS(2, "STA Bonus", "STA", AwakeningValueType.STATIC, -12, +12, AwakeningCategory.WEAPON_ARMOR),
    INT_BONUS(3, "INT Bonus", "INT", AwakeningValueType.STATIC, -12, +12, AwakeningCategory.WEAPON_ARMOR),
    DEX_BONUS(4, "DEX Bonus", "DEX", AwakeningValueType.STATIC, -12, +12, AwakeningCategory.WEAPON_ARMOR),
    AGI_BONUS(5, "AGI Bonus", "AGI", AwakeningValueType.STATIC, -12, +12, AwakeningCategory.WEAPON_ARMOR),
    DEFENSE(6, "Defense", "DEF", AwakeningValueType.PERCENTAGE, -28, +12, AwakeningCategory.WEAPON_ARMOR),
    ATTACK_DAMAGE(7, "Attack Damage", "ATK", AwakeningValueType.PERCENTAGE, -25, +45, AwakeningCategory.WEAPON_ARMOR),
    THORNS(8, "Thorns", "THR", AwakeningValueType.PERCENTAGE, -12, +4, AwakeningCategory.WEAPON_ARMOR),
    MELEE_DAMAGE(9, "Melee Damage", "MDMG", AwakeningValueType.PERCENTAGE, -32, +56, AwakeningCategory.WEAPON_ARMOR),
    SKILL_DAMAGE(10, "Skill Damage", "SDMG", AwakeningValueType.PERCENTAGE, -32, +56, AwakeningCategory.WEAPON_ARMOR),
    HP_REGENERATION(11, "HP Regeneration", "HPREGEN", AwakeningValueType.PERCENTAGE, -26, +12, AwakeningCategory.WEAPON_ARMOR),
    MP_REGENERATION(12, "MP Regeneration", "MPREGEN", AwakeningValueType.PERCENTAGE, -26, +12, AwakeningCategory.WEAPON_ARMOR),
    XP_BONUS(13, "XP Bonus", "XP", AwakeningValueType.PERCENTAGE, -45, +45, AwakeningCategory.WEAPON_ARMOR),
    LOOT_BONUS(14, "Loot Bonus", "LOOT", AwakeningValueType.PERCENTAGE, -45, +26, AwakeningCategory.WEAPON_ARMOR),
    HP_LEECH(15, "HP Leech", "LEECH", AwakeningValueType.PERCENTAGE, -12, +6, AwakeningCategory.WEAPON_ARMOR),
    MP_LEECH(16, "MP Leech", "MPLEECH", AwakeningValueType.PERCENTAGE, -12, +6, AwakeningCategory.WEAPON_ARMOR),
    DODGING(17, "Dodging", "DODGE", AwakeningValueType.PERCENTAGE, -14, +19, AwakeningCategory.WEAPON_ARMOR),
    FORTUNE(18, "Fortune", "FOR", AwakeningValueType.PERCENTAGE, -3, +11, AwakeningCategory.WEAPON_ARMOR),
    ADOCH(19, "Critical Damage", "ADOCH", AwakeningValueType.PERCENTAGE, -24, +34, AwakeningCategory.WEAPON_ARMOR),
    MINING_SPEED(20, "Mining Speed", "MSPEED", AwakeningValueType.STATIC, -6, +6, AwakeningCategory.PICKAXE),
    WALK_SPEED(21, "Walk Speed", "SPEED", AwakeningValueType.PERCENTAGE, -21, +14, AwakeningCategory.WEAPON_ARMOR),
    ADDITIONAL_HP(22, "HP", "HP", AwakeningValueType.PERCENTAGE, -76, +81, AwakeningCategory.WEAPON_ARMOR),
    ADDITIONAL_MP(23, "MP", "MP", AwakeningValueType.PERCENTAGE, -76, +81, AwakeningCategory.WEAPON_ARMOR),
    CRIT_CHANCE(24, "Critical Chance", "CRIT", AwakeningValueType.PERCENTAGE, -39, +16, AwakeningCategory.WEAPON_ARMOR);

    private int id;
    private String displayName;
    private String abbreviation;
    private AwakeningValueType valueType;
    private int minimum;
    private int maximum;
    private AwakeningCategory category;

    AwakeningType(int id, String displayName, String abbreviation, AwakeningValueType valueType, int minimum, int maximum, AwakeningCategory category) {
        this.id = id;
        this.displayName = displayName;
        this.abbreviation = abbreviation;
        this.valueType = valueType;
        this.minimum = minimum;
        this.maximum = maximum;
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public AwakeningValueType getValueType() {
        return valueType;
    }

    public int getMaximum() {
        return maximum;
    }

    public int getMinimum() {
        return minimum;
    }

    public AwakeningCategory getCategory() {
        return category;
    }

    public static AwakeningType[] getAwakenings(){
        ArrayList<AwakeningType> a = new ArrayList<AwakeningType>();
        for(AwakeningType awakening : values()) a.add(awakening);

        return a.toArray(new AwakeningType[]{});
    }

    public static AwakeningType[] getAwakenings(AwakeningCategory category){
        ArrayList<AwakeningType> a = new ArrayList<AwakeningType>();
        for(AwakeningType awakening : values()) if(awakening.getCategory() == category) a.add(awakening);

        return a.toArray(new AwakeningType[]{});
    }

    public static AwakeningType fromID(int id){
        for(AwakeningType a : values()) if(a.getId() == id) return a;

        return null;
    }

    public static AwakeningType fromAbbreviation(String abbreviation){
        for(AwakeningType a : values()) if(a.getAbbreviation().equalsIgnoreCase(abbreviation)) return a;

        return null;
    }

    public static AwakeningType fromDisplayName(String displayName){
        for(AwakeningType a : values()) if(a.getDisplayName().equalsIgnoreCase(displayName)) return a;

        return null;
    }
}
