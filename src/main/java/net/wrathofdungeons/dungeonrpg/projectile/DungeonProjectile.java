package net.wrathofdungeons.dungeonrpg.projectile;

import net.wrathofdungeons.dungeonrpg.damage.SkillData;
import net.wrathofdungeons.dungeonrpg.skill.PoisonData;
import net.wrathofdungeons.dungeonrpg.skill.Skill;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class DungeonProjectile {
    private Player p;
    private DungeonProjectileType type;
    private Location firedFrom;
    private int range;
    private double damage;
    private boolean isSkill;
    private double force;
    private Entity entity;
    private PoisonData poisonData;
    private SkillData skillData;

    @Deprecated
    private Skill skill;

    private static DungeonProjectile fakeProjectile;

    public static DungeonProjectile getFakeProjectile(){
        if(fakeProjectile == null) fakeProjectile = new DungeonProjectile(null,DungeonProjectileType.FAKE_PROJECTILE,null,0,0,false);

        return fakeProjectile;
    }

    public DungeonProjectile(Player p, DungeonProjectileType type, Location firedFrom, int range, double damage, boolean isSkill) {
        this.p = p;
        this.type = type;
        this.firedFrom = firedFrom;
        this.range = range;
        this.damage = damage;
        this.isSkill = isSkill;
        this.force = 1.0;
    }

    public DungeonProjectile(Player p, DungeonProjectileType type, Location firedFrom, int range, double damage, boolean isSkill, double force) {
        this.p = p;
        this.type = type;
        this.firedFrom = firedFrom;
        this.range = range;
        this.damage = damage;
        this.isSkill = isSkill;
        this.force = force;
    }

    public Player getPlayer(){
        return this.p;
    }

    public DungeonProjectileType getType(){
        return this.type;
    }

    public Location getFiredFrom(){
        return this.firedFrom;
    }

    public int getRange(){
        return this.range;
    }

    public double getDamage(){
        return this.damage;
    }

    public boolean isSkill(){
        return this.isSkill;
    }

    public double getForce(){
        return this.force;
    }

    public void setEntity(Entity e){
        this.entity = e;
    }

    public Entity getEntity(){
        return entity;
    }

    @Deprecated
    public Skill getSkill() {
        return skill;
    }

    @Deprecated
    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    public SkillData getSkillData() {
        return skillData;
    }

    public void setSkillData(SkillData skillData) {
        this.skillData = skillData;
    }

    public PoisonData getPoisonData() {
        return poisonData;
    }

    public void setPoisonData(PoisonData poisonData) {
        this.poisonData = poisonData;
    }
}
