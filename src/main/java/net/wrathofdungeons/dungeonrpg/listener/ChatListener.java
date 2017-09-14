package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

public class ChatListener implements Listener {
    @EventHandler
    public void onChat(PlayerChatEvent e){
        Player p = e.getPlayer();
        String msg = e.getMessage();

        if(GameUser.isLoaded(p)){
            GameUser u = GameUser.getUser(p);

            if(u.getCurrentCharacter() != null){
                e.setCancelled(true);

                p.sendMessage(ChatColor.GRAY + "[" + u.getCurrentCharacter().getRpgClass().getName().substring(0,2) + u.getCurrentCharacter().getLevel() + "] " + u.getRank().getColor() + p.getName() + ": " + ChatColor.WHITE + msg);

                for(Entity entity : p.getNearbyEntities(60,60,60)){
                    if(entity instanceof Player){
                        if(CustomEntity.fromEntity((LivingEntity)entity) == null){
                            Player p2 = (Player)entity;

                            if(GameUser.isLoaded(p2)){
                                GameUser u2 = GameUser.getUser(p2);

                                if(u2.getCurrentCharacter() != null){
                                    double distance = p.getLocation().distance(p2.getLocation());

                                    ChatColor color = null;

                                    if(distance < 20){
                                        color = ChatColor.WHITE;
                                    } else if(distance > 20 && distance < 40){
                                        color = ChatColor.GRAY;
                                    } else if(distance > 40 && distance < 60){
                                        color = ChatColor.DARK_GRAY;
                                    }

                                    if(color != null){
                                        p2.sendMessage(ChatColor.GRAY + "[" + u.getCurrentCharacter().getRpgClass().getName().substring(0,2) + u.getCurrentCharacter().getLevel() + "] " + u.getRank().getColor() + p.getName() + ": " + color + msg);
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                e.setCancelled(true);
            }
        } else {
            e.setCancelled(true);
        }
    }
}
