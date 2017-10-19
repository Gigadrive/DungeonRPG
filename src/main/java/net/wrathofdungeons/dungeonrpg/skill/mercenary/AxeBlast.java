package net.wrathofdungeons.dungeonrpg.skill.mercenary;

import net.wrathofdungeons.dungeonapi.util.ParticleEffect;
import net.wrathofdungeons.dungeonrpg.Duel;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.damage.DamageHandler;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.skill.Skill;
import net.wrathofdungeons.dungeonrpg.skill.SkillType;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class AxeBlast implements Skill {
    @Override
    public String getName() {
        return "Axe Blast";
    }

    @Override
    public RPGClass getRPGClass() {
        return RPGClass.MERCENARY;
    }

    @Override
    public SkillType getType() {
        return SkillType.FAST_ATTACK;
    }

    @Override
    public String getCombo() {
        return "RLR";
    }

    @Override
    public void execute(Player p) {
        GameUser u = GameUser.getUser(p);
        Location loc = p.getEyeLocation();
        AxeBlast axeBlast = this;

        int bashes = 1;
        int rangeDifference = 2;
        final int explodingRange = 3;
        final int baseRangeDifference = 3;

        try {
            BlockIterator bi = new BlockIterator(loc, 0.0, baseRangeDifference+(bashes*rangeDifference));

            Location lastLoc = null;
            while(bi.hasNext()){
                Block b = bi.next();

                if(b.getType() == Material.AIR || !b.getType().isSolid()){
                    lastLoc = b.getLocation();
                } else {
                    break;
                }
            }

            while(lastLoc.getY() > 0 && (lastLoc.getBlock() == null || lastLoc.getBlock().getType() == null || lastLoc.getBlock().getType() == Material.AIR || !lastLoc.getBlock().getType().isBlock())) lastLoc.setY(lastLoc.getY()-1);

            ArrayList<Location> locations = new ArrayList<Location>();

            loc = p.getLocation();
            int c = (int)Math.ceil(loc.distance(lastLoc) / 2F) - 1;
            if(c > 0){
                Vector v = lastLoc.toVector().subtract(loc.toVector()).normalize().multiply(2F);
                Location l = loc.clone();
                for (int i = 0; i < c; i++) {
                    l.add(v);
                    while(l.getY() > 0 && (l.getBlock() == null || l.getBlock().getType() == null || l.getBlock().getType() == Material.AIR || (!l.getBlock().getType().isBlock() && !l.getBlock().getType().isSolid()))) l.setY(l.getY()-1);
                    while(l.getY() > 0 && (l.getBlock() != null && l.getBlock().getType() != null && l.getBlock().getType() != Material.AIR && l.getBlock().getType().isBlock() && l.getBlock().getType().isSolid())) l.setY(l.getY()+1);

                    //p.sendMessage("X: " + l.getX() + " Y: " + l.getY() + " Z: " + l.getZ());

                    if(l.distance(loc) < baseRangeDifference) continue;

                    locations.add(l.clone());
                }
            }

            if(bashes > locations.size()) bashes = locations.size();

            for(int casts = 0; casts < bashes; casts++){
                final int castNumber = casts;
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        Location castLocation = locations.get(castNumber);
                        Location particleLocation = castLocation.clone().add(0,0.1,0);

                        ParticleEffect.SMOKE_LARGE.display(0.05f,0.05f,0.05f,0.005f,60,particleLocation,600);
                        particleLocation.getWorld().playSound(particleLocation,Sound.EXPLODE,1f,1f);

                        for(Entity entity : castLocation.getWorld().getNearbyEntities(castLocation,explodingRange,explodingRange,explodingRange)){
                            if(entity instanceof LivingEntity){
                                LivingEntity livingEntity = (LivingEntity)entity;
                                CustomEntity c = CustomEntity.fromEntity(livingEntity);

                                if(c != null){
                                    DungeonRPG.showBloodEffect(c.getBukkitEntity().getLocation());
                                    c.getData().playSound(c.getBukkitEntity().getLocation());
                                    c.damage(DamageHandler.calculatePlayerToMobDamage(u,c,axeBlast),p);
                                    c.giveNormalKnockback(castLocation);
                                } else {
                                    if(livingEntity instanceof Player){
                                        Player p2 = (Player)livingEntity;

                                        if(GameUser.isLoaded(p2)){
                                            GameUser u2 = GameUser.getUser(p2);

                                            if(Duel.isDuelingWith(p,p2)){
                                                DungeonRPG.showBloodEffect(entity.getLocation());
                                                u2.damage(DamageHandler.calculatePlayerToPlayerDamage(u,u2,axeBlast),p);
                                                u2.giveNormalKnockback(castLocation);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }.runTaskLater(DungeonRPG.getInstance(),casts*10);
            }
        } catch(Exception e){
            p.sendMessage(ChatColor.RED + "Skill failed! Are you standing too close to something?");
        }
    }
}
