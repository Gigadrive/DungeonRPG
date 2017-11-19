package net.wrathofdungeons.dungeonrpg.professions;

import com.google.gson.Gson;
import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.MySQLManager;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.items.StoredCustomItem;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class CraftingRecipe {
    public static HashMap<Integer,CraftingRecipe> STORAGE = new HashMap<Integer,CraftingRecipe>();

    public static void init(){
        STORAGE.clear();

        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `craftingRecipes`");
            ResultSet rs = ps.executeQuery();

            while(rs.next()) new CraftingRecipe(rs.getInt("id"));

            MySQLManager.getInstance().closeResources(rs,ps);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public static CraftingRecipe getRecipe(int id){
        return STORAGE.getOrDefault(id,null);
    }

    public static CraftingRecipe getResult(StoredCustomItem[] used){
        for(CraftingRecipe r : STORAGE.values()){
            int matching = 0;
            int needed = r.getNeededItems().length;

            if(needed == 0) continue;
            if(r.getResult() == null) continue;

            for(StoredCustomItem i : r.getNeededItems()){
                for(StoredCustomItem c : used){
                    if(i.isSameItem(c) && i.amount == c.amount) matching++;

                    if(matching == needed) return r;
                }
            }
        }

        return null;
    }

    public static ArrayList<CraftingRecipe> getUnsavedData(){
        ArrayList<CraftingRecipe> a = new ArrayList<CraftingRecipe>();

        for(CraftingRecipe r : STORAGE.values()) if(r.hassUnsavedData()) a.add(r);

        return a;
    }

    private int id;
    private StoredCustomItem[] neededItems;
    private StoredCustomItem result;
    private UUID addedBy;
    private Timestamp timeAdded;

    private boolean unsavedData = false;

    public CraftingRecipe(int id){
        if(STORAGE.containsKey(id)) return;

        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `craftingRecipes` WHERE `id` = ?");
            ps.setInt(1,id);

            ResultSet rs = ps.executeQuery();
            if(rs.first()){
                this.id = rs.getInt("id");

                if(rs.getString("neededItems") != null){
                    this.neededItems = DungeonAPI.GSON.fromJson(rs.getString("neededItems"),StoredCustomItem[].class);
                } else {
                    this.neededItems = new StoredCustomItem[]{};
                }

                if(rs.getString("result") != null){
                    this.result = DungeonAPI.GSON.fromJson(rs.getString("result"),StoredCustomItem.class);
                } else {
                    this.result = null;
                }

                this.timeAdded = rs.getTimestamp("time");
                this.addedBy = UUID.fromString(rs.getString("addedBy"));

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

    public StoredCustomItem[] getNeededItems() {
        return neededItems;
    }

    public void setNeededItems(StoredCustomItem[] neededItems) {
        this.neededItems = neededItems;
    }

    public StoredCustomItem getResult() {
        return result;
    }

    public void setResult(StoredCustomItem result) {
        this.result = result;
    }

    public UUID getAddedBy() {
        return addedBy;
    }

    public Timestamp getTimeAdded() {
        return timeAdded;
    }

    public boolean hassUnsavedData() {
        return unsavedData;
    }

    public void setHasUnsavedData(boolean unsavedData) {
        this.unsavedData = unsavedData;
    }

    public void saveData(){
        saveData(true);
    }

    public void saveData(boolean async){
        if(async){
            DungeonAPI.async(() -> saveData(false));
        } else {
            setHasUnsavedData(false);
            Gson gson = DungeonAPI.GSON;

            String neededItemsString = getNeededItems() != null && getNeededItems().length > 0 ? DungeonAPI.GSON.toJson(getNeededItems()) : null;
            String resultString = getResult() != null ? DungeonAPI.GSON.toJson(getResult()) : null;

            try {
                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("UPDATE `craftingRecipes` SET `neededItems` = ?, `result` = ? WHERE `id` = ?");
                ps.setString(1,neededItemsString);
                ps.setString(2,resultString);
                ps.setInt(3,getId());
                ps.executeUpdate();
                ps.close();
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
