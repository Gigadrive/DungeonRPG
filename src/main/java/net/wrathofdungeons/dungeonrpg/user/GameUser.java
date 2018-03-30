package net.wrathofdungeons.dungeonrpg.user;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import com.mojang.authlib.GameProfile;
import net.citizensnpcs.api.CitizensAPI;
import net.minecraft.server.v1_9_R2.EntityLiving;
import net.minecraft.server.v1_9_R2.PacketPlayOutWorldBorder;
import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.MySQLManager;
import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonapi.user.User;
import net.wrathofdungeons.dungeonapi.util.BountifulAPI;
import net.wrathofdungeons.dungeonapi.util.ChatIcons;
import net.wrathofdungeons.dungeonapi.util.ItemUtil;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.StatPointType;
import net.wrathofdungeons.dungeonrpg.dungeon.Dungeon;
import net.wrathofdungeons.dungeonrpg.event.CharacterCreationDoneEvent;
import net.wrathofdungeons.dungeonrpg.event.FinalDataLoadedEvent;
import net.wrathofdungeons.dungeonrpg.guilds.Guild;
import net.wrathofdungeons.dungeonrpg.guilds.GuildCreationStatus;
import net.wrathofdungeons.dungeonrpg.guilds.GuildRank;
import net.wrathofdungeons.dungeonrpg.inv.CharacterSelectionMenu;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.items.ItemCategory;
import net.wrathofdungeons.dungeonrpg.items.ItemData;
import net.wrathofdungeons.dungeonrpg.items.PlayerInventory;
import net.wrathofdungeons.dungeonrpg.items.awakening.AwakeningType;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.mobs.handler.TargetHandler;
import net.wrathofdungeons.dungeonrpg.npc.CustomNPC;
import net.wrathofdungeons.dungeonrpg.party.Party;
import net.wrathofdungeons.dungeonrpg.party.PartyMember;
import net.wrathofdungeons.dungeonrpg.professions.Profession;
import net.wrathofdungeons.dungeonrpg.quests.Quest;
import net.wrathofdungeons.dungeonrpg.quests.QuestObjective;
import net.wrathofdungeons.dungeonrpg.quests.QuestStage;
import net.wrathofdungeons.dungeonrpg.skill.PoisonData;
import net.wrathofdungeons.dungeonrpg.skill.Skill;
import net.wrathofdungeons.dungeonrpg.skill.SkillStorage;
import net.wrathofdungeons.dungeonrpg.skill.SkillValues;
import net.wrathofdungeons.dungeonrpg.skins.StoredSkin;
import net.wrathofdungeons.dungeonrpg.util.FormularUtils;
import net.wrathofdungeons.dungeonrpg.util.WorldUtilities;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftItem;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import org.inventivetalent.nicknamer.api.NickNamerAPI;
import org.mineskin.SkinOptions;
import org.mineskin.Visibility;
import org.mineskin.data.Skin;
import org.mineskin.data.SkinCallback;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class GameUser extends User {
    public static HashMap<Player,GameUser> TEMP = new HashMap<Player,GameUser>();

    public static GameUser getUser(Player p){
        if(isLoaded(p)){
            return ((GameUser)User.STORAGE.get(p));
        } else {
            return null;
        }
    }

    public static void load(Player p){
        if(CustomEntity.fromEntity(p) != null || CustomNPC.fromEntity(p) != null || CitizensAPI.getNPCRegistry().isNPC(p)){
            User.STORAGE.remove(p);
            return;
        }

        if(!isLoaded(p)){
            new GameUser(p);
        }
    }

    private User originalUser;
    private Player p;
    private ArrayList<Character> characters;
    private Character currentCharacter;
    private SkillValues skillValues;

    private int hp = 20;
    private int mp = 20;

    private boolean init = false;
    public boolean __associateDamageWithSystem = true;
    private boolean attackCooldown = false;
    private boolean foodCooldown = false;
    private boolean setupMode = false;
    public boolean mayActivateMobs = true;
    public boolean ignoreFistCheck = false;
    public boolean ignoreDamageCheck = false;
    public boolean onGround = true;

    public int lootChestTier = 0;
    public int lootChestLevel = 0;

    public String currentCombo = "";
    public boolean canCastCombo = true;
    public int comboDelay = 0;

    public int merchantAddItemSlot = -1;
    public CustomNPC merchantAddItem = null;
    public CustomItem merchantAddItemHandle = null;
    public int merchantAddMoneyCost = -1;
    public ArrayList<CustomItem> merchantAddItemCosts = null;

    public Quest questModifying = null;
    public QuestStage stageAdding = null;
    public QuestObjective objectiveAdding = null;
    public boolean stageSettingItemsToGet = false;
    public boolean definingRewardItems = false;

    public CustomNPC npcAddTextLine = null;

    private BukkitTask mpRegenTask;
    private BukkitTask hpRegenTask;
    private BukkitTask comboResetTask;

    private ArrayList<BukkitTask> cancellableTasks;

    private boolean dying = false;

    public GuildCreationStatus guildCreationStatus = null;
    public String guildCreationName = null;
    public String guildCreationTag = null;

    public String friendsMenuPlayerToMessage = null;
    public boolean friendsMenuAddPlayer = false;
    public boolean friendsMenuRemovePlayer = false;

    public Horse currentMountEntity = null;
    public int currentMountItemSlot = -1;

    public int barTimer = 0;

    private Hologram plateHologram;
    private TextLine guildPlate;
    private TextLine titlePlate;

    private PoisonData poisonData;
    public LivingEntity lastDamageSource;

    public boolean updatingArmorSkin = false;
    public BukkitTask armorSkinClickTask;

    public Timestamp lastHPLeechTime = null;
    public Timestamp lastMPLeechTime = null;

    public GameUser(Player p){
        super(p);

        TEMP.put(p,this);
    }

    public void resetMount(){
        resetMount(true);
    }

    public void resetMount(boolean eject){
        if(currentMountEntity != null){
            if(eject && currentMountEntity.getPassenger() != null) currentMountEntity.eject();
            if(currentMountEntity != null) currentMountEntity.remove();
        }

        currentMountEntity = null;

        currentMountItemSlot = -1;
    }

    public PoisonData getPoisonData() {
        return poisonData;
    }

    public boolean isPoisoned(){
        return getPoisonData() != null;
    }

    public void setPoisonData(PoisonData poisonData){
        if(this.poisonData != null) this.poisonData.cancelTasks();

        this.poisonData = poisonData;
    }

    public boolean hasFameTitle(){
        return false;
    }

    public void updateHoloPlate(){
        DungeonAPI.sync(() -> {
            if ((!isInGuild() && !hasFameTitle()) || getCurrentCharacter() == null || isRespawning()) {
                removeHoloPlate();
                return;
            }

            Hologram h = null;
            if(plateHologram != null) h = plateHologram;
            if(h == null) h = HologramsAPI.createHologram(DungeonRPG.getInstance(),getSupposedHoloPlateLocation());

            int neededLines = 0;
            if(isInGuild()) neededLines++;
            if(hasFameTitle()) neededLines++;

            ChatColor c = getGuild().getRank(p) == GuildRank.LEADER ? ChatColor.YELLOW : ChatColor.WHITE;
            String guildText = c + getGuild().getName() + ChatColor.GRAY + " [" + getGuild().getTag() + "]";
            String titleText = "";

            if(h.size() != neededLines){
                h.clearLines();

                if(isInGuild()){
                    guildPlate = h.appendTextLine(guildText);
                }

                if(hasFameTitle()){
                    titlePlate = h.appendTextLine(titleText); // TODO: Add fame title plate
                }
            } else {
                if(guildPlate != null){
                    if(isInGuild()){
                        guildPlate.setText(guildText);
                    }
                }

                if(titlePlate != null){
                    if(hasFameTitle()){
                        titlePlate.setText(titleText);
                    }
                }
            }

            h.getVisibilityManager().setVisibleByDefault(true);
            h.getVisibilityManager().hideTo(p);

            h.teleport(getSupposedHoloPlateLocation());

            plateHologram = h;
        });
    }

    public void removeHoloPlate(){
        if(plateHologram != null){
            if(!plateHologram.isDeleted()) plateHologram.delete();

            plateHologram = null;
            guildPlate = null;
            titlePlate = null;
        }
    }

    public Location getSupposedHoloPlateLocation(){
        if(hasFameTitle() && isInGuild()){
            return p.getLocation().clone().add(0,2.57,0);
        } else {
            return p.getLocation().clone().add(0,2.55,0);
        }
    }

    public void spawnMount(CustomItem item,int slot){
        if(currentMountEntity != null || currentMountItemSlot != -1) resetMount();

        currentMountEntity = (Horse)p.getWorld().spawnEntity(p.getLocation(),EntityType.HORSE);

        currentMountEntity.setAdult();
        currentMountEntity.setMaxHealth(10);
        currentMountEntity.setHealth(currentMountEntity.getMaxHealth());

        currentMountEntity.setVariant(item.getMountData().getHorseVariant());
        currentMountEntity.setStyle(item.getMountData().getHorseStyle());
        currentMountEntity.setColor(item.getMountData().getHorseColor());
        currentMountEntity.setJumpStrength(item.getData().getMountJumpStrength()*0.075);
        currentMountEntity.getInventory().setSaddle(new ItemStack(Material.SADDLE));
        WorldUtilities.setEntitySpeed(currentMountEntity,item.getData().getMountSpeed()*0.035);

        currentMountItemSlot = slot;
        currentMountEntity.setPassenger(p);
        TargetHandler.clearGoals(currentMountEntity);
    }

    /*public boolean canAddToInventory(CustomItem item){
        if(getEmptySlotsInInventory() == 0){
            for(ItemStack i : p.getInventory().getContents()){
                if(CustomItem.fromItemStack(i) != null){
                    if(CustomItem.fromItemStack(i).isSameItem(item)){
                        return item.getAmount()+CustomItem.fromItemStack(i).getAmount() <= item.getData().getStackLimit();
                    }
                }
            }
        } else {
            return true;
        }

        return false;
    }

    public void addItem(CustomItem item){
        if(canAddToInventory(item)){
            if(getAmountInInventory(item) > 0){
                int slot = -1;

                for(ItemStack i : p.getInventory().getContents()){
                    if(CustomItem.fromItemStack(i) != null){
                        if(item.isSameItem(CustomItem.fromItemStack(i))){
                            if(item.getAmount()+CustomItem.fromItemStack(i).getAmount() <= item.getData().getStackLimit()){
                                slot = WorldUtilities.getSlotFromItemStack(p.getInventory(),i);
                            }
                        }
                    }
                }

                if(slot > -1){
                    int newAmount = p.getInventory().getItem(slot).getAmount()+item.getAmount();
                    p.getInventory().getItem(slot).setAmount(newAmount);
                    CustomItem.fromItemStack(p.getInventory().getItem(slot)).setAmount(newAmount);
                }
            } else {
                p.getInventory().addItem(item.build(p));
            }
        }
    }*/

    public boolean isDying() {
        return dying;
    }

    public void setDying(boolean dying) {
        this.dying = dying;
    }

    public int getHP(){
        return this.hp;
    }

    public void setHP(int hp){
        this.hp = hp;
        if(this.hp > getMaxHP()) this.hp = getMaxHP();
        updateHPBar();
    }

    public void addHP(double hp){
        addHP(((int)hp));
    }

    public void addHP(int hp){
        hp += this.hp;

        if(hp > getMaxHP()) hp = getMaxHP();

        setHP(hp);
    }

    public int getMaxHP(){
        int maxHP = 0;

        if(getCurrentCharacter() != null){
            Character c = getCurrentCharacter();

            maxHP += FormularUtils.getBaseHP(c);

            maxHP += Math.pow(getCurrentCharacter().getStatpointsTotal(StatPointType.STAMINA),2);

            maxHP += maxHP * (getCurrentCharacter().getTotalValue(AwakeningType.ADDITIONAL_HP) * 0.01);
        } else {
            maxHP = 20;
        }

        if(maxHP <= 0) maxHP = 1;

        return maxHP;
    }

    public void startMPRegenTask(){
        if(getCurrentCharacter() != null && mpRegenTask == null){
            mpRegenTask = new BukkitRunnable(){
                @Override
                public void run() {
                    doMPRegen();
                }
            }.runTaskTimer(DungeonRPG.getInstance(),30,30);
        }
    }

    private void doMPRegen(){
        if(getMP() < getMaxMP()){
            int mp = getMP();
            int mpToAdd = 1;

            mpToAdd += getCurrentCharacter().getStatpointsTotal(StatPointType.INTELLIGENCE)*0.3125;
            if(mpToAdd < 1) mpToAdd = 1;

            mp += mpToAdd;

            if (getCurrentCharacter().getTotalValue(AwakeningType.MP_REGENERATION) > 0)
                mp += mp * (getCurrentCharacter().getTotalValue(AwakeningType.MP_REGENERATION) * 0.01);

            setMP(mp);
        }
    }

    private void doHPRegen(){
        if(getHP() < getMaxHP()){
            int hp = getHP();
            hp += getCurrentCharacter().getLevel();

            if(getCurrentCharacter().getTotalValue(AwakeningType.HP_REGENERATION) > 0) hp += hp*(getCurrentCharacter().getTotalValue(AwakeningType.HP_REGENERATION)*0.01);

            setHP(hp);
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
                    doHPRegen();
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

    public void startComboResetTask(){
        stopComboResetTask();

        comboResetTask = new BukkitRunnable(){
            @Override
            public void run() {
                currentCombo = "";
                comboResetTask = null;
                updateActionBar();
            }
        }.runTaskLater(DungeonRPG.getInstance(),2*20);
    }

    public void stopComboResetTask(){
        if(comboResetTask != null){
            comboResetTask.cancel();
            comboResetTask = null;
        }
    }

    public int getHPPercentage(){
        return (getHP()/getMaxHP())*100;
    }

    public int getMP(){
        return this.mp;
    }

    public void setMP(int mp){
        if(mp > getMaxMP()) mp = getMaxMP();

        this.mp = mp;

        updateHPBar();
    }

    public int getMaxMP(){
        int mp = 20;

        if (getCurrentCharacter() != null) {
            mp += mp * (getCurrentCharacter().getTotalValue(AwakeningType.ADDITIONAL_HP) * 0.01);
        }

        if (mp < 1) mp = 1;
        return mp;
    }

    public void addMP(double mp){
        addMP(((int)hp));
    }

    public void addMP(int mp){
        setMP(this.mp + mp);
    }

    public int getMPPercentage(){
        return (getMP()/getMaxMP())*100;
    }

    @Deprecated
    public void updateHPBar(){
        updateActionBar();
    }

    public void updateActionBar(){
        if(!p.isDead()){
            if(DungeonRPG.SHOW_HP_IN_ACTION_BAR){
                String hpString = ChatColor.DARK_RED.toString() + ChatColor.BOLD.toString() + ChatIcons.HEART + " HP: " + ChatColor.RED + getHP() + "/" + getMaxHP();
                String mpString = ChatColor.DARK_AQUA.toString() + ChatColor.BOLD.toString() + "✸ MP: " + ChatColor.AQUA + getMP() + "/" + getMaxMP();

                String toSend = "            ";

                if(!currentCombo.isEmpty()){
                    String left = ChatColor.GREEN + "L";
                    String middle = ChatColor.GREEN + "M";
                    String right = ChatColor.GREEN + "R";
                    String unknown = ChatColor.GRAY + "?";
                    String unknownPrime = ChatColor.GRAY.toString() + ChatColor.UNDERLINE.toString() + "?" + ChatColor.RESET;
                    String seperator = ChatColor.GRAY + "-" + ChatColor.RESET;

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
                } else {
                    if (actionBarSkillDisplay != null)
                        toSend = actionBarSkillDisplay;
                }

                if (!isRespawning()) BountifulAPI.sendActionBar(p, hpString + "  " + toSend + "  " + mpString);
            }
            p.setMaxHealth(20);

            if (!isRespawning()) {
                double healthPercentage = ((double) hp) / ((double) getMaxHP());
                double manaPercentage = ((double) mp) / ((double) getMaxMP());

                if (healthPercentage <= 0.35)
                    addRedScreenEffect();
                else
                    removeRedScreenEffect();

                if (hp > getMaxHP()) hp = getMaxHP();
                if (hp < 0) hp = 0;
                double healthDis = healthPercentage * p.getMaxHealth();
                if (healthDis > p.getMaxHealth()) healthDis = p.getMaxHealth();
                if (healthDis < 0.5) {
                    healthDis = 20;

                    PlayerDeathEvent event = new PlayerDeathEvent(p, null, 0, null);
                    Bukkit.getPluginManager().callEvent(event);
                }

                p.setHealth(healthDis);

                if (mp > getMaxMP()) mp = getMaxMP();
                if (mp < 0) mp = 0;
                double manaDis = manaPercentage * 20;

                p.setFoodLevel((int) manaDis);
            } else {
                p.setHealth(20);
                p.setFoodLevel(20);
            }
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

    public boolean isInFoodCooldown(){
        return foodCooldown;
    }

    public void setFoodCooldown(boolean b){
        this.foodCooldown = b;
    }

    public Guild getGuild(){
        return getGuildID() > 0 ? Guild.getGuild(getGuildID()) : null;
    }

    public boolean isInGuild(){
        return getGuild() != null;
    }

    private boolean screenRed = false;

    private String reloadingWorld = null;

    public void addRedScreenEffect() {
        if (!screenRed) {
            screenRed = true;

            net.minecraft.server.v1_9_R2.WorldBorder w = new net.minecraft.server.v1_9_R2.WorldBorder();
            w.setSize(1);
            w.setCenter(p.getLocation().getX() + 10_000, p.getLocation().getZ() + 10_000);
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutWorldBorder(w, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE));
        }
    }

    public void removeRedScreenEffect() {
        if (screenRed) {
            screenRed = false;

            net.minecraft.server.v1_9_R2.WorldBorder w = new net.minecraft.server.v1_9_R2.WorldBorder();
            w.setSize(30_000_000);
            w.setCenter(p.getLocation().getX(), p.getLocation().getZ());
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutWorldBorder(w, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE));
        }
    }

    public boolean isScreenRed() {
        return screenRed;
    }

    public void damage(double damage){
        damage(damage,null);
    }

    public void damage(double damage, LivingEntity source){
        if (isDying() || isRespawning()) return;

        lastDamageSource = source;
        this.hp -= damage;

        if(this.hp <= 0){
            if(source != null){
                __associateDamageWithSystem = false;
                //p.damage(0, source);

                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonAPI.getInstance(), new Runnable(){
                    public void run(){
                        __associateDamageWithSystem = true;
                    }
                });
            } else {
                //p.damage(0, source);
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

    public void updateWalkSpeed(){
        double speed = 0.2;

        if(getCurrentCharacter() != null){
            speed += speed*(getCurrentCharacter().getTotalValue(AwakeningType.WALK_SPEED)*0.01);
        }

        if(speed > 1) speed = 1;

        final float s = (float) speed;

        if (s != p.getWalkSpeed())
            p.setWalkSpeed(s);
    }

    public void updateTabList(){
        super.updateTabList();

        if(getCurrentCharacter() == null) return;

        //TabAPI.clearTab(p);

        /*int ping = 5;
        WrappedGameProfile profile = WrappedGameProfile.fromHandle(GameProfileBuilder.getProfile(UUID.randomUUID(),Util.randomString(5),"http://textures.minecraft.net/texture/a6aa6e936534ea2f120418c9aa3f91828e23a9f0f73ca4d3486e961e6f0fa"));

        for(int width = 0; width < TabAPI.getHorizSize()-1; width++){
            for(int height = -1; height < TabAPI.getVertSize()-1; height++){
                TabAPI.setTabString(DungeonRPG.getInstance(),p,width,height,ChatColor.WHITE.toString() + width + " - " + height,ping,profile);
            }
        }

        TabAPI.updatePlayer(p);*/
    }

    private String actionBarSkillDisplay = null;

    public void updateClickComboBar(Skill toCast, int manaCost){
        String s = ChatColor.GREEN + toCast.getName() + " " + ChatColor.GRAY + "[-" + manaCost + " MP]";

        if(!DungeonRPG.SHOW_HP_IN_ACTION_BAR){
            BountifulAPI.sendActionBar(p,s);
        } else {
            //p.setItemInHand(WorldUtilities.updateDisplayName(p.getItemInHand(),DungeonRPG.getSkillIndicatorPrefix() + s));
            actionBarSkillDisplay = s;
            startClickComboClearTask();
            updateActionBar();
        }

        /*p.setItemInHand(WorldUtilities.updateDisplayName(p.getItemInHand(),DungeonRPG.getSkillIndicatorPrefix() + toSend));
        startClickComboClearTask();*/
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
                        Character character = new Character(charID,p);
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
        if(c != null && c.getPlayer().getUniqueId().toString().equals(p.getUniqueId().toString())){
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

    public void giveNormalKnockback(Location from) {
        giveNormalKnockback(from, false);
    }

    public void giveNormalKnockback(Location from, boolean closerLocation){
        if(closerLocation){
            double distance = from.distance(p.getLocation());
            if(distance <= 1){
                giveNormalKnockback(from,false);
                return;
            }

            BlockIterator blocksToAdd = new BlockIterator(from, 0D, ((Double)distance).intValue());
            Location lastLoc = null;
            while (blocksToAdd.hasNext()) {
                lastLoc = blocksToAdd.next().getLocation();
            }

            Location f = from;
            Location fl = from;
            Location finalLoc = from;

            int c = (int) Math.ceil(finalLoc.distance(lastLoc) / 2F) - 1;
            if (c > 0) {
                Vector v = lastLoc.toVector().subtract(finalLoc.toVector()).normalize().multiply(2F);
                Location l = finalLoc.clone();
                for (int i = 0; i < c; i++) {
                    l.add(v);
                    f = fl;
                    fl = finalLoc;
                    finalLoc = l;
                }
            }

            finalLoc = f;

            giveNormalKnockback(finalLoc,false);
        } else {
            p.setVelocity(WorldUtilities.safenVelocity(p.getLocation().toVector().subtract(from.toVector()).setY(-1).multiply(0.5)));
        }
    }

    public void playItemPickupSound(){
        Random random = new Random();
        p.playSound(p.getEyeLocation(), Sound.ENTITY_ITEM_PICKUP, 0.2F, ((random.nextFloat() - random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
    }

    public void playItemPickupEffect(org.bukkit.entity.Item item) {
        if (item != null && item.isValid()) {
            ((EntityLiving) ((CraftPlayer) p).getHandle()).receive(((CraftItem) item).getHandle(), 1);

            item.remove();
        }
    }

    public void updateHandSpeed(){
        updateHandSpeed(CustomItem.fromItemStack(p.getItemInHand()));
    }

    public void updateHandSpeed(CustomItem item){
        if(p.hasPotionEffect(PotionEffectType.SLOW_DIGGING)) p.removePotionEffect(PotionEffectType.SLOW_DIGGING);
        if(p.hasPotionEffect(PotionEffectType.FAST_DIGGING)) p.removePotionEffect(PotionEffectType.FAST_DIGGING);

        if(item != null){
            if(item.getData().getCategory() == ItemCategory.WEAPON_STICK || item.getData().getCategory() == ItemCategory.WEAPON_SHEARS || item.getData().getCategory() == ItemCategory.WEAPON_AXE || item.getData().getCategory() == ItemCategory.WEAPON_BOW){

            } else if(item.getData().getCategory() == ItemCategory.PICKAXE) {
                int miningSpeed = getCurrentCharacter().getTotalValue(AwakeningType.MINING_SPEED);
                if(miningSpeed < -6) miningSpeed = -5;
                if(miningSpeed > 6) miningSpeed = 5;

                if(miningSpeed == -6){
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING,Integer.MAX_VALUE,5,true,true));
                } else if(miningSpeed == -5){
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING,Integer.MAX_VALUE,4,true,true));
                } else if(miningSpeed == -4){
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING,Integer.MAX_VALUE,3,true,true));
                } else if(miningSpeed == -3){
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING,Integer.MAX_VALUE,2,true,true));
                } else if(miningSpeed == -2){
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING,Integer.MAX_VALUE,1,true,true));
                } else if(miningSpeed == -1){
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING,Integer.MAX_VALUE,0,true,true));
                } else if(miningSpeed == 0){
                    // normal speed
                } else if(miningSpeed == 1){
                    p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING,Integer.MAX_VALUE,0,true,true));
                } else if(miningSpeed == 2){
                    p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING,Integer.MAX_VALUE,1,true,true));
                } else if(miningSpeed == 3){
                    p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING,Integer.MAX_VALUE,2,true,true));
                } else if(miningSpeed == 4){
                    p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING,Integer.MAX_VALUE,3,true,true));
                } else if(miningSpeed == 5){
                    p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING,Integer.MAX_VALUE,4,true,true));
                } else if(miningSpeed == 6){
                    p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING,Integer.MAX_VALUE,5,true,true));
                }
            }
        }
    }

    public void playCharacter(Character c){
        if(getCurrentCharacter() == null && c != null && c.getPlayer().getUniqueId().toString().equals(p.getUniqueId().toString())){
            p.closeInventory();
            setCurrentCharacter(c);
            DungeonRPG.updateVanishing();
            DungeonRPG.updateNames();
            c.setLastLogin(new Timestamp(System.currentTimeMillis()));

            for(Character character : getCharacters())
                if(c != character && character.getVariables() != null && character.getVariables().autoJoin){
                    character.getVariables().autoJoin = false;
                    character.saveLoggedOutData();
                }

            c.getVariables().autoJoin = true;

            bukkitReset();
            p.teleport(c.getStoredLocation());
            new BukkitRunnable() {
                @Override
                public void run() {
                    p.teleport(c.getStoredLocation());
                    p.setGameMode(DungeonRPG.PLAYER_DEFAULT_GAMEMODE);
                    updateLevelBar();
                    updateTabList();
                    updateWalkSpeed();
                }
            }.runTaskLater(DungeonRPG.getInstance(), 20);

            getGuild(); // load guild
            for(Profession p : Profession.values()) c.getVariables().getProfessionProgress(p); // load professions

            PlayerInventory inventory = c.getStoredInventory();
            if(inventory != null){
                inventory.loadToPlayer(p);

                if(DungeonRPG.ENABLE_BOWDRAWBACK){
                    p.getInventory().setItem(17,new CustomItem(6,64).build(p));
                } else {
                    if(CustomItem.fromItemStack(p.getInventory().getItem(17)) != null && CustomItem.fromItemStack(p.getInventory().getItem(17)).getData().getId() == 6) p.getInventory().setItem(17,new ItemStack(Material.AIR));
                }

                p.getInventory().setItem(8,new CustomItem(5).build(p));
            } else {
                if(c.getRpgClass() == RPGClass.ARCHER || c.getRpgClass() == RPGClass.HUNTER || c.getRpgClass() == RPGClass.RANGER){
                    p.getInventory().addItem(new CustomItem(1,1,true).build(p));
                } else if(c.getRpgClass() == RPGClass.MERCENARY || c.getRpgClass() == RPGClass.KNIGHT || c.getRpgClass() == RPGClass.SOLDIER){
                    p.getInventory().addItem(new CustomItem(2,1,true).build(p));
                } else if(c.getRpgClass() == RPGClass.MAGICIAN || c.getRpgClass() == RPGClass.WIZARD || c.getRpgClass() == RPGClass.ALCHEMIST){
                    p.getInventory().addItem(new CustomItem(3,1,true).build(p));
                } else if(c.getRpgClass() == RPGClass.ASSASSIN || c.getRpgClass() == RPGClass.NINJA || c.getRpgClass() == RPGClass.BLADEMASTER){
                    p.getInventory().addItem(new CustomItem(4,1,true).build(p));
                }

                p.getInventory().addItem(new CustomItem(54,5).build(p));

                p.getInventory().setItem(8,new CustomItem(5).build(p));

                p.getInventory().setHelmet(new CustomItem(14,1,true).build(p));
                p.getInventory().setChestplate(new CustomItem(10,1,true).build(p));

                if(DungeonRPG.ENABLE_BOWDRAWBACK){
                    p.getInventory().setItem(17,new CustomItem(6,64).build(p));
                }
            }

            setHP(getMaxHP());
            setMP(getMaxMP());
            checkRequirements();

            startMPRegenTask();
            startHPRegenTask();

            p.sendMessage(" ");
            p.sendMessage(ChatColor.DARK_GREEN + "Welcome to Wrath of Dungeons! If you need help, visit " + ChatColor.YELLOW + "www.wrathofdungeons.net");
            p.sendMessage(ChatColor.GREEN + "Please note that you are playing an alpha version of the game. Bugs and glitches may occur..");
            p.sendMessage(ChatColor.BLUE + "Current Level: " + c.getLevel());
            if(c.getRpgClass() != null && c.getRpgClass() != RPGClass.NONE) p.sendMessage(ChatColor.BLUE + "Current Class: " + c.getRpgClass().getName());

            DungeonAPI.executeBungeeCommand("BungeeConsole","callgloballogin " + p.getName() + " " + c.getLevel() + " " + c.getRpgClass().getName());
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

    public void consumeCurrentItem(int amount){
        if(p.getItemInHand() != null){
            if(p.getItemInHand().getAmount() > amount){
                p.getItemInHand().setAmount(p.getItemInHand().getAmount()-amount);
            } else {
                p.setItemInHand(new ItemStack(Material.AIR));
            }
        }
    }

    private boolean f = false;

    public void reloadFriends(){
        super.reloadFriends();

        if(!f){
            f = true;
        } else {
            DungeonRPG.updateNames();
        }
    }

    public void updateName(){
        for(Player all : Bukkit.getOnlinePlayers()){
            if(GameUser.isLoaded(all)){
                GameUser a = GameUser.getUser(all);
                Scoreboard b = a.getScoreboard();

                if(b == null) continue;

                Team t = b.getTeam(p.getName());
                if(t == null) t = b.registerNewTeam(p.getName());
                t.addEntry(p.getName());

                p.setPlayerListName(Util.limitString(getRank().getColor() + p.getName(),16));

                if(getCurrentCharacter() != null){
                    if(all != p){
                        if(a.getParty() != null && a.getParty().hasMember(p)){
                            t.setPrefix(ChatColor.GREEN.toString());
                        } else if(getFriends().contains(all.getUniqueId().toString())){
                            t.setPrefix(ChatColor.AQUA.toString());
                        } else if(isInGuild() && a.isInGuild() && getGuild() == a.getGuild()){
                            t.setPrefix(ChatColor.LIGHT_PURPLE.toString());
                        } else {
                            t.setPrefix(ChatColor.WHITE.toString());
                        }
                    } else {
                        t.setPrefix(ChatColor.YELLOW.toString());
                    }
                } else {
                    t.setPrefix(ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH.toString());
                }

                if(b.getTeam("npcNameReset") == null){
                    Team n = b.registerNewTeam("npcNameReset");
                    n.setNameTagVisibility(NameTagVisibility.NEVER);

                    for(ChatColor color : ChatColor.values()) n.addEntry(color.toString());
                }
            } else {

            }
        }
    }

    public void updateArrows(){
        //if(getCurrentCharacter() != null) p.getInventory().setItem(17,new CustomItem(6,64).build(p));

        if(DungeonRPG.ENABLE_BOWDRAWBACK){
            p.getInventory().setItem(17,new CustomItem(6,64).build(p));
            /*if(isInAttackCooldown()){
                p.getInventory().setItem(17,new ItemStack(Material.AIR));
            } else {
                p.getInventory().setItem(17,new CustomItem(6,64).build(p));
            }*/
        }
    }

    public void checkRequirements(){
        if(getCurrentCharacter() != null){
            DungeonAPI.sync(() -> {
                updateWalkSpeed();
                updateArmorDisplay();

                if (!hasElytraOnChestplate() && isInElytraMode())
                    toggleElytraMode();

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
                    if (chestplate.getData().getCategory() == ItemCategory.ELYTRA) {
                        if (p.getInventory().firstEmpty() != -1) {
                            p.getInventory().setChestplate(null);
                            p.getInventory().addItem(chestplate.build(p));
                        } else {
                            WorldUtilities.dropItem(p.getLocation(), chestplate, p);
                        }

                        p.sendMessage(ChatColor.DARK_RED + "To use your " + chestplate.getData().getName() + ", right-click it whilst holding it in your hand.");
                    } else {
                        if (getCurrentCharacter().getLevel() < chestplate.getData().getNeededLevel()) {
                            if (p.getInventory().firstEmpty() != -1) {
                                p.getInventory().setChestplate(null);
                                p.getInventory().addItem(chestplate.build(p));
                            } else {
                                WorldUtilities.dropItem(p.getLocation(), chestplate, p);
                            }

                            p.sendMessage(ChatColor.DARK_RED + "This chestplate is for level " + chestplate.getData().getNeededLevel() + "+ only.");
                        }

                        if (chestplate.getData().getNeededClass() != RPGClass.NONE && !chestplate.getData().getNeededClass().matches(getCurrentCharacter().getRpgClass())) {
                            if (p.getInventory().firstEmpty() != -1) {
                                p.getInventory().setChestplate(null);
                                p.getInventory().addItem(chestplate.build(p));
                            } else {
                                WorldUtilities.dropItem(p.getLocation(), chestplate, p);
                            }

                            p.sendMessage(ChatColor.DARK_RED + "This chestplate is for a different class.");
                        }
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

    public CustomItem getHelmet() {
        return CustomItem.fromItemStack(p.getInventory().getHelmet());
    }

    public boolean hasHelmet() {
        return getHelmet() != null;
    }

    public CustomItem getChestplate() {
        return CustomItem.fromItemStack(p.getInventory().getChestplate());
    }

    public boolean hasChestplate() {
        return getChestplate() != null;
    }

    public CustomItem getLeggings() {
        return CustomItem.fromItemStack(p.getInventory().getLeggings());
    }

    public boolean hasLeggings() {
        return getLeggings() != null;
    }

    public CustomItem getBoots() {
        return CustomItem.fromItemStack(p.getInventory().getBoots());
    }

    public boolean hasBoots() {
        return getBoots() != null;
    }

    private boolean inElytraMode;
    public BukkitTask elytraResetTask;

    public void startElytraResetTask() {
        if (elytraResetTask != null) {
            elytraResetTask.cancel();
            elytraResetTask = null;
        }

        elytraResetTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (isInElytraMode())
                    toggleElytraMode();
            }
        }.runTaskLater(DungeonRPG.getInstance(), 5 * 20);
    }

    public void toggleElytraMode() {
        toggleElytraMode(null);
    }

    public void toggleElytraMode(CustomItem glider) {
        this.inElytraMode = !this.inElytraMode;

        if (this.inElytraMode) {
            if (hasChestplate()) {
                p.getInventory().setChestplate(getChestplate().build(p, Material.ELYTRA));
            } else {
                if (glider == null) return;
                ItemStack original = glider.build(p);

                p.getInventory().setChestplate(ItemUtil.setUnbreakable(ItemUtil.hideFlags(ItemUtil.namedItem(Material.ELYTRA, original.getItemMeta().getDisplayName(), null, original.getDurability()))));
            }

            startElytraResetTask();
        } else {
            if (elytraResetTask != null) {
                elytraResetTask.cancel();
                elytraResetTask = null;
            }

            if (hasChestplate()) {
                p.getInventory().setChestplate(getChestplate().build(p));
                updateArmorDisplay();
            } else {
                p.getInventory().setChestplate(null);
            }
        }
    }

    public boolean isInElytraMode() {
        return this.inElytraMode;
    }

    public int getEmptySlotsInInventory(){
        int count = 0;
        for (ItemStack i : p.getInventory()) {
            if (i == null) {
                count++;
            }
        }

        return count;
    }

    public boolean isInSetupMode() {
        return setupMode;
    }

    private Hologram soulLightHologram;

    public Hologram getSoulLightHologram() {
        return soulLightHologram;
    }

    public void spawnSoulLight(Location loc, int secondsLeft) {
        if (getSoulLightHologram() == null) {
            soulLightHologram = HologramsAPI.createHologram(DungeonRPG.getInstance(), loc);

            soulLightHologram.appendTextLine(ChatColor.DARK_RED + p.getName());
            soulLightHologram.appendTextLine(ChatColor.RED + "Respawns in " + ChatColor.GRAY + Util.secondsToString((long) secondsLeft));

            soulLightHologram.getVisibilityManager().setVisibleByDefault(true);
            soulLightHologram.getVisibilityManager().hideTo(p);
        }
    }

    public void updateSoulLight(int respawnSecondsLeft) {
        if (getSoulLightHologram() != null)
            ((TextLine) getSoulLightHologram().getLine(1)).setText(ChatColor.RED + "Respawns in " + ChatColor.GRAY + Util.secondsToString((long) respawnSecondsLeft));
    }

    public void removeSoulLight() {
        if (getSoulLightHologram() != null) {
            if (!getSoulLightHologram().isDeleted()) getSoulLightHologram().delete();
            soulLightHologram = null;
        }
    }

    public boolean isRespawning() {
        return getSoulLightHologram() != null && respawnCountdown != null && p.getGameMode() == GameMode.SPECTATOR;
    }

    public void resetTemporaryData(){
        resetTemporaryData(true);
    }

    public void resetTemporaryData(boolean withTasks) {
        // GAME
        currentCombo = "";
        getSkillValues().reset();
        setPoisonData(null);
        merchantAddItem = null;
        merchantAddItemHandle = null;
        merchantAddItemCosts = null;
        merchantAddMoneyCost = -1;
        merchantAddItemSlot = -1;
        npcAddTextLine = null;
        friendsMenuPlayerToMessage = null;
        friendsMenuAddPlayer = false;
        friendsMenuRemovePlayer = false;
        if(comboResetTask != null) comboResetTask.cancel();

        if (withTasks) {
            stopMPRegenTask();
            stopHPRegenTask();

            if (respawnCountdown != null) respawnCountdown.cancel();
        }

        // SETUP
        merchantAddItem = null;
        merchantAddItemHandle = null;
        merchantAddItemCosts = null;
        merchantAddMoneyCost = -1;
        merchantAddItemSlot = -1;
        npcAddTextLine = null;
        lootChestLevel = 0;
        lootChestTier = 0;
    }

    public void setSetupMode(boolean setupMode) {
        this.setupMode = setupMode;

        resetTemporaryData();

        if(this.setupMode){
            if (getCurrentCharacter() != null) getCurrentCharacter().saveData(false, true, false);

            removeHoloPlate();
            removeSoulLight();
            bukkitReset();
            getSkillValues().reset();
            stopMPRegenTask();
            stopHPRegenTask();
            resetTemporaryData();
            resetMount();
            updateWalkSpeed();
            DungeonRPG.updateVanishing();
            setCurrentCharacter(null);
            p.setGameMode(GameMode.CREATIVE);
            p.sendMessage(ChatColor.GREEN + "You are now in setup mode!");

            p.getInventory().addItem(ItemUtil.namedItem(Material.STICK,"Mob Spawn Setter",null));
            p.getInventory().addItem(ItemUtil.namedItem(Material.STICK,"Mob Activation Setter (1)",null));
            p.getInventory().addItem(ItemUtil.namedItem(Material.STICK,"Mob Activation Setter (2)",null));
            p.getInventory().addItem(ItemUtil.namedItem(Material.STICK,"Ore Setter",null));
            p.getInventory().addItem(ItemUtil.namedItem(Material.STICK,"Crafting Station Setter",null));
            p.getInventory().addItem(ItemUtil.namedItem(Material.STICK,"PvP Arena Setter",null));
            p.getInventory().addItem(ItemUtil.namedItem(Material.STICK,"PvP Respawn Setter",null));
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

            while((getCurrentCharacter().getExp() >= (required = FormularUtils.getExpNeededForLevel(getCurrentCharacter().getLevel()+1 + levels))) && (getCurrentCharacter().getLevel() + levels < DungeonRPG.getMaxLevel())){
                getCurrentCharacter().setExp(getCurrentCharacter().getExp()-required);
                levels++;
            }

            if(levels > 0){
                levelUp(levels);
            }

            updateLevelBar();
        }
    }

    public BukkitTask clearClickCombo;

    public void startClickComboClearTask(){
        if(getCurrentCharacter() == null) return;

        startComboResetTask();

        if(DungeonRPG.SHOW_HP_IN_ACTION_BAR){
            if(clearClickCombo != null){
                clearClickCombo.cancel();
                clearClickCombo = null;
            }

            clearClickCombo = new BukkitRunnable(){
                @Override
                public void run() {
                    resetComboDisplay(p.getInventory().getHeldItemSlot());
                }
            }.runTaskLater(DungeonRPG.getInstance(),2*20);
        }
    }

    public void resetComboDisplay(int slot){
        if(getCurrentCharacter() == null) return;

        if(DungeonRPG.SHOW_HP_IN_ACTION_BAR){
            if(clearClickCombo != null){
                clearClickCombo.cancel();
                clearClickCombo = null;
            }

            actionBarSkillDisplay = null;
            /*CustomItem i = CustomItem.fromItemStack(p.getInventory().getItem(slot));
            if(i != null && p.getInventory().getItem(slot) != null){
                i.setAmount(p.getInventory().getItem(slot).getAmount());

                p.setItemInHand(i.build(p));
            }*/

            //currentCombo = "";
        }
    }

    public void levelSkill(Skill skill){
        levelSkill(skill,1);
    }

    public void levelSkill(Skill skill, int times){
        getCurrentCharacter().getVariables().addInvestedSkillPoint(skill);
        int lvl = getCurrentCharacter().getVariables().getInvestedSkillPoints(skill);

        p.sendMessage(ChatColor.GOLD + "[Skill Level Up!] " + ChatColor.YELLOW + skill.getName() + " is now level " + lvl);
        for(String effect : skill.getEffects(lvl).keySet()){
            String value = skill.getEffects(lvl).get(effect);
            String oldValue = skill.getEffects(lvl-1).getOrDefault(effect,null);

            if(oldValue != null){
                if(!oldValue.equals(value)){
                    p.sendMessage(ChatColor.WHITE + "  " + effect + ": " + ChatColor.GRAY + " " + oldValue + ChatColor.DARK_GREEN + " => " + ChatColor.GREEN + value);
                }
            } else {
                p.sendMessage(ChatColor.WHITE + "  " + effect + ": " + ChatColor.GREEN + value);
            }
        }
    }

    public boolean hasElytraOnChestplate(){
        return p.getInventory() != null && p.getInventory().getChestplate() != null && p.getInventory().getChestplate().getType() != null && p.getInventory().getChestplate().getType() == Material.ELYTRA;
    }

    public void checkSkillLevelUp(Skill skill){
        if(getCurrentCharacter() != null &&
           getCurrentCharacter().getVariables().getInvestedSkillPoints(skill) < skill.getMaxInvestingPoints() &&
           getCurrentCharacter().getVariables().getCurrentSkillUses(skill) >= FormularUtils.getNeededSkillUsesForLevel(getCurrentCharacter().getVariables().getInvestedSkillPoints(skill)+1))
            levelSkill(skill);
    }

    public void levelUp(){
        levelUp(1);
    }

    public void levelUp(int times){
        final int[] newBankRow = new int[]{10,20,30,40,50};

        if(getCurrentCharacter() != null){
            for (int i = 0; i < times; i++) {
                if(getCurrentCharacter().mayGetEXP()){
                    getCurrentCharacter().setLevel(getCurrentCharacter().getLevel()+1);
                    getCurrentCharacter().addStatpointsLeft(2);

                    if(getParty() != null){
                        for(PartyMember member : getParty().getMembers()){
                            if(member.p == p) continue;

                            member.p.sendMessage(ChatColor.GREEN + p.getName() + " has reached level " + getCurrentCharacter().getLevel() + "!");
                        }
                    }

                    p.sendMessage(" ");
                    sendCenteredMessage(ChatColor.DARK_AQUA.toString() + ChatColor.BOLD.toString() + "Level Up!");
                    sendCenteredMessage(ChatColor.AQUA.toString() + "You are now level " + getCurrentCharacter().getLevel());
                    p.sendMessage(" ");
                    p.sendMessage(ChatColor.YELLOW + "+2 Stat Points" + ChatColor.GRAY + " [Use them from the Game Menu]");

                    for(int b : newBankRow) if(getCurrentCharacter().getLevel() == b) p.sendMessage(ChatColor.YELLOW + "+1 Bank Row" + ChatColor.GRAY + " [" + getCurrentCharacter().getBankRows() + " total]");

                    for(Quest q : Quest.STORAGE.values()) if(q.getRequiredLevel() == getCurrentCharacter().getLevel()){
                        p.sendMessage(ChatColor.YELLOW + "+ New Quest available! " + ChatColor.GRAY + "[" + q.getName() + "]");
                    }

                    for(Skill skill : SkillStorage.getInstance().getSkills()){
                        if(skill.getRPGClass() == getCurrentCharacter().getRpgClass() && skill.getMinLevel() == getCurrentCharacter().getLevel()){
                            p.sendMessage(ChatColor.DARK_GREEN + "[New Skill available! " + ChatColor.GREEN + skill.getName() + ChatColor.DARK_GREEN + "]");
                        }
                    }

                    p.sendMessage(" ");
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
            getCurrentCharacter().updateBankSize();
            updateInventory();

            p.playSound(p.getEyeLocation(),Sound.ENTITY_PLAYER_LEVELUP,1f,1f);

            BountifulAPI.sendTitle(p,5,3*20,5,ChatColor.DARK_AQUA.toString() + ChatColor.BOLD.toString() + "Level Up!",ChatColor.AQUA.toString() + "New stat points are available!");
        }
    }

    public void updateInventory(){
        if(getCurrentCharacter() != null) getCurrentCharacter().getConvertedInventory(p).loadToPlayer(p);
    }

    public int getAmountInInventory(ItemData data){
        int i = 0;

        for(ItemStack iStack : p.getInventory().getContents()){
            CustomItem c = CustomItem.fromItemStack(iStack);

            if(c != null){
                if(c.getData().getId() == data.getId()){
                    i += iStack.getAmount();
                }
            }
        }

        return i;
    }

    public int getAmountInInventory(CustomItem item){
        int i = 0;

        for(ItemStack iStack : p.getInventory().getContents()){
            CustomItem c = CustomItem.fromItemStack(iStack);

            if(c != null){
                if(c.isSameItem(item)){
                    i += iStack.getAmount();
                }
            }
        }

        return i;
    }

    public void resetVanillaAttackSpeed(){
        p.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(16);
    }

    public boolean mayExecuteArmorCheck = true;

    public String getReloadingWorld() {
        return reloadingWorld;
    }

    public void forceReloadWorld() {
        Location loc = p.getLocation().clone();
        reloadingWorld = p.getWorld().getName();

        World w = DungeonRPG.MAIN_WORLD;
        while (w == p.getWorld())
            w = Bukkit.getWorlds().get(Util.randomInteger(0, Bukkit.getWorlds().size() - 1));

        final World world = w;

        new BukkitRunnable() {
            @Override
            public void run() {
                p.teleport(world.getSpawnLocation());
            }
        }.runTaskLater(DungeonRPG.getInstance(), 1);
        new BukkitRunnable() {
            @Override
            public void run() {
                p.teleport(loc);
            }
        }.runTaskLater(DungeonRPG.getInstance(), 3);

        p.setAllowFlight(p.getAllowFlight());
        p.setGameMode(p.getGameMode());
        p.setMaxHealth(p.getMaxHealth());
        p.setHealth(p.getHealth());
        p.setLevel(p.getLevel());
        p.setExp(p.getExp());

        reloadingWorld = null;
    }

    public StoredSkin storedSkin;
    public Integer[] lastArmorSkinCheckEquipment;

    public void updateArmorDisplay() {
        updateArmorDisplay(true);
    }

    public void updateArmorDisplay(boolean updateInventory) {
        if (DungeonRPG.PREVENT_MINECRAFT_ARMOR) {
            if (getHelmet() != null && getHelmet().getData().hasArmorSkin())
                p.getInventory().setHelmet(CustomItem.fromItemStack(p.getInventory().getHelmet()).build(p, DungeonRPG.ARMOR_SKIN_DISPLAY_ITEM, DungeonRPG.ARMOR_SKIN_DISPLAY_DURABILITY));
            if (p.getInventory().getChestplate() != null && p.getInventory().getChestplate().getType() != Material.ELYTRA && getChestplate() != null && getChestplate().getData().hasArmorSkin())
                p.getInventory().setChestplate(CustomItem.fromItemStack(p.getInventory().getChestplate()).build(p, DungeonRPG.ARMOR_SKIN_DISPLAY_ITEM, DungeonRPG.ARMOR_SKIN_DISPLAY_DURABILITY));
            if (getLeggings() != null && getLeggings().getData().hasArmorSkin())
                p.getInventory().setLeggings(CustomItem.fromItemStack(p.getInventory().getLeggings()).build(p, DungeonRPG.ARMOR_SKIN_DISPLAY_ITEM, DungeonRPG.ARMOR_SKIN_DISPLAY_DURABILITY));
            if (getBoots() != null && getBoots().getData().hasArmorSkin())
                p.getInventory().setBoots(CustomItem.fromItemStack(p.getInventory().getBoots()).build(p, DungeonRPG.ARMOR_SKIN_DISPLAY_ITEM, DungeonRPG.ARMOR_SKIN_DISPLAY_DURABILITY));

            for (int i = 0; i < 36; i++) {
                ItemStack itemStack = p.getInventory().getItem(i);
                if (itemStack == null) continue;
                CustomItem item = CustomItem.fromItemStack(itemStack);

                if (item != null && item.getData().getCategory() == ItemCategory.ARMOR && itemStack.getType() == DungeonRPG.ARMOR_SKIN_DISPLAY_ITEM && itemStack.getDurability() == (short) DungeonRPG.ARMOR_SKIN_DISPLAY_DURABILITY) {
                    p.getInventory().setItem(p.getInventory().first(itemStack), item.build(p));
                }
            }

            if (updateInventory) p.updateInventory();
        }
    }

    private boolean noArmor = false;
    private static boolean timeout = false;
    public boolean reloadWorld = false;

    public void updateArmorSkin() {
        final boolean sendMessages = true;
        if (timeout) return;

        try {
            if (getCurrentCharacter() != null) {
                ArrayList<ItemData> skinsToLoad = new ArrayList<ItemData>();

                for (CustomItem item : getCurrentCharacter().getEquipment())
                    if (item.getData().hasArmorSkin())
                        skinsToLoad.add(item.getData());

                if (skinsToLoad.size() > 0) {
                    noArmor = false;
                    if (!updatingArmorSkin) {
                        if (sendMessages) p.sendMessage(ChatColor.YELLOW + "Your skin is being updated..");
                        GameProfile profile = ((CraftPlayer) p).getProfile();
                        String originalSkinURL = WorldUtilities.getSkinURLFromGameProfile(profile);
                        updatingArmorSkin = true;
                        ArrayList<Integer> a = new ArrayList<Integer>();
                        for (ItemData data : skinsToLoad) a.add(data.getId());
                        StoredSkin storedSkin = StoredSkin.getEqualSkin(a.toArray(new Integer[]{}), originalSkinURL);

                        String key = Util.randomString(1, 60);

                        if (storedSkin != null) {
                            key = storedSkin.key;

                            if (storedSkin.equals(this.storedSkin)) {
                                updatingArmorSkin = false;
                                return;
                            }

                            reloadWorld = true;

                            NickNamerAPI.getNickManager().setSkin(p.getUniqueId(), key);
                            this.storedSkin = storedSkin;

                            if (sendMessages) p.sendMessage(ChatColor.GREEN + "Your skin has been updated!");
                            if (sendMessages)
                                p.sendMessage(ChatColor.GREEN + "Press F3+A if you experience world loading issues.");
                            updatingArmorSkin = false;
                        } else {
                            String[] s = originalSkinURL.split("/");
                            File originalSkin = new File(DungeonRPG.getTemporaryFolder() + "texture_" + s[s.length - 1] + ".png");

                            if (!originalSkin.exists())
                                Util.saveRemoteImageLocally(originalSkinURL, "png", originalSkin);

                            BufferedImage skinImage = ImageIO.read(originalSkin);
                            BufferedImage finalSkin = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);

                            Graphics g = finalSkin.getGraphics();
                            g.drawImage(skinImage, 0, 0, null);

                            for (ItemData itemData : skinsToLoad) {
                                try {
                                    String url = "https://skins.wrathofdungeons.net/armorSkinParts/" + itemData.getId() + ".png";
                                    File skinPartFile = new File(DungeonRPG.getTemporaryFolder() + "armorSkinPart_" + itemData.getId() + ".png");

                                    if (!skinPartFile.exists())
                                        Util.saveRemoteImageLocally(url, "png", skinPartFile);

                                    if (!skinPartFile.exists())
                                        continue;

                                    BufferedImage skinPartImage = ImageIO.read(skinPartFile);
                                    g.drawImage(skinPartImage, 0, 0, null);
                                } catch (Exception e) {
                                }
                            }

                            File outputFile = new File(DungeonRPG.getTemporaryFolder() + "finalSkin_" + key + ".png");
                            if (ImageIO.write(finalSkin, "png", outputFile)) {
                                final String k = key;
                                final GameUser self = this;

                                DungeonRPG.getMineskinClient().generateUpload(outputFile, SkinOptions.create("wod_" + key, WorldUtilities.getModelFromGameProfile(profile), Visibility.PRIVATE), new SkinCallback() {
                                    @Override
                                    public void done(Skin skin) {
                                        reloadWorld = true;
                                        GameProfile newProfile = WorldUtilities.getGameProfileFromSkinData(skin.data);

                                        NickNamerAPI.getNickManager().loadCustomSkin(k, newProfile);
                                        NickNamerAPI.getNickManager().setSkin(p.getUniqueId(), k);

                                        if (sendMessages)
                                            p.sendMessage(ChatColor.GREEN + "Your skin has been updated!");
                                        if (sendMessages)
                                            p.sendMessage(ChatColor.GREEN + "Press F3+A if you experience world loading issues.");
                                        updatingArmorSkin = false;

                                        self.storedSkin = new StoredSkin(k, a.toArray(new Integer[]{}), originalSkinURL);
                                        self.lastArmorSkinCheckEquipment = a.toArray(new Integer[]{});
                                    }

                                    @Override
                                    public void error(String errorMessage) {
                                        if (sendMessages)
                                            p.sendMessage(ChatColor.RED + "Failed to update your skin! Please try again later.");
                                        updatingArmorSkin = false;
                                        self.storedSkin = null;

                                        if (errorMessage.equalsIgnoreCase("Too many requests")) {
                                            if (!timeout) {
                                                timeout = true;
                                                new BukkitRunnable() {
                                                    @Override
                                                    public void run() {
                                                        timeout = false;
                                                    }
                                                }.runTaskLater(DungeonRPG.getInstance(), 10 * 20);
                                            }
                                        }
                                    }

                                    @Override
                                    public void exception(Exception exception) {
                                        if (sendMessages)
                                            p.sendMessage(ChatColor.RED + "Failed to update your skin! Please try again later.");
                                        updatingArmorSkin = false;
                                        self.storedSkin = null;
                                        exception.printStackTrace();
                                    }
                                });
                            } else {
                                if (sendMessages)
                                    p.sendMessage(ChatColor.RED + "Failed to update your skin! Please try again later.");
                                updatingArmorSkin = false;
                                this.storedSkin = null;
                            }
                        }
                    }
                } else {
                    if (noArmor) return;
                    if (sendMessages) p.sendMessage(ChatColor.YELLOW + "Your skin is being updated..");
                    reloadWorld = true;

                    if (NickNamerAPI.getNickManager().hasSkin(p.getUniqueId()))
                        NickNamerAPI.getNickManager().removeSkin(p.getUniqueId());

                    this.storedSkin = null;
                    this.lastArmorSkinCheckEquipment = new Integer[]{};

                    if (sendMessages) p.sendMessage(ChatColor.GREEN + "Your skin has been updated!");
                    if (sendMessages)
                        p.sendMessage(ChatColor.GREEN + "Press F3+A if you experience world loading issues.");
                    noArmor = true;
                }
            } else {
                reloadWorld = true;
                if (NickNamerAPI.getNickManager().hasSkin(p.getUniqueId()))
                    NickNamerAPI.getNickManager().removeSkin(p.getUniqueId());

                this.storedSkin = null;
                this.lastArmorSkinCheckEquipment = new Integer[]{};
            }

            updateArmorDisplay();
        } catch (Exception e) {
            if (sendMessages) p.sendMessage(ChatColor.RED + "Failed to update your skin! Please try again later.");
            this.storedSkin = null;
            this.lastArmorSkinCheckEquipment = new Integer[]{};
            updatingArmorSkin = false;
            e.printStackTrace();
        }
    }

    private File s(String base, String suffix) {
        File file = null;

        while (file == null || file.exists() || file.isDirectory()) {
            file = new File(base + Util.randomString(1, 25) + suffix);
        }

        return file;
    }

    @Override
    public void bukkitReset() {
        super.bukkitReset();

        resetVanillaAttackSpeed();
    }

    public boolean hasInInventory(ItemData data, int amount){
        int left = amount;

        for(ItemStack iStack : p.getInventory().getContents()){
            CustomItem c = CustomItem.fromItemStack(iStack);

            if(c != null){
                if(c.getData().getId() == data.getId()){
                    left -= iStack.getAmount();
                }
            }

            if(left <= 0) return true;
        }

        return false;
    }

    public void removeFromInventory(ItemData data, int amount){
        if(hasInInventory(data,amount)){
            int left = amount;

            for(ItemStack iStack : p.getInventory().getContents()){
                CustomItem c = CustomItem.fromItemStack(iStack);

                if(c != null){
                    if(c.getData().getId() == data.getId()){
                        if(left < iStack.getAmount()){
                            iStack.setAmount(iStack.getAmount()-left);
                            return;
                        } else if(left == iStack.getAmount()){
                            p.getInventory().setItem(p.getInventory().first(iStack),new ItemStack(Material.AIR));
                            return;
                        } else if(left > iStack.getAmount()){
                            left -= iStack.getAmount();
                            p.getInventory().setItem(p.getInventory().first(iStack),new ItemStack(Material.AIR));

                            if(left <= 0) return;
                        }
                    }
                }
            }
        }
    }

    public int getTotalMoneyInInventory(){
        int i = 0;

        for(ItemStack iStack : p.getInventory().getContents()){
            CustomItem c = CustomItem.fromItemStack(iStack);

            if(c != null){
                if(c.getData().getId() == 7){
                    i += iStack.getAmount();
                } else if(c.getData().getId() == 8){
                    i += iStack.getAmount()*64;
                } else if(c.getData().getId() == 9){
                    i += iStack.getAmount()*4096;
                }
            }
        }

        return i;
    }

    public boolean removeMoneyFromInventory(int i){
        return removeMoneyFromInventory(i,0);
    }

    public boolean removeMoneyFromInventory(int money, int neededSpaces){
        if(getTotalMoneyInInventory() >= money){
            ArrayList<ItemStack> items = new ArrayList<ItemStack>();

            for(ItemStack iStack : p.getInventory().getContents()){
                CustomItem c = CustomItem.fromItemStack(iStack);

                if(c != null){
                    if(c.getData().getId() == 7 || c.getData().getId() == 8 || c.getData().getId() == 9){
                        items.add(iStack);
                    }
                }
            }

            CustomItem[] newMoney = WorldUtilities.convertNuggetAmount(getTotalMoneyInInventory()-money);
            int neededSlots = newMoney.length+neededSpaces;

            if(neededSlots <= (getEmptySlotsInInventory()+items.size())){
                p.getInventory().removeItem(items.toArray(new ItemStack[]{}));
                for(CustomItem c : newMoney) p.getInventory().addItem(c.build(p));

                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
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

                cancellableTasks = new ArrayList<BukkitTask>();

                FinalDataLoadedEvent event = new FinalDataLoadedEvent(p);
                Bukkit.getPluginManager().callEvent(event);
            } catch(Exception e){
                e.printStackTrace();
            }
        });
    }

    public ArrayList<BukkitTask> getCancellableTasks() {
        return cancellableTasks;
    }

    public void cancelAllTasks(){
        for(BukkitTask t : getCancellableTasks()) t.cancel();

        getCancellableTasks().clear();

        CustomNPC.READING.remove(p);
    }

    public int respawnCountdownCount = 0;
    public BukkitTask respawnCountdown;

    private void loadCharacters(){
        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `characters` WHERE `uuid` = ?");
            ps.setString(1,p.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                this.characters.add(new Character(rs.getInt("id"),p));
            }

            MySQLManager.getInstance().closeResources(rs,ps);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void saveData(){
        saveData(false);
    }

    public Dungeon getCurrentDungeon(){
        if(getParty() != null){
            for(Dungeon dungeon : Dungeon.STORAGE)
                if(dungeon.getParty() == getParty())
                    return dungeon;
        }

        return null;
    }

    public boolean isInDungeon(){
        return getCurrentDungeon() != null;
    }

    public void saveData(boolean continueCharsel){
        saveData(continueCharsel, true);
    }

    public void saveData(boolean continueCharsel, boolean resetSaveLocation) {
        if (getCurrentCharacter() != null && !setupMode)
            getCurrentCharacter().saveData(continueCharsel, resetSaveLocation, !Bukkit.getPluginManager().isPluginEnabled(DungeonRPG.getInstance()));
        super.saveData();
    }
}
