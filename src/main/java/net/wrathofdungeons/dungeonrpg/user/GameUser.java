package net.wrathofdungeons.dungeonrpg.user;

import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.MySQLManager;
import net.wrathofdungeons.dungeonapi.user.User;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
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
    private ArrayList<Character> characters;
    private Character currentCharacter;

    private boolean init = false;

    public GameUser(Player p){
        super(p);

        TEMP.put(p,this);
    }

    public ArrayList<Character> getCharacters() {
        return characters;
    }

    public Character getCurrentCharacter() {
        return currentCharacter;
    }

    public void setCurrentCharacter(Character c){
        if(c != null){
            if(getCharacters().contains(c)){
                this.currentCharacter = c;
            }
        } else {
            this.currentCharacter = null;
        }
    }

    public void init(Player p){
        if(init) return;

        DungeonAPI.async(() -> {
            try {
                TEMP.remove(p);
                this.originalUser = User.getUser(p);
                this.p = p;

                this.characters = new ArrayList<Character>();
                loadCharacters();

                User.STORAGE.remove(p);
                User.STORAGE.put(p,this);
                init = true;
            } catch(Exception e){
                e.printStackTrace();
            }
        });
    }

    private void loadCharacters(){
        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `characters` WHERE `uuid` = ?");
            ps.setString(1,p.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                this.characters.add(new Character(rs.getInt("id")));
            }

            MySQLManager.getInstance().closeResources(rs,ps);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void saveData(){
        super.saveData();

        if(getCurrentCharacter() != null) getCurrentCharacter().saveData();
    }
}
