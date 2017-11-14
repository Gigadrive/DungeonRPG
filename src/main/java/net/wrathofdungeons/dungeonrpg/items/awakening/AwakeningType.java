package net.wrathofdungeons.dungeonrpg.items.awakening;

import java.util.ArrayList;
import java.util.Arrays;

public enum AwakeningType {
    STR_BONUS(1,"STR Bonus","STR",true,false,-7,+7,AwakeningCategory.WEAPON_ARMOR),
    STA_BONUS(2,"STA Bonus","STA",true,false,-7,+7,AwakeningCategory.WEAPON_ARMOR),
    INT_BONUS(3,"INT Bonus","INT",true,false,-7,+7,AwakeningCategory.WEAPON_ARMOR),
    DEX_BONUS(4,"DEX Bonus","DEX",true,false,-7,+7,AwakeningCategory.WEAPON_ARMOR),
    AGI_BONUS(5,"AGI Bonus","AGI",true,false,-7,+7,AwakeningCategory.WEAPON_ARMOR),
    DEFENSE(6,"Defense","DEF",false,true,-28,+34,AwakeningCategory.WEAPON_ARMOR),
    ATTACK_DAMAGE(7,"Attack Damage","ATK",false,true,-25,+45,AwakeningCategory.WEAPON_ARMOR),
    THORNS(8,"Thorns","THR",false,true,-12,+8,AwakeningCategory.WEAPON_ARMOR),
    MELEE_DAMAGE(9,"Melee Damage","MDMG",false,true,-32,+56,AwakeningCategory.WEAPON_ARMOR),
    SKILL_DAMAGE(10,"Skill Damage","SDMG",false,true,-32,+56,AwakeningCategory.WEAPON_ARMOR),
    HP_REGENERATION(11,"HP Regeneration","HPREGEN",false,true,-26,+12,AwakeningCategory.WEAPON_ARMOR),
    MP_REGENERATION(12,"MP Regeneration","MPREGEN",false,true,-26,+12,AwakeningCategory.WEAPON_ARMOR),
    XP_BONUS(13,"XP Bonus","XP",false,true,-45,+45,AwakeningCategory.WEAPON_ARMOR),
    LOOT_BONUS(14,"Loot Bonus","LOOT",false,true,-45,+45,AwakeningCategory.WEAPON_ARMOR),
    HP_LEECH(15,"HP Leech","LEECH",false,true,-12,+6,AwakeningCategory.WEAPON_ARMOR),
    MP_LEECH(16,"MP Leech","MPLEECH",false,true,-12,+6,AwakeningCategory.WEAPON_ARMOR),
    DODGING(17,"Dodging","DODGE",false,true,-14,+19,AwakeningCategory.WEAPON_ARMOR),
    FORTUNE(18,"Fortune","FOR",false,true,-3,+5,AwakeningCategory.WEAPON_ARMOR),
    ADOCH(19,"Critical Damage","ADOCH",false,true,-24,+34,AwakeningCategory.WEAPON_ARMOR),
    MINING_SPEED(20,"Mining Speed","MSPEED",true,false,-6,+6,AwakeningCategory.PICKAXE);

    private int id;
    private String displayName;
    private String abbreviation;
    private boolean mayBeStatic;
    private boolean mayBePercentage;
    private int minimum;
    private int maximum;
    private AwakeningCategory category;

    AwakeningType(int id, String displayName, String abbreviation,boolean mayBeStatic,boolean mayBePercentage,int minimum,int maximum,AwakeningCategory category){
        this.id = id;
        this.displayName = displayName;
        this.abbreviation = abbreviation;
        this.mayBeStatic = mayBeStatic;
        this.mayBePercentage = mayBePercentage;
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

    public boolean mayBePercentage() {
        return mayBePercentage;
    }

    public boolean mayBeStatic() {
        return mayBeStatic;
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
