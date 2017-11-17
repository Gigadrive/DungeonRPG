package net.wrathofdungeons.dungeonrpg.items;

import net.wrathofdungeons.dungeonrpg.mobs.HorseArmor;
import org.bukkit.entity.Horse;

public class MountData {
    private Horse.Variant horseVariant;
    private Horse.Color horseColor;
    private Horse.Style horseStyle;
    private HorseArmor horseArmor;

    public MountData(){
        this.horseVariant = Horse.Variant.HORSE;
        this.horseColor = Horse.Color.BROWN;
        this.horseStyle = Horse.Style.NONE;
        this.horseArmor = HorseArmor.NONE;
    }

    public HorseArmor getHorseArmor() {
        return horseArmor;
    }

    public Horse.Variant getHorseVariant() {
        return horseVariant;
    }

    public Horse.Style getHorseStyle() {
        return horseStyle;
    }

    public Horse.Color getHorseColor() {
        return horseColor;
    }
}
