package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonapi.util.ParticleEffect;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.Duel;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.damage.DamageData;
import net.wrathofdungeons.dungeonrpg.damage.DamageHandler;
import net.wrathofdungeons.dungeonrpg.event.PlayerLandOnGroundEvent;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.skill.SkillStorage;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.util.WorldUtilities;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class LandOnGroundListener implements Listener {
    @EventHandler
    public void onLand(PlayerLandOnGroundEvent e){
        Player p = e.getPlayer();

        if(GameUser.isLoaded(p)){
            GameUser u = GameUser.getUser(p);

            if(u.getCurrentCharacter() != null){
                u.getSkillValues().leapIsInAir = false;

                if(u.getSkillValues().stomperActive){
                    int investedSkillPoints = u.getCurrentCharacter().getVariables().getInvestedSkillPoints(SkillStorage.getInstance().getSkill("Stomper"));
                    final int range = Integer.parseInt(SkillStorage.getInstance().getSkill("Stomper").getEffects(investedSkillPoints).get("Range"));

                    for(Location l : WorldUtilities.getParticleCircle(p.getLocation(),range/2,((int)(range*0.5))))
                        ParticleEffect.EXPLOSION_HUGE.display(0f,0f,0f,0.005f,3,l,600);

                    p.getWorld().playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE,1f,1f);

                    ArrayList<FallingBlock> blocks = new ArrayList<FallingBlock>();

                    for(Location l : WorldUtilities.getParticleCircle(p.getLocation().clone().add(0,1,0),3,10)){
                        Material material = null;

                        for(int i = 0; i < 4; i++){
                            if(material != null) break;

                            Location loc = l.clone().add(0,-i,0);

                            if(loc.getBlock() != null && loc.getBlock().getType() != null && loc.getBlock().getType().isSolid()) material = loc.getBlock().getType();
                        }

                        if(material == null) material = Material.DIRT;

                        FallingBlock b = p.getWorld().spawnFallingBlock(l, material,(byte)0);
                        blocks.add(b);

                        b.setDropItem(false);
                        b.setHurtEntities(false);

                        b.setVelocity(new Vector(0,0.5,0));
                    }

                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            for(FallingBlock b : blocks) b.remove();
                        }
                    }.runTaskLater(DungeonRPG.getInstance(),15);

                    for(Entity ent : p.getNearbyEntities(range,range,range)){
                        if(ent instanceof LivingEntity){
                            LivingEntity livingEntity = (LivingEntity)ent;
                            CustomEntity c = CustomEntity.fromEntity(livingEntity);

                            if(c != null){
                                u.ignoreDamageCheck = true;

                                DungeonRPG.showBloodEffect(livingEntity.getLocation());
                                c.getData().playSound(livingEntity.getLocation());
                                DamageData damageData = DamageHandler.calculatePlayerToMobDamage(u,c,u.getSkillValues().stomperSkill);
                                DamageHandler.spawnDamageIndicator(p,damageData,c.getBukkitEntity().getLocation());
                                c.damage(damageData.getDamage(),p);

                                new BukkitRunnable(){
                                    @Override
                                    public void run() {
                                        u.ignoreDamageCheck = false;
                                    }
                                }.runTaskLater(DungeonRPG.getInstance(),1);
                            } else {
                                if(ent.getType() == EntityType.PLAYER){
                                    Player p2 = (Player)ent;

                                    if(GameUser.isLoaded(p2) && Duel.isDuelingWith(p,p2)){
                                        GameUser u2 = GameUser.getUser(p2);

                                        DungeonRPG.showBloodEffect(livingEntity.getLocation());
                                        DamageData damageData = DamageHandler.calculatePlayerToPlayerDamage(u,u2,u.getSkillValues().stomperSkill);
                                        DamageHandler.spawnDamageIndicator(p,damageData,p2.getLocation());
                                        u2.damage(damageData.getDamage(),p);
                                    }
                                }
                            }
                        }
                    }

                    u.getSkillValues().stomperActive = false;
                }
            }
        }
    }

    @EventHandler
    public void onLand(EntityChangeBlockEvent e){
        if(e.getEntity().getType() == EntityType.FALLING_BLOCK) e.setCancelled(true);
    }
}
