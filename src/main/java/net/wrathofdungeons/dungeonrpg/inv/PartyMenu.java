package net.wrathofdungeons.dungeonrpg.inv;

import net.wrathofdungeons.dungeonapi.util.ItemUtil;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.party.Party;
import net.wrathofdungeons.dungeonrpg.party.PartyMember;
import net.wrathofdungeons.dungeonrpg.party.PartyRank;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.util.FormularUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.inventivetalent.menubuilder.inventory.InventoryMenuBuilder;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class PartyMenu {
    public static void openFor(Player p){
        openFor(p,1);
    }

    public static void openFor(Player p, int page){
        if(!GameUser.isLoaded(p)) return;
        GameUser u = GameUser.getUser(p);
        if(u.getCurrentCharacter() == null) return;

        Party party = u.getParty();

        ItemStack pl = ItemUtil.namedItem(Material.STAINED_GLASS_PANE, " ", null, 15);
        ItemStack prev = ItemUtil.namedItem(Material.ARROW, ChatColor.GOLD + "<< " + ChatColor.AQUA + "Previous page", null);
        ItemStack next = ItemUtil.namedItem(Material.ARROW, ChatColor.AQUA + "Next page" + ChatColor.GOLD + " >>", null);
        ItemStack close = ItemUtil.namedItem(Material.BARRIER, ChatColor.DARK_RED + "Close", null);

        ArrayList<PartyMember> members = new ArrayList<PartyMember>();
        members.addAll(party.getMembers());

        int sizePerPage = 36;
        int total = members.size();

        double d = (((double)total)/((double)sizePerPage));
        int maxPages = ((Double)d).intValue();
        if(maxPages < d) maxPages++;

        InventoryMenuBuilder inv = new InventoryMenuBuilder(Util.MAX_INVENTORY_SIZE);
        inv.withTitle("Party (" + page + "/" + maxPages + ")");

        int slot = 0;
        for(PartyMember member : members.stream().skip((page-1) * sizePerPage).limit(sizePerPage).collect(Collectors.toCollection(ArrayList::new))){
            Player p2 = member.p;
            GameUser u2 = GameUser.getUser(p2);

            ItemStack i = new ItemStack(Material.SKULL_ITEM,1,(short)3);
            SkullMeta iM = (SkullMeta)i.getItemMeta();
            iM.setOwner(p2.getName());
            ArrayList<String> iL = new ArrayList<String>();

            iM.setDisplayName(ChatColor.GREEN + p2.getName());
            if(u2.getCurrentCharacter() != null){
                if(member.rank == PartyRank.LEADER){
                    iL.add(ChatColor.GRAY + "Role: " + ChatColor.WHITE + "Party Leader");
                } else {
                    iL.add(ChatColor.GRAY + "Role: " + ChatColor.WHITE + "Party Member");
                }
                iL.add(ChatColor.GRAY + "Class: " + ChatColor.WHITE + u2.getCurrentCharacter().getRpgClass().getName());
                iL.add(ChatColor.GRAY + "Level: " + ChatColor.WHITE + u2.getCurrentCharacter().getLevel());
                iL.add(ChatColor.GRAY + "EXP: " + ChatColor.WHITE + Util.round((u2.getCurrentCharacter().getExp()/ FormularUtils.getExpNeededForLevel(u2.getCurrentCharacter().getLevel()+1))*100,2) + "%");

                if(p != p2){
                    iL.add(" ");
                    iL.add(ChatColor.LIGHT_PURPLE + "Click to interact with this player.");
                }
            } else {
                iL.add(ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "Currently not logged in.");
            }

            iM.setLore(iL);
            i.setItemMeta(iM);

            inv.withItem(slot,i,((player, action, item) -> {
                if(p != p2 && u2.getCurrentCharacter() != null) InteractionMenu.open(p,p2);
            }), ClickType.LEFT);

            slot++;
        }

        inv.withItem(36, pl);
        inv.withItem(37, pl);
        inv.withItem(38, pl);
        inv.withItem(39, pl);
        inv.withItem(40, pl);
        inv.withItem(41, pl);
        inv.withItem(42, pl);
        inv.withItem(43, pl);
        inv.withItem(44, pl);

        if(page != 1) inv.withItem(47,prev, ((player, clickType, itemStack) -> openFor(p,page-1)), ClickType.LEFT);
        inv.withItem(49,close, ((player, clickType, itemStack) -> GameMenu.openFor(p)), ClickType.LEFT);
        if(maxPages > page) inv.withItem(51,next, ((player, clickType, itemStack) -> openFor(p,page+1)), ClickType.LEFT);

        inv.show(p);
    }
}
