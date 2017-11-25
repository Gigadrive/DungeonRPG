package net.wrathofdungeons.dungeonrpg.inv;

import net.wrathofdungeons.dungeonapi.util.ItemUtil;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.skill.ClickComboType;
import net.wrathofdungeons.dungeonrpg.skill.Skill;
import net.wrathofdungeons.dungeonrpg.skill.SkillStorage;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.inventivetalent.menubuilder.inventory.InventoryMenuBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.stream.Collectors;

public class SkillsMenu {
    public static void openFor(Player p){
        openFor(p,1);
    }

    public static void openFor(Player p, int page){
        GameUser u = GameUser.getUser(p);

        ItemStack pl = ItemUtil.namedItem(Material.STAINED_GLASS_PANE, " ", null, 15);
        ItemStack prev = ItemUtil.namedItem(Material.ARROW, ChatColor.GOLD + "<< " + ChatColor.AQUA + "Previous page", null);
        ItemStack next = ItemUtil.namedItem(Material.ARROW, ChatColor.AQUA + "Next page" + ChatColor.GOLD + " >>", null);
        ItemStack close = ItemUtil.namedItem(Material.BARRIER, ChatColor.DARK_RED + "Close", null);

        ArrayList<Skill> skills = new ArrayList<Skill>();
        for(Skill s : SkillStorage.getInstance().getSkills()) if(s.getRPGClass().matches(u.getCurrentCharacter().getRpgClass())) skills.add(s);

        Collections.sort(skills, new Comparator<Skill>() {
            @Override
            public int compare(Skill q1, Skill q2){
                return q1.getMinLevel() - q2.getMinLevel();
            }
        });

        int sizePerPage = 36;
        int total = skills.size();

        double d = (((double)total)/((double)sizePerPage));
        int maxPages = ((Double)d).intValue();
        if(maxPages < d) maxPages++;
        if(maxPages <= 0) maxPages = 1;

        InventoryMenuBuilder inv = new InventoryMenuBuilder(Util.MAX_INVENTORY_SIZE);
        inv.withTitle(u.getCurrentCharacter().getVariables().leftSkillPoints > 0 ? "[" + u.getCurrentCharacter().getVariables().leftSkillPoints + "] Skills (" + page + "/" + maxPages + ")" : "Skills (" + page + "/" + maxPages + ")");

        int slot = 0;
        for(Skill skill : skills.stream().skip((page-1) * sizePerPage).limit(sizePerPage).collect(Collectors.toCollection(ArrayList::new))){
            int invested = u.getCurrentCharacter().getVariables().getInvestedSkillPoints(skill);

            if(invested <= 0){
                ItemStack i = new ItemStack(Material.BARRIER);
                ItemMeta iM = i.getItemMeta();
                iM.setDisplayName(ChatColor.DARK_RED + skill.getName());
                ArrayList<String> iL = new ArrayList<String>();
                iL.add(ChatColor.RED + "Lv. Min: " + ChatColor.WHITE + skill.getMinLevel());
                if(skill.getDescription() != null && !skill.getDescription().isEmpty()){
                    iL.add(" ");

                    for(String s : Util.getWordWrapLore(skill.getDescription())){
                        iL.add(ChatColor.GRAY + s);
                    }
                }

                if(u.getCurrentCharacter().getLevel() >= skill.getMinLevel() && u.getCurrentCharacter().getVariables().leftSkillPoints > 0){
                    iL.add(" ");
                    for(String s : Util.getWordWrapLore("Click to unlock at the cost of 1 skill point!"))
                        iL.add(ChatColor.YELLOW + s);
                }

                iM.setLore(iL);
                i.setItemMeta(iM);
                i = ItemUtil.setUnbreakable(i);
                i = ItemUtil.hideFlags(i);
                inv.withItem(slot,i,((player, action, item) -> {
                    if(u.getCurrentCharacter().getLevel() >= skill.getMinLevel()){
                        if(u.getCurrentCharacter().getVariables().leftSkillPoints > 0){
                            u.getCurrentCharacter().getVariables().addInvestedSkillPoints(skill,1);
                            u.getCurrentCharacter().getVariables().leftSkillPoints--;
                            p.playSound(p.getEyeLocation(), Sound.ORB_PICKUP,1f,1f);
                            openFor(p,page);
                        } else {
                            p.sendMessage(ChatColor.RED + "You don't have enough skill points to unlock this skill.");
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "You must be at least level " + skill.getMinLevel() + " to unlock this skill.");
                    }
                }),ClickType.LEFT);
            } else {
                ItemStack i = new ItemStack(skill.getIcon(),1,(short)skill.getIconDurability());
                ItemMeta iM = i.getItemMeta();
                iM.setDisplayName(ChatColor.GOLD + skill.getName() + " " + ChatColor.YELLOW + "[Lv. " + invested + "/" + skill.getMaxInvestingPoints() + "]");
                ArrayList<String> iL = new ArrayList<String>();
                iL.add(ChatColor.GRAY + "Lv. Min: " + ChatColor.WHITE + skill.getMinLevel());

                HashMap<String,String> effects = skill.getEffects(invested);
                if(effects != null && effects.size() > 0){
                    for(String effectName : effects.keySet()){
                        String effect = effects.get(effectName);

                        iL.add(ChatColor.GRAY + effectName + ": " + ChatColor.WHITE + effect);
                    }
                }

                String comboString = null;
                ClickComboType combo = u.getCurrentCharacter().getVariables().getComboFromSkill(skill);
                if(combo != null){
                    if(u.getCurrentCharacter().getRpgClass().matches(RPGClass.ARCHER)){
                        comboString = DungeonRPG.convertComboString(combo.getAlternateComboString());
                    } else {
                        comboString = DungeonRPG.convertComboString(combo.getComboString());
                    }
                }

                if(comboString != null){
                    iL.add(" ");
                    iL.add(ChatColor.GRAY + "Current Click Combo: " + ChatColor.WHITE + comboString);
                }

                if(skill.getDescription() != null && !skill.getDescription().isEmpty()){
                    iL.add(" ");

                    for(String s : Util.getWordWrapLore(skill.getDescription())){
                        iL.add(ChatColor.GRAY + s);
                    }
                }

                iL.add(" ");

                if(invested < skill.getMaxInvestingPoints() && u.getCurrentCharacter().getVariables().leftSkillPoints > 0){
                    for(String s : Util.getWordWrapLore("Left-Click to level this skill up at the cost of 1 skill point!"))
                        iL.add(ChatColor.YELLOW + s);
                }

                for(String s : Util.getWordWrapLore("Right-Click to bind this skill to a click combination!"))
                    iL.add(ChatColor.GREEN + s);

                iM.setLore(iL);
                i.setItemMeta(iM);
                i = ItemUtil.setUnbreakable(i);
                i = ItemUtil.hideFlags(i);
                inv.withItem(slot,i,((player, action, item) -> {
                    if(action == ClickType.LEFT){
                        // upgrade

                        if(invested < skill.getMaxInvestingPoints()){
                            if(u.getCurrentCharacter().getVariables().leftSkillPoints > 0){
                                u.getCurrentCharacter().getVariables().addInvestedSkillPoints(skill,1);
                                u.getCurrentCharacter().getVariables().leftSkillPoints--;
                                p.playSound(p.getEyeLocation(), Sound.ORB_PICKUP,1f,1f);
                                openFor(p,page);
                            } else {
                                p.sendMessage(ChatColor.RED + "You don't have enough skill points to upgrade this skill.");
                            }
                        } else {
                            p.sendMessage(ChatColor.RED + "You have already reached the maximum level of this skill.");
                        }
                    } else {
                        // bind

                        openBindingMenu(p,skill,page);
                    }
                }),ClickType.LEFT,ClickType.RIGHT);
            }

            slot++;
        }

        inv.withItem(36, pl);
        inv.withItem(37, pl);
        inv.withItem(38, pl);
        inv.withItem(39, pl);
        inv.withItem(40, pl);
        inv.withItem(41, pl);
        inv.withItem(42, pl);
        inv.withItem(43, pl);
        inv.withItem(44, pl);

        if(page != 1) inv.withItem(47,prev, ((player, clickType, itemStack) -> openFor(p,page-1)), ClickType.LEFT);
        inv.withItem(49,close, ((player, clickType, itemStack) -> GameMenu.openFor(p)), ClickType.LEFT);
        if(maxPages > page) inv.withItem(51,next, ((player, clickType, itemStack) -> openFor(p,page+1)), ClickType.LEFT);

        inv.show(p);
    }

