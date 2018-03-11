package net.wrathofdungeons.dungeonrpg.listener;

import de.dytanic.cloudnet.api.CloudAPI;
import de.dytanic.cloudnet.lib.player.CloudPlayer;
import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.util.PlayerUtilities;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.guilds.Guild;
import net.wrathofdungeons.dungeonrpg.guilds.GuildRank;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.UUID;

public class PluginMessageListener implements Listener, org.bukkit.plugin.messaging.PluginMessageListener {
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if(channel.equals("BungeeCord")) {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));

            try {
                String subchannel = in.readUTF();
                if(subchannel == null) return;

                if(subchannel.equals("guildAction")){
                    String executor = in.readUTF();
                    String i = in.readUTF();
                    if(Util.isValidInteger(i)){
                        int guildID = Integer.parseInt(i);

                        String action = in.readUTF();

                        if(action != null){
                            if(action.equals("chat")){
                                String m = in.readUTF();

                                if(m != null){
                                    for(Player p : Bukkit.getOnlinePlayers()){
                                        if(GameUser.isLoaded(p)){
                                            GameUser u = GameUser.getUser(p);

                                            if(u.getCurrentCharacter() != null && u.getGuildID() == guildID){
                                                p.sendMessage(ChatColor.DARK_PURPLE + "[" + executor + "] " + ChatColor.LIGHT_PURPLE + m);
                                            }
                                        }
                                    }
                                }
                            } else if(action.equals("reloadMembers")){
                                Guild guild = Guild.getGuild(guildID);
                                if(guild != null) DungeonAPI.async(() -> {
                                    guild.reloadMembers();
                                    DungeonRPG.updateNames();
                                });
                            } else if(action.equals("disband")){
                                for(Player p : Bukkit.getOnlinePlayers()){
                                    if(GameUser.isLoaded(p)){
                                        GameUser u = GameUser.getUser(p);

                                        if(u.getGuildID() == guildID){
                                            p.sendMessage(ChatColor.DARK_RED + "The guild has been disbanded.");
                                            u.setGuildID(0);
                                        }
                                    }
                                }

                                Guild.STORAGE.remove(guildID);
                                DungeonRPG.updateNames();
                            } else if(action.equals("message")){
                                String m = in.readUTF();

                                if(m != null){
                                    String[] s = m.split(" ");
                                    if(s.length >= 2){
                                        GuildRank minRank = GuildRank.valueOf(s[0]);

                                        m = m.substring(minRank.toString().length()+1);

                                        for(Player p : Bukkit.getOnlinePlayers()){
                                            if(GameUser.isLoaded(p)){
                                                GameUser u = GameUser.getUser(p);

                                                if(u.getCurrentCharacter() != null && u.getGuildID() == guildID){
                                                    if(u.getGuild().getRank(p).getId() >= minRank.getId()) p.sendMessage(m);
                                                }
                                            }
                                        }
                                    }
                                }
                            } else if(action.equals("")){

                            }
                        }
                    }
                } else if(subchannel.equals("callGlobalLogin")){
                    DungeonAPI.async(() -> {
                        try {
                            UUID uuid = UUID.fromString(in.readUTF());
                            int level = Integer.parseInt(in.readUTF());
                            String className = in.readUTF();
                            String playerName = PlayerUtilities.getNameFromUUID(uuid);

                            if(playerName != null){
                                for(Player p : Bukkit.getOnlinePlayers()){
                                    if(GameUser.isLoaded(p)){
                                        GameUser u = GameUser.getUser(p);

                                        if(u.getCurrentCharacter() != null){
                                            CloudPlayer cp = CloudAPI.getInstance().getOnlinePlayer(uuid);

                                            if(cp != null){
                                                String server = cp.getServer();

                                                if(server != null){
                                                    if(u.getFriends().contains(uuid.toString())){
                                                        p.sendMessage(ChatColor.AQUA + playerName + " has logged in on " + server + " as " + className + " [Lv. " + level + "]");
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } catch(Exception e){
                            e.printStackTrace();
                        }
                    });
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
