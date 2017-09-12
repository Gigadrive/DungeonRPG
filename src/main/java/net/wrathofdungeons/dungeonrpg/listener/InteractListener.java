package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class InteractListener implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e){
        Player p = e.getPlayer();

        if(GameUser.isLoaded(p)){
            if(e.getAction() == Action.PHYSICAL){
                if(e.getClickedBlock() != null && e.getClickedBlock().getType() == Material.SOIL){
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onEntityInteract(EntityInteractEvent e){
        if(e.getEntity().getType() == EntityType.PLAYER) return;

        if(e.getBlock() != null && e.getBlock().getType() == Material.SOIL){
            e.setCancelled(true);
        }
    }
}
