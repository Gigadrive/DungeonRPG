package net.wrathofdungeons.dungeonrpg.npc;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.VillagerProfession;
import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.MySQLManager;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Villager;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class CustomNPC {
    public static ArrayList<CustomNPC> STORAGE = new ArrayList<CustomNPC>();

    public static CustomNPC fromID(int id){
        for(CustomNPC npc : STORAGE){
            if(npc.getId() == id) return npc;
        }

        return null;
    }

    public static CustomNPC fromCitizensNPC(NPC npc){
        for(CustomNPC c : STORAGE){
            if(c.npc != null && (c.npc == npc || c.npc.getId() == npc.getId())){
                return c;
            }
        }

        return null;
    }

    public static CustomNPC fromEntity(Entity e){
        for(CustomNPC c : STORAGE){
            if(c.npc != null && c.npc.getEntity() != null && c.npc.getEntity().getUniqueId() != null && c.npc.getEntity().getUniqueId().toString().equals(e.getUniqueId().toString())){
                return c;
            }
        }

        return null;
    }

    public static void init(){
        if(STORAGE.size() > 0) for(CustomNPC npc : STORAGE) npc.despawnNPC();
        STORAGE.clear();

        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `npcs` ORDER BY `id` DESC");
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                new CustomNPC(rs.getInt("id"));
            }

            MySQLManager.getInstance().closeResources(rs,ps);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public static ArrayList<CustomNPC> getUnsavedData(){
        ArrayList<CustomNPC> a = new ArrayList<CustomNPC>();

        for(CustomNPC npc : STORAGE) if(npc.hasUnsavedData) a.add(npc);

        return a;
    }

    private int id;
    private String customName;
    private CustomNPCType npcType;
    private EntityType entityType;
    private Villager.Profession villagerProfession;
    private Location storedLocation;

    private NPC npc;
    private Hologram hologram;

    private boolean hasUnsavedData;

    public CustomNPC(int id){
        if(fromID(id) != null) return;

        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `npcs` WHERE `id` = ?");
            ps.setInt(1,id);
            ResultSet rs = ps.executeQuery();

            if(rs.first()){
                this.id = rs.getInt("id");
                this.customName = rs.getString("customName");
                this.npcType = CustomNPCType.fromName(rs.getString("npcType"));
                this.entityType = EntityType.valueOf(rs.getString("entityType"));
                this.villagerProfession = Villager.Profession.valueOf(rs.getString("villager.profession"));
                this.storedLocation = new Location(Bukkit.getWorld(rs.getString("location.world")),rs.getDouble("location.x"),rs.getDouble("location.y"),rs.getDouble("location.z"),rs.getFloat("location.yaw"),rs.getFloat("location.pitch"));

                spawnNPC();

                STORAGE.add(this);
            }

            MySQLManager.getInstance().closeResources(rs,ps);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
        setHasUnsavedData(true);

        despawnNPC();
        spawnNPC();
    }

    public CustomNPCType getNpcType() {
        return npcType;
    }

    public void setNpcType(CustomNPCType npcType) {
        this.npcType = npcType;
        setHasUnsavedData(true);

        despawnNPC();
        spawnNPC();
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
        setHasUnsavedData(true);

        despawnNPC();
        spawnNPC();
    }

    public Villager.Profession getVillagerProfession() {
        return villagerProfession;
    }

    public void setVillagerProfession(Villager.Profession villagerProfession) {
        this.villagerProfession = villagerProfession;
        setHasUnsavedData(true);

        despawnNPC();
        spawnNPC();
    }

    public Location getLocation() {
        return storedLocation;
    }

    public void setLocation(Location loc){
        if(loc != null){
            storedLocation = loc;
            setHasUnsavedData(true);

            if(npc != null) npc.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
            updateHologram();
        }
    }

    public Hologram getHologram() {
        return hologram;
    }

    public void spawnNPC(){
        if(!isSpawned()){
            npc = CitizensAPI.getNPCRegistry().createNPC(getEntityType(),"a");

            npc.setName(ChatColor.GREEN.toString());

            if(getNpcType() == CustomNPCType.BUYING_MERCHANT){
                npc.setBukkitEntityType(EntityType.VILLAGER);
            }

            npc.spawn(getLocation());

            if(getEntityType() == EntityType.VILLAGER){
                npc.getTrait(VillagerProfession.class).setProfession(getVillagerProfession());
            }

            if(getEntityType() != EntityType.PLAYER){
                DungeonAPI.nmsMakeSilent(npc.getEntity());
                ((LivingEntity)npc.getEntity()).setRemoveWhenFarAway(false);
            }
        }

        updateHologram();
    }

    public void despawnNPC(){
        if(isSpawned()){
            toCitizensNPC().destroy();
            this.npc = null;
        }

        if(hologram != null && !hologram.isDeleted()) hologram.delete();
        hologram = null;
    }

    public boolean isSpawned(){
        return this.npc != null;
    }

    public Location getPotentialHologramLocation(){
        if(isSpawned()){
            return getLocation().clone().add(0,2.5,0);
        } else {
            return null;
        }
    }

    public void updateHologram(){
        DungeonAPI.sync(() -> {
            if(isSpawned()){
                if(hologram == null){
                    hologram = HologramsAPI.createHologram(DungeonRPG.getInstance(),getPotentialHologramLocation());

                    if(getCustomName() == null){
                        hologram.appendTextLine(getNpcType().getColor() + getNpcType().getDefaultName());
                    } else {
                        hologram.appendTextLine(getNpcType().getColor() + getCustomName());
                    }
                } else {
                    hologram.teleport(getPotentialHologramLocation());
                }
            } else {
                if(hologram != null && !hologram.isDeleted()) hologram.delete();
                hologram = null;
            }
        });
    }

    public boolean hasUnsavedData() {
        return hasUnsavedData;
    }

    public void setHasUnsavedData(boolean hasUnsavedData){
        this.hasUnsavedData = hasUnsavedData;
    }

    public NPC toCitizensNPC(){
        return npc;
    }

    public void saveData(){
        saveData(true);
    }

    public void saveData(boolean async){
        if(async){
            DungeonAPI.async(() -> saveData(false));
        } else {
            setHasUnsavedData(false);

            try {
                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("UPDATE `npcs` SET `customName` = ?, `npcType` = ?, `entityType` = ?, `villager.profession` = ?, `location.world` = ?, `location.x` = ?, `location.y` = ?, `location.z` = ?, `location.yaw`= ?, `location.pitch` = ? WHERE `id` = ?");
                ps.setString(1,getCustomName());
                ps.setString(2,getNpcType().toString());
                ps.setString(3,getEntityType().toString());
                ps.setString(4,getVillagerProfession().toString());
                ps.setString(5,getLocation().getWorld().getName());
                ps.setDouble(6,getLocation().getX());
                ps.setDouble(7,getLocation().getY());
                ps.setDouble(8,getLocation().getZ());
                ps.setFloat(9,getLocation().getYaw());
                ps.setFloat(10,getLocation().getPitch());
                ps.setInt(11,getId());
                ps.executeUpdate();
                ps.close();
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public void unregister(){
        despawnNPC();
        STORAGE.remove(this);
    }
}
