package net.wrathofdungeons.dungeonrpg.cmd;

import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.items.awakening.Awakening;
import net.wrathofdungeons.dungeonrpg.items.awakening.AwakeningType;
import net.wrathofdungeons.dungeonrpg.skill.SkillValues;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class AwakeningCommand extends Command {
    public AwakeningCommand(){
        super("awakening", Rank.GM);
    }

    @Override
    public void execute(Player p, String label, String[] args) {
        GameUser u = GameUser.getUser(p);

        if(u.getCurrentCharacter() != null){
            CustomItem weapon = CustomItem.fromItemStack(p.getItemInHand());

            if(args.length == 3){
                if(Util.isValidInteger(args[0])){
                    AwakeningType type = AwakeningType.fromID(Integer.parseInt(args[0]));

                    if(type != null){
                        if(Util.isValidInteger(args[1])){
                            int value = Integer.parseInt(args[1]);

                            if(Util.isValidInteger(args[2])){
                                boolean isPercentage = Util.convertIntegerToBoolean(Integer.parseInt(args[2]));

                                if((isPercentage && type.mayBePercentage()) || (!isPercentage && type.mayBeStatic())){
                                    if(!weapon.hasAwakening(type)){
                                        Awakening a = new Awakening(type,value,isPercentage);
                                        weapon.addAwakening(a);
                                        p.setItemInHand(weapon.build(p));
                                        String d = isPercentage ? "%" : "";
                                        p.sendMessage(ChatColor.GREEN + "Added Awakening to " + ChatColor.stripColor(weapon.getData().getName()) + "! (" + type.getDisplayName() + " " + value + d + ")");
                                    } else {
                                        p.sendMessage(ChatColor.RED + "That item already has this awakening type.");
                                    }
                                } else {
                                    if(isPercentage){
                                        p.sendMessage(ChatColor.RED + "That awakening type may not be a percentage.");
                                    } else {
                                        p.sendMessage(ChatColor.RED + "That awakening type may not be static.");
                                    }
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "Is Percentage must be an integer-boolean (0 or 1).");
                            }
                        } else {
                            p.sendMessage(ChatColor.RED + "Value must be an integer.");
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "Unknown Type ID.");
                    }
                } else {
                    p.sendMessage(ChatColor.RED + "Please enter a valid type ID.");
                }
            } else {
                p.sendMessage(ChatColor.RED + "/" + label + " <Type ID> <Value> <Is Percentage?>");
            }

            if(weapon != null){
                if(weapon.canHoldAwakenings()){

                } else {
                    p.sendMessage(ChatColor.RED + "That item can't hold awakenings.");
                }
            } else {
                p.sendMessage(ChatColor.RED + "You must hold an item in your hand.");
            }
        } else {
            p.sendMessage(ChatColor.RED + "Please select a character first.");
        }
    }
}