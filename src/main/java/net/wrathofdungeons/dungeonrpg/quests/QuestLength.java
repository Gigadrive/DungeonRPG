package net.wrathofdungeons.dungeonrpg.quests;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public enum QuestLength {
    SHORT(1,"Short",ChatColor.GREEN),
    MEDIUM(5,"Medium",ChatColor.GOLD),
    LONG(10,"Long",ChatColor.RED),
    VERY_LONG(15,"Very Long",ChatColor.DARK_RED);

    private int minLength;
    private String name;
    private ChatColor color;

    QuestLength(int minLength, String name, ChatColor color){
        this.minLength = minLength;
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public ChatColor getColor() {
        return color;
    }

    public int getMinLength() {
        return minLength;
    }

    public static QuestLength fromStageAmount(int stageAmount){
        ArrayList<QuestLength> a = new ArrayList<QuestLength>();
        a.addAll(Arrays.asList(values()));
        Collections.reverse(a);

        for(QuestLength l : a) if(stageAmount >= l.getMinLength()) return l;

        return SHORT;
    }
}
