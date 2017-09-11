package net.wrathofdungeons.dungeonrpg.items;

import com.google.gson.Gson;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagInt;
import net.wrathofdungeons.dungeonapi.util.ItemUtil;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;

public class CustomItem {
    public static HashMap<Integer,CustomItem> STORAGE = new HashMap<Integer,CustomItem>();

    private int id;
    private int dataID;

    public CustomItem(ItemData data){
        while(id == 0 || STORAGE.containsKey(id)) id = Util.randomInteger(1,Integer.MAX_VALUE);
        this.dataID = data.getId();
        STORAGE.put(id,this);
    }

    public int getId() {
        return id;
    }

    public ItemData getData() {
        return ItemData.getData(dataID);
    }

    public static CustomItem fromItemStack(ItemStack i){
        if(i == null){
            return null;
        } else {
            net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(i);
            if(nmsItem != null && nmsItem.hasTag()){
                NBTTagCompound tag = nmsItem.getTag();

                if(tag.hasKey("assignedID")){
                    int id = tag.getInt("assignedID");

                    if(STORAGE.containsKey(id)) return STORAGE.get(id);
                }
            }

            return null;
        }
    }

    public ItemStack build(Player p){
        GameUser u = GameUser.getUser(p);

        if(getData() == null){
            return new ItemStack(Material.AIR);
        } else {
            ItemStack i = new ItemStack(getData().getIcon());
            i.setDurability((short)getData().getDurability());
            ItemMeta iM = i.getItemMeta();
            iM.setDisplayName(getData().getRarity().getColor() + getData().getName());
            ArrayList<String> iL = new ArrayList<String>();
            if(getData().getCategory().toString().startsWith("WEAPON_")){
                iL.add(ChatColor.GOLD + "ATK: " + getData().getAtkMin() + "-" + getData().getAtkMax());

                if(getData().getDescription() != null) iL.add(" ");
            }

            if(getData().getCategory() == ItemCategory.ARMOR){
                iL.add(ChatColor.GOLD + "DEF: " + getData().getDefMin() + "-" + getData().getDefMax());

                if(getData().getDescription() != null) iL.add(" ");
            }

            if(getData().getDescription() != null){
                for(String s : Util.getWordWrapLore(getData().getDescription())){
                    iL.add(ChatColor.GRAY + s);
                }
            }

            iM.setLore(iL);
            i.setItemMeta(iM);
            i = ItemUtil.hideFlags(ItemUtil.setUnbreakable(i,true));

            net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(i);
            NBTTagCompound tag = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();
            if(!tag.hasKey("assignedID")) tag.set("assignedID",new NBTTagInt(getId()));

            if(getData().getCategory() == ItemCategory.WEAPON_STICK){
                tag.set("stackProtection", new NBTTagInt(Util.randomInteger(-5000, 5000)));
            }

            nmsItem.setTag(tag);
            i = CraftItemStack.asCraftMirror(nmsItem);

            return i;
        }
    }

    public static CustomItem fromString(String s){
        Gson gson = new Gson();
        CustomItem i = gson.fromJson(s,CustomItem.class);
        return i;
    }

    public String toString(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
