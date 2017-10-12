package net.wrathofdungeons.dungeonrpg.items;

import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagInt;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.NBTTagString;
import net.wrathofdungeons.dungeonapi.util.ItemUtil;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.items.awakening.Awakening;
import net.wrathofdungeons.dungeonrpg.items.awakening.AwakeningType;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import net.wrathofdungeons.dungeonrpg.util.NBTTypeID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class CustomItem {
    private int dataID;
    private int amount = 1;
    private boolean untradeable = false;
    private ArrayList<Awakening> awakenings;

    public CustomItem(int data){
        this.dataID = data;
        this.amount = 1;
        this.untradeable = getData().isUntradeable();
        this.awakenings = new ArrayList<Awakening>();
    }

    public CustomItem(int data, int amount){
        this.dataID = data;
        this.amount = amount;
        this.untradeable = getData().isUntradeable();
        this.awakenings = new ArrayList<Awakening>();
    }

    public CustomItem(int data, int amount, boolean untradeable){
        this.dataID = data;
        this.amount = amount;
        this.untradeable = untradeable;
        this.awakenings = new ArrayList<Awakening>();
    }

    public CustomItem(int data, int amount, boolean untradeable, Awakening[] awakenings){
        this.dataID = data;
        this.amount = amount;
        this.untradeable = untradeable;
        this.awakenings = new ArrayList<Awakening>();

        if(awakenings != null && awakenings.length > 0){
            this.awakenings = new ArrayList<Awakening>();
            this.awakenings.addAll(Arrays.asList(awakenings));
        }
    }

    public CustomItem(ItemData data){
        this.dataID = data.getId();
        this.amount = 1;
        this.untradeable = getData().isUntradeable();
        this.awakenings = new ArrayList<Awakening>();
    }

    public CustomItem(ItemData data, int amount){
        this.dataID = data.getId();
        this.amount = amount;
        this.untradeable = getData().isUntradeable();
        this.awakenings = new ArrayList<Awakening>();
    }

    public CustomItem(ItemData data, int amount, boolean untradeable){
        this.dataID = data.getId();
        this.amount = amount;
        this.untradeable = untradeable;
        this.awakenings = new ArrayList<Awakening>();
    }

    public CustomItem(ItemData data, int amount, boolean untradeable, Awakening[] awakenings){
        this.dataID = data.getId();
        this.amount = amount;
        this.untradeable = untradeable;
        this.awakenings = new ArrayList<Awakening>();

        if(awakenings != null && awakenings.length > 0){
            this.awakenings.addAll(Arrays.asList(awakenings));
        }
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
                    boolean untradeable = ItemData.getData(id).isUntradeable();

                    if(tag.hasKey("untradeable")) untradeable = Util.convertIntegerToBoolean(tag.getInt("untradeable"));

                    if(tag.hasKey("awakenings")){
                        NBTTagList list = tag.getList("awakenings", NBTTypeID.STRING);

                        ArrayList<Awakening> awakenings = new ArrayList<Awakening>();

                        for(int j = 0; j < list.size(); j++){
                            String s = list.getString(j);

                            awakenings.add(Awakening.fromString(s));
                        }

                        return new CustomItem(id,i.getAmount(),untradeable,awakenings.toArray(new Awakening[]{}));
                    } else {
                        return new CustomItem(id,i.getAmount(),untradeable);
                    }
                }
            }

            return null;
        }
    }

    public void updateAmount(ItemStack i){
        if(fromItemStack(i) == this && this.amount != i.getAmount()) this.amount = i.getAmount();
    }

    public boolean isUntradeable() {
        return untradeable;
    }

    public boolean mayUse(Player p){
        GameUser u = GameUser.getUser(p);

        if(u.getCurrentCharacter() != null){
            if(getData().getCategory() == ItemCategory.WEAPON_BOW && !u.getCurrentCharacter().getRpgClass().matches(RPGClass.ARCHER)){
                return false;
            } else if(getData().getCategory() == ItemCategory.WEAPON_AXE && !u.getCurrentCharacter().getRpgClass().matches(RPGClass.MERCENARY)){
                return false;
            } else if(getData().getCategory() == ItemCategory.WEAPON_SHEARS && !u.getCurrentCharacter().getRpgClass().matches(RPGClass.ASSASSIN)){
                return false;
            } else if(getData().getCategory() == ItemCategory.WEAPON_STICK && !u.getCurrentCharacter().getRpgClass().matches(RPGClass.MAGICIAN)){
                return false;
            } else if(getData().getNeededLevel() > u.getCurrentCharacter().getLevel()){
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public boolean canHoldAwakenings(){
        return getData().getCategory() == ItemCategory.WEAPON_BOW || getData().getCategory() == ItemCategory.WEAPON_STICK || getData().getCategory() == ItemCategory.WEAPON_SHEARS || getData().getCategory() == ItemCategory.WEAPON_AXE || getData().getCategory() == ItemCategory.ARMOR;
    }

    public ArrayList<Awakening> getAwakenings() {
        return awakenings;
    }

    public boolean hasAwakenings(){
        return awakenings != null && awakenings.size() > 0;
    }

    public boolean hasAwakening(AwakeningType type){
        if(getAwakenings() != null){
            for(Awakening a : getAwakenings()){
                if(a.type == type) return true;
            }
        }

        return false;
    }

    public int getAwakeningValue(AwakeningType type){
        if(getAwakenings() != null){
            for(Awakening a : getAwakenings()){
                if(a.type == type) return a.value;
            }
        }

        return 0;
    }

    public void addAwakening(Awakening a){
        if(!hasAwakening(a.type)){
            getAwakenings().add(a);
        }
    }

    public void removeAwakening(AwakeningType type){
        if(hasAwakening(type)){
            Awakening toRemove = null;

            for(Awakening a : getAwakenings()){
                if(a.type == type) toRemove = a;
            }

            getAwakenings().remove(toRemove);
        }
    }

    public int getSellprice(){
        int sellprice = 0;

        if(getData().getCategory() == ItemCategory.WEAPON_BOW || getData().getCategory() == ItemCategory.WEAPON_SHEARS || getData().getCategory() == ItemCategory.WEAPON_STICK || getData().getCategory() == ItemCategory.WEAPON_AXE || getData().getCategory() == ItemCategory.ARMOR){
            sellprice += getData().getNeededLevel()*1.5;

            if(getData().getRarity() == ItemRarity.COMMON){
                sellprice *= 1;
            } else if(getData().getRarity() == ItemRarity.SPECIAL){
                sellprice *= 1;
            } else if(getData().getRarity() == ItemRarity.RARE){
                sellprice *= 1.2;
            } else if(getData().getRarity() == ItemRarity.EPIC){
                sellprice *= 1.6;
            } else if(getData().getRarity() == ItemRarity.LEGENDARY){
                sellprice *= 2.2;
            } else if(getData().getRarity() == ItemRarity.NONE){
                sellprice *= 0;
            }

            // TODO: Add awakenings etc. to calculation
        }

        sellprice *= amount;

        if(sellprice < 0) sellprice = 1;

        return sellprice;
    }

    public int getAwakeningPrice(){
        if(canHoldAwakenings()){
            if(getMaxAwakenings() > getAwakenings().size()){
                int price = 0;

                price += getData().getNeededLevel()*1.6;

                if(getData().getRarity() == ItemRarity.COMMON){
                    price *= 1;
                } else if(getData().getRarity() == ItemRarity.SPECIAL){
                    price *= 1;
                } else if(getData().getRarity() == ItemRarity.RARE){
                    price *= 1.6;
                } else if(getData().getRarity() == ItemRarity.EPIC){
                    price *= 2.1;
                } else if(getData().getRarity() == ItemRarity.LEGENDARY){
                    price *= 2.8;
                } else if(getData().getRarity() == ItemRarity.NONE){
                    price *= 0;
                }

                if(getAwakenings().size() > 0){
                    double j = 1+((getAwakenings().size()*2)*0.1);
                    price *= j;
                }

                return price;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public int getMaxAwakenings(){
        return getData().getRarity().getMaxAwakenings();
    }

    public int getAmount() {
        return amount;
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
                    iL.add("Minimum Level: " + getData().getNeededLevel());
                    iL.add(ChatColor.GOLD + "ATK: " + getData().getAtkMin() + "-" + getData().getAtkMax());

                    if(getData().getDescription() != null) iL.add(" ");
                }

                if(getData().getCategory() == ItemCategory.ARMOR){
                    iL.add("Minimum Level: " + getData().getNeededLevel());
                    iL.add(ChatColor.GOLD + "DEF: " + getData().getDefMin() + "-" + getData().getDefMax());

                    if(getData().getDescription() != null) iL.add(" ");
                }

                if(getData().getCategory() == ItemCategory.FOOD){
                    if(getData().getNeededLevel() > 0) iL.add("Minimum Level: " + getData().getNeededLevel());
                    iL.add(ChatColor.GOLD + "HP Regeneration: " + getData().getFoodRegeneration());

                    if(getData().getDescription() != null) iL.add(" ");
                }

                if(getData().getDescription() != null){
                    for(String s : Util.getWordWrapLore(getData().getDescription())){
                        iL.add(ChatColor.GRAY + s);
                    }
                }

                if(canHoldAwakenings()){
                    if(hasAwakenings()){
                        for(Awakening a : getAwakenings()){
                            if(a.value > 0){
                                // IS POSITIVE
                                if(a.isPercentage){
                                    iL.add(ChatColor.AQUA + a.type.getDisplayName() + ": " + ChatColor.GREEN + "+" + a.value + "%");
                                } else {
                                    iL.add(ChatColor.AQUA + a.type.getDisplayName() + ": " + ChatColor.GREEN + "+" + a.value);
                                }
                            } else if(a.value == 0){
                                // IS NEUTRAL (shouldn't really happen)
                                if(a.isPercentage){
                                    iL.add(ChatColor.AQUA + a.type.getDisplayName() + ": " + ChatColor.YELLOW + "+" + a.value + "%");
                                } else {
                                    iL.add(ChatColor.AQUA + a.type.getDisplayName() + ": " + ChatColor.YELLOW + "+" + a.value);
                                }
                            } else {
                                // IS NEGATIVE
                                if(a.isPercentage){
                                    iL.add(ChatColor.AQUA + a.type.getDisplayName() + ": " + ChatColor.RED + a.value + "%");
                                } else {
                                    iL.add(ChatColor.AQUA + a.type.getDisplayName() + ": " + ChatColor.RED + a.value);
                                }
                            }
                        }
                    } else {
                        iL.add(ChatColor.BLUE + "Awakening available!");
                    }
                }

                if(isUntradeable()){
                    iL.add(ChatColor.RED + "Untradeable");
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

        if(!tag.hasKey("untradeable")) tag.set("untradeable",new NBTTagInt(Util.convertBooleanToInteger(isUntradeable())));

        if(hasAwakenings()){
            if(!tag.hasKey("awakenings")){
                NBTTagList list = new NBTTagList();
                for(Awakening a : getAwakenings()){
                    list.add(new NBTTagString(a.toString()));
                }

                tag.set("awakenings",list);
            }
        }

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

    public boolean isMatchingWeapon(RPGClass rpgClass){
        return getData().isMatchingWeapon(rpgClass);
    }

    public String toString(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
