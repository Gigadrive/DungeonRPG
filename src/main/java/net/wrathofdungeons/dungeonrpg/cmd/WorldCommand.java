package net.wrathofdungeons.dungeonrpg.cmd;

import de.dytanic.cloudnet.api.CloudAPI;
import de.dytanic.cloudnet.lib.server.info.ServerInfo;
import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class WorldCommand extends Command {
    public WorldCommand(){
        super("world", Rank.ADMIN);
    }

    @Override
    public void execute(Player p, String label, String[] args) {
        GameUser u = GameUser.getUser(p);

        if(p.getWorld() != null){
            World w = p.getWorld();

            p.sendMessage(ChatColor.YELLOW + "Name: " + w.getName());
            p.sendMessage(ChatColor.YELLOW + "Loaded Chunks: " + w.getLoadedChunks().length);
            p.sendMessage(ChatColor.YELLOW + "Entities: " + w.getEntities().size());
            p.sendMessage(ChatColor.YELLOW + "Auto Save: " + w.isAutoSave());
            p.sendMessage(ChatColor.YELLOW + "Max Height: " + w.getMaxHeight());
            p.sendMessage(ChatColor.YELLOW + "Type: " + w.getWorldType().toString());

            for(ServerInfo info : CloudAPI.getInstance().getServers()){
                if(info.getServiceId().getServerId().equals(CloudAPI.getInstance().getServerId())){
                    p.sendMessage(ChatColor.YELLOW + "Service ID: " + info.getServiceId().getServerId());
                    p.sendMessage(ChatColor.YELLOW + "Server ID: " + info.getServiceId().getId());
                    break;
                }
            }
        }
    }
}
