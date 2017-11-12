package net.wrathofdungeons.dungeonrpg.professions;

import org.bukkit.Material;

public enum OreLevel {
    LEVEL_1(1,Material.COAL_ORE,new String[]{
            "651:1:12",
    },0),
    LEVEL_2(2,Material.IRON_ORE,new String[]{
            "651:25:13",
            "652:5:13",
    },87),
    LEVEL_3(3,Material.LAPIS_ORE,new String[]{
            "651:120:14",
            "652:75:14",
            "653:15:14",
    },87),
    LEVEL_4(4,Material.REDSTONE_ORE,new String[]{
            "651:260:15",
            "652:130:15",
            "653:85:15",
            "654:15:15",
    },145),
    LEVEL_5(5,Material.GOLD_ORE,new String[]{
            "651:430:16",
            "652:320:16",
            "653:110:16",
            "654:65:16",
            "655:5:16",
    },145),
    LEVEL_6(6,Material.DIAMOND_ORE,new String[]{
            "651:1120:17",
            "652:460:17",
            "653:220:18",
            "654:130:17",
            "655:60:18",
            "656:50:17",
    },178);

    private int id;
    private Material block;
    private String[] drops;
    private int requiredPickaxeStrength;

    OreLevel(int id, Material block, String[] drops, int requiredPickaxeStrength){
        this.id = id;
        this.block = block;
        this.drops = drops;
        this.requiredPickaxeStrength = requiredPickaxeStrength;
    }

    public int getID() {
        return id;
    }

    public Material getBlock() {
        return block;
    }

    public String[] getDrops() {
        return drops;
    }

    public int getRequiredPickaxeStrength() {
        return requiredPickaxeStrength;
    }

    public static OreLevel fromID(int id){
        for(OreLevel level : values()){
            if(level.getID() == id) return level;
        }

        return null;
    }

    public static OreLevel fromBlock(Material block){
        for(OreLevel level : values()){
            if(level.getBlock() == block) return level;
        }

        return null;
    }
}
