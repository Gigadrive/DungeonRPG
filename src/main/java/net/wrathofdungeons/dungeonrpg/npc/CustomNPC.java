package net.wrathofdungeons.dungeonrpg.npc;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.VillagerProfession;
import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.MySQLManager;
import net.wrathofdungeons.dungeonapi.util.ChatIcons;
import net.wrathofdungeons.dungeonapi.util.ItemUtil;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.dungeon.DungeonType;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.items.ItemData;
import net.wrathofdungeons.dungeonrpg.npc.dialogue.NPCDialogue;
import net.wrathofdungeons.dungeonrpg.npc.dialogue.NPCDialogueConditionType;
import net.wrathofdungeons.dungeonrpg.quests.Quest;
import net.wrathofdungeons.dungeonrpg.quests.QuestProgressStatus;
import net.wrathofdungeons.dungeonrpg.quests.QuestStage;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.inventivetalent.menubuilder.inventory.InventoryMenuBuilder;
import org.mineskin.data.Skin;
import org.mineskin.data.SkinCallback;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

public class CustomNPC {
    public static ArrayList<CustomNPC> STORAGE = new ArrayList<CustomNPC>();

    public static CustomNPC fromID(int id){
        for(CustomNPC npc : STORAGE){
            if(npc.getId() == id) return npc;
        }

        return null;
    }

    public static CustomNPC fromCitizensNPC(NPC npc){
        for(CustomNPC c : STORAGE){
            if(c.npc != null && (c.npc == npc || c.npc.getId() == npc.getId())){
                return c;
            }
        }

        return null;
    }

    public static CustomNPC fromEntity(Entity e){
        for(CustomNPC c : STORAGE){
            if(c.npc != null && c.npc.getEntity() != null && c.npc.getEntity().getUniqueId() != null && c.npc.getEntity().getUniqueId().toString().equals(e.getUniqueId().toString())){
                return c;
            }
        }

        return null;
    }

    public static void init(){
        if(STORAGE.size() > 0) for(CustomNPC npc : STORAGE) npc.despawnNPC();
        STORAGE.clear();

        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `npcs` ORDER BY `id` DESC");
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                new CustomNPC(rs.getInt("id"));
            }

            MySQLManager.getInstance().closeResources(rs,ps);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public static ArrayList<CustomNPC> getUnsavedData(){
        ArrayList<CustomNPC> a = new ArrayList<CustomNPC>();

        for(CustomNPC npc : STORAGE) if(npc.hasUnsavedData) a.add(npc);

        return a;
    }

    private int id;
    private String customName;
    private CustomNPCType npcType;
    private EntityType entityType;
    private Villager.Profession villagerProfession;
    private int skinID;
    private ArrayList<NPCDialogue> dialogues;
    private ArrayList<MerchantOffer> offers;
    private Location storedLocation;

    private Skin mineSkin;
    private NPC npc;
    private Hologram hologram;
    private String skinName;

    public CustomItem keyMasterItem;
    public KeyMasterLocation keyMasterLocation;
    public DungeonType dungeonType;

    private CustomNPCData additionalData;

    private boolean hasUnsavedData;

    public static ArrayList<String> READING = new ArrayList<String>();

