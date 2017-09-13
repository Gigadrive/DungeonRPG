package net.wrathofdungeons.dungeonrpg.listener;

import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class FrameProtectListener implements Listener {
    @EventHandler(priority=EventPriority.HIGHEST)
    public void onDestroyByEntity(HangingBreakByEntityEvent event) {
        if ((event.getRemover() instanceof Player)) {
            Player p = (Player)event.getRemover();

            if ((event.getEntity().getType() == EntityType.ITEM_FRAME) && (!p.isOp()) || (p.getGameMode() != GameMode.CREATIVE)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority= EventPriority.HIGHEST)
    public void OnPlaceByEntity(HangingPlaceEvent event) {
        if ((event.getPlayer() instanceof Player)) {
            Player p = (Player)event.getPlayer();

            if ((event.getEntity().getType() == EntityType.ITEM_FRAME) && (!p.isOp()) || (p.getGameMode() != GameMode.CREATIVE)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void canRotate(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();

        if (!entity.getType().equals(EntityType.ITEM_FRAME)) {
            return;
        }

		    /*ItemFrame iFrame = (ItemFrame)entity;
		    if ((iFrame.getItem().equals(null)) || (iFrame.getItem().getType().equals(Material.AIR))) {
		    	return;
		    }*/

        if(event.getPlayer().getGameMode() != GameMode.CREATIVE){
            event.setCancelled(true);
        } else {
            if(!player.isOp()){
                event.setCancelled(true);
            }
        }

        if (!entity.getType().equals(EntityType.ARMOR_STAND)) {
            return;
        }

        if(event.getPlayer().getGameMode() != GameMode.CREATIVE){
            event.setCancelled(true);
        } else {
            if(!player.isOp()){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void ItemRemoval(EntityDamageByEntityEvent e) {
        if ((e.getDamager() instanceof Player)) {
            Player p = (Player)e.getDamager();

            if ((e.getEntity().getType() == EntityType.ITEM_FRAME) && (!p.isOp()) || (e.getEntity().getType() == EntityType.ITEM_FRAME) && (p.getGameMode() != GameMode.CREATIVE)) {
                e.setCancelled(true);
            }

        }

        if (((e.getDamager() instanceof Projectile)) && (e.getEntity().getType() == EntityType.ITEM_FRAME)) {
            Projectile p = (Projectile)e.getDamager();

            if(p.getShooter() instanceof Player){
                Player player = (Player)p.getShooter();

                if ((!player.isOp()) || (player.getGameMode() != GameMode.CREATIVE)) {
                    e.setCancelled(true);
                }
            }
        }

        if ((e.getDamager() instanceof Player)) {
            Player p = (Player)e.getDamager();

            if ((e.getEntity().getType() == EntityType.ARMOR_STAND) && (!p.isOp()) || (e.getEntity().getType() == EntityType.ARMOR_STAND) && (p.getGameMode() != GameMode.CREATIVE)) {
                e.setCancelled(true);
            }

        }

        if (((e.getDamager() instanceof Projectile)) && (e.getEntity().getType() == EntityType.ARMOR_STAND)) {
            Projectile p = (Projectile)e.getDamager();

            if(p.getShooter() instanceof Player){
                Player player = (Player)p.getShooter();

                if ((!player.isOp()) || (player.getGameMode() != GameMode.CREATIVE)) {
                    e.setCancelled(true);
                }
            }
        }
    }
}
