package net.wrathofdungeons.dungeonrpg.cmd;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.MySQLManager;
import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonapi.util.PlayerUtilities;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.guilds.Guild;
import net.wrathofdungeons.dungeonrpg.guilds.GuildMember;
import net.wrathofdungeons.dungeonrpg.guilds.GuildRank;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.UUID;

public class GuildCommand extends Command {
    public GuildCommand(){
        super(new String[]{"guild","g"});
    }

    private void showUsage(Player p){
        execute(p,"guild",new String[]{});
    }

    @Override
    public void execute(Player p, String label, String[] args) {
        GameUser u = GameUser.getUser(p);

        if(u.getCurrentCharacter() != null){
            if(label.equalsIgnoreCase("guild")){
                if(args.length == 0){
                    p.sendMessage(ChatColor.RED + "/" + label + " invite <Player>");
                    p.sendMessage(ChatColor.RED + "/" + label + " kick <Player>");
                    p.sendMessage(ChatColor.RED + "/" + label + " promote <Player>");
                    p.sendMessage(ChatColor.RED + "/" + label + " demote <Player>");
                    p.sendMessage(ChatColor.RED + "/" + label + " transfer <Player>");
                    p.sendMessage(ChatColor.RED + "/" + label + " accept <Guild ID>");
                    p.sendMessage(ChatColor.RED + "/" + label + " deny <Guild ID>");
                    p.sendMessage(ChatColor.RED + "/" + label + " leave");
                    p.sendMessage(ChatColor.RED + "/" + label + " disband");
                    p.sendMessage(ChatColor.RED + "/g <Message>");
                } else if(args.length == 1) {
                    if(args[0].equalsIgnoreCase("leave")){
                        if(u.isInGuild()){
                            if(u.getGuild().toGuildMember(p).getRank() != GuildRank.LEADER){
                                u.getGuild().getMembers().remove(u.getGuild().toGuildMember(p));
                                u.getGuild().saveMembers();
                                DungeonAPI.executeBungeeCommand("BungeeConsole","guildaction " + p.getName() + " " + u.getGuild().getId() + " reloadMembers");
                                u.getGuild().sendMessage(ChatColor.LIGHT_PURPLE + p.getName() + " has left the guild.");
                                u.setGuildID(0);
                                u.saveData();

                                p.sendMessage(ChatColor.RED + "You have left the guild.");
                            } else {
                                execute(p,label,new String[]{"disband"});
                            }
                        } else {
                            p.sendMessage(ChatColor.RED + "You are not in a guild.");
                        }
                    } else if(args[0].equalsIgnoreCase("disband")){
                        if(u.isInGuild()){
                            if(u.getGuild().toGuildMember(p).getRank() == GuildRank.LEADER){
                                DungeonAPI.async(() -> {
                                    try {
                                        PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("UPDATE `users` SET `guildID` = ? WHERE `guildID` = ?");
                                        ps.setInt(1,0);
                                        ps.setInt(2,u.getGuild().getId());
                                        ps.executeUpdate();
                                        ps.close();

                                        ps = MySQLManager.getInstance().getConnection().prepareStatement("DELETE FROM `guilds` WHERE `id` = ?");
                                        ps.setInt(1,u.getGuild().getId());
                                        ps.executeUpdate();
                                        ps.close();
                                    } catch(Exception e){
                                        e.printStackTrace();
                                    }
                                });

                                DungeonAPI.executeBungeeCommand("BungeeConsole","guildaction " + p.getName() + " " + u.getGuild().getId() + " disband");
                            } else {
                                p.sendMessage(ChatColor.RED + "Only the guild leader can disband the guild.");
                            }
                        } else {
                            p.sendMessage(ChatColor.RED + "You are not in a guild.");
                        }
                    } else {
                        showUsage(p);
                    }
                } else if(args.length == 2){
                    if(args[0].equalsIgnoreCase("invite")){
                        if(u.isInGuild()){
                            String name = args[1];

                            if(u.getGuild().getRank(p) == GuildRank.OFFICER || u.getGuild().getRank(p) == GuildRank.LEADER){
                                Player p2 = Bukkit.getPlayer(name);

                                if(p2 != null && GameUser.isLoaded(p2)){
                                    GameUser u2 = GameUser.getUser(p2);

                                    if(u2.getGuild() == null){
                                        if(u.hasPermission(Rank.MODERATOR) || u2.getSettingsManager().allowsGuildRequests()){
                                            if(!u.getGuild().getInvitedPlayers().contains(p2)){
                                                u.getGuild().getInvitedPlayers().add(p2);
                                                u.getGuild().sendMessage(ChatColor.LIGHT_PURPLE + p2.getName() + " has been invited to the guild by " + p.getName() + ".",GuildRank.OFFICER);

                                                p2.sendMessage(ChatColor.LIGHT_PURPLE + "You have been invited to the guild " + ChatColor.YELLOW + u.getGuild().getName() + ChatColor.LIGHT_PURPLE + "!");
                                                p2.spigot().sendMessage(new ComponentBuilder("Click here: ").color(net.md_5.bungee.api.ChatColor.LIGHT_PURPLE).append("[ACCEPT]").color(net.md_5.bungee.api.ChatColor.GREEN).bold(true).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/guild accept " + u.getGuild().getId())).append(" ").append("[DENY]").color(net.md_5.bungee.api.ChatColor.RED).bold(true).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/guild deny " + u.getGuild().getId())).create());
                                            } else {
                                                p.sendMessage(ChatColor.RED + "That player has already been invited to the guild.");
                                            }
                                        } else {
                                            p.sendMessage(ChatColor.RED + "That player doesn't allow guild requests.");
                                        }
                                    } else {
                                        p.sendMessage(ChatColor.RED + "That player is already in a guild.");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "You must be on the same server as the player you want to invite.");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "You are not allowed to invite players to the guild.");
                            }
                        } else {
                            p.sendMessage(ChatColor.RED + "You are not in a guild.");
                        }
                    } else if(args[0].equalsIgnoreCase("kick")){
                        if(u.isInGuild()){
                            String name = args[1];

                            if(u.getGuild().getRank(p) == GuildRank.OFFICER || u.getGuild().getRank(p) == GuildRank.LEADER){
                                if(u.getGuild().isInGuild(name)){
                                    if(u.getGuild().getRank(p) == GuildRank.OFFICER && u.getGuild().getRank(name) != GuildRank.MEMBER){
                                        p.sendMessage(ChatColor.RED + "You are not allowed to kick that member from the guild.");
                                        return;
                                    }

                                    if(!name.equalsIgnoreCase(p.getName())){
                                        DungeonAPI.async(() -> {
                                            UUID uuid = u.getGuild().toGuildMember(name).getUUID();
                                            DungeonAPI.executeBungeeCommand("BungeeConsole","guildaction " + p.getName() + " " + u.getGuild().getId() + " reloadMembers");
                                            u.getGuild().sendMessage(ChatColor.LIGHT_PURPLE + PlayerUtilities.getNameFromUUID(uuid) + " was kicked from the guild by " + p.getName() + ".",GuildRank.MEMBER);
                                            u.getGuild().getMembers().remove(u.getGuild().toGuildMember(name));
                                            u.getGuild().saveMembers();
                                        });
                                    } else {
                                        p.sendMessage(ChatColor.RED + "You can't kick yourself from the guild.");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "That player is not in your guild.");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "You are not allowed to kick that member from the guild.");
                            }
                        } else {
                            p.sendMessage(ChatColor.RED + "You are not in a guild.");
                        }
                    } else if(args[0].equalsIgnoreCase("promote")){
                        if(u.isInGuild()){
                            if(u.getGuild().getRank(p) == GuildRank.LEADER){
                                String name = args[1];

                                if(u.getGuild().getRank(name) == GuildRank.MEMBER){
                                    DungeonAPI.async(() -> {
                                        u.getGuild().toGuildMember(name).setGuildRank(GuildRank.OFFICER);
                                        u.getGuild().saveMembers();
                                        DungeonAPI.executeBungeeCommand("BungeeConsole","guildaction " + p.getName() + " " + u.getGuild().getId() + " reloadMembers");
                                        u.getGuild().sendMessage(ChatColor.LIGHT_PURPLE + PlayerUtilities.getNameFromUUID(u.getGuild().toGuildMember(name).getUUID()) + " has been promoted to Officer!");
                                    });
                                } else {
                                    p.sendMessage(ChatColor.RED + "That player has already been promoted to officer.");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "Only the guild leader may promote members.");
                            }
                        } else {
                            p.sendMessage(ChatColor.RED + "You are not in a guild.");
                        }
                    } else if(args[0].equalsIgnoreCase("demote")){
                        if(u.isInGuild()){
                            if(u.getGuild().getRank(p) == GuildRank.LEADER){
                                String name = args[1];

                                if(u.getGuild().getRank(name) == GuildRank.OFFICER){
                                    DungeonAPI.async(() -> {
                                        u.getGuild().toGuildMember(name).setGuildRank(GuildRank.MEMBER);
                                        u.getGuild().saveMembers();
                                        DungeonAPI.executeBungeeCommand("BungeeConsole","guildaction " + p.getName() + " " + u.getGuild().getId() + " reloadMembers");
                                        u.getGuild().sendMessage(ChatColor.LIGHT_PURPLE + PlayerUtilities.getNameFromUUID(u.getGuild().toGuildMember(name).getUUID()) + " has been demoted to Member!");
                                    });
                                } else {
                                    p.sendMessage(ChatColor.RED + "Only officers may be demoted.");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "Only the guild leader may promote members.");
                            }
                        } else {
                            p.sendMessage(ChatColor.RED + "You are not in a guild.");
                        }
                    } else if(args[0].equalsIgnoreCase("transfer")){
                        if(u.isInGuild()){
                            if(u.getGuild().getRank(p) == GuildRank.LEADER){
                                String name = args[1];

                                if(u.getGuild().getRank(name) == GuildRank.OFFICER){
                                    DungeonAPI.async(() -> {
                                        u.getGuild().toGuildMember(name).setGuildRank(GuildRank.LEADER);
                                        u.getGuild().toGuildMember(p).setGuildRank(GuildRank.OFFICER);
                                        u.getGuild().saveMembers();
                                        DungeonAPI.executeBungeeCommand("BungeeConsole","guildaction " + p.getName() + " " + u.getGuild().getId() + " reloadMembers");
                                        u.getGuild().sendMessage(ChatColor.LIGHT_PURPLE + p.getName() + " has transferred the leadership of the guild to " + PlayerUtilities.getNameFromUUID(u.getGuild().toGuildMember(name).getUUID()) + "!");
                                    });
                                } else {
                                    p.sendMessage(ChatColor.RED + "Only officers may be promoted to Leader.");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "Only the guild leader may promote members.");
                            }
                        } else {
                            p.sendMessage(ChatColor.RED + "You are not in a guild.");
                        }
                    } else if(args[0].equalsIgnoreCase("accept")){
                        if(!u.isInGuild()){
                            if(Util.isValidInteger(args[1])){
                                int id = Integer.parseInt(args[1]);
                                Guild guild = Guild.getGuild(id);

                                if(guild != null){
                                    if(guild.getInvitedPlayers().contains(p)){
                                        DungeonAPI.async(() -> {
                                            guild.getInvitedPlayers().remove(p);
                                            GuildMember m = new GuildMember();
                                            m.setUUID(p.getUniqueId());
                                            m.setGuildRank(GuildRank.MEMBER);
                                            m.setTimeJoined(new Timestamp(System.currentTimeMillis()));
                                            guild.getMembers().add(m);
                                            guild.saveMembers();
                                            u.setGuildID(guild.getId());
                                            u.saveData();
                                            DungeonAPI.executeBungeeCommand("BungeeConsole","guildaction " + p.getName() + " " + u.getGuild().getId() + " reloadMembers");
                                            guild.sendMessage(ChatColor.LIGHT_PURPLE + p.getName() + " has joined the guild.");
                                        });
                                    } else {
                                        p.sendMessage(ChatColor.RED + "That guild hasn't invited you.");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "That guild doesn't exist.");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "Please enter a valid guild ID.");
                            }
                        } else {
                            p.sendMessage(ChatColor.RED + "You are already in a guild.");
                        }
                    } else if(args[0].equalsIgnoreCase("deny") || args[0].equalsIgnoreCase("decline") || args[0].equalsIgnoreCase("refuse")){
                        if(!u.isInGuild()){
                            if(Util.isValidInteger(args[1])){
                                int id = Integer.parseInt(args[1]);
                                Guild guild = Guild.getGuild(id);

                                if(guild != null){
                                    if(guild.getInvitedPlayers().contains(p)){
                                        guild.getInvitedPlayers().remove(p);
                                        u.getGuild().sendMessage(ChatColor.LIGHT_PURPLE + p.getName() + " has been denied the guild invitation.",GuildRank.OFFICER);
                                    } else {
                                        p.sendMessage(ChatColor.RED + "That guild doesn't exist.");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "That guild doesn't exist.");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "Please enter a valid guild ID.");
                            }
                        } else {
                            p.sendMessage(ChatColor.RED + "You are already in a guild.");
                        }
                    } else {
                        showUsage(p);
                    }
                }
            } else if(label.equalsIgnoreCase("g")){
                if(u.isInGuild()){
                    if(args.length > 0){
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < args.length; i++) {
                            sb.append(args[i]).append(" ");
                        }
                        String message = sb.toString().trim();

                        DungeonAPI.executeBungeeCommand("BungeeConsole","guildaction " + p.getName() + " " + u.getGuild().getId() + " chat " + message);
                    } else {
                        showUsage(p);
                    }
                } else {
                    p.sendMessage(ChatColor.RED + "You are not in a guild.");
                }
            }
        }
    }
}
