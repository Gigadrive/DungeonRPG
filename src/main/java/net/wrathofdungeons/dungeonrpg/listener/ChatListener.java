package net.wrathofdungeons.dungeonrpg.listener;

import com.google.gson.Gson;
import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.MySQLManager;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.guilds.*;
import net.wrathofdungeons.dungeonrpg.inv.MerchantSetupMenu;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.items.ItemData;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.mobs.MobData;
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
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;

public class ChatListener implements Listener {
    @EventHandler
    public void onChat(PlayerChatEvent e){
        Player p = e.getPlayer();
        String msg = e.getMessage().trim();

        if(GameUser.isLoaded(p)){
            GameUser u = GameUser.getUser(p);

            if(u.getCurrentCharacter() != null){
                e.setCancelled(true);

                if(u.guildCreationStatus != null){
                    if(msg.equalsIgnoreCase("cancel")){
                        p.sendMessage(ChatColor.RED + "The operation has been cancelled.");

                        GuildUtil.releaseReservedName(u.guildCreationName);
                        u.guildCreationStatus = null;
                        u.guildCreationName = null;
                        u.guildCreationTag = null;
                    } else {
                        if(u.guildCreationStatus == GuildCreationStatus.CHOOSING_NAME && u.guildCreationName == null && u.guildCreationTag == null){
                            String name = msg;

                            if(name.length() >= 3 && name.length() <= 20){
                                if(Util.isAlphaNumeric(name.replace(" ",""))){
                                    DungeonAPI.async(() -> {
                                        if(GuildUtil.isNameAvailable(name)){
                                            u.guildCreationStatus = GuildCreationStatus.CHOOSING_TAG;
                                            u.guildCreationName = name;

                                            try {
                                                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `reservedGuildNames` (`name`,`uuid`) VALUES(?,?);");
                                                ps.setString(1,name);
                                                ps.setString(2,p.getUniqueId().toString());
                                                ps.executeUpdate();
                                                ps.close();

                                                p.sendMessage(" ");
                                                p.sendMessage(ChatColor.DARK_AQUA + "Please choose a tag for your guild, by typing it in the chat.");
                                                p.sendMessage(ChatColor.GRAY + "(Type 'cancel' to cancel the creation.)");
                                                p.sendMessage(" ");
                                            } catch(Exception e1){
                                                e1.printStackTrace();
                                                p.sendMessage(ChatColor.RED + "An error occurred.");
                                                p.sendMessage(ChatColor.RED + "The operation has been cancelled.");

                                                GuildUtil.releaseReservedName(u.guildCreationName);
                                                u.guildCreationStatus = null;
                                                u.guildCreationName = null;
                                                u.guildCreationTag = null;
                                            }
                                        } else {
                                            p.sendMessage(ChatColor.RED + "That name is not available.");
                                        }
                                    });
                                } else {
                                    p.sendMessage(ChatColor.RED + "A guild name must be alphanumeric (only letters and numbers).");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "A guild name must be 3-20 characters long.");
                            }
                        } else if(u.guildCreationStatus == GuildCreationStatus.CHOOSING_TAG && u.guildCreationName != null && u.guildCreationTag == null){
                            String tag = msg;

                            if(tag.length() >= 2 && tag.length() <= 5){
                                if(Util.isAlphaNumeric(tag)){
                                    DungeonAPI.async(() -> {
                                        if(GuildUtil.isTagAvailable(tag)){
                                            String name = u.guildCreationName;

                                            if(u.getTotalMoneyInInventory() >= 4096*3){
                                                try {
                                                    Gson gson = new Gson();
                                                    ArrayList<GuildMember> members = new ArrayList<GuildMember>();
                                                    GuildMember m = new GuildMember();
                                                    m.setUUID(p.getUniqueId());
                                                    m.setGuildRank(GuildRank.LEADER);
                                                    m.setTimeJoined(new Timestamp(System.currentTimeMillis()));
                                                    members.add(m);
                                                    String membersSerialized = gson.toJson(members);

                                                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `guilds` (`name`,`tag`,`creator`,`members`) VALUES(?,?,?,?);", Statement.RETURN_GENERATED_KEYS);
                                                    ps.setString(1,name);
                                                    ps.setString(2,tag);
                                                    ps.setString(3,p.getUniqueId().toString());
                                                    ps.setString(4,membersSerialized);
                                                    ps.executeUpdate();

                                                    ResultSet rs = ps.getGeneratedKeys();
                                                    int guildID = -1;
                                                    if(rs.first()) guildID = rs.getInt(1);

                                                    MySQLManager.getInstance().closeResources(rs,ps);

                                                    if(guildID > -1){
                                                        Guild.getGuild(guildID);
                                                        u.setGuildID(guildID);
                                                        GuildUtil.releaseReservedName(u.guildCreationName);

                                                        u.guildCreationStatus = null;
                                                        u.guildCreationName = null;
                                                        u.guildCreationTag = null;

                                                        u.removeMoneyFromInventory(4096*3);
                                                        p.sendMessage(ChatColor.GREEN + "Your guild has been created!");
                                                        p.playSound(p.getEyeLocation(), Sound.LEVEL_UP,1f,1f);
                                                        DungeonRPG.updateNames();
                                                    } else {
                                                        p.sendMessage(ChatColor.RED + "An error occurred.");
                                                        p.sendMessage(ChatColor.RED + "The operation has been cancelled.");

                                                        GuildUtil.releaseReservedName(u.guildCreationName);
                                                        u.guildCreationStatus = null;
                                                        u.guildCreationName = null;
                                                        u.guildCreationTag = null;
                                                    }
                                                } catch(Exception e1){
                                                    e1.printStackTrace();
                                                    p.sendMessage(ChatColor.RED + "An error occurred.");
                                                    p.sendMessage(ChatColor.RED + "The operation has been cancelled.");

                                                    GuildUtil.releaseReservedName(u.guildCreationName);
                                                    u.guildCreationStatus = null;
                                                    u.guildCreationName = null;
                                                    u.guildCreationTag = null;
                                                }
                                            } else {
                                                p.sendMessage(ChatColor.RED + "You do not have enough money to create a guild.");
                                            }
                                        } else {
                                            p.sendMessage(ChatColor.RED + "That tag is not available.");
                                        }
                                    });
                                } else {
                                    p.sendMessage(ChatColor.RED + "A guild tag must be alphanumeric (only letters and numbers).");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "A guild tag must be 2-5 characters long.");
                            }
                        } else {
                            p.sendMessage(ChatColor.RED + "The operation has been cancelled.");

                            GuildUtil.releaseReservedName(u.guildCreationName);
                            u.guildCreationStatus = null;
                            u.guildCreationName = null;
                            u.guildCreationTag = null;
                        }
                    }
                } else {
                    String prefix = u.getRank().getChatPrefix() != null ? u.getRank().getChatPrefix() : "";
                    if(!prefix.isEmpty() && !prefix.endsWith(" ")) prefix += " ";

                    String guildPrefix = u.isInGuild() ? ChatColor.DARK_GRAY + "|" + ChatColor.GRAY + u.getGuild().getTag() : "";

                    p.sendMessage(ChatColor.DARK_GRAY + "<" + ChatColor.GRAY + u.getCurrentCharacter().getLevel() + guildPrefix +  ChatColor.DARK_GRAY + "> " + prefix + u.getRank().getColor() + p.getName() + ": " + ChatColor.WHITE + msg);

                    if(!u.getCurrentCharacter().getVariables().hasSeenChatRangeInfo){
                        p.sendMessage(ChatColor.GRAY + "(Note: Only players near you will see your chat messages.)");
                        u.getCurrentCharacter().getVariables().hasSeenChatRangeInfo = true;
                    }

                    for(Entity entity : p.getNearbyEntities(90,90,90)){
                        if(entity instanceof Player){
                            if(CustomEntity.fromEntity((LivingEntity)entity) == null){
                                Player p2 = (Player)entity;

                                if(GameUser.isLoaded(p2)){
                                    GameUser u2 = GameUser.getUser(p2);

                                    if(u2.getCurrentCharacter() != null){
                                        double distance = p.getLocation().distance(p2.getLocation());

                                        String d = "";
                                        if(u2.getFriends().contains(p.getUniqueId().toString())) d += ChatColor.AQUA.toString() + ChatColor.BOLD.toString() + "F";
                                        if(u.getParty() != null && u2.getParty() != null && u.getParty() == u2.getParty()) d += ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "P";
                                        if(u.isInGuild() && u2.isInGuild() && u.getGuild() == u2.getGuild()) d += ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD.toString() + "G";

                                        ChatColor color = null;

                                        if(distance < 35){
                                            color = ChatColor.WHITE;
                                        } else if(distance > 35 && distance < 65){
                                            color = ChatColor.GRAY;
                                        } else if(distance > 65 && distance < 90){
                                            color = ChatColor.DARK_GRAY;
                                        }

                                        if(color != null){
                                            p2.sendMessage(d + ChatColor.DARK_GRAY + "<" + ChatColor.GRAY + u.getCurrentCharacter().getLevel() + guildPrefix + ChatColor.DARK_GRAY + "> " + prefix + u.getRank().getColor() + p.getName() + ": " + color + msg);
                                        }
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

                        if(/*u.stageAdding.objectives.length == 0 && */u.objectiveAdding == null){
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
                        } else {
                            if(msg.equalsIgnoreCase("cancel")){
                                p.sendMessage(ChatColor.RED + "Operation aborted.");
                                u.stageAdding = null;
                                u.objectiveAdding = null;
                                return;
                            }

                            if(u.objectiveAdding.type == QuestObjectiveType.KILL_MOBS){
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

                                        if(MobData.getData(Integer.parseInt(s[0])) != null){
                                            MobData mob = MobData.getData(Integer.parseInt(s[0]));
                                            ArrayList<QuestObjective> ob = new ArrayList<QuestObjective>();
                                            for(QuestObjective o : u.stageAdding.objectives) if(o != u.objectiveAdding) ob.add(o);
                                            u.objectiveAdding.mobToKill = mob.getId();
                                            u.objectiveAdding.mobToKillAmount = amount;
                                            ob.add(u.objectiveAdding);

                                            u.stageAdding.objectives = ob.toArray(new QuestObjective[]{});

                                            ArrayList<QuestStage> ss = new ArrayList<QuestStage>();
                                            for(QuestStage st : u.questModifying.getStages()) if(st != u.stageAdding) ss.add(st);
                                            ss.add(u.stageAdding);

                                            u.questModifying.setStages(ss.toArray(new QuestStage[]{}));

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
                                            p.sendMessage(ChatColor.RED + "That mob doesn't exist.");
                                        }
                                    } else {
                                        p.sendMessage(ChatColor.RED + "Please enter a valid integer.");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "Invalid format. Use ID[:AMOUNT]!");
                                }
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
                                            ArrayList<QuestObjective> ob = new ArrayList<QuestObjective>();
                                            for(QuestObjective o : u.stageAdding.objectives) if(o != u.objectiveAdding) ob.add(o);
                                            u.objectiveAdding.itemToFind = item.getData().getId();
                                            u.objectiveAdding.itemToFindAmount = item.getAmount();
                                            ob.add(u.objectiveAdding);

                                            u.stageAdding.objectives = ob.toArray(new QuestObjective[]{});

                                            ArrayList<QuestStage> ss = new ArrayList<QuestStage>();
                                            for(QuestStage st : u.questModifying.getStages()) if(st != u.stageAdding) ss.add(st);
                                            ss.add(u.stageAdding);

                                            u.questModifying.setStages(ss.toArray(new QuestStage[]{}));

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
                                            ArrayList<QuestObjective> ob = new ArrayList<QuestObjective>();
                                            for(QuestObjective o : u.stageAdding.objectives) if(o != u.objectiveAdding) ob.add(o);
                                            u.objectiveAdding.npcToTalkTo = Integer.parseInt(msg);
                                            ob.add(u.objectiveAdding);

                                            u.stageAdding.objectives = ob.toArray(new QuestObjective[]{});

                                            ArrayList<QuestStage> s = new ArrayList<QuestStage>();
                                            for(QuestStage st : u.questModifying.getStages()) if(st != u.stageAdding) s.add(st);
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
                    } else if(u.questModifying != null && u.definingRewardItems){
                        String[] s = msg.split(":");
                        if(s.length == 1 || s.length == 2) {
                            if (Util.isValidInteger(s[0])) {
                                int amount = 1;

                                if (s.length == 2) {
                                    if (Util.isValidInteger(s[1])) {
                                        amount = Integer.parseInt(s[1]);
                                    } else {
                                        p.sendMessage(ChatColor.RED + "Please enter a valid integer.");
                                        return;
                                    }
                                }

                                if(ItemData.getData(Integer.parseInt(s[0])) != null){
                                    ArrayList<CustomItem> items = new ArrayList<CustomItem>();
                                    if(u.questModifying.getRewardItems() != null) for(CustomItem item : u.questModifying.getRewardItems()) items.add(item);
                                    items.add(new CustomItem(Integer.parseInt(s[0]),amount));
                                    u.questModifying.setRewardItems(items.toArray(new CustomItem[]{}));

                                    p.sendMessage(ChatColor.GREEN + "Reward items updated.");
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
            }
        } else {
            e.setCancelled(true);
        }
    }
}
