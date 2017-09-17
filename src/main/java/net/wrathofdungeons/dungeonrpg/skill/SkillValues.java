package net.wrathofdungeons.dungeonrpg.skill;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class SkillValues {
    public int dartRainArrows;
    public BukkitTask dartRainTask;

    public SkillValues(){
        reset();
    }

    public void reset(){
        dartRainArrows = 0;

        if(dartRainTask != null){
            dartRainTask.cancel();
            dartRainTask = null;
        }
    }
}
