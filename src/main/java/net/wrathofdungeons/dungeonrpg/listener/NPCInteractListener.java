package net.wrathofdungeons.dungeonrpg.listener;

import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.event.CustomNPCInteractEvent;
import net.wrathofdungeons.dungeonrpg.inv.AwakeningMenu;
import net.wrathofdungeons.dungeonrpg.inv.BuyingMerchantMenu;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.items.ItemData;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.npc.CustomNPC;
import net.wrathofdungeons.dungeonrpg.npc.CustomNPCType;
import net.wrathofdungeons.dungeonrpg.npc.dialogue.NPCDialogue;
import net.wrathofdungeons.dungeonrpg.npc.dialogue.NPCDialogueConditionType;
import net.wrathofdungeons.dungeonrpg.quests.*;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.util.WorldUtilities;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class NPCInteractListener implements Listener {
    @EventHandler
    public void onInteact(CustomNPCInteractEvent e){
        Player p = e.getPlayer();
        GameUser u = GameUser.getUser(p);
        CustomNPC npc = e.getNPC();

        if(!u.isInSetupMode()){
            if(npc.getNpcType() == CustomNPCType.BUYING_MERCHANT){
                BuyingMerchantMenu.openFor(p);
            } else if(npc.getNpcType() == CustomNPCType.AWAKENING_SPECIALIST){
                AwakeningMenu.openFor(p);
            } else if(npc.getNpcType() == CustomNPCType.MERCHANT){
                npc.openShop(p);
            } else if(npc.getNpcType() == CustomNPCType.QUEST_NPC){
                if(npc.getDialogues().size() > 0 && !CustomNPC.READING.contains(p.getName())){
                    NPCDialogue dialogue = npc.getPreferredDialogue(p);

                    if(dialogue != null && dialogue.lines.size() > 0){
                        if(dialogue.condition.type == NPCDialogueConditionType.QUEST_ENDING){
                            Quest q = Quest.getQuest(dialogue.condition.questID);

                            if(q != null){
                                if(u.getEmptySlotsInInventory() < q.getSlotsNeededForReward()){
                                    p.sendMessage(ChatColor.RED + "Please empty some space in your inventory.");
                                    return;
                                }
                            }
                        }

                        if(dialogue.condition.type == NPCDialogueConditionType.QUEST_STAGE_STARTING || dialogue.condition.type == NPCDialogueConditionType.QUEST_ENDING){
                            Quest q = Quest.getQuest(dialogue.condition.questID);

                            if(q != null){
                                QuestStage stage = q.getStages()[u.getCurrentCharacter().getCurrentStage(q)];

                                if(stage != null){
                                    for(QuestObjective o : stage.objectives){
                                        if(o.type == QuestObjectiveType.FIND_ITEM){
                                            u.removeFromInventory(ItemData.getData(o.itemToFind),o.itemToFindAmount);
                                        }
                                    }
                                }
                            }
                        }

                        CustomNPC.READING.add(p.getName());

                        int i = 0;
                        for(String line : dialogue.lines){
                            final int j = i;

                            u.getCancellableTasks().add(new BukkitRunnable(){
                                @Override
                                public void run() {
                                    p.sendMessage(ChatColor.DARK_GRAY + "<" + npc.getDisplayName() + ChatColor.DARK_GRAY + "> " + ChatColor.GRAY + ChatColor.translateAlternateColorCodes('&',line));

                                    if(j == dialogue.lines.size()-1){
                                        if(dialogue.condition.type == NPCDialogueConditionType.NONE || dialogue.condition.type == NPCDialogueConditionType.QUEST_NOTSTARTED || dialogue.condition.type == NPCDialogueConditionType.QUEST_RUNNING){
                                            CustomNPC.READING.remove(p.getName());
                                        } else if(dialogue.condition.type == NPCDialogueConditionType.QUEST_STARTING){
                                            Quest q = Quest.getQuest(dialogue.condition.questID);

                                            if(q != null){
                                                u.getCancellableTasks().add(new BukkitRunnable(){
                                                    @Override
                                                    public void run() {
                                                        u.getCurrentCharacter().setQuestStatus(q, QuestProgressStatus.STARTED);
                                                        u.getCurrentCharacter().setCurrentStage(q,0);
                                                        p.sendMessage(ChatColor.GOLD + "New Quest started! " + ChatColor.YELLOW + q.getName());
                                                        p.sendMessage(ChatColor.GOLD + "Check your quest diary for help.");

                                                        CustomNPC.READING.remove(p.getName());
                                                    }
                                                }.runTaskLater(DungeonRPG.getInstance(),DungeonRPG.QUEST_NPC_TEXT_LINE_DELAY*20));
                                            } else {
                                                CustomNPC.READING.remove(p.getName());
                                            }
                                        } else if(dialogue.condition.type == NPCDialogueConditionType.QUEST_STAGE_STARTING){
                                            Quest q = Quest.getQuest(dialogue.condition.questID);

                                            if(q != null){
                                                int newStageIndex = u.getCurrentCharacter().getCurrentStage(q)+1;

                                                u.getCancellableTasks().add(new BukkitRunnable(){
                                                    @Override
                                                    public void run() {
                                                        u.getCurrentCharacter().setCurrentStage(q,newStageIndex);
                                                        p.sendMessage(ChatColor.YELLOW + "Your quest diary has been updated.");

                                                        CustomNPC.READING.remove(p.getName());
                                                    }
                                                }.runTaskLater(DungeonRPG.getInstance(),DungeonRPG.QUEST_NPC_TEXT_LINE_DELAY*20));
                                            } else {
                                                CustomNPC.READING.remove(p.getName());
                                            }
                                        } else if(dialogue.condition.type == NPCDialogueConditionType.QUEST_ENDING){
                                            Quest q = Quest.getQuest(dialogue.condition.questID);

                                            if(q != null){
                                                u.getCancellableTasks().add(new BukkitRunnable(){
                                                    @Override
                                                    public void run() {
                                                        u.getCurrentCharacter().setQuestStatus(q,QuestProgressStatus.FINISHED);

                                                        p.sendMessage(ChatColor.GOLD + "Quest finished: " + ChatColor.YELLOW + q.getName());
                                                        if(q.getRewardExp() > 0){
                                                            u.giveEXP(q.getRewardExp());

                                                            p.sendMessage(ChatColor.GRAY + "+" + q.getRewardExp() + " EXP");
                                                        }

                                                        if(q.getRewardGoldenNuggets() > 0){
                                                            p.sendMessage(ChatColor.GRAY + "+" + q.getRewardGoldenNuggets() + " Golden Nuggets");

                                                            for(CustomItem item : WorldUtilities.convertNuggetAmount(q.getRewardGoldenNuggets())){
                                                                p.getInventory().addItem(item.build(p));
                                                            }
                                                        }

                                                        if(q.getRewardItems().length > 0){
                                                            for(CustomItem item : q.getRewardItems()){
                                                                p.sendMessage(ChatColor.GRAY + "+" + item.getAmount() + " " + item.getData().getName());

                                                                p.getInventory().addItem(item.build(p));
                                                            }
                                                        }

                                                        CustomNPC.READING.remove(p.getName());
                                                    }
                                                }.runTaskLater(DungeonRPG.getInstance(),DungeonRPG.QUEST_NPC_TEXT_LINE_DELAY*20));
                                            } else {
                                                CustomNPC.READING.remove(p.getName());
                                            }
                                        }
                                    }
                                }
                            }.runTaskLater(DungeonRPG.getInstance(),DungeonRPG.QUEST_NPC_TEXT_LINE_DELAY*i*20));

                            i++;
                        }
                    }
                }
            }
        } else {
            p.sendMessage(ChatColor.YELLOW + "NPC ID: " + npc.getId());
            p.sendMessage(ChatColor.YELLOW + "Entity: " + npc.getEntityType().toString());
            p.sendMessage(ChatColor.YELLOW + "Type: " + npc.getNpcType().toString());
        }
    }

    @EventHandler
    public void onSpawn(NPCSpawnEvent e){
        CustomEntity entity = null;

        for(CustomEntity c : CustomEntity.STORAGE.values()){
            if(c.toCitizensNPC() != null && (c.toCitizensNPC() == e.getNPC() || c.toCitizensNPC().getId() == e.getNPC().getId())){
                entity = c;
            }
        }

        if(entity != null){
            entity.setBukkitEntity((LivingEntity)e.getNPC().getEntity());
            final CustomEntity c = entity;

            if(!DungeonRPG.IGNORE_SPAWN_NPC.contains(e.getNPC())){
                if(entity.getBukkitEntity().getType() == EntityType.PLAYER){
                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            DungeonRPG.IGNORE_SPAWN_NPC.add(e.getNPC());
                            WorldUtilities.applySkinToNPC(e.getNPC(),c.getData().getSkin(),c.getData().getSkinName());

                            new BukkitRunnable(){
                                @Override
                                public void run() {
                                    DungeonRPG.IGNORE_SPAWN_NPC.remove(e.getNPC());

                                }
                            }.runTaskLater(DungeonRPG.getInstance(),20);
                        }
                    }.runTaskLater(DungeonRPG.getInstance(),2*20);
                }
            }
        }

        if(!DungeonRPG.IGNORE_SPAWN_NPC.contains(e.getNPC())){
            CustomNPC customNPC = CustomNPC.fromCitizensNPC(e.getNPC());
            if(customNPC != null){
                DungeonAPI.nmsMakeSilent(e.getNPC().getEntity());

                if(customNPC.getEntityType() == EntityType.PLAYER){
                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            DungeonRPG.IGNORE_SPAWN_NPC.add(e.getNPC());
                            WorldUtilities.applySkinToNPC(e.getNPC(),customNPC.getSkin(),customNPC.getSkinName());

                            new BukkitRunnable(){
                                @Override
                                public void run() {
                                    DungeonRPG.IGNORE_SPAWN_NPC.remove(e.getNPC());

                                }
                            }.runTaskLater(DungeonRPG.getInstance(),20);
                        }
                    }.runTaskLater(DungeonRPG.getInstance(),2*20);
                }
            }
        }
    }
}
