package net.wrathofdungeons.dungeonrpg.items;

import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagInt;
import net.wrathofdungeons.dungeonapi.util.ItemUtil;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;

public class CustomItem {
    private int dataID;
    private int amount = 1;

    public CustomItem(int data){
        this.dataID = data;
    }

    public CustomItem(int data, int amount){
        this.dataID = data;
        this.amount = amount;
    }

    public CustomItem(ItemData data){
        this.dataID = data.getId();
    }

    public CustomItem(ItemData data, int amount){
        this.dataID = data.getId();
        this.amount = amount;
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

                if(tag.hasKey("dataID")){
                    int id = tag.getInt("dataID");

                    return new CustomItem(id,i.getAmount());
                }
            }

            return null;
        }
    }

    public void updateAmount(ItemStack i){
        if(fromItemStack(i) == this && this.amount != i.getAmount()) this.amount = i.getAmount();
    }

    public ItemStack build(Player p){
        if(p != null){
            GameUser u = GameUser.getUser(p);

            if(getData() == null){
                return new ItemStack(Material.AIR);
            } else {
                ItemStack i = new ItemStack(getData().getIcon());
                i.setAmount(amount);
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

                i = assignNBTData(i);

                return i;
            }
        } else {
            if(Bukkit.getOnlinePlayers().size() > 0){
                return build(Iterables.getFirst(Bukkit.getOnlinePlayers(),null));
            } else {
                ItemStack i = new ItemStack(getData().getIcon());
                i.setAmount(amount);
                i.setDurability((short)getData().getDurability());
                i = assignNBTData(i);
                return i;
            }
        }
    }

    public ItemStack assignNBTData(ItemStack i){
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(i);
        NBTTagCompound tag = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();
        if(!tag.hasKey("dataID")) tag.set("dataID",new NBTTagInt(getData().getId()));

        if(getData().getCategory() == ItemCategory.WEAPON_STICK) tag.set("stackProtection", new NBTTagInt(Util.randomInteger(-5000, 5000)));

        nmsItem.setTag(tag);
        i = CraftItemStack.asCraftMirror(nmsItem);

        return i;
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
