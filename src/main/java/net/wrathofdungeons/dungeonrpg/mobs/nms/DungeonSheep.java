package net.wrathofdungeons.dungeonrpg.mobs.nms;

import net.minecraft.server.v1_8_R3.*;

public class DungeonSheep extends EntitySheep {
    public DungeonSheep(World world){
        super(world);
    }

    /*@Override
    protected void E(){

    }

    @Override
    public void m(){

    }

    @Override
    public float a(BlockPosition blockPosition){
        return 0f;
    }

    @Override
    public void b(NBTTagCompound nbttagcompound) {

    }

    @Override
    public void a(NBTTagCompound nbttagcompound) {

    }

    @Override
    public boolean bR() {
        return false;
    }

    @Override
    public int w() {
        return 120;
    }*/

    @Override
    protected boolean isTypeNotPersistent() {
        return false;
    }

    @Override
    protected int getExpValue(EntityHuman entityhuman) {
        return 0;
    }

    @Override
    public boolean d(ItemStack itemstack) {
        return false;
    }

    @Override
    public boolean a(EntityHuman entityhuman) {
        return false;
    }

    @Override
    protected void a(EntityHuman entityhuman, ItemStack itemstack) {

    }

    @Override
    public void c(EntityHuman entityhuman) {

    }

    @Override
    public EntityHuman cq() {
        return null;
    }

    @Override
    public boolean isInLove() {
        return false;
    }

    @Override
    public void cs() {

    }

    @Override
    public boolean mate(EntityAnimal entityanimal) {
        return false;
    }
}
