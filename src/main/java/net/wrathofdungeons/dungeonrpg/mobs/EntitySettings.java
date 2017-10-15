package net.wrathofdungeons.dungeonrpg.mobs;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.*;

public class EntitySettings {
    private boolean zombieVillager;
    private Villager.Profession villagerProfession;
    private DyeColor sheepColor;
    private Ocelot.Type catType;
    private boolean creeperPowered;
    private Skeleton.SkeletonType skeletonType;
    private int slimeSize;
    private Material endermanBlock;
    private boolean elderGuardian;
    private Horse.Color horseColor;
    private boolean horseChest;
    private Horse.Style horseStyle;
    private Horse.Variant horseVariant;
    private boolean horseSaddle;
    private HorseArmor horseArmor;
    private Rabbit.Type rabbitType;

    public boolean isZombieVillager() {
        return zombieVillager;
    }

    public void setZombieVillager(boolean zombieVillager) {
        this.zombieVillager = zombieVillager;
    }

    public Villager.Profession getVillagerProfession() {
        return villagerProfession;
    }

    public void setVillagerProfession(Villager.Profession villagerProfession) {
        this.villagerProfession = villagerProfession;
    }

    public DyeColor getSheepColor() {
        return sheepColor;
    }

    public void setSheepColor(DyeColor sheepColor) {
        this.sheepColor = sheepColor;
    }

    public Ocelot.Type getCatType() {
        return catType;
    }

    public void setCatType(Ocelot.Type catType) {
        this.catType = catType;
    }

    public boolean isCreeperPowered() {
        return creeperPowered;
    }

    public void setCreeperPowered(boolean creeperPowered) {
        this.creeperPowered = creeperPowered;
    }

    public Skeleton.SkeletonType getSkeletonType() {
        return skeletonType;
    }

    public void setSkeletonType(Skeleton.SkeletonType skeletonType) {
        this.skeletonType = skeletonType;
    }

    public int getSlimeSize() {
        return slimeSize;
    }

    public void setSlimeSize(int slimeSize) {
        this.slimeSize = slimeSize;
    }

    public Material getEndermanBlock() {
        return endermanBlock;
    }

    public void setEndermanBlock(Material endermanBlock) {
        this.endermanBlock = endermanBlock;
    }

    public boolean isElderGuardian() {
        return elderGuardian;
    }

    public void setElderGuardian(boolean elderGuardian) {
        this.elderGuardian = elderGuardian;
    }

    public boolean hasHorseChest() {
        return horseChest;
    }

    public void setHasHorseChest(boolean horseChest) {
        this.horseChest = horseChest;
    }

    public Horse.Color getHorseColor() {
        return horseColor;
    }

    public void setHorseColor(Horse.Color horseColor) {
        this.horseColor = horseColor;
    }

    public Horse.Style getHorseStyle() {
        return horseStyle;
    }

    public void setHorseStyle(Horse.Style horseStyle) {
        this.horseStyle = horseStyle;
    }

    public Horse.Variant getHorseVariant() {
        return horseVariant;
    }

    public void setHorseVariant(Horse.Variant horseVariant) {
        this.horseVariant = horseVariant;
    }

    public boolean hasHorseSaddle() {
        return horseSaddle;
    }

    public void setHasHorseSaddle(boolean horseSaddle) {
        this.horseSaddle = horseSaddle;
    }

    public HorseArmor getHorseArmor() {
        return horseArmor;
    }

    public void setHorseArmor(HorseArmor horseArmor) {
        this.horseArmor = horseArmor;
    }

    public Rabbit.Type getRabbitType() {
        return rabbitType;
    }

    public void setRabbitType(Rabbit.Type rabbitType) {
        this.rabbitType = rabbitType;
    }
}
