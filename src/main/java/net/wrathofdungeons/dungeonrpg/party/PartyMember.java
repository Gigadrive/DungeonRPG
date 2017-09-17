package net.wrathofdungeons.dungeonrpg.party;

import org.bukkit.entity.Player;

public class PartyMember {
    public Player p;
    public PartyRank rank;

    public PartyMember(Player p, PartyRank rank){
        this.p = p;
        this.rank = rank;
    }
}
