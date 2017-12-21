package net.wrathofdungeons.dungeonrpg.skill.assassin;

import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.util.ParticleEffect;
import net.wrathofdungeons.dungeonrpg.Duel;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.damage.DamageData;
import net.wrathofdungeons.dungeonrpg.damage.DamageHandler;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
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

public class StabbingStorm implements Skill {
    @Override
    public String getName() {
        return "Stabbing Storm";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public HashMap<String, String> getEffects(int investedSkillPoints) {
        HashMap<String,String> effects = new HashMap<String, String>();

        int totalAttacks = 1;
        double attackSpeed = 1;
        double damage = 1;

        if(investedSkillPoints == 1){
            totalAttacks = 3;
            attackSpeed = 1;
            damage = 1;
        } else if(investedSkillPoints == 2){
            totalAttacks = 6;
            attackSpeed = 1.15;
            damage = 1.2;
        } else if(investedSkillPoints == 3){
            totalAttacks = 9;
            attackSpeed = 1.3;
            damage = 1.4;
        } else if(investedSkillPoints == 4){
            totalAttacks = 12;
            attackSpeed = 1.45;
            damage = 1.4;
        } else if(investedSkillPoints == 5){
            totalAttacks = 15;
            attackSpeed = 1.8;
            damage = 2.1;
        }

        effects.put("Total Attacks",String.valueOf(totalAttacks));
        effects.put("Attack Speed",String.valueOf(attackSpeed) + "x");
        effects.put("Damage",String.valueOf(damage) + "x");

        return effects;
    }

    @Override
    public int getIcon() {
        return 359;
    }

    @Override
    public int getIconDurability() {
        return 0;
    }

    @Override
    public RPGClass getRPGClass() {
        return RPGClass.ASSASSIN;
    }

    @Override
    public int getMinLevel() {
        return 1;
    }

    @Override
    public int getMaxInvestingPoints() {
        return 5;
    }

    @Override
    public int getBaseMPCost() {
        return 3;
    }

    @Override
    public void execute(Player p) {
        GameUser u = GameUser.getUser(p);
        CustomItem weapon = CustomItem.fromItemStack(p.getItemInHand());

        int investedSkillPoints = u.getCurrentCharacter().getVariables().getInvestedSkillPoints(this);
        Location loc = p.getEyeLocation();
        int totalAttacks = Integer.parseInt(getEffects(investedSkillPoints).get("Total Attacks"));
        double attackSpeed = Double.parseDouble(getEffects(investedSkillPoints).get("Attack Speed").replace("x",""));
        double damage = Double.parseDouble(getEffects(investedSkillPoints).get("Damage").replace("x",""));

        final int explodingRange = 4;
        final StabbingStorm stabbingStorm = this;
        int rangeDifference = 2;
        final int baseRangeDifference = 3;

        try {
            BlockIterator bi = new BlockIterator(loc, 0.0, baseRangeDifference+(totalAttacks*rangeDifference));

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

            if(totalAttacks > locations.size()) totalAttacks = locations.size();
            final Location playerLoc = loc;

            for(int casts = 0; casts < totalAttacks; casts++){
                final int castNumber = casts;
                u.getSkillValues().skillTasks.add(new BukkitRunnable(){
                    @Override
                    public void run() {
                        Location castLocation = locations.get(castNumber);
                        Location particleLocation = castLocation.clone().add(0,0.1,0);

                        if(investedSkillPoints == 1 || investedSkillPoints == 1 || investedSkillPoints == 3){
                            ParticleEffect.BLOCK_CRACK.display(new ParticleEffect.BlockData(Material.GOLD_BLOCK, (byte)0), 0f, 0f, 0f, 0f, 10, particleLocation, 600);
                            particleLocation.getWorld().playSound(particleLocation,Sound.BLOCK_GLASS_BREAK,1f,1f);
                        } else if(investedSkillPoints == 4){
                            ParticleEffect.BLOCK_CRACK.display(new ParticleEffect.BlockData(Material.GOLD_BLOCK, (byte)0), 0.05f, 0.05f, 0.05f, 0f, 20, particleLocation, 600);
                            particleLocation.getWorld().playSound(particleLocation,Sound.BLOCK_GLASS_BREAK,1f,1f);
                        } else if(investedSkillPoints == 5){
                            ParticleEffect.BLOCK_CRACK.display(new ParticleEffect.BlockData(Material.GOLD_BLOCK, (byte)0), 0.05f, 0.05f, 0.05f, 0f, 20, particleLocation, 600);
                            ParticleEffect.BLOCK_CRACK.display(new ParticleEffect.BlockData(Material.IRON_BLOCK, (byte)0), 0.05f, 0.05f, 0.05f, 0f, 20, particleLocation, 600);
                            particleLocation.getWorld().playSound(particleLocation,Sound.BLOCK_GLASS_BREAK,1f,1f);
                        }

                        for(Entity entity : castLocation.getWorld().getNearbyEntities(castLocation,explodingRange,explodingRange,explodingRange)){
                            if(entity instanceof LivingEntity){
                                LivingEntity livingEntity = (LivingEntity)entity;
                                CustomEntity c = CustomEntity.fromEntity(livingEntity);

                                if(c != null){
                                    DungeonRPG.showBloodEffect(c.getBukkitEntity().getLocation());
                                    c.getData().playSound(c.getBukkitEntity().getLocation());
                                    DamageData damageData = DamageHandler.calculatePlayerToMobDamage(u,c,stabbingStorm);
                                    DamageHandler.spawnDamageIndicator(p,damageData,c.getBukkitEntity().getLocation());
                                    c.damage(damageData.getDamage(),p);
                                    c.giveNormalKnockback(playerLoc);
                                } else {
                                    if(livingEntity instanceof Player){
                                        Player p2 = (Player)livingEntity;

                                        if(GameUser.isLoaded(p2)){
                                            GameUser u2 = GameUser.getUser(p2);

                                            if(Duel.isDuelingWith(p,p2)){
                                                DungeonRPG.showBloodEffect(entity.getLocation());
                                                DamageData damageData = DamageHandler.calculatePlayerToPlayerDamage(u,u2,stabbingStorm);
                                                DamageHandler.spawnDamageIndicator(p,damageData,p2.getLocation());
                                                u2.damage(damageData.getDamage(),p);
                                                u2.giveNormalKnockback(playerLoc);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }.runTaskLater(DungeonRPG.getInstance(),casts*2));
            }
        } catch(Exception e){
            p.sendMessage(ChatColor.RED + "Skill failed! Are you standing too close to something?");
        }
    }
}
