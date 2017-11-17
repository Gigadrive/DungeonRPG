package net.wrathofdungeons.dungeonrpg.items;

import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagInt;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.NBTTagString;
import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.util.ChatIcons;
import net.wrathofdungeons.dungeonapi.util.ItemUtil;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.items.awakening.Awakening;
import net.wrathofdungeons.dungeonrpg.items.awakening.AwakeningType;
import net.wrathofdungeons.dungeonrpg.professions.Profession;
import net.wrathofdungeons.dungeonrpg.user.Character;
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
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class CustomItem {
    private int dataID;
    private transient int amount = 1;
    private boolean untradeable = false;
    private ArrayList<Awakening> awakenings;
    private int upgradeValue = 0;
    private MountData mountData = null;

    public CustomItem(int data){
        this.dataID = data;
        this.amount = 1;
        this.untradeable = getData().isUntradeable();
        this.awakenings = new ArrayList<Awakening>();

        if(getData() != null && getData().getCategory() == ItemCategory.MOUNT && this.mountData == null) this.mountData = new MountData();
    }

    public CustomItem(int data, int amount){
        this.dataID = data;
        this.amount = amount;
        this.untradeable = getData().isUntradeable();
        this.awakenings = new ArrayList<Awakening>();

        if(getData() != null && getData().getCategory() == ItemCategory.MOUNT && this.mountData == null) this.mountData = new MountData();
    }

    public CustomItem(int data, int amount, boolean untradeable){
        this.dataID = data;
        this.amount = amount;
        this.untradeable = untradeable;
        this.awakenings = new ArrayList<Awakening>();

        if(getData() != null && getData().getCategory() == ItemCategory.MOUNT && this.mountData == null) this.mountData = new MountData();
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

        if(getData() != null && getData().getCategory() == ItemCategory.MOUNT && this.mountData == null) this.mountData = new MountData();
    }

    public CustomItem(int data, int amount, boolean untradeable, Awakening[] awakenings, int upgradeValue){
        this.dataID = data;
        this.amount = amount;
        this.untradeable = untradeable;
        this.awakenings = new ArrayList<Awakening>();
        this.upgradeValue = upgradeValue;

        if(awakenings != null && awakenings.length > 0){
            this.awakenings = new ArrayList<Awakening>();
            this.awakenings.addAll(Arrays.asList(awakenings));
        }

        if(getData() != null && getData().getCategory() == ItemCategory.MOUNT && this.mountData == null) this.mountData = new MountData();
    }

    public CustomItem(int data, int amount, boolean untradeable, Awakening[] awakenings, int upgradeValue, MountData mountData){
        this.dataID = data;
        this.amount = amount;
        this.untradeable = untradeable;
        this.awakenings = new ArrayList<Awakening>();
        this.upgradeValue = upgradeValue;
        this.mountData = mountData;

        if(awakenings != null && awakenings.length > 0){
            this.awakenings = new ArrayList<Awakening>();
            this.awakenings.addAll(Arrays.asList(awakenings));
        }

        if(getData() != null && getData().getCategory() == ItemCategory.MOUNT && this.mountData == null) this.mountData = new MountData();
    }

    public CustomItem(ItemData data){
        this.dataID = data.getId();
        this.amount = 1;
        this.untradeable = getData().isUntradeable();
        this.awakenings = new ArrayList<Awakening>();

        if(getData() != null && getData().getCategory() == ItemCategory.MOUNT && this.mountData == null) this.mountData = new MountData();
    }

    public CustomItem(ItemData data, int amount){
        this.dataID = data.getId();
        this.amount = amount;
        this.untradeable = getData().isUntradeable();
        this.awakenings = new ArrayList<Awakening>();

        if(getData() != null && getData().getCategory() == ItemCategory.MOUNT && this.mountData == null) this.mountData = new MountData();
    }

    public CustomItem(ItemData data, int amount, boolean untradeable){
        this.dataID = data.getId();
        this.amount = amount;
        this.untradeable = untradeable;
        this.awakenings = new ArrayList<Awakening>();

        if(getData() != null && getData().getCategory() == ItemCategory.MOUNT && this.mountData == null) this.mountData = new MountData();
    }

    public CustomItem(ItemData data, int amount, boolean untradeable, Awakening[] awakenings){
        this.dataID = data.getId();
        this.amount = amount;
        this.untradeable = untradeable;
        this.awakenings = new ArrayList<Awakening>();

        if(awakenings != null && awakenings.length > 0){
            this.awakenings.addAll(Arrays.asList(awakenings));
        }

        if(getData() != null && getData().getCategory() == ItemCategory.MOUNT && this.mountData == null) this.mountData = new MountData();
    }

    public CustomItem(ItemData data, int amount, boolean untradeable, Awakening[] awakenings, int upgradeValue){
        this.dataID = data.getId();
        this.amount = amount;
        this.untradeable = untradeable;
        this.awakenings = new ArrayList<Awakening>();
        this.upgradeValue = upgradeValue;

        if(awakenings != null && awakenings.length > 0){
            this.awakenings.addAll(Arrays.asList(awakenings));
        }

        if(getData() != null && getData().getCategory() == ItemCategory.MOUNT && this.mountData == null) this.mountData = new MountData();
    }

    public CustomItem(ItemData data, int amount, boolean untradeable, Awakening[] awakenings, int upgradeValue, MountData mountData){
        this.dataID = data.getId();
        this.amount = amount;
        this.untradeable = untradeable;
        this.awakenings = new ArrayList<Awakening>();
        this.upgradeValue = upgradeValue;
        this.mountData = mountData;

        if(awakenings != null && awakenings.length > 0){
            this.awakenings.addAll(Arrays.asList(awakenings));
        }

        if(getData() != null && getData().getCategory() == ItemCategory.MOUNT && this.mountData == null) this.mountData = new MountData();
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

                if(DungeonRPG.STORE_NBT_JSON){
                    if(tag.hasKey("wodItemData")){
                        CustomItem t = fromString(tag.getString("wodItemData"));
                        t.setAmount(i.getAmount());

                        return t;
                    }
                } else {
                    if(tag.hasKey("dataID")){
                        int id = tag.getInt("dataID");
                        boolean untradeable = ItemData.getData(id).isUntradeable();
                        int upgradeValue = 0;

                        if(tag.hasKey("untradeable")) untradeable = Util.convertIntegerToBoolean(tag.getInt("untradeable"));

                        if(tag.hasKey("upgradeValue")) upgradeValue = tag.getInt("upgradeValue");

                        if(tag.hasKey("awakenings")){
                            NBTTagList list = tag.getList("awakenings", NBTTypeID.STRING);

                            ArrayList<Awakening> awakenings = new ArrayList<Awakening>();

                            for(int j = 0; j < list.size(); j++){
                                String s = list.getString(j);

                                awakenings.add(Awakening.fromString(s));
                            }

                            return new CustomItem(id,i.getAmount(),untradeable,awakenings.toArray(new Awakening[]{}),upgradeValue);
                        } else {
                            return new CustomItem(id,i.getAmount(),untradeable,null,upgradeValue);
                        }
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

    public int getUpgradeValue() {
        return upgradeValue;
    }

    public void setUpgradeValue(int upgradeValue) {
        this.upgradeValue = upgradeValue;
    }

    public double getModifiedAtkMin(){
        double base = getData().getAtkMin();
        double p = ((double)getUpgradeValue())/DungeonRPG.UPGRADING_STONE_DIVIDING_VALUE;

        return (base+(base*p));
    }

    public double getModifiedAtkMax(){
        double base = getData().getAtkMax();
        double p = ((double)getUpgradeValue())/DungeonRPG.UPGRADING_STONE_DIVIDING_VALUE;

        return (base+(base*p));
    }

    public double getModifiedDefMin(){
        double base = getData().getDefMin();
        double p = ((double)getUpgradeValue())/DungeonRPG.UPGRADING_STONE_DIVIDING_VALUE;

        return (base+(base*p));
    }

    public double getModifiedDefMax(){
        double base = getData().getDefMax();
        double p = ((double)getUpgradeValue())/DungeonRPG.UPGRADING_STONE_DIVIDING_VALUE;

        return (base+(base*p));
    }

    public double getModifiedPickaxeStrength(){
        double base = getData().getPickaxeStrength();
        double p = ((double)getUpgradeValue())/DungeonRPG.UPGRADING_STONE_DIVIDING_VALUE;

        return (base+(base*p));
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
            } else if(getData().getNeededBlacksmithingLevel() > u.getCurrentCharacter().getVariables().getProfessionProgress(Profession.BLACKSMITHING).getLevel()){
                return false;
            } else if(getData().getNeededCraftingLevel() > u.getCurrentCharacter().getVariables().getProfessionProgress(Profession.CRAFTING).getLevel()){
                return false;
            } else if(getData().getNeededMiningLevel() > u.getCurrentCharacter().getVariables().getProfessionProgress(Profession.MINING).getLevel()){
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

        if(getData().getCategory() == ItemCategory.WEAPON_BOW || getData().getCategory() == ItemCategory.WEAPON_SHEARS || getData().getCategory() == ItemCategory.WEAPON_STICK || getData().getCategory() == ItemCategory.WEAPON_AXE || getData().getCategory() == ItemCategory.ARMOR || getData().getCategory() == ItemCategory.PICKAXE){
            if(getData().getNeededLevel() > 0) sellprice += getData().getNeededLevel()*1.5;
            if(getData().getNeededBlacksmithingLevel() > 0) sellprice += getData().getNeededBlacksmithingLevel()*1.5;
            if(getData().getNeededCraftingLevel() > 0) sellprice += getData().getNeededCraftingLevel()*1.5;
            if(getData().getNeededMiningLevel() > 0) sellprice += getData().getNeededMiningLevel()*1.5;

            if(sellprice < 1) sellprice = 1;

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
        } else if(getData().getCategory() == ItemCategory.MATERIAL){
            sellprice = 2;
        }

        sellprice *= amount;

        if(sellprice < 0) sellprice = 1;

        return sellprice;
    }

    public MountData getMountData() {
        return mountData;
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

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public ItemStack build(Player p){
        if(p != null){
            GameUser u = GameUser.getUser(p);
            Character character = u.getCurrentCharacter();
            if(character == null) character = u.getCharacters().get(0);

            if(getData() == null){
                return new ItemStack(Material.AIR);
            } else {
                ItemStack i = new ItemStack(getData().getIcon());
                i.setAmount(amount);
                i.setDurability((short)getData().getDurability());
                ItemMeta iM = i.getItemMeta();

                String g = getUpgradeValue() > 0 ? " " + ChatColor.YELLOW + "[+" + getUpgradeValue() + "]" : "";

                iM.setDisplayName(getData().getRarity().getColor() + getData().getName() + g);
                ArrayList<String> iL = new ArrayList<String>();
                if(getData().getCategory() == ItemCategory.WEAPON_BOW || getData().getCategory() == ItemCategory.WEAPON_AXE || getData().getCategory() == ItemCategory.WEAPON_SHEARS || getData().getCategory() == ItemCategory.WEAPON_STICK){
                    iL.add(" ");
                    iL.add(ChatColor.YELLOW + "Damage: " + (int)getModifiedAtkMin() + "-" + (int)getModifiedAtkMax());
                    for(Awakening a : getData().getAdditionalStats()){
                        if(a.value > 0){
                            // IS POSITIVE
                            if(a.isPercentage){
                                iL.add(ChatColor.YELLOW + a.type.getDisplayName() + ": " + ChatColor.GREEN + "+" + a.value + "%");
                            } else {
                                iL.add(ChatColor.YELLOW + a.type.getDisplayName() + ": " + ChatColor.GREEN + "+" + a.value);
                            }
                        } else if(a.value == 0){
                            // IS NEUTRAL (shouldn't really happen)
                            if(a.isPercentage){
                                iL.add(ChatColor.YELLOW + a.type.getDisplayName() + ": " + ChatColor.YELLOW + "+" + a.value + "%");
                            } else {
                                iL.add(ChatColor.YELLOW + a.type.getDisplayName() + ": " + ChatColor.YELLOW + "+" + a.value);
                            }
                        } else {
                            // IS NEGATIVE
                            if(a.isPercentage){
                                iL.add(ChatColor.YELLOW + a.type.getDisplayName() + ": " + ChatColor.RED + a.value + "%");
                            } else {
                                iL.add(ChatColor.YELLOW + a.type.getDisplayName() + ": " + ChatColor.RED + a.value);
                            }
                        }
                    }
                    iL.add(" ");
                    if(character.getLevel() >= getData().getNeededLevel()){
                        iL.add(ChatColor.DARK_GREEN + ChatIcons.CHECK_MARK + ChatColor.GRAY + " Required Level: " + getData().getNeededLevel());
                    } else {
                        iL.add(ChatColor.DARK_RED + ChatIcons.X + ChatColor.GRAY + " Required Level: " + getData().getNeededLevel());
                    }

                    if(getData().getNeededClass() != RPGClass.NONE){
                        if(character.getRpgClass().matches(getData().getNeededClass())){
                            iL.add(ChatColor.DARK_GREEN + ChatIcons.CHECK_MARK + ChatColor.GRAY + " Required Class: " + getData().getNeededClass().getName());
                        } else {
                            iL.add(ChatColor.DARK_RED + ChatIcons.X + ChatColor.GRAY + " Required Class: " + getData().getNeededClass().getName());
                        }
                    }

                    int b = 0;

                    b = getData().getNeededBlacksmithingLevel();
                    if(b > 0){
                        if(character.getVariables().getProfessionProgress(Profession.BLACKSMITHING).getLevel() >= b){
                            iL.add(ChatColor.DARK_GREEN + ChatIcons.CHECK_MARK + ChatColor.GRAY + " Required Blacksmithing Level: " + b);
                        } else {
                            iL.add(ChatColor.DARK_RED + ChatIcons.X + ChatColor.GRAY + " Required Blacksmithing Level: " + b);
                        }
                    }

                    b = getData().getNeededCraftingLevel();
                    if(b > 0){
                        if(character.getVariables().getProfessionProgress(Profession.CRAFTING).getLevel() >= b){
                            iL.add(ChatColor.DARK_GREEN + ChatIcons.CHECK_MARK + ChatColor.GRAY + " Required Crafting Level: " + b);
                        } else {
                            iL.add(ChatColor.DARK_RED + ChatIcons.X + ChatColor.GRAY + " Required Crafting Level: " + b);
                        }
                    }

                    b = getData().getNeededMiningLevel();
                    if(b > 0){
                        if(character.getVariables().getProfessionProgress(Profession.MINING).getLevel() >= b){
                            iL.add(ChatColor.DARK_GREEN + ChatIcons.CHECK_MARK + ChatColor.GRAY + " Required Mining Level: " + b);
                        } else {
                            iL.add(ChatColor.DARK_RED + ChatIcons.X + ChatColor.GRAY + " Required Mining Level: " + b);
                        }
                    }

                    iL.add(" ");

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
                            iL.add(ChatColor.AQUA + "Awakening available!");
                        }
                    }

                    iL.add(" ");
                    // TODO: Add crystal info
                    iL.add(getData().getRarity().getColor() + getData().getRarity().getName() + " Item");

                    if(getData().getDescription() != null) iL.add(" ");
                } else if(getData().getCategory() == ItemCategory.ARMOR){
                    iL.add(" ");
                    iL.add(ChatColor.YELLOW + "Defense: " + (int)getModifiedDefMin() + "-" + (int)getModifiedDefMax());
                    for(Awakening a : getData().getAdditionalStats()){
                        if(a.value > 0){
                            // IS POSITIVE
                            if(a.isPercentage){
                                iL.add(ChatColor.YELLOW + a.type.getDisplayName() + ": " + ChatColor.GREEN + "+" + a.value + "%");
                            } else {
                                iL.add(ChatColor.YELLOW + a.type.getDisplayName() + ": " + ChatColor.GREEN + "+" + a.value);
                            }
                        } else if(a.value == 0){
                            // IS NEUTRAL (shouldn't really happen)
                            if(a.isPercentage){
                                iL.add(ChatColor.YELLOW + a.type.getDisplayName() + ": " + ChatColor.YELLOW + "+" + a.value + "%");
                            } else {
                                iL.add(ChatColor.YELLOW + a.type.getDisplayName() + ": " + ChatColor.YELLOW + "+" + a.value);
                            }
                        } else {
                            // IS NEGATIVE
                            if(a.isPercentage){
                                iL.add(ChatColor.YELLOW + a.type.getDisplayName() + ": " + ChatColor.RED + a.value + "%");
                            } else {
                                iL.add(ChatColor.YELLOW + a.type.getDisplayName() + ": " + ChatColor.RED + a.value);
                            }
                        }
                    }
                    iL.add(" ");
                    if(character.getLevel() >= getData().getNeededLevel()){
                        iL.add(ChatColor.DARK_GREEN + ChatIcons.CHECK_MARK + ChatColor.GRAY + " Required Level: " + getData().getNeededLevel());
                    } else {
                        iL.add(ChatColor.DARK_RED + ChatIcons.X + ChatColor.GRAY + " Required Level: " + getData().getNeededLevel());
                    }

                    if(getData().getNeededClass() != RPGClass.NONE){
                        if(character.getRpgClass().matches(getData().getNeededClass())){
                            iL.add(ChatColor.DARK_GREEN + ChatIcons.CHECK_MARK + ChatColor.GRAY + " Required Class: " + getData().getNeededClass().getName());
                        } else {
                            iL.add(ChatColor.DARK_RED + ChatIcons.X + ChatColor.GRAY + " Required Class: " + getData().getNeededClass().getName());
                        }
                    }

                    int b = 0;

                    b = getData().getNeededBlacksmithingLevel();
                    if(b > 0){
                        if(character.getVariables().getProfessionProgress(Profession.BLACKSMITHING).getLevel() >= b){
                            iL.add(ChatColor.DARK_GREEN + ChatIcons.CHECK_MARK + ChatColor.GRAY + " Required Blacksmithing Level: " + b);
                        } else {
                            iL.add(ChatColor.DARK_RED + ChatIcons.X + ChatColor.GRAY + " Required Blacksmithing Level: " + b);
                        }
                    }

                    b = getData().getNeededCraftingLevel();
                    if(b > 0){
                        if(character.getVariables().getProfessionProgress(Profession.CRAFTING).getLevel() >= b){
                            iL.add(ChatColor.DARK_GREEN + ChatIcons.CHECK_MARK + ChatColor.GRAY + " Required Crafting Level: " + b);
                        } else {
                            iL.add(ChatColor.DARK_RED + ChatIcons.X + ChatColor.GRAY + " Required Crafting Level: " + b);
                        }
                    }

                    b = getData().getNeededMiningLevel();
                    if(b > 0){
                        if(character.getVariables().getProfessionProgress(Profession.MINING).getLevel() >= b){
                            iL.add(ChatColor.DARK_GREEN + ChatIcons.CHECK_MARK + ChatColor.GRAY + " Required Mining Level: " + b);
                        } else {
                            iL.add(ChatColor.DARK_RED + ChatIcons.X + ChatColor.GRAY + " Required Mining Level: " + b);
                        }
                    }

                    iL.add(" ");

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
                            iL.add(ChatColor.AQUA + "Awakening available!");
                        }
                    }

                    iL.add(" ");
                    // TODO: Add crystal info
                    iL.add(getData().getRarity().getColor() + getData().getRarity().getName() + " Item");

                    if(getData().getDescription() != null) iL.add(" ");
                } else if(getData().getCategory() == ItemCategory.PICKAXE){
                    iL.add(" ");
                    iL.add(ChatColor.YELLOW + "Pickaxe Strength: " + (int)getModifiedPickaxeStrength());
                    if(getModifiedAtkMin() > 0 && getModifiedAtkMax() > 0) iL.add(ChatColor.YELLOW + "Damage: " + (int)getModifiedAtkMin() + "-" + (int)getModifiedAtkMax());
                    for(Awakening a : getData().getAdditionalStats()){
                        if(a.value > 0){
                            // IS POSITIVE
                            if(a.isPercentage){
                                iL.add(ChatColor.YELLOW + a.type.getDisplayName() + ": " + ChatColor.GREEN + "+" + a.value + "%");
                            } else {
                                iL.add(ChatColor.YELLOW + a.type.getDisplayName() + ": " + ChatColor.GREEN + "+" + a.value);
                            }
                        } else if(a.value == 0){
                            // IS NEUTRAL (shouldn't really happen)
                            if(a.isPercentage){
                                iL.add(ChatColor.YELLOW + a.type.getDisplayName() + ": " + ChatColor.YELLOW + "+" + a.value + "%");
                            } else {
                                iL.add(ChatColor.YELLOW + a.type.getDisplayName() + ": " + ChatColor.YELLOW + "+" + a.value);
                            }
                        } else {
                            // IS NEGATIVE
                            if(a.isPercentage){
                                iL.add(ChatColor.YELLOW + a.type.getDisplayName() + ": " + ChatColor.RED + a.value + "%");
                            } else {
                                iL.add(ChatColor.YELLOW + a.type.getDisplayName() + ": " + ChatColor.RED + a.value);
                            }
                        }
                    }
                    iL.add(" ");
                    if(character.getLevel() >= getData().getNeededLevel()){
                        iL.add(ChatColor.DARK_GREEN + ChatIcons.CHECK_MARK + ChatColor.GRAY + " Required Level: " + getData().getNeededLevel());
                    } else {
                        iL.add(ChatColor.DARK_RED + ChatIcons.X + ChatColor.GRAY + " Required Level: " + getData().getNeededLevel());
                    }

                    if(getData().getNeededClass() != RPGClass.NONE){
                        if(character.getRpgClass().matches(getData().getNeededClass())){
                            iL.add(ChatColor.DARK_GREEN + ChatIcons.CHECK_MARK + ChatColor.GRAY + " Required Class: " + getData().getNeededClass().getName());
                        } else {
                            iL.add(ChatColor.DARK_RED + ChatIcons.X + ChatColor.GRAY + " Required Class: " + getData().getNeededClass().getName());
                        }
                    }

                    int b = 0;

                    b = getData().getNeededBlacksmithingLevel();
                    if(b > 0){
                        if(character.getVariables().getProfessionProgress(Profession.BLACKSMITHING).getLevel() >= b){
                            iL.add(ChatColor.DARK_GREEN + ChatIcons.CHECK_MARK + ChatColor.GRAY + " Required Blacksmithing Level: " + b);
                        } else {
                            iL.add(ChatColor.DARK_RED + ChatIcons.X + ChatColor.GRAY + " Required Blacksmithing Level: " + b);
                        }
                    }

                    b = getData().getNeededCraftingLevel();
                    if(b > 0){
                        if(character.getVariables().getProfessionProgress(Profession.CRAFTING).getLevel() >= b){
                            iL.add(ChatColor.DARK_GREEN + ChatIcons.CHECK_MARK + ChatColor.GRAY + " Required Crafting Level: " + b);
                        } else {
                            iL.add(ChatColor.DARK_RED + ChatIcons.X + ChatColor.GRAY + " Required Crafting Level: " + b);
                        }
                    }

                    b = getData().getNeededMiningLevel();
                    if(b > 0){
                        if(character.getVariables().getProfessionProgress(Profession.MINING).getLevel() >= b){
                            iL.add(ChatColor.DARK_GREEN + ChatIcons.CHECK_MARK + ChatColor.GRAY + " Required Mining Level: " + b);
                        } else {
                            iL.add(ChatColor.DARK_RED + ChatIcons.X + ChatColor.GRAY + " Required Mining Level: " + b);
                        }
                    }

                    iL.add(" ");

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
                            iL.add(ChatColor.AQUA + "Awakening available!");
                        }

                        iL.add(" ");
                    }

                    iL.add(getData().getRarity().getColor() + getData().getRarity().getName() + " Item");

                    if(getData().getDescription() != null) iL.add(" ");
                } else if(getData().getCategory() == ItemCategory.FOOD){
                    iL.add(" ");
                    iL.add(ChatColor.YELLOW + "HP Regeneration: " + getData().getFoodRegeneration());
                    iL.add(" ");
                    if(character.getLevel() >= getData().getNeededLevel()){
                        iL.add(ChatColor.DARK_GREEN + ChatIcons.CHECK_MARK + ChatColor.GRAY + " Required Level: " + getData().getNeededLevel());
                    } else {
                        iL.add(ChatColor.DARK_RED + ChatIcons.X + ChatColor.GRAY + " Required Level: " + getData().getNeededLevel());
                    }

                    if(getData().getNeededClass() != RPGClass.NONE){
                        if(character.getRpgClass().matches(getData().getNeededClass())){
                            iL.add(ChatColor.DARK_GREEN + ChatIcons.CHECK_MARK + ChatColor.GRAY + " Required Class: " + getData().getNeededClass().getName());
                        } else {
                            iL.add(ChatColor.DARK_RED + ChatIcons.X + ChatColor.GRAY + " Required Class: " + getData().getNeededClass().getName());
                        }
                    }

                    int b = 0;

                    b = getData().getNeededBlacksmithingLevel();
                    if(b > 0){
                        if(character.getVariables().getProfessionProgress(Profession.BLACKSMITHING).getLevel() >= b){
                            iL.add(ChatColor.DARK_GREEN + ChatIcons.CHECK_MARK + ChatColor.GRAY + " Required Blacksmithing Level: " + b);
                        } else {
                            iL.add(ChatColor.DARK_RED + ChatIcons.X + ChatColor.GRAY + " Required Blacksmithing Level: " + b);
                        }
                    }

                    b = getData().getNeededCraftingLevel();
                    if(b > 0){
                        if(character.getVariables().getProfessionProgress(Profession.CRAFTING).getLevel() >= b){
                            iL.add(ChatColor.DARK_GREEN + ChatIcons.CHECK_MARK + ChatColor.GRAY + " Required Crafting Level: " + b);
                        } else {
                            iL.add(ChatColor.DARK_RED + ChatIcons.X + ChatColor.GRAY + " Required Crafting Level: " + b);
                        }
                    }

                    b = getData().getNeededMiningLevel();
                    if(b > 0){
                        if(character.getVariables().getProfessionProgress(Profession.MINING).getLevel() >= b){
                            iL.add(ChatColor.DARK_GREEN + ChatIcons.CHECK_MARK + ChatColor.GRAY + " Required Mining Level: " + b);
                        } else {
                            iL.add(ChatColor.DARK_RED + ChatIcons.X + ChatColor.GRAY + " Required Mining Level: " + b);
                        }
                    }

                    if(getData().getDescription() != null) iL.add(" ");
                } else if(getData().getCategory() == ItemCategory.MOUNT){
                    iL.add(" ");
                    iL.add(ChatColor.YELLOW + "Speed: " + getData().getMountSpeed());
                    iL.add(ChatColor.YELLOW + "Jump Strength: " + getData().getMountJumpStrength());
                    for(Awakening a : getData().getAdditionalStats()){
                        if(a.value > 0){
                            // IS POSITIVE
                            if(a.isPercentage){
                                iL.add(ChatColor.YELLOW + a.type.getDisplayName() + ": " + ChatColor.GREEN + "+" + a.value + "%");
                            } else {
                                iL.add(ChatColor.YELLOW + a.type.getDisplayName() + ": " + ChatColor.GREEN + "+" + a.value);
                            }
                        } else if(a.value == 0){
                            // IS NEUTRAL (shouldn't really happen)
                            if(a.isPercentage){
                                iL.add(ChatColor.YELLOW + a.type.getDisplayName() + ": " + ChatColor.YELLOW + "+" + a.value + "%");
                            } else {
                                iL.add(ChatColor.YELLOW + a.type.getDisplayName() + ": " + ChatColor.YELLOW + "+" + a.value);
                            }
                        } else {
                            // IS NEGATIVE
                            if(a.isPercentage){
                                iL.add(ChatColor.YELLOW + a.type.getDisplayName() + ": " + ChatColor.RED + a.value + "%");
                            } else {
                                iL.add(ChatColor.YELLOW + a.type.getDisplayName() + ": " + ChatColor.RED + a.value);
                            }
                        }
                    }
                    iL.add(" ");
                    if(getData().getNeededLevel() > 0){
                        if(character.getLevel() >= getData().getNeededLevel()){
                            iL.add(ChatColor.DARK_GREEN + ChatIcons.CHECK_MARK + ChatColor.GRAY + " Required Level: " + getData().getNeededLevel());
                        } else {
                            iL.add(ChatColor.DARK_RED + ChatIcons.X + ChatColor.GRAY + " Required Level: " + getData().getNeededLevel());
                        }
                    }

                    if(getData().getNeededClass() != RPGClass.NONE){
                        if(character.getRpgClass().matches(getData().getNeededClass())){
                            iL.add(ChatColor.DARK_GREEN + ChatIcons.CHECK_MARK + ChatColor.GRAY + " Required Class: " + getData().getNeededClass().getName());
                        } else {
                            iL.add(ChatColor.DARK_RED + ChatIcons.X + ChatColor.GRAY + " Required Class: " + getData().getNeededClass().getName());
                        }
                    }

                    int b = 0;

                    b = getData().getNeededBlacksmithingLevel();
                    if(b > 0){
                        if(character.getVariables().getProfessionProgress(Profession.BLACKSMITHING).getLevel() >= b){
                            iL.add(ChatColor.DARK_GREEN + ChatIcons.CHECK_MARK + ChatColor.GRAY + " Required Blacksmithing Level: " + b);
                        } else {
                            iL.add(ChatColor.DARK_RED + ChatIcons.X + ChatColor.GRAY + " Required Blacksmithing Level: " + b);
                        }
                    }

                    b = getData().getNeededCraftingLevel();
                    if(b > 0){
                        if(character.getVariables().getProfessionProgress(Profession.CRAFTING).getLevel() >= b){
                            iL.add(ChatColor.DARK_GREEN + ChatIcons.CHECK_MARK + ChatColor.GRAY + " Required Crafting Level: " + b);
                        } else {
                            iL.add(ChatColor.DARK_RED + ChatIcons.X + ChatColor.GRAY + " Required Crafting Level: " + b);
                        }
                    }

                    b = getData().getNeededMiningLevel();
                    if(b > 0){
                        if(character.getVariables().getProfessionProgress(Profession.MINING).getLevel() >= b){
                            iL.add(ChatColor.DARK_GREEN + ChatIcons.CHECK_MARK + ChatColor.GRAY + " Required Mining Level: " + b);
                        } else {
                            iL.add(ChatColor.DARK_RED + ChatIcons.X + ChatColor.GRAY + " Required Mining Level: " + b);
                        }
                    }

                    if(canHoldAwakenings()){
                        iL.add(" ");

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
                            iL.add(ChatColor.AQUA + "Awakening available!");
                        }
                    }
                } else if(getData().getCategory() == ItemCategory.MATERIAL){
                    iL.add(ChatColor.BLUE + "Material");
                } else if(getData().getCategory() == ItemCategory.COLLECTIBLE){
                    iL.add(ChatColor.GOLD + "Collectible");
                } else if(getData().getCategory() == ItemCategory.QUEST){
                    iL.add(ChatColor.RED + "Quest Item");
                }

                if(getData().getDescription() != null){
                    for(String s : Util.getWordWrapLore(getData().getDescription())){
                        iL.add(ChatColor.GRAY + s);
                    }
                }

                if(isUntradeable()){
                    iL.add(ChatColor.RED + "Untradeable");
                }

                iM.setLore(iL);
                i.setItemMeta(iM);

                if(getData().getIcon() == Material.LEATHER_HELMET || getData().getIcon() == Material.LEATHER_CHESTPLATE || getData().getIcon() == Material.LEATHER_LEGGINGS || getData().getIcon() == Material.LEATHER_BOOTS){
                    LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta)i.getItemMeta();

                    if(getData().getLeatherArmorColor() != null) leatherArmorMeta.setColor(getData().getLeatherArmorColor());
                    i.setItemMeta(leatherArmorMeta);
                }

                i = ItemUtil.hideFlags(ItemUtil.setUnbreakable(i,true));
                if(getUpgradeValue() >= 10) i = ItemUtil.addGlow(i);
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

        if(DungeonRPG.STORE_NBT_JSON){
            if(!tag.hasKey("wodItemData")) tag.set("wodItemData",new NBTTagString(toString()));
        } else {
            if(!tag.hasKey("dataID")) tag.set("dataID",new NBTTagInt(getData().getId()));

            if(!tag.hasKey("untradeable")) tag.set("untradeable",new NBTTagInt(Util.convertBooleanToInteger(isUntradeable())));

            if(!tag.hasKey("upgradeValue")) tag.set("upgradeValue",new NBTTagInt(getUpgradeValue()));

            if(hasAwakenings()){
                if(!tag.hasKey("awakenings")){
                    NBTTagList list = new NBTTagList();
                    for(Awakening a : getAwakenings()){
                        list.add(new NBTTagString(a.toString()));
                    }

                    tag.set("awakenings",list);
                }
            }
        }

        if(getData().getStackLimit() == 1) tag.set("stackProtection", new NBTTagInt(Util.randomInteger(-5000, 5000)));

        nmsItem.setTag(tag);
        i = CraftItemStack.asCraftMirror(nmsItem);

        return i;
    }

    public boolean isSameItem(CustomItem item){
        if(item == this){
            return true;
        } else {
            if(getData().getId() == item.getData().getId()){
                if(getAwakenings() == null && item.getAwakenings() == null){

                } else if(item.getAwakenings().size() == getAwakenings().size()) {
                    for(Awakening a : getAwakenings()){
                        if(!(getAwakeningValue(a.type) == item.getAwakeningValue(a.type))) return false;
                    }
                } else {
                    return false;
                }

                if(isUntradeable() == item.isUntradeable()){
                    return true;
                }
            }
        }

        return false;
    }

    public static CustomItem fromString(String s){
        Gson gson = DungeonAPI.GSON;
        CustomItem i = gson.fromJson(s,CustomItem.class);
        return i;
    }

    public boolean isMatchingWeapon(RPGClass rpgClass){
        return getData().isMatchingWeapon(rpgClass);
    }

    public String toString(){
        Gson gson = DungeonAPI.GSON;
        return gson.toJson(this).replace("\"amount\":" + amount + ",","");
    }
}
