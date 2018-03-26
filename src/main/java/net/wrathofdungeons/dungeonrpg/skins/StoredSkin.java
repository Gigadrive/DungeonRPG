package net.wrathofdungeons.dungeonrpg.skins;

import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.user.Character;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;

public class StoredSkin {
    private static ArrayList<StoredSkin> storage = new ArrayList<StoredSkin>();

    public static ArrayList<StoredSkin> getStorage() {
        return storage;
    }

    public static StoredSkin getEqualSkin(Integer[] equipment, String originalURL) {
        for (StoredSkin skin : getStorage())
            if (skin.equipmentRequired.length == equipment.length && Arrays.asList(skin.equipmentRequired).containsAll(Arrays.asList(equipment)))
                if (skin.originalURL.equals(originalURL))
                    return skin;

        return null;
    }

    public String key;
    public Integer[] equipmentRequired;
    public String originalURL;

    public StoredSkin(String key, Integer[] equipmentRequired, String originalURL) {
        this.key = key;
        this.equipmentRequired = equipmentRequired;
        this.originalURL = originalURL;

        final StoredSkin self = this;
        getStorage().add(this);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (getStorage().contains(self))
                    getStorage().remove(self);

            }
        }.runTaskLater(DungeonRPG.getInstance(), 5 * 60 * 20);
    }

    public boolean matchesEquipment(Character character) {
        ArrayList<Integer> a = new ArrayList<Integer>();
        for (CustomItem item : character.getEquipment())
            if (item.getData().hasArmorSkin())
                a.add(item.getData().getId());

        return equipmentRequired.length == a.size() && Arrays.asList(equipmentRequired).containsAll(a);
    }
}
