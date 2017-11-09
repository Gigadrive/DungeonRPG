package net.wrathofdungeons.dungeonrpg.professions;

public enum Profession {
    BLACKSMITHING("Blacksmithing","145:0"),
    CRAFTING("Crafting","58:0"),
    POTION_MAKING("Potion Making","373:0"),
    MINING("Mining","257:0");

    private String name;
    private String icon;

    Profession(String name, String icon){
        this.name = name;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }
}
