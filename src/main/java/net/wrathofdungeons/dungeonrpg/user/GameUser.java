package net.wrathofdungeons.dungeonrpg.user;

import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.MySQLManager;
import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonapi.user.User;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.event.CharacterCreationDoneEvent;
import net.wrathofdungeons.dungeonrpg.event.FinalDataLoadedEvent;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.items.PlayerInventory;
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
        if(getCurrentCharacter() == null && c != null && c.getOwner().toString().equals(p.getUniqueId().toString())){
            p.closeInventory();
            setCurrentCharacter(c);
            DungeonRPG.updateVanishing();
            c.setLastLogin(new Timestamp(System.currentTimeMillis()));

            bukkitReset();
            p.teleport(c.getStoredLocation());
            updateLevelBar();

            PlayerInventory inventory = c.getStoredInventory();
            if(inventory != null){
                inventory.update();
                inventory.loadToPlayer(p);
            } else {
                if(c.getRpgClass() == RPGClass.ARCHER || c.getRpgClass() == RPGClass.HUNTER || c.getRpgClass() == RPGClass.RANGER){
                    p.getInventory().addItem(new CustomItem(1).build(p));
                } else if(c.getRpgClass() == RPGClass.MERCENARY || c.getRpgClass() == RPGClass.KNIGHT || c.getRpgClass() == RPGClass.SOLDIER){
                    p.getInventory().addItem(new CustomItem(2).build(p));
                } else if(c.getRpgClass() == RPGClass.MAGICIAN || c.getRpgClass() == RPGClass.WIZARD || c.getRpgClass() == RPGClass.ALCHEMIST){
                    p.getInventory().addItem(new CustomItem(3).build(p));
                } else if(c.getRpgClass() == RPGClass.ASSASSIN || c.getRpgClass() == RPGClass.NINJA || c.getRpgClass() == RPGClass.BLADEMASTER){
                    p.getInventory().addItem(new CustomItem(4).build(p));
                }

                p.getInventory().setItem(8,new CustomItem(5).build(p));
                p.getInventory().setItem(17,new CustomItem(6,64).build(p));
            }

            checkRequirements();
        }
    }

    public void updateVanishing(){
        for(Player all : Bukkit.getOnlinePlayers()){
            if(all == p) continue;
            GameUser a = GameUser.getUser(all);

            if(getCurrentCharacter() == null){
                p.hidePlayer(all);
            } else {
                if(a.getCurrentCharacter() == null){
                    p.hidePlayer(all);
                } else {
                    p.showPlayer(all);
                }
            }
        }
    }

    public void updateName(){

    }

    public void updateArrows(){
        if(getCurrentCharacter() != null) p.getInventory().setItem(17,new CustomItem(6,64).build(p));
    }

    public void checkRequirements(){
        // TODO: Check items for level and class requirements
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
