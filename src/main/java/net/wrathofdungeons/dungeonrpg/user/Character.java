package net.wrathofdungeons.dungeonrpg.user;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.MySQLManager;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.StatPointType;
import net.wrathofdungeons.dungeonrpg.dungeon.DungeonType;
import net.wrathofdungeons.dungeonrpg.inv.CharacterSelectionMenu;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.items.ItemData;
import net.wrathofdungeons.dungeonrpg.items.PlayerInventory;
import net.wrathofdungeons.dungeonrpg.items.awakening.AwakeningType;
import net.wrathofdungeons.dungeonrpg.quests.*;
import net.wrathofdungeons.dungeonrpg.skill.ClickComboType;
import net.wrathofdungeons.dungeonrpg.skill.Skill;
import net.wrathofdungeons.dungeonrpg.skill.SkillStorage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;

public class Character {
    private int id;
    private Player p;
    private RPGClass rpgClass;
    private int level;
    private double exp;
    private int strength;
    private int stamina;
    private int intelligence;
    private int dexterity;
    private int agility;
    private int statpointsLeft;
    private Location storedLocation;
    private ArrayList<QuestProgress> questProgress;
    private UserVariables variables;
    private PlayerInventory storedInventory;
    private Timestamp creationTime;
    private Timestamp lastLogin;
    private Inventory bank;
    private int playtime;
    private Timestamp lastPlaytimeSave;

