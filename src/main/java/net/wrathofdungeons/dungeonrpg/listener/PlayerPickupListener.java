package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.npc.CustomNPC;
import net.wrathofdungeons.dungeonrpg.quests.Quest;
import net.wrathofdungeons.dungeonrpg.quests.QuestObjective;
import net.wrathofdungeons.dungeonrpg.quests.QuestProgressStatus;
import net.wrathofdungeons.dungeonrpg.quests.QuestStage;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.ChatColor;
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
                                u.playItemPickupSound();

                                for(Quest q : Quest.STORAGE.values()){
                                    if(u.getCurrentCharacter().getStatus(q) == QuestProgressStatus.STARTED){
                                        QuestStage stage = q.getStages()[u.getCurrentCharacter().getCurrentStage(q)];

                                        if(stage != null){
                                            for(QuestObjective o : stage.objectives){
                                                if(o.itemToFind == item.getData().getId()){
                                                    p.sendMessage(ChatColor.GRAY + "[Find " + ChatColor.stripColor(item.getData().getName()) + " " + ChatColor.WHITE + "(" + u.getAmountInInventory(item.getData()) + "/" + o.itemToFindAmount + ")" + ChatColor.GRAY + "]");
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                if(now-dropTime >= 30*1000 && !item.isUntradeable()){
                                    e.getItem().remove();
                                    p.getInventory().addItem(item.build(p));
                                    u.playItemPickupSound();
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
