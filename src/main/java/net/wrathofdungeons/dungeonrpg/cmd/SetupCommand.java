package net.wrathofdungeons.dungeonrpg.cmd;

import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.entity.Player;

public class SetupCommand extends Command {
    public SetupCommand(){
        super("setup", Rank.ADMIN);
    }

    @Override
    public void execute(Player p, String label, String[] args) {
        GameUser u = GameUser.getUser(p);

        u.setSetupMode(!u.isInSetupMode());
    }
}
