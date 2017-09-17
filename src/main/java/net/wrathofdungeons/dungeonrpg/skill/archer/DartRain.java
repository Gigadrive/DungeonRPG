package net.wrathofdungeons.dungeonrpg.skill.archer;

import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.projectile.DungeonProjectile;
import net.wrathofdungeons.dungeonrpg.projectile.DungeonProjectileType;
import net.wrathofdungeons.dungeonrpg.skill.Skill;
import net.wrathofdungeons.dungeonrpg.skill.SkillType;
import net.wrathofdungeons.dungeonrpg.skill.SkillValues;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class DartRain implements Skill {
    @Override
    public String getName() {
        return "Dart Rain";
    }

    @Override
    public RPGClass getRPGClass() {
        return RPGClass.ARCHER;
    }

    @Override
    public SkillType getType() {
        return SkillType.FAST_ATTACK;
    }

    @Override
    public String getCombo() {
        return "LRL";
    }

    @Override
    public void execute(Player p) {
        GameUser u = GameUser.getUser(p);
        CustomItem weapon = CustomItem.fromItemStack(p.getItemInHand());

        if(weapon != null){
            SkillValues values = u.getSkillValues();

            final int arrowAmount = 6;

            values.dartRainArrows = arrowAmount;

            if(values.dartRainTask == null){
                values.dartRainTask = new BukkitRunnable(){
                    @Override
                    public void run() {
                        if(values.dartRainArrows == 0){
                            cancel();
                            values.dartRainTask = null;
                        } else {
                            double damage = 0;
                            damage += Util.randomDouble(weapon.getData().getAtkMin(),weapon.getData().getAtkMax());

                            // TODO: Calculate damage with equipment, stat values etc.

                            if(damage < 1) damage = 1;

                            damage /= 4;

                            Arrow a = p.launchProjectile(Arrow.class);
                            p.getWorld().playSound(p.getEyeLocation(), Sound.SHOOT_ARROW,1f,1f);
                            a.setVelocity(p.getLocation().getDirection().multiply(2));
                            DungeonProjectile data = new DungeonProjectile(p, DungeonProjectileType.DART_RAIN, p.getLocation(), 0, damage, true);
                            DungeonRPG.SHOT_PROJECTILE_DATA.put(a.getUniqueId().toString(),data);

                            values.dartRainArrows--;
                        }
                    }
                }.runTaskTimer(DungeonRPG.getInstance(),1,20/arrowAmount);
            }
        }
    }
}
