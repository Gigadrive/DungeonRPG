package net.wrathofdungeons.dungeonrpg.professions;

public enum Profession {
    BLACKSMITHING("Blacksmithing",145,0,5,4*64,10),
    CRAFTING("Crafting",58,0,6,6*64,10),
    POTION_MAKING("Potion Making",373,0,7,8*64,10),
    MINING("Mining",257,0,8,10*64,10);

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
