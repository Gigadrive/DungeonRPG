package net.wrathofdungeons.dungeonrpg.inv;

import net.wrathofdungeons.dungeonapi.util.ItemUtil;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.guilds.GuildCreationStatus;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.inventivetalent.menubuilder.inventory.InventoryMenuBuilder;

public class GuildMasterMenu {
    public static void openFor(Player p){
        GameUser u = GameUser.getUser(p);
        InventoryMenuBuilder inv = new InventoryMenuBuilder(Util.INVENTORY_1ROW);
        inv.withTitle("Guild Master");

        inv.withItem(0,ItemUtil.hideFlags(ItemUtil.namedItem(Material.DIAMOND_SWORD,ChatColor.GOLD + "Create a guild",new String[]{" ",ChatColor.YELLOW + "Cost:",ChatColor.GRAY + "3 Golden Blocks"})),((player, action, item) -> {
            p.closeInventory();

            if(!u.isInGuild()){
                if(u.guildCreationStatus == null){
                    if(u.getTotalMoneyInInventory() >= 4096*3){
                        u.guildCreationStatus = GuildCreationStatus.CHOOSING_NAME;
                        p.sendMessage(" ");
                        p.sendMessage(ChatColor.DARK_AQUA + "Please choose a name for your guild, by typing it in the chat.");
                        p.sendMessage(ChatColor.GRAY + "(Type 'cancel' to cancel the creation.)");
                        p.sendMessage(" ");
                    } else {
                        p.sendMessage(ChatColor.RED + "You do not have enough money to create a guild.");
                    }
                } else {
                    p.sendMessage(ChatColor.RED + "You are already creating a guild.");
                }
            } else {
                p.sendMessage(ChatColor.RED + "You are already in a guild.");
            }
        }), ClickType.LEFT);

        inv.withItem(8, ItemUtil.namedItem(Material.BARRIER, ChatColor.DARK_RED + "Close",null),((player, action, item) -> player.closeInventory()), ClickType.LEFT);

        inv.show(p);
    }
}
