package net.wrathofdungeons.dungeonrpg.lootchests;

import net.wrathofdungeons.dungeonapi.MySQLManager;
import net.wrathofdungeons.dungeonapi.util.ParticleEffect;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.omg.CORBA.SetOverrideType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.UUID;

public class LootChest {
    public static HashMap<Integer,LootChest> STORAGE = new HashMap<Integer,LootChest>();

    public static void init(){
        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `lootchests`");
            ResultSet rs = ps.executeQuery();

            while(rs.next()) new LootChest(rs.getInt("id"));

            MySQLManager.getInstance().closeResources(rs,ps);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public static LootChest getChest(int id){
        if(STORAGE.containsKey(id)){
            return STORAGE.get(id);
        } else {
            return null;
        }
    }

    public static LootChest getChest(Location loc){
        Location l = new Location(loc.getWorld(),loc.getBlockX(),loc.getBlockY(),loc.getBlockZ());

        for(LootChest c : STORAGE.values()){
            if(Util.isLocationEqual(c.getLocation(),l)) return c;
        }

        return null;
    }

    public static LootChest getChest(Player p){
        for(LootChest c : STORAGE.values()){
            if(c.getClaimed() != null && c.getClaimed().equals(p)) return c;
        }

        return null;
    }

    private int id;
    private int level;
    private LootChestTier tier;
    private Location loc;
    private UUID addedBy;

    private boolean spawned = false;
    private byte chestData = 0;
    private BukkitTask respawnTask;
    private Player claimed;

    public LootChest(int id){
        if(STORAGE.containsKey(id)) return;

        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `lootchests` WHERE `id` = ?");
            ps.setInt(1,id);

            ResultSet rs = ps.executeQuery();
            if(rs.first()){
                this.id = id;
                this.level = rs.getInt("level");
                this.tier = LootChestTier.fromNumber(rs.getInt("tier"));
                this.loc = new Location(Bukkit.getWorld(rs.getString("location.world")), rs.getDouble("location.x"), rs.getDouble("location.y"), rs.getDouble("location.z"));
                if(rs.getString("addedBy") != null) this.addedBy = UUID.fromString(rs.getString("addedBy"));

                spawn();

                STORAGE.put(id,this);
            }

            MySQLManager.getInstance().closeResources(rs,ps);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public int getLevel() {
        return level;
    }

    public LootChestTier getTier() {
        return tier;
    }

    public Location getLocation() {
        return loc;
    }

    public UUID getAddedBy() {
        return addedBy;
    }

    public void unregister(){
        STORAGE.remove(id);
        despawn(false);
    }

    public void delete(){
        unregister();

        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("DELETE FROM `lootchests` WHERE `id` = ?");
            ps.setInt(1,getId());
            ps.executeUpdate();
            ps.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean isClaimed(){
        return getClaimed() != null;
    }

    public void claim(Player p){
        claimed = p;
    }

    public Player getClaimed() {
        return claimed;
    }

    public void spawn(){
        if(!isSpawned()){
            spawned = true;

            if(getLocation().getBlock().getType() != Material.CHEST){
                getLocation().getBlock().setType(Material.CHEST);
                getLocation().getBlock().setData(chestData);
            } else {
                chestData = getLocation().getBlock().getData();
            }
        }
    }

    public void despawn(){
        despawn(true);
    }

    public void despawn(boolean respawn){
        if(isSpawned()){
            if(claimed != null){
                Player p = claimed;

                claimed = null;
                if(p.isOnline()) p.closeInventory();
            }

            getLocation().getBlock().setType(Material.AIR);
            getLocation().getWorld().playSound(getLocation(), Sound.ZOMBIE_WOODBREAK, 0.8f, 1f);
            ParticleEffect.FLAME.display(1F, 1F, 1F, 0.1F, 40, getLocation(), 150);
            ParticleEffect.FLAME.display(1F, 1F, 1F, 0.1F, 40, getLocation(), 150);

            //getLocation().getWorld().playSound(getLocation(), Sound.DIG_WOOD, 1f, 1f);
            //ParticleEffect.BLOCK_CRACK.display(new ParticleEffect.BlockData(Material.WOOD, (byte)0), 0f, 0f, 0f, 0f, 20, getLocation(), 600);

            spawned = false;
            if(respawn) startRespawnTask();
        }
    }

    public void startRespawnTask(){
        stopRespawnTask();

        respawnTask = new BukkitRunnable(){
            @Override
            public void run() {
                spawn();
            }
        }.runTaskLater(DungeonRPG.getInstance(), 10*60*20);
    }

    public void stopRespawnTask(){
        if(respawnTask != null){
            respawnTask.cancel();
            respawnTask = null;
        }
    }

    public boolean isSpawned(){
        return spawned;
    }
}
