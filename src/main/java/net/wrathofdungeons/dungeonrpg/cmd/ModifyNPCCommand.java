package net.wrathofdungeons.dungeonrpg.cmd;

import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.inv.MerchantSetupMenu;
import net.wrathofdungeons.dungeonrpg.npc.CustomNPC;
import net.wrathofdungeons.dungeonrpg.npc.CustomNPCType;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

import java.util.Arrays;

public class ModifyNPCCommand extends Command {
    public ModifyNPCCommand(){
        super(new String[]{"modifynpc","npc"}, Rank.ADMIN);
    }

    public void sendUsage(Player p, String label){
        p.sendMessage(ChatColor.RED + "/" + label + " create <NPC-Type>");
        p.sendMessage(ChatColor.RED + "/" + label + " <NPC> teleport");
        p.sendMessage(ChatColor.RED + "/" + label + " <NPC> offers");
        p.sendMessage(ChatColor.RED + "/" + label + " <NPC> customname [Name]");
        p.sendMessage(ChatColor.RED + "/" + label + " <NPC> type <NPC-Type>");
        p.sendMessage(ChatColor.RED + "/" + label + " <NPC> entity <Entity Type>");
        p.sendMessage(ChatColor.RED + "/" + label + " <NPC> profession <Profession>");
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
                        } else {
                            if(args[1].equalsIgnoreCase("customname")){
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
