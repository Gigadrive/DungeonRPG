package net.wrathofdungeons.dungeonrpg.inv;

import net.wrathofdungeons.dungeonapi.util.ItemUtil;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.StatPointType;
import net.wrathofdungeons.dungeonrpg.items.awakening.AwakeningType;
import net.wrathofdungeons.dungeonrpg.items.awakening.AwakeningValueType;
import net.wrathofdungeons.dungeonrpg.professions.OreLevel;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.util.FormularUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.inventivetalent.menubuilder.inventory.InventoryMenuBuilder;

import java.util.ArrayList;

public class GameMenu {
    public static void openFor(Player p){
        if(!GameUser.isLoaded(p)) return;
        GameUser u = GameUser.getUser(p);
        if(u.getCurrentCharacter() == null) return;

        InventoryMenuBuilder inv = new InventoryMenuBuilder(Util.MAX_INVENTORY_SIZE);
        inv.withTitle(u.getCurrentCharacter().getStatpointsLeft() > 0 ? "[" + u.getCurrentCharacter().getStatpointsLeft() + "] Game Menu" : "Game Menu");

        int mobsKilled = 0;
        for(int id : u.getCurrentCharacter().getVariables().statisticsManager.mobsKilled.keySet()) mobsKilled += u.getCurrentCharacter().getVariables().statisticsManager.mobsKilled.get(id);

        int oresMined = 0;
        for(OreLevel level : u.getCurrentCharacter().getVariables().statisticsManager.oresMined.keySet()) oresMined += u.getCurrentCharacter().getVariables().statisticsManager.oresMined.get(level);

        ItemStack cashShop = ItemUtil.namedItem(Material.GOLD_INGOT, ChatColor.GOLD + "Cash Shop",null);
        ItemStack quests = ItemUtil.namedItem(Material.BOOK, ChatColor.GOLD + "Quest Diary",null);
        ItemStack skills = ItemUtil.namedItem(Material.ARROW, ChatColor.GOLD + "Skills",null);
        ItemStack horse = ItemUtil.namedItem(Material.SADDLE, ChatColor.GOLD + "Horse",null);
        ItemStack settings = ItemUtil.namedItem(Material.DIODE, ChatColor.GOLD + "Settings",null);
        ItemStack party = ItemUtil.namedItem(Material.FIREWORK, ChatColor.GOLD + "Party",null);
        ItemStack friends = ItemUtil.namedItem(Material.SKULL_ITEM, ChatColor.GOLD + "Friends",null,3);
        ItemStack guild = ItemUtil.namedItem(Material.PAINTING, ChatColor.GOLD + "Guild",null);
        ItemStack statsInfo = ItemUtil.namedItem(Material.IRON_SWORD, ChatColor.GOLD + "Stats Info",null);
        ItemStack statistics = ItemUtil.namedItem(Material.SKULL_ITEM, ChatColor.GOLD + "Statistics",new String[]{
                ChatColor.GRAY + "Playtime: " + ChatColor.WHITE + Util.round(u.getCurrentCharacter().getPlaytime()/60/60,1) + " h",
                ChatColor.GRAY + "Mobs killed: " + ChatColor.WHITE + mobsKilled,
                ChatColor.GRAY + "Items awakened: " + ChatColor.WHITE + u.getCurrentCharacter().getVariables().statisticsManager.itemsAwakened,
                ChatColor.GRAY + "Chests looted: " + ChatColor.WHITE + u.getCurrentCharacter().getVariables().statisticsManager.chestsLooted,
                ChatColor.GRAY + "Blocks walked: " + ChatColor.WHITE + u.getCurrentCharacter().getVariables().statisticsManager.blocksWalked,
                ChatColor.GRAY + "Ores mined: " + ChatColor.WHITE + oresMined
        },2);

        ArrayList<String> statsLore = new ArrayList<String>();
        boolean b = false;
        for(AwakeningType a : AwakeningType.values()){
            int value = u.getCurrentCharacter().getTotalValue(a);

            if(value != 0){
                if(value > 0){
                    if (a.getValueType() == AwakeningValueType.PERCENTAGE) {
                        statsLore.add(ChatColor.GRAY + a.getDisplayName() + ": " + ChatColor.GREEN + "+" + value + "%");
                        b = true;
                    } else {
                        statsLore.add(ChatColor.GRAY + a.getDisplayName() + ": " + ChatColor.GREEN + "+" + value);
                        b = true;
                    }
                } else {
                    if (a.getValueType() == AwakeningValueType.PERCENTAGE) {
                        statsLore.add(ChatColor.GRAY + a.getDisplayName() + ": " + ChatColor.RED + value + "%");
                        b = true;
                    } else {
                        statsLore.add(ChatColor.GRAY + a.getDisplayName() + ": " + ChatColor.RED + value);
                        b = true;
                    }
                }
            }
        }

        if(!b) statsLore.add(ChatColor.GRAY + "(You currently don't have any stat additions)");

        ItemMeta sM = statsInfo.getItemMeta();
        sM.setLore(statsLore);
        statsInfo.setItemMeta(sM);

        ItemStack profile = new ItemStack(Material.SKULL_ITEM);
        profile.setDurability((short)3);
        SkullMeta m = (SkullMeta)profile.getItemMeta();
        m.setOwner(p.getName());
        m.setDisplayName(ChatColor.GOLD + p.getName());
        ArrayList<String> l = new ArrayList<String>();
        l.add(ChatColor.GRAY + "Class: " + ChatColor.WHITE + u.getCurrentCharacter().getRpgClass().getName());
        l.add(ChatColor.GRAY + "Level: " + ChatColor.WHITE + u.getCurrentCharacter().getLevel());
        l.add(ChatColor.GRAY + "EXP: " + ChatColor.WHITE + Util.round((u.getCurrentCharacter().getExp()/ FormularUtils.getExpNeededForLevel(u.getCurrentCharacter().getLevel()+1))*100,2) + "%");
        l.add(ChatColor.GRAY + "Rank: " + u.getRank().getColor() + u.getRank().getName());
        m.setLore(l);
        profile.setItemMeta(m);

        inv.withItem(22,profile);
        inv.withItem(29,b(u,StatPointType.STRENGTH,"Increases all damage dealt."),((player, action, item) -> c(p,StatPointType.STRENGTH,action)), ClickType.LEFT, ClickType.RIGHT);
        inv.withItem(30,b(u,StatPointType.STAMINA,"Increases total health."),((player, action, item) -> c(p,StatPointType.STAMINA,action)), ClickType.LEFT, ClickType.RIGHT);
        inv.withItem(31,b(u,StatPointType.INTELLIGENCE,"Increases mana regeneration."),((player, action, item) -> c(p,StatPointType.INTELLIGENCE,action)), ClickType.LEFT, ClickType.RIGHT);
        inv.withItem(32,b(u,StatPointType.DEXTERITY,"Increases the chance to do critical hits."),((player, action, item) -> c(p,StatPointType.DEXTERITY,action)), ClickType.LEFT, ClickType.RIGHT);
        inv.withItem(33,b(u,StatPointType.AGILITY,"Increases the chance to dodge enemy attacks."),((player, action, item) -> c(p,StatPointType.AGILITY,action)), ClickType.LEFT, ClickType.RIGHT);

        inv.withItem(2,ItemUtil.hideFlags(cashShop),((player, action, item) -> {
            // TODO
        }),ClickType.LEFT);

        inv.withItem(4,ItemUtil.hideFlags(quests),((player, action, item) -> {
            QuestDiary.openFor(p);
        }),ClickType.LEFT);

        inv.withItem(6,ItemUtil.hideFlags(skills),((player, action, item) -> {
            SkillsMenu.openFor(p);
        }),ClickType.LEFT);

        inv.withItem(17,ItemUtil.hideFlags(horse),((player, action, item) -> {
            // TODO
        }),ClickType.LEFT);

        inv.withItem(44,ItemUtil.hideFlags(settings),((player, action, item) -> {
            SettingsMenu.openFor(p);
        }),ClickType.LEFT);

        inv.withItem(51,ItemUtil.hideFlags(party),((player, action, item) -> {
            if(u.getParty() != null){
                PartyMenu.openFor(p);
            } else {
                p.sendMessage(ChatColor.RED + "You are not in a party.");
            }
        }),ClickType.LEFT);

        inv.withItem(49,ItemUtil.hideFlags(friends),((player, action, item) -> {
            FriendsMenu.openFor(p);
        }),ClickType.LEFT);

        inv.withItem(47,ItemUtil.hideFlags(guild),((player, action, item) -> {
            // TODO
        }),ClickType.LEFT);

        inv.withItem(36,ItemUtil.hideFlags(statsInfo));

        inv.withItem(9,ItemUtil.hideFlags(statistics));

        inv.show(p);
    }

