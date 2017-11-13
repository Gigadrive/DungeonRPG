package net.wrathofdungeons.dungeonrpg.professions;

public enum Profession {
    BLACKSMITHING("Blacksmithing",145,0,11,7*64,10),
    CRAFTING("Crafting",58,0,4,2*64,10),
    MINING("Mining",257,0,0,32,10);

    private String name;
    private int icon;
    private int iconDurability;
    private int minLevel;
    private int goldCost;
    private int maxLevel;

    Profession(String name, int icon, int iconDurability, int minLevel, int goldCost, int maxLevel){
        this.name = name;
        this.icon = icon;
        this.iconDurability = iconDurability;
        this.minLevel = minLevel;
        this.goldCost = goldCost;
        this.maxLevel = maxLevel;
    }

    public String getName() {
        return name;
    }

    public int getIcon() {
        return icon;
    }

    public int getIconDurability() {
        return iconDurability;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public int getGoldCost() {
        return goldCost;
    }

    public int getMaxLevel() {
        return maxLevel;
    }
}
