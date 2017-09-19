package net.wrathofdungeons.dungeonrpg.npc;

import org.bukkit.ChatColor;

public enum  CustomNPCType {
    AWAKENING_SPECIALIST(ChatColor.LIGHT_PURPLE + "Awakening Specialist"),
    BUYING_MERCHANT(ChatColor.LIGHT_PURPLE + "Buying Merchant"),
    QUEST_NPC(ChatColor.DARK_GREEN + "Quest NPC");

    private String defaultName;

    CustomNPCType(String defaultName){
        this.defaultName = defaultName;
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
