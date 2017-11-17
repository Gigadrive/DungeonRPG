package net.wrathofdungeons.dungeonrpg.items;

import com.google.gson.Gson;
import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonrpg.user.Character;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class PlayerInventory {
    public HashMap<Integer,StoredCustomItem> inventory;
    public HashMap<Integer,StoredCustomItem> bank;

    public static PlayerInventory fromInventory(Player p, Character c){
        PlayerInventory inv = new PlayerInventory();

        inv.inventory = new HashMap<Integer,StoredCustomItem>();
        for(int i = 0; i < p.getInventory().getSize(); i++){
            CustomItem item = CustomItem.fromItemStack(p.getInventory().getItem(i));

            if(item != null){
                inv.inventory.put(i,new StoredCustomItem(item,item.getAmount()));
            }
        }

        if(p.getInventory().getHelmet() != null) inv.inventory.put(103,new StoredCustomItem(CustomItem.fromItemStack(p.getInventory().getHelmet()),p.getInventory().getHelmet().getAmount()));
        if(p.getInventory().getChestplate() != null) inv.inventory.put(102,new StoredCustomItem(CustomItem.fromItemStack(p.getInventory().getChestplate()),p.getInventory().getChestplate().getAmount()));
        if(p.getInventory().getLeggings() != null) inv.inventory.put(101,new StoredCustomItem(CustomItem.fromItemStack(p.getInventory().getLeggings()),p.getInventory().getLeggings().getAmount()));
        if(p.getInventory().getBoots() != null) inv.inventory.put(100,new StoredCustomItem(CustomItem.fromItemStack(p.getInventory().getBoots()),p.getInventory().getBoots().getAmount()));

        if(c.getBank() != null){
            inv.bank = new HashMap<Integer,StoredCustomItem>();

            for(int i = 0; i < c.getBank().getSize(); i++){
                CustomItem item = CustomItem.fromItemStack(c.getBank().getItem(i));

                if(item != null){
                    inv.bank.put(i,new StoredCustomItem(item,item.getAmount()));
                }
            }
        }

        return inv;
    }

    public static PlayerInventory fromString(String s){
        Gson gson = DungeonAPI.GSON;
        PlayerInventory inv = gson.fromJson(s,PlayerInventory.class);
        return inv;
    }

    public void loadToPlayer(Player p){
        GameUser u = GameUser.getUser(p);
        Character c = u.getCurrentCharacter();

        if(inventory != null){
            for(int slot : inventory.keySet()){
                StoredCustomItem item = inventory.get(slot);
                item.update();

                if(slot == 103){
                    p.getInventory().setHelmet(item.build(p));
                } else if(slot == 102){
                    p.getInventory().setChestplate(item.build(p));
                } else if(slot == 101){
                    p.getInventory().setLeggings(item.build(p));
                } else if(slot == 100){
                    p.getInventory().setBoots(item.build(p));
                } else {
                    p.getInventory().setItem(slot,item.build(p));
                }
            }
        }

        if(bank != null){
            for(int slot : bank.keySet()){
                StoredCustomItem item = bank.get(slot);
                item.update();

                c.getBank().setItem(slot,item.build(p));
            }
        }
    }

    public String toString(){
        Gson gson = DungeonAPI.GSON;
        return gson.toJson(this);
    }
}
