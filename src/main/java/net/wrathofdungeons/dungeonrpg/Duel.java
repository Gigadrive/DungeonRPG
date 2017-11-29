package net.wrathofdungeons.dungeonrpg;

import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class Duel {
    private static ArrayList<Duel> STORAGE = new ArrayList<Duel>();
    private static ArrayList<Request> REQUESTS = new ArrayList<Request>();

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

        if(isDueling(p)){
            Duel d = Duel.getDuel(p);

            if(d.isPlayer1(p)){
                d.endGame(d.getPlayer2());
            } else {
                d.endGame(d.getPlayer1());
            }
        }
    }

    public static void removeRequest(Request request){
        REQUESTS.remove(request);
    }

    public static Duel getDuel(Player p){
        for(Duel d : STORAGE){
            if(d.isInDuel(p)) return d;
        }

        return null;
    }

    public static Duel startDuel(Player p, Player p2){
        Duel d = new Duel(p,p2);

        STORAGE.add(d);

        return d;
    }

    public static boolean isDueling(Player p){
        return getDuel(p) != null;
    }

    public static boolean isDuelingWith(Player p, Player p2){
        return getDuel(p) != null && getDuel(p2) != null && getDuel(p) == getDuel(p2);
    }

    private Player p;
    private Player p2;

    public Duel(Player p, Player p2){
        this.p = p;
        this.p2 = p2;
    }

    public Player getPlayer1() {
        return p;
    }

    public Player getPlayer2() {
        return p2;
    }

    public boolean isInDuel(Player p){
        return isPlayer1(p) || isPlayer2(p);
    }

    public boolean isPlayer1(Player p){
        return this.p.getName().equals(p.getName());
    }

    public boolean isPlayer2(Player p){
        return this.p2.getName().equals(p.getName());
    }

    public void endGame(Player winner){
        if(isInDuel(winner)){
            GameUser.getUser(winner).getCurrentCharacter().getVariables().statisticsManager.duelsWon++;

            getPlayer1().sendMessage(ChatColor.YELLOW + winner.getName() + ChatColor.AQUA + " has won the duel!");
            getPlayer2().sendMessage(ChatColor.YELLOW + winner.getName() + ChatColor.AQUA + " has won the duel!");

            unregister();
        }
    }

    public void unregister(){
        STORAGE.remove(this);
    }

    public static class Request {
        public String from;
        public String to;

        public Request(String from, String to){
            this.from = from;
            this.to = to;
        }

        public void accept(){
            Bukkit.getPlayer(from).sendMessage(ChatColor.AQUA + "The duel starts in " + ChatColor.YELLOW + "5 seconds" + ChatColor.AQUA + ".");
            Bukkit.getPlayer(to).sendMessage(ChatColor.AQUA + "The duel starts in " + ChatColor.YELLOW + "5 seconds" + ChatColor.AQUA + ".");

            final Request r = this;

            GameUser.getUser(Bukkit.getPlayer(from)).getCurrentCharacter().getVariables().statisticsManager.duelsPlayed++;
            GameUser.getUser(Bukkit.getPlayer(to)).getCurrentCharacter().getVariables().statisticsManager.duelsPlayed++;

            new BukkitRunnable(){
                @Override
                public void run() {
                    Duel d = Duel.startDuel(Bukkit.getPlayer(from),Bukkit.getPlayer(to));

                    Duel.removeRequest(r);

                    if(!DungeonRPG.isInGame(from)){
                        d.endGame(Bukkit.getPlayer(to));
                        return;
                    } else if(!DungeonRPG.isInGame(to)){
                        d.endGame(Bukkit.getPlayer(from));
                        return;
                    }

                    d.getPlayer1().sendMessage(ChatColor.AQUA + "The duel starts " + ChatColor.YELLOW + "NOW" + ChatColor.AQUA + "!");
                    d.getPlayer2().sendMessage(ChatColor.AQUA + "The duel starts " + ChatColor.YELLOW + "NOW" + ChatColor.AQUA + "!");
                }
            }.runTaskLater(DungeonRPG.getInstance(),5*20);
        }
    }
}
