package net.wrathofdungeons.dungeonrpg.cmd;

import net.citizensnpcs.api.npc.NPC;
import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.MySQLManager;
import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.inv.MerchantSetupMenu;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.npc.CustomNPC;
import net.wrathofdungeons.dungeonrpg.npc.CustomNPCType;
import net.wrathofdungeons.dungeonrpg.npc.KeyMasterLocation;
import net.wrathofdungeons.dungeonrpg.npc.dialogue.NPCDialogue;
import net.wrathofdungeons.dungeonrpg.npc.dialogue.NPCDialogueCondition;
import net.wrathofdungeons.dungeonrpg.npc.dialogue.NPCDialogueConditionType;
import net.wrathofdungeons.dungeonrpg.quests.Quest;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;

public class ModifyNPCCommand extends Command {
    public ModifyNPCCommand(){
        super(new String[]{"modifynpc","npc"}, Rank.ADMIN);
    }

    public void sendUsage(Player p, String label){
        p.sendMessage(ChatColor.RED + "/createnpc <NPC-Type>");
        p.sendMessage(ChatColor.RED + "/" + label + " <NPC> teleport");
        p.sendMessage(ChatColor.RED + "/" + label + " <NPC> offers");
        p.sendMessage(ChatColor.RED + "/" + label + " <NPC> adddialogue <Condition Type>");
        p.sendMessage(ChatColor.RED + "/" + label + " <NPC> dialoguequest <Dialogue Index> <Quest ID>");
        p.sendMessage(ChatColor.RED + "/" + label + " <NPC> dialoguestage <Dialogue Index> <Stage Index>");
        p.sendMessage(ChatColor.RED + "/" + label + " <NPC> addline <Dialogue Index> <Text>");
        p.sendMessage(ChatColor.RED + "/" + label + " <NPC> removeline <Dialogue Index> <Line Index>");
        p.sendMessage(ChatColor.RED + "/" + label + " <NPC> lines <Dialogue Index>");
        p.sendMessage(ChatColor.RED + "/" + label + " <NPC> keymasteritem <Item>");
        p.sendMessage(ChatColor.RED + "/" + label + " <NPC> keymasterlocation");
        p.sendMessage(ChatColor.RED + "/" + label + " <NPC> keymaster");
        p.sendMessage(ChatColor.RED + "/" + label + " <NPC> dialogues");
        p.sendMessage(ChatColor.RED + "/" + label + " <NPC> copy");
        p.sendMessage(ChatColor.RED + "/" + label + " <NPC> customname [Name]");
        p.sendMessage(ChatColor.RED + "/" + label + " <NPC> type <NPC-Type>");
        p.sendMessage(ChatColor.RED + "/" + label + " <NPC> entity <Entity Type>");
        p.sendMessage(ChatColor.RED + "/" + label + " <NPC> profession <Profession>");
        p.sendMessage(ChatColor.RED + "/" + label + " <NPC> skin <Skin ID>");
        p.sendMessage(ChatColor.RED + "/" + label + " <NPC> removedialogue <Dialogue Index>");
    }

