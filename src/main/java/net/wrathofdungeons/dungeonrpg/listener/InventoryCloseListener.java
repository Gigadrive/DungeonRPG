package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.inv.CharacterSelectionMenu;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.scheduler.BukkitRunnable;

public class InventoryCloseListener implements Listener {
    @EventHandler
    public void onClose(InventoryCloseEvent e){
        if(e.getPlayer() instanceof Player){
            Player p = (Player)e.getPlayer();

            if(GameUser.isLoaded(p)){
                GameUser u = GameUser.getUser(p);

                new BukkitRunnable(){
                    @Override
                    public void run() {
                        if(u.getCurrentCharacter() == null && !u.isInSetupMode()){
                            if(p.getOpenInventory() == null || p.getOpenInventory().getType() == null || p.getOpenInventory().getType() == InventoryType.CRAFTING || p.getOpenInventory().getTitle().equals("container.crafting")){
                                CharacterSelectionMenu.openSelection(p);
                            }
                        }
                    }
                }.runTaskLater(DungeonRPG.getInstance(),20);
            }
        }
    }
}
