package net.wrathofdungeons.dungeonrpg.cmd;

import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.MySQLManager;
import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.npc.CustomNPC;
import net.wrathofdungeons.dungeonrpg.npc.CustomNPCType;
import net.wrathofdungeons.dungeonrpg.quests.Quest;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class CreateQuestCommand extends Command {
    public CreateQuestCommand(){
        super("createquest", Rank.ADMIN);
    }

    @Override
    public void execute(Player p, String label, String[] args) {
        GameUser u = GameUser.getUser(p);

        if(u.isInSetupMode()){
            if(args.length > 1){
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    sb.append(" ").append(args[i]);
                }
                String name = sb.toString().substring(1);

                if(Util.isValidInteger(args[0])){
                    CustomNPC npc = CustomNPC.fromID(Integer.parseInt(args[0]));

                    if(npc.getNpcType() == CustomNPCType.QUEST_NPC){
                        if(Quest.isNameAvailable(name)){
                            DungeonAPI.async(() -> {
                                try {
                                    int questID = 0;

                                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `quests` (`name`,`stages`,`giverNpc`) VALUES(?,?,?);", Statement.RETURN_GENERATED_KEYS);
                                    ps.setString(1,name);
                                    ps.setString(2,"[]");
                                    ps.setInt(3,npc.getId());
                                    ps.executeUpdate();

                                    ResultSet rs = ps.getGeneratedKeys();
                                    if(rs.first()){
                                        questID = rs.getInt(1);
                                    }

                                    if(questID > 0){
                                        new Quest(questID);
                                        p.sendMessage(ChatColor.GREEN + "Created Quest! ID: #" + questID);
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
                            p.sendMessage(ChatColor.RED + "That name is not available.");
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "The target NPC has to be a quest npc.");
                    }
                } else {
                    p.sendMessage(ChatColor.RED + "That NPC could not be found.");
                }
            } else {
                p.sendMessage(ChatColor.RED + "/" + label + " <Giver ID> <Name>");
            }
        } else {
            p.sendMessage(ChatColor.RED + "You are not in setup mode.");
        }
    }
}
