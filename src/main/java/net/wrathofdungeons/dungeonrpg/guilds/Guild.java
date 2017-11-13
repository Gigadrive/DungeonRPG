package net.wrathofdungeons.dungeonrpg.guilds;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.MySQLManager;
import net.wrathofdungeons.dungeonapi.util.PlayerUtilities;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Guild {
    public static HashMap<Integer,Guild> STORAGE = new HashMap<Integer,Guild>();

    public static Guild getGuild(int id){
        if(STORAGE.containsKey(id)){
            return STORAGE.get(id);
        } else {
            new Guild(id);

            return STORAGE.getOrDefault(id,null);
        }
    }

    public static boolean isLoaded(int id){
        return STORAGE.containsKey(id);
    }

    private int id;
    private String name;
    private String tag;
    private UUID creator;
    private Timestamp created;
    private ArrayList<GuildMember> members;
    private int level;
    private double exp;

    private ArrayList<Player> invitedPlayers;

    public Guild(int id){
        if(STORAGE.containsKey(id)) return;

        Gson gson = DungeonAPI.GSON;
        this.id = id;
        this.invitedPlayers = new ArrayList<Player>();

        DungeonAPI.async(() -> {
            try {
                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `guilds` WHERE `id` = ?");
                ps.setInt(1,id);

                ResultSet rs = ps.executeQuery();
                if(rs.first()){
                    this.id = rs.getInt("id");
                    this.name = rs.getString("name");
                    this.tag = rs.getString("tag");
                    if(rs.getString("creator") != null) this.creator = UUID.fromString(rs.getString("creator"));
                    this.created = rs.getTimestamp("created");
                    if(rs.getString("members") != null){
                        this.members = gson.fromJson(rs.getString("members"),new TypeToken<ArrayList<GuildMember>>(){}.getType());
                    } else {
                        this.members = new ArrayList<GuildMember>();
                    }

                    STORAGE.put(id,this);
                }

                MySQLManager.getInstance().closeResources(rs,ps);
            } catch(Exception e){
                e.printStackTrace();
            }
        });
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTag() {
        return tag;
    }

    public UUID getCreator() {
        return creator;
    }

    public Timestamp getCreated() {
        return created;
    }

    public ArrayList<GuildMember> getMembers() {
        return members;
    }

    public GuildMember toGuildMember(Player p){
        return toGuildMember(p.getUniqueId());
    }

    public GuildMember toGuildMember(UUID uuid){
        for(GuildMember m : getMembers()){
            if(m.getUUID().toString().equals(uuid.toString())) return m;
        }

        return null;
    }

    public GuildMember toGuildMember(String name){
        UUID uuid = PlayerUtilities.getUUIDFromName(name);

        if(uuid != null) return toGuildMember(uuid);

        return null;
    }

    public GuildRank getRank(Player p){
        GuildMember m = toGuildMember(p);

        if(m != null) return m.getRank();

        return null;
    }

    public GuildRank getRank(UUID uuid){
        GuildMember m = toGuildMember(uuid);

        if(m != null) return m.getRank();

        return null;
    }

    public GuildRank getRank(String name){
        GuildMember m = toGuildMember(name);

        if(m != null) return m.getRank();

        return null;
    }

    public boolean isInGuild(Player p){
        return isInGuild(p.getUniqueId());
    }

    public boolean isInGuild(String name){
        UUID uuid = PlayerUtilities.getUUIDFromName(name);

        if(uuid != null) return isInGuild(uuid);

        return false;
    }

    public GuildMember getLeader(){
        return getMembers(GuildRank.LEADER).length > 0 ? getMembers(GuildRank.LEADER)[0] : null;
    }

    public GuildMember[] getMembers(GuildRank rank){
        ArrayList<GuildMember> a = new ArrayList<GuildMember>();
        for(GuildMember m : getMembers()) if(m.getRank() == rank) a.add(m);

        return a.toArray(new GuildMember[]{});
    }

    public ArrayList<Player> getInvitedPlayers() {
        return invitedPlayers;
    }

    public void sendMessage(String message){
        sendMessage(message,GuildRank.MEMBER);
    }

    public void sendMessage(String message,GuildRank minRank){
        DungeonAPI.executeBungeeCommand("BungeeConsole","guildaction " + "NONE" + " " + getId() + " message " + minRank.toString() + " " + message);
    }

    public boolean isInGuild(UUID uuid){
        return toGuildMember(uuid) != null;
    }

    public void reloadMembers(){
        try {
            Gson gson = DungeonAPI.GSON;

            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `guilds` WHERE `id` = ?");
            ps.setInt(1,getId());
            ResultSet rs = ps.executeQuery();

            if(rs.first()){
                if(rs.getString("members") != null){
                    this.members = gson.fromJson(rs.getString("members"),new TypeToken<ArrayList<GuildMember>>(){}.getType());
                } else {
                    this.members = new ArrayList<GuildMember>();
                }
            }

            MySQLManager.getInstance().closeResources(rs,ps);

            for(Player p : Bukkit.getOnlinePlayers()){
                if(GameUser.isLoaded(p)){
                    GameUser u = GameUser.getUser(p);

                    if(u.getGuildID() == getId() && !isInGuild(p)) u.setGuildID(0);
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void unregister(){
        STORAGE.remove(id);
    }

    public void saveMembers(){
        try {
            Gson gson = DungeonAPI.GSON;

            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("UPDATE `guilds` SET `members` = ? WHERE `id` = ?");
            ps.setString(1,gson.toJson(getMembers()));
            ps.setInt(2,getId());
            ps.executeUpdate();
            ps.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
