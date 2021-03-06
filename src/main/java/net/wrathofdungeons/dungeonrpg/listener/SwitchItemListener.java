package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.items.ItemCategory;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class SwitchItemListener implements Listener {
    @EventHandler
    public void onSwitch(PlayerItemHeldEvent e){
        Player p = e.getPlayer();

        if(GameUser.isLoaded(p)){
            GameUser u = GameUser.getUser(p);

            if(u.getCurrentCharacter() != null){
                if(DungeonRPG.SHOW_HP_IN_ACTION_BAR && (!u.currentCombo.equals("") || u.clearClickCombo != null)){
                    u.resetComboDisplay(e.getPreviousSlot());
                }

                CustomItem item = CustomItem.fromItemStack(p.getInventory().getItem(e.getNewSlot()));
                u.updateHandSpeed(item);
                u.updateHPBar();

                new BukkitRunnable(){
                    @Override
                    public void run() {
                        u.updateWalkSpeed();
                    }
                }.runTaskLater(DungeonRPG.getInstance(),5);

                if(u.getSkillValues().skillTasks != null && u.getSkillValues().skillTasks.size() > 0){
                    for(BukkitTask t : u.getSkillValues().skillTasks) t.cancel();

                    u.getSkillValues().skillTasks.clear();
                }

                if(item != null){
                    if(p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE){
                        if(item.getData().getCategory() != ItemCategory.PICKAXE || !item.mayUse(p)){
                            p.setGameMode(GameMode.ADVENTURE);
                        } else if(item.getData().getCategory() == ItemCategory.PICKAXE && item.mayUse(p)){
                            p.setGameMode(GameMode.SURVIVAL);
                        }
                    }
                } else {
                    if(p.getGameMode() == GameMode.SURVIVAL) p.setGameMode(GameMode.ADVENTURE);
                }
            }
        }
    }
}
