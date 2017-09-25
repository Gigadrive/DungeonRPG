package net.wrathofdungeons.dungeonrpg.lootchests;

public enum LootChestTier {
    TIER_1(1,6,"I"),
    TIER_2(2,18,"II"),
    TIER_3(3,29,"III"),
    TIER_4(4,41,"IV"),
    TIER_5(5,72,"V");

    private int id;
    private int goldNuggetAmount;
    private String display;

    LootChestTier(int id,int goldNuggetAmount,String display){
        this.id = id;
        this.goldNuggetAmount = goldNuggetAmount;
        this.display = display;
    }

    public int getId() {
        return id;
    }

    public int getGoldNuggetAmount() {
        return goldNuggetAmount;
    }

    public String getDisplay() {
        return display;
    }

    public static LootChestTier fromNumber(int tier){
        for(LootChestTier t : values()) if(t.getId() == tier) return t;

        return null;
    }
}
