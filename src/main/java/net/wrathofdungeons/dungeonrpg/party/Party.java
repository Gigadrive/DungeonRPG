package net.wrathofdungeons.dungeonrpg.party;

import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.dungeon.Dungeon;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Party {
    public static ArrayList<Party> STORAGE = new ArrayList<Party>();

    public static Party getParty(Player p){
        for(Party party : STORAGE){
            if(party.hasMember(p)) return party;
        }

        return null;
    }

    private ArrayList<PartyMember> members;
    private ArrayList<Player> invited;

    public boolean loadingDungeon = false;

    public Party(Player p){
        this.members = new ArrayList<PartyMember>();
        this.members.add(new PartyMember(p,PartyRank.LEADER));

        this.invited = new ArrayList<Player>();

        STORAGE.add(this);
    }

    public Player getLeader(){
        for(PartyMember member : members){
            if(member.rank == PartyRank.LEADER) return member.p;
        }

        return null;
    }

    public ArrayList<PartyMember> getMembers(){
        return members;
    }

    public ArrayList<Player> getOnlinePlayers(){
        ArrayList<Player> a = new ArrayList<Player>();

        for(PartyMember m : getMembers()){
            if(m.p.isOnline()) a.add(m.p);
        }

        return a;
    }

    public ArrayList<Player> getInvited() {
        return invited;
    }

    public boolean isInvited(Player p){
        return invited.contains(p);
    }

    public PartyRank getRank(Player p){
        for(PartyMember member : members){
            if(member.p.getUniqueId().toString().equals(p.getUniqueId().toString())) return member.rank;
        }

        return null;
    }

    public boolean hasMember(Player p){
        return getRank(p) != null;
    }

    public boolean isLeader(Player p){
        return getRank(p) != null && getRank(p).equals(PartyRank.LEADER);
    }

    public void joinParty(Player p){
        GameUser u = GameUser.getUser(p);

        if(u.getParty() == null){
            getMembers().add(new PartyMember(p,PartyRank.MEMBER));

            for(Player all : getOnlinePlayers()){
                all.sendMessage(ChatColor.GREEN + p.getName() + " has joined the party.");
            }
        }

        DungeonRPG.updateNames();
    }

    public PartyMember toPartyMember(Player p){
        PartyMember m = null;

        for(PartyMember member : getMembers()){
            if(member.p.getUniqueId().toString().equals(p.getUniqueId().toString())) m = member;
        }

        return m;
    }

    public void leaveParty(Player p){
        GameUser u = GameUser.getUser(p);
        p.closeInventory();

        if(u.getParty() == this){
            if(u.isInDungeon())
                p.teleport(getDungeon().getType().getPortalEntranceLocation());

            if(getRank(p) == PartyRank.LEADER){
                disband();
            } else {
                for(Player all : getOnlinePlayers()){
                    all.sendMessage(ChatColor.RED + p.getName() + " has left the party.");
                }

                getMembers().remove(toPartyMember(p));
            }
        }

        DungeonRPG.updateNames();
    }

    public Dungeon getDungeon(){
        for(Dungeon dungeon : Dungeon.STORAGE)
            if(dungeon.getParty() == this)
                return dungeon;

        return null;
    }

    public boolean isInDungeon(){
        return getDungeon() != null;
    }

    public void disband(){
        for(Player all : getOnlinePlayers()){
            all.closeInventory();
            all.sendMessage(ChatColor.DARK_RED + "The party has been disbanded.");
        }

        getMembers().clear();

        if(isInDungeon()) getDungeon().unregister();

        STORAGE.remove(this);
        DungeonRPG.updateNames();
    }
}
