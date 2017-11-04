package net.wrathofdungeons.dungeonrpg.cmd;

import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonrpg.Duel;
import net.wrathofdungeons.dungeonrpg.Trade;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class DuelCommand extends Command {
    public DuelCommand(){
        super("duel");
    }

    @Override
    public void execute(Player p, String label, String[] args) {
        GameUser u = GameUser.getUser(p);

        if(args.length == 1){
            Player p2 = Bukkit.getPlayer(args[0]);

            if(p2 != null && p != p2){
                if(GameUser.isLoaded(p2)){
                    GameUser u2 = GameUser.getUser(p2);

                    if(u2.getCurrentCharacter() != null){
                        if(p.getWorld() == p2.getWorld() && p.getLocation().distance(p2.getLocation()) <= 20.0){
                            if(!Duel.isDueling(p)){
                                if(!Duel.isDueling(p2)){
                                    if(Duel.getRequest(p,p2) == null){
                                        if(Duel.getRequest(p2,p) == null){
                                            if(u2.getSettingsManager().allowsDuelRequests()){
                                                Duel.Request r = Duel.createRequest(p,p2);

                                                p.sendMessage(ChatColor.AQUA + "Duel request sent!");

                                                p2.playSound(p2.getEyeLocation(), Sound.ORB_PICKUP,1f,1f);
                                                p2.sendMessage(ChatColor.YELLOW + p.getName() + " " + ChatColor.AQUA + "[Level " + u.getCurrentCharacter().getLevel() + "] wants to duel with you. Use " + ChatColor.YELLOW + "/duel " + p.getName() + ChatColor.AQUA + " to accept their request.");
                                            } else {
                                                p.sendMessage(ChatColor.RED + "That player doesn't allow duel requests.");
                                            }
                                        } else {
                                            Duel.getRequest(p2,p).accept();
                                        }
                                    } else {
                                        p.sendMessage(ChatColor.RED + "You have already sent a duel request to that player.");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "That player is already in a duel.");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "You are already in a duel.");
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
