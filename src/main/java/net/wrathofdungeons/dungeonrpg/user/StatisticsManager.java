package net.wrathofdungeons.dungeonrpg.user;

import java.util.HashMap;

public class StatisticsManager {
    public int blocksWalked = 0;
    public int itemsAwakened = 0;
    public int chestsLooted = 0;

    public HashMap<Integer,Integer> mobsKilled = new HashMap<Integer,Integer>();
}
