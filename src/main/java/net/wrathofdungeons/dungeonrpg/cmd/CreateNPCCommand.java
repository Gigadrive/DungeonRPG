package net.wrathofdungeons.dungeonrpg.cmd;

import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.MySQLManager;
import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.npc.CustomNPC;
import net.wrathofdungeons.dungeonrpg.npc.CustomNPCType;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class CreateNPCCommand extends Command {
    public CreateNPCCommand(){
        super("createnpc", Rank.ADMIN);
    }

    @Override
    public void execute(Player p, String label, String[] args) {
        GameUser u = GameUser.getUser(p);

        if(!DungeonRPG.isTestServer()){
            p.sendMessage(ChatColor.RED + "That command is not available on this server.");
            return;
        }

        if(u.isInSetupMode()){
            if(args.length == 1){
                CustomNPCType type = CustomNPCType.fromName(args[0]);

                if(type != null){
                    DungeonAPI.async(() -> {
                        try {
                            int npcID = 0;

                            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `npcs` (`npcType`,`location.world`,`location.x`,`location.y`,`location.z`,`location.yaw`,`location.pitch`) VALUES(?,?,?,?,?,?,?);", Statement.RETURN_GENERATED_KEYS);
                            ps.setString(1,type.toString());
                            ps.setString(2,p.getLocation().getWorld().getName());
                            ps.setDouble(3,p.getLocation().getX());
                            ps.setDouble(4,p.getLocation().getY());
                            ps.setDouble(5,p.getLocation().getZ());
                            ps.setFloat(6,p.getLocation().getYaw());
                            ps.setFloat(7,p.getLocation().getPitch());
                            ps.executeUpdate();

                            ResultSet rs = ps.getGeneratedKeys();
                            if(rs.first()){
                                npcID = rs.getInt(1);
                            }

                            if(npcID > 0){
                                new CustomNPC(npcID);
                                p.sendMessage(ChatColor.GREEN + "Created NPC! ID: #" + npcID);
                            } else {
                                p.sendMessage(ChatColor.RED + "An error occurred.");
                            }

                            MySQLManager.getInstance().closeResources(rs,ps);
                        } catch(Exception e){
                            p.sendMessage(ChatColor.RED + "An error occurred.");
                            e.printStackTrace();
                        }
                    });
                } else {
                    p.sendMessage(ChatColor.RED + "Invalid NPC type!");
                }
            } else {
                p.sendMessage(ChatColor.RED + "/" + label + " <NPC-Type>");
            }
        } else {
            p.sendMessage(ChatColor.RED + "You are not in setup mode.");
        }
    }
}
