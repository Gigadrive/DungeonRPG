package net.wrathofdungeons.dungeonrpg.cmd;

import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.MySQLManager;
import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.mobs.MobData;
import net.wrathofdungeons.dungeonrpg.regions.Region;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class CreateRegionCommand extends Command {
    public CreateRegionCommand(){
        super("createregion", Rank.ADMIN);
    }

    @Override
    public void execute(Player p, String label, String[] args) {
        GameUser u = GameUser.getUser(p);

        if(!DungeonRPG.isTestServer()){
            p.sendMessage(ChatColor.RED + "That command is not available on this server.");
            return;
        }

        if(u.isInSetupMode()){
            if(args.length == 2){
                if(Util.isValidInteger(args[0]) && Util.isValidInteger(args[1])){
                    int mobID = Integer.parseInt(args[0]);
                    int mobLimit = Integer.parseInt(args[1]);
                    MobData data = MobData.getData(mobID);

                    if(data != null){
                        if(mobLimit > 0){
                            DungeonAPI.async(() -> {
                                try {
                                    int regionID = 0;

                                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `regions` (`mobDataID`,`mobLimit`,`addedBy`) VALUES(?,?,?);", Statement.RETURN_GENERATED_KEYS);
                                    ps.setInt(1,mobID);
                                    ps.setInt(2,mobLimit);
                                    ps.setString(3,p.getUniqueId().toString());
                                    ps.executeUpdate();

                                    ResultSet rs = ps.getGeneratedKeys();
                                    if(rs.first()){
                                        regionID = rs.getInt(1);
                                    }

                                    MySQLManager.getInstance().closeResources(rs,ps);

                                    if(regionID > 0){
                                        new Region(regionID);
                                        p.sendMessage(ChatColor.GREEN + "Region with Mob #" + mobID + " (" + data.getName() + ") added! ID: " + regionID);
                                    } else {
                                        p.sendMessage(ChatColor.RED + "An error occurred.");
                                    }
                                } catch(Exception e){
                                    e.printStackTrace();
                                    p.sendMessage(ChatColor.RED + "An error occurred.");
                                }
                            });
                        } else {
                            p.sendMessage(ChatColor.RED + "Unknown Mob Data.");
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "Unknown Mob Data.");
                    }
                } else {
                    p.sendMessage(ChatColor.RED + "/" + label + " [<Mob-ID> <Mob Limit>]");
                }
            } else if(args.length == 0){
                DungeonAPI.async(() -> {
                    try {
                        int regionID = 0;

                        PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `regions` (`addedBy`) VALUES(?);", Statement.RETURN_GENERATED_KEYS);
                        ps.setString(1,p.getUniqueId().toString());
                        ps.executeUpdate();

                        ResultSet rs = ps.getGeneratedKeys();
                        if(rs.first()){
                            regionID = rs.getInt(1);
                        }

                        MySQLManager.getInstance().closeResources(rs,ps);

                        if(regionID > 0){
                            new Region(regionID);
                            p.sendMessage(ChatColor.GREEN + "Region with NO mobdata added! ID: " + regionID);
                        } else {
                            p.sendMessage(ChatColor.RED + "An error occurred.");
                        }
                    } catch(Exception e){
                        e.printStackTrace();
                        p.sendMessage(ChatColor.RED + "An error occurred.");
                    }
                });
            } else {
                p.sendMessage(ChatColor.RED + "/" + label + " [<Mob-ID> <Mob Limit>]");
            }
        } else {
            p.sendMessage(ChatColor.RED + "You are not in setup mode.");
        }
    }
}