    public Character(int id, Player p){
        this.id = id;
        Gson gson = DungeonAPI.GSON;

        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `characters` WHERE `id` = ?");
            ps.setInt(1,id);

            ResultSet rs = ps.executeQuery();
            if(rs.first()){
                this.p = p;
                this.rpgClass = RPGClass.valueOf(rs.getString("class"));
                this.level = rs.getInt("level");
                this.exp = rs.getDouble("exp");
                this.strength = rs.getInt("statpoints.str");
                this.stamina = rs.getInt("statpoints.sta");
                this.intelligence = rs.getInt("statpoints.int");
                this.dexterity = rs.getInt("statpoints.dex");
                this.agility = rs.getInt("statpoints.agi");
                this.statpointsLeft = rs.getInt("statpoints.left");

                if(Bukkit.getWorld(rs.getString("location.world")) != null && !rs.getString("location.world").startsWith("dungeonTemplate_")){
                    this.storedLocation = new Location(Bukkit.getWorld(rs.getString("location.world")),rs.getDouble("location.x"),rs.getDouble("location.y"),rs.getDouble("location.z"),rs.getFloat("location.yaw"),rs.getFloat("location.pitch"));
                } else {
                    DungeonType type = DungeonType.fromWorldName(rs.getString("location.world"));

                    if(type != null){
                        this.storedLocation = type.getPortalEntranceLocation();
                    } else {
                        this.storedLocation = DungeonRPG.sortedTownLocations().get(Util.randomInteger(0,DungeonRPG.sortedTownLocations().size()-1)).toBukkitLocation();
                    }
                }

                if(rs.getString("variables") != null){
                    this.variables = gson.fromJson(rs.getString("variables"),UserVariables.class);
                } else {
                    this.variables = new UserVariables();

                    initSkills();
                }

                if(rs.getString("questProgress") != null){
                    this.questProgress = gson.fromJson(rs.getString("questProgress"),new TypeToken<ArrayList<QuestProgress>>(){}.getType());
                } else {
                    this.questProgress = new ArrayList<QuestProgress>();
                }
                if(rs.getString("inventory") != null) this.storedInventory = PlayerInventory.fromString(rs.getString("inventory"));
                this.creationTime = rs.getTimestamp("time");
                this.lastLogin = rs.getTimestamp("lastLogin");
                this.playtime = rs.getInt("playtime");

                this.bank = Bukkit.createInventory(null,getBankSlots(),"Your Bank");

                if(lastLogin == null) storedLocation = DungeonRPG.getStartLocation();
            }

            MySQLManager.getInstance().closeResources(rs,ps);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private void initSkills(){
        Skill firstSkill = null;

        if(getRpgClass().matches(RPGClass.ARCHER)){
            firstSkill = SkillStorage.getInstance().getSkill("DartRain");
        } else if(getRpgClass().matches(RPGClass.ASSASSIN)){
            firstSkill = SkillStorage.getInstance().getSkill("StabbingStorm");
        } else if(getRpgClass().matches(RPGClass.MAGICIAN)){
            firstSkill = SkillStorage.getInstance().getSkill("FlameBurst");
        } else if(getRpgClass().matches(RPGClass.MERCENARY)){
            firstSkill = SkillStorage.getInstance().getSkill("AxeBlast");
        }

        if(firstSkill != null){
            this.variables.setSkillForCombo(ClickComboType.COMBO_1,firstSkill);
            this.variables.setInvestedSkillPoints(firstSkill,1);
        }
    }

    public int getId() {
        return id;
    }

    public Player getPlayer() {
        return p;
    }

    public RPGClass getRpgClass() {
        return rpgClass;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level){
        this.level = level;
    }

    public double getExp() {
        return exp;
    }

    public void setExp(double exp){
        this.exp = exp;
    }

    public Location getStoredLocation() {
        return storedLocation;
    }

    public PlayerInventory getStoredInventory() {
        return storedInventory;
    }

    public PlayerInventory getConvertedInventory(Player p){
        return PlayerInventory.fromInventory(p,this);
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public Timestamp getLastLogin() {
        return lastLogin;
    }

    public int getPlaytime() {
        return playtime+playtimeToAdd();
    }

    private int playtimeToAdd(){
        Timestamp l = lastPlaytimeSave;
        if(l == null) l = new Timestamp(System.currentTimeMillis());

        Timestamp now = new Timestamp(System.currentTimeMillis());

        return (int)((now.getTime()-l.getTime())/1000);
    }

    public boolean mayGetEXP(){
        if(this.level >= DungeonRPG.getMaxLevel()){
            return false;
        } else {
            return true;
        }
    }

    public int getStatpointsPure(StatPointType type){
        switch(type){
            case STRENGTH: return strength;
            case STAMINA: return stamina;
            case INTELLIGENCE: return intelligence;
            case DEXTERITY: return dexterity;
            case AGILITY: return agility;
            default: return 0;
        }
    }

    public int getStatpointsArtificial(StatPointType type){
        if(type == StatPointType.STRENGTH){
            return getTotalValue(AwakeningType.STR_BONUS);
        } else if(type == StatPointType.STAMINA){
            return getTotalValue(AwakeningType.STA_BONUS);
        } else if(type == StatPointType.INTELLIGENCE){
            return getTotalValue(AwakeningType.INT_BONUS);
        } else if(type == StatPointType.DEXTERITY){
            return getTotalValue(AwakeningType.DEX_BONUS);
        } else if(type == StatPointType.AGILITY){
            return getTotalValue(AwakeningType.AGI_BONUS);
        } else {
            return 0;
        }
    }

    public int getStatpointsTotal(StatPointType type){
        int total = getStatpointsPure(type)+getStatpointsArtificial(type);

        return total < 0 ? 0 : total;
    }

    public int getStatpointsLeft(){
        return statpointsLeft;
    }

    public void addStatpointsLeft(int i){
        this.statpointsLeft += i;
        if(this.statpointsLeft < 0) this.statpointsLeft = 0;
    }

    public void reduceStatpointsLeft(int i){
        if(this.statpointsLeft-i >= 0){
            this.statpointsLeft -= i;
        }
    }

    public int getTotalValue(AwakeningType type){
        int i = 0;

        for(CustomItem item : getEquipment()){
            if(item.hasAwakening(type)) i += item.getAwakeningValue(type);
            if(item.getData().hasAdditionalStat(type)) i += item.getData().getAdditionalStatValue(type);
            if(item.hasCrystalValue(type)) i += item.getCrystalValue(type);
        }

        return i;
    }

    public long getAttackSpeedTicks(){
        return 10;
    }

    public void addStatpoint(StatPointType type){
        addStatpoints(type,1);
    }

    public void addStatpoints(StatPointType type, int statpoints){
        switch(type){
            case STRENGTH:
                strength += statpoints;
                break;
            case STAMINA:
                stamina += statpoints;
                break;
            case INTELLIGENCE:
                intelligence += statpoints;
                break;
            case DEXTERITY:
                dexterity += statpoints;
                break;
            case AGILITY:
                agility += statpoints;
                break;
        }
    }

    public Inventory getBank() {
        return bank;
    }

    public int getBankRows(){
        if(getLevel() <= 9){
            return 1;
        } else if(getLevel() >= 10 && getLevel() <= 19){
            return 2;
        } else if(getLevel() >= 20 && getLevel() <= 29){
            return 3;
        } else if(getLevel() >= 30 && getLevel() <= 39){
            return 4;
        } else if(getLevel() >= 40 && getLevel() <= 49){
            return 5;
        } else if(getLevel() >= 50){
            return 6;
        }

        return 1;
    }

    public boolean mayEnterDungeon(DungeonType type){
        return getVariables().getLastDungeonEntry(type) == null || (new Timestamp(getVariables().getLastDungeonEntry(type).getTime() + DungeonRPG.DUNGEON_ENTRANCE_INTERVAL).before(new Timestamp(System.currentTimeMillis())));
    }

    private void registerProgress(Quest q){
        QuestProgress p = new QuestProgress();
        p.questID = q.getId();
        p.status = QuestProgressStatus.NOT_STARTED;
        questProgress.add(p);
    }

    public QuestProgress getProgress(Quest q){
        for(QuestProgress p : questProgress){
            if(p.questID == q.getId()) return p;
        }

        registerProgress(q);
        return getProgress(q);
    }

    public int getCurrentStage(Quest q){
        if(getProgress(q).status == QuestProgressStatus.STARTED){
            return getProgress(q).questStage;
        } else {
            return -1;
        }
    }

    public QuestObjectiveProgress getObjectiveProgress(Quest q, QuestObjective o){
        if(getCurrentStage(q) < q.getStages().length && getCurrentStage(q) >= 0){
            QuestStage stage = q.getStages()[getCurrentStage(q)];

            int objectiveIndex = Arrays.asList(stage.objectives).indexOf(o);
            if(objectiveIndex >= 0){
                for(QuestObjectiveProgress p : getProgress(q).objectiveProgress){
                    if(p.objectiveIndex == objectiveIndex) return p;
                }
            }
        }

        return null;
    }

    public void setQuestStatus(Quest q, QuestProgressStatus status){
        getProgress(q).status = status;
    }

    public QuestProgressStatus getStatus(Quest q){
        return getProgress(q).status;
    }

    public void setCurrentStage(Quest q, int stageIndex){
        if(stageIndex < q.getStages().length && stageIndex >= 0){
            QuestStage s = q.getStages()[stageIndex];

            if(s != null){
                getProgress(q).questStage = stageIndex;

                if(getProgress(q).objectiveProgress == null) getProgress(q).objectiveProgress = new ArrayList<QuestObjectiveProgress>();
                getProgress(q).objectiveProgress.clear();

                int index = 0;
                for(QuestObjective o : s.objectives){
                    QuestObjectiveProgress op = new QuestObjectiveProgress();
                    op.objectiveIndex = index;
                    op.killedMobs = 0;

                    getProgress(q).objectiveProgress.add(op);
                    index++;
                }
            }
        }
    }

    public boolean isDoneWithStage(Quest q){
        return isDoneWithStage(q,getCurrentStage(q));
    }

    public boolean isDoneWithStage(Quest q, int stageIndex){
        if(getStatus(q) == QuestProgressStatus.NOT_STARTED){
            return false;
        } else if(getStatus(q) == QuestProgressStatus.STARTED){
            QuestStage s = q.getStages()[stageIndex];

            for(QuestObjective o : s.objectives){
                if(o.type == QuestObjectiveType.KILL_MOBS){
                    if(getObjectiveProgress(q,o).killedMobs < o.mobToKillAmount){
                        return false;
                    }
                } else if(o.type == QuestObjectiveType.FIND_ITEM){
                    if(getAmountInInventory(o.itemToFind) < o.itemToFindAmount){
                        return false;
                    }
                }
            }

            return true;
        } else if(getStatus(q) == QuestProgressStatus.FINISHED){
            return true;
        } else {
            return false;
        }
    }

    public boolean mayStartQuest(Quest q){
        if(getStatus(q) != QuestProgressStatus.NOT_STARTED){
            return false;
        } else {
            if(getLevel() < q.getRequiredLevel()){
                return false;
            } else {
                for(int qq : q.getRequiredQuests()){
                    if(Quest.getQuest(qq) != null){
                        Quest req = Quest.getQuest(qq);

                        if(getStatus(req) != QuestProgressStatus.FINISHED){
                            return false;
                        }
                    }
                }

                return true;
            }
        }
    }

    public int getAmountInInventory(ItemData data){
        return getAmountInInventory(data.getId());
    }

    public int getAmountInInventory(int id){
        int i = 0;

        for(ItemStack iStack : p.getInventory().getContents()){
            CustomItem item = CustomItem.fromItemStack(iStack);

            if(item != null){
                if(item.getData().getId() == id) i += item.getAmount();
            }
        }

        return i;
    }

    public int getBankSlots(){
        return getBankRows()*9;
    }

    public void updateBankSize(){
        if(getBankSlots() > bank.getSize()){
            Inventory newBank = Bukkit.createInventory(null,getBankSlots(),"Your Bank");
            newBank.setContents(bank.getContents());

            bank = newBank;
        }
    }

    public CustomItem[] getEquipment(){
        ArrayList<CustomItem> a = new ArrayList<CustomItem>();

        if(p.getInventory().getHelmet() != null && CustomItem.fromItemStack(p.getInventory().getHelmet()) != null && CustomItem.fromItemStack(p.getInventory().getHelmet()).mayUse(p)) a.add(CustomItem.fromItemStack(p.getInventory().getHelmet()));
        if(p.getInventory().getChestplate() != null && CustomItem.fromItemStack(p.getInventory().getChestplate()) != null && CustomItem.fromItemStack(p.getInventory().getChestplate()).mayUse(p)) a.add(CustomItem.fromItemStack(p.getInventory().getChestplate()));
        if(p.getInventory().getLeggings() != null && CustomItem.fromItemStack(p.getInventory().getLeggings()) != null && CustomItem.fromItemStack(p.getInventory().getLeggings()).mayUse(p)) a.add(CustomItem.fromItemStack(p.getInventory().getLeggings()));
        if(p.getInventory().getBoots() != null && CustomItem.fromItemStack(p.getInventory().getBoots()) != null && CustomItem.fromItemStack(p.getInventory().getBoots()).mayUse(p)) a.add(CustomItem.fromItemStack(p.getInventory().getBoots()));

        //if(CustomItem.fromItemStack(p.getItemInHand()) != null && !CustomItem.fromItemStack(p.getItemInHand()).isMatchingWeapon(getRpgClass())){
        if (CustomItem.fromItemStack(p.getInventory().getItemInMainHand()) != null && CustomItem.fromItemStack(p.getItemInHand()).getData().getId() == 5) {
            for(int i = 0; i < 8; i++){
                CustomItem item = CustomItem.fromItemStack(p.getInventory().getItem(i));

                if(item != null){
                    if(item.getData().getCategory().toString().startsWith("WEAPON_") && item.getData().getNeededClass().matches(getRpgClass())){
                        if(item.getData().getNeededLevel() <= getLevel()){
                            a.add(item);
                            break;
                        }
                    }
                }
            }
        } else {
            if (p.getInventory().getItemInMainHand() != null && CustomItem.fromItemStack(p.getInventory().getItemInMainHand()) != null && CustomItem.fromItemStack(p.getInventory().getItemInMainHand()).getData().getCategory().name().startsWith("WEAPON_") && CustomItem.fromItemStack(p.getInventory().getItemInMainHand()).mayUse(p))
                a.add(CustomItem.fromItemStack(p.getInventory().getItemInMainHand()));
        }

        return a.toArray(new CustomItem[]{});
    }

    public int getTotalDodgingChance(){
        return getStatpointsTotal(StatPointType.AGILITY)+getTotalValue(AwakeningType.DODGING);
    }

    public int getTotalCriticalHitChance(){
        return getStatpointsTotal(StatPointType.DEXTERITY);
    }

    public UserVariables getVariables() {
        return variables;
    }

    public void setLastLogin(Timestamp t){
        this.lastLogin = t;
    }

    public void saveData(){
        saveData(false,true);
    }

    public void saveData(boolean continueCharsel){
        saveData(continueCharsel,true);
    }

    public void saveData(boolean continueCharsel, boolean resetSaveLocation) {
        saveData(continueCharsel, resetSaveLocation, true);
    }

    public void saveData(boolean continueCharsel, boolean resetSaveLocation, boolean async) {
        GameUser u = GameUser.getUser(p);

        if(async){
            DungeonAPI.async(() -> saveData(continueCharsel,false));
        } else {
            try {
                if (getVariables().saveLocation != null) {
                    this.storedLocation = getVariables().saveLocation.toBukkitLocation();
                    if (resetSaveLocation) getVariables().saveLocation = null;
                } else {
                    if (!p.getLocation().getWorld().getName().startsWith("dungeonTemplate_")) {
                        this.storedLocation = p.getLocation();
                    } else {
                        DungeonType type = DungeonType.fromWorldName(p.getLocation().getWorld().getName());

                        if (type != null) {
                            this.storedLocation = type.getPortalEntranceLocation();
                        } else {
                            this.storedLocation = DungeonRPG.sortedTownLocations().get(Util.randomInteger(0, DungeonRPG.sortedTownLocations().size() - 1)).toBukkitLocation();
                        }
                    }
                }

                this.storedInventory = getConvertedInventory(p);
                Gson gson = DungeonAPI.GSON;

                playtime = getPlaytime();
                lastPlaytimeSave = new Timestamp(System.currentTimeMillis());

                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("UPDATE `characters` SET `level` = ?, `exp` = ?, `statpoints.str` = ?, `statpoints.sta` = ?, `statpoints.int` = ?, `statpoints.dex` = ?, `statpoints.agi` = ?, `statpoints.left` = ?, `location.world` = ?, `location.x` = ?, `location.y` = ?, `location.z` = ?, `location.yaw` = ?, `location.pitch` = ?, `playtime` = ?, `questProgress` = ?, `variables` = ?, `inventory` = ?, `lastLogin` = ? WHERE `id` = ?");
                ps.setInt(1,getLevel());
                ps.setDouble(2,getExp());
                ps.setInt(3,strength);
                ps.setInt(4,stamina);
                ps.setInt(5,intelligence);
                ps.setInt(6,dexterity);
                ps.setInt(7,agility);
                ps.setInt(8,statpointsLeft);
                ps.setString(9, this.storedLocation.getWorld().getName());
                ps.setDouble(10, this.storedLocation.getX());
                ps.setDouble(11, this.storedLocation.getY());
                ps.setDouble(12, this.storedLocation.getZ());
                ps.setFloat(13, this.storedLocation.getYaw());
                ps.setFloat(14, this.storedLocation.getPitch());
                ps.setInt(15,playtime);
                ps.setString(16,gson.toJson(questProgress));
                ps.setString(17,gson.toJson(variables));
                ps.setString(18,getConvertedInventory(p).toString());
                ps.setTimestamp(19,lastLogin);
                ps.setInt(20,getId());
                ps.executeUpdate();
                ps.close();

                if(continueCharsel){
                    u.setCurrentCharacter(null);
                    u.removeSoulLight();
                    u.bukkitReset();
                    u.getSkillValues().reset();
                    u.stopMPRegenTask();
                    u.stopHPRegenTask();
                    u.resetTemporaryData();
                    if (u.respawnCountdown != null) u.respawnCountdown.cancel();
                    u.resetMount();
                    u.removeHoloPlate();
                    u.removeSoulLight();
                    u.updateWalkSpeed();
                    p.teleport(DungeonRPG.getCharSelLocation());
                    DungeonRPG.updateVanishing();
                    u.lastDamageSource = null;
                    for(Player a : Bukkit.getOnlinePlayers())
                        if(GameUser.isLoaded(a) && GameUser.getUser(a).lastDamageSource == p)
                            GameUser.getUser(a).lastDamageSource = null;

                    CharacterSelectionMenu.openSelection(p);

                    lastPlaytimeSave = null;
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public void saveLoggedOutData(){
        GameUser u = GameUser.getUser(p);

        DungeonAPI.async(() -> {
            try {
                Gson gson = DungeonAPI.GSON;

                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("UPDATE `characters` SET `level` = ?, `exp` = ?, `statpoints.str` = ?, `statpoints.sta` = ?, `statpoints.int` = ?, `statpoints.dex` = ?, `statpoints.agi` = ?, `statpoints.left` = ?, `questProgress` = ?, `variables` = ?, `lastLogin` = ? WHERE `id` = ?");
                ps.setInt(1,getLevel());
                ps.setDouble(2,getExp());
                ps.setInt(3,strength);
                ps.setInt(4,stamina);
                ps.setInt(5,intelligence);
                ps.setInt(6,dexterity);
                ps.setInt(7,agility);
                ps.setInt(8,statpointsLeft);
                ps.setString(9,gson.toJson(questProgress));
                ps.setString(10,gson.toJson(variables));
                ps.setTimestamp(11,lastLogin);
                ps.setInt(12,getId());
                ps.executeUpdate();
                ps.close();
            } catch(Exception e){
                e.printStackTrace();
            }
        });
    }
}
