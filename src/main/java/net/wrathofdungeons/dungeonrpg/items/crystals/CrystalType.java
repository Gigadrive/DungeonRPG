package net.wrathofdungeons.dungeonrpg.items.crystals;

import net.wrathofdungeons.dungeonrpg.items.awakening.Awakening;
import net.wrathofdungeons.dungeonrpg.items.awakening.AwakeningType;
import org.bukkit.ChatColor;

public enum CrystalType {
    RED_D(ChatColor.RED, new Awakening[]{new Awakening(AwakeningType.STR_BONUS,2,false)},new Awakening[]{new Awakening(AwakeningType.ADDITIONAL_HP,2,true)}),
    RED_C(ChatColor.RED, new Awakening[]{new Awakening(AwakeningType.STR_BONUS,3,false),new Awakening(AwakeningType.ADOCH,1,true)},new Awakening[]{new Awakening(AwakeningType.ADDITIONAL_HP,3,true)}),
    RED_B(ChatColor.RED, new Awakening[]{new Awakening(AwakeningType.STR_BONUS,4,false),new Awakening(AwakeningType.ADOCH,1,true)},new Awakening[]{new Awakening(AwakeningType.ADDITIONAL_HP,4,true)}),
    RED_A(ChatColor.RED, new Awakening[]{new Awakening(AwakeningType.STR_BONUS,7,false),new Awakening(AwakeningType.ADOCH,2,true)},new Awakening[]{new Awakening(AwakeningType.ADDITIONAL_HP,7,true)}),

    BLUE_D(ChatColor.BLUE, new Awakening[]{new Awakening(AwakeningType.INT_BONUS,2,false)},new Awakening[]{new Awakening(AwakeningType.ADDITIONAL_MP,2,true)}),
    BLUE_C(ChatColor.BLUE, new Awakening[]{new Awakening(AwakeningType.INT_BONUS,3,false),new Awakening(AwakeningType.SKILL_DAMAGE,1,true)},new Awakening[]{new Awakening(AwakeningType.ADDITIONAL_MP,3,true)}),
    BLUE_B(ChatColor.BLUE, new Awakening[]{new Awakening(AwakeningType.INT_BONUS,4,false),new Awakening(AwakeningType.SKILL_DAMAGE,1,true)},new Awakening[]{new Awakening(AwakeningType.ADDITIONAL_MP,4,true)}),
    BLUE_A(ChatColor.BLUE, new Awakening[]{new Awakening(AwakeningType.INT_BONUS,7,false),new Awakening(AwakeningType.SKILL_DAMAGE,2,true)},new Awakening[]{new Awakening(AwakeningType.ADDITIONAL_MP,7,true)}),

    GREEN_D(ChatColor.DARK_GREEN, new Awakening[]{new Awakening(AwakeningType.DEX_BONUS,2,false)},new Awakening[]{new Awakening(AwakeningType.THORNS,2,true)}),
    GREEN_C(ChatColor.DARK_GREEN, new Awakening[]{new Awakening(AwakeningType.DEX_BONUS,3,false),new Awakening(AwakeningType.CRIT_CHANCE,1,true)},new Awakening[]{new Awakening(AwakeningType.THORNS,3,true)}),
    GREEN_B(ChatColor.DARK_GREEN, new Awakening[]{new Awakening(AwakeningType.DEX_BONUS,4,false),new Awakening(AwakeningType.CRIT_CHANCE,1,true)},new Awakening[]{new Awakening(AwakeningType.THORNS,4,true)}),
    GREEN_A(ChatColor.DARK_GREEN, new Awakening[]{new Awakening(AwakeningType.DEX_BONUS,7,false),new Awakening(AwakeningType.CRIT_CHANCE,2,true)},new Awakening[]{new Awakening(AwakeningType.THORNS,7,true)}),

    YELLOW_D(ChatColor.YELLOW, new Awakening[]{new Awakening(AwakeningType.STA_BONUS,2,false)},new Awakening[]{new Awakening(AwakeningType.HP_LEECH,2,true)}),
    YELLOW_C(ChatColor.YELLOW, new Awakening[]{new Awakening(AwakeningType.STA_BONUS,3,false),new Awakening(AwakeningType.DEFENSE,1,true)},new Awakening[]{new Awakening(AwakeningType.HP_LEECH,3,true)}),
    YELLOW_B(ChatColor.YELLOW, new Awakening[]{new Awakening(AwakeningType.STA_BONUS,4,false),new Awakening(AwakeningType.DEFENSE,1,true)},new Awakening[]{new Awakening(AwakeningType.HP_LEECH,4,true)}),
    YELLOW_A(ChatColor.YELLOW, new Awakening[]{new Awakening(AwakeningType.STA_BONUS,7,false),new Awakening(AwakeningType.DEFENSE,2,true)},new Awakening[]{new Awakening(AwakeningType.HP_LEECH,7,true)}),

    WHITE_D(ChatColor.WHITE, new Awakening[]{new Awakening(AwakeningType.AGI_BONUS,2,false)},new Awakening[]{new Awakening(AwakeningType.HP_LEECH,2,true)}),
    WHITE_C(ChatColor.WHITE, new Awakening[]{new Awakening(AwakeningType.AGI_BONUS,3,false),new Awakening(AwakeningType.WALK_SPEED,1,true)},new Awakening[]{new Awakening(AwakeningType.DODGING,3,true)}),
    WHITE_B(ChatColor.WHITE, new Awakening[]{new Awakening(AwakeningType.AGI_BONUS,4,false),new Awakening(AwakeningType.WALK_SPEED,1,true)},new Awakening[]{new Awakening(AwakeningType.DODGING,4,true)}),
    WHITE_A(ChatColor.WHITE, new Awakening[]{new Awakening(AwakeningType.AGI_BONUS,7,false),new Awakening(AwakeningType.WALK_SPEED,2,true)},new Awakening[]{new Awakening(AwakeningType.DODGING,7,true)});

    private ChatColor color;
    private Awakening[] effectsWeapon;
    private Awakening[] effectsArmor;

    CrystalType(ChatColor color, Awakening[] effectsWeapon, Awakening[] effectsArmor){
        this.color = color;
        this.effectsWeapon = effectsWeapon;
        this.effectsArmor = effectsArmor;
    }

    public ChatColor getColor() {
        return color;
    }

    public Awakening[] getEffectsOnWeapons() {
        return effectsWeapon;
    }

    public Awakening[] getEffectsOnArmor() {
        return effectsArmor;
    }

    public static CrystalType fromColor(ChatColor color){
        for(CrystalType type : values())
            if(type.getColor() == color)
                return type;

        return null;
    }
}
