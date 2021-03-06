package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.projectile.DungeonProjectile;
import net.wrathofdungeons.dungeonrpg.projectile.DungeonProjectileType;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;

public class ShootBowListener implements Listener {
    @EventHandler
    public void onShoot(EntityShootBowEvent e){
        if(e.getEntity() instanceof Player){
            Player p = (Player)e.getEntity();

            if(GameUser.isLoaded(p)){
                GameUser u = GameUser.getUser(p);

                if (u.getCurrentCharacter() == null || (u.getCurrentCharacter() != null && !u.getCurrentCharacter().getRpgClass().matches(RPGClass.ARCHER)) || CustomItem.fromItemStack(e.getBow()) == null || u.isRespawning()) {
                    e.setCancelled(true);
                    return;
                }

                long itemCooldown = u.getCurrentCharacter().getAttackSpeedTicks();

                CustomItem customItem = CustomItem.fromItemStack(e.getBow());

                e.setCancelled(true);

                Arrow oldArrow = e.getProjectile() != null ? (Arrow)e.getProjectile() : null;
                Arrow projectile;

                if(oldArrow != null){
                    projectile = (Arrow)p.launchProjectile(Arrow.class);

                    projectile.setVelocity(oldArrow.getVelocity());
                    projectile.setCritical(oldArrow.isCritical());
                    projectile.setKnockbackStrength(oldArrow.getKnockbackStrength());

                    oldArrow.remove();
                } else {
                    projectile = oldArrow;
                }

                double damage = 1;
                damage += Util.randomDouble(customItem.getData().getAtkMin(), customItem.getData().getAtkMax());

                DungeonProjectile data = new DungeonProjectile(p, DungeonProjectileType.ARCHER_ARROW, projectile.getLocation(), 0, damage, false);
                data.setEntity(projectile);
                DungeonRPG.SHOT_PROJECTILE_DATA.put(projectile.getUniqueId().toString(), data);

                u.setAttackCooldown(true);
                u.playShootArrowSound();

                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRPG.getInstance(), new Runnable(){
                    public void run(){
                        u.setAttackCooldown(false);
                        u.updateArrows();
                    }
                }, itemCooldown);

                u.updateArrows();
                p.updateInventory();
            }
        }
    }
}