    public static void openBindingMenu(Player p, Skill skill, int returnPage){
        GameUser u = GameUser.getUser(p);
        int invested = u.getCurrentCharacter().getVariables().getInvestedSkillPoints(skill);
        ItemStack close = ItemUtil.namedItem(Material.BARRIER, ChatColor.DARK_RED + "Close", null);
        ItemStack noCombo = ItemUtil.namedItem(Material.INK_SACK, ChatColor.RED + "Unbind Skill", null, 1);
        InventoryMenuBuilder inv = new InventoryMenuBuilder(Util.INVENTORY_1ROW).withTitle("Bind " + skill.getName());

        ItemStack i = new ItemStack(skill.getIcon(),1,(short)skill.getIconDurability());
        ItemMeta iM = i.getItemMeta();
        iM.setDisplayName(ChatColor.GOLD + skill.getName() + " " + ChatColor.YELLOW + "[Lv. " + invested + "/" + skill.getMaxInvestingPoints() + "]");
        ArrayList<String> iL = new ArrayList<String>();
        iL.add(ChatColor.GRAY + "Lv. Min: " + ChatColor.WHITE + skill.getMinLevel());

        HashMap<String,String> effects = skill.getEffects(invested);
        if(effects != null && effects.size() > 0){
            for(String effectName : effects.keySet()){
                String effect = effects.get(effectName);

                iL.add(ChatColor.GRAY + effectName + ": " + ChatColor.WHITE + effect);
            }
        }

        String comboString = null;
        ClickComboType combo = u.getCurrentCharacter().getVariables().getComboFromSkill(skill);
        if(combo != null){
            if(u.getCurrentCharacter().getRpgClass().matches(RPGClass.ARCHER)){
                comboString = DungeonRPG.convertComboString(combo.getAlternateComboString());
            } else {
                comboString = DungeonRPG.convertComboString(combo.getComboString());
            }
        }

        if(comboString != null){
            iL.add(" ");
            iL.add(ChatColor.GRAY + "Current Click Combo: " + ChatColor.WHITE + comboString);
        }

        if(skill.getDescription() != null && !skill.getDescription().isEmpty()){
            iL.add(" ");

            for(String s : Util.getWordWrapLore(skill.getDescription())){
                iL.add(ChatColor.GRAY + s);
            }
        }

        iM.setLore(iL);
        i.setItemMeta(iM);
        i = ItemUtil.setUnbreakable(i);
        i = ItemUtil.hideFlags(i);

        inv.withItem(0,i);

        b(2,inv,p,skill,ClickComboType.COMBO_1,returnPage);
        b(3,inv,p,skill,ClickComboType.COMBO_2,returnPage);
        b(4,inv,p,skill,ClickComboType.COMBO_3,returnPage);
        b(5,inv,p,skill,ClickComboType.COMBO_4,returnPage);

        if(u.getCurrentCharacter().getVariables().getComboFromSkill(skill) != null) inv.withItem(6,noCombo,((player, action, item) -> {
            u.getCurrentCharacter().getVariables().setSkillForCombo(u.getCurrentCharacter().getVariables().getComboFromSkill(skill),null);
            p.playSound(p.getEyeLocation(), Sound.ORB_PICKUP,1f,1f);
            openFor(p,returnPage);
        }),ClickType.LEFT);

        inv.withItem(8,close,((player, action, item) -> {
            openFor(p,returnPage);
        }),ClickType.LEFT);

        inv.show(p);
    }

