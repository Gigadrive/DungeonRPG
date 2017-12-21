package net.wrathofdungeons.dungeonrpg.cmd;

import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.util.NBTTypeID;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;

public class ItemInfoCommand extends Command {
    public ItemInfoCommand(){
        super("iteminfo", Rank.GM);
    }

    @Override
    public void execute(Player p, String label, String[] args) {
        if(p.getItemInHand() != null){
            CustomItem item = CustomItem.fromItemStack(p.getItemInHand());

            if(item != null){
                p.sendMessage(ChatColor.GREEN + "Data ID: " + item.getData().getId());
                p.sendMessage(ChatColor.GREEN + "JSON Data:" + ChatColor.GRAY + item.toString());
                net.minecraft.server.v1_9_R2.ItemStack nmsItem = CraftItemStack.asNMSCopy(p.getItemInHand());
                if(nmsItem != null && nmsItem.hasTag()){
                    NBTTagCompound tag = nmsItem.getTag();

                    p.sendMessage(ChatColor.GREEN + "NBT Data:");
                    for(String s : tag.c()){
                        switch(tag.d(s)){
                            case NBTTypeID.BYTE:
                                p.sendMessage(ChatColor.GRAY + s + ": " + ChatColor.WHITE + tag.getByte(s));
                                break;
                            case NBTTypeID.SHORT:
                                p.sendMessage(ChatColor.GRAY + s + ": " + ChatColor.WHITE + tag.getShort(s));
                                break;
                            case NBTTypeID.INTEGER:
                                p.sendMessage(ChatColor.GRAY + s + ": " + ChatColor.WHITE + tag.getInt(s));
                                break;
                            case NBTTypeID.LONG:
                                p.sendMessage(ChatColor.GRAY + s + ": " + ChatColor.WHITE + tag.getLong(s));
                                break;
                            case NBTTypeID.FLOAT:
                                p.sendMessage(ChatColor.GRAY + s + ": " + ChatColor.WHITE + tag.getFloat(s));
                                break;
                            case NBTTypeID.DOUBLE:
                                p.sendMessage(ChatColor.GRAY + s + ": " + ChatColor.WHITE + tag.getDouble(s));
                                break;
                            case NBTTypeID.BYTE_ARRAY:
                                p.sendMessage(ChatColor.GRAY + s + ": " + ChatColor.WHITE + tag.getByteArray(s));
                                break;
                            case NBTTypeID.STRING:
                                p.sendMessage(ChatColor.GRAY + s + ": " + ChatColor.WHITE + tag.getString(s));
                                break;
                            case NBTTypeID.INTEGER_ARRAY:
                                p.sendMessage(ChatColor.GRAY + s + ": " + ChatColor.WHITE + tag.getIntArray(s));
                                break;
                        }
                    }
                }
            } else {
                p.sendMessage(ChatColor.RED + "Unknown item.");
            }
        } else {
            p.sendMessage(ChatColor.RED + "You have to hold an item in your hand.");
        }
    }
}
