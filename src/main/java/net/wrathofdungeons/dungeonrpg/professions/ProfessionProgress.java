package net.wrathofdungeons.dungeonrpg.professions;

import net.wrathofdungeons.dungeonapi.util.BarUtil;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.util.FormularUtils;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class ProfessionProgress {
    private Profession profession;
    private int level = 0;
    private double exp = 0;
    private boolean started = false;

    public ProfessionProgress(Profession profession){
        this.profession = profession;
    }

    public Profession getProfession() {
        return profession;
    }

    public int getLevel() {
        return level;
    }

    public double getExp() {
        return exp;
    }

    public void setExp(Player p, double exp) {
        if(exp > 0 && !mayGetEXP()) return;

        this.exp = exp;
        checkLevelUp(p);
    }

    public void giveExp(Player p, double exp){
        if(!mayGetEXP()) return;

        this.exp += exp;
        checkLevelUp(p);
        updateBar(p);
    }

    public void levelUp(Player p,int times){
        for (int i = 0; i < times; i++) {
            if(mayGetEXP()){
                setLevel(p,getLevel()+times);
                p.sendMessage(ChatColor.GREEN + "Your " + ChatColor.YELLOW + getProfession().getName() + ChatColor.GREEN + " profession leveled up to Level " + ChatColor.YELLOW + getLevel() + ChatColor.GREEN + "!");
                p.playSound(p.getEyeLocation(), Sound.LEVEL_UP,1f,1f);
            }
        }

        if(getLevel() >= getProfession().getMaxLevel()) setExp(p,0);
    }

    public boolean mayGetEXP(){
        return this.level < getProfession().getMaxLevel();
    }

    public void updateBar(Player p){
        if(isOwner(p)){
            GameUser u = GameUser.getUser(p);
            u.barTimer = 5;
            double neededExp = FormularUtils.getNeededProfessionEXP(getProfession(),getLevel()+1);
            int xp = ((Double)((getExp()/neededExp*100))).intValue();
            //String text = ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + getProfession().getName() + ": " + ChatColor.YELLOW + xp + "%";
            float percent = (float)((double)(getExp()/neededExp));

            p.setExp(percent);
            p.setLevel(getLevel());
        }
    }

    private boolean isOwner(Player p){
        return GameUser.isLoaded(p) && GameUser.getUser(p).getCurrentCharacter() != null && GameUser.getUser(p).getCurrentCharacter().getVariables().getProfessionProgress(getProfession()) == this;
    }

    public void setLevel(Player p, int level) {
        this.level = level;

        checkLevelUp(p);

        if(this.level < 1) this.level = 1;
        if(this.level > getProfession().getMaxLevel()) this.level = getProfession().getMaxLevel();
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    private void checkLevelUp(Player p){
        int levels = 0;
        double required = 0;

        while((getExp() >= (required = FormularUtils.getNeededProfessionEXP(getProfession(),getLevel()+1 + levels))) && (getLevel() + levels < getProfession().getMaxLevel())){
            setExp(p,getExp()-required);
            levels++;
        }

        if(levels > 0) levelUp(p,levels);
    }
}
