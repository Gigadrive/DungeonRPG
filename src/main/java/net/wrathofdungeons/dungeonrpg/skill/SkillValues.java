package net.wrathofdungeons.dungeonrpg.skill;

import net.wrathofdungeons.dungeonrpg.skill.mercenary.Stomper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;

public class SkillValues {
    public int dartRainArrows;
    public BukkitTask dartRainTask;
    public boolean leapIsInAir;
    public BukkitTask vortexBarrierTask;
    public int vortexBarrierCount;
    public Location vortexBarrierLoc;
    public int magicCureCount;
    public BukkitTask magicCureTask;
    public boolean stomperActive;
    public Stomper stomperSkill;
    public BukkitTask lightningStrikeTask;
    public int lightningStrikeCount;

    public ArrayList<BukkitTask> skillTasks;

    public SkillValues(){
        reset();
    }

    public void reset(){
        dartRainArrows = 0;
        leapIsInAir = false;
        vortexBarrierCount = 0;
        magicCureCount = 0;
        stomperActive = false;
        lightningStrikeCount = 0;

        if(dartRainTask != null){
            dartRainTask.cancel();
            dartRainTask = null;
        }

        if(magicCureTask != null){
            magicCureTask.cancel();
            magicCureTask = null;
        }

        if(dartRainTask != null){
            dartRainTask.cancel();
            dartRainTask = null;
        }

        if(lightningStrikeTask != null){
            lightningStrikeTask.cancel();
            lightningStrikeTask = null;
        }

        if(skillTasks != null){
            for(BukkitTask t : skillTasks) t.cancel();

            skillTasks.clear();
        } else {
            skillTasks = new ArrayList<BukkitTask>();
        }
    }
}
