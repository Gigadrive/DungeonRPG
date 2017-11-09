package net.wrathofdungeons.dungeonrpg.damage;

public class DamageData {
    private double damage = 0;
    private boolean dodged = false;
    private boolean blocked = false;
    private boolean critical = false;

    public boolean isBlocked() {
        return blocked;
    }

    public boolean isCritical() {
        return critical;
    }

    public boolean isDodged() {
        return dodged;
    }

    public double getDamage() {
        return damage;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public void setCritical(boolean critical) {
        this.critical = critical;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public void setDodged(boolean dodged) {
        this.dodged = dodged;
    }
}
