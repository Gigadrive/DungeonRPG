package net.wrathofdungeons.dungeonrpg.lootchests;

public enum LootChestTier {
    TIER_1(1,6,0,"I"),
    TIER_2(2,18,1,"II"),
    TIER_3(3,29,2,"III"),
    TIER_4(4,41,3,"IV"),
    TIER_5(5,72,4,"V");

    private int id;
    private int goldNuggetAmount;
    private int itemAmount;
    private String display;

    LootChestTier(int id,int goldNuggetAmount,int itemAmount,String display){
        this.id = id;
        this.goldNuggetAmount = goldNuggetAmount;
        this.itemAmount = itemAmount;
        this.display = display;
    }

    public int getId() {
        return id;
    }

    public int getGoldNuggetAmount() {
        return goldNuggetAmount;
    }

    public int getItemAmount() {
        return itemAmount;
    }

    public String getDisplay() {
        return display;
    }

    public static LootChestTier fromNumber(int tier){
        for(LootChestTier t : values()) if(t.getId() == tier) return t;

        return null;
    }
}
