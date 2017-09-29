package net.wrathofdungeons.dungeonrpg.npc;

import java.util.ArrayList;

public class MerchantOffer {
    public int slot;
    public int itemToBuy;
    public int amount;
    public int moneyCost;
    public ArrayList<MerchantOfferCost> itemCost = new ArrayList<MerchantOfferCost>();
}
