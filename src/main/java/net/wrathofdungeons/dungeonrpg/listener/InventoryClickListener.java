package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryClickListener implements Listener {
    @EventHandler
    public void onClick(InventoryClickEvent e){
        if(e.getWhoClicked() instanceof Player){
            Player p = (Player)e.getWhoClicked();
            p.sendMessage("SLOT: " + e.getSlot() + " RAWSLOT: " + e.getRawSlot() + " ACTION: " + e.getAction().toString());

            if(GameUser.isLoaded(p)){
                GameUser u = GameUser.getUser(p);

                if(u.getCurrentCharacter() != null){
                    if(e.getCurrentItem() != null){
                        CustomItem item = CustomItem.fromItemStack(e.getCurrentItem());

                        if(item != null){
                            if(item.getData().getId() == 5){
                                e.setCancelled(true);
                            }
                        }
                    }
                }
            }
        }
    }
}
