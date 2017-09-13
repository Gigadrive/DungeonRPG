package net.wrathofdungeons.dungeonrpg.items;

import org.bukkit.ChatColor;

public enum ItemRarity {
    NONE(null,ChatColor.WHITE, new ItemSource[]{}),
    COMMON("Common",ChatColor.GREEN, new ItemSource[]{ItemSource.MOB_DROP_LOWCLASS, ItemSource.MOB_DROP_MIDCLASS, ItemSource.LOOT_CHESTS}),
    SPECIAL("Special",ChatColor.DARK_AQUA, new ItemSource[]{}),
    RARE("Rare",ChatColor.BLUE, new ItemSource[]{ItemSource.MOB_DROP_MIDCLASS, ItemSource.MOB_DROP_HIGHCLASS, ItemSource.LOOT_CHESTS}),
    EPIC("Epic",ChatColor.DARK_PURPLE, new ItemSource[]{ItemSource.MOB_DROP_MIDCLASS, ItemSource.MOB_DROP_HIGHCLASS, ItemSource.MOB_DROP_BOSS, ItemSource.LOOT_CHESTS}),
    LEGENDARY("Legendary",ChatColor.GOLD, new ItemSource[]{ItemSource.MOB_DROP_HIGHCLASS, ItemSource.MOB_DROP_BOSS, ItemSource.LOOT_CHESTS});

    private String name;
    private ChatColor color;
    private ItemSource[] sources;

    ItemRarity(String name, ChatColor color, ItemSource[] sources){
        this.name = name;
        this.color = color;
        this.sources = sources;
    }

    public String getName() {
        return name;
    }

    public ChatColor getColor() {
        return color;
    }

    public ItemSource[] getSources() {
        return sources;
    }
}
