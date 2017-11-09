package net.wrathofdungeons.dungeonrpg.inv;

import net.wrathofdungeons.dungeonapi.util.ItemUtil;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.inventivetalent.menubuilder.inventory.InventoryMenuBuilder;

public class InteractionMenu {
    public static void open(Player p, Player p2){
        if(GameUser.isLoaded(p) && GameUser.isLoaded(p2)){
            GameUser u = GameUser.getUser(p);
            GameUser u2 = GameUser.getUser(p2);

            if(u.getCurrentCharacter() != null && u2.getCurrentCharacter() != null){
                InventoryMenuBuilder inv = new InventoryMenuBuilder(Util.INVENTORY_1ROW);
                inv.withTitle("Interact with " + p2.getName() + " [Lv " + u2.getCurrentCharacter().getLevel() + "]");

                inv.withItem(0, ItemUtil.hideFlags(ItemUtil.namedItem(Material.DIAMOND_SWORD, ChatColor.GOLD + "Challenge to a duel",null)), (player, action, item) -> {
                    p.closeInventory();
                    p.chat("/duel " + p2.getName());
                }, ClickType.LEFT);

                inv.withItem(1, ItemUtil.hideFlags(ItemUtil.namedItem(Material.CHEST, ChatColor.GOLD + "Request a trade",null)), (player, action, item) -> {
                    p.closeInventory();
                    p.chat("/trade " + p2.getName());
                }, ClickType.LEFT);

                if(u2.getParty() != null && u2.getParty().getInvited().contains(p)){
                    inv.withItem(2, ItemUtil.hideFlags(ItemUtil.namedItem(Material.FIREWORK, ChatColor.GOLD + "Accept party request",null)), (player, action, item) -> {
                        p.closeInventory();
                        p.chat("/party accept " + p2.getName());
                    }, ClickType.LEFT);
                } else {
                    inv.withItem(2, ItemUtil.hideFlags(ItemUtil.namedItem(Material.FIREWORK, ChatColor.GOLD + "Invite to party",null)), (player, action, item) -> {
                        p.closeInventory();
                        if(u.getParty() == null) p.chat("/party create");
                        p.chat("/party invite " + p2.getName());
                    }, ClickType.LEFT);
                }

                if(u2.getGuild() != null && u2.getGuild().getInvitedPlayers().contains(p)){
                    inv.withItem(3, ItemUtil.hideFlags(ItemUtil.namedItem(Material.PAINTING, ChatColor.GOLD + "Accept guild request",null)), (player, action, item) -> {
                        p.closeInventory();
                        p.chat("/guild accept " + u2.getGuild().getId());
                    }, ClickType.LEFT);
                } else {
                    inv.withItem(3, ItemUtil.hideFlags(ItemUtil.namedItem(Material.PAINTING, ChatColor.GOLD + "Invite to guild",null)), (player, action, item) -> {
                        p.closeInventory();
                        p.chat("/guild invite " + p2.getName());
                    }, ClickType.LEFT);
                }

                inv.withItem(8, ItemUtil.hideFlags(ItemUtil.namedItem(Material.BARRIER, ChatColor.DARK_RED + "Close",null)), (player, action, item) -> p.closeInventory(), ClickType.LEFT);

                inv.show(p);
            }
        }
    }
}
