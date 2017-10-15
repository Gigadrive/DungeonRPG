package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.damage.DamageHandler;
import net.wrathofdungeons.dungeonrpg.event.CustomDamageEvent;
import net.wrathofdungeons.dungeonrpg.event.CustomDamageMobToMobEvent;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.items.ItemCategory;
import net.wrathofdungeons.dungeonrpg.items.awakening.AwakeningType;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class CustomDamageListener implements Listener {

    //
    // PLAYER -> MOB
    //

    @EventHandler
    public void playerToMob(CustomDamageEvent e){
        if(e.isPlayerAttacking()){
            Player p = e.getPlayer();
            GameUser u = GameUser.getUser(p);
            CustomEntity c = e.getEntity();

            if(p.getItemInHand() != null && CustomItem.fromItemStack(p.getItemInHand()) != null) {
                CustomItem item = CustomItem.fromItemStack(p.getItemInHand());
                long itemCooldown = item.getCooldownInTicks();

                boolean wrongClass = false;

                if(item.getData().getCategory() == ItemCategory.WEAPON_BOW && !(u.getCurrentCharacter().getRpgClass() == RPGClass.ARCHER || u.getCurrentCharacter().getRpgClass() == RPGClass.RANGER || u.getCurrentCharacter().getRpgClass() == RPGClass.HUNTER)){
                    wrongClass = true;
                }

                if(item.getData().getCategory() == ItemCategory.WEAPON_STICK && !(u.getCurrentCharacter().getRpgClass() == RPGClass.MAGICIAN || u.getCurrentCharacter().getRpgClass() == RPGClass.WIZARD || u.getCurrentCharacter().getRpgClass() == RPGClass.ALCHEMIST)){
                    wrongClass = true;
                }

                if(item.getData().getCategory() == ItemCategory.WEAPON_AXE && !(u.getCurrentCharacter().getRpgClass() == RPGClass.MERCENARY || u.getCurrentCharacter().getRpgClass() == RPGClass.KNIGHT || u.getCurrentCharacter().getRpgClass() == RPGClass.SOLDIER)){
                    wrongClass = true;
                }

                if(item.getData().getCategory() == ItemCategory.WEAPON_SHEARS && !(u.getCurrentCharacter().getRpgClass() == RPGClass.ASSASSIN || u.getCurrentCharacter().getRpgClass() == RPGClass.BLADEMASTER || u.getCurrentCharacter().getRpgClass() == RPGClass.NINJA)){
                    wrongClass = true;
                }

                if(item.getData().getNeededClass() != RPGClass.NONE){
                    if(item.getData().getNeededClass() == RPGClass.MERCENARY){
                        if(u.getCurrentCharacter().getRpgClass() != RPGClass.MERCENARY && u.getCurrentCharacter().getRpgClass() != RPGClass.KNIGHT && u.getCurrentCharacter().getRpgClass() != RPGClass.SOLDIER) wrongClass = true;
                    } else if(item.getData().getNeededClass() == RPGClass.MAGICIAN){
                        if(u.getCurrentCharacter().getRpgClass() != RPGClass.MAGICIAN && u.getCurrentCharacter().getRpgClass() != RPGClass.WIZARD && u.getCurrentCharacter().getRpgClass() != RPGClass.ALCHEMIST) wrongClass = true;
                    } else if(item.getData().getNeededClass() == RPGClass.ASSASSIN){
                        if(u.getCurrentCharacter().getRpgClass() != RPGClass.ASSASSIN && u.getCurrentCharacter().getRpgClass() != RPGClass.BLADEMASTER && u.getCurrentCharacter().getRpgClass() != RPGClass.NINJA) wrongClass = true;
                    } else if(item.getData().getNeededClass() == RPGClass.ARCHER){
                        if(u.getCurrentCharacter().getRpgClass() != RPGClass.ARCHER && u.getCurrentCharacter().getRpgClass() != RPGClass.RANGER && u.getCurrentCharacter().getRpgClass() != RPGClass.HUNTER) wrongClass = true;
                    } else {
                        if(u.getCurrentCharacter().getRpgClass() != item.getData().getNeededClass()) wrongClass = true;
                    }
                }

                if(wrongClass){
                    p.sendMessage(ChatColor.DARK_RED + "This weapon is for a different class.");
                    return;
                }

                double damage = DamageHandler.calculatePlayerToMobDamage(u,c,null);
                int hpLeech = u.getCurrentCharacter().getTotalValue(AwakeningType.HP_LEECH);
                if(hpLeech > 0){
                    u.addHP(damage*(hpLeech*0.01));
                }

                int mpLeech = u.getCurrentCharacter().getTotalValue(AwakeningType.MP_LEECH);
                if(mpLeech > 0){
                    u.addMP(damage*(mpLeech*0.01));
                }

                c.damage(damage,p);
                c.giveNormalKnockback(p.getLocation(),e.isProjectile());
            } else {
                c.damage(1,p);
                c.giveNormalKnockback(p.getLocation(),e.isProjectile());
            }
        }
    }

    //
    // MOB -> PLAYER
    //

    @EventHandler
    public void mobToPlayer(CustomDamageEvent e){
        if(!e.isPlayerAttacking()){
            Player p = e.getPlayer();
            GameUser u = GameUser.getUser(p);
            CustomEntity c = e.getEntity();

            p.damage(0);

            double damage = DamageHandler.calculateMobToPlayerDamage(u,c);
            int thorns = u.getCurrentCharacter().getTotalValue(AwakeningType.THORNS);
            u.damage(damage,c.getBukkitEntity());

            if(thorns > 0){
                damage *= thorns*0.01;
                c.getBukkitEntity().damage(damage,p);
            }

            u.giveNormalKnockback(c.getBukkitEntity().getLocation(),e.isProjectile());
            DungeonRPG.showBloodEffect(p.getLocation());
        }
    }

    //
    // MOB -> MOB
    //

    @EventHandler
    public void mobToMob(CustomDamageMobToMobEvent e){
        CustomEntity damager = e.getDamager();
        CustomEntity entity = e.getEntity();

        entity.damage(damager.getData().getAtk());
        entity.giveNormalKnockback(damager.getBukkitEntity().getLocation(),e.isProjectile());
    }
}