    public CustomNPC(int id){
        if(fromID(id) != null) return;

        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `npcs` WHERE `id` = ?");
            ps.setInt(1,id);
            ResultSet rs = ps.executeQuery();

            if(rs.first()){
                this.id = rs.getInt("id");
                this.customName = rs.getString("customName");
                this.npcType = CustomNPCType.fromName(rs.getString("npcType"));
                this.entityType = EntityType.valueOf(rs.getString("entityType"));
                this.villagerProfession = Villager.Profession.valueOf(rs.getString("villager.profession"));
                this.skinID = rs.getInt("player.skin");
                String offerString = rs.getString("merchant.offers");
                Gson gson = DungeonAPI.GSON;
                if(offerString != null){
                    this.offers = gson.fromJson(offerString, new TypeToken<ArrayList<MerchantOffer>>(){}.getType());
                } else {
                    this.offers = new ArrayList<MerchantOffer>();
                }

                String textLinesString = rs.getString("textLines");
                if(textLinesString != null){
                    this.dialogues = gson.fromJson(textLinesString, new TypeToken<ArrayList<NPCDialogue>>(){}.getType());
                } else {
                    this.dialogues = new ArrayList<NPCDialogue>();
                }

                String keyMasterLocationString = rs.getString("keyMaster.location");
                if(keyMasterLocationString != null){
                    this.keyMasterLocation = gson.fromJson(keyMasterLocationString,KeyMasterLocation.class);
                } else {
                    this.keyMasterLocation = null;
                }

                String dataString = rs.getString("additionalData");
                if(dataString != null){
                    this.additionalData = gson.fromJson(dataString,CustomNPCData.class);
                } else {
                    this.additionalData = new CustomNPCData();
                }

                this.dungeonType = rs.getString("dungeonType") != null ? DungeonType.valueOf(rs.getString("dungeonType")) : null;

                String keyMasterItemString = rs.getString("keyMaster.item");
                if(keyMasterItemString != null && !keyMasterItemString.isEmpty()){
                    String[] s = keyMasterItemString.split(":");
                    if(s.length == 1){
                        if(Util.isValidInteger(s[0])){
                            this.keyMasterItem = new CustomItem(Integer.parseInt(s[0]));
                        } else {
                            this.keyMasterItem = null;
                        }
                    } else if(s.length == 2){
                        if(Util.isValidInteger(s[0]) && Util.isValidInteger(s[1])){
                            this.keyMasterItem = new CustomItem(Integer.parseInt(s[0]),Integer.parseInt(s[1]));
                        } else {
                            this.keyMasterItem = null;
                        }
                    } else {
                        this.keyMasterItem = null;
                    }
                } else {
                    this.keyMasterItem = null;
                }

                this.storedLocation = new Location(Bukkit.getWorld(rs.getString("location.world")),rs.getDouble("location.x"),rs.getDouble("location.y"),rs.getDouble("location.z"),rs.getFloat("location.yaw"),rs.getFloat("location.pitch"));

                updateSkinName();
                reloadSkin();
                spawnNPC();

                STORAGE.add(this);
            }

            MySQLManager.getInstance().closeResources(rs,ps);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public String getSkinName() {
        return skinName;
    }

    private void updateSkinName(){
        skinName = Util.randomString(1,16);
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
        setHasUnsavedData(true);

        despawnNPC();
        spawnNPC();
    }

    public String getDisplayName(){
        return getCustomName() == null ? getNpcType().getColor() + getNpcType().getDefaultName() : getNpcType().getColor() + getCustomName();
    }

    public void setSkin(int mineSkinID){
        if(mineSkinID < 0) mineSkinID = 0;
        this.skinID = mineSkinID;

        setHasUnsavedData(true);

        updateSkinName();
        reloadSkin();
    }

    public void setSkin(Skin skin){
        if(skin != null){
            this.skinID = skin.id;
        } else {
            this.skinID = 0;
        }

        setHasUnsavedData(true);

        updateSkinName();
        reloadSkin();
        respawnNPC();
    }

    public void reloadSkin(){
        if(this.skinID > 0){
            DungeonRPG.getMineskinClient().getSkin(this.skinID, new SkinCallback() {
                @Override
                public void done(Skin skin) {
                    mineSkin = skin;
                }
            });
        } else {
            this.mineSkin = null;
        }
    }

    public Skin getSkin() {
        return mineSkin;
    }

    public CustomNPCType getNpcType() {
        return npcType;
    }

    public void setNpcType(CustomNPCType npcType) {
        this.npcType = npcType;
        setHasUnsavedData(true);

        respawnNPC();
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
        setHasUnsavedData(true);

        respawnNPC();
    }

    public Villager.Profession getVillagerProfession() {
        return villagerProfession;
    }

    public void setVillagerProfession(Villager.Profession villagerProfession) {
        this.villagerProfession = villagerProfession;
        setHasUnsavedData(true);

        respawnNPC();
    }

    public ArrayList<MerchantOffer> getOffers() {
        return offers;
    }

    public void setOffers(ArrayList<MerchantOffer> offers){
        if(offers != null) this.offers = offers;
    }

    public Location getLocation() {
        return storedLocation;
    }

    public void setLocation(Location loc){
        if(loc != null){
            storedLocation = loc;
            setHasUnsavedData(true);

            if(npc != null) npc.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
            updateHologram();
        }
    }

    public CustomItem getKeyMasterItem() {
        return keyMasterItem;
    }

    public KeyMasterLocation getKeyMasterLocation() {
        return keyMasterLocation;
    }

    public void setKeyMasterItem(CustomItem keyMasterItem) {
        setHasUnsavedData(true);
        this.keyMasterItem = keyMasterItem;
    }

    public void setKeyMasterLocation(KeyMasterLocation keyMasterLocation) {
        setHasUnsavedData(true);
        this.keyMasterLocation = keyMasterLocation;
    }

    public ArrayList<NPCDialogue> getDialogues() {
        return dialogues;
    }

    public Hologram getHologram() {
        return hologram;
    }

    public void respawnNPC(){
        despawnNPC();
        spawnNPC();
    }

    public void spawnNPC(){
        if(!isSpawned()){
            DungeonRPG.IGNORE_SPAWN_NPC.add(npc);
            npc = CitizensAPI.getNPCRegistry().createNPC(getEntityType(),DungeonRPG.randomColor().toString());

            npc.spawn(getLocation());
            npc.data().setPersistent(NPC.NAMEPLATE_VISIBLE_METADATA, false);

            if(getEntityType() == EntityType.VILLAGER){
                npc.getTrait(VillagerProfession.class).setProfession(getVillagerProfession());
            } else if(getEntityType() == EntityType.PLAYER){
                //WorldUtilities.applySkinToNPC(npc,getSkin());
            }

            if(getEntityType() != EntityType.PLAYER){
                DungeonAPI.nmsMakeSilent(npc.getEntity());
            }

            new BukkitRunnable(){
                @Override
                public void run() {
                    DungeonRPG.IGNORE_SPAWN_NPC.remove(npc);
                }
            }.runTaskLater(DungeonRPG.getInstance(),5);
        }

        updateHologram();
    }

    public void despawnNPC(){
        if(isSpawned()){
            toCitizensNPC().destroy();
            this.npc = null;
        }

        if(hologram != null && !hologram.isDeleted()) hologram.delete();
        hologram = null;
    }

    public boolean isSpawned(){
        return this.npc != null;
    }

    public Location getPotentialHologramLocation(){
        if(isSpawned()){
            return getLocation().clone().add(0,2.5,0);
        } else {
            return null;
        }
    }

    public void updateHologram(){
        DungeonAPI.sync(() -> {
            if(isSpawned()){
                if(hologram == null){
                    hologram = HologramsAPI.createHologram(DungeonRPG.getInstance(),getPotentialHologramLocation());

                    if(getCustomName() == null){
                        hologram.appendTextLine(getNpcType().getColor() + getNpcType().getDefaultName());
                    } else {
                        hologram.appendTextLine(getNpcType().getColor() + getCustomName());
                    }
                } else {
                    hologram.teleport(getPotentialHologramLocation());
                }
            } else {
                if(hologram != null && !hologram.isDeleted()) hologram.delete();
                hologram = null;
            }
        });
    }

    public void openShop(Player p){
        if(getNpcType() != CustomNPCType.MERCHANT) throw new IllegalArgumentException("NPC must be a merchant!");

        GameUser u = GameUser.getUser(p);
        InventoryMenuBuilder inv = new InventoryMenuBuilder(Util.MAX_INVENTORY_SIZE);
        inv.withTitle(getCustomName() != null ? ChatColor.stripColor(getCustomName()) : getNpcType().getDefaultName());

        for(MerchantOffer offer : getOffers()){
            ItemStack i = new CustomItem(offer.itemToBuy,offer.amount).build(p);
            ItemMeta iM = i.getItemMeta();
            ArrayList<String> iL = new ArrayList<String>();
            if(iM.hasLore()) iL.addAll(iM.getLore());

            /*ArrayList<CustomItem> offerItems = new ArrayList<CustomItem>();
            if(offer.moneyCost > 0) offerItems.addAll(Arrays.asList(WorldUtilities.convertNuggetAmount(offer.moneyCost)));
            for(MerchantOfferCost cost : offer.itemCost) offerItems.add(new CustomItem(cost.item,cost.amount));

            iL.add(ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH.toString() + Util.SCOREBOARD_LINE_SEPERATOR);
            iL.add(ChatColor.YELLOW + "Cost:");

            for(CustomItem c : offerItems) iL.add(ChatColor.GRAY + "[" + c.getAmount() + "x " + ChatColor.stripColor(c.getData().getName()) + "]");*/

            iL.add(" ");
            iL.add(ChatColor.GOLD + "Price:");
            for(MerchantOfferCost cost : offer.itemCost){
                CustomItem item = new CustomItem(cost.item,cost.amount);

                if(u.getAmountInInventory(item.getData()) >= cost.amount){
                    iL.add(ChatColor.GOLD + "- " + ChatColor.DARK_GREEN + ChatIcons.CHECK_MARK + " " + ChatColor.GREEN + item.getAmount() + "x " + ChatColor.stripColor(item.getData().getName()));
                } else {
                    iL.add(ChatColor.GOLD + "- " + ChatColor.DARK_RED + ChatIcons.X + " " + ChatColor.RED + item.getAmount() + "x " + ChatColor.stripColor(item.getData().getName()));
                }
            }

            if(offer.moneyCost > 0){
                if(u.getTotalMoneyInInventory() >= offer.moneyCost){
                    iL.add(ChatColor.GOLD + "- " + ChatColor.DARK_GREEN + ChatIcons.CHECK_MARK + " " + ChatColor.WHITE + offer.moneyCost + " " + ChatColor.GRAY + "Golden Nuggets");
                } else {
                    iL.add(ChatColor.GOLD + "- " + ChatColor.DARK_RED + ChatIcons.X + " " + ChatColor.WHITE + offer.moneyCost + " " + ChatColor.GRAY + "Golden Nuggets");
                }
            }

            iM.setLore(iL);
            i.setItemMeta(iM);

            inv.withItem(offer.slot,i,((player, action, item) -> {
                if(u.getEmptySlotsInInventory() >= 1){
                    boolean canAfford = true;

                    if(offer.moneyCost > 0 && u.getTotalMoneyInInventory() < offer.moneyCost) canAfford = false;

                    for(MerchantOfferCost cost : offer.itemCost){
                        if(!u.hasInInventory(ItemData.getData(cost.item),cost.amount)) canAfford = false;
                    }

                    if(canAfford){
                        HashMap<Integer,Integer> toRemove = new HashMap<Integer,Integer>();

                        if(offer.moneyCost > 0) u.removeMoneyFromInventory(offer.moneyCost);

                        if(offer.itemCost.size() > 0){
                            for(MerchantOfferCost cost : offer.itemCost){
                                toRemove.put(cost.item,cost.amount);
                            }
                        }

                        for(int itemID : toRemove.keySet()){
                            int amount = toRemove.get(itemID);

                            u.removeFromInventory(ItemData.getData(itemID),amount);
                        }

                        p.getInventory().addItem(new CustomItem(offer.itemToBuy,offer.amount).build(p));
                        p.playSound(p.getEyeLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP,1f,1f);
                    } else {
                        p.sendMessage(ChatColor.RED + "You can't afford that item.");
                    }
                } else {
                    p.sendMessage(ChatColor.RED + "Please empty some space in your inventory first.");
                }
            }),ClickType.LEFT);
        }

        inv.withItem(45, ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
        inv.withItem(46, ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
        inv.withItem(47, ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
        inv.withItem(48, ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
        inv.withItem(49, ItemUtil.namedItem(Material.BARRIER, ChatColor.DARK_RED + "Close",null), ((player, action, item) -> player.closeInventory()), ClickType.LEFT);
        inv.withItem(50, ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
        inv.withItem(51, ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
        inv.withItem(52, ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
        inv.withItem(53, ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));

        inv.show(p);
    }

    public boolean hasUnsavedData() {
        return hasUnsavedData;
    }

    public void setHasUnsavedData(boolean hasUnsavedData){
        this.hasUnsavedData = hasUnsavedData;
    }

    public NPC toCitizensNPC(){
        return npc;
    }

    public void saveData(){
        saveData(true);
    }

    public NPCDialogue getPreferredDialogue(Player p){
        if(!GameUser.isLoaded(p)) return null;
        GameUser u = GameUser.getUser(p);
        if(u.getCurrentCharacter() == null) return null;

        for(NPCDialogue dialogue : getDialogues()){
            Quest q = Quest.getQuest(dialogue.condition.questID);
            if(q == null) continue;
            if(u.getCurrentCharacter().getStatus(q) != QuestProgressStatus.STARTED && (dialogue.condition.type != NPCDialogueConditionType.QUEST_NOTSTARTED && dialogue.condition.type != NPCDialogueConditionType.QUEST_STARTING)) continue;
            //if(u.getCurrentCharacter().getCurrentStage(q) != dialogue.condition.questStageIndex) continue;

            int stageIndex = u.getCurrentCharacter().getCurrentStage(q);
            QuestStage stage = null;
            if(stageIndex >= 0) stage = q.getStages()[stageIndex];

            if(dialogue.condition.type == NPCDialogueConditionType.QUEST_ENDING){
                if(u.getCurrentCharacter().isDoneWithStage(q,stageIndex) && stageIndex == q.getStages().length-1){
                    return dialogue;
                }
            } else if(dialogue.condition.type == NPCDialogueConditionType.QUEST_STAGE_STARTING){
                if(u.getCurrentCharacter().isDoneWithStage(q,stageIndex) && stageIndex < q.getStages().length-1 && stageIndex == dialogue.condition.questStageIndex-1){
                    return dialogue;
                }
            } else if(dialogue.condition.type == NPCDialogueConditionType.QUEST_RUNNING){
                if((!u.getCurrentCharacter().isDoneWithStage(q,stageIndex) || !stage.isLastNPC(this)) && (stageIndex == dialogue.condition.questStageIndex)){
                    return dialogue;
                }
            } else if(dialogue.condition.type == NPCDialogueConditionType.QUEST_STARTING){
                //p.sendMessage("status: " + u.getCurrentCharacter().getStatus(q).toString());
                //p.sendMessage("may start: " + u.getCurrentCharacter().mayStartQuest(q));
                if(u.getCurrentCharacter().getStatus(q) == QuestProgressStatus.NOT_STARTED && u.getCurrentCharacter().mayStartQuest(q)){
                    return dialogue;
                }
            } else if(dialogue.condition.type == NPCDialogueConditionType.QUEST_NOTSTARTED){
                if(u.getCurrentCharacter().getStatus(q) == QuestProgressStatus.NOT_STARTED && !u.getCurrentCharacter().mayStartQuest(q)){
                    return dialogue;
                }
            }
        }

        return getDefaultDialogue();
    }

    private NPCDialogue getDialogue(Quest q, int questStage, NPCDialogueConditionType type){
        for(NPCDialogue dialogue : getDialogues()){
            if(dialogue.lines == null || dialogue.lines.size() == 0) continue;

            if(dialogue.condition != null && dialogue.condition.type == type && dialogue.condition.questID == q.getId() && dialogue.condition.questStageIndex == questStage) return dialogue;
        }

        return null;
    }

    private NPCDialogue getDefaultDialogue(){
        for(NPCDialogue dialogue : getDialogues()){
            if(dialogue.lines == null || dialogue.lines.size() == 0) continue;

            if(dialogue.condition != null && dialogue.condition.type == NPCDialogueConditionType.NONE) return dialogue;
        }

        return null;
    }

    public void saveData(boolean async){
        if(async){
            DungeonAPI.async(() -> saveData(false));
        } else {
            setHasUnsavedData(false);
            Gson gson = DungeonAPI.GSON;

            String keyMasterItemString = null;
            String keyMasterLocationString = null;

            if(getKeyMasterItem() != null) keyMasterItemString = getKeyMasterItem().getData().getId() + ":" + getKeyMasterItem().getAmount();
            if(getKeyMasterLocation() != null) keyMasterLocationString = gson.toJson(getKeyMasterLocation());

            try {
                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("UPDATE `npcs` SET `customName` = ?, `npcType` = ?, `entityType` = ?, `villager.profession` = ?, `player.skin` = ?, `textLines` = ?, `merchant.offers` = ?, `keyMaster.item` = ?, `keyMaster.location` = ?, `location.world` = ?, `location.x` = ?, `location.y` = ?, `location.z` = ?, `location.yaw`= ?, `location.pitch` = ?, `dungeonType` = ?, `additionalData` = ? WHERE `id` = ?");
                ps.setString(1,getCustomName());
                ps.setString(2,getNpcType().toString());
                ps.setString(3,getEntityType().toString());
                ps.setString(4,getVillagerProfession().toString());
                ps.setInt(5,skinID);
                ps.setString(6,gson.toJson(getDialogues()));
                ps.setString(7,gson.toJson(getOffers()));
                ps.setString(8,keyMasterItemString);
                ps.setString(9,keyMasterLocationString);
                ps.setString(10,getLocation().getWorld().getName());
                ps.setDouble(11,getLocation().getX());
                ps.setDouble(12,getLocation().getY());
                ps.setDouble(13,getLocation().getZ());
                ps.setFloat(14,getLocation().getYaw());
                ps.setFloat(15,getLocation().getPitch());
                ps.setString(16,dungeonType != null ? dungeonType.name() : null);
                ps.setString(17,gson.toJson(additionalData));
                ps.setInt(18,getId());
                ps.executeUpdate();
                ps.close();
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public void unregister(){
        despawnNPC();
        STORAGE.remove(this);
    }
}
