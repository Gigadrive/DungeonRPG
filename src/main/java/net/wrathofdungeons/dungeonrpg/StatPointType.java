package net.wrathofdungeons.dungeonrpg;

import org.bukkit.ChatColor;

public enum StatPointType {
    STRENGTH(ChatColor.RED,"Strength","STR"),
    STAMINA(ChatColor.YELLOW,"Stamina","STA"),
    INTELLIGENCE(ChatColor.BLUE,"Intelligence","INT"),
    DEXTERITY(ChatColor.DARK_GREEN,"Dexterity","DEX"),
    AGILITY(ChatColor.WHITE,"Agility","AGI");

    private ChatColor color;
    private String name;
    private String abbreviation;

    StatPointType(ChatColor color, String name, String abbreviation){
        this.color = color;
        this.name = name;
        this.abbreviation = abbreviation;
    }

    public ChatColor getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public static StatPointType fromAbbreviation(String abbreviation){
        for(StatPointType s : values())
            if(s.getAbbreviation().equalsIgnoreCase(abbreviation)) return s;

        return null;
    }
}
