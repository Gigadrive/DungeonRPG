package net.wrathofdungeons.dungeonrpg.user;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import eu.the5zig.mod.server.api.Stat;
import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.MySQLManager;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.StatPointType;
import net.wrathofdungeons.dungeonrpg.inv.CharacterSelectionMenu;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.items.ItemData;
import net.wrathofdungeons.dungeonrpg.items.PlayerInventory;
import net.wrathofdungeons.dungeonrpg.items.awakening.AwakeningType;
import net.wrathofdungeons.dungeonrpg.quests.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.naming.ldap.PagedResultsControl;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

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
    private PlayerInventory storedInventory;
    private Timestamp creationTime;
    private Timestamp lastLogin;
    private Inventory bank;

    public Character(int id, Player p){
        this.id = id;
        Gson gson = new Gson();

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
                this.storedLocation = new Location(Bukkit.getWorld(rs.getString("location.world")),rs.getDouble("location.x"),rs.getDouble("location.y"),rs.getDouble("location.z"),rs.getFloat("location.yaw"),rs.getFloat("location.pitch"));
                if(rs.getString("questProgress") != null){
                    this.questProgress = gson.fromJson(rs.getString("questProgress"),new TypeToken<ArrayList<QuestProgress>>(){}.getType());
                } else {
                    this.questProgress = new ArrayList<QuestProgress>();
                }
                if(rs.getString("inventory") != null) this.storedInventory = PlayerInventory.fromString(rs.getString("inventory"));
                this.creationTime = rs.getTimestamp("time");
                this.lastLogin = rs.getTimestamp("lastLogin");

                this.bank = Bukkit.createInventory(null,getBankSlots(),"Your Bank");

                if(lastLogin == null) storedLocation = DungeonRPG.getStartLocation();
            }

            MySQLManager.getInstance().closeResources(rs,ps);
        } catch(Exception e){
            e.printStackTrace();
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
            if(item.hasAwakening(type)){
                i += item.getAwakeningValue(type);
            }
        }

        return i;
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

        if(CustomItem.fromItemStack(p.getItemInHand()) != null && CustomItem.fromItemStack(p.getItemInHand()).getData().getId() == 5){
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
            if(p.getInventory().getItemInHand() != null && CustomItem.fromItemStack(p.getInventory().getItemInHand()) != null && CustomItem.fromItemStack(p.getInventory().getItemInHand()).mayUse(p)) a.add(CustomItem.fromItemStack(p.getInventory().getItemInHand()));
        }

        return a.toArray(new CustomItem[]{});
    }

    public int getTotalDodgingChance(){
        return getStatpointsTotal(StatPointType.AGILITY)+getTotalValue(AwakeningType.DODGING);
    }

    public int getTotalCriticalHitChance(){
        return getStatpointsTotal(StatPointType.DEXTERITY);
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

    public void saveData(boolean continueCharsel, boolean async){
        if(async){
            DungeonAPI.async(() -> saveData(continueCharsel,false));
        } else {
            try {
                this.storedLocation = p.getLocation();
                this.storedInventory = getConvertedInventory(p);
                Gson gson = new Gson();

                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("UPDATE `characters` SET `level` = ?, `exp` = ?, `statpoints.str` = ?, `statpoints.sta` = ?, `statpoints.int` = ?, `statpoints.dex` = ?, `statpoints.agi` = ?, `statpoints.left` = ?, `location.world` = ?, `location.x` = ?, `location.y` = ?, `location.z` = ?, `location.yaw` = ?, `location.pitch` = ?, `questProgress` = ?, `inventory` = ?, `lastLogin` = ? WHERE `id` = ?");
                ps.setInt(1,getLevel());
                ps.setDouble(2,getExp());
                ps.setInt(3,strength);
                ps.setInt(4,stamina);
                ps.setInt(5,intelligence);
                ps.setInt(6,dexterity);
                ps.setInt(7,agility);
                ps.setInt(8,statpointsLeft);
                ps.setString(9,p.getLocation().getWorld().getName());
                ps.setDouble(10,p.getLocation().getX());
                ps.setDouble(11,p.getLocation().getY());
                ps.setDouble(12,p.getLocation().getZ());
                ps.setFloat(13,p.getLocation().getYaw());
                ps.setFloat(14,p.getLocation().getPitch());
                ps.setString(15,gson.toJson(questProgress));
                ps.setString(16,getConvertedInventory(p).toString());
                ps.setTimestamp(17,lastLogin);
                ps.setInt(18,getId());
                ps.executeUpdate();
                ps.close();

                if(continueCharsel){
                    GameUser.getUser(p).setCurrentCharacter(null);
                    GameUser.getUser(p).bukkitReset();
                    GameUser.getUser(p).getSkillValues().reset();
                    GameUser.getUser(p).stopMPRegenTask();
                    GameUser.getUser(p).stopHPRegenTask();
                    GameUser.getUser(p).currentCombo = "";
                    p.teleport(DungeonRPG.getCharSelLocation());
                    DungeonRPG.updateVanishing();

                    CharacterSelectionMenu.openSelection(p);
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
