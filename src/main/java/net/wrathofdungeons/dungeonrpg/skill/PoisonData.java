package net.wrathofdungeons.dungeonrpg.skill;

import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.damage.DamageData;
import net.wrathofdungeons.dungeonrpg.damage.DamageHandler;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.mobs.MobType;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;

public class PoisonData {
    public int investedSkillPoints = 0;
    public Player givenByPlayer = null;
    public CustomEntity givenByEntity = null;
    private ArrayList<BukkitTask> tasks;

    public Player targetPlayer = null;
    public CustomEntity targetEntity = null;

    private double damage = -1;

    public void startTask(){
        if(tasks == null){
            tasks = new ArrayList<BukkitTask>();

            int amount = Integer.parseInt(SkillStorage.getInstance().getSkill("PoisonArrow").getEffects(investedSkillPoints).get("Poison Time").replace(" seconds",""))*2;
            int interval = 10;

            for(int i = 1; i < amount+1; i++){
                tasks.add(new BukkitRunnable(){
                    @Override
                    public void run() {
                        if(targetEntity != null){
                            if(DungeonRPG.isInGame(givenByPlayer)){
                                GameUser u = GameUser.getUser(givenByPlayer);

                                if(damage == -1) damage = DamageHandler.calculatePlayerToMobDamage(u,targetEntity,SkillStorage.getInstance().getSkill("PoisonArrow")).getDamage();

                                DamageData damageData = new DamageData();
                                damageData.setDamage(damage);

                                DamageHandler.spawnDamageIndicator(u.getPlayer(),damageData,targetEntity.getBukkitEntity().getLocation());
                                targetEntity.damage(damage,givenByPlayer);
                            } /*else if(givenByEntity != null && givenByEntity.getBukkitEntity().isValid() && givenByEntity.getData().getMobType().mayAttack(targetEntity.getData().getMobType())){

                            } */else {
                                cancelTasks();
                            }
                        } else if(targetPlayer != null){
                            if(DungeonRPG.isInGame(givenByPlayer)){
                                GameUser u = GameUser.getUser(givenByPlayer);
                                GameUser u2 = GameUser.getUser(targetPlayer);

                                if(damage == -1) damage = DamageHandler.calculatePlayerToPlayerDamage(u,u2,SkillStorage.getInstance().getSkill("PoisonArrow")).getDamage();

                                DamageData damageData = new DamageData();
                                damageData.setDamage(damage);

                                targetPlayer.damage(damage,givenByPlayer);
                                DamageHandler.spawnDamageIndicator(u.getPlayer(),damageData,targetPlayer.getLocation());
                            } /*else if(givenByEntity != null && givenByEntity.getBukkitEntity().isValid() && (givenByEntity.getData().getMobType() == MobType.AGGRO || givenByEntity.getData().getMobType() == MobType.NEUTRAL)){

                            } */else {
                                cancelTasks();
                            }
                        }
                    }
                }.runTaskLater(DungeonRPG.getInstance(),i*interval));
            }
        }
    }

    public void cancelTasks(){
        if(tasks != null){
            for(BukkitTask task : tasks) task.cancel();

            tasks = null;
        }
    }
}
