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
    public CustomItem helmet;
    public CustomItem chestplate;
    public CustomItem leggings;
    public CustomItem boots;

    public CustomItem slot0;
    public CustomItem slot1;
    public CustomItem slot2;
    public CustomItem slot3;
    public CustomItem slot4;
    public CustomItem slot5;
    public CustomItem slot6;
    public CustomItem slot7;
    public CustomItem slot8;
    public CustomItem slot9;
    public CustomItem slot10;
    public CustomItem slot11;
    public CustomItem slot12;
    public CustomItem slot13;
    public CustomItem slot14;
    public CustomItem slot15;
    public CustomItem slot16;
    public CustomItem slot17;
    public CustomItem slot18;
    public CustomItem slot19;
    public CustomItem slot20;
    public CustomItem slot21;
    public CustomItem slot22;
    public CustomItem slot23;
    public CustomItem slot24;
    public CustomItem slot25;
    public CustomItem slot26;
    public CustomItem slot27;
    public CustomItem slot28;
    public CustomItem slot29;
    public CustomItem slot30;
    public CustomItem slot31;
    public CustomItem slot32;
    public CustomItem slot33;
    public CustomItem slot34;
    public CustomItem slot35;

    public HashMap<Integer,CustomItem> bank;

    public static PlayerInventory fromInventory(Player p, Character c){
        PlayerInventory inv = new PlayerInventory();

        inv.helmet = CustomItem.fromItemStack(p.getInventory().getHelmet());
        inv.chestplate = CustomItem.fromItemStack(p.getInventory().getChestplate());
        inv.leggings = CustomItem.fromItemStack(p.getInventory().getLeggings());
        inv.boots = CustomItem.fromItemStack(p.getInventory().getBoots());

        inv.slot0 = CustomItem.fromItemStack(p.getInventory().getItem(0));
        inv.slot1 = CustomItem.fromItemStack(p.getInventory().getItem(1));
        inv.slot2 = CustomItem.fromItemStack(p.getInventory().getItem(2));
        inv.slot3 = CustomItem.fromItemStack(p.getInventory().getItem(3));
        inv.slot4 = CustomItem.fromItemStack(p.getInventory().getItem(4));
        inv.slot5 = CustomItem.fromItemStack(p.getInventory().getItem(5));
        inv.slot6 = CustomItem.fromItemStack(p.getInventory().getItem(6));
        inv.slot7 = CustomItem.fromItemStack(p.getInventory().getItem(7));
        inv.slot8 = CustomItem.fromItemStack(p.getInventory().getItem(8));
        inv.slot9 = CustomItem.fromItemStack(p.getInventory().getItem(9));
        inv.slot10 = CustomItem.fromItemStack(p.getInventory().getItem(10));
        inv.slot11 = CustomItem.fromItemStack(p.getInventory().getItem(11));
        inv.slot12 = CustomItem.fromItemStack(p.getInventory().getItem(12));
        inv.slot13 = CustomItem.fromItemStack(p.getInventory().getItem(13));
        inv.slot14 = CustomItem.fromItemStack(p.getInventory().getItem(14));
        inv.slot15 = CustomItem.fromItemStack(p.getInventory().getItem(15));
        inv.slot16 = CustomItem.fromItemStack(p.getInventory().getItem(16));
        inv.slot17 = CustomItem.fromItemStack(p.getInventory().getItem(17));
        inv.slot18 = CustomItem.fromItemStack(p.getInventory().getItem(18));
        inv.slot19 = CustomItem.fromItemStack(p.getInventory().getItem(19));
        inv.slot20 = CustomItem.fromItemStack(p.getInventory().getItem(20));
        inv.slot21 = CustomItem.fromItemStack(p.getInventory().getItem(21));
        inv.slot22 = CustomItem.fromItemStack(p.getInventory().getItem(22));
        inv.slot23 = CustomItem.fromItemStack(p.getInventory().getItem(23));
        inv.slot24 = CustomItem.fromItemStack(p.getInventory().getItem(24));
        inv.slot25 = CustomItem.fromItemStack(p.getInventory().getItem(25));
        inv.slot26 = CustomItem.fromItemStack(p.getInventory().getItem(26));
        inv.slot27 = CustomItem.fromItemStack(p.getInventory().getItem(27));
        inv.slot28 = CustomItem.fromItemStack(p.getInventory().getItem(28));
        inv.slot29 = CustomItem.fromItemStack(p.getInventory().getItem(29));
        inv.slot30 = CustomItem.fromItemStack(p.getInventory().getItem(30));
        inv.slot31 = CustomItem.fromItemStack(p.getInventory().getItem(31));
        inv.slot32 = CustomItem.fromItemStack(p.getInventory().getItem(32));
        inv.slot33 = CustomItem.fromItemStack(p.getInventory().getItem(33));
        inv.slot34 = CustomItem.fromItemStack(p.getInventory().getItem(34));
        inv.slot35 = CustomItem.fromItemStack(p.getInventory().getItem(35));

        if(c.getBank() != null){
            inv.bank = new HashMap<Integer,CustomItem>();

            for(int i = 0; i < c.getBank().getSize(); i++){
                CustomItem item = CustomItem.fromItemStack(c.getBank().getItem(i));

                if(item != null){
                    inv.bank.put(i,item);
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

        if(helmet != null) p.getInventory().setHelmet(helmet.build(p));
        if(chestplate != null) p.getInventory().setChestplate(chestplate.build(p));
        if(leggings != null) p.getInventory().setLeggings(leggings.build(p));
        if(boots != null) p.getInventory().setBoots(boots.build(p));

        if(slot0 != null) p.getInventory().setItem(0,slot0.build(p));
        if(slot1 != null) p.getInventory().setItem(1,slot1.build(p));
        if(slot2 != null) p.getInventory().setItem(2,slot2.build(p));
        if(slot3 != null) p.getInventory().setItem(3,slot3.build(p));
        if(slot4 != null) p.getInventory().setItem(4,slot4.build(p));
        if(slot5 != null) p.getInventory().setItem(5,slot5.build(p));
        if(slot6 != null) p.getInventory().setItem(6,slot6.build(p));
        if(slot7 != null) p.getInventory().setItem(7,slot7.build(p));
        if(slot8 != null) p.getInventory().setItem(8,slot8.build(p));
        if(slot9 != null) p.getInventory().setItem(9,slot9.build(p));
        if(slot10 != null) p.getInventory().setItem(10,slot10.build(p));
        if(slot11 != null) p.getInventory().setItem(11,slot11.build(p));
        if(slot12 != null) p.getInventory().setItem(12,slot12.build(p));
        if(slot13 != null) p.getInventory().setItem(13,slot13.build(p));
        if(slot14 != null) p.getInventory().setItem(14,slot14.build(p));
        if(slot15 != null) p.getInventory().setItem(15,slot15.build(p));
        if(slot16 != null) p.getInventory().setItem(16,slot16.build(p));
        if(slot17 != null) p.getInventory().setItem(17,slot17.build(p));
        if(slot18 != null) p.getInventory().setItem(18,slot18.build(p));
        if(slot19 != null) p.getInventory().setItem(19,slot19.build(p));
        if(slot20 != null) p.getInventory().setItem(20,slot20.build(p));
        if(slot21 != null) p.getInventory().setItem(21,slot21.build(p));
        if(slot22 != null) p.getInventory().setItem(22,slot22.build(p));
        if(slot23 != null) p.getInventory().setItem(23,slot23.build(p));
        if(slot24 != null) p.getInventory().setItem(24,slot24.build(p));
        if(slot25 != null) p.getInventory().setItem(25,slot25.build(p));
        if(slot26 != null) p.getInventory().setItem(26,slot26.build(p));
        if(slot27 != null) p.getInventory().setItem(27,slot27.build(p));
        if(slot28 != null) p.getInventory().setItem(28,slot28.build(p));
        if(slot29 != null) p.getInventory().setItem(29,slot29.build(p));
        if(slot30 != null) p.getInventory().setItem(30,slot30.build(p));
        if(slot31 != null) p.getInventory().setItem(31,slot31.build(p));
        if(slot32 != null) p.getInventory().setItem(32,slot32.build(p));
        if(slot33 != null) p.getInventory().setItem(33,slot33.build(p));
        if(slot34 != null) p.getInventory().setItem(34,slot34.build(p));
        if(slot35 != null) p.getInventory().setItem(35,slot35.build(p));

        if(bank != null){
            for(int slot : bank.keySet()){
                CustomItem item = bank.get(slot);

                c.getBank().setItem(slot,item.build(p));
            }
        }
    }

    public String toString(){
        Gson gson = DungeonAPI.GSON;
        return gson.toJson(this);
    }
}
