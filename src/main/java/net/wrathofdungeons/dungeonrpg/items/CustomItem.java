package net.wrathofdungeons.dungeonrpg.items;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagInt;
import net.wrathofdungeons.dungeonapi.util.ItemUtil;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
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
    private ItemData data;

    public CustomItem(ItemData data){
        while(id == 0 || STORAGE.containsKey(id)) id = Util.randomInteger(1,Integer.MAX_VALUE);
        this.data = data;
        STORAGE.put(id,this);
    }

    public int getId() {
        return id;
    }

    public ItemData getData() {
        return data;
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

        if(data == null){
            return new ItemStack(Material.AIR);
        } else {
            ItemStack i = new ItemStack(getData().getIcon());
            i.setDurability((short)getData().getDurability());
            ItemMeta iM = i.getItemMeta();
            iM.setDisplayName(getData().getName());
            ArrayList<String> iL = new ArrayList<String>();
            iL.add(getData().getCategory().toString());
            iM.setLore(iL);
            i.setItemMeta(iM);
            i = ItemUtil.hideFlags(ItemUtil.setUnbreakable(i,true));

            net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(i);
            NBTTagCompound tag = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();
            if(!tag.hasKey("assignedID")) tag.set("assignedID",new NBTTagInt(getId()));

            if(data.getCategory() == ItemCategory.WEAPON_STICK){
                tag.set("stackProtection", new NBTTagInt(Util.randomInteger(-5000, 5000)));
            }

            return i;
        }
    }
}
