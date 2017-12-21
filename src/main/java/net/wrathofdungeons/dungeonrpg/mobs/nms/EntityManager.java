package net.wrathofdungeons.dungeonrpg.mobs.nms;

import net.minecraft.server.v1_9_R2.EntityHorse;
import net.minecraft.server.v1_9_R2.EntitySheep;
import net.minecraft.server.v1_9_R2.EntityZombie;
import net.wrathofdungeons.dungeonrpg.mobs.handler.TargetHandler;

public class EntityManager {
    public static void registerEntities(){
        TargetHandler.registerEntity("Zombie",54, EntityZombie.class, ZombieArcher.class);
        TargetHandler.registerEntity("Zombie",54, EntityZombie.class, DungeonZombie.class);
        TargetHandler.registerEntity("Horse",100, EntityHorse.class, DungeonHorse.class);
    }
}
