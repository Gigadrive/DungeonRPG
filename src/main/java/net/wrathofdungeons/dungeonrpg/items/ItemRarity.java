package net.wrathofdungeons.dungeonrpg.items;

import org.bukkit.ChatColor;

public enum ItemRarity {
    NONE(null,ChatColor.WHITE),
    COMMON("Common",ChatColor.GREEN),
    SPECIAL("Special",ChatColor.DARK_AQUA),
    RARE("Rare",ChatColor.BLUE),
    EPIC("Epic",ChatColor.DARK_PURPLE),
    LEGENDARY("Legendary",ChatColor.GOLD);

    private String name;
    private ChatColor color;

    ItemRarity(String name, ChatColor color){
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public ChatColor getColor() {
        return color;
    }
}
