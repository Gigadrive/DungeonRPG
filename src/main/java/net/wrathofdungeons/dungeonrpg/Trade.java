package net.wrathofdungeons.dungeonrpg;

import net.wrathofdungeons.dungeonapi.util.ItemUtil;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashMap;

public class Trade {
    private static ArrayList<Trade> STORAGE = new ArrayList<Trade>();
    private static ArrayList<Request> REQUESTS = new ArrayList<Request>();

    public static final int[] player1slots = new int[]{9,10,11,12,18,19,20,21,27,28,29,30,36,37,38,39};
    public static final int[] player2slots = new int[]{14,15,16,17,23,24,25,26,32,33,34,35,41,42,43,44};

    public static final int MAX_OFFERS = player1slots.length;

    public static Request getRequest(Player from, Player to){
        for(Request r : REQUESTS){
            if(r.from.equals(from.getName()) && r.to.equals(to.getName())){
                return r;
            }
        }

        return null;
    }

    public static Request createRequest(Player from, Player to){
        if(getRequest(from,to) != null || getRequest(to,from) != null) return null;

        Request r = new Request(from.getName(),to.getName());

        REQUESTS.add(r);

        return r;
    }

    public static void clearRequests(Player p){
        ArrayList<Request> toRemove = new ArrayList<Request>();

        for(Request r : REQUESTS) if(r.to.equals(p.getName()) || r.from.equals(p.getName())) toRemove.add(r);

        REQUESTS.removeAll(toRemove);
    }

    public static void removeRequest(Request request){
        REQUESTS.remove(request);
    }

    public static Trade getTrade(Player p){
        for(Trade t : STORAGE){
            if(t.isInTrade(p)) return t;
        }

        return null;
    }

    public static boolean isTrading(Player p){
        return getTrade(p) != null;
    }

    public static Trade startTrade(Player p, Player p2){
        Trade t = new Trade(p,p2);
        t.initInventory();
        t.setStatus(Status.TRADING);

        STORAGE.add(t);

        return t;
    }

    private Player p;
    private Player p2;

    private Inventory inv1;
    private Inventory inv2;
    private Status status;

    private ArrayList<ItemStack> offers1;
    private ArrayList<ItemStack> offers2;

    private boolean player1ready;
    private boolean player2ready;

    public Trade(Player p, Player p2){
        this.p = p;
        this.p2 = p2;

        this.offers1 = new ArrayList<ItemStack>();
        this.offers2 = new ArrayList<ItemStack>();

        this.player1ready = false;
        this.player2ready = false;
    }

    public Player getPlayer1() {
        return p;
    }

    public Player getPlayer2() {
        return p2;
    }

    public ArrayList<ItemStack> getOffers1() {
        return offers1;
    }

