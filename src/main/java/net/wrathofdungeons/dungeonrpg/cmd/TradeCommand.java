package net.wrathofdungeons.dungeonrpg.cmd;

import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonrpg.Trade;
import net.wrathofdungeons.dungeonrpg.npc.CustomNPC;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class TradeCommand extends Command {
    public TradeCommand(){
        super("trade");
    }

    @Override
    public void execute(Player p, String label, String[] args) {
        GameUser u = GameUser.getUser(p);

        if(u.getCurrentCharacter() != null){
            if(args.length == 1){
                Player p2 = Bukkit.getPlayer(args[0]);

                if(p2 != null && p != p2){
                    if(GameUser.isLoaded(p2)){
                        GameUser u2 = GameUser.getUser(p2);

                        if(u2.getCurrentCharacter() != null){
                            if(p.getWorld() == p2.getWorld() && p.getLocation().distance(p2.getLocation()) <= 20.0){
                                if(!CustomNPC.READING.contains(p.getName())){
                                    if(!CustomNPC.READING.contains(p2.getName())){
                                        if(!Trade.isTrading(p)){
                                            if(!Trade.isTrading(p2)){
                                                if(Trade.getRequest(p,p2) == null){
                                                    if(Trade.getRequest(p2,p) == null){
                                                        if(u2.getSettingsManager().allowsTradeRequests()){
                                                            Trade.Request r = Trade.createRequest(p,p2);

                                                            p.sendMessage(ChatColor.GREEN + "Trade request sent!");

                                                            p2.playSound(p2.getEyeLocation(), Sound.ORB_PICKUP,1f,1f);
                                                            p2.sendMessage(ChatColor.YELLOW + p.getName() + " " + ChatColor.GREEN + "wants to trade with you. Use " + ChatColor.YELLOW + "/trade " + p.getName() + ChatColor.GREEN + " to accept their request.");
                                                        } else {
                                                            p.sendMessage(ChatColor.RED + "That player doesn't allow trade requests.");
                                                        }
                                                    } else {
                                                        Trade.getRequest(p2,p).accept();
                                                    }
                                                } else {
                                                    p.sendMessage(ChatColor.RED + "You have already sent a trade request to that player.");
                                                }
                                            } else {
                                                p.sendMessage(ChatColor.RED + "That player is already in a trade.");
                                            }
                                        } else {
                                            p.sendMessage(ChatColor.RED + "You are already in a trade.");
                                        }
                                    } else {
                                        p.sendMessage(ChatColor.RED + "That player can't trade right now.");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "You can't trade while talking to NPCs.");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "That player is too far away.");
                            }
                        } else {
                            p.sendMessage(ChatColor.RED + "That player is not online.");
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "That player is not online.");
                    }
                } else {
                    p.sendMessage(ChatColor.RED + "That player is not online.");
                }
            } else {
                p.sendMessage(ChatColor.RED + "/" + label + " <Player>");
            }
        }
    }
}
