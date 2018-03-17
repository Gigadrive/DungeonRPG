package net.wrathofdungeons.dungeonrpg.listener;

import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.trait.VillagerProfession;
import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.dungeon.Dungeon;
import net.wrathofdungeons.dungeonrpg.event.CustomNPCInteractEvent;
import net.wrathofdungeons.dungeonrpg.inv.*;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.items.ItemData;
import net.wrathofdungeons.dungeonrpg.items.StoredCustomItem;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.npc.CustomNPC;
import net.wrathofdungeons.dungeonrpg.npc.CustomNPCType;
import net.wrathofdungeons.dungeonrpg.npc.dialogue.NPCDialogue;
import net.wrathofdungeons.dungeonrpg.npc.dialogue.NPCDialogueConditionType;
import net.wrathofdungeons.dungeonrpg.party.PartyMember;
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
            } else if(npc.getNpcType() == CustomNPCType.CRYSTAL_SPECIALIST){
                CrystalMenu.openFor(p);
            } else if(npc.getNpcType() == CustomNPCType.MERCHANT){
                npc.openShop(p);
            } else if(npc.getNpcType() == CustomNPCType.GUILD_MASTER){
                GuildMasterMenu.openFor(p);
            } else if(npc.getNpcType() == CustomNPCType.GUILD_BANK_MANAGER){
                // TODO: Add Guild bank
            } else if(npc.getNpcType() == CustomNPCType.PROFESSION_MASTER){
                ProfessionMasterMenu.openFor(p);
            } else if(npc.getNpcType() == CustomNPCType.DUNGEON_KEY_MASTER){
                if(npc.getKeyMasterItem() != null && npc.getKeyMasterItem().getAmount() > 0 && npc.dungeonType != null && npc.dungeonType.getEntryLocation() != null){
                    if(u.hasInInventory(npc.getKeyMasterItem().getData(),npc.getKeyMasterItem().getAmount())){
                        if(u.getParty() != null){
                            if(u.getParty().isLeader(p)){
                                if(u.getCurrentCharacter().getLevel() >= npc.dungeonType.getMinLevel()){
                                    if(!u.getParty().isInDungeon()){
                                        if(u.getParty().loadingDungeon) return;
                                        u.getParty().loadingDungeon = true;

                                        u.removeFromInventory(npc.getKeyMasterItem().getData(),npc.getKeyMasterItem().getAmount());
                                        p.sendMessage(ChatColor.GREEN + "Creating dungeon..");

                                        for(PartyMember m : u.getParty().getMembers()){
                                            Player p2 = m.p;

                                            if(GameUser.isLoaded(p2) && GameUser.getUser(p2).getCurrentCharacter() != null && GameUser.getUser(p2).getCurrentCharacter().getLevel() >= npc.dungeonType.getMinLevel()){
                                                p2.sendMessage(ChatColor.DARK_GREEN + "The party is entering: " + ChatColor.GREEN + npc.dungeonType.getName() + ChatColor.GRAY + " (Min. Level: " + npc.dungeonType.getMinLevel() + ")" + ChatColor.DARK_GREEN + "!");
                                                p2.sendMessage(ChatColor.DARK_GREEN + "Get ready!");
                                            }
                                        }

                                        new BukkitRunnable(){
                                            @Override
                                            public void run() {
                                                try {
                                                    if(u.getParty() == null) return;

                                                    Dungeon dungeon = Dungeon.loadNewTemplate(npc.dungeonType);
                                                    dungeon.setParty(u.getParty());

                                                    if(u.getParty() == null){
                                                        dungeon.unregister();
                                                        return;
                                                    }

                                                    for(PartyMember member : u.getParty().getMembers()){
                                                        Player p2 = member.p;

                                                        if(GameUser.isLoaded(p2)){
                                                            GameUser u2 = GameUser.getUser(p2);

                                                            if(u2.getCurrentCharacter() != null && u2.getParty() == u.getParty()){
                                                                if(u2.getCurrentCharacter().getLevel() >= npc.dungeonType.getMinLevel()){
                                                                    DungeonAPI.sync(() -> p2.teleport(npc.dungeonType.getEntryLocation()));
                                                                }
                                                            }
                                                        }
                                                    }

                                                    p.sendMessage(ChatColor.GREEN + "Dungeon created! Teleporting..");
                                                    u.getParty().loadingDungeon = false;
                                                } catch(Exception ex){
                                                    u.getParty().loadingDungeon = false;
                                                    p.sendMessage(ChatColor.RED + "An error occurred while loading the dungeon. Please try again later.");
                                                    ex.printStackTrace();
                                                }
                                            }
                                        }.runTaskLaterAsynchronously(DungeonRPG.getInstance(),2*20);
                                    } else {
                                        p.sendMessage(ChatColor.DARK_GRAY + "<" + npc.getDisplayName() + ChatColor.DARK_GRAY + "> " + ChatColor.GRAY + "The party is already in a dungeon.");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.DARK_GRAY + "<" + npc.getDisplayName() + ChatColor.DARK_GRAY + "> " + ChatColor.GRAY + "You must be at least level " + npc.dungeonType.getMinLevel() + " to enter this dungeon.");
                                }
                            } else {
                                p.sendMessage(ChatColor.DARK_GRAY + "<" + npc.getDisplayName() + ChatColor.DARK_GRAY + "> " + ChatColor.GRAY + "Only the party leader may enter a dungeon.");
                            }
                        } else {
                            p.sendMessage(ChatColor.DARK_GRAY + "<" + npc.getDisplayName() + ChatColor.DARK_GRAY + "> " + ChatColor.GRAY + "You must be in a party to enter a dungeon.");
                        }
                    } else {
                        p.sendMessage(ChatColor.DARK_GRAY + "<" + npc.getDisplayName() + ChatColor.DARK_GRAY + "> " + ChatColor.GRAY + "This gate requires the following item: " + ChatColor.DARK_AQUA + "[" + npc.getKeyMasterItem().getAmount() + "x " + ChatColor.stripColor(npc.getKeyMasterItem().getData().getName()) + "]");
                    }
                } else if(npc.getKeyMasterItem() != null && npc.getKeyMasterItem().getAmount() > 0 && npc.getKeyMasterLocation() != null){
                    if(u.hasInInventory(npc.getKeyMasterItem().getData(),npc.getKeyMasterItem().getAmount())){
                        u.removeFromInventory(npc.getKeyMasterItem().getData(),npc.getKeyMasterItem().getAmount());
                        p.teleport(npc.getKeyMasterLocation().toBukkitLocation());
                    } else {
                        p.sendMessage(ChatColor.DARK_GRAY + "<" + npc.getDisplayName() + ChatColor.DARK_GRAY + "> " + ChatColor.GRAY + "This gate requires the following item: " + ChatColor.DARK_AQUA + "[" + npc.getKeyMasterItem().getAmount() + "x " + ChatColor.stripColor(npc.getKeyMasterItem().getData().getName()) + "]");
                    }
                }
            } else if(npc.getNpcType() == CustomNPCType.QUEST_NPC){
                if(npc.getDialogues().size() > 0 && !CustomNPC.READING.contains(p.getName())){
                    NPCDialogue dialogue = npc.getPreferredDialogue(p);

                    //if(dialogue == null) p.sendMessage("null");

                    if(dialogue != null && dialogue.lines.size() > 0){
                        //p.sendMessage(dialogue.condition.type.toString());
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
                                    p.sendMessage(ChatColor.GRAY + "[" + (j+1) + "/" + dialogue.lines.size() + "] " + ChatColor.DARK_GRAY + "<" + npc.getDisplayName() + ChatColor.DARK_GRAY + "> " + ChatColor.GRAY + ChatColor.translateAlternateColorCodes('&',line));

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
                                                            for(StoredCustomItem item : q.getRewardItems()){
                                                                item.update();
                                                                p.sendMessage(ChatColor.GRAY + "+" + item.amount + " " + item.getData().getName());

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
                            WorldUtilities.applySkinToNPC(e.getNPC(),c.getData().getSkins().get(Util.randomInteger(0,c.getData().getSkins().size()-1)),c.getData().getSkinName());

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
                } else {
                    DungeonAPI.nmsMakeSilent(e.getNPC().getEntity());
                }
            }
        }
    }
}
