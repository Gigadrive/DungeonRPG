package net.wrathofdungeons.dungeonrpg.regions;

public enum RegionLocationType {
    MOB_LOCATION,TOWN_LOCATION,MOB_ACTIVATION_1,MOB_ACTIVATION_2,CRAFTING_STATION,PVP_ARENA,PVP_RESPAWN,DUNGEON_GATEWAY,DUNGEON_ENTRY_POINT;

    public static RegionLocationType fromName(String s){
        for(RegionLocationType t : values()){
            if(t.toString().equalsIgnoreCase(s)) return t;
        }

        return null;
    }
}
