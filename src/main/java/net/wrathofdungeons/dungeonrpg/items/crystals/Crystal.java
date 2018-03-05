package net.wrathofdungeons.dungeonrpg.items.crystals;

public class Crystal {
    private CrystalType type;

    public Crystal(CrystalType type){
        this.type = type;
    }

    public CrystalType getType() {
        return type;
    }

    public void setType(CrystalType type) {
        this.type = type;
    }
}
