package net.wrathofdungeons.dungeonrpg.damage;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import net.wrathofdungeons.dungeonapi.util.ChatIcons;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.items.ItemCategory;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.mobs.MobData;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class DamageManager {
    public static Hologram spawnDamageIndicator(Location loc, double damage){
        return spawnDamageIndicator(loc, damage, true, false, false);
    }

    public static Hologram spawnDamageIndicator(Location loc, double damage, boolean isPositive){
        return spawnDamageIndicator(loc, damage, isPositive, false, false);
    }

    public static Hologram spawnDamageIndicator(Location loc, double damage, boolean isPositive, boolean missed, boolean critical){
        final Hologram hologram = HologramsAPI.createHologram(DungeonRPG.getInstance(), loc.clone().add(Util.randomInteger(-2, 2), 2, Util.randomInteger(-2, 2)));

        if(missed){
            hologram.appendTextLine(ChatColor.DARK_AQUA.toString() + ChatColor.BOLD.toString() + "MISS!");
        } else {
            if(critical){
                hologram.appendTextLine(ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + "CRITICAL!");
            }

            if(isPositive){
                hologram.appendTextLine(ChatColor.AQUA + "-" + ((Double)damage).intValue() + " " + ChatIcons.HEART);
            } else {
                hologram.appendTextLine(ChatColor.RED + "-" + ((Double)damage).intValue() + " " + ChatIcons.HEART);
            }
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRPG.getInstance(), new Runnable(){
            public void run(){
                hologram.delete();
            }
        }, 10);

        return hologram;
    }

    public static double calculateDamage(Player p, LivingEntity enemy, DamageSource source, boolean gotAttacked, boolean isSkill){
        return calculateDamage(p, enemy, source, gotAttacked, isSkill, 0, true, 1.0);
    }

    public static double calculateDamage(Player p, LivingEntity enemy, DamageSource source, boolean gotAttacked, boolean isSkill, double baseDamage){
        return calculateDamage(p, enemy, source, gotAttacked, isSkill, baseDamage, true, 1.0);
    }

    public static double calculateDamage(Player p, LivingEntity enemy, DamageSource source, boolean gotAttacked, boolean isSkill, double baseDamage, boolean spawnDamageIndicator){
        return calculateDamage(p, enemy, source, gotAttacked, isSkill, baseDamage, spawnDamageIndicator, 1.0);
    }

    public static double calculateDamage(Player p, LivingEntity enemy, DamageSource source, boolean gotAttacked, boolean isSkill, double baseDamage, boolean spawnDamageIndicator, double force){
        if(source == DamageSource.NATURAL) return 0;

        GameUser u = GameUser.getUser(p);
        double damage = 0;

        boolean missed = false;
        boolean isPositive = true;
        boolean critical = false;

        if(baseDamage > 0){
            damage = baseDamage;
        }

        int str = 0;
        int sta = 0;
        int intel = 0;
        int dex = 0;
        int agi = 0;

        /*str += u.currentCharacter.getAttributeValue(AttributeType.STRENGTH);
        sta += u.currentCharacter.getAttributeValue(AttributeType.STAMINA);
        intel += u.currentCharacter.getAttributeValue(AttributeType.INTELLIGENCE);
        dex += u.currentCharacter.getAttributeValue(AttributeType.DEXTERITY);
        agi += u.currentCharacter.getAttributeValue(AttributeType.AGILITY);

        str += u.currentCharacter.getArtificialAttributeValue(AttributeType.STRENGTH);
        sta += u.currentCharacter.getArtificialAttributeValue(AttributeType.STAMINA);
        intel += u.currentCharacter.getArtificialAttributeValue(AttributeType.INTELLIGENCE);
        dex += u.currentCharacter.getArtificialAttributeValue(AttributeType.DEXTERITY);
        agi += u.currentCharacter.getArtificialAttributeValue(AttributeType.AGILITY);*/

        if(gotAttacked){
            if(source == DamageSource.PVP){
                spawnDamageIndicator = false;

                Player p2 = (Player)enemy;
                GameUser u2 = GameUser.getUser(p2);

                double dmg1 = calculateDamage(p2, p, DamageSource.PVP, false, isSkill);

                damage += dmg1;

                if((sta + "").startsWith("-")){
                    for (int i = 0; i > sta; i--) {
                        damage += 0.125;
                    }
                } else {
                    for (int i = 0; i < sta; i++) {
                        damage -= 0.125;
                    }
                }

                for(CustomItem i : u.getCurrentCharacter().getEquipment(p)){
                    if(i.getData().getCategory().equals(ItemCategory.ARMOR)){
                        damage -= Util.randomDouble(i.getData().getDefMin(), i.getData().getDefMax());
                    }
                }

                if(damage <= 0) damage = 1;
            } else {
                if(CustomEntity.fromEntity(enemy) != null){
                    CustomEntity ce = CustomEntity.fromEntity(enemy);
                    MobData mob = ce.getData();

                    damage += mob.getAtk();

                    if((sta + "").startsWith("-")){
                        for (int i = 0; i > sta; i--) {
                            damage += 0.125;
                        }
                    } else {
                        for (int i = 0; i < sta; i++) {
                            damage -= 0.125;
                        }
                    }

                    for(CustomItem i : u.getCurrentCharacter().getEquipment(p)){
                        if(i.getData().getCategory().equals(ItemCategory.ARMOR)){
                            damage -= Util.randomDouble(i.getData().getDefMin(), i.getData().getDefMax());
                        }
                    }

                    if(damage <= 0) damage = 1;

                    isPositive = false;
                }

                // DODGE
                if(Util.getChanceBoolean(agi, 100)){
                    damage = 0;
                    missed = true;
                }
            }

            if(u.getCurrentCharacter().getRpgClass() == RPGClass.ARCHER || u.getCurrentCharacter().getRpgClass() == RPGClass.HUNTER || u.getCurrentCharacter().getRpgClass() == RPGClass.RANGER){
                // 60% DEF
                damage = damage*1.4;
            } else if(u.getCurrentCharacter().getRpgClass() == RPGClass.MERCENARY || u.getCurrentCharacter().getRpgClass() == RPGClass.KNIGHT || u.getCurrentCharacter().getRpgClass() == RPGClass.SOLDIER){
                // 120% DEF
                damage = damage*0.8;
            } else if(u.getCurrentCharacter().getRpgClass() == RPGClass.MAGICIAN || u.getCurrentCharacter().getRpgClass() == RPGClass.ALCHEMIST || u.getCurrentCharacter().getRpgClass() == RPGClass.WIZARD){
                // 80% DEF
                damage = damage*1.2;
            }

            // not mentioning Assassin (100% def, would not change)
        } else {
            if(p.getItemInHand() != null && CustomItem.fromItemStack(p.getItemInHand()) != null){ // Player has weapon in hand
                CustomItem item = CustomItem.fromItemStack(p.getItemInHand());

                if(item.getData().getCategory() == u.getCurrentCharacter().getRpgClass().getWeapon()){
                    damage += Util.randomDouble(item.getData().getAtkMin(), item.getData().getAtkMax());
                }

                if(!isSkill){
                    if((str + "").startsWith("-")){
                        for (int i = 0; i > str; i--) {
                            damage -= damage*0.01;
                        }
                    } else {
                        for (int i = 0; i < str; i++) {
                            damage += damage*0.01;
                        }
                    }
                }

                if(damage <= 0) damage = 1;
            } else {
                damage = 0;
            }

            // CRITICAL
            if(Util.getChanceBoolean(dex, 100)){
                damage = damage*2;
                critical = true;
            }

			/*for(CustomItem item : u.currentCharacter.getEquipment()){
				if(item.isAwakened()){
					for(Awakening a : item.getAwakenings()){
						if(a.getType() == AwakeningType.ADDITIONAL_STRENGTH) str += a.getMulitplier();
						if(a.getType() == AwakeningType.ADDITIONAL_STAMINA) sta += a.getMulitplier();
						if(a.getType() == AwakeningType.ADDITIONAL_INTELLIGENCE) intel += a.getMulitplier();
						if(a.getType() == AwakeningType.ADDITIONAL_DEXTERITY) dex += a.getMulitplier();
						if(a.getType() == AwakeningType.ADDITIONAL_AGILITY) agi += a.getMulitplier();

						if(a.getType() == AwakeningType.ADDITIONAL_ATTACKDAMAGE) damage += a.getMulitplier();
					}
				}
			}*/

			/*if(isSkill){
				if((intel + "").startsWith("-")){
					for (int i = 0; i > intel; i--) {
						damage -= 0.20;
					}
				} else {
					for (int i = 0; i < intel; i++) {
						damage += 0.20;
					}
				}
			} else {
				if((str + "").startsWith("-")){
					for (int i = 0; i > str; i--) {
						damage -= 0.15;
					}
				} else {
					for (int i = 0; i < str; i++) {
						damage += 0.15;
					}
				}
			}*/

			/*if(source == DungeonDamageSource.PVP){
				Player p2 = (Player)enemy;
				DungeonUser u2 = DungeonUserStorage.getUserByPlayer(p2);

				for(CustomItem item : u2.currentCharacter.getEquipment()){
					if(item.getItemData().getCategory() == ItemCategory.ARMOR){
						damage -= item.getItemData().getDEF();
					}

					if(item.isAwakened()){
						for(Awakening a : item.getAwakenings()){

						}
					}
				}

				damage = damage/2;
			}*/
        }

        damage *= force;
        if(spawnDamageIndicator && gotAttacked){
            //spawnDamageIndicator(p.getLocation(), damage, isPositive, missed, critical);
        } else if(spawnDamageIndicator && !gotAttacked){
            //spawnDamageIndicator(enemy.getLocation(), damage, isPositive, missed, critical);
        }

        return damage;
    }
}
