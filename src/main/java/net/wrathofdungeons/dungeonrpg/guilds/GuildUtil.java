package net.wrathofdungeons.dungeonrpg.guilds;

import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.MySQLManager;
import net.wrathofdungeons.dungeonapi.util.PlayerUtilities;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

public class GuildUtil {
    private static boolean nameLocalCheck(String name){
        for(Guild g : Guild.STORAGE.values()) if(g.getName().equalsIgnoreCase(name)) return false;

        return true;
    }

    private static boolean nameDBCheck(String name){
        boolean b = true;

        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `guilds` WHERE `name` = ?");
            ps.setString(1,name);
            ResultSet rs = ps.executeQuery();

            b = !rs.first();

            MySQLManager.getInstance().closeResources(rs,ps);
        } catch(Exception e){
            e.printStackTrace();
        }

        return b;
    }

    private static boolean nameReservedCheck(String name){
        boolean b = true;

        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `reservedGuildNames` WHERE `name` = ?");
            ps.setString(1,name);
            ResultSet rs = ps.executeQuery();

            b = !rs.first();

            MySQLManager.getInstance().closeResources(rs,ps);
        } catch(Exception e){
            e.printStackTrace();
        }

        return b;
    }

    public static boolean isNameAvailable(String name){
        return nameLocalCheck(name) && nameDBCheck(name) && nameReservedCheck(name);
    }

    private static boolean tagLocalCheck(String tag){
        for(Guild g : Guild.STORAGE.values()) if(g.getTag().equalsIgnoreCase(tag)) return false;

        return true;
    }

    private static boolean tagDBCheck(String tag){
        boolean b = true;

        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `guilds` WHERE `tag` = ?");
            ps.setString(1,tag);
            ResultSet rs = ps.executeQuery();

            b = !rs.first();

            MySQLManager.getInstance().closeResources(rs,ps);
        } catch(Exception e){
            e.printStackTrace();
        }

        return b;
    }

    public static boolean isTagAvailable(String tag){
        return tagLocalCheck(tag) && tagDBCheck(tag);
    }

    public static void releaseReservedName(String name){
        DungeonAPI.async(() -> {
            try {
                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("DELETE FROM `reservedGuildNames` WHERE `name` = ?");
                ps.setString(1,name);
                ps.executeUpdate();
                ps.close();
            } catch(Exception e){
                e.printStackTrace();
            }
        });
    }

    public static Guild getGuildByPlayer(String name){
        UUID uuid = PlayerUtilities.getUUIDFromName(name);

        if(uuid != null) return getGuildByPlayer(uuid);

        return null;
    }

    public static Guild getGuildByPlayer(Player p){
        if(GameUser.isLoaded(p)){
            return GameUser.getUser(p).getGuild();
        } else {
            return getGuildByPlayer(p.getUniqueId());
        }
    }

    public static Guild getGuildByPlayer(UUID uuid){
        Player p = Bukkit.getPlayer(uuid);

        if(p != null && GameUser.isLoaded(p)){
            return getGuildByPlayer(p);
        } else {
            Guild g = null;

            try {
                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `users` WHERE `uuid` = ?");
                ps.setString(1,p.getUniqueId().toString());

                ResultSet rs = ps.executeQuery();
                if(rs.first()){
                    int guildID = rs.getInt("guildID");

                    if(guildID > 0) g = Guild.getGuild(guildID);
                }

                MySQLManager.getInstance().closeResources(rs,ps);
            } catch(Exception e){
                e.printStackTrace();
            }

            return g;
        }
    }

    public static void clearInvites(Player p){
        for(Guild g : Guild.STORAGE.values()) if(g.getInvitedPlayers().contains(p)) g.getInvitedPlayers().remove(p);
    }
}
