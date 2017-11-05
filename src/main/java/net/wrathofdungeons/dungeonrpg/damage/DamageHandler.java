package net.wrathofdungeons.dungeonrpg.damage;

import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.StatPointType;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.items.ItemCategory;
import net.wrathofdungeons.dungeonrpg.items.awakening.AwakeningType;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.mobs.MobData;
import net.wrathofdungeons.dungeonrpg.mobs.skills.MobSkill;
import net.wrathofdungeons.dungeonrpg.skill.Skill;
import net.wrathofdungeons.dungeonrpg.skill.archer.DartRain;
import net.wrathofdungeons.dungeonrpg.skill.archer.ExplosionArrow;
import net.wrathofdungeons.dungeonrpg.skill.magician.FlameBurst;
import net.wrathofdungeons.dungeonrpg.skill.mercenary.AxeBlast;
import net.wrathofdungeons.dungeonrpg.skill.mercenary.Shockwave;
import net.wrathofdungeons.dungeonrpg.skill.mercenary.Stomper;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class DamageHandler {
    public static double calculateMobToPlayerDamage(GameUser u, CustomEntity c){
        return calculateMobToPlayerDamage(u,c,null);
    }

    public static double calculateMobToPlayerDamage(GameUser u, CustomEntity c, MobSkill skill){
        Player p = u.getPlayer();
        LivingEntity e = c.getBukkitEntity();
        double damage = 1;

        MobData mob = c.getData();

        damage += mob.getAtk();

        if(skill instanceof net.wrathofdungeons.dungeonrpg.mobs.skills.Shockwave){
            damage *= 1.35;
        }

        for(CustomItem i : u.getCurrentCharacter().getEquipment()){
            if(i.getData().getCategory().equals(ItemCategory.ARMOR)){
                damage -= Util.randomDouble(i.getModifiedDefMin(), i.getModifiedDefMax());
            }
        }

        int defense = u.getCurrentCharacter().getTotalValue(AwakeningType.DEFENSE);

        if(defense > 0){
            damage -= damage*(defense*0.01);
        } else if(defense < 0) {
            damage += damage*((defense/-1)*0.01);
        }

        if(damage <= 0) damage = 1;

        if(Util.getChanceBoolean(u.getCurrentCharacter().getTotalDodgingChance(), 100)){
            damage = 0;
        }

        return damage;
    }

    public static double calculatePlayerToMobDamage(GameUser u, CustomEntity c, Skill skill){
        Player p = u.getPlayer();
        LivingEntity e = c.getBukkitEntity();
        double damage = 1;

        if(p.getItemInHand() != null && CustomItem.fromItemStack(p.getItemInHand()) != null){ // Player has weapon in hand
            CustomItem item = CustomItem.fromItemStack(p.getItemInHand());

            if(item.getData().getCategory() == u.getCurrentCharacter().getRpgClass().getWeapon()){
                damage += Util.randomDouble(item.getModifiedAtkMin(), item.getModifiedAtkMax());
            }

            damage = damage+damage*(u.getCurrentCharacter().getStatpointsTotal(StatPointType.STRENGTH)*0.1);
            damage = damage+damage*(u.getCurrentCharacter().getTotalValue(AwakeningType.ATTACK_DAMAGE)*0.01);

            if(skill == null){
                damage = damage+damage*(u.getCurrentCharacter().getTotalValue(AwakeningType.MELEE_DAMAGE)*0.01);
            } else {
                damage = damage+damage*(u.getCurrentCharacter().getTotalValue(AwakeningType.SKILL_DAMAGE)*0.01);

                if(skill instanceof DartRain){
                    damage /= 2;
                } else if(skill instanceof ExplosionArrow){
                    damage *= 3.5;
                } else if(skill instanceof Stomper){
                    damage *= 3;
                } else if(skill instanceof LightningStrike){
                    damage *= 2.5;
                } else if(skill instanceof Shockwave){
                    damage *= 3.5;
                } else if(skill instanceof FlameBurst){
                    damage *= 1.5;
                } else if(skill instanceof AxeBlast){
                    damage *= 2.5;
                }
            }

            if(damage <= 0) damage = 1;
        } else {
            damage = 0;
        }

        // CRITICAL
        if(Util.getChanceBoolean(u.getCurrentCharacter().getTotalCriticalHitChance(), 100)){
            damage *= 2;
            damage += damage*(u.getCurrentCharacter().getTotalValue(AwakeningType.ADOCH)*0.01);
        }

        return damage;
    }

    public static double calculatePlayerToPlayerDamage(GameUser u, GameUser u2, Skill skill){
        Player p = u.getPlayer();
        Player p2 = u2.getPlayer();
        double damage = 1;

        if(p.getItemInHand() != null && CustomItem.fromItemStack(p.getItemInHand()) != null){ // Player has weapon in hand
            CustomItem item = CustomItem.fromItemStack(p.getItemInHand());

            if(item.getData().getCategory() == u.getCurrentCharacter().getRpgClass().getWeapon()){
                damage += Util.randomDouble(item.getModifiedAtkMin(), item.getModifiedAtkMax());
            }

            damage = damage+damage*(u.getCurrentCharacter().getStatpointsTotal(StatPointType.STRENGTH)*0.1);
            damage = damage+damage*(u.getCurrentCharacter().getTotalValue(AwakeningType.ATTACK_DAMAGE)*0.01);

            if(skill == null){
                damage = damage+damage*(u.getCurrentCharacter().getTotalValue(AwakeningType.MELEE_DAMAGE)*0.01);
            } else {
                damage = damage+damage*(u.getCurrentCharacter().getTotalValue(AwakeningType.SKILL_DAMAGE)*0.01);

                if(skill instanceof DartRain){
                    damage /= 2;
                } else if(skill instanceof ExplosionArrow){
                    damage *= 3.5;
                } else if(skill instanceof Stomper){
                    damage *= 3;
                } else if(skill instanceof LightningStrike){
                    damage *= 2.5;
                } else if(skill instanceof Shockwave){
                    damage *= 3.5;
                } else if(skill instanceof FlameBurst){
                    damage *= 1.5;
                } else if(skill instanceof AxeBlast){
                    damage *= 2.5;
                }
            }

            if(damage <= 0) damage = 1;
        } else {
            damage = 0;
        }

        // CRITICAL
        if(Util.getChanceBoolean(u.getCurrentCharacter().getTotalCriticalHitChance(), 100)){
            damage *= 2;
            damage += damage*(u.getCurrentCharacter().getTotalValue(AwakeningType.ADOCH)*0.01);
        }

        for(CustomItem i : u.getCurrentCharacter().getEquipment()){
            if(i.getData().getCategory().equals(ItemCategory.ARMOR)){
                damage -= Util.randomDouble(i.getModifiedDefMin(), i.getModifiedDefMax());
            }
        }

        int defense = u.getCurrentCharacter().getTotalValue(AwakeningType.DEFENSE);

        if(defense > 0){
            damage -= damage*(defense*0.01);
        } else if(defense < 0) {
            damage += damage*((defense/-1)*0.01);
        }

        if(damage <= 0) damage = 1;

        if(Util.getChanceBoolean(u.getCurrentCharacter().getTotalDodgingChance(), 100)){
            damage = 0;
        }

        if(damage < 1) damage = 1;

        return damage;
    }
}
