package net.wrathofdungeons.dungeonrpg.listener;

import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.wrathofdungeons.dungeonrpg.event.CustomNPCInteractEvent;
import net.wrathofdungeons.dungeonrpg.inv.AwakeningMenu;
import net.wrathofdungeons.dungeonrpg.inv.BuyingMerchantMenu;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.npc.CustomNPC;
import net.wrathofdungeons.dungeonrpg.npc.CustomNPCType;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
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
            } else if(npc.getNpcType() == CustomNPCType.AWAKENING_SPECIALIST){
                AwakeningMenu.openFor(p);
            }
        } else {
            p.sendMessage(ChatColor.YELLOW + "NPC ID: " + npc.getId());
            p.sendMessage(ChatColor.YELLOW + "Entity: " + npc.getEntityType().toString());
            p.sendMessage(ChatColor.YELLOW + "Type: " + npc.getNpcType().toString());
        }
    }

    @EventHandler
    public void onSpawn(NPCSpawnEvent e){
        CustomEntity entity = null;

        for(CustomEntity c : CustomEntity.STORAGE.values()){
            if(c.toCitizensNPC() != null && (c.toCitizensNPC() == e.getNPC() || c.toCitizensNPC().getId() == e.getNPC().getId())){
                entity = c;
            }
        }

        if(entity != null) entity.setBukkitEntity((LivingEntity)e.getNPC().getEntity());
    }
}
