package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class PlayerPickupListener implements Listener {
    @EventHandler
    public void onPickup(PlayerPickupItemEvent e){
        Player p = e.getPlayer();

        if(GameUser.isLoaded(p)){
            GameUser u = GameUser.getUser(p);

            if(e.getItem().getItemStack() != null){
                if(!e.isCancelled()){
                    e.setCancelled(true);
                    CustomItem item = CustomItem.fromItemStack(e.getItem().getItemStack());

                    if(item != null){
                        e.getItem().remove();
                        p.getInventory().addItem(item.build(p));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onMerge(ItemMergeEvent e){
        e.setCancelled(true);
    }
}
