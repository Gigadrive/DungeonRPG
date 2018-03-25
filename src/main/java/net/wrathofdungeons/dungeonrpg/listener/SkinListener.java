package net.wrathofdungeons.dungeonrpg.listener;

import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.inventivetalent.nicknamer.api.event.NickNamerSelfUpdateEvent;

public class SkinListener implements Listener {
    @EventHandler
    public void onUpdate(NickNamerSelfUpdateEvent e) {
        if (GameUser.isLoaded(e.getPlayer()))
            GameUser.getUser(e.getPlayer()).forceReloadWorld();
    }
}
