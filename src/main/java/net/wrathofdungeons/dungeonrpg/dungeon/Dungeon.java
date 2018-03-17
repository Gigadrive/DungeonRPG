package net.wrathofdungeons.dungeonrpg.dungeon;

import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.party.Party;
import net.wrathofdungeons.dungeonrpg.regions.Region;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class Dungeon {
    public static ArrayList<Dungeon> STORAGE = new ArrayList<Dungeon>();

    // DON'T CALL THIS IN THE MAIN THREAD
    public static Dungeon loadNewTemplate(DungeonType type) throws Exception {
        final String originalName = "dungeonTemplate_" + type.getWorldTemplateName();

        String worldName = null;

        while(worldName == null || (new File(Bukkit.getWorldContainer().getAbsolutePath() + DungeonRPG.pathSuffix(Bukkit.getWorldContainer().getAbsolutePath()) + worldName).exists() || new File(Bukkit.getWorldContainer().getAbsolutePath() + DungeonRPG.pathSuffix(Bukkit.getWorldContainer().getAbsolutePath()) + worldName).isDirectory())){
            worldName = originalName + "_" + Util.randomInteger(1,99999);
        }

        DungeonRPG.copyWorldToNewWorld(originalName,worldName);

        Dungeon dungeon = new Dungeon(worldName,type);

        for(Region region : Region.getRegions(originalName)){
            Region r = region.clone(worldName);

            r.setCooldown(-1);

            dungeon.getRegions().add(r);
        }


        return dungeon;
    }

    public static Dungeon fromWorld(World world){
        for(Dungeon dungeon : STORAGE)
            if(dungeon.getWorldName().equalsIgnoreCase(world.getName()))
                return dungeon;

        return null;
    }

    private String worldName;
    private DungeonType type;
    private Party party;
    private ArrayList<Region> regions;

    public Dungeon(String worldName, DungeonType type){
        this.worldName = worldName;
        this.type = type;
        this.regions = new ArrayList<Region>();
    }

    public String getWorldName() {
        return worldName;
    }

    public DungeonType getType() {
        return type;
    }

    public Party getParty() {
        return party;
    }

    public void setParty(Party party) {
        this.party = party;
    }

    public World getWorld(){
        return Bukkit.getWorld(worldName);
    }

    public ArrayList<Region> getRegions() {
        return regions;
    }

    public ArrayList<CustomEntity> getMobs(){
        ArrayList<CustomEntity> a = new ArrayList<CustomEntity>();

        for(CustomEntity entity : CustomEntity.STORAGE.values())
            if(entity.getBukkitEntity() != null && entity.getBukkitEntity().getLocation().getWorld().getName().equals(getWorldName()))
                a.add(entity);

        return a;
    }

    public void unregister(){
        World world = getWorld();

        if(world != null){
            for(Player p : getWorld().getPlayers()){
                p.teleport(getType().getPortalEntranceLocation());
            }

            for(Region region : getRegions())
                Region.getStorage().remove(region);

            STORAGE.remove(this);

            new BukkitRunnable(){
                @Override
                public void run() {
                    try {
                        Bukkit.unloadWorld(world,false);
                        FileUtils.deleteDirectory(new File(Bukkit.getWorldContainer() + File.pathSeparator + worldName));
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }.runTaskLaterAsynchronously(DungeonRPG.getInstance(),2*20);
        }
    }
}
