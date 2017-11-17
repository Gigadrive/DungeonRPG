package net.wrathofdungeons.dungeonrpg.items;

import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.items.awakening.Awakening;

public class StoredCustomItem extends CustomItem {
    public StoredCustomItem(CustomItem item){
        super(item.getData(),1,item.isUntradeable(),item.getAwakenings().toArray(new Awakening[]{}),item.getUpgradeValue(),item.getMountData());

        this.amount = 1;
    }

    public StoredCustomItem(CustomItem item, int amount){
        super(item.getData(),amount,item.isUntradeable(),item.getAwakenings().toArray(new Awakening[]{}),item.getUpgradeValue(),item.getMountData());

        this.amount = amount;
    }

    @Override
    public String toString(){
        return DungeonAPI.GSON.toJson((CustomItem)this).replace("\"amount\":" + amount + ",","");
    }

    public void update(){
        super.setAmount(amount);
    }

    public int amount;
}
