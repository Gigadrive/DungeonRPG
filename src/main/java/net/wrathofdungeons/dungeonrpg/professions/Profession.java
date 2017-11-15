package net.wrathofdungeons.dungeonrpg.professions;

public enum Profession {
    MINING("Mining","Mine ores with your pickaxe to retrieve materials that may be used to craft new weapons, armor and other items.",257,0,0,32,10),
    CRAFTING("Crafting","",58,0,4,2*64,10),
    BLACKSMITHING("Blacksmithing","",145,0,11,7*64,10);

    private String name;
    private String description;
    private int icon;
    private int iconDurability;
    private int minLevel;
    private int goldCost;
    private int maxLevel;

    Profession(String name, String description, int icon, int iconDurability, int minLevel, int goldCost, int maxLevel){
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.iconDurability = iconDurability;
        this.minLevel = minLevel;
        this.goldCost = goldCost;
        this.maxLevel = maxLevel;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
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