    private static void c(Player p, StatPointType type, ClickType action){
        GameUser u = GameUser.getUser(p);

        if(u.getCurrentCharacter().getStatpointsLeft() > 0){
            if((DungeonRPG.STATPOINTS_LIMIT == -1) || (u.getCurrentCharacter().getStatpointsPure(type) < DungeonRPG.STATPOINTS_LIMIT)){
                int toAdd = 1;
                if(action == ClickType.RIGHT){
                    if(u.getCurrentCharacter().getStatpointsLeft() >= 5){
                        toAdd = 5;
                    } else {
                        toAdd = u.getCurrentCharacter().getStatpointsLeft();
                    }
                }

                u.getCurrentCharacter().addStatpoints(type,toAdd);
                u.getCurrentCharacter().reduceStatpointsLeft(toAdd);
                p.closeInventory();
                openFor(p);
                p.playSound(p.getEyeLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP,1f,2f);
            } else {
                p.sendMessage(ChatColor.RED + "You have reached the limit for that statpoint.");
            }
        } else {
            p.sendMessage(ChatColor.RED + "You don't have any statpoints left.");
        }
    }

    private static ItemStack b(GameUser u, StatPointType type, String description){
        ItemStack strength = new ItemStack(Material.ENCHANTED_BOOK);
        switch(type){
            case STRENGTH:
                strength.setType(Material.REDSTONE_BLOCK);
                break;
            case STAMINA:
                strength.setType(Material.GOLD_BLOCK);
                break;
            case INTELLIGENCE:
                strength.setType(Material.LAPIS_BLOCK);
                break;
            case DEXTERITY:
                strength.setType(Material.EMERALD_BLOCK);
                break;
            case AGILITY:
                strength.setType(Material.IRON_BLOCK);
                break;
        }
        strength.setAmount(u.getCurrentCharacter().getStatpointsPure(type));
        ItemMeta strengthMeta = strength.getItemMeta();
        strengthMeta.setDisplayName(type.getColor() + type.getName());
        ArrayList<String> strengthLore = new ArrayList<String>();
        for(String s : Util.getWordWrapLore(description)){
            strengthLore.add(ChatColor.GOLD + s);
        }
        strengthLore.add(" ");
        strengthLore.add(ChatColor.GRAY + "Pure value: " + ChatColor.WHITE + u.getCurrentCharacter().getStatpointsPure(type));
        strengthLore.add(ChatColor.GRAY + "Additional value: " + ChatColor.WHITE + u.getCurrentCharacter().getStatpointsArtificial(type));
        strengthLore.add(ChatColor.GREEN + "Total value: " + ChatColor.YELLOW.toString() + ChatColor.BOLD + u.getCurrentCharacter().getStatpointsTotal(type));

        if(u.getCurrentCharacter().getStatpointsLeft() > 0){
            strengthLore.add(" ");

            strengthLore.add(ChatColor.DARK_GRAY + "Left-click to add 1 stat point");

            if(u.getCurrentCharacter().getStatpointsLeft() > 1){
                if(u.getCurrentCharacter().getStatpointsLeft() >= 5){
                    strengthLore.add(ChatColor.DARK_GRAY + "Right-click to add 5 stat points");
                } else {
                    strengthLore.add(ChatColor.DARK_GRAY + "Right-click to add " + u.getCurrentCharacter().getStatpointsLeft() + " stat points");
                }
            }
        }

        strengthMeta.setLore(strengthLore);
        strength.setItemMeta(strengthMeta);
        strength = ItemUtil.hideFlags(strength);

        if(DungeonRPG.STATPOINTS_LIMIT != -1 && u.getCurrentCharacter().getStatpointsPure(type) >= DungeonRPG.STATPOINTS_LIMIT) strength = ItemUtil.addGlow(strength);

        return strength;
    }
}
