package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.inv.CharacterSelectionMenu;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

public class InventoryCloseListener implements Listener {
    @EventHandler
    public void onClose(InventoryCloseEvent e){
        if(e.getPlayer() instanceof Player){
            Player p = (Player)e.getPlayer();
            Inventory inv = e.getInventory();

            if(GameUser.isLoaded(p)){
                GameUser u = GameUser.getUser(p);

                new BukkitRunnable(){
                    @Override
                    public void run() {
                        if(p.isOnline()){
                            if(u.getCurrentCharacter() == null && !u.isInSetupMode()){
                                if(p.getOpenInventory() == null || p.getOpenInventory().getType() == null || p.getOpenInventory().getType() == InventoryType.CRAFTING || p.getOpenInventory().getTitle().equals("container.crafting")){
                                    CharacterSelectionMenu.openSelection(p);
                                }
                            }
                        }
                    }
                }.runTaskLater(DungeonRPG.getInstance(),20);

                if(inv.getName().equals("Buying Merchant")){
                    int[] i = new int[]{0,1,2,3,4,5,6,7};

                    for(int ii : i){
                        if(inv.getItem(ii) != null){
                            if(CustomItem.fromItemStack(inv.getItem(ii)) != null){
                                p.getInventory().addItem(CustomItem.fromItemStack(inv.getItem(ii)).build(p));
                            }
                        }
                    }
                }
            }
        }
    }
}
