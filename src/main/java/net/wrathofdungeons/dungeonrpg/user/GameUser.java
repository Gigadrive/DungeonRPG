package net.wrathofdungeons.dungeonrpg.user;

import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.MySQLManager;
import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonapi.user.User;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.event.CharacterCreationDoneEvent;
import net.wrathofdungeons.dungeonrpg.event.FinalDataLoadedEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
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

    public int getMaxCharacterAmount(){
        if(hasPermission(Rank.DONATOR)){
            return 8;
        } else {
            return 4;
        }
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

    public void addCharacter(RPGClass rpgClass){
        if(getCharacters().size() < getMaxCharacterAmount()){
            DungeonAPI.async(() -> {
                try {
                    int charID = 0;

                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `characters` (`uuid`,`class`,`location.world`,`location.x`,`location.y`,`location.z`,`location.yaw`,`location.pitch`) VALUES(?,?,?,?,?,?,?,?);", Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1,p.getUniqueId().toString());
                    ps.setString(2,rpgClass.toString());
                    ps.setString(3, DungeonRPG.getStartLocation().getWorld().getName());
                    ps.setDouble(4,DungeonRPG.getStartLocation().getX());
                    ps.setDouble(5,DungeonRPG.getStartLocation().getY());
                    ps.setDouble(6,DungeonRPG.getStartLocation().getZ());
                    ps.setFloat(7,DungeonRPG.getStartLocation().getYaw());
                    ps.setFloat(8,DungeonRPG.getStartLocation().getPitch());
                    ps.executeUpdate();

                    ResultSet rs = ps.getGeneratedKeys();
                    if(rs.first()){
                        charID = rs.getInt(1);
                    }

                    MySQLManager.getInstance().closeResources(rs,ps);

                    if(charID > 0){
                        Character character = new Character(charID);
                        getCharacters().add(character);

                        CharacterCreationDoneEvent event = new CharacterCreationDoneEvent(p,character);
                        Bukkit.getPluginManager().callEvent(event);
                    }
                } catch(Exception e){
                    e.printStackTrace();
                }
            });
        }
    }

    public void playCharacter(Character c){
        if(c != null && c.getOwner().toString().equals(p.getUniqueId().toString())){
            p.closeInventory();
            setCurrentCharacter(c);
            c.setLastLogin(new Timestamp(System.currentTimeMillis()));

            bukkitReset();
            p.teleport(c.getStoredLocation());
            updateLevelBar();
        }
    }

    public void updateLevelBar(){
        if(getCurrentCharacter() != null){
            p.setLevel(getCurrentCharacter().getLevel());
            // TODO: Show exp
        } else {
            p.setLevel(0);
            p.setExp(0);
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

                FinalDataLoadedEvent event = new FinalDataLoadedEvent(p);
                Bukkit.getPluginManager().callEvent(event);
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

        if(getCurrentCharacter() != null) getCurrentCharacter().saveData(p);
    }
}
