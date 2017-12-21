package net.wrathofdungeons.dungeonrpg.skill.archer;

import net.wrathofdungeons.dungeonapi.util.ParticleEffect;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.skill.Skill;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class Leap implements Skill {
    @Override
    public String getName() {
        return "Leap";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public HashMap<String, String> getEffects(int investedSkillPoints) {
        HashMap<String,String> effects = new HashMap<String, String>();

        effects.put("Jump Strength",investedSkillPoints + "x");

        return effects;
    }

    @Override
    public int getIcon() {
        return 288;
    }

    @Override
    public int getIconDurability() {
        return 0;
    }

    @Override
    public RPGClass getRPGClass() {
        return RPGClass.ARCHER;
    }

    @Override
    public int getMinLevel() {
        return 15;
    }

    @Override
    public int getMaxInvestingPoints() {
        return 3;
    }

    @Override
    public int getBaseMPCost() {
        return 2;
    }

    @Override
    public void execute(Player p) {
        GameUser u = GameUser.getUser(p);

        int investedSkillPoints = u.getCurrentCharacter().getVariables().getInvestedSkillPoints(this);

        double m = 2.5;
        double y = 1.25;

        if(investedSkillPoints == 1){
            m = 2;
            y = 1;
        } else if(investedSkillPoints == 2){
            m = 2.5;
            y = 1.25;
        } else if(investedSkillPoints == 3){
            m = 3;
            y = 1.5;
        }

        if(!u.getSkillValues().leapIsInAir){
            u.getSkillValues().leapIsInAir = true;
            p.getWorld().playSound(p.getEyeLocation(), Sound.ENTITY_FIREWORK_LAUNCH, 1f, 1f);

            for(int i = 0; i < 100; i++){
                ParticleEffect.SPELL_MOB.display(new ParticleEffect.OrdinaryColor(92,214,255),p.getLocation().clone().add(Util.randomInteger(-1,1),0,Util.randomInteger(-1,1)),600);
            }

            p.setVelocity(p.getLocation().getDirection().multiply(m).setY(y));
        }
    }
}