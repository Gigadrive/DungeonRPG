package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class PlayerDropListener implements Listener {
    @EventHandler
    public void onDrop(PlayerDropItemEvent e){
        Player p = e.getPlayer();

        if(GameUser.isLoaded(p)){
            GameUser u = GameUser.getUser(p);

            if(u.getCurrentCharacter() != null){
                if(e.getItemDrop() != null && e.getItemDrop().getItemStack() != null){
                    CustomItem item = CustomItem.fromItemStack(e.getItemDrop().getItemStack());

                    if(item != null){
                        if(item.getId() == 5){
                            e.setCancelled(true);
                        }
                    }
                }
            }
        }
    }
}
