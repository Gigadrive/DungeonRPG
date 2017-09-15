package net.wrathofdungeons.dungeonrpg.user;

import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.MySQLManager;
import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonapi.user.User;
import net.wrathofdungeons.dungeonapi.util.BountifulAPI;
import net.wrathofdungeons.dungeonapi.util.ItemUtil;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.event.CharacterCreationDoneEvent;
import net.wrathofdungeons.dungeonrpg.event.FinalDataLoadedEvent;
import net.wrathofdungeons.dungeonrpg.inv.CharacterSelectionMenu;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.items.PlayerInventory;
import net.wrathofdungeons.dungeonrpg.util.FormularUtils;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

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

    private int hp = 20;

    private boolean init = false;
    public boolean __associateDamageWithSystem = true;
    private boolean attackCooldown = false;
    private boolean setupMode = false;
    public String currentCombo = "";
    public boolean mayActivateMobs = true;
    public boolean ignoreFistCheck = false;

    public GameUser(Player p){
        super(p);

        TEMP.put(p,this);
    }

    public int getHP(){
        return this.hp;
    }

    public void setHP(int hp){
        this.hp = hp;
        if(this.hp > getMaxHP()) this.hp = getMaxHP();
        updateHPBar();
    }

    public int getMaxHP(){
        int maxHP = 0;

        if(getCurrentCharacter() != null){
            Character c = getCurrentCharacter();

            maxHP += FormularUtils.getBaseHP(c);
            // TODO: Add health values from gear
        } else {
            maxHP = 20;
        }

        return maxHP;
    }

    public int getHPPercentage(){
        return (getHP()/getMaxHP())*100;
    }

    public int getMP(){
        return p.getFoodLevel();
    }

    public void setMP(int mp){
        if(mp > getMaxMP()) mp = getMaxMP();
        p.setFoodLevel(mp);
        updateHPBar();
    }

    public int getMaxMP(){
        return 20;
    }

    public int getMPPercentage(){
        return (getMP()/getMaxMP())*100;
    }

    public void updateHPBar(){
        if(!p.isDead()){
            if(DungeonRPG.SHOW_HP_IN_ACTION_BAR) BountifulAPI.sendActionBar(p, ChatColor.DARK_RED + "HP: " + ChatColor.RED + getHP() + "/" + getMaxHP() + "       " + ChatColor.BLUE + "MP: " + ChatColor.AQUA + getMP() + "/" + getMaxMP());
            p.setMaxHealth(20);

            if(hp > getMaxHP()) hp = getMaxHP();
            if(hp < 0) hp = 0;
            double healthDis = (((double)hp)/((double)getMaxHP()))*20;
            if(healthDis > p.getMaxHealth()) healthDis = p.getMaxHealth();

            p.setHealth(healthDis);
        }
    }

    @Deprecated
    public void updateMPBar(){
        updateHPBar();
    }

    public boolean isInAttackCooldown(){
        return attackCooldown;
    }

    public void setAttackCooldown(boolean b){
        this.attackCooldown = b;
    }

    public void damage(double damage){
        damage(damage,null);
    }

    public void damage(double damage, LivingEntity source){
        this.hp -= damage;

        if(this.hp <= 0){
            if(source != null){
                __associateDamageWithSystem = false;
                p.damage(((Damageable)p).getMaxHealth(), source);

                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonAPI.getInstance(), new Runnable(){
                    public void run(){
                        __associateDamageWithSystem = true;
                    }
                });
            } else {
                p.damage(((Damageable)p).getMaxHealth());
            }
        }

        updateHPBar();
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
                    p.sendMessage(ChatColor.RED + "An error occurred!");
                    e.printStackTrace();
                    CharacterSelectionMenu.CREATING.remove(p);
                }
            });
        }
    }

    public void deleteCharacter(Character c){
        if(c != null && c.getOwner().toString().equals(p.getUniqueId().toString())){
            CharacterSelectionMenu.CREATING.add(p);
            p.closeInventory();

            DungeonAPI.async(() -> {
                p.sendMessage(ChatColor.GRAY + "Deleting character..");

                try {
                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("DELETE FROM `characters` WHERE `id` = ?");
                    ps.setInt(1,c.getId());
                    ps.executeUpdate();
                    ps.close();

                    getCharacters().remove(c);

                    CharacterSelectionMenu.CREATING.remove(p);
                    CharacterSelectionMenu.openSelection(p);
                    p.sendMessage(ChatColor.GREEN + "Done!");
                } catch(Exception e){
                    p.sendMessage(ChatColor.RED + "An error occurred!");
                    e.printStackTrace();
                    CharacterSelectionMenu.CREATING.remove(p);
                }
            });
        }
    }

    public void playCharacter(Character c){
        if(getCurrentCharacter() == null && c != null && c.getOwner().toString().equals(p.getUniqueId().toString())){
            p.closeInventory();
            setCurrentCharacter(c);
            DungeonRPG.updateVanishing();
            DungeonRPG.updateNames();
            c.setLastLogin(new Timestamp(System.currentTimeMillis()));

            bukkitReset();
            p.teleport(c.getStoredLocation());
            updateLevelBar();

            PlayerInventory inventory = c.getStoredInventory();
            if(inventory != null){
                inventory.loadToPlayer(p);

                if(DungeonRPG.ENABLE_BOWDRAWBACK){
                    p.getInventory().setItem(17,new CustomItem(6,64).build(p));
                } else {
                    if(CustomItem.fromItemStack(p.getInventory().getItem(17)) != null && CustomItem.fromItemStack(p.getInventory().getItem(17)).getData().getId() == 6) p.getInventory().setItem(17,new ItemStack(Material.AIR));
                }
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

                if(DungeonRPG.ENABLE_BOWDRAWBACK){
                    p.getInventory().setItem(17,new CustomItem(6,64).build(p));
                }
            }

            setHP(getMaxHP());
            setMP(getMaxMP());
            checkRequirements();
        }
    }

    public void updateVanishing(){
        for(Player all : Bukkit.getOnlinePlayers()){
            if(all == p) continue;

            if(GameUser.isLoaded(all)){
                GameUser a = GameUser.getUser(all);

                if(getCurrentCharacter() == null && !isInSetupMode()){
                    p.hidePlayer(all);
                } else {
                    if(a.getCurrentCharacter() == null){
                        p.hidePlayer(all);
                    } else {
                        p.showPlayer(all);
                    }
                }
            } else {
                p.hidePlayer(all);
            }
        }
    }

    public void updateName(){
        for(Player all : Bukkit.getOnlinePlayers()){
            if(GameUser.isLoaded(all)){
                GameUser a = GameUser.getUser(all);
                Scoreboard b = a.getScoreboard();
                Team t = b.getTeam(p.getName());
                if(t == null) t = b.registerNewTeam(p.getName());
                t.addEntry(p.getName());

                if(getCurrentCharacter() != null){
                    if(all != p){
                        // TODO: Add colors for friends and party
                        t.setPrefix(ChatColor.WHITE.toString());
                    } else {
                        t.setPrefix(ChatColor.YELLOW.toString());
                    }
                } else {
                    t.setPrefix(ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH.toString());
                }
            } else {

            }
        }
    }

    public void updateArrows(){
        if(getCurrentCharacter() != null) p.getInventory().setItem(17,new CustomItem(6,64).build(p));
    }

    public void checkRequirements(){
        // TODO: Check items for level and class requirements
    }

    public boolean isInSetupMode() {
        return setupMode;
    }

    public void setSetupMode(boolean setupMode) {
        this.setupMode = setupMode;

        if(this.setupMode){
            if(getCurrentCharacter() != null) getCurrentCharacter().saveData(p,false,false);

            setCurrentCharacter(null);
            bukkitReset();
            p.setGameMode(GameMode.CREATIVE);
            p.sendMessage(ChatColor.GREEN + "You are now in setup mode!");

            p.getInventory().addItem(ItemUtil.namedItem(Material.STICK,"Mob Spawn Setter",null));
            p.getInventory().addItem(ItemUtil.namedItem(Material.STICK,"Mob Activation Setter (1)",null));
            p.getInventory().addItem(ItemUtil.namedItem(Material.STICK,"Mob Activation Setter (2)",null));
        } else {
            bukkitReset();
            setCurrentCharacter(null);
            p.teleport(DungeonRPG.getCharSelLocation());
            CharacterSelectionMenu.openSelection(p);
            p.sendMessage(ChatColor.RED + "You are no longer in setup mode!");
        }
    }

    public void updateLevelBar(){
        if(getCurrentCharacter() != null){
            p.setLevel(getCurrentCharacter().getLevel());
            p.setExp((float)((double)(getCurrentCharacter().getExp()/FormularUtils.getExpNeededForLevel(getCurrentCharacter().getLevel()+1))));
        } else {
            p.setLevel(0);
            p.setExp(0);
        }
    }

    public double giveEXP(double xp){
        return giveEXP(xp,false);
    }

    public double giveEXP(double xp, boolean affectedByParty){
        if(getCurrentCharacter() != null){
            if(!getCurrentCharacter().mayGetEXP()) return 0;

            // TODO: Handle xp bonus

            if(!affectedByParty){
                // TODO: give exp to party members
            }

            getCurrentCharacter().setExp(getCurrentCharacter().getExp()+xp);

            checkLevelUp();
            updateLevelBar();

            return xp;
        } else {
            return 0;
        }
    }

    public void checkLevelUp(){
        if(getCurrentCharacter() != null){
            int levels = 0;
            double required = 0;

            while((getCurrentCharacter().getExp() >= (required = FormularUtils.getExpNeededForLevel(getCurrentCharacter().getLevel() + levels))) && (getCurrentCharacter().getLevel() + levels < DungeonRPG.getMaxLevel())){
                getCurrentCharacter().setExp(getCurrentCharacter().getExp()-required);
                levels++;
            }

            if(levels > 0){
                levelUp(levels);
            }

            updateLevelBar();
        }
    }

    public void levelUp(){
        levelUp(1);
    }

    public void levelUp(int times){
        if(getCurrentCharacter() != null){
            for (int i = 0; i < times; i++) {
                if(getCurrentCharacter().mayGetEXP()){
                    getCurrentCharacter().setLevel(getCurrentCharacter().getLevel()+1);
                    // TODO: Give stat points
                    /*setLeftStatpoints(getLeftStatpoints()+2);
                    statpointsGained += 2;*/
                }
            }

            setHP(getMaxHP());
            setMP(getMaxMP());

            Firework f = (Firework)getPlayer().getLocation().getWorld().spawn(getPlayer().getLocation(), Firework.class);
            FireworkMeta fm = f.getFireworkMeta();
            fm.addEffect(FireworkEffect.builder().flicker(false).trail(true).with(FireworkEffect.Type.CREEPER).withColor(org.bukkit.Color.GREEN).withFade(org.bukkit.Color.BLUE).build());
            fm.setPower(3);
            f.setFireworkMeta(fm);

            if(getCurrentCharacter().getLevel() >= DungeonRPG.getMaxLevel()) getCurrentCharacter().setExp(0);
            updateLevelBar();
        }
    }

    public void updateInventory(){
        if(getCurrentCharacter() != null) getCurrentCharacter().getConvertedInventory(p).loadToPlayer(p);
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
        saveData(false);
    }

    public void saveData(boolean continueCharsel){
        if(!Bukkit.getPluginManager().isPluginEnabled(DungeonRPG.getInstance())){
            if(getCurrentCharacter() != null && !setupMode) getCurrentCharacter().saveData(p,continueCharsel,false);
        } else {
            if(getCurrentCharacter() != null && !setupMode) getCurrentCharacter().saveData(p,continueCharsel);
        }

        super.saveData();
    }
}
