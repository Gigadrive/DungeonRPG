package net.wrathofdungeons.dungeonrpg.inv;

import de.dytanic.cloudnet.api.CloudAPI;
import de.dytanic.cloudnet.lib.player.CloudPlayer;
import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.util.ItemUtil;
import net.wrathofdungeons.dungeonapi.util.PlayerUtilities;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.inventivetalent.menubuilder.inventory.InventoryMenuBuilder;
import org.inventivetalent.menubuilder.inventory.ItemListener;

import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

public class FriendsMenu {
    public static void openFor(Player p){
        openFor(p,1);
    }

    public static void openFor(Player p, int page){
        GameUser u = GameUser.getUser(p);

        ItemStack pl = ItemUtil.namedItem(Material.STAINED_GLASS_PANE, " ", null, 15);
        ItemStack prev = ItemUtil.namedItem(Material.ARROW, ChatColor.GOLD + "<< " + ChatColor.AQUA + "Previous page", null);
        ItemStack next = ItemUtil.namedItem(Material.ARROW, ChatColor.AQUA + "Next page" + ChatColor.GOLD + " >>", null);
        ItemStack close = ItemUtil.namedItem(Material.BARRIER, ChatColor.DARK_RED + "Close", null);
        ItemStack add = ItemUtil.namedItem(Material.STAINED_CLAY,ChatColor.GREEN + "Add someone to your friends list",null,5);
        ItemStack remove = ItemUtil.namedItem(Material.STAINED_CLAY,ChatColor.RED + "Remove someone from your friends list",null,14);
        ItemStack info = ItemUtil.namedItem(Material.SIGN,ChatColor.GOLD + "Information",new String[]{
                ChatColor.GRAY + "Total: " + ChatColor.WHITE + u.getFriends().size(),
                ChatColor.GRAY + "Online: " + ChatColor.WHITE + u.getOnlineFriends().size()
        });

        ArrayList<Friend> friends = new ArrayList<Friend>();

        for(String s : u.getFriends()){
            Friend f = new Friend();

            f.uuid = s;
            f.status = FriendStatus.FRIENDS;

            friends.add(f);
        }

        for(String s : PlayerUtilities.getFriendRequestsFromUUID(p.getUniqueId())){
            Friend f = new Friend();

            f.uuid = s;
            f.status = FriendStatus.OUTGOING_REQUEST;

            friends.add(f);
        }

        for(String s : PlayerUtilities.getFriendRequestsToUUID(p.getUniqueId())){
            Friend f = new Friend();

            f.uuid = s;
            f.status = FriendStatus.INCOMING_REQUEST;

            friends.add(f);
        }

        int sizePerPage = 36;
        int total = friends.size();

        double d = (((double)total)/((double)sizePerPage));
        int maxPages = ((Double)d).intValue();
        if(maxPages < d) maxPages++;
        if(maxPages <= 0) maxPages = 1;

        InventoryMenuBuilder inv = new InventoryMenuBuilder(Util.MAX_INVENTORY_SIZE);
        inv.withTitle("Friends (" + page + "/" + maxPages + ")");

        ItemListener listener = ((player, action, item) -> {});

        int slot = 0;
        for(Friend f : friends.stream().skip((page-1) * sizePerPage).limit(sizePerPage).collect(Collectors.toCollection(ArrayList::new))){
            UUID uuid = UUID.fromString(f.uuid);
            FriendStatus status = f.status;

            String name = PlayerUtilities.getNameFromUUID(uuid);
            if(name == null || name.isEmpty()) continue;

            CloudPlayer cloudPlayer = CloudAPI.getInstance().getOnlinePlayer(uuid);
            boolean online = cloudPlayer != null && cloudPlayer.getServer() != null && !cloudPlayer.getServer().isEmpty();

            if(status == FriendStatus.FRIENDS){
                ItemStack i = new ItemStack(Material.SKULL_ITEM,1,(short)3);
                SkullMeta iM = (SkullMeta)i.getItemMeta();

                iM.setOwner(name);

                iM.setDisplayName(ChatColor.WHITE + name);
                ArrayList<String> iL = new ArrayList<String>();

                if(online){
                    iL.add(ChatColor.DARK_GREEN + "Online on " + ChatColor.GREEN + cloudPlayer.getServer());
                } else {
                    iL.add(ChatColor.DARK_RED + "Offline");
                }

                Player p2 = Bukkit.getPlayer(uuid);
                if(p2 != null){
                    if(GameUser.isLoaded(p2)){
                        GameUser u2 = GameUser.getUser(p2);

                        if(u2.getCurrentCharacter() != null){
                            iL.add(ChatColor.GRAY + "Class: " + ChatColor.WHITE + u2.getCurrentCharacter().getRpgClass().getName());
                            iL.add(ChatColor.GRAY + "Level: " + ChatColor.WHITE + u2.getCurrentCharacter().getLevel());
                            if(u2.getGuild() != null) iL.add(ChatColor.GRAY + "Guild: " + ChatColor.WHITE + u2.getGuild().getName());
                        }
                    }
                }

                if(online){
                    iL.add(" ");
                    iL.add(ChatColor.YELLOW + "Click to message " + name + ".");

                    listener = ((player, action, item) -> {
                        u.friendsMenuPlayerToMessage = name;

                        p.closeInventory();
                        p.sendMessage(ChatColor.GOLD + "Enter the message you want to send to " + ChatColor.YELLOW + name + ChatColor.GOLD + ".");
                        p.sendMessage(ChatColor.GRAY + "(Type 'cancel' to cancel.)");
                    });
                }

                iM.setLore(iL);
                i.setItemMeta(iM);

                inv.withItem(slot,i,listener,InventoryMenuBuilder.ALL_CLICK_TYPES);
            } else if(status == FriendStatus.INCOMING_REQUEST){
                ItemStack i = new ItemStack(Material.STAINED_CLAY,1,(short)4);
                ItemMeta iM = i.getItemMeta();

                iM.setDisplayName(ChatColor.WHITE + name);
                ArrayList<String> iL = new ArrayList<String>();

                for(String s : Util.getWordWrapLore(name + " has requested to be your friend!"))
                    iL.add(ChatColor.GRAY + s);

                iL.add(" ");

                iL.add(ChatColor.GREEN + "Left-click to accept");
                iL.add(ChatColor.RED + "Right-click to deny");

                listener = ((player, action, item) -> {
                    if(action == ClickType.LEFT){
                        p.closeInventory();
                        DungeonAPI.executeBungeeCommand(p.getName(),"friend accept " + name);
                    } else if(action == ClickType.RIGHT){
                        p.closeInventory();
                        DungeonAPI.executeBungeeCommand(p.getName(),"friend deny " + name);
                    }
                });

                iM.setLore(iL);
                i.setItemMeta(iM);

                inv.withItem(slot,i,listener,InventoryMenuBuilder.ALL_CLICK_TYPES);
            } else if(status == FriendStatus.OUTGOING_REQUEST){
                ItemStack i = new ItemStack(Material.STAINED_CLAY,1,(short)5);
                ItemMeta iM = i.getItemMeta();

                iM.setDisplayName(ChatColor.WHITE + "Request to: " + ChatColor.GREEN + name);
                ArrayList<String> iL = new ArrayList<String>();

                for(String s : Util.getWordWrapLore("You have requested to be friends with " + name + "!"))
                    iL.add(ChatColor.GRAY + s);

                iL.add(" ");

                iL.add(ChatColor.RED + "Click to cancel");

                iM.setLore(iL);
                i.setItemMeta(iM);

                listener = ((player, action, item) -> {
                    p.closeInventory();
                    DungeonAPI.executeBungeeCommand(p.getName(),"friend retract " + name);
                });

                inv.withItem(slot,i,listener,InventoryMenuBuilder.ALL_CLICK_TYPES);
            }

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

        inv.withItem(45,add,((player, action, item) -> {
            u.friendsMenuAddPlayer = true;

            p.closeInventory();
            p.sendMessage(ChatColor.GOLD + "Enter the name of the player you want to add as a friend.");
            p.sendMessage(ChatColor.GRAY + "(Type 'cancel' to cancel.)");
        }),ClickType.LEFT);

        inv.withItem(46,remove,((player, action, item) -> {
            u.friendsMenuRemovePlayer = true;

            p.closeInventory();
            p.sendMessage(ChatColor.GOLD + "Enter the name of the player you want to remove from your friends list.");
            p.sendMessage(ChatColor.GRAY + "(Type 'cancel' to cancel.)");
        }),ClickType.LEFT);

        if(page != 1) inv.withItem(47,prev, ((player, clickType, itemStack) -> openFor(p,page-1)), ClickType.LEFT);
        inv.withItem(49,close, ((player, clickType, itemStack) -> GameMenu.openFor(p)), ClickType.LEFT);
        if(maxPages > page) inv.withItem(51,next, ((player, clickType, itemStack) -> openFor(p,page+1)), ClickType.LEFT);

        inv.withItem(inv.getInventory().getSize()-1,info);

        inv.show(p);
    }

    public static class Friend {
        public String uuid;
        public FriendStatus status;
    }

    private enum FriendStatus {
        FRIENDS,INCOMING_REQUEST,OUTGOING_REQUEST
    }
}
