package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.inv.MerchantSetupMenu;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.items.ItemData;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.npc.CustomNPC;
import net.wrathofdungeons.dungeonrpg.npc.CustomNPCType;
import net.wrathofdungeons.dungeonrpg.npc.MerchantOffer;
import net.wrathofdungeons.dungeonrpg.npc.MerchantOfferCost;
import net.wrathofdungeons.dungeonrpg.quests.Quest;
import net.wrathofdungeons.dungeonrpg.quests.QuestObjective;
import net.wrathofdungeons.dungeonrpg.quests.QuestObjectiveType;
import net.wrathofdungeons.dungeonrpg.quests.QuestStage;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

import java.util.ArrayList;

public class ChatListener implements Listener {
    @EventHandler
    public void onChat(PlayerChatEvent e){
        Player p = e.getPlayer();
        String msg = e.getMessage();

        if(GameUser.isLoaded(p)){
            GameUser u = GameUser.getUser(p);

            if(u.getCurrentCharacter() != null){
                e.setCancelled(true);

                p.sendMessage(ChatColor.GRAY + "[" + u.getCurrentCharacter().getRpgClass().getName().substring(0,2) + u.getCurrentCharacter().getLevel() + "] " + u.getRank().getColor() + p.getName() + ": " + ChatColor.WHITE + msg);

                for(Entity entity : p.getNearbyEntities(60,60,60)){
                    if(entity instanceof Player){
                        if(CustomEntity.fromEntity((LivingEntity)entity) == null){
                            Player p2 = (Player)entity;

                            if(GameUser.isLoaded(p2)){
                                GameUser u2 = GameUser.getUser(p2);

                                if(u2.getCurrentCharacter() != null){
                                    double distance = p.getLocation().distance(p2.getLocation());

                                    ChatColor color = null;

                                    if(distance < 35){
                                        color = ChatColor.WHITE;
                                    } else if(distance > 35 && distance < 65){
                                        color = ChatColor.GRAY;
                                    } else if(distance > 65 && distance < 90){
                                        color = ChatColor.DARK_GRAY;
                                    }

                                    if(color != null){
                                        p2.sendMessage(ChatColor.GRAY + "[" + u.getCurrentCharacter().getRpgClass().getName().substring(0,2) + u.getCurrentCharacter().getLevel() + "] " + u.getRank().getColor() + p.getName() + ": " + color + msg);
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                e.setCancelled(true);

                if(u.isInSetupMode()){
                    if(u.merchantAddItem != null){
                        if(u.merchantAddItemHandle == null){
                            msg = msg.trim();
                            String[] s = msg.split(":");

                            if(!msg.equalsIgnoreCase("cancel")){
                                if(s.length == 1 || s.length == 2){
                                    if(Util.isValidInteger(s[0])){
                                        int itemID = Integer.parseInt(s[0]);

                                        if(ItemData.getData(itemID) != null){
                                            int amount = 1;

                                            if(s.length == 2){
                                                if(Util.isValidInteger(s[1])){
                                                    amount = Integer.parseInt(s[1]);
                                                } else {
                                                    p.sendMessage(ChatColor.RED + "Please enter a valid integer.");
                                                    return;
                                                }
                                            }

                                            u.merchantAddItemHandle = new CustomItem(itemID,amount);
                                            p.sendMessage(ChatColor.GOLD + "Enter the amount of gold nuggets players should be charged for this item.");
                                            p.sendMessage(ChatColor.GRAY + "(Type 0 to skip.)");
                                            p.sendMessage(ChatColor.GRAY + "(Type 'cancel' to cancel.)");
                                        } else {
                                            p.sendMessage(ChatColor.RED + "That item doesn't exist.");
                                        }
                                    } else {
                                        p.sendMessage(ChatColor.RED + "Please enter a valid integer.");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "Invalid format. Use ID[:AMOUNT]!");
                                }
                            } else {
                                u.merchantAddItem = null;
                                u.merchantAddItemHandle = null;
                                u.merchantAddItemCosts = null;
                                u.merchantAddMoneyCost = -1;
                                u.merchantAddItemSlot = -1;
                                p.sendMessage(ChatColor.RED + "Operation aborted.");
                            }
                        } else if(u.merchantAddMoneyCost == -1){
                            msg = msg.trim();

                            if(Util.isValidInteger(msg)){
                                int money = Integer.parseInt(msg);
                                if(money < 0) money = 0;

                                if(money == 0){
                                    p.sendMessage(ChatColor.GRAY + "Skipped money cost..");
                                } else {
                                    p.sendMessage(ChatColor.GREEN + "Money cost set to: " + money + " gold nugget(s).");
                                }

                                p.sendMessage(ChatColor.GOLD + "Enter item costs that should be charged additionally to the gold nugget cost.");
                                p.sendMessage(ChatColor.GOLD + "Use ID[:AMOUNT] as a format template.");
                                p.sendMessage(ChatColor.GRAY + "(Type 'skip' to skip.)");
                                p.sendMessage(ChatColor.GRAY + "(Type 'cancel' to cancel.)");

                                u.merchantAddMoneyCost = money;
                            } else {
                                if(msg.equalsIgnoreCase("cancel")){
                                    u.merchantAddItem = null;
                                    u.merchantAddItemHandle = null;
                                    u.merchantAddItemCosts = null;
                                    u.merchantAddMoneyCost = -1;
                                    u.merchantAddItemSlot = -1;
                                    p.sendMessage(ChatColor.RED + "Operation aborted.");
                                } else {
                                    p.sendMessage(ChatColor.RED + "Please enter a valid integer.");
                                }
                            }
                        } else {
                            msg = msg.trim();
                            String[] s = msg.split(":");
                            if(u.merchantAddItemCosts == null) u.merchantAddItemCosts = new ArrayList<CustomItem>();
                            int costAmount = u.merchantAddItemCosts.size();

                            if(msg.equalsIgnoreCase("cancel")){
                                u.merchantAddItem = null;
                                u.merchantAddItemHandle = null;
                                u.merchantAddItemCosts = null;
                                u.merchantAddMoneyCost = -1;
                                u.merchantAddItemSlot = -1;
                                p.sendMessage(ChatColor.RED + "Operation aborted.");
                            } else if(msg.equalsIgnoreCase("skip")){
                                if(costAmount > 0 || u.merchantAddMoneyCost > 0){
                                    MerchantOffer offer = new MerchantOffer();
                                    offer.moneyCost = u.merchantAddMoneyCost;
                                    offer.itemToBuy = u.merchantAddItemHandle.getData().getId();
                                    offer.amount = u.merchantAddItemHandle.getAmount();
                                    offer.slot = u.merchantAddItemSlot;
                                    offer.itemCost = new ArrayList<MerchantOfferCost>();

                                    for(CustomItem i : u.merchantAddItemCosts){
                                        MerchantOfferCost c = new MerchantOfferCost();
                                        c.item = i.getData().getId();
                                        c.amount = i.getAmount();
                                        offer.itemCost.add(c);
                                    }

                                    u.merchantAddItem.getOffers().add(offer);
                                    u.merchantAddItem.setHasUnsavedData(true);

                                    p.sendMessage(ChatColor.GREEN + "Offer added!");
                                    MerchantSetupMenu.open(p,u.merchantAddItem);

                                    u.merchantAddItem = null;
                                    u.merchantAddItemHandle = null;
                                    u.merchantAddItemCosts = null;
                                    u.merchantAddMoneyCost = -1;
                                    u.merchantAddItemSlot = -1;
                                } else {
                                    p.sendMessage(ChatColor.RED + "You have to add at least one cost!");
                                }
                            } else {
                                if(s.length == 1 || s.length == 2){
                                    if(Util.isValidInteger(s[0])){
                                        int itemID = Integer.parseInt(s[0]);

                                        if(ItemData.getData(itemID) != null){
                                            int amount = 1;

                                            if(s.length == 2){
                                                if(Util.isValidInteger(s[1])){
                                                    amount = Integer.parseInt(s[1]);
                                                } else {
                                                    p.sendMessage(ChatColor.RED + "Please enter a valid integer.");
                                                    return;
                                                }
                                            }

                                            u.merchantAddItemCosts.add(new CustomItem(itemID,amount));
                                            costAmount++;

                                            if(costAmount < 5){
                                                p.sendMessage(ChatColor.GOLD + "Items left to be added: " + String.valueOf(5-costAmount));
                                                p.sendMessage(ChatColor.GOLD + "Use ID[:AMOUNT] as a format template.");
                                                p.sendMessage(ChatColor.GRAY + "(Type 'skip' to skip.)");
                                                p.sendMessage(ChatColor.GRAY + "(Type 'cancel' to cancel.)");
                                            } else {
                                                MerchantOffer offer = new MerchantOffer();
                                                offer.moneyCost = u.merchantAddMoneyCost;
                                                offer.itemToBuy = u.merchantAddItemHandle.getData().getId();
                                                offer.amount = u.merchantAddItemHandle.getAmount();
                                                offer.slot = u.merchantAddItemSlot;
                                                offer.itemCost = new ArrayList<MerchantOfferCost>();

                                                for(CustomItem i : u.merchantAddItemCosts){
                                                    MerchantOfferCost c = new MerchantOfferCost();
                                                    c.item = i.getData().getId();
                                                    c.amount = i.getAmount();
                                                    offer.itemCost.add(c);
                                                }

                                                u.merchantAddItem.getOffers().add(offer);
                                                u.merchantAddItem.setHasUnsavedData(true);

                                                p.sendMessage(ChatColor.GREEN + "Offer added!");
                                                MerchantSetupMenu.open(p,u.merchantAddItem);

                                                u.merchantAddItem = null;
                                                u.merchantAddItemHandle = null;
                                                u.merchantAddItemCosts = null;
                                                u.merchantAddMoneyCost = -1;
                                                u.merchantAddItemSlot = -1;
                                            }
                                        } else {
                                            p.sendMessage(ChatColor.RED + "That item doesn't exist.");
                                        }
                                    } else {
                                        p.sendMessage(ChatColor.RED + "Please enter a valid integer.");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "Invalid format. Use ID[:AMOUNT]!");
                                }
                            }
                        }
                    } /*else if(u.npcAddTextLine != null){
                        msg = msg.trim();

                        if(msg.equalsIgnoreCase("cancel")){
                            p.sendMessage(ChatColor.RED + "Operation aborted.");
                            u.npcAddTextLine = null;
                        } else {
                            u.npcAddTextLine.addTextLine(msg);
                            u.npcAddTextLine = null;
                            p.sendMessage(ChatColor.GREEN + "Line added!");
                        }
                    } */else if(u.stageAdding != null){
                        msg = msg.trim();

                        if(msg.equalsIgnoreCase("skip")){
                            u.stageAdding = null;
                            u.objectiveAdding = null;

                            p.sendMessage(ChatColor.GREEN + "Finished.");
                            return;
                        }

                        if(u.stageAdding.objectives.length == 0 && u.objectiveAdding == null){
                            QuestObjectiveType type = null;

                            for(QuestObjectiveType t : QuestObjectiveType.values()) if(t.toString().equalsIgnoreCase(msg)) type = t;

                            if(type != null){
                                if(type == QuestObjectiveType.KILL_MOBS){
                                    p.sendMessage(ChatColor.GOLD + "Enter the ID of the mob that you want to add.");
                                    p.sendMessage(ChatColor.GOLD + "Use ID[:AMOUNT] as a format template.");
                                    p.sendMessage(ChatColor.GRAY + "(Type 'cancel' to cancel.)");
                                } else if(type == QuestObjectiveType.FIND_ITEM){
                                    p.sendMessage(ChatColor.GOLD + "Enter the ID of the item that you want to add.");
                                    p.sendMessage(ChatColor.GOLD + "Use ID[:AMOUNT] as a format template.");
                                    p.sendMessage(ChatColor.GRAY + "(Type 'cancel' to cancel.)");
                                } else if(type == QuestObjectiveType.TALK_TO_NPC){
                                    p.sendMessage(ChatColor.GOLD + "Enter the ID of the NPC that you want to add.");
                                    p.sendMessage(ChatColor.GRAY + "(Type 'cancel' to cancel.)");
                                } else {
                                    p.sendMessage(ChatColor.RED + "Please enter a valid type.");
                                    return;
                                }

                                QuestObjective o = new QuestObjective();
                                o.type = type;
                                u.objectiveAdding = o;
                            } else {
                                if(msg.equalsIgnoreCase("cancel")){
                                    p.sendMessage(ChatColor.RED + "Operation aborted.");
                                    u.stageAdding = null;
                                } else {
                                    p.sendMessage(ChatColor.RED + "Please enter a valid type.");
                                }
                            }
                        } else if(u.objectiveAdding != null){
                            if(msg.equalsIgnoreCase("cancel")){
                                p.sendMessage(ChatColor.RED + "Operation aborted.");
                                u.stageAdding = null;
                                u.objectiveAdding = null;
                                return;
                            }

                            if(u.objectiveAdding.type == QuestObjectiveType.KILL_MOBS){

                            } else if(u.objectiveAdding.type == QuestObjectiveType.FIND_ITEM){
                                String[] s = msg.split(":");
                                if(s.length == 1 || s.length == 2){
                                    if(Util.isValidInteger(s[0])){
                                        int amount = 1;

                                        if(s.length == 2){
                                            if(Util.isValidInteger(s[1])){
                                                amount = Integer.parseInt(s[1]);
                                            } else {
                                                p.sendMessage(ChatColor.RED + "Please enter a valid integer.");
                                                return;
                                            }
                                        }

                                        if(ItemData.getData(Integer.parseInt(s[0])) != null){
                                            CustomItem item = new CustomItem(Integer.parseInt(s[0]),amount);
                                            u.objectiveAdding.itemToFind = item.getData().getId();
                                            u.objectiveAdding.itemToFindAmount = item.getAmount();

                                            ArrayList<QuestObjective> ob = new ArrayList<QuestObjective>();
                                            for(QuestObjective o : u.stageAdding.objectives) ob.add(o);
                                            ob.add(u.objectiveAdding);

                                            u.stageAdding.objectives = ob.toArray(new QuestObjective[]{});

                                            ArrayList<QuestStage> ss = new ArrayList<QuestStage>();
                                            for(QuestStage st : u.questModifying.getStages()) ss.add(st);
                                            ss.add(u.stageAdding);

                                            u.questModifying.setStages(ss.toArray(new QuestStage[]{}));

                                            u.objectiveAdding = null;
                                        } else {
                                            p.sendMessage(ChatColor.RED + "That item doesn't exist.");
                                        }
                                    } else {
                                        p.sendMessage(ChatColor.RED + "Please enter a valid integer.");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "Invalid format. Use ID[:AMOUNT]!");
                                }
                            } else if(u.objectiveAdding.type == QuestObjectiveType.TALK_TO_NPC){
                                if(Util.isValidInteger(msg)){
                                    if(CustomNPC.fromID(Integer.parseInt(msg)) != null){
                                        if(CustomNPC.fromID(Integer.parseInt(msg)).getNpcType() == CustomNPCType.QUEST_NPC){
                                            u.objectiveAdding.npcToTalkTo = Integer.parseInt(msg);

                                            ArrayList<QuestObjective> ob = new ArrayList<QuestObjective>();
                                            for(QuestObjective o : u.stageAdding.objectives) ob.add(o);
                                            ob.add(u.objectiveAdding);

                                            u.stageAdding.objectives = ob.toArray(new QuestObjective[]{});

                                            ArrayList<QuestStage> s = new ArrayList<QuestStage>();
                                            for(QuestStage st : u.questModifying.getStages()) s.add(st);
                                            s.add(u.stageAdding);

                                            u.questModifying.setStages(s.toArray(new QuestStage[]{}));

                                            u.objectiveAdding = null;

                                            String sts = "";
                                            for(QuestObjectiveType type : QuestObjectiveType.values()){
                                                if(sts.equals("")){
                                                    sts = type.toString();
                                                } else {
                                                    sts += ", " + type.toString();
                                                }
                                            }

                                            p.sendMessage(ChatColor.RED + "(Objective " + (u.stageAdding.objectives.length+1) + ")");
                                            p.sendMessage(ChatColor.GOLD + "Enter the type of objective you want to add.");
                                            p.sendMessage(ChatColor.GOLD + "The following ones are available: " + ChatColor.GREEN + sts);
                                            p.sendMessage(ChatColor.GREEN + "(Type 'skip' to finish.)");
                                            p.sendMessage(ChatColor.GRAY + "(Type 'cancel' to cancel.)");
                                        } else {
                                            p.sendMessage(ChatColor.RED + "The target NPC has to be a quest npc.");
                                        }
                                    } else {
                                        p.sendMessage(ChatColor.RED + "Please enter a valid ID.");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "Please enter a valid ID.");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "Operation aborted.");
                                u.stageAdding = null;
                                u.objectiveAdding = null;
                            }
                        }
                    }
                }
            }
        } else {
            e.setCancelled(true);
        }
    }
}
