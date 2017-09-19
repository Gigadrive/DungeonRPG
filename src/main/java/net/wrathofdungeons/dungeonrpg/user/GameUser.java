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
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.party.Party;
import net.wrathofdungeons.dungeonrpg.skill.SkillValues;
import net.wrathofdungeons.dungeonrpg.util.FormularUtils;
import net.wrathofdungeons.dungeonrpg.util.WorldUtilities;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
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
    private SkillValues skillValues;

    private int hp = 20;

    private boolean init = false;
    public boolean __associateDamageWithSystem = true;
    private boolean attackCooldown = false;
    private boolean setupMode = false;
    public boolean mayActivateMobs = true;
    public boolean ignoreFistCheck = false;
    public boolean ignoreDamageCheck = false;
    public boolean onGround = true;

    public String currentCombo = "";
    public boolean canCastCombo = true;
    public int comboDelay = 0;

    private BukkitTask mpRegenTask;
    private BukkitTask hpRegenTask;

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

    public void startMPRegenTask(){
        if(getCurrentCharacter() != null && mpRegenTask == null){
            mpRegenTask = new BukkitRunnable(){
                @Override
                public void run() {
                    int mp = getMP();
                    mp += 1;
                    if(mp > getMaxMP()) mp = getMaxMP();

                    // TODO: Consider awakenings that increase or decresae MP regeneration

                    setMP(mp);
                }
            }.runTaskTimer(DungeonRPG.getInstance(),30,30);
        }
    }

    public void stopMPRegenTask(){
        if(mpRegenTask != null){
            mpRegenTask.cancel();
            mpRegenTask = null;
        }
    }

    public void startHPRegenTask(){
        if(getCurrentCharacter() != null && hpRegenTask == null){
            hpRegenTask = new BukkitRunnable(){
                @Override
                public void run() {
                    int hp = getHP();
                    hp += 5*getCurrentCharacter().getLevel();
                    if(hp > getMaxHP()) hp = getMaxHP();

                    // TODO: Consider awakenings that increase or decresae HP regeneration

                    setHP(hp);
                }
            }.runTaskTimer(DungeonRPG.getInstance(),50,50);
        }
    }

    public void stopHPRegenTask(){
        if(hpRegenTask != null){
            hpRegenTask.cancel();
            hpRegenTask = null;
        }
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

    public void updateClickComboBar(){
        if(!DungeonRPG.SHOW_HP_IN_ACTION_BAR){
            if(!currentCombo.equals("") && currentCombo.length() > 0){
                String left = ChatColor.GREEN + "Left";
                String middle = ChatColor.GREEN + "Middle";
                String right = ChatColor.GREEN + "Right";
                String unknown = ChatColor.GRAY + "???";
                String unknownPrime = ChatColor.GRAY.toString() + ChatColor.BOLD.toString() + ChatColor.UNDERLINE.toString() + "???" + ChatColor.RESET;
                String seperator = " " + ChatColor.DARK_GRAY + "-" + ChatColor.RESET + " ";

                String toSend = "";

                if(currentCombo.equals("L")) toSend = left + seperator + unknownPrime + seperator + unknown;
                if(currentCombo.equals("LR")) toSend = left + seperator + right + seperator + unknownPrime;
                if(currentCombo.equals("LRL")) toSend = left + seperator + right + seperator + left;
                if(currentCombo.equals("LRM")) toSend = left + seperator + right + seperator + middle;
                if(currentCombo.equals("LRR")) toSend = left + seperator + right + seperator + right;
                if(currentCombo.equals("LM")) toSend = left + seperator + middle + seperator + unknownPrime;
                if(currentCombo.equals("LML")) toSend = left + seperator + middle + seperator + left;
                if(currentCombo.equals("LMM")) toSend = left + seperator + middle + seperator + middle;
                if(currentCombo.equals("LMR")) toSend = left + seperator + middle + seperator + right;
                if(currentCombo.equals("LL")) toSend = left + seperator + left + seperator + unknownPrime;
                if(currentCombo.equals("LLL")) toSend = left + seperator + left + seperator + left;
                if(currentCombo.equals("LLM")) toSend = left + seperator + left + seperator + middle;
                if(currentCombo.equals("LLR")) toSend = left + seperator + left + seperator + right;
                if(currentCombo.equals("R")) toSend = right + seperator + unknownPrime + seperator + unknown;
                if(currentCombo.equals("RR")) toSend = right + seperator + right + seperator + unknownPrime;
                if(currentCombo.equals("RRL")) toSend = right + seperator + right + seperator + left;
                if(currentCombo.equals("RRM")) toSend = right + seperator + right + seperator + middle;
                if(currentCombo.equals("RRR")) toSend = right + seperator + right + seperator + right;
                if(currentCombo.equals("RM")) toSend = right + seperator + middle + seperator + unknownPrime;
                if(currentCombo.equals("RML")) toSend = right + seperator + middle + seperator + left;
                if(currentCombo.equals("RMM")) toSend = right + seperator + middle + seperator + middle;
                if(currentCombo.equals("RMR")) toSend = right + seperator + middle + seperator + right;
                if(currentCombo.equals("RL")) toSend = right + seperator + left + seperator + unknownPrime;
                if(currentCombo.equals("RLL")) toSend = right + seperator + left + seperator + left;
                if(currentCombo.equals("RLM")) toSend = right + seperator + left + seperator + middle;
                if(currentCombo.equals("RLR")) toSend = right + seperator + left + seperator + right;
                if(currentCombo.equals("M")) toSend = middle + seperator + unknownPrime + seperator + unknown;
                if(currentCombo.equals("MR")) toSend = middle + seperator + right + seperator + unknown;
                if(currentCombo.equals("MRL")) toSend = middle + seperator + right + seperator + left;
                if(currentCombo.equals("MRM")) toSend = middle + seperator + right + seperator + middle;
                if(currentCombo.equals("MRR")) toSend = middle + seperator + right + seperator + right;
                if(currentCombo.equals("MM")) toSend = middle + seperator + middle + seperator + unknownPrime;
                if(currentCombo.equals("MML")) toSend = middle + seperator + middle + seperator + left;
                if(currentCombo.equals("MMM")) toSend = middle + seperator + middle + seperator + middle;
                if(currentCombo.equals("MMR")) toSend = middle + seperator + middle + seperator + right;
                if(currentCombo.equals("ML")) toSend = middle + seperator + left + seperator + unknownPrime;
                if(currentCombo.equals("MLL")) toSend = middle + seperator + left + seperator + left;
                if(currentCombo.equals("MLM")) toSend = middle + seperator + left + seperator + middle;
                if(currentCombo.equals("MLR")) toSend = middle + seperator + left + seperator + right;

                BountifulAPI.sendActionBar(p, toSend);
            }
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

                p.getInventory().setHelmet(new CustomItem(14).build(p));
                p.getInventory().setChestplate(new CustomItem(10).build(p));

                if(DungeonRPG.ENABLE_BOWDRAWBACK){
                    p.getInventory().setItem(17,new CustomItem(6,64).build(p));
                }
            }

            setHP(getMaxHP());
            setMP(getMaxMP());
            checkRequirements();

            startMPRegenTask();
            startHPRegenTask();
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
                        if(a.getParty() != null && a.getParty().hasMember(p)){
                            t.setPrefix(ChatColor.GREEN.toString());
                        } else if(false){
                            // TODO: Add prefix for friends
                            t.setPrefix(ChatColor.AQUA.toString());
                        } else {
                            t.setPrefix(ChatColor.WHITE.toString());
                        }
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
        if(getCurrentCharacter() != null){
            DungeonAPI.sync(() -> {
                CustomItem helmet = null;
                CustomItem chestplate = null;
                CustomItem leggings = null;
                CustomItem boots = null;

                if(p.getInventory().getHelmet() != null && CustomItem.fromItemStack(p.getInventory().getHelmet()) != null) helmet = CustomItem.fromItemStack(p.getInventory().getHelmet());
                if(p.getInventory().getChestplate() != null && CustomItem.fromItemStack(p.getInventory().getChestplate()) != null) chestplate = CustomItem.fromItemStack(p.getInventory().getChestplate());
                if(p.getInventory().getLeggings() != null && CustomItem.fromItemStack(p.getInventory().getLeggings()) != null) leggings = CustomItem.fromItemStack(p.getInventory().getLeggings());
                if(p.getInventory().getBoots() != null && CustomItem.fromItemStack(p.getInventory().getBoots()) != null) boots = CustomItem.fromItemStack(p.getInventory().getBoots());

                if(helmet != null){
                    if(getCurrentCharacter().getLevel() < helmet.getData().getNeededLevel()){
                        if(p.getInventory().firstEmpty() != -1){
                            p.getInventory().setHelmet(null);
                            p.getInventory().addItem(helmet.build(p));
                        } else {
                            WorldUtilities.dropItem(p.getLocation(),helmet,p);
                        }

                        p.sendMessage(ChatColor.DARK_RED + "This helmet is for level " + helmet.getData().getNeededLevel() + "+ only.");
                    }

                    if(helmet.getData().getNeededClass() != RPGClass.NONE && !helmet.getData().getNeededClass().matches(getCurrentCharacter().getRpgClass())){
                        if(p.getInventory().firstEmpty() != -1){
                            p.getInventory().setHelmet(null);
                            p.getInventory().addItem(helmet.build(p));
                        } else {
                            WorldUtilities.dropItem(p.getLocation(),helmet,p);
                        }

                        p.sendMessage(ChatColor.DARK_RED + "This helmet is for a different class.");
                    }
                }

                if(chestplate != null){
                    if(getCurrentCharacter().getLevel() < chestplate.getData().getNeededLevel()){
                        if(p.getInventory().firstEmpty() != -1){
                            p.getInventory().setChestplate(null);
                            p.getInventory().addItem(chestplate.build(p));
                        } else {
                            WorldUtilities.dropItem(p.getLocation(),chestplate,p);
                        }

                        p.sendMessage(ChatColor.DARK_RED + "This chestplate is for level " + chestplate.getData().getNeededLevel() + "+ only.");
                    }

                    if(chestplate.getData().getNeededClass() != RPGClass.NONE && !chestplate.getData().getNeededClass().matches(getCurrentCharacter().getRpgClass())){
                        if(p.getInventory().firstEmpty() != -1){
                            p.getInventory().setChestplate(null);
                            p.getInventory().addItem(chestplate.build(p));
                        } else {
                            WorldUtilities.dropItem(p.getLocation(),chestplate,p);
                        }

                        p.sendMessage(ChatColor.DARK_RED + "This chestplate is for a different class.");
                    }
                }

                if(leggings != null){
                    if(getCurrentCharacter().getLevel() < leggings.getData().getNeededLevel()){
                        if(p.getInventory().firstEmpty() != -1){
                            p.getInventory().setLeggings(null);
                            p.getInventory().addItem(leggings.build(p));
                        } else {
                            WorldUtilities.dropItem(p.getLocation(),leggings,p);
                        }

                        p.sendMessage(ChatColor.DARK_RED + "These leggings are for level " + leggings.getData().getNeededLevel() + "+ only.");
                    }

                    if(leggings.getData().getNeededClass() != RPGClass.NONE && !leggings.getData().getNeededClass().matches(getCurrentCharacter().getRpgClass())){
                        if(p.getInventory().firstEmpty() != -1){
                            p.getInventory().setLeggings(null);
                            p.getInventory().addItem(leggings.build(p));
                        } else {
                            WorldUtilities.dropItem(p.getLocation(),leggings,p);
                        }

                        p.sendMessage(ChatColor.DARK_RED + "These leggings are for a different class.");
                    }
                }

                if(boots != null){
                    if(getCurrentCharacter().getLevel() < boots.getData().getNeededLevel()){
                        if(p.getInventory().firstEmpty() != -1){
                            p.getInventory().setBoots(null);
                            p.getInventory().addItem(boots.build(p));
                        } else {
                            WorldUtilities.dropItem(p.getLocation(),boots,p);
                        }

                        p.sendMessage(ChatColor.DARK_RED + "These boots are for level " + boots.getData().getNeededLevel() + "+ only.");
                    }

                    if(boots.getData().getNeededClass() != RPGClass.NONE && !boots.getData().getNeededClass().matches(getCurrentCharacter().getRpgClass())){
                        if(p.getInventory().firstEmpty() != -1){
                            p.getInventory().setBoots(null);
                            p.getInventory().addItem(boots.build(p));
                        } else {
                            WorldUtilities.dropItem(p.getLocation(),boots,p);
                        }

                        p.sendMessage(ChatColor.DARK_RED + "These boots are for a different class.");
                    }
                }
            });
        }
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

    public Party getParty(){
        return Party.getParty(p);
    }

    public double giveEXP(double xp){
        return giveEXP(xp,false);
    }

    public double giveEXP(double xp, boolean affectedByParty){
        if(getCurrentCharacter() != null){
            if(!getCurrentCharacter().mayGetEXP()) return 0;

            // TODO: Handle xp bonus

            if(!affectedByParty){
                if(getParty() != null){
                    ArrayList<GameUser> membersInRange = new ArrayList<GameUser>();

                    for(Entity e : p.getNearbyEntities(DungeonRPG.PARTY_EXP_RANGE,DungeonRPG.PARTY_EXP_RANGE,DungeonRPG.PARTY_EXP_RANGE)){
                        if(e instanceof Player){
                            Player p2 = (Player)e;

                            if(CustomEntity.fromEntity(p2) == null && GameUser.isLoaded(p2)){
                                GameUser u2 = GameUser.getUser(p2);

                                if(u2.getCurrentCharacter() != null && u2.getParty() != null){
                                    if(getParty().hasMember(p2)){
                                        membersInRange.add(u2);
                                    }
                                }
                            }
                        }
                    }

                    double xpPerMember = xp/membersInRange.size();

                    for(GameUser a : membersInRange){
                        a.giveEXP(xp,true);
                    }
                }
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

    public SkillValues getSkillValues() {
        return skillValues;
    }

    public void init(Player p){
        if(init) return;

        DungeonAPI.async(() -> {
            try {
                TEMP.remove(p);
                this.originalUser = User.getUser(p);
                this.p = p;

                this.characters = new ArrayList<Character>();
                this.skillValues = new SkillValues();
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
