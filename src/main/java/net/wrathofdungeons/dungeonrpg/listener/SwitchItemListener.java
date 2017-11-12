package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.items.ItemCategory;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;

public class SwitchItemListener implements Listener {
    @EventHandler
    public void onSwitch(PlayerItemHeldEvent e){
        Player p = e.getPlayer();

        if(GameUser.isLoaded(p)){
            GameUser u = GameUser.getUser(p);

            if(u.getCurrentCharacter() != null){
                CustomItem item = CustomItem.fromItemStack(p.getInventory().getItem(e.getNewSlot()));
                u.updateHandSpeed(item);

                if(item != null){
                    if(p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE){
                        if(item.getData().getCategory() != ItemCategory.PICKAXE || !item.mayUse(p)){
                            p.setGameMode(GameMode.ADVENTURE);
                        } else if(item.getData().getCategory() == ItemCategory.PICKAXE && item.mayUse(p)){
                            p.setGameMode(GameMode.SURVIVAL);
                        }
                    }
                }
            }
        }
    }
}
