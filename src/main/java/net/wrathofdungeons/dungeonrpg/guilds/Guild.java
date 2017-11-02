package net.wrathofdungeons.dungeonrpg.guilds;

import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.MySQLManager;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

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

    private int id;

    public Guild(int id){
        if(STORAGE.containsKey(id)) return;

        this.id = id;

        DungeonAPI.async(() -> {
            try {
                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `guilds` WHERE `id` = ?");
                ps.setInt(1,id);

                ResultSet rs = ps.executeQuery();
                if(rs.first()){


                    STORAGE.put(id,this);
                }

                MySQLManager.getInstance().closeResources(rs,ps);
            } catch(Exception e){
                e.printStackTrace();
            }
        });
    }

    public void handleDisconnect(Player p){

    }
}
