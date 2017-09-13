package net.wrathofdungeons.dungeonrpg.user;

import net.wrathofdungeons.dungeonrpg.items.ItemCategory;
import org.bukkit.Material;

public enum RPGClass {
    NONE(null,null,null),

    ARCHER("Archer",Material.BOW,ItemCategory.WEAPON_BOW),
    ASSASSIN("Assassin",Material.SHEARS,ItemCategory.WEAPON_SHEARS),
    MERCENARY("Mercenary",Material.IRON_AXE,ItemCategory.WEAPON_AXE),
    MAGICIAN("Magician",Material.STICK,ItemCategory.WEAPON_STICK),

    HUNTER("Hunter",Material.BOW,ItemCategory.WEAPON_BOW),
    RANGER("Ranger",Material.BOW,ItemCategory.WEAPON_BOW),

    NINJA("Ninja",Material.SHEARS,ItemCategory.WEAPON_SHEARS),
    BLADEMASTER("Blade Master",Material.SHEARS,ItemCategory.WEAPON_SHEARS),

    KNIGHT("Knight",Material.IRON_AXE,ItemCategory.WEAPON_AXE),
    SOLDIER("Soldier",Material.IRON_AXE,ItemCategory.WEAPON_AXE),

    WIZARD("Wizard",Material.STICK,ItemCategory.WEAPON_STICK),
    ALCHEMIST("Alchemist",Material.STICK,ItemCategory.WEAPON_STICK);

    private String name;
    private Material icon;
    private ItemCategory weapon;

    RPGClass(String name, Material icon, ItemCategory weapon){
        this.name = name;
        this.icon = icon;
        this.weapon = weapon;
    }

    public String getName() {
        return name;
    }

    public Material getIcon() {
        return icon;
    }

    public ItemCategory getWeapon() {
        return weapon;
    }
}
