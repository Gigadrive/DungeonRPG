package net.wrathofdungeons.dungeonrpg.dungeon;

import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.regions.Region;
import net.wrathofdungeons.dungeonrpg.regions.RegionLocation;
import net.wrathofdungeons.dungeonrpg.regions.RegionLocationType;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;

public enum DungeonType {
    CRYPT_OF_THE_UNDEAD("Crypt of the Undead",10,"crypt");

    private String name;
    private int minLevel;
    private String worldTemplateName;

    DungeonType(String name, int minLevel, String worldTemplateName){
        this.name = name;
        this.minLevel = minLevel;
        this.worldTemplateName = worldTemplateName;
    }

    public String getName() {
        return name;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public String getWorldTemplateName() {
        return worldTemplateName;
    }

    public Location getPortalEntranceLocation(){
        for(Region region : Region.getRegions()){
            if(!region.isCopy()){
                ArrayList<RegionLocation> loc = region.getLocations(RegionLocationType.DUNGEON_GATEWAY,1);

                if(region.getAdditionalData().dungeonType == this && loc.size() > 0){
                    Location location = loc.get(0).toBukkitLocation();
                    location.setWorld(DungeonRPG.MAIN_WORLD);
                    return location;
                }
            }
        }

        return DungeonRPG.sortedTownLocations().get(Util.randomInteger(0,DungeonRPG.sortedTownLocations().size()-1)).toBukkitLocation();
    }

    public Location getEntryLocation(String worldName){
        for(Region region : Region.getRegions()){
            if(!region.isCopy()){
                ArrayList<RegionLocation> loc = region.getLocations(RegionLocationType.DUNGEON_ENTRY_POINT,1);

                if(region.getAdditionalData().dungeonType == this && loc.size() > 0){
                    Location location = loc.get(0).toBukkitLocation();
                    location.setWorld(Bukkit.getWorld(worldName));

                    return location;
                }
            }
        }

        return null;
    }

    public static DungeonType fromWorldName(String name){
        for(DungeonType type : values()){
            if(name.startsWith("dungeonTemplate_" + type.getWorldTemplateName())){
                return type;
            }
        }

        return null;
    }
}
