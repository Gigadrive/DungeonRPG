package net.wrathofdungeons.dungeonrpg.listener;

import com.google.common.collect.Sets;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;
import java.util.Set;

public class AnimationListener implements Listener {
    @EventHandler
    public void onAnimate(PlayerAnimationEvent e){
        Player p = e.getPlayer();

        if(GameUser.isLoaded(p)){
            GameUser u = GameUser.getUser(p);

            if(u.getCurrentCharacter() != null){
                if(p.getGameMode() == GameMode.ADVENTURE){
                    if(e.getAnimationType() == PlayerAnimationType.ARM_SWING){
                        Set<Material> s = Sets.newConcurrentHashSet();
                        s.add(Material.AIR);
                        s.add(Material.WATER);
                        s.add(Material.STATIONARY_WATER);
                        s.add(Material.LAVA);
                        s.add(Material.STATIONARY_LAVA);

                        Block target = p.getTargetBlock(s,4);

                        if(target != null && !s.contains(target.getType())){
                            PlayerInteractEvent event = new PlayerInteractEvent(p, Action.LEFT_CLICK_BLOCK, p.getItemInHand(), target, BlockFace.UP);
                            Bukkit.getPluginManager().callEvent(event);
                        }
                    }
                }
            }
        }
    }
}
