package net.wrathofdungeons.dungeonrpg.inv;

import net.wrathofdungeons.dungeonapi.util.ItemUtil;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.items.ItemData;
import net.wrathofdungeons.dungeonrpg.mobs.MobData;
import net.wrathofdungeons.dungeonrpg.npc.CustomNPC;
import net.wrathofdungeons.dungeonrpg.quests.*;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.util.WorldUtilities;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.inventivetalent.menubuilder.inventory.InventoryMenuBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

public class QuestDiary {
    public static void openFor(Player p){
        openFor(p,1);
    }

    public static void openFor(Player p, int page){
        GameUser u = GameUser.getUser(p);

        ItemStack pl = ItemUtil.namedItem(Material.STAINED_GLASS_PANE, " ", null, 15);
        ItemStack prev = ItemUtil.namedItem(Material.ARROW, ChatColor.GOLD + "<< " + ChatColor.AQUA + "Previous page", null);
        ItemStack next = ItemUtil.namedItem(Material.ARROW, ChatColor.AQUA + "Next page" + ChatColor.GOLD + " >>", null);
        ItemStack close = ItemUtil.namedItem(Material.BARRIER, ChatColor.DARK_RED + "Close", null);

        ArrayList<Quest> quests = new ArrayList<Quest>();
        quests.addAll(Quest.STORAGE.values());

        Collections.sort(quests, new Comparator<Quest>() {
            @Override
            public int compare(Quest q1, Quest q2){
                return q1.getRequiredLevel() - q2.getRequiredLevel();
            }
        });

        int sizePerPage = 36;
        int total = quests.size();

        double d = (((double)total)/((double)sizePerPage));
        int maxPages = ((Double)d).intValue();
        if(maxPages < d) maxPages++;
        if(maxPages <= 0) maxPages = 1;

        InventoryMenuBuilder inv = new InventoryMenuBuilder(Util.MAX_INVENTORY_SIZE);
        inv.withTitle("Quest Diary (" + page + "/" + maxPages + ")");

        int slot = 0;
        for(Quest quest : quests.stream().skip((page-1) * sizePerPage).limit(sizePerPage).collect(Collectors.toCollection(ArrayList::new))){
            QuestProgress progress = u.getCurrentCharacter().getProgress(quest);

            if(progress.status == QuestProgressStatus.NOT_STARTED && u.getCurrentCharacter().getLevel() < quest.getRequiredLevel()){
                // UNAVAILABLE

                ItemStack i = new ItemStack(Material.BARRIER);
                ItemMeta iM = i.getItemMeta();

                iM.setDisplayName(ChatColor.RED + quest.getName());

                ArrayList<String> iL = new ArrayList<String>();
                iL.add(" ");
                iL.add(ChatColor.WHITE + "Lv. Min: " + ChatColor.RED + quest.getRequiredLevel());
                iL.add(ChatColor.WHITE + "Length: " + quest.getLength().getColor() + quest.getLength().getName());
                iL.add(" ");
                iL.add(ChatColor.WHITE + "Status: " + ChatColor.RED + "Unavailable");

                iM.setLore(iL);
                i.setItemMeta(iM);
                inv.withItem(slot,i);
            } else if(progress.status == QuestProgressStatus.NOT_STARTED && u.getCurrentCharacter().getLevel() >= quest.getRequiredLevel()){
                // AVAILABLE

                ItemStack i = new ItemStack(Material.BOOK);
                ItemMeta iM = i.getItemMeta();

                iM.setDisplayName(ChatColor.YELLOW + quest.getName());

                ArrayList<String> iL = new ArrayList<String>();
                iL.add(" ");
                iL.add(ChatColor.WHITE + "Lv. Min: " + ChatColor.GREEN + quest.getRequiredLevel());
                iL.add(ChatColor.WHITE + "Length: " + quest.getLength().getColor() + quest.getLength().getName());
                iL.add(" ");
                iL.add(ChatColor.WHITE + "Status: " + ChatColor.YELLOW + "Not Started");
                for(String s : Util.getWordWrapLore("Talk to " + ChatColor.stripColor(quest.getGiverNPC().getCustomName()) + " at " + WorldUtilities.getReadableLocation(quest.getGiverNPC().getLocation())))
                    iL.add(ChatColor.GRAY + s);

                iM.setLore(iL);
                i.setItemMeta(iM);
                inv.withItem(slot,i);
            } else if(progress.status == QuestProgressStatus.STARTED){
                // STARTED

                ItemStack i = new ItemStack(Material.BOOK_AND_QUILL);
                ItemMeta iM = i.getItemMeta();

                iM.setDisplayName(ChatColor.GOLD + quest.getName());

                ArrayList<String> iL = new ArrayList<String>();
                iL.add(" ");
                iL.add(ChatColor.WHITE + "Lv. Min: " + ChatColor.GREEN + quest.getRequiredLevel());
                iL.add(ChatColor.WHITE + "Length: " + quest.getLength().getColor() + quest.getLength().getName());
                iL.add(" ");
                iL.add(ChatColor.WHITE + "Status: " + ChatColor.GOLD + "Started");
                iL.add(ChatColor.WHITE + "Stage " + ChatColor.GREEN + (progress.questStage+1) + ChatColor.WHITE + "/" + ChatColor.GREEN + (quest.getStages().length) + ChatColor.WHITE + ":");

                int j = 0;
                for(QuestObjective o : quest.getStages()[progress.questStage].objectives){
                    if(o.type == QuestObjectiveType.FIND_ITEM){
                        if(ItemData.getData(o.itemToFind) != null)
                            iL.add(ChatColor.GRAY + "Find " + ChatColor.stripColor(ItemData.getData(o.itemToFind).getName()) + " " + ChatColor.DARK_GRAY + "[" + u.getAmountInInventory(ItemData.getData(o.itemToFind)) + "/" + o.itemToFindAmount + "]");
                    } else if(o.type == QuestObjectiveType.KILL_MOBS){
                        if(MobData.getData(o.mobToKill) != null)
                            iL.add(ChatColor.GRAY + "Kill " + MobData.getData(o.mobToKill).getName() + " " + ChatColor.DARK_GRAY + "[" + progress.objectiveProgress.get(j).killedMobs + "/" + o.mobToKillAmount + "]");
                    } else if(o.type == QuestObjectiveType.TALK_TO_NPC){
                        if(CustomNPC.fromID(o.npcToTalkTo) != null)
                            iL.add(ChatColor.GRAY + "Talk to " + ChatColor.stripColor(CustomNPC.fromID(o.npcToTalkTo).getDisplayName()));
                    }

                    j++;
                }

                iM.setLore(iL);
                i.setItemMeta(iM);
                inv.withItem(slot,i);
            } else if(progress.status == QuestProgressStatus.FINISHED){
                // COMPLETED

                ItemStack i = new ItemStack(Material.ENCHANTED_BOOK);
                ItemMeta iM = i.getItemMeta();

                iM.setDisplayName(ChatColor.GREEN + quest.getName());

                ArrayList<String> iL = new ArrayList<String>();
                iL.add(" ");
                iL.add(ChatColor.WHITE + "Lv. Min: " + ChatColor.GREEN + quest.getRequiredLevel());
                iL.add(ChatColor.WHITE + "Length: " + quest.getLength().getColor() + quest.getLength().getName());
                iL.add(" ");
                iL.add(ChatColor.WHITE + "Status: " + ChatColor.GREEN + "Completed");

                iM.setLore(iL);
                i.setItemMeta(iM);
                inv.withItem(slot,i);
            }

            slot++;

            /*ItemStack i = new ItemStack(0);
            i.setType(progress.status == QuestProgressStatus.STARTED ? Material.BOOK_AND_QUILL : Material.ENCHANTED_BOOK);

            ChatColor c = progress.status == QuestProgressStatus.STARTED ? ChatColor.GOLD : ChatColor.GREEN;
            String n = progress.status == QuestProgressStatus.STARTED ? "Started" : "Completed";

            ItemMeta iM = i.getItemMeta();
            iM.setDisplayName(c + quest.getName());
            ArrayList<String> iL = new ArrayList<String>();
            iL.add(" ");
            iL.add(ChatColor.WHITE + "Lv. Min: " + ChatColor.GREEN + quest.getRequiredLevel());
            iL.add(ChatColor.WHITE + "Length: " + quest.getLength().getColor() + quest.getLength().getName());
            iL.add(" ");
            iL.add(ChatColor.WHITE + "Status: " + c + n);
            if(progress.status == QuestProgressStatus.STARTED){
                iL.add(ChatColor.WHITE + "Stage " + ChatColor.GREEN + (progress.questStage+1) + ChatColor.WHITE + "/" + ChatColor.GREEN + (quest.getStages().length) + ChatColor.WHITE + ":");

                int j = 0;
                for(QuestObjective o : quest.getStages()[progress.questStage].objectives){
                    if(o.type == QuestObjectiveType.FIND_ITEM){
                        if(ItemData.getData(o.itemToFind) != null)
                            iL.add(ChatColor.GRAY + "Find " + ChatColor.stripColor(ItemData.getData(o.itemToFind).getName()) + " " + ChatColor.DARK_GRAY + "[" + u.getAmountInInventory(ItemData.getData(o.itemToFind)) + "/" + o.itemToFindAmount + "]");
                    } else if(o.type == QuestObjectiveType.KILL_MOBS){
                        if(MobData.getData(o.mobToKill) != null)
                            iL.add(ChatColor.GRAY + "Kill " + MobData.getData(o.mobToKill).getName() + " " + ChatColor.DARK_GRAY + "[" + progress.objectiveProgress.get(j).killedMobs + "/" + o.mobToKillAmount + "]");
                    } else if(o.type == QuestObjectiveType.TALK_TO_NPC){
                        if(CustomNPC.fromID(o.npcToTalkTo) != null)
                            iL.add(ChatColor.GRAY + "Talk to " + ChatColor.stripColor(CustomNPC.fromID(o.npcToTalkTo).getDisplayName()));
                    }

                    j++;
                }
            }
            iM.setLore(iL);
            i.setItemMeta(iM);

            i = ItemUtil.setUnbreakable(i);
            i = ItemUtil.hideFlags(i);

            inv.withItem(slot,i);
            slot++;*/
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

        ItemStack info = new ItemStack(Material.ENDER_PEARL);
        ItemMeta infoM = info.getItemMeta();
        infoM.setDisplayName(ChatColor.AQUA + "Quests Info");

        ArrayList<String> infoL = new ArrayList<String>();

        double questsCompleted = 0;
        double questsStarted = 0;
        double questsAvailable = 0;
        double questsTotal = 0;

        for(Quest q : Quest.STORAGE.values()){
            QuestProgress pr = u.getCurrentCharacter().getProgress(q);

            questsTotal++;

            if(pr.status == QuestProgressStatus.NOT_STARTED){
                questsAvailable++;
            } else if(pr.status == QuestProgressStatus.STARTED){
                questsStarted++;
            } else if(pr.status == QuestProgressStatus.FINISHED){
                questsCompleted++;
            }
        }

        double percent = (questsCompleted/questsTotal)*100;

        infoL.add(ChatColor.WHITE + "Quests Completed: " + ChatColor.GREEN + (int)questsCompleted + ChatColor.WHITE + "/" + ChatColor.GRAY + (int)questsTotal + " " + ChatColor.DARK_GRAY + "[" + (int)percent + "%]");
        infoL.add(ChatColor.WHITE + "Quests Started: " + ChatColor.GOLD + (int)questsStarted);
        infoL.add(ChatColor.WHITE + "Quests Available: " + ChatColor.YELLOW + (int)questsAvailable);

        infoM.setLore(infoL);
        info.setItemMeta(infoM);

        inv.withItem(45,info);

        if(page != 1) inv.withItem(47,prev, ((player, clickType, itemStack) -> openFor(p,page-1)), ClickType.LEFT);
        inv.withItem(49,close, ((player, clickType, itemStack) -> GameMenu.openFor(p)), ClickType.LEFT);
        if(maxPages > page) inv.withItem(51,next, ((player, clickType, itemStack) -> openFor(p,page+1)), ClickType.LEFT);

        inv.show(p);
    }
}
