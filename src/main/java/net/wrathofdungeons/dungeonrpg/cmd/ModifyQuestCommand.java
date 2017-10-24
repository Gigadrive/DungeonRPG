package net.wrathofdungeons.dungeonrpg.cmd;

import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.items.ItemData;
import net.wrathofdungeons.dungeonrpg.mobs.MobData;
import net.wrathofdungeons.dungeonrpg.npc.CustomNPC;
import net.wrathofdungeons.dungeonrpg.quests.Quest;
import net.wrathofdungeons.dungeonrpg.quests.QuestObjective;
import net.wrathofdungeons.dungeonrpg.quests.QuestObjectiveType;
import net.wrathofdungeons.dungeonrpg.quests.QuestStage;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class ModifyQuestCommand extends Command {
    public ModifyQuestCommand(){
        super(new String[]{"modifyquest","quest"}, Rank.ADMIN);
    }

    public void sendUsage(Player p, String label){
        p.sendMessage(ChatColor.RED + "/createquest <NPC-Type>");
        p.sendMessage(ChatColor.RED + "/" + label + " list");
        p.sendMessage(ChatColor.RED + "/" + label + " <ID> info");
        p.sendMessage(ChatColor.RED + "/" + label + " <ID> stages");
        p.sendMessage(ChatColor.RED + "/" + label + " <ID> rewards");
        p.sendMessage(ChatColor.RED + "/" + label + " <ID> addstage");
        p.sendMessage(ChatColor.RED + "/" + label + " <ID> requiredlevel <Level>");
        p.sendMessage(ChatColor.RED + "/" + label + " <ID> removestage <Stage Index>");
        p.sendMessage(ChatColor.RED + "/" + label + " <ID> addrequiredquest <Quest ID>");
        p.sendMessage(ChatColor.RED + "/" + label + " <ID> removerequiredquest <Quest ID>");
        p.sendMessage(ChatColor.RED + "/" + label + " <ID> rewardnuggets <Amount>");
        p.sendMessage(ChatColor.RED + "/" + label + " <ID> rewardexp <Amount>");
        p.sendMessage(ChatColor.RED + "/" + label + " <ID> removerequiredquest <Quest ID>");
        p.sendMessage(ChatColor.RED + "/" + label + " <ID> addrewarditem");
        p.sendMessage(ChatColor.RED + "/" + label + " <ID> removerewarditem <Item Index>");
    }

    @Override
    public void execute(Player p, String label, String[] args) {
        GameUser u = GameUser.getUser(p);

        if(u.isInSetupMode()){
            if(args.length == 1){
                if(args[0].equalsIgnoreCase("list")){
                    if(Quest.STORAGE.values().size() > 0){
                        for(Quest q : Quest.STORAGE.values()){
                            p.sendMessage(ChatColor.YELLOW + String.valueOf(q.getId()) + ": " + ChatColor.GREEN + q.getName());
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "There are no loaded quests. Use /reload quests to reload the quests from the database.");
                    }
                } else {
                    sendUsage(p,label);
                }
            } else if(args.length == 2){
                if(Util.isValidInteger(args[0])){
                    Quest quest = Quest.getQuest(Integer.parseInt(args[0]));

                    if(quest != null){
                        if(args[1].equalsIgnoreCase("info")){
                            p.sendMessage(ChatColor.YELLOW + "Name: " + ChatColor.GREEN + quest.getName());
                            p.sendMessage(ChatColor.YELLOW + "Req. Level: " + ChatColor.GREEN + quest.getRequiredLevel());
                            p.sendMessage(ChatColor.YELLOW + "Stages: " + ChatColor.GREEN + quest.getStages().length);
                            p.sendMessage(ChatColor.YELLOW + "Rewards: ");
                            p.sendMessage(ChatColor.YELLOW + "      Golden Nuggets: " + ChatColor.GREEN + quest.getRewardGoldenNuggets());
                            p.sendMessage(ChatColor.YELLOW + "      EXP: " + ChatColor.GREEN + quest.getRewardExp());
                            p.sendMessage(ChatColor.YELLOW + "      Items: " + ChatColor.GREEN + quest.getRewardItems().length);
                        } else if(args[1].equalsIgnoreCase("stages")){
                            if(quest.getStages().length > 0){
                                int i = 0;

                                for(QuestStage stage : quest.getStages()){
                                    p.sendMessage(ChatColor.YELLOW + String.valueOf(i) + ": ");
                                    p.sendMessage(ChatColor.YELLOW + "      Objectives: " + ChatColor.GREEN + stage.objectives.length);
                                    if(stage.objectives.length > 0){
                                        int j = 0;
                                        for(QuestObjective objective : stage.objectives){
                                            p.sendMessage(ChatColor.YELLOW + "          " + j + ":");
                                            p.sendMessage(ChatColor.YELLOW + "              Type: " + ChatColor.GREEN + objective.type.toString());

                                            if(objective.type == QuestObjectiveType.FIND_ITEM && ItemData.getData(objective.itemToFind) != null){
                                                p.sendMessage(ChatColor.YELLOW + "              Item to find: " + ChatColor.GREEN + String.valueOf(objective.itemToFindAmount) + "x " + ChatColor.stripColor(ItemData.getData(objective.itemToFind).getName()));
                                            } else if(objective.type == QuestObjectiveType.KILL_MOBS && MobData.getData(objective.mobToKill) != null){
                                                p.sendMessage(ChatColor.YELLOW + "              Mob to kill: " + ChatColor.GREEN + String.valueOf(objective.mobToKillAmount) + "x " + ChatColor.stripColor(MobData.getData(objective.mobToKill).getName()));
                                            } else if(objective.type == QuestObjectiveType.TALK_TO_NPC && CustomNPC.fromID(objective.npcToTalkTo) != null){
                                                p.sendMessage(ChatColor.YELLOW + "              NPC to talk to: " + ChatColor.GREEN + ChatColor.stripColor(CustomNPC.fromID(objective.npcToTalkTo).getDisplayName()));
                                            }

                                            j++;
                                        }
                                    }

                                    /*p.sendMessage(ChatColor.YELLOW + "      Items to get: " + ChatColor.GREEN + stage.itemsToGet.length);
                                    if(stage.itemsToGet.length > 0){
                                        int j = 0;
                                        for(CustomItem item : stage.itemsToGet){
                                            p.sendMessage(ChatColor.YELLOW + "          " + j + ":" + ChatColor.GREEN + " " + item.getAmount() + "x " + ChatColor.stripColor(item.getData().getName()));

                                            j++;
                                        }
                                    }*/

                                    if(stage.hint != null && !stage.hint.isEmpty()){
                                        p.sendMessage(ChatColor.YELLOW + "      Hint: " + ChatColor.GREEN + stage.hint);
                                    }

                                    i++;
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "This quest doesn't have any stages.");
                            }
                        } else if(args[1].equalsIgnoreCase("rewards")){
                            p.sendMessage(ChatColor.YELLOW + "Rewards: ");
                            p.sendMessage(ChatColor.YELLOW + "      Golden Nuggets: " + ChatColor.GREEN + quest.getRewardGoldenNuggets());
                            p.sendMessage(ChatColor.YELLOW + "      EXP: " + ChatColor.GREEN + quest.getRewardExp());
                            p.sendMessage(ChatColor.YELLOW + "      Items: " + ChatColor.GREEN + quest.getRewardItems().length);
                            if(quest.getRewardItems().length > 0){
                                for(CustomItem reward : quest.getRewardItems()){
                                    p.sendMessage(ChatColor.YELLOW + "          " + ChatColor.stripColor(reward.getData().getName()) + ": " + ChatColor.GREEN + reward.getAmount() + "x");
                                }
                            }
                        } else if(args[1].equalsIgnoreCase("addstage")){
                            u.stageAdding = new QuestStage();
                            u.questModifying = quest;

                            String s = "";
                            for(QuestObjectiveType type : QuestObjectiveType.values()){
                                if(s.equals("")){
                                    s = type.toString();
                                } else {
                                    s += ", " + type.toString();
                                }
                            }

                            p.sendMessage(ChatColor.RED + "(Objective 1)");
                            p.sendMessage(ChatColor.GOLD + "Enter the type of objective you want to add.");
                            p.sendMessage(ChatColor.GOLD + "The following ones are available: " + ChatColor.GREEN + s);
                            p.sendMessage(ChatColor.GRAY + "(Type 'cancel' to cancel.)");
                        } else if(args[1].equalsIgnoreCase("addrewarditem")){
                            u.questModifying = quest;
                            u.definingRewardItems = true;

                            p.sendMessage(ChatColor.GOLD + "Enter the ID of the item that you want to add as a reward.");
                            p.sendMessage(ChatColor.GOLD + "Use ID[:AMOUNT] as a format template.");
                            p.sendMessage(ChatColor.GRAY + "(Type 'cancel' to cancel.)");
                        } else {
                            sendUsage(p,label);
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "Please enter a valid quest ID.");
                    }
                } else {
                    p.sendMessage(ChatColor.RED + "Please enter a valid quest ID.");
                }
            } else if(args.length == 3){
                if(Util.isValidInteger(args[0])){
                    Quest quest = Quest.getQuest(Integer.parseInt(args[0]));

                    if(quest != null){
                        if(args[1].equalsIgnoreCase("removestage")){
                            if(Util.isValidInteger(args[2])){
                                int index = Integer.parseInt(args[2]);

                                if(index < quest.getStages().length && index >= 0){
                                    ArrayList<QuestStage> stages = new ArrayList<QuestStage>();

                                    int i = 0;
                                    for(QuestStage stage : quest.getStages()){
                                        if(i == index){
                                            i++;
                                            continue;
                                        }

                                        stages.add(stage);
                                    }

                                    quest.setStages(stages.toArray(new QuestStage[]{}));

                                    p.sendMessage(ChatColor.GREEN + "Stage removed.");
                                } else {
                                    p.sendMessage(ChatColor.RED + "Please enter a valid index number.");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "Please enter a valid number.");
                            }
                        } else if(args[1].equalsIgnoreCase("addrequiredquest")){

                        } else if(args[1].equalsIgnoreCase("removerequiredquest")){

                        } else if(args[1].equalsIgnoreCase("rewardnuggets")){
                            if(Util.isValidInteger(args[2])){
                                int nuggets = Integer.parseInt(args[2]);

                                if(nuggets >= 0){
                                    quest.setRewardGoldenNuggets(nuggets);
                                    p.sendMessage(ChatColor.GREEN + "Reward nuggets set to: " + nuggets);
                                } else {
                                    p.sendMessage(ChatColor.RED + "Please enter a valid number.");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "Please enter a valid number.");
                            }
                        } else if(args[1].equalsIgnoreCase("rewardexp")){
                            if(Util.isValidInteger(args[2])){
                                int exp = Integer.parseInt(args[2]);

                                if(exp >= 0){
                                    quest.setRewardExp(exp);
                                    p.sendMessage(ChatColor.GREEN + "Reward EXP set to: " + exp);
                                } else {
                                    p.sendMessage(ChatColor.RED + "Please enter a valid number.");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "Please enter a valid number.");
                            }
                        } else if(args[1].equalsIgnoreCase("requiredlevel")){
                            if(Util.isValidInteger(args[2])){
                                int level = Integer.parseInt(args[2]);

                                if(level >= 1){
                                    quest.setRequiredLevel(level);
                                    p.sendMessage(ChatColor.GREEN + "Required Level set to: " + level);
                                } else {
                                    p.sendMessage(ChatColor.RED + "Please enter a valid number.");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "Please enter a valid number.");
                            }
                        } else if(args[1].equalsIgnoreCase("removerewarditem")){
                            if(Util.isValidInteger(args[2])){
                                int index = Integer.parseInt(args[2]);

                                if(index < quest.getRewardItems().length && index >= 0){
                                    ArrayList<CustomItem> items = new ArrayList<CustomItem>();

                                    int i = 0;
                                    for(CustomItem item : quest.getRewardItems()){
                                        if(i == index) continue;

                                        items.add(item);
                                    }

                                    quest.setRewardItems(items.toArray(new CustomItem[]{}));

                                    p.sendMessage(ChatColor.GREEN + "Updated reward items.");
                                } else {
                                    p.sendMessage(ChatColor.RED + "Please enter a valid index number.");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "Please enter a valid number.");
                            }
                        } else {
                            sendUsage(p,label);
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "Please enter a valid quest ID.");
                    }
                } else {
                    p.sendMessage(ChatColor.RED + "Please enter a valid quest ID.");
                }
            } else {
                sendUsage(p,label);
            }
        } else {
            p.sendMessage(ChatColor.RED + "You are not in setup mode.");
        }
    }
}
