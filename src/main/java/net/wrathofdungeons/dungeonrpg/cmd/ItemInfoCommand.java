package net.wrathofdungeons.dungeonrpg.cmd;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
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
                net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(p.getItemInHand());
                if(nmsItem != null && nmsItem.hasTag()){
                    NBTTagCompound tag = nmsItem.getTag();

                    p.sendMessage(ChatColor.GREEN + "NBT Data:");
                    for(String s : tag.c()) p.sendMessage(s);
                }
            } else {
                p.sendMessage(ChatColor.RED + "Unknown item.");
            }
        } else {
            p.sendMessage(ChatColor.RED + "You have to hold an item in your hand.");
        }
    }
}
