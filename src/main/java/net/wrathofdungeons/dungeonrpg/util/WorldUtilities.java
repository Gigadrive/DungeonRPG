package net.wrathofdungeons.dungeonrpg.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.mineskin.data.Skin;

import java.lang.reflect.Method;
import java.util.ArrayList;

import static net.citizensnpcs.api.npc.NPC.*;
import static net.citizensnpcs.api.npc.NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA;
import static net.citizensnpcs.api.npc.NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN_METADATA;

public class WorldUtilities {
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
                    //GameProfile profile = new Gson().fromJson("{\"id\":\"" + skin.data.uuid.toString() + "\",\"name\":\"" + skinName + "\",\"properties\":[{\"signature\":\"" + skin.data.texture.signature + "\",\"name\":\"textures\",\"value\":\"" + skin.data.texture.value + "\"}]}",GameProfile.class);
                    GameProfile profile = new GameProfile(skin.data.uuid,skinName);
                    profile.getProperties().put("textures",new Property("textures",skin.data.texture.value,skin.data.texture.signature));

                    Class<net.citizensnpcs.npc.skin.Skin> clazz = net.citizensnpcs.npc.skin.Skin.class;
                    Method met = clazz.getDeclaredMethod("setData", GameProfile.class);

                    met.setAccessible(true);
                    met.invoke(net.citizensnpcs.npc.skin.Skin.get(skinName),profile);
                } catch(Exception e){
                    e.printStackTrace();
                }

                skinnable.getSkinTracker().notifySkinChange();
            } else {
                //System.out.println("SKINNABLE IS NULL");
            }
        } else {
            npc.data().remove(PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA);
            npc.data().remove(PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN_METADATA);
        }
    }
}
