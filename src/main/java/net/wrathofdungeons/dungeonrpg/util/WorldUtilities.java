package net.wrathofdungeons.dungeonrpg.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.minecraft.server.v1_9_R2.AttributeInstance;
import net.minecraft.server.v1_9_R2.AttributeModifier;
import net.minecraft.server.v1_9_R2.EntityInsentient;
import net.minecraft.server.v1_9_R2.GenericAttributes;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.npc.CustomNPC;
import net.wrathofdungeons.dungeonrpg.regions.Region;
import net.wrathofdungeons.dungeonrpg.regions.RegionLocation;
import net.wrathofdungeons.dungeonrpg.regions.RegionLocationType;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.mineskin.Model;
import org.mineskin.data.Skin;
import org.mineskin.data.SkinData;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

import static net.citizensnpcs.api.npc.NPC.*;

public class WorldUtilities {
    public static HashMap<Location,Boolean> pvpArenaResults = new HashMap<Location,Boolean>();

    public static Item dropItem(Location loc, CustomItem item){
        return dropItem(loc,item,null);
    }

    public static Item dropItem(Location loc, CustomItem item, Player p){
        Item i = loc.getWorld().dropItem(loc,item.build(p));

        if(p != null){
            addAssignmentData(i,p);
        }

        return i;
    }

    public static void addAssignmentData(Item i, Player p){
        i.setMetadata("assignedPlayer", new FixedMetadataValue(DungeonRPG.getInstance(),p.getName()));
        i.setMetadata("dropTime", new FixedMetadataValue(DungeonRPG.getInstance(),System.currentTimeMillis()));
    }

    public static ArrayList<Location> getParticleCircle(Location center){
        return getParticleCircle(center,4);
    }

    public static ArrayList<Location> getParticleCircle(Location center, double radius){
        return getParticleCircle(center,radius,15);
    }

    public static String getReadableLocation(Location loc){
        return "[" + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + "]";
    }

    public static ItemStack updateDisplayName(ItemStack i, String displayName){
        ItemMeta iM = i.getItemMeta();
        iM.setDisplayName(displayName);
        i.setItemMeta(iM);

        return i;
    }

    public static ArrayList<Location> getParticleCircle(Location center, double radius, int amount){
        World world = center.getWorld();
        double increment = (2 * Math.PI) / amount;
        ArrayList<Location> locations = new ArrayList<Location>();
        for(int i = 0;i < amount; i++) {
            double angle = i * increment;
            double x = center.getX() + (radius * Math.cos(angle));
            double z = center.getZ() + (radius * Math.sin(angle));
            locations.add(new Location(world, x, center.getY(), z));
        }

        return locations;
    }

    public static boolean isPvPArena(Location loc){
        if(pvpArenaResults.containsKey(loc)){
            return pvpArenaResults.get(loc);
        } else {
            boolean b = false;

            for(Region region : Region.getRegions(false)){
                if(region.getLocations().size() > 0){
                    for(RegionLocation location : region.getLocations(RegionLocationType.PVP_ARENA)){
                        if(location.world.equalsIgnoreCase(loc.getWorld().getName()) && location.toBukkitLocation().getBlockX() == loc.getBlockX() && location.toBukkitLocation().getBlockZ() == loc.getBlockZ()){
                            b = true;
                            break;
                        }
                    }
                }
            }

            pvpArenaResults.put(loc,b);
            return b;
        }
    }

    public static boolean isUnsafeVelocity(Vector vel) {
        double x = vel.getX();
        double y = vel.getY();
        double z = vel.getZ();

        return x > 4 || x < -4 || y > 4 || y < -4 || z > 4 || z < -4;
    }

    public static Vector safenVelocity(Vector vel) {
        if (vel.getX() > 4) vel.setX(4);
        if (vel.getX() < -4) vel.setX(-4);

        if (vel.getY() > 4) vel.setY(4);
        if (vel.getY() < -4) vel.setY(-4);

        if (vel.getZ() > 4) vel.setZ(4);
        if (vel.getZ() < -4) vel.setZ(-4);

        return vel;
    }

    public static boolean isValidEntity(LivingEntity e){
        return e.getType() == EntityType.PLAYER || CustomNPC.fromEntity(e) != null || CustomEntity.fromEntity(e) != null;
    }

    public static CustomItem[] convertNuggetAmount(int nuggets){
        ArrayList<CustomItem> a = new ArrayList<CustomItem>();

        int blocks = 0;
        int ingots = 0;

        while(nuggets >= 64){
            ingots++;
            nuggets -= 64;
        }

        while(ingots >= 64){
            blocks++;
            ingots -= 64;
        }

        if(nuggets > 0) a.add(new CustomItem(7,nuggets));
        if(ingots > 0) a.add(new CustomItem(8,ingots));
        if(blocks > 0) a.add(new CustomItem(9,blocks));

        return a.toArray(new CustomItem[]{});
    }

