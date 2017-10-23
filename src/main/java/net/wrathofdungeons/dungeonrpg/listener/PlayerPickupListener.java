package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.npc.CustomNPC;
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

            if(CustomNPC.READING.contains(p.getName())){
                e.setCancelled(true);
                return;
            }

            if(e.getItem().getItemStack() != null){
                if(!e.isCancelled()){
                    e.setCancelled(true);
                    CustomItem item = CustomItem.fromItemStack(e.getItem().getItemStack());

                    if(item != null){
                        if(e.getItem().hasMetadata("assignedPlayer") && e.getItem().hasMetadata("dropTime")){
                            long dropTime = e.getItem().getMetadata("dropTime").get(0).asLong();
                            long now = System.currentTimeMillis();
                            String assignedPlayer = e.getItem().getMetadata("assignedPlayer").get(0).asString();

                            if(assignedPlayer.equalsIgnoreCase(p.getName())){
                                e.getItem().remove();
                                p.getInventory().addItem(item.build(p));
                            } else {
                                if(now-dropTime >= 30*1000 && !item.isUntradeable()){
                                    e.getItem().remove();
                                    p.getInventory().addItem(item.build(p));
                                }
                            }
                        }
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
