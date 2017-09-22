package net.wrathofdungeons.dungeonrpg.items.awakening;

public enum AwakeningType {
    STR_BONUS(1,"STR Bonus","STR",true,false),
    STA_BONUS(2,"STA Bonus","STA",true,false),
    INT_BONUS(3,"INT Bonus","INT",true,false),
    DEX_BONUS(4,"DEX Bonus","DEX",true,false),
    AGI_BONUS(5,"AGI Bonus","AGI",true,false),
    DEFENSE(6,"Defense","DEF",false,true),
    ATTACK_DAMAGE(7,"Attack Damage","ATK",false,true),
    THORNS(8,"Thorns","THR",false,true),
    MELEE_DAMAGE(9,"Melee Damage","MDMG",false,true),
    SKILL_DAMAGE(10,"Skill Damage","SDMG",false,true),
    HP_REGENERATION(11,"HP Regeneration","HPREGEN",false,true),
    MP_REGENERATION(12,"MP Regeneration","MPREGEN",false,true),
    XP_BONUS(13,"XP Bonus","XP",false,true),
    LOOT_BONUS(14,"Loot Bonus","LOOT",false,true),
    HP_LEECH(15,"HP Leech","LEECH",false,true),
    MP_LEECH(16,"MP Leech","MPLEECH",false,true),
    DODGING(17,"Dodging","DODGE",false,true),
    FORTUNE(18,"Fortune","FOR",false,true),
    ADOCH(19,"Critical Damage","ADOCH",false,true);

    private int id;
    private String displayName;
    private String abbreviation;
    private boolean mayBeStatic;
    private boolean mayBePercentage;

    AwakeningType(int id, String displayName, String abbreviation,boolean mayBeStatic,boolean mayBePercentage){
        this.id = id;
        this.displayName = displayName;
        this.abbreviation = abbreviation;
        this.mayBeStatic = mayBeStatic;
        this.mayBePercentage = mayBePercentage;
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
