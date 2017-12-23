package net.wrathofdungeons.dungeonrpg.items;

import org.bukkit.ChatColor;

public enum ItemRarity {
    NONE(null,ChatColor.WHITE, new ItemSource[]{}, 0),
    COMMON("Common",ChatColor.GREEN, new ItemSource[]{ItemSource.MOB_DROP_LOWCLASS, ItemSource.MOB_DROP_MIDCLASS, ItemSource.MOB_DROP_HIGHCLASS, ItemSource.MOB_DROP_BOSS, ItemSource.LOOT_CHESTS}, 1),
    SPECIAL("Special",ChatColor.DARK_AQUA, new ItemSource[]{}, 2),
    RARE("Rare",ChatColor.BLUE, new ItemSource[]{ItemSource.MOB_DROP_LOWCLASS, ItemSource.MOB_DROP_MIDCLASS, ItemSource.MOB_DROP_HIGHCLASS, ItemSource.MOB_DROP_BOSS, ItemSource.LOOT_CHESTS}, 2),
    EPIC("Epic",ChatColor.DARK_PURPLE, new ItemSource[]{ItemSource.MOB_DROP_LOWCLASS, ItemSource.MOB_DROP_MIDCLASS, ItemSource.MOB_DROP_HIGHCLASS, ItemSource.MOB_DROP_BOSS, ItemSource.LOOT_CHESTS}, 4),
    LEGENDARY("Legendary",ChatColor.GOLD, new ItemSource[]{ItemSource.MOB_DROP_LOWCLASS, ItemSource.MOB_DROP_MIDCLASS, ItemSource.MOB_DROP_HIGHCLASS, ItemSource.MOB_DROP_BOSS, ItemSource.LOOT_CHESTS}, 5),
    ULTIMATE("Ultimate",ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD.toString(),new ItemSource[]{},7);

    private String name;
    private String color;
    private ItemSource[] sources;
    private int maxAwakenings;

    ItemRarity(String name, ChatColor color, ItemSource[] sources, int maxAwakenings){
        this.name = name;
        this.color = color.toString();
        this.sources = sources;
        this.maxAwakenings = maxAwakenings;
    }

    ItemRarity(String name, String color, ItemSource[] sources, int maxAwakenings){
        this.name = name;
        this.color = color;
        this.sources = sources;
        this.maxAwakenings = maxAwakenings;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public ItemSource[] getSources() {
        return sources;
    }

    public int getMaxAwakenings() {
        return maxAwakenings;
    }
}
