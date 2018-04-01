package net.wrathofdungeons.dungeonrpg.guilds;

public enum GuildRank {
    LEADER(2, "Leader"),
    OFFICER(1, "Officer"),
    MEMBER(0, "Member");

    private int id;
    private String name;

    GuildRank(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