    private static void b(int slot, InventoryMenuBuilder inv, Player p, Skill skill, ClickComboType comboType, int returnPage){
        GameUser u = GameUser.getUser(p);

        Skill currentSkill = u.getCurrentCharacter().getVariables().getSkillFromCombo(comboType);

        if(currentSkill != null){
            ChatColor c = currentSkill == skill ? ChatColor.DARK_GREEN : ChatColor.GREEN;

            inv.withItem(slot,ItemUtil.namedItem(Material.INK_SACK,c + DungeonRPG.convertComboString(comboType.getMatchingComboString(u.getCurrentCharacter().getRpgClass())),new String[]{
                    ChatColor.GRAY + "Current Skill: " + ChatColor.WHITE + currentSkill.getName()," ",
                    ChatColor.YELLOW + "Click to bind " + skill.getName() + " to this combo!"
            }, currentSkill == skill ? 2 : 10),((player, action, item) -> {
                u.getCurrentCharacter().getVariables().setSkillForCombo(comboType,skill);
                p.playSound(p.getEyeLocation(), Sound.ORB_PICKUP,1f,1f);
                openFor(p,returnPage);
            }),ClickType.LEFT);
        } else {
            inv.withItem(slot,ItemUtil.namedItem(Material.INK_SACK,ChatColor.DARK_GRAY + DungeonRPG.convertComboString(comboType.getMatchingComboString(u.getCurrentCharacter().getRpgClass())),new String[]{
                    ChatColor.GRAY + "Current Skill: " + ChatColor.WHITE + "(none)"," ",
                    ChatColor.YELLOW + "Click to bind " + skill.getName() + " to this combo!"
            }, 8),((player, action, item) -> {
                u.getCurrentCharacter().getVariables().setSkillForCombo(comboType,skill);
                p.playSound(p.getEyeLocation(), Sound.ORB_PICKUP,1f,1f);
                openFor(p,returnPage);
            }),ClickType.LEFT);
        }
    }
}
