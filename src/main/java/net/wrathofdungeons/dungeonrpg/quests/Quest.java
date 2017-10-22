package net.wrathofdungeons.dungeonrpg.quests;

import com.google.gson.Gson;
import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.MySQLManager;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.npc.CustomNPC;
import net.wrathofdungeons.dungeonrpg.util.WorldUtilities;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

public class Quest {
    public static HashMap<Integer,Quest> STORAGE = new HashMap<Integer,Quest>();

    public static void init(){
        try {
            STORAGE.clear();

            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `quests` WHERE `stages` IS NOT NULL AND `active` = ?");
            ps.setBoolean(1,true);
            ResultSet rs = ps.executeQuery();

            while(rs.next()) new Quest(rs.getInt("id"));

            MySQLManager.getInstance().closeResources(rs,ps);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public static ArrayList<Quest> getUnsavedData(){
        ArrayList<Quest> a = new ArrayList<Quest>();

        for(Quest q : STORAGE.values()){
            if(q.hasUnsavedData()) a.add(q);
        }

        return a;
    }

    public static boolean isNameAvailable(String name){
        for(Quest q : STORAGE.values()){
            if(q.getName().equalsIgnoreCase(name)) return false;
        }

        boolean b = true;

        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `quests` WHERE `name` = ?");
            ps.setString(1,name);
            ResultSet rs = ps.executeQuery();

            b = !rs.first();

            MySQLManager.getInstance().closeResources(rs,ps);
        } catch(Exception e){
            e.printStackTrace();
        }

        return b;
    }

    public static Quest getQuest(int id){
        for(Quest q : STORAGE.values()){
            if(q.getId() == id) return q;
        }

        return null;
    }

    private int id;
    private String name;
    private int giverNpc;
    private int requiredLevel;
    private int[] requiredQuests = new int[]{};
    private QuestStage[] stages = new QuestStage[]{};
    private int rewardGoldenNuggets;
    private int rewardExp;
    private CustomItem[] rewardItems = new CustomItem[]{};

    private boolean hasUnsavedData = false;

    public Quest(int id){
        this.id = id;
        Gson gson = new Gson();

        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `quests` WHERE `id` = ?");
            ps.setInt(1,this.id);

            ResultSet rs = ps.executeQuery();
            if(rs.first()){
                this.id = rs.getInt("id");
                this.name = rs.getString("name");
                this.giverNpc = rs.getInt("giverNpc");
                this.requiredLevel = rs.getInt("requiredLevel");
                if(rs.getString("requiredQuests") != null && !rs.getString("requiredQuests").isEmpty()) this.requiredQuests = gson.fromJson(rs.getString("requiredQuests"),int[].class);
                if(rs.getString("stages") != null && !rs.getString("stages").isEmpty()) this.stages = gson.fromJson(rs.getString("stages"),QuestStage[].class);
                this.rewardGoldenNuggets = rs.getInt("rewards.goldenNuggets");
                this.rewardExp = rs.getInt("rewards.exp");
                if(rs.getString("rewards.items") != null && !rs.getString("rewards.items").isEmpty()) this.rewardItems = gson.fromJson(rs.getString("rewards.items"),CustomItem[].class);

                STORAGE.put(id,this);
            }

            MySQLManager.getInstance().closeResources(rs,ps);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getGiverNPCId() {
        return giverNpc;
    }

    public CustomNPC getGiverNPC() {
        return CustomNPC.fromID(giverNpc);
    }

    public void setGiverNPC(int giverNpc) {
        this.giverNpc = giverNpc;
        setHasUnsavedData(true);
    }

    public void setGiverNPC(CustomNPC giverNpc) {
        this.giverNpc = giverNpc.getId();
        setHasUnsavedData(true);
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public int[] getRequiredQuests() {
        return requiredQuests;
    }

    public QuestStage[] getStages() {
        return stages;
    }

    public int getRewardGoldenNuggets() {
        return rewardGoldenNuggets;
    }

    public int getRewardExp() {
        return rewardExp;
    }

    public CustomItem[] getRewardItems() {
        return rewardItems;
    }

    public boolean hasUnsavedData() {
        return hasUnsavedData;
    }

    public void setRequiredLevel(int requiredLevel) {
        this.requiredLevel = requiredLevel;
        setHasUnsavedData(true);
    }

    public void setRequiredQuests(int[] requiredQuests) {
        this.requiredQuests = requiredQuests;
        setHasUnsavedData(true);
    }

    public void setStages(QuestStage[] stages) {
        this.stages = stages;
        setHasUnsavedData(true);
    }

    public void setRewardGoldenNuggets(int rewardGoldenNuggets) {
        this.rewardGoldenNuggets = rewardGoldenNuggets;
        setHasUnsavedData(true);
    }

    public void setRewardExp(int rewardExp) {
        this.rewardExp = rewardExp;
    }

    public void setRewardItems(CustomItem[] rewardItems) {
        this.rewardItems = rewardItems;
        setHasUnsavedData(true);
    }

    public int getSlotsNeededForReward(){
        int i = 0;

        if(getRewardGoldenNuggets() > 0){
            for(CustomItem item : WorldUtilities.convertNuggetAmount(getRewardGoldenNuggets())){
                i += 1;
            }
        }

        if(getRewardItems().length > 0){
            for(CustomItem item : getRewardItems()){
                i += 1;
            }
        }

        return i;
    }

    public void setHasUnsavedData(boolean hasUnsavedData) {
        this.hasUnsavedData = hasUnsavedData;
    }

    public void saveData(){
        saveData(true);
    }

    public void saveData(boolean async){
        if(async){
            DungeonAPI.async(() -> saveData(false));
        } else {
            try {
                Gson gson = new Gson();

                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("UPDATE `quests` SET `name` = ?, `requiredLevel` = ?, `requiredQuests` = ?, `stages` = ?, `rewards.goldenNuggets` = ?, `rewards.exp` = ?, `rewards.items` = ?, `giverNpc` = ? WHERE `id` = ?");
                ps.setString(1,getName());
                ps.setInt(2,getRequiredLevel());
                ps.setString(3,gson.toJson(getRequiredQuests()));
                ps.setString(4,gson.toJson(getStages()));
                ps.setInt(5,getRewardGoldenNuggets());
                ps.setInt(6,getRewardExp());
                ps.setString(7,gson.toJson(getRewardItems()));
                ps.setInt(8,giverNpc);
                ps.setInt(9,getId());
                ps.executeUpdate();
                ps.close();

                setHasUnsavedData(false);
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
