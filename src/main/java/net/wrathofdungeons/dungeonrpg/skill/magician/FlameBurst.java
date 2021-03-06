package net.wrathofdungeons.dungeonrpg.skill.magician;

import net.wrathofdungeons.dungeonapi.util.ParticleEffect;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.damage.DamageData;
import net.wrathofdungeons.dungeonrpg.damage.DamageHandler;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.skill.Skill;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;

public class FlameBurst implements Skill {
    @Override
    public String getName() {
        return "Flame Burst";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public HashMap<String, String> getEffects(int investedSkillPoints) {
        HashMap<String,String> effects = new HashMap<String, String>();

        int range = 10;

        if(investedSkillPoints == 2){
            range = 12;
        } else if(investedSkillPoints == 3){
            range = 14;
        } else if(investedSkillPoints == 4){
            range = 16;
        } else if(investedSkillPoints == 5){
            range = 18;
        }

        effects.put("Range",String.valueOf(range));

        return effects;
    }

    @Override
    public int getIcon() {
        return 377;
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
        int investedSkillPoints = u.getCurrentCharacter().getVariables().getInvestedSkillPoints(this);

        final int range = Integer.parseInt(getEffects(investedSkillPoints).get("Range"));

        ArrayList<Entity> entities = new ArrayList<Entity>();
        ArrayList<Location> locations = new ArrayList<Location>();

        Location loc = p.getEyeLocation();
        BlockIterator blocksToAdd = new BlockIterator(loc, 0D, range);
        Location lastLoc = null;
        while (blocksToAdd.hasNext()) {
            lastLoc = blocksToAdd.next().getLocation();
        }
        int c = (int) Math.ceil(loc.distance(lastLoc) / 2F) - 1;
        if (c > 0) {
            Vector v = lastLoc.toVector().subtract(loc.toVector()).normalize().multiply(2F);
            Location l = loc.clone();
            for (int i = 0; i < c; i++) {
                l.add(v);
                locations.add(l.clone());
            }
        }

        FlameBurst flameBurst = this;

        int i = 0;
        for(Location t : locations){
            u.getSkillValues().skillTasks.add(new BukkitRunnable(){
                @Override
                public void run() {
                    u.ignoreDamageCheck = true;
                    u.ignoreFistCheck = true;

                    ParticleEffect.FLAME.display(0.5f,0.5f,0.5f,0.05f,15,t,600);
                    ParticleEffect.SMOKE_NORMAL.display(0.5f,0.5f,0.05f,0.05f,4,t,600);
                    t.getWorld().playSound(t, Sound.BLOCK_FIRE_AMBIENT,1f,1f);

                    for(Entity entity : t.getWorld().getNearbyEntities(t,1,1,1)){
                        if(!entities.contains(entity)){
                            if(entity instanceof LivingEntity){
                                LivingEntity livingEntity = (LivingEntity)entity;
                                CustomEntity c = CustomEntity.fromEntity(livingEntity);

                                if(c != null){
                                    entities.add(livingEntity);
                                    DungeonRPG.showBloodEffect(livingEntity.getLocation());
                                    c.getData().playSound(livingEntity.getLocation());
                                    c.giveNormalKnockback(loc, true);
                                    DamageData damageData = DamageHandler.calculatePlayerToMobDamage(u,c,flameBurst);
                                    DamageHandler.spawnDamageIndicator(p,damageData,c.getBukkitEntity().getLocation());
                                    c.damage(damageData.getDamage(),p);
                                    livingEntity.setFireTicks(4*20);
                                } else {
                                    if(livingEntity instanceof Player){
                                        Player p2 = (Player)livingEntity;

                                        if(GameUser.isLoaded(p2)){
                                            GameUser u2 = GameUser.getUser(p2);

                                            if(DungeonRPG.mayAttack(p,p2)){
                                                entities.add(livingEntity);
                                                DungeonRPG.showBloodEffect(livingEntity.getLocation());
                                                u2.giveNormalKnockback(loc, true);
                                                DamageData damageData = DamageHandler.calculatePlayerToPlayerDamage(u,u2,flameBurst);
                                                DamageHandler.spawnDamageIndicator(p,damageData,p2.getLocation());
                                                u2.damage(damageData.getDamage(),p);
                                                livingEntity.setFireTicks(4*20);
                                            }
                                        }
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
            }.runTaskLater(DungeonRPG.getInstance(),i*2));

            i++;
        }
    }
}
