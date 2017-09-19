package net.wrathofdungeons.dungeonrpg.skill;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;

public class SkillValues {
    public int dartRainArrows;
    public BukkitTask dartRainTask;
    public boolean leapIsInAir;
    public BukkitTask vortexBarrierTask;
    public int vortexBarrierCount;
    public Location vortexBarrierLoc;

    public SkillValues(){
        reset();
    }

    public void reset(){
        dartRainArrows = 0;
        leapIsInAir = false;
        vortexBarrierCount = 0;

        if(dartRainTask != null){
            dartRainTask.cancel();
            dartRainTask = null;
        }
    }
}
