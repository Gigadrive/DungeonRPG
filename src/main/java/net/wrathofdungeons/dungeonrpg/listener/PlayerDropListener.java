package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.npc.CustomNPC;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.util.WorldUtilities;
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

            if(CustomNPC.READING.contains(p.getName())){
                e.setCancelled(true);
                return;
            }

            if(!u.isInSetupMode()){
                if(u.getCurrentCharacter() != null){
                    if(e.getItemDrop() != null && e.getItemDrop().getItemStack() != null){
                        CustomItem item = CustomItem.fromItemStack(e.getItemDrop().getItemStack());

                        if(item != null){
                            if(item.getData().getId() == 5 || ((DungeonRPG.ENABLE_BOWDRAWBACK) && item.getData().getId() == 6)){
                                e.setCancelled(true);
                                return;
                            }
                        }

                        if(!e.isCancelled() && e.getItemDrop() != null){
                            WorldUtilities.addAssignmentData(e.getItemDrop(),p);
                        }
                    }
                }
            } else {
                e.setCancelled(true);
            }
        }
    }
}
