package net.wrathofdungeons.dungeonrpg.guilds;

public enum GuildRank {
    LEADER(2),
    OFFICER(1),
    MEMBER(0);

    private int id;

    GuildRank(int id){
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
