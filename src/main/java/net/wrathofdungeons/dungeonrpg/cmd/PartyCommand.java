package net.wrathofdungeons.dungeonrpg.cmd;

import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonrpg.party.Party;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class PartyCommand extends Command {
    public PartyCommand(){
        super(new String[]{"party","p"});
    }

    private void showUsage(Player p){
        execute(p,"party",new String[]{});
    }

    @Override
    public void execute(Player p, String label, String[] args) {
        GameUser u = GameUser.getUser(p);
        Party party = u.getParty();

        if(label.equalsIgnoreCase("party")){
            if(args.length == 1) {
                String cmd = args[0];

                if(cmd.equalsIgnoreCase("create")){
                    if(party == null){
                        party = new Party(p);
                        p.playSound(p.getEyeLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 2F);
                        p.sendMessage(ChatColor.GREEN + "The party has been created!");
                    } else {
                        p.sendMessage(ChatColor.RED + "You are already in a party.");
                    }
                } else if(cmd.equalsIgnoreCase("leave")){
                    if(party != null){
                        party.leaveParty(p);
                    } else {
                        p.sendMessage(ChatColor.RED + "You are not in a party.");
                    }
                } else if(cmd.equalsIgnoreCase("disband")){
                    if(party != null){
                        if(party.isLeader(p)){
                            party.disband();
                        } else {
                            p.sendMessage(ChatColor.RED + "Only the party leader may do this.");
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "You are not in a party.");
                    }
                } else {
                    showUsage(p);
                }
            } else if(args.length == 2) {
                String cmd = args[0];

                if(cmd.equalsIgnoreCase("invite")){
                    if(party != null){
                        if(party.isLeader(p)){
                            Player p2 = Bukkit.getPlayer(args[1]);

                            if(p2 != null){
                                if(GameUser.isLoaded(p2)){
                                    GameUser u2 = GameUser.getUser(p2);

                                    if(u2.getCurrentCharacter() != null){
                                        if(!party.isInvited(p2)){
                                            if(u2.getSettingsManager().allowsPartyRequests()){
                                                party.getInvited().add(p2);
                                                p.sendMessage(ChatColor.GREEN + "The invitation has been sent.");
                                                p2.sendMessage(ChatColor.GREEN + "You have been invited to a party by " + p.getName() + ". Use " + ChatColor.YELLOW + "/party accept " + p.getName() + ChatColor.GREEN + " to accept.");
                                            } else {
                                                p.sendMessage(ChatColor.RED + "That player doesn't allow party requests.");
                                            }
                                        } else {
                                            p.sendMessage(ChatColor.RED + "You have already invited that player.");
                                        }
                                    } else {
                                        p.sendMessage(ChatColor.RED + "The target must be in game.");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "The target must be in game.");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "That player is not online.");
                            }
                        } else {
                            p.sendMessage(ChatColor.RED + "Only the party leader may do this.");
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "You are not in a party.");
                    }
                } else if(cmd.equalsIgnoreCase("accept")){
                    if(party == null){
                        Player p2 = Bukkit.getPlayer(args[1]);

                        if(p2 != null){
                            if(GameUser.isLoaded(p2)){
                                GameUser u2 = GameUser.getUser(p2);
                                party = u2.getParty();

                                if(party != null){
                                    if(party.getInvited().contains(p)){
                                        party.getInvited().remove(p);
                                        party.joinParty(p);
                                    } else {
                                        p.sendMessage(ChatColor.RED + "You were not invited to that party.");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "That player is not in a party.");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "The target must be in game.");
                            }
                        } else {
                            p.sendMessage(ChatColor.RED + "That player is not online.");
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "You are already in a party.");
                    }
                } else if(cmd.equalsIgnoreCase("decline") || cmd.equalsIgnoreCase("deny")){
                    if(party == null){
                        Player p2 = Bukkit.getPlayer(args[1]);

                        if(p2 != null){
                            if(GameUser.isLoaded(p2)){
                                GameUser u2 = GameUser.getUser(p2);
                                party = u2.getParty();

                                if(party != null){
                                    if(party.getInvited().contains(p)){
                                        party.getInvited().remove(p);
                                        p.sendMessage(ChatColor.YELLOW + "You have declined the invitation.");

                                        for(Player pp : party.getOnlinePlayers()){
                                            pp.sendMessage(ChatColor.RED + p.getName() + " has denied the party invitation.");
                                        }
                                    } else {
                                        p.sendMessage(ChatColor.RED + "You were not invited to that party.");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "That player is not in a party.");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "The target must be in game.");
                            }
                        } else {
                            p.sendMessage(ChatColor.RED + "That player is not online.");
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "You are already in a party.");
                    }
                } else if(cmd.equalsIgnoreCase("kick")){
                    if(party != null){
                        if(party.isLeader(p)){
                            Player p2 = Bukkit.getPlayer(args[1]);

                            if(p2 != null){
                                if(GameUser.isLoaded(p2)){
                                    GameUser u2 = GameUser.getUser(p2);

                                    if(party.hasMember(p2)){
                                        party.leaveParty(p2);

                                        for(Player pp : party.getOnlinePlayers()){
                                            p.sendMessage(ChatColor.RED + p2.getName() + " has been kicked out of the party.");
                                        }
                                    } else {
                                        p.sendMessage(ChatColor.RED + "That player is not in your party.");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "The target must be in game.");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "That player is not online.");
                            }
                        } else {
                            p.sendMessage(ChatColor.RED + "Only the party leader may do this.");
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "You are not in a party.");
                    }
                } else {
                    showUsage(p);
                }
            } else {
                p.sendMessage(ChatColor.RED + "/" + label + " create");
                p.sendMessage(ChatColor.RED + "/" + label + " invite <Player>");
                p.sendMessage(ChatColor.RED + "/" + label + " accept <Player>");
                p.sendMessage(ChatColor.RED + "/" + label + " decline <Player>");
                p.sendMessage(ChatColor.RED + "/" + label + " kick <Player>");
                p.sendMessage(ChatColor.RED + "/" + label + " leave");
                p.sendMessage(ChatColor.RED + "/" + label + " disband");
                p.sendMessage(ChatColor.RED + "/p <Message>");
            }
        } else {
            if(args.length == 0){
                showUsage(p);
            } else {
                if(party != null){
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < args.length; i++) {
                        sb.append(args[i]).append(" ");
                    }
                    String message = sb.toString().trim();

                    for(Player p2 : party.getOnlinePlayers()){
                        p2.sendMessage(ChatColor.DARK_GREEN + "[" + p.getName() + "] " + ChatColor.GREEN + message);
                    }
                } else {
                    p.sendMessage(ChatColor.RED + "You are not in a party.");
                }
            }
        }
    }
}
