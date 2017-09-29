package net.wrathofdungeons.dungeonrpg.inv;

import net.wrathofdungeons.dungeonapi.util.ItemUtil;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.npc.CustomNPC;
import net.wrathofdungeons.dungeonrpg.npc.CustomNPCType;
import net.wrathofdungeons.dungeonrpg.npc.MerchantOffer;
import net.wrathofdungeons.dungeonrpg.npc.MerchantOfferCost;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.util.WorldUtilities;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.inventivetalent.menubuilder.inventory.InventoryMenuBuilder;

import java.util.ArrayList;
import java.util.Arrays;

public class MerchantSetupMenu implements Listener {
    public static void open(Player p, CustomNPC merchant){
        if(merchant.getNpcType() != CustomNPCType.MERCHANT) throw new IllegalArgumentException("NPC must be a merchant!");

        GameUser u = GameUser.getUser(p);
        InventoryMenuBuilder inv = new InventoryMenuBuilder(Util.MAX_INVENTORY_SIZE);
        inv.withTitle("Merchant Setup");

        for(MerchantOffer offer : merchant.getOffers()){
            ItemStack i = new CustomItem(offer.itemToBuy,offer.amount).build(p);
            ItemMeta iM = i.getItemMeta();
            ArrayList<String> iL = new ArrayList<String>();
            if(iM.hasLore()) iL.addAll(iM.getLore());

            ArrayList<CustomItem> offerItems = new ArrayList<CustomItem>();
            if(offer.moneyCost > 0) offerItems.addAll(Arrays.asList(WorldUtilities.convertNuggetAmount(offer.moneyCost)));
            for(MerchantOfferCost cost : offer.itemCost) offerItems.add(new CustomItem(cost.item,cost.amount));

            iL.add(ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH.toString() + Util.SCOREBOARD_LINE_SEPERATOR);

            for(CustomItem c : offerItems) iL.add(ChatColor.GRAY + "[" + c.getAmount() + "x " + ChatColor.stripColor(c.getData().getName()) + "]");

            iL.add(" ");
            iL.add(ChatColor.GREEN + "[Left-click to modify]");
            iL.add(ChatColor.RED + "[Right-click to delete]");

            iM.setLore(iL);
            i.setItemMeta(iM);

            inv.withItem(offer.slot,i,((player, action, item) -> {
                if(action == ClickType.LEFT){
                    p.closeInventory();
                    p.sendMessage(ChatColor.GRAY + "To modify an offer please delete it and re-add it.");
                } else if(action == ClickType.RIGHT){
                    InventoryMenuBuilder inv2 = new InventoryMenuBuilder(Util.INVENTORY_1ROW);
                    inv2.withTitle("Are you sure?");

                    inv2.withItem(2,ItemUtil.namedItem(Material.WOOL,ChatColor.GREEN + "No, KEEP this item",null,5), ((player1, action1, item1) -> open(p,merchant)),ClickType.LEFT);
                    inv2.withItem(6,ItemUtil.namedItem(Material.WOOL,ChatColor.RED + "Yes, DELETE this item",null,14), ((player1, action1, item1) -> {
                        merchant.getOffers().remove(offer);

                        open(p,merchant);
                    }),ClickType.LEFT);

                    inv2.show(p);
                }
            }),ClickType.LEFT,ClickType.RIGHT);
        }

        int slot = 0;
        while(slot <= 44){
            if(inv.getInventory().getItem(slot) == null){
                final int s = slot;
                inv.withItem(slot, ItemUtil.namedItem(Material.WOOL, ChatColor.GREEN + "Click to add an item at slot " + ChatColor.YELLOW + String.valueOf(slot),null,5), ((player, action, item) -> {
                    u.merchantAddItem = merchant;
                    u.merchantAddItemSlot = s;
                    p.closeInventory();
                    p.sendMessage(ChatColor.GOLD + "Enter the ID of the item that you want to add.");
                    p.sendMessage(ChatColor.GOLD + "Use ID[:AMOUNT] as a format template.");
                    p.sendMessage(ChatColor.GRAY + "(Type 'cancel' to cancel.)");
                }), ClickType.LEFT);
            }

            slot++;
        }

        inv.withItem(45, ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
        inv.withItem(46, ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
        inv.withItem(47, ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
        inv.withItem(48, ItemUtil.namedItem(Material.STAINED_GLASS_PANE,"",null,15));
        inv.withItem(49, ItemUtil.namedItem(Material.BARRIER, ChatColor.DARK_RED + "Close",null), ((player, action, item) -> player.closeInventory()), ClickType.LEFT);
        inv.withItem(50, ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
        inv.withItem(51, ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
        inv.withItem(52, ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));
        inv.withItem(53, ItemUtil.namedItem(Material.STAINED_GLASS_PANE," ",null,15));

        inv.show(p);
    }
}
