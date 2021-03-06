package net.wrathofdungeons.dungeonrpg.damage;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import net.wrathofdungeons.dungeonapi.util.ChatIcons;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.StatPointType;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.items.ItemCategory;
import net.wrathofdungeons.dungeonrpg.items.awakening.AwakeningType;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.mobs.MobData;
import net.wrathofdungeons.dungeonrpg.mobs.skills.MobSkill;
import net.wrathofdungeons.dungeonrpg.skill.Skill;
import net.wrathofdungeons.dungeonrpg.skill.SkillStorage;
import net.wrathofdungeons.dungeonrpg.skill.archer.DartRain;
import net.wrathofdungeons.dungeonrpg.skill.archer.ExplosionArrow;
import net.wrathofdungeons.dungeonrpg.skill.archer.PoisonArrow;
import net.wrathofdungeons.dungeonrpg.skill.assassin.StabbingStorm;
import net.wrathofdungeons.dungeonrpg.skill.assassin.WhirlingBlade;
import net.wrathofdungeons.dungeonrpg.skill.magician.ChargedExplosion;
import net.wrathofdungeons.dungeonrpg.skill.magician.FlameBurst;
import net.wrathofdungeons.dungeonrpg.skill.mercenary.AxeBlast;
import net.wrathofdungeons.dungeonrpg.skill.mercenary.Shockwave;
import net.wrathofdungeons.dungeonrpg.skill.mercenary.Stomper;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class DamageHandler {
    public static DamageData calculateMobToPlayerDamage(GameUser u, CustomEntity c){
        return calculateMobToPlayerDamage(u,c,null);
    }

    public static DamageData calculateMobToPlayerDamage(GameUser u, CustomEntity c, MobSkill skill){
        Player p = u.getPlayer();
        LivingEntity e = c.getBukkitEntity();
        DamageData damage = new DamageData();
        damage.setDamage(1);

        MobData mob = c.getData();

        damage.setDamage(mob.getAtk());

        if(skill instanceof net.wrathofdungeons.dungeonrpg.mobs.skills.Shockwave){
            damage.setDamage(damage.getDamage()*1.35);
        }

        for(CustomItem i : u.getCurrentCharacter().getEquipment()){
            if(i.getData().getCategory().equals(ItemCategory.ARMOR)){
                damage.setDamage(damage.getDamage()-Util.randomDouble(i.getModifiedDefMin(), i.getModifiedDefMax()));
            }
        }

        int defense = u.getCurrentCharacter().getTotalValue(AwakeningType.DEFENSE);

        if(defense > 0){
            damage.setDamage(damage.getDamage()-damage.getDamage()*(defense*0.01));
        } else if(defense < 0) {
            damage.setDamage(damage.getDamage()+damage.getDamage()*((defense/-1)*0.01));
        }

        if(damage.getDamage() <= 0) damage.setDamage(1);

        if(Util.getChanceBoolean(u.getCurrentCharacter().getTotalDodgingChance(), 100)){
            damage.setDamage(0);
            damage.setDodged(true);
        }

        return damage;
    }

    public static DamageData calculatePlayerToMobDamage(GameUser u, CustomEntity c, Skill skill){
        return calculatePlayerToMobDamage(u,c,skill != null ? new SkillData(skill,u.getCurrentCharacter().getVariables().getInvestedSkillPoints(skill)) : null);
    }

    public static DamageData calculatePlayerToMobDamage(GameUser u, CustomEntity c, SkillData skillData){
        Player p = u.getPlayer();
        LivingEntity e = c.getBukkitEntity();
        DamageData damage = new DamageData();
        damage.setDamage(1);

        Skill skill = null;
        int investedSkillPoints = 0;

        if(skillData != null){
            skill = skillData.skill;
            investedSkillPoints = skillData.investedSkillPoints;
        }

        if(p.getItemInHand() != null && CustomItem.fromItemStack(p.getItemInHand()) != null){ // Player has weapon in hand
            CustomItem item = CustomItem.fromItemStack(p.getItemInHand());

            if(item.getData().getCategory() == u.getCurrentCharacter().getRpgClass().getWeapon()){
                damage.setDamage(damage.getDamage()+Util.randomDouble(item.getModifiedAtkMin(), item.getModifiedAtkMax()));
            }

            damage.setDamage(damage.getDamage()+damage.getDamage()*(u.getCurrentCharacter().getStatpointsTotal(StatPointType.STRENGTH)*0.1));
            damage.setDamage(damage.getDamage()+damage.getDamage()*(u.getCurrentCharacter().getTotalValue(AwakeningType.ATTACK_DAMAGE)*0.01));

            if(skill == null){
                damage.setDamage(damage.getDamage()+damage.getDamage()*(u.getCurrentCharacter().getTotalValue(AwakeningType.MELEE_DAMAGE)*0.01));
            } else {
                damage.setDamage(damage.getDamage()+damage.getDamage()*(u.getCurrentCharacter().getTotalValue(AwakeningType.SKILL_DAMAGE)*0.01));

                if(skill instanceof DartRain){
                    damage.setDamage(damage.getDamage()/2);
                } else if(skill instanceof ExplosionArrow){
                    damage.setDamage(damage.getDamage()*3.5);
                } else if(skill instanceof Stomper){
                    damage.setDamage(damage.getDamage()*3);
                } else if(skill instanceof ChargedExplosion){
                    damage.setDamage(damage.getDamage()*2.5);
                } else if(skill instanceof Shockwave){
                    damage.setDamage(damage.getDamage()*3.5);
                } else if(skill instanceof FlameBurst){
                    damage.setDamage(damage.getDamage()*1.5);
                } else if(skill instanceof AxeBlast){
                    damage.setDamage(damage.getDamage()*2.5);
                } else if(skill instanceof StabbingStorm){
                    damage.setDamage((damage.getDamage()*2.75)*Double.parseDouble(skill.getEffects(investedSkillPoints).get("Damage").replace("x","")));
                } else if(skill instanceof PoisonArrow){
                    damage.setDamage((damage.getDamage()/2.5)*Double.parseDouble(skill.getEffects(investedSkillPoints).get("Damage").replace("x","")));
                } else if(skill instanceof WhirlingBlade){
                    damage.setDamage((damage.getDamage()*2.95)*Double.parseDouble(skill.getEffects(investedSkillPoints).get("Damage").replace("x","")));
                }
            }

            if(damage.getDamage() <= 0) damage.setDamage(1);
        } else {
            damage.setDamage(0);
        }

        // CRITICAL
        if(Util.getChanceBoolean(u.getCurrentCharacter().getTotalCriticalHitChance(), 100)){
            damage.setDamage(damage.getDamage()*2);
            damage.setDamage(damage.getDamage()+damage.getDamage()*(u.getCurrentCharacter().getTotalValue(AwakeningType.ADOCH)*0.01));
            damage.setCritical(true);
        }

        return damage;
    }

    public static DamageData calculatePlayerToPlayerDamage(GameUser u, GameUser u2, Skill skill){
        return calculatePlayerToPlayerDamage(u,u2,skill != null ? new SkillData(skill,u.getCurrentCharacter().getVariables().getInvestedSkillPoints(skill)) : null);
    }

    public static DamageData calculatePlayerToPlayerDamage(GameUser u, GameUser u2, SkillData skillData){
        Player p = u.getPlayer();
        Player p2 = u2.getPlayer();
        DamageData damage = new DamageData();
        damage.setDamage(1);

        Skill skill = null;
        int investedSkillPoints = 0;

        if(skillData != null){
            skill = skillData.skill;
            investedSkillPoints = skillData.investedSkillPoints;
        }

        if(p.getItemInHand() != null && CustomItem.fromItemStack(p.getItemInHand()) != null){ // Player has weapon in hand
            CustomItem item = CustomItem.fromItemStack(p.getItemInHand());

            if(item.getData().getCategory() == u.getCurrentCharacter().getRpgClass().getWeapon()){
                damage.setDamage(damage.getDamage()+Util.randomDouble(item.getModifiedAtkMin(), item.getModifiedAtkMax()));
            }

            damage.setDamage(damage.getDamage()+damage.getDamage()*(u.getCurrentCharacter().getStatpointsTotal(StatPointType.STRENGTH)*0.1));
            damage.setDamage(damage.getDamage()+damage.getDamage()*(u.getCurrentCharacter().getTotalValue(AwakeningType.ATTACK_DAMAGE)*0.01));

            if(skill == null){
                damage.setDamage(damage.getDamage()+damage.getDamage()*(u.getCurrentCharacter().getTotalValue(AwakeningType.MELEE_DAMAGE)*0.01));
            } else {
                damage.setDamage(damage.getDamage()+damage.getDamage()*(u.getCurrentCharacter().getTotalValue(AwakeningType.SKILL_DAMAGE)*0.01));

                if(skill instanceof DartRain){
                    damage.setDamage(damage.getDamage()/2);
                } else if(skill instanceof ExplosionArrow){
                    damage.setDamage(damage.getDamage()*3.5);
                } else if(skill instanceof Stomper){
                    damage.setDamage(damage.getDamage()*3);
                } else if(skill instanceof ChargedExplosion){
                    damage.setDamage(damage.getDamage()*2.5);
                } else if(skill instanceof Shockwave){
                    damage.setDamage(damage.getDamage()*3.5);
                } else if(skill instanceof FlameBurst){
                    damage.setDamage(damage.getDamage()*1.5);
                } else if(skill instanceof AxeBlast){
                    damage.setDamage(damage.getDamage()*2.5);
                } else if(skill instanceof StabbingStorm){
                    damage.setDamage((damage.getDamage()*2.75)*Double.parseDouble(skill.getEffects(investedSkillPoints).get("Damage").replace("x","")));
                } else if(skill instanceof PoisonArrow){
                    damage.setDamage((damage.getDamage()/2.5)*Double.parseDouble(skill.getEffects(investedSkillPoints).get("Damage").replace("x","")));
                } else if(skill instanceof WhirlingBlade){
                    damage.setDamage((damage.getDamage()*2.95)*Double.parseDouble(skill.getEffects(investedSkillPoints).get("Damage").replace("x","")));
                }
            }

            if(damage.getDamage() <= 0) damage.setDamage(1);
        } else {
            damage.setDamage(0);
        }

        // CRITICAL
        if(Util.getChanceBoolean(u.getCurrentCharacter().getTotalCriticalHitChance(), 100)){
            damage.setDamage(damage.getDamage()*2);
            damage.setDamage(damage.getDamage()+damage.getDamage()*(u.getCurrentCharacter().getTotalValue(AwakeningType.ADOCH)*0.01));
            damage.setCritical(true);
        }

        for(CustomItem i : u2.getCurrentCharacter().getEquipment()){
            if(i.getData().getCategory().equals(ItemCategory.ARMOR)){
                damage.setDamage(damage.getDamage()-Util.randomDouble(i.getModifiedDefMin(), i.getModifiedDefMax()));
            }
        }

        int defense = u2.getCurrentCharacter().getTotalValue(AwakeningType.DEFENSE);

        if(defense > 0){
            damage.setDamage(damage.getDamage()-damage.getDamage()*(defense*0.01));
        } else if(defense < 0) {
            damage.setDamage(damage.getDamage()+damage.getDamage()*((defense/-1)*0.01));
        }

        if(damage.getDamage() <= 0) damage.setDamage(1);

        if(Util.getChanceBoolean(u.getCurrentCharacter().getTotalDodgingChance(), 100)){
            damage.setDamage(0);
            damage.setDodged(true);
        }

        if(damage.getDamage() < 1) damage.setDamage(damage.getDamage());

        return damage;
    }

    public static void spawnDamageIndicator(Player p, DamageData damageData, Location loc){
        spawnDamageIndicatorExact(p,damageData,loc.clone().add(Util.randomInteger(-1,1),Util.randomInteger(1,3),Util.randomInteger(-1,1)));
    }

    public static void spawnDamageIndicatorExact(Player p, DamageData damageData, Location loc){
        if(GameUser.isLoaded(p)){
            GameUser u = GameUser.getUser(p);

            if(u.getCurrentCharacter() != null){
                if(u.getSettingsManager().mayShowDamageIndicators()){
                    Hologram holo = HologramsAPI.createHologram(DungeonRPG.getInstance(),loc);

                    if(damageData.isDodged()){
                        holo.appendTextLine(ChatColor.DARK_AQUA.toString() + ChatColor.BOLD.toString() + "MISSED!");
                    } else if(damageData.isBlocked()){
                        holo.appendTextLine(ChatColor.DARK_GREEN.toString() + ChatColor.BOLD.toString() + "BLOCKED!");
                    } else {
                        if(damageData.isCritical()) holo.appendTextLine(ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + "CRITICAL!");

                        holo.appendTextLine(ChatColor.RED.toString() + ChatColor.BOLD + "-" + ((Double)damageData.getDamage()).intValue() + " " + ChatIcons.HEART);
                    }

                    holo.getVisibilityManager().setVisibleByDefault(false);
                    holo.getVisibilityManager().showTo(p);

                    /*new BukkitRunnable(){
                        @Override
                        public void run() {
                            if(!holo.isDeleted()) holo.delete();
                        }
                    }.runTaskLater(DungeonRPG.getInstance(),20);*/

                    final int max = 25;
                    for(int i = 0; i <= max; i++){
                        final int ii = i;

                        new BukkitRunnable(){
                            @Override
                            public void run() {
                                if(ii == max){
                                    if(!holo.isDeleted()) holo.delete();
                                } else if(ii < max){
                                    if(!holo.isDeleted()) holo.teleport(holo.getLocation().clone().add(0,0.1,0));
                                }
                            }
                        }.runTaskLater(DungeonRPG.getInstance(),i);
                    }
                }
            }
        }
    }
}