    public ArrayList<ItemStack> getOffers2() {
        return offers2;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isPlayer1Ready() {
        return player1ready;
    }

    public boolean isPlayer2Ready() {
        return player2ready;
    }

    public void setPlayer1Ready(boolean player1ready) {
        this.player1ready = player1ready;
    }

    public void setPlayer2Ready(boolean player2ready) {
        this.player2ready = player2ready;
    }

    public boolean isPlayer1(Player p){
        return getPlayer1().getName().equals(p.getName());
    }

    public boolean isPlayer2(Player p){
        return getPlayer2().getName().equals(p.getName());
    }

    public void updateInventories(){
        HashMap<Player,Inventory> inventories = new HashMap<Player,Inventory>();
        inventories.put(p,inv1);
        inventories.put(p2,inv2);

        for(Player p : inventories.keySet()){
            GameUser u = GameUser.getUser(p);
            Inventory inv = inventories.get(p);

            ItemStack pl = ItemUtil.namedItem(Material.STAINED_GLASS_PANE, " ", null, 7);
            ItemStack sep = ItemUtil.namedItem(Material.IRON_FENCE, " ", null);

            ItemStack skull1 = new ItemStack(Material.SKULL_ITEM);
            skull1.setDurability((short) 3);
            SkullMeta skull1M = (SkullMeta)skull1.getItemMeta();
            skull1M.setDisplayName(this.p.getName());
            skull1M.setOwner(this.p.getName());
            skull1.setItemMeta(skull1M);

            ItemStack skull2 = new ItemStack(Material.SKULL_ITEM);
            skull2.setDurability((short) 3);
            SkullMeta skull2M = (SkullMeta)skull2.getItemMeta();
            skull2M.setDisplayName(this.p2.getName());
            skull2M.setOwner(this.p2.getName());
            skull2.setItemMeta(skull2M);

            inv.setItem(0, pl);
            inv.setItem(1, skull1);
            if(player1ready){
                inv.setItem(2,ItemUtil.namedItem(Material.WOOL, ChatColor.RED + "CANCEL", null, 5));
            } else {
                inv.setItem(2, ItemUtil.namedItem(Material.WOOL, ChatColor.GREEN + "ACCEPT", null, 14));
            }
            inv.setItem(3, pl);
            inv.setItem(4, sep);
            inv.setItem(5, pl);
            if(player2ready){
                inv.setItem(6,ItemUtil.namedItem(Material.WOOL, ChatColor.RED + "CANCEL", null, 5));
            } else {
                inv.setItem(6, ItemUtil.namedItem(Material.WOOL, ChatColor.GREEN + "ACCEPT", null, 14));
            }
            inv.setItem(7, skull2);
            inv.setItem(8, pl);

            inv.setItem(13, sep);
            inv.setItem(22, sep);
            inv.setItem(31, sep);
            inv.setItem(40, sep);

            inv.setItem(45, pl);
            inv.setItem(46, pl);
            inv.setItem(47, pl);
            inv.setItem(48, pl);
            inv.setItem(49, sep);
            inv.setItem(50, pl);
            inv.setItem(51, pl);
            inv.setItem(52, pl);
            inv.setItem(53, pl);

            for(int i = 0; i < player1slots.length; i++){
                int slot = player1slots[i];

                if(i < offers1.size()){
                    inv.setItem(slot,CustomItem.fromItemStack(offers1.get(i)).build(p));
                } else {
                    inv.setItem(slot,new ItemStack(Material.AIR));
                }
            }

            for(int i = 0; i < player2slots.length; i++){
                int slot = player2slots[i];

                if(i < offers2.size()){
                    inv.setItem(slot,CustomItem.fromItemStack(offers2.get(i)).build(p));
                } else {
                    inv.setItem(slot,new ItemStack(Material.AIR));
                }
            }

            //p.openInventory(inv);
        }
    }

    private void initInventory(){
        inv1 = Bukkit.createInventory(null, Util.MAX_INVENTORY_SIZE,"Trading");
        inv2 = Bukkit.createInventory(null, Util.MAX_INVENTORY_SIZE,"Trading");

        updateInventories();
    }

    public Inventory getInventory1() {
        return inv1;
    }

    public Inventory getInventory2() {
        return inv2;
    }

    public boolean isInTrade(Player p){
        return isPlayer1(p) || isPlayer2(p);
    }

    public static ArrayList<Integer> getPlayer1SlotsAsList(){
        ArrayList<Integer> a = new ArrayList<Integer>();
        for(int i : player1slots) a.add(i);
        return a;
    }

    public static ArrayList<Integer> getPlayer2SlotsAsList(){
        ArrayList<Integer> a = new ArrayList<Integer>();
        for(int i : player2slots) a.add(i);
        return a;
    }

    public void openInventory(Player p){
        if(isInTrade(p)){
            if(isPlayer1(p)){
                if(getInventory1() != null) p.openInventory(getInventory1());
            } else if(isPlayer2(p)){
                if(getInventory2() != null) p.openInventory(getInventory2());
            }
        }
    }

    public void cancelTrade(){
        if(getStatus() != Status.CANCELLED){
            setStatus(Status.CANCELLED);

            getPlayer1().closeInventory();
            getPlayer2().closeInventory();

            for(ItemStack i : getOffers2()){
                CustomItem item = CustomItem.fromItemStack(i);
                getPlayer1().getInventory().addItem(item.build(getPlayer1()));
            }

            for(ItemStack i : getOffers1()){
                CustomItem item = CustomItem.fromItemStack(i);
                getPlayer2().getInventory().addItem(item.build(getPlayer2()));
            }

            getPlayer1().sendMessage(ChatColor.RED + "The trade has been cancelled.");
            getPlayer2().sendMessage(ChatColor.RED + "The trade has been cancelled.");

            unregister();
        }
    }

    public void unregister(){
        STORAGE.remove(this);
    }

    public enum Status {
        TRADING,CANCELLED
    }

    public static class Request {
        public String from;
        public String to;

        public Request(String from, String to){
            this.from = from;
            this.to = to;
        }

        public void accept(){
            Trade t = Trade.startTrade(Bukkit.getPlayer(from),Bukkit.getPlayer(to));

            t.openInventory(Bukkit.getPlayer(from));
            t.openInventory(Bukkit.getPlayer(to));

            Trade.removeRequest(this);
        }
    }
}
