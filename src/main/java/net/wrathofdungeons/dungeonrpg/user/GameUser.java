package net.wrathofdungeons.dungeonrpg.user;

import net.wrathofdungeons.dungeonapi.user.User;
import net.wrathofdungeons.dungeonapi.util.Util;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class GameUser extends User {
    public static HashMap<Player,GameUser> TEMP = new HashMap<Player,GameUser>();

    public static GameUser getUser(Player p){
        if(User.isLoaded(p)){
            return ((GameUser)User.STORAGE.get(p));
        } else {
            return null;
        }
    }

    public static void load(Player p){
        if(!User.isLoaded(p)){
            new GameUser(p);
        }
    }

    private User originalUser;
    private Player p;
    private boolean init = false;

    public GameUser(Player p){
        super(p);

        TEMP.put(p,this);
    }

    public void init(Player p){
        TEMP.remove(p);
        this.originalUser = User.getUser(p);
        this.p = p;

        User.STORAGE.remove(p);
        User.STORAGE.put(p,this);
    }

    public void saveData(){
        super.saveData();
    }
}
