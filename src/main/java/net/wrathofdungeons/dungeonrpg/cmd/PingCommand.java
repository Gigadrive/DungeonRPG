package net.wrathofdungeons.dungeonrpg.cmd;

import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PingCommand extends Command {
    public PingCommand(){
        super("ping");
    }

    @Override
    public void execute(Player p, String label, String[] args) {
        p.sendMessage(ChatColor.GREEN + "Ping: " + ((CraftPlayer)p).getHandle().ping + "ms");
    }
}
