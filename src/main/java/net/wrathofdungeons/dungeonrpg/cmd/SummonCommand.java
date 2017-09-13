package net.wrathofdungeons.dungeonrpg.cmd;

import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.mobs.CustomEntity;
import net.wrathofdungeons.dungeonrpg.mobs.MobData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SummonCommand extends Command {
    public SummonCommand(){
        super(new String[]{"summon","spawnmob"}, Rank.GM);
    }

    @Override
    public void execute(Player p, String label, String[] args) {
        if(args.length == 1){
            if(Util.isValidInteger(args[0])){
                int mobID = Integer.parseInt(args[0]);
                MobData data = MobData.getData(mobID);

                if(data != null){
                    CustomEntity e = new CustomEntity(data);
                    e.setOriginRegion(null);
                    e.spawn(p.getLocation());

                    p.sendMessage(ChatColor.GREEN + "Success!");
                } else {
                    p.sendMessage(ChatColor.RED + "Unknown Mob ID.");
                }
            } else {
                p.sendMessage(ChatColor.RED + "/" + label + " <Mob-ID> [Amount>");
            }
        } else if(args.length == 2){
            if(Util.isValidInteger(args[0]) && Util.isValidInteger(args[1])){
                int mobID = Integer.parseInt(args[0]);
                int amount = Integer.parseInt(args[1]);
                MobData data = MobData.getData(mobID);

                if(data != null){
                    for(int i = 0; i < amount; i++){
                        CustomEntity e = new CustomEntity(data);
                        e.setOriginRegion(null);
                        e.spawn(p.getLocation());

                        p.sendMessage(ChatColor.GREEN + "Success!");
                    }
                } else {
                    p.sendMessage(ChatColor.RED + "Unknown Mob ID.");
                }
            } else {
                p.sendMessage(ChatColor.RED + "/" + label + " <Mob-ID> [Amount>");
            }
        } else {
            p.sendMessage(ChatColor.RED + "/" + label + " <Mob-ID> [Amount>");
        }
    }
}
