package net.wrathofdungeons.dungeonrpg.mobs;

import org.bukkit.Location;
import org.bukkit.Sound;

public class MobSoundData {
    public Sound sound;
    public float volume;
    public float pitch;

    public void play(Location loc){
        if(loc != null && sound != null){
            loc.getWorld().playSound(loc,sound,volume,pitch);
        }
    }

    public void playDeath(Location loc){
        if(loc != null && sound != null){
            loc.getWorld().playSound(loc,sound,volume,pitch/2);
        }
    }
}
