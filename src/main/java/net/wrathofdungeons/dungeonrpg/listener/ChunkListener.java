package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkListener implements Listener {
    @EventHandler
    public void onUnload(ChunkUnloadEvent e){
        Chunk c = e.getChunk();

        for(Entity entity : c.getEntities()){
            CustomEntity ce = CustomEntity.fromEntity(entity);

            if(ce != null){
                ce.remove();
            }
        }
    }
}
