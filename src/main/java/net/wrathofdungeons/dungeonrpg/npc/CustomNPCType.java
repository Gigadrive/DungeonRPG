package net.wrathofdungeons.dungeonrpg.npc;

import org.bukkit.ChatColor;

public enum  CustomNPCType {
    AWAKENING_SPECIALIST(ChatColor.LIGHT_PURPLE,"Awakening Specialist"),
    BUYING_MERCHANT(ChatColor.LIGHT_PURPLE,"Buying Merchant"),
    QUEST_NPC(ChatColor.BLUE,"Quest NPC"),
    MERCHANT(ChatColor.DARK_GREEN,"Merchant"),
    DUNGEON_KEY_MASTER(ChatColor.LIGHT_PURPLE,"Dungeon Key Master"),
    GUILD_MASTER(ChatColor.LIGHT_PURPLE,"Guild Master"),
    GUILD_BANK_MANAGER(ChatColor.LIGHT_PURPLE,"Guild Bank Manager");

    private ChatColor color;
    private String defaultName;

    CustomNPCType(ChatColor color, String defaultName){
        this.color = color;
        this.defaultName = defaultName;
    }

    public ChatColor getColor() {
        return color;
    }

    public String getDefaultName() {
        return defaultName;
    }

    public static CustomNPCType fromName(String s){
        for(CustomNPCType type : values()){
            if(type.toString().equalsIgnoreCase(s)) return type;
        }

        return null;
    }
}
