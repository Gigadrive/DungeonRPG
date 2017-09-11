package net.wrathofdungeons.dungeonrpg.user;

import org.bukkit.Material;

public enum RPGClass {
    NONE(null,null),

    ARCHER("Archer",Material.BOW),
    ASSASSIN("Assassin",Material.SHEARS),
    MERCENARY("Mercenary",Material.IRON_AXE),
    MAGICIAN("Magician",Material.STICK),

    HUNTER("Hunter",Material.BOW),
    RANGER("Ranger",Material.BOW),

    NINJA("Ninja",Material.SHEARS),
    BLADEMASTER("Blade Master",Material.SHEARS),

    KNIGHT("Knight",Material.IRON_AXE),
    SOLDIER("Soldier",Material.IRON_AXE),

    WIZARD("Wizard",Material.STICK),
    ALCHEMIST("Alchemist",Material.STICK);

    private String name;
    private Material icon;

    RPGClass(String name, Material icon){
        this.name = name;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public Material getIcon() {
        return icon;
    }
}