    public static void setEntitySpeed(LivingEntity livingEntity,double speed){
        if(livingEntity != null){
            if(livingEntity.getType() == EntityType.PLAYER){
                ((Player)livingEntity).setWalkSpeed((float)speed);
            } else {
                EntityInsentient nmsEntity = (EntityInsentient) ((CraftLivingEntity)livingEntity).getHandle();

                AttributeInstance attributes = nmsEntity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED);

                double toAdd = speed-getEntitySpeed(livingEntity);

                attributes.b(new AttributeModifier(CustomEntity.movementSpeedUID,"wod movement speed",toAdd, AttributeOperation.ADD));
            }
        }
    }

    public static Vector rotateVector(Vector vector, double whatAngle) {
        double sin = Math.sin(whatAngle);
        double cos = Math.cos(whatAngle);
        double x = vector.getX() * cos + vector.getZ() * sin;
        double z = vector.getX() * -sin + vector.getZ() * cos;

        return vector.setX(x).setZ(z);
    }

    public static boolean isMount(Entity entity){
        for(Player p : Bukkit.getOnlinePlayers()){
            if(GameUser.isLoaded(p)){
                GameUser u = GameUser.getUser(p);

                if(u.currentMountEntity == entity) return true;
            }
        }

        return false;
    }

    public static int getSlotFromItem(Inventory inventory, org.bukkit.inventory.ItemStack stack){
        for(int i = 0; i < inventory.getSize(); i++){
            if(inventory.getItem(i) == stack) return i;
        }

        return -1;
    }

    public static int getSlotFromItem(Inventory inventory, CustomItem item){
        for(int i = 0; i < inventory.getSize(); i++){
            if(inventory.getItem(i) != null && CustomItem.fromItemStack(inventory.getItem(i)) != null && CustomItem.fromItemStack(inventory.getItem(i)).isSameItem(item)) return i;
        }

        return -1;
    }

    public static double getEntitySpeed(LivingEntity livingEntity){
        if(livingEntity != null){
            EntityInsentient nmsEntity = (EntityInsentient) ((CraftLivingEntity)livingEntity).getHandle();

            if(nmsEntity != null){
                AttributeInstance attributes = nmsEntity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED);

                if(attributes != null){
                    return attributes.getValue();
                } else {
                    return 0;
                }
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public static void applySkinToNPC(NPC npc, Skin skin){
        applySkinToNPC(npc,skin,Util.randomString(3,16));
    }

    public static void applySkinToNPC(NPC npc, Skin skin, String skinName){
        applySkinToNPC(npc,skin,skinName,20);
    }

    public static void applySkinToNPC(NPC npc, Skin skin, String skinName, int repeat){
        if(skin == null || skinName == null) return;

        //System.out.println("[APPLYING SKIN] NAME: " + skinName + " | ID: " + skin.id);

        npc.data().setPersistent(PLAYER_SKIN_UUID_METADATA, skinName);
        npc.data().setPersistent(PLAYER_SKIN_USE_LATEST,false);

        if(skin != null){
            npc.data().setPersistent(PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA,skin.data.texture.value);
            if(skin.data.texture.signature != null){
                npc.data().setPersistent(PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN_METADATA,skin.data.texture.signature);
            } else {
                System.out.println("SIGNATURE IS NULL");
            }

            if(npc.getEntity() == null){
                //System.out.println("entity is null");

                if(repeat > 0){
                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            applySkinToNPC(npc,skin,skinName,repeat-1);
                        }
                    }.runTaskLater(DungeonRPG.getInstance(),2);

                    return;
                }
            }

            SkinnableEntity skinnable = npc.getEntity() instanceof SkinnableEntity ? (SkinnableEntity)npc.getEntity() : null;
            if(skinnable != null){
                try {
                    GameProfile profile = getGameProfileFromSkinData(skin.data, skinName);

                    Class<net.citizensnpcs.npc.skin.Skin> clazz = net.citizensnpcs.npc.skin.Skin.class;
                    Method met = clazz.getDeclaredMethod("setData", GameProfile.class);

                    met.setAccessible(true);
                    met.invoke(net.citizensnpcs.npc.skin.Skin.get(skinName,false),profile);
                } catch(Exception e){
                    e.printStackTrace();
                }

                skinnable.getSkinTracker().notifySkinChange(false);
            } else {
                //System.out.println("SKINNABLE IS NULL");
            }
        } else {
            npc.data().remove(PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA);
            npc.data().remove(PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN_METADATA);
        }
    }

    public static GameProfile getGameProfileFromSkinData(SkinData skinData) {
        String skinName = Util.randomString(3, 16);

        GameProfile profile = new GameProfile(skinData.uuid, skinName);
        profile.getProperties().put("textures", new Property("textures", skinData.texture.value, skinData.texture.signature));

        return profile;
    }

    public static String getSkinURLFromGameProfile(GameProfile profile) {
        for (Property property : profile.getProperties().get("textures"))
            if (property.getName().equals("textures")) {
                String s = new String(Base64.getDecoder().decode(property.getValue()));

                JsonObject o = new JsonParser().parse(s).getAsJsonObject();
                return o.get("textures").getAsJsonObject().get("SKIN").getAsJsonObject().get("url").getAsString();
            }

        return null;
    }

    public static Model getModelFromGameProfile(GameProfile profile) {
        for (Property property : profile.getProperties().get("textures"))
            if (property.getName().equals("textures")) {
                String s = new String(Base64.getDecoder().decode(property.getValue()));

                JsonObject skin = new JsonParser().parse(s).getAsJsonObject().get("textures").getAsJsonObject().get("SKIN").getAsJsonObject();
                if (skin.get("metadata") != null)
                    return skin.get("metadata").getAsJsonObject().get("model").getAsString().equalsIgnoreCase("slim") ? Model.SLIM : Model.DEFAULT;
            }

        return Model.DEFAULT;
    }

    public static GameProfile getGameProfileFromSkinData(SkinData skinData, String skinName) {
        GameProfile profile = new GameProfile(skinData.uuid, skinName);
        profile.getProperties().put("textures", new Property("textures", skinData.texture.value, skinData.texture.signature));

        return profile;
    }
}
