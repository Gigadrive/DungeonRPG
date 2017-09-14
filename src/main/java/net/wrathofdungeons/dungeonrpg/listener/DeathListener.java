package net.wrathofdungeons.dungeonrpg.listener;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.items.*;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.mobs.MobData;
import net.wrathofdungeons.dungeonrpg.mobs.MobType;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class DeathListener implements Listener {
    @EventHandler
    public void onEntityDeath(EntityDeathEvent e){
        Entity ent = e.getEntity();

        if(ent instanceof LivingEntity){
            LivingEntity livingEntity = (LivingEntity)ent;
            CustomEntity c = CustomEntity.fromEntity(livingEntity);

            if(c != null){
                MobData mob = c.getData();

                e.setDroppedExp(0);
                e.getDrops().clear();
                c.remove();

                if(livingEntity.getKiller() != null){
                    Player p = livingEntity.getKiller();

                    if(p.isOnline()){
                        if(GameUser.isLoaded(p)){
                            GameUser u = GameUser.getUser(p);

                            if(u.getCurrentCharacter() != null){
                                // DROP GOLD
                                if(Util.getIntegerDifference(u.getCurrentCharacter().getLevel(),mob.getLevel()) <= DungeonRPG.PLAYER_MOB_LEVEL_DIFFERENCE){
                                    if(Util.getChanceBoolean(50,190)) livingEntity.getLocation().getWorld().dropItem(livingEntity.getLocation(),new CustomItem(7).build(p));
                                }

                                //TODO: Add pre-defined dropable items

                                // DROP WEAPONS [automated]
                                if(Util.getIntegerDifference(u.getCurrentCharacter().getLevel(), mob.getLevel()) < DungeonRPG.PLAYER_MOB_LEVEL_DIFFERENCE){
                                    if(mob.getMobType() == MobType.AGGRO){
                                        int limit = 2;

                                        if(Util.getChanceBoolean(15, 125)){
                                            int[] usableLvls = new int[]{mob.getLevel()-2, mob.getLevel()-1, mob.getLevel(), mob.getLevel()+1, mob.getLevel()+2};

                                            for(ItemData data : ItemData.STORAGE){
                                                if(data.getRarity().getSources() != null){
                                                    for(ItemSource s : data.getRarity().getSources()){
                                                        if(s.mobClass != null){
                                                            if(s.mobClass == mob.getMobClass()){
                                                                if(data.getCategory() == ItemCategory.ARMOR || data.getCategory() == ItemCategory.WEAPON_BOW || data.getCategory() == ItemCategory.WEAPON_AXE || data.getCategory() == ItemCategory.WEAPON_STICK || data.getCategory() == ItemCategory.WEAPON_SHEARS){
                                                                    if(data.getRarity() != ItemRarity.NONE && data.getRarity() != ItemRarity.SPECIAL){
                                                                        for(int i : usableLvls){
                                                                            if(i == data.getNeededLevel()){
                                                                                if(mob.getMobClass().getChance(data.getRarity()) != null){
                                                                                    if(Util.getChanceBoolean(mob.getMobClass().getChance(data.getRarity()).min, mob.getMobClass().getChance(data.getRarity()).max)){
                                                                                        if(limit != 0){
                                                                                            ent.getLocation().getWorld().dropItem(ent.getLocation(),new CustomItem(data).build(p));
                                                                                            limit--;
                                                                                        }
                                                                                    }

                                                                                    break;
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // GIVE EXP
                                double xp = Util.randomDouble(mob.getXp()/2, mob.getXp());

                                if(Util.getIntegerDifference(u.getCurrentCharacter().getLevel(), mob.getLevel()) < DungeonRPG.PLAYER_MOB_LEVEL_DIFFERENCE){
                                    xp = u.giveEXP(xp);
                                } else {
                                    xp = 0.0;
                                }

                                p.playSound(p.getEyeLocation(), Sound.ORB_PICKUP, 1F, 1F);

                                final Hologram holo = HologramsAPI.createHologram(DungeonRPG.getInstance(), ent.getLocation().clone().add(0,2,0));
                                holo.appendTextLine(ChatColor.GOLD + "+" + ((Double)xp).intValue() + " EXP");

                                new BukkitRunnable(){
                                    @Override
                                    public void run() {
                                        holo.delete();
                                    }
                                }.runTaskLater(DungeonRPG.getInstance(),10);
                            }
                        }
                    }
                }
            }
        }
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

                p.teleport(DungeonRPG.getNearestTown(p));
                p.sendMessage(ChatColor.RED + "You died!");
            }
        }
    }
}
