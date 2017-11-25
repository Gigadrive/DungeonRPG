package net.wrathofdungeons.dungeonrpg.skill.magician;

import net.wrathofdungeons.dungeonapi.util.ParticleEffect;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.skill.Skill;
import net.wrathofdungeons.dungeonrpg.skill.SkillValues;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import net.wrathofdungeons.dungeonrpg.util.WorldUtilities;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;

public class MagicCure implements Skill {
    @Override
    public String getName() {
        return "Magic Cure";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public HashMap<String, String> getEffects(int investedSkillPoints) {
        HashMap<String,String> effects = new HashMap<String, String>();

        double healStrength = 1.2;
        int range = 4;

        if(investedSkillPoints == 2){
            healStrength = 1.3;
            range = 5;
        } else if(investedSkillPoints == 3){
            healStrength = 1.3;
            range = 6;
        } else if(investedSkillPoints == 4){
            healStrength = 1.35;
            range = 7;
        } else if(investedSkillPoints == 5){
            healStrength = 1.4;
            range = 7;
        } else if(investedSkillPoints == 6){
            healStrength = 1.6;
            range = 8;
        } else if(investedSkillPoints == 7){
            healStrength = 1.8;
            range = 9;
        }

        effects.put("Range",String.valueOf(range));
        effects.put("Heal Strength",String.valueOf(healStrength));

        return effects;
    }

    @Override
    public int getIcon() {
        return 38;
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
        return 45;
    }

    @Override
    public int getMaxInvestingPoints() {
        return 6;
    }

    @Override
    public int getBaseMPCost() {
        return 4;
    }

    @Override
    public void execute(Player p) {
        GameUser u = GameUser.getUser(p);
        int investedSkillPoints = u.getCurrentCharacter().getVariables().getInvestedSkillPoints(this);
        CustomItem weapon = CustomItem.fromItemStack(p.getItemInHand());
        final int range = Integer.parseInt(getEffects(investedSkillPoints).get("Range"));

        if(weapon != null){
            SkillValues values = u.getSkillValues();

            // START PARTICLES

            ArrayList<Location> circle = WorldUtilities.getParticleCircle(p.getLocation().clone().add(0,1,0),range,15);

            if(values.magicCureTask != null){
                values.magicCureTask.cancel();
                values.magicCureTask = null;
            }

            values.magicCureCount = 0;

            values.magicCureTask = new BukkitRunnable(){
                @Override
                public void run() {
                    if(values.magicCureCount == circle.size()){
                        cancel();
                        values.magicCureTask = null;
                    } else {
                        Location l = circle.get(values.magicCureCount);

                        ParticleEffect.HEART.display(0f,0f,0f,0.005f,1,l,600);

                        values.magicCureCount++;
                    }
                }
            }.runTaskTimer(DungeonRPG.getInstance(),0,1);

            // START ACTUAL HEALING

            int toHeal = (int)(Util.randomInteger(weapon.getData().getAtkMin(),weapon.getData().getAtkMax())*Double.parseDouble(getEffects(investedSkillPoints).get("Heal Strength").replace("x","")));

            ArrayList<Player> players = new ArrayList<Player>();
            players.add(p);

            for(Entity ent : p.getNearbyEntities(range,range,range)){
                if(ent instanceof Player){
                    Player p2 = (Player)ent;

                    if(GameUser.isLoaded(p2)){
                        GameUser u2 = GameUser.getUser(p2);

                        if(u2.getCurrentCharacter() != null) players.add(p2);
                    }
                }
            }

            for(Player p2 : players){
                GameUser u2 = GameUser.getUser(p2);
                p2.sendMessage(ChatColor.GREEN + p.getName() + " has healed you.");

                int hp = u2.getHP()+toHeal;
                if(hp > u2.getMaxHP()) hp = u2.getMaxHP();
                u2.setHP(hp);
            }
        }
    }
}
