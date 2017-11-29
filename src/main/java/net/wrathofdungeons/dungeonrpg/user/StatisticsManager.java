package net.wrathofdungeons.dungeonrpg.user;

import net.wrathofdungeons.dungeonrpg.professions.OreLevel;

import java.util.HashMap;

public class StatisticsManager {
    public int blocksWalked = 0;
    public int itemsAwakened = 0;
    public int chestsLooted = 0;

    public int duelsPlayed = 0;
    public int duelsWon = 0;

    public int playersDefeatedInArena = 0;

    public HashMap<Integer,Integer> mobsKilled = new HashMap<Integer,Integer>();
    public HashMap<OreLevel,Integer> oresMined = new HashMap<OreLevel,Integer>();
}
