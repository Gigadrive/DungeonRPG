package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonrpg.event.CustomNPCInteractEvent;
import net.wrathofdungeons.dungeonrpg.inv.BuyingMerchantMenu;
import net.wrathofdungeons.dungeonrpg.npc.CustomNPC;
import net.wrathofdungeons.dungeonrpg.npc.CustomNPCType;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NPCInteractListener implements Listener {
    @EventHandler
    public void onInteact(CustomNPCInteractEvent e){
        Player p = e.getPlayer();
        GameUser u = GameUser.getUser(p);
        CustomNPC npc = e.getNPC();

        if(!u.isInSetupMode()){
            if(npc.getNpcType() == CustomNPCType.BUYING_MERCHANT){
                BuyingMerchantMenu.openFor(p);
            }
        } else {
            p.sendMessage(ChatColor.YELLOW + "NPC ID: " + npc.getId());
            p.sendMessage(ChatColor.YELLOW + "Entity: " + npc.getEntityType().toString());
            p.sendMessage(ChatColor.YELLOW + "Type: " + npc.getNpcType().toString());
        }
    }
}
