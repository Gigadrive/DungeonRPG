package net.wrathofdungeons.dungeonrpg.listener;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.items.*;
import net.wrathofdungeons.dungeonrpg.items.awakening.AwakeningType;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.mobs.MobData;
import net.wrathofdungeons.dungeonrpg.mobs.MobType;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.util.WorldUtilities;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

public class DeathListener implements Listener {
    @EventHandler
    public void onEntityDeath(EntityDeathEvent e){
        e.setDroppedExp(0);
        e.getDrops().clear();
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e){
        Player p = e.getEntity();

        if(GameUser.isLoaded(p)){
            GameUser u = GameUser.getUser(p);

            if(u.getCurrentCharacter() != null){
                p.setHealth(p.getMaxHealth());
                u.setHP(u.getMaxHP());
                u.setMP(u.getMaxMP());
                for(PotionEffect pe : p.getActivePotionEffects()) p.removePotionEffect(pe.getType());

                p.teleport(DungeonRPG.getNearestTown(p));
                p.sendMessage(ChatColor.RED + "You died!");
            }
        }
    }
}