    @Override
    public void execute(Player p, String label, String[] args) {
        GameUser u = GameUser.getUser(p);

        if(u.isInSetupMode()){
            if(args.length >= 2){
                if(Util.isValidInteger(args[0])){
                    int npcID = Integer.parseInt(args[0]);
                    CustomNPC npc = CustomNPC.fromID(npcID);

                    if(npc != null){
                        if(args.length == 2){
                            if(args[1].equalsIgnoreCase("teleport")){
                                npc.setLocation(p.getLocation());
                                p.sendMessage(ChatColor.GREEN + "Success!");
                            } else if(args[1].equalsIgnoreCase("offers")){
                                if(npc.getNpcType() == CustomNPCType.MERCHANT){
                                    MerchantSetupMenu.open(p,npc);
                                } else {
                                    p.sendMessage(ChatColor.RED + "Only Merchant NPCs can hold item offers.");
                                }
                            } else if(args[1].equalsIgnoreCase("keymaster")){
                                if(npc.getNpcType() == CustomNPCType.DUNGEON_KEY_MASTER){
                                    if(npc.getKeyMasterLocation() != null){
                                        p.sendMessage(ChatColor.GREEN + "Location: " + ChatColor.YELLOW + npc.getKeyMasterLocation().world + " " + npc.getKeyMasterLocation().x + " " + npc.getKeyMasterLocation().y + " " + npc.getKeyMasterLocation().z + " " + npc.getKeyMasterLocation().yaw + " " + npc.getKeyMasterLocation().pitch);
                                    }

                                    if(npc.getKeyMasterItem() != null){
                                        p.sendMessage(ChatColor.GREEN + "Item: " + ChatColor.YELLOW + npc.getKeyMasterItem().getAmount() + "x " + ChatColor.stripColor(npc.getKeyMasterItem().getData().getName()));
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "That NPC is not a DUNGEON_KEY_MASTER type.");
                                }
                            } else if(args[1].equalsIgnoreCase("keymasterlocation")){
                                if(npc.getNpcType() == CustomNPCType.DUNGEON_KEY_MASTER){
                                    KeyMasterLocation loc = new KeyMasterLocation();
                                    loc.world = p.getWorld().getName();
                                    loc.x = p.getLocation().getX();
                                    loc.y = p.getLocation().getY();
                                    loc.z = p.getLocation().getZ();
                                    loc.yaw = p.getLocation().getYaw();
                                    loc.pitch = p.getLocation().getPitch();

                                    npc.setKeyMasterLocation(loc);
                                    p.sendMessage(ChatColor.GREEN + "Updated key master location.");
                                } else {
                                    p.sendMessage(ChatColor.RED + "That NPC is not a DUNGEON_KEY_MASTER type.");
                                }
                            } else if(args[1].equalsIgnoreCase("dialogues")){
                                if(npc.getNpcType() != CustomNPCType.QUEST_NPC){
                                    p.sendMessage(ChatColor.RED + "Only Quest NPCs can hold dialogues.");
                                    return;
                                }

                                if(npc.getDialogues().size() > 0){
                                    int i = 0;

                                    for(NPCDialogue dialogue : npc.getDialogues()){
                                        p.sendMessage(ChatColor.YELLOW.toString() + i + ": ");
                                        p.sendMessage(ChatColor.YELLOW.toString() + "       Condition" + ": ");
                                        p.sendMessage(ChatColor.YELLOW.toString() + "           Type" + ": " + ChatColor.GREEN + dialogue.condition.type);
                                        p.sendMessage(ChatColor.YELLOW.toString() + "           Quest ID" + ": " + ChatColor.GREEN + dialogue.condition.questID);
                                        p.sendMessage(ChatColor.YELLOW.toString() + "           Stage Index" + ": " + ChatColor.GREEN + dialogue.condition.questStageIndex);

                                        i++;
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "This NPC doesn't have any dialogues.");
                                }
                            } else if(args[1].equalsIgnoreCase("copy")){
                                DungeonAPI.async(() -> {
                                    try {
                                        int n = 0;

                                        PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `npcs` (`npcType`,`location.world`,`location.x`,`location.y`,`location.z`,`location.yaw`,`location.pitch`) VALUES(?,?,?,?,?,?,?);", Statement.RETURN_GENERATED_KEYS);
                                        ps.setString(1,npc.getNpcType().toString());
                                        ps.setString(2,p.getLocation().getWorld().getName());
                                        ps.setDouble(3,p.getLocation().getX());
                                        ps.setDouble(4,p.getLocation().getY());
                                        ps.setDouble(5,p.getLocation().getZ());
                                        ps.setFloat(6,p.getLocation().getYaw());
                                        ps.setFloat(7,p.getLocation().getPitch());
                                        ps.executeUpdate();

                                        ResultSet rs = ps.getGeneratedKeys();
                                        if(rs.first()){
                                            n = rs.getInt(1);
                                        }

                                        if(n > 0){
                                            CustomNPC copy = new CustomNPC(n);
                                            copy.setEntityType(npc.getEntityType());
                                            copy.setVillagerProfession(npc.getVillagerProfession());
                                            copy.setCustomName(npc.getCustomName());
                                            copy.setSkin(npc.getSkin());
                                            copy.setLocation(p.getLocation());
                                            copy.setOffers(npc.getOffers());

                                            p.sendMessage(ChatColor.GREEN + "Created NPC! ID: #" + copy.getId());
                                        } else {
                                            p.sendMessage(ChatColor.RED + "An error occurred.");
                                        }

                                        MySQLManager.getInstance().closeResources(rs,ps);
                                    } catch(Exception e){
                                        p.sendMessage(ChatColor.RED + "An error occurred.");
                                        e.printStackTrace();
                                    }
                                });
                            } else if(args[1].equalsIgnoreCase("customname")){
                                npc.setCustomName(null);
                                p.sendMessage(ChatColor.GREEN + "Success!");
                            } else {
                                sendUsage(p,label);
                            }
                        } else if(args.length == 3){
                            if(args[1].equalsIgnoreCase("customname")){
                                npc.setCustomName(ChatColor.translateAlternateColorCodes('&',args[2]));
                                p.sendMessage(ChatColor.GREEN + "Success!");
                            } else if(args[1].equalsIgnoreCase("type")){
                                CustomNPCType type = null;

                                for(CustomNPCType c : CustomNPCType.values()) if(type.toString().equalsIgnoreCase(args[2])) type = c;

                                if(type != null){
                                    npc.setNpcType(type);
                                    p.sendMessage(ChatColor.GREEN + "Success!");
                                } else {
                                    p.sendMessage(ChatColor.RED + "Invalid NPC type!");
                                }
                            } else if(args[1].equalsIgnoreCase("profession")){
                                Villager.Profession profession = null;

                                for(Villager.Profession pr : Villager.Profession.values()) if(pr.toString().equalsIgnoreCase(args[2])) profession = pr;

                                if(profession != null){
                                    npc.setVillagerProfession(profession);
                                    p.sendMessage(ChatColor.GREEN + "Success!");
                                } else {
                                    p.sendMessage(ChatColor.RED + "Invalid NPC type!");
                                }
                            } else if(args[1].equalsIgnoreCase("skin")){
                                if(Util.isValidInteger(args[2])){
                                    int skinID = Integer.parseInt(args[2]);

                                    if(skinID >= 0){
                                        npc.setSkin(skinID);
                                        p.sendMessage(ChatColor.GREEN + "Success!");
                                    } else {
                                        p.sendMessage(ChatColor.RED + "Invalid value.");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "Skin ID must be an integer.");
                                }
                            } else if(args[1].equalsIgnoreCase("adddialogue")){
                                if(npc.getNpcType() != CustomNPCType.QUEST_NPC){
                                    p.sendMessage(ChatColor.RED + "Only Quest NPCs can hold dialogues.");
                                    return;
                                }

                                NPCDialogueConditionType type = null;

                                for(NPCDialogueConditionType t : NPCDialogueConditionType.values()) if(t.toString().equalsIgnoreCase(args[2])) type = t;

                                if(type != null){
                                    NPCDialogue dialogue = new NPCDialogue();
                                    dialogue.condition = new NPCDialogueCondition();
                                    dialogue.condition.type = type;

                                    npc.getDialogues().add(dialogue);
                                    npc.setHasUnsavedData(true);

                                    p.sendMessage(ChatColor.GREEN + "New dialogue added. Use " + ChatColor.YELLOW + "/" + label + " " + npc.getId() + " addline " + npc.getDialogues().indexOf(dialogue) + " <Text> " + ChatColor.GREEN + "to add a new text line to this dialogue.");

                                    if(type != NPCDialogueConditionType.NONE) p.sendMessage(ChatColor.RED + "This dialogue type needs further settings made, such as Quest ID, stage index etc.");
                                } else {
                                    p.sendMessage(ChatColor.RED + "Invalid dialogue condition. Valid conditions are:");
                                    for(NPCDialogueConditionType t : NPCDialogueConditionType.values()) p.sendMessage(ChatColor.RED + "- " + ChatColor.YELLOW + t.toString());
                                }
                            } else if(args[1].equalsIgnoreCase("lines")){
                                if(npc.getNpcType() != CustomNPCType.QUEST_NPC){
                                    p.sendMessage(ChatColor.RED + "Only Quest NPCs can hold dialogues.");
                                    return;
                                }

                                if(Util.isValidInteger(args[2])){
                                    int index = Integer.parseInt(args[2]);

                                    if(index < npc.getDialogues().size() && index >= 0){
                                        NPCDialogue dialogue = npc.getDialogues().get(index);

                                        if(dialogue != null){
                                            if(dialogue.lines.size() > 0){
                                                int i = 0;

                                                for(String s : dialogue.lines){
                                                    p.sendMessage(ChatColor.YELLOW.toString() + i + ": " + ChatColor.GRAY + ChatColor.translateAlternateColorCodes('&',s));
                                                    i++;
                                                }
                                            } else {
                                                p.sendMessage(ChatColor.RED + "This dialogue doesn't have any lines!");
                                            }
                                        } else {
                                            p.sendMessage(ChatColor.RED + "Invalid integer number!");
                                        }
                                    } else {
                                        p.sendMessage(ChatColor.RED + "Invalid integer number!");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "Dialogue index must be an integer!");
                                }
                            } else if(args[1].equalsIgnoreCase("keymasteritem")){
                                if(npc.getNpcType() == CustomNPCType.DUNGEON_KEY_MASTER){
                                    String[] s = args[2].split(":");
                                    if(s.length == 1){
                                        if(Util.isValidInteger(args[0])){
                                            npc.setKeyMasterItem(new CustomItem(Integer.parseInt(args[0])));
                                            p.sendMessage(ChatColor.GREEN + "Key Master item set to: " + ChatColor.YELLOW + npc.getKeyMasterItem().getAmount() + "x " + npc.getKeyMasterItem().getData().getName());
                                        } else {
                                            p.sendMessage(ChatColor.RED + "Please use the format ID[:AMOUNT]");
                                        }
                                    } else if(s.length == 2){
                                        if(Util.isValidInteger(args[0]) && Util.isValidInteger(args[1])){
                                            npc.setKeyMasterItem(new CustomItem(Integer.parseInt(args[0]),Integer.parseInt(args[1])));
                                            p.sendMessage(ChatColor.GREEN + "Key Master item set to: " + ChatColor.YELLOW + npc.getKeyMasterItem().getAmount() + "x " + npc.getKeyMasterItem().getData().getName());
                                        } else {
                                            p.sendMessage(ChatColor.RED + "Please use the format ID[:AMOUNT]");
                                        }
                                    } else {
                                        p.sendMessage(ChatColor.RED + "Please use the format ID[:AMOUNT]");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "That NPC is not a DUNGEON_KEY_MASTER type.");
                                }
                            } else if(args[1].equalsIgnoreCase("removedialogue")){
                                if(npc.getNpcType() != CustomNPCType.QUEST_NPC){
                                    p.sendMessage(ChatColor.RED + "Only Quest NPCs can hold dialogues.");
                                    return;
                                }

                                if(Util.isValidInteger(args[2])){
                                    int index = Integer.parseInt(args[2]);

                                    if(index < npc.getDialogues().size() && index >= 0){
                                        NPCDialogue dialogue = npc.getDialogues().get(index);

                                        if(dialogue != null){
                                            npc.getDialogues().remove(dialogue);
                                            npc.setHasUnsavedData(true);
                                            p.sendMessage(ChatColor.GREEN + "Removed dialogue.");
                                        } else {
                                            p.sendMessage(ChatColor.RED + "Invalid integer number!");
                                        }
                                    } else {
                                        p.sendMessage(ChatColor.RED + "Invalid integer number!");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "Dialogue index must be an integer!");
                                }
                            } else if(args[1].equalsIgnoreCase("entity")){
                                String[] allowed = new String[]{"PLAYER","VILLAGER"};

                                if(Arrays.asList(allowed).contains(args[2])){
                                    npc.setEntityType(EntityType.valueOf(args[2]));
                                    p.sendMessage(ChatColor.GREEN + "Success!");
                                } else {
                                    p.sendMessage(ChatColor.RED + "Invalid entity type!");
                                }
                            } else {
                                sendUsage(p,label);
                            }
                        } else if(args.length == 4){
                            if(args[1].equalsIgnoreCase("dialoguequest")){
                                if(npc.getNpcType() != CustomNPCType.QUEST_NPC){
                                    p.sendMessage(ChatColor.RED + "Only Quest NPCs can hold dialogues.");
                                    return;
                                }

                                if(Util.isValidInteger(args[2]) && Util.isValidInteger(args[3])){
                                    int dialogueIndex = Integer.parseInt(args[2]);
                                    int questID = Integer.parseInt(args[3]);

                                    if(dialogueIndex < npc.getDialogues().size() && dialogueIndex >= 0){
                                        NPCDialogue dialogue = npc.getDialogues().get(dialogueIndex);

                                        if(dialogue != null){
                                            if(Quest.getQuest(questID) != null){
                                                dialogue.condition.questID = questID;
                                                p.sendMessage(ChatColor.GREEN + "Quest ID set!");
                                                npc.setHasUnsavedData(true);
                                            } else {
                                                p.sendMessage(ChatColor.RED + "Invalid quest ID!");
                                            }
                                        } else {
                                            p.sendMessage(ChatColor.RED + "Invalid dialogue number!");
                                        }
                                    } else {
                                        p.sendMessage(ChatColor.RED + "Invalid dialogue number!");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "Invalid integer number!");
                                }
                            } else if(args[1].equalsIgnoreCase("dialoguestage")){
                                if(npc.getNpcType() != CustomNPCType.QUEST_NPC){
                                    p.sendMessage(ChatColor.RED + "Only Quest NPCs can hold dialogues.");
                                    return;
                                }

                                if(Util.isValidInteger(args[2]) && Util.isValidInteger(args[3])){
                                    int dialogueIndex = Integer.parseInt(args[2]);
                                    int stageIndex = Integer.parseInt(args[3]);

                                    if(dialogueIndex < npc.getDialogues().size() && dialogueIndex >= 0){
                                        NPCDialogue dialogue = npc.getDialogues().get(dialogueIndex);

                                        if(dialogue != null){
                                            if(Quest.getQuest(dialogue.condition.questID) != null){
                                                if(stageIndex < Quest.getQuest(dialogue.condition.questID).getStages().length && stageIndex >= 0){
                                                    dialogue.condition.questStageIndex = stageIndex;
                                                    p.sendMessage(ChatColor.GREEN + "Quest Stage set!");
                                                    npc.setHasUnsavedData(true);
                                                } else {
                                                    p.sendMessage(ChatColor.RED + "Invalid stage number!");
                                                }
                                            } else {
                                                p.sendMessage(ChatColor.RED + "Invalid quest ID! Define the quest ID with " + ChatColor.YELLOW + "/npc " + npc.getId() + " dialoguequest " + dialogueIndex + " <Quest ID>");
                                            }
                                        } else {
                                            p.sendMessage(ChatColor.RED + "Invalid dialogue number!");
                                        }
                                    } else {
                                        p.sendMessage(ChatColor.RED + "Invalid dialogue number!");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "Invalid integer number!");
                                }
                            } else if(args[1].equalsIgnoreCase("removeline")){
                                if(npc.getNpcType() != CustomNPCType.QUEST_NPC){
                                    p.sendMessage(ChatColor.RED + "Only Quest NPCs can hold dialogues.");
                                    return;
                                }

                                if(Util.isValidInteger(args[2]) && Util.isValidInteger(args[3])){
                                    int dialogueIndex = Integer.parseInt(args[2]);
                                    int lineIndex = Integer.parseInt(args[3]);

                                    if(dialogueIndex < npc.getDialogues().size() && dialogueIndex >= 0){
                                        NPCDialogue dialogue = npc.getDialogues().get(dialogueIndex);

                                        if(dialogue != null){
                                            if(lineIndex < dialogue.lines.size() && lineIndex >= 0){
                                                dialogue.lines.remove(lineIndex);
                                                p.sendMessage(ChatColor.GREEN + "Line removed.");
                                                npc.setHasUnsavedData(true);
                                            } else {
                                                p.sendMessage(ChatColor.RED + "Invalid stage number!");
                                            }
                                        } else {
                                            p.sendMessage(ChatColor.RED + "Invalid dialogue number!");
                                        }
                                    } else {
                                        p.sendMessage(ChatColor.RED + "Invalid dialogue number!");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "Invalid integer number!");
                                }
                            } else if(args[1].equalsIgnoreCase("customname")){
                                StringBuilder sb = new StringBuilder("");
                                for (int i = 2; i < args.length; i++) {
                                    sb.append(args[i]).append(" ");
                                }
                                String s = sb.toString().trim();

                                npc.setCustomName(ChatColor.translateAlternateColorCodes('&',s));
                                p.sendMessage(ChatColor.GREEN + "Success!");
                            } else {
                                sendUsage(p,label);
                            }
                        } else {
                            if(args[1].equalsIgnoreCase("customname")){
                                StringBuilder sb = new StringBuilder("");
                                for (int i = 2; i < args.length; i++) {
                                    sb.append(args[i]).append(" ");
                                }
                                String s = sb.toString().trim();

                                npc.setCustomName(ChatColor.translateAlternateColorCodes('&',s));
                                p.sendMessage(ChatColor.GREEN + "Success!");
                            } else if(args[1].equalsIgnoreCase("addline")){
                                if(npc.getNpcType() != CustomNPCType.QUEST_NPC){
                                    p.sendMessage(ChatColor.RED + "Only Quest NPCs can hold dialogues.");
                                    return;
                                }

                                if(Util.isValidInteger(args[2])){
                                    int dialogueIndex = Integer.parseInt(args[2]);

                                    if(dialogueIndex < npc.getDialogues().size() && dialogueIndex >= 0){
                                        NPCDialogue dialogue = npc.getDialogues().get(dialogueIndex);

                                        if(dialogue != null){
                                            StringBuilder sb = new StringBuilder("");
                                            for (int i = 3; i < args.length; i++) {
                                                sb.append(args[i]).append(" ");
                                            }
                                            String s = sb.toString().trim();

                                            dialogue.lines.add(s);
                                            p.sendMessage(ChatColor.GREEN + "Line added.");
                                            npc.setHasUnsavedData(true);
                                        } else {
                                            p.sendMessage(ChatColor.RED + "Invalid dialogue number!");
                                        }
                                    } else {
                                        p.sendMessage(ChatColor.RED + "Invalid dialogue number!");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "Invalid integer number!");
                                }
                            } else {
                                sendUsage(p,label);
                            }
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "Unknown NPC ID.");
                    }
                } else {
                    sendUsage(p,label);
                }
            } else {
                sendUsage(p,label);
            }
        } else {
            p.sendMessage(ChatColor.RED + "You are not in setup mode.");
        }
    }
}
