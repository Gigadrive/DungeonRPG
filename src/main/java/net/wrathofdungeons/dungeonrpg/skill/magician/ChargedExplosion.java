package net.wrathofdungeons.dungeonrpg.skill.magician;

import net.wrathofdungeons.dungeonapi.util.ParticleEffect;
import net.wrathofdungeons.dungeonrpg.Duel;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.damage.DamageData;
import net.wrathofdungeons.dungeonrpg.damage.DamageHandler;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.skill.Skill;
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
import java.util.HashMap;

public class ChargedExplosion implements Skill {
    @Override
    public String getName() {
        return "Charged Explosion";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public HashMap<String, String> getEffects(int investedSkillPoints) {
        HashMap<String,String> effects = new HashMap<String, String>();

        int range = 12;
        int explosionRange = 1;

        if(investedSkillPoints == 2){
            range = 14;
            explosionRange = 2;
        } else if(investedSkillPoints == 3){
            range = 16;
            explosionRange = 2;
        } else if(investedSkillPoints == 4){
            range = 18;
            explosionRange = 3;
        } else if(investedSkillPoints == 5){
            range = 20;
            explosionRange = 4;
        }

        effects.put("Range",String.valueOf(range));
        effects.put("Explosion Range",String.valueOf(explosionRange));

        return effects;
    }

    @Override
    public int getIcon() {
        return 46;
    }

    @Override
    public int getIconDurability() {
        return 0;
    }

    @Override
    public RPGClass getRPGClass() {
        return RPGClass.MAGICIAN;
    }

    @Override
    public int getMinLevel() {
        return 30;
    }

    @Override
    public int getMaxInvestingPoints() {
        return 5;
    }

    @Override
    public int getBaseMPCost() {
        return 7;
    }

    @Override
    public void execute(Player p) {
        GameUser u = GameUser.getUser(p);
        int investedSkillPoints = u.getCurrentCharacter().getVariables().getInvestedSkillPoints(this);

        final int range = Integer.parseInt(getEffects(investedSkillPoints).get("Range"));
        final int explosionRange = Integer.parseInt(getEffects(investedSkillPoints).get("Explosion Range"));

        Location loc = p.getEyeLocation();
        try {
            BlockIterator bi = new BlockIterator(loc, 0.0, range);

            Location lastLoc = null;
            while(bi.hasNext()){
                Block b = bi.next();

                if(b.getType() == Material.AIR || !b.getType().isSolid()){
                    lastLoc = b.getLocation();
                } else {
                    break;
                }
            }

            //if(lastLoc == null) lastLoc = loc;

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
                    locations.add(l.clone());
                }
            }

            final Location lastLocL = lastLoc;
            final ChargedExplosion chargedExplosion = this;

            final BukkitRunnable f = new BukkitRunnable() {
                @Override
                public void run() {
                    //if(lastLocL != null) lastLocL.getWorld().strikeLightningEffect(lastLocL);
                    if(investedSkillPoints == 1){
                        ParticleEffect.EXPLOSION_LARGE.display(0f,0f,0f,0.005f,3,lastLocL,600);
                    } else if(investedSkillPoints == 2){
                        ParticleEffect.EXPLOSION_LARGE.display(0.05f,0.05f,0.05f,0.005f,7,lastLocL,600);
                    } else if(investedSkillPoints == 3){
                        ParticleEffect.EXPLOSION_LARGE.display(0.05f,0.05f,0.05f,0.005f,7,lastLocL,600);
                    } else if(investedSkillPoints == 4){
                        ParticleEffect.EXPLOSION_LARGE.display(0.05f,0.05f,0.05f,0.005f,9,lastLocL,600);
                    } else if(investedSkillPoints == 5){
                        ParticleEffect.EXPLOSION_HUGE.display(0.05f,0.05f,0.05f,0.005f,12,lastLocL,600);
                    }

                    lastLocL.getWorld().playSound(lastLocL, Sound.EXPLODE,1f,1f);

                    u.ignoreDamageCheck = true;
                    u.ignoreFistCheck = true;

                    for(Entity entity : lastLocL.getWorld().getNearbyEntities(lastLocL,explosionRange,explosionRange,explosionRange)){
                        if(entity instanceof LivingEntity){
                            CustomEntity c = CustomEntity.fromEntity((LivingEntity)entity);

                            if(c != null){
                                DungeonRPG.showBloodEffect(c.getBukkitEntity().getLocation());
                                c.getData().playSound(c.getBukkitEntity().getLocation());
                                DamageData damageData = DamageHandler.calculatePlayerToMobDamage(u,c,chargedExplosion);
                                DamageHandler.spawnDamageIndicator(p,damageData,c.getBukkitEntity().getLocation());
                                c.damage(damageData.getDamage(),p);
                            } else {
                                if(entity instanceof Player){
                                    Player p2 = (Player)entity;

                                    if(GameUser.isLoaded(p2) && Duel.isDuelingWith(p,p2)){
                                        GameUser u2 = GameUser.getUser(p2);
                                        DungeonRPG.showBloodEffect(entity.getLocation());
                                        DamageData damageData = DamageHandler.calculatePlayerToPlayerDamage(u,u2,chargedExplosion);
                                        DamageHandler.spawnDamageIndicator(p,damageData,p2.getLocation());
                                        u2.damage(damageData.getDamage(),p);
                                    }
                                }
                            }
                        }
                    }

                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            u.ignoreDamageCheck = false;
                            u.ignoreFistCheck = false;
                        }
                    }.runTaskLater(DungeonRPG.getInstance(),1);
                }
            };

            u.getSkillValues().lightningStrikeCount = 0;
            if(u.getSkillValues().lightningStrikeTask != null) u.getSkillValues().lightningStrikeTask.cancel();
            u.getSkillValues().lightningStrikeTask = new BukkitRunnable(){
                @Override
                public void run() {
                    if(u.getSkillValues().lightningStrikeCount == locations.size()){
                        f.runTaskLater(DungeonRPG.getInstance(),2);
                        cancel();
                        u.getSkillValues().lightningStrikeCount = 0;
                        u.getSkillValues().lightningStrikeTask = null;
                    } else {
                        Location particleLocation = locations.get(u.getSkillValues().lightningStrikeCount);
                        //ParticleEffect.SMOKE_LARGE.display(0f,0f,0f,0f,1,particleLocation,600);
                        ParticleEffect.BLOCK_CRACK.display(new ParticleEffect.BlockData(Material.COAL_BLOCK, (byte)0), 0f, 0f, 0f, 0f, 20, particleLocation, 600);
                        u.getSkillValues().lightningStrikeCount++;
                    }
                }
            }.runTaskTimer(DungeonRPG.getInstance(),2,2);
        } catch(IllegalStateException ex){
            p.sendMessage(ChatColor.RED + "Skill failed! Are you standing too close to something?");
        }
    }
}
