package net.wrathofdungeons.dungeonrpg.inv;

import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.entity.Player;

public class GuildMenu {
    public static void openFor(Player p){
        GameUser u = GameUser.getUser(p);
    }
}
