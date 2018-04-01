package net.wrathofdungeons.dungeonrpg.inv;

import de.dytanic.cloudnet.lib.player.CloudPlayer;
import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonapi.util.ItemUtil;
import net.wrathofdungeons.dungeonapi.util.PlayerUtilities;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.guilds.GuildMember;
import net.wrathofdungeons.dungeonrpg.guilds.GuildRank;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.inventivetalent.menubuilder.inventory.InventoryMenuBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class GuildMenu {
    public static void openFor(Player p){
        openFor(p, 1);
    }

    public static void openFor(Player p, int page) {
        GameUser u = GameUser.getUser(p);
        if (u.getCurrentCharacter() == null || !u.isInGuild()) return;

        ItemStack pl = ItemUtil.namedItem(Material.STAINED_GLASS_PANE, " ", null, 15);
        ItemStack prev = ItemUtil.namedItem(Material.ARROW, ChatColor.GOLD + "<< " + ChatColor.AQUA + "Previous page", null);
        ItemStack next = ItemUtil.namedItem(Material.ARROW, ChatColor.AQUA + "Next page" + ChatColor.GOLD + " >>", null);
        ItemStack close = ItemUtil.namedItem(Material.BARRIER, ChatColor.DARK_RED + "Close", null);
        ItemStack options = ItemUtil.namedItem(Material.DIODE, ChatColor.GOLD + "Guild Options", null);
        ItemStack info = ItemUtil.namedItem(Material.SIGN, ChatColor.GOLD + u.getGuild().getName() + ChatColor.GRAY + " [" + u.getGuild().getTag() + "]", new String[]{
                ChatColor.GRAY + "Total Members: " + ChatColor.WHITE + u.getGuild().getMembers().size(),
                ChatColor.GRAY + "Leader: " + ChatColor.WHITE + PlayerUtilities.getNameFromUUID(u.getGuild().getMembers(GuildRank.LEADER)[0].getUUID()),
                ChatColor.GRAY + "Creator: " + ChatColor.WHITE + PlayerUtilities.getNameFromUUID(u.getGuild().getCreator()),
                ChatColor.GRAY + "Created at: " + ChatColor.WHITE + u.getGuild().getCreated().toGMTString()
        });

        ArrayList<GuildMember> members = new ArrayList<GuildMember>();
        members.addAll(u.getGuild().getMembers());

        int sizePerPage = 36;
        int total = members.size();

        double d = (((double) total) / ((double) sizePerPage));
        int maxPages = ((Double) d).intValue();
        if (maxPages < d) maxPages++;
        if (maxPages <= 0) maxPages = 1;

        InventoryMenuBuilder inv = new InventoryMenuBuilder(Util.MAX_INVENTORY_SIZE);
        inv.withTitle("Guild (" + page + "/" + maxPages + ")");

        int slot = 0;
        for (GuildMember member : members.stream().skip((page - 1) * sizePerPage).limit(sizePerPage).collect(Collectors.toCollection(ArrayList::new))) {
            String name = PlayerUtilities.getNameFromUUID(member.getUUID());
            CloudPlayer cloudPlayer = member.toCloudPlayer();
            boolean online = cloudPlayer != null && cloudPlayer.getServer() != null && !cloudPlayer.getServer().isEmpty();

            ItemStack i = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta iM = (SkullMeta) i.getItemMeta();
            iM.setOwner(name);
            iM.setDisplayName(ChatColor.GOLD + name);

            ArrayList<String> iL = new ArrayList<String>();
            iL.add(ChatColor.GRAY + "Rank: " + ChatColor.WHITE + member.getRank().getName());
            iL.add(ChatColor.GRAY + "Joined: " + ChatColor.WHITE + member.getTimeJoined().toGMTString());

            iL.add(" ");

            if (online) {
                iL.add(ChatColor.DARK_GREEN + "Online on " + ChatColor.GREEN + cloudPlayer.getServer());

                Player p2 = Bukkit.getPlayer(member.getUUID());
                if (p2 != null) {
                    if (GameUser.isLoaded(p2)) {
                        GameUser u2 = GameUser.getUser(p2);

                        if (u2.getCurrentCharacter() != null) {
                            iL.add(ChatColor.GRAY + "Class: " + ChatColor.WHITE + u2.getCurrentCharacter().getRpgClass().getName());
                            iL.add(ChatColor.GRAY + "Level: " + ChatColor.WHITE + u2.getCurrentCharacter().getLevel());
                        }
                    }
                }

                if (u.getGuild().getRank(p) == GuildRank.LEADER || (u.getGuild().getRank(p) == GuildRank.OFFICER && member.getRank() == GuildRank.MEMBER))
                    iL.add(ChatColor.YELLOW + "Click to manage " + name + ".");
            } else {
                iL.add(ChatColor.DARK_RED + "Offline");
            }

            iM.setLore(iL);
            i.setItemMeta(iM);

            inv.withItem(slot, i, ((player, action, item) -> {
                if (u.getGuild().getRank(p) == GuildRank.LEADER || (u.getGuild().getRank(p) == GuildRank.OFFICER && member.getRank() == GuildRank.MEMBER)) {
                    openManageMenu(u, member, page);
                }
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

        inv.withItem(45, info);

        if (page != 1) inv.withItem(47, prev, ((player, clickType, itemStack) -> openFor(p, page - 1)), ClickType.LEFT);
        inv.withItem(49, close, ((player, clickType, itemStack) -> GameMenu.openFor(p)), ClickType.LEFT);
        if (maxPages > page)
            inv.withItem(51, next, ((player, clickType, itemStack) -> openFor(p, page + 1)), ClickType.LEFT);

        inv.withItem(inv.getInventory().getSize() - 1, options, ((player, action, item) -> {
            p.sendMessage(ChatColor.RED + "Coming soon.");
        }), ClickType.LEFT);

        inv.show(p);
    }

    private static void openManageMenu(GameUser u, GuildMember member, int pageToReturn) {
        Player p = u.getPlayer();
        String name = PlayerUtilities.getNameFromUUID(member.getUUID());

        ItemStack pl = ItemUtil.namedItem(Material.STAINED_GLASS_PANE, " ", null, 15);
        ItemStack close = ItemUtil.namedItem(Material.BARRIER, ChatColor.DARK_RED + "Close", null);

        InventoryMenuBuilder inv = new InventoryMenuBuilder(Util.INVENTORY_3ROWS);
        inv.withTitle("Manage guild member " + name);

        if (u.getGuild().getRank(p) == GuildRank.LEADER) {
            if (member.getRank() == GuildRank.MEMBER) {
                inv.withItem(3, ItemUtil.hideFlags(ItemUtil.namedItem(Material.GOLD_HELMET, ChatColor.GOLD + "Promote " + name + " to Officer", null)), ((player, action, item) -> {
                    p.closeInventory();

                    for (Command cmd : DungeonAPI.getCommandManager().getCommands())
                        if (Arrays.asList(cmd.getNames()).contains("guild"))
                            cmd.execute(p, "guild", new String[]{"promote", name});
                }), ClickType.LEFT);
            } else if (member.getRank() == GuildRank.OFFICER) {
                inv.withItem(3, ItemUtil.hideFlags(ItemUtil.namedItem(Material.IRON_HELMET, ChatColor.GOLD + "Demote " + name + " to Member", null)), ((player, action, item) -> {
                    p.closeInventory();

                    for (Command cmd : DungeonAPI.getCommandManager().getCommands())
                        if (Arrays.asList(cmd.getNames()).contains("guild"))
                            cmd.execute(p, "guild", new String[]{"demote", name});
                }), ClickType.LEFT);
            }

            inv.withItem(5, ItemUtil.hideFlags(ItemUtil.namedItem(Material.DIAMOND_SWORD, ChatColor.GOLD + "Transfer guild leadership to " + name, null)), ((player, action, item) -> {
                InventoryMenuBuilder inv2 = new InventoryMenuBuilder(Util.INVENTORY_1ROW);
                inv2.withTitle("Are you sure?");

                inv2.withItem(2, ItemUtil.namedItem(Material.STAINED_CLAY, ChatColor.DARK_GREEN.toString() + ChatColor.BOLD.toString() + "YES, " + ChatColor.GREEN + "Transfer guild leadership to " + name, null, 5), ((player1, action1, item1) -> {
                    p.closeInventory();

                    for (Command cmd : DungeonAPI.getCommandManager().getCommands())
                        if (Arrays.asList(cmd.getNames()).contains("guild"))
                            cmd.execute(p, "guild", new String[]{"transfer", name});
                }), ClickType.LEFT);

                inv2.withItem(6, ItemUtil.namedItem(Material.STAINED_CLAY, ChatColor.DARK_RED.toString() + ChatColor.BOLD.toString() + "NO, " + ChatColor.RED + "Keep guild leadership", null, 14), ((player1, action1, item1) -> {
                    openManageMenu(u, member, pageToReturn);
                }), ClickType.LEFT);

                inv2.show(p);
            }), ClickType.LEFT);
        }

        inv.withItem(4, ItemUtil.hideFlags(ItemUtil.namedItem(Material.LAVA_BUCKET, ChatColor.GOLD + "Kick " + name + " from the guild", null)), ((player, action, item) -> {
            InventoryMenuBuilder inv2 = new InventoryMenuBuilder(Util.INVENTORY_1ROW);
            inv2.withTitle("Are you sure?");

            inv2.withItem(2, ItemUtil.namedItem(Material.STAINED_CLAY, ChatColor.DARK_GREEN.toString() + ChatColor.BOLD.toString() + "YES, " + ChatColor.GREEN + "Kick " + name + " from the guild", null, 5), ((player1, action1, item1) -> {
                p.closeInventory();

                for (Command cmd : DungeonAPI.getCommandManager().getCommands())
                    if (Arrays.asList(cmd.getNames()).contains("guild"))
                        cmd.execute(p, "guild", new String[]{"kick", name});
            }), ClickType.LEFT);

            inv2.withItem(6, ItemUtil.namedItem(Material.STAINED_CLAY, ChatColor.DARK_RED.toString() + ChatColor.BOLD.toString() + "NO, " + ChatColor.RED + "Keep " + name + " in the guild", null, 14), ((player1, action1, item1) -> {
                openManageMenu(u, member, pageToReturn);
            }), ClickType.LEFT);

            inv2.show(p);
        }), ClickType.LEFT);

        inv.withItem(9, pl);
        inv.withItem(10, pl);
        inv.withItem(11, pl);
        inv.withItem(12, pl);
        inv.withItem(13, pl);
        inv.withItem(14, pl);
        inv.withItem(15, pl);
        inv.withItem(16, pl);
        inv.withItem(17, pl);

        inv.withItem(22, close, ((player, action, item) -> {
            openFor(p, pageToReturn);
        }), ClickType.LEFT);

        inv.show(p);
    }
}
