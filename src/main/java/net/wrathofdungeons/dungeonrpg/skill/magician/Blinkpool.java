package net.wrathofdungeons.dungeonrpg.skill.magician;

import net.wrathofdungeons.dungeonapi.util.ParticleEffect;
import net.wrathofdungeons.dungeonrpg.skill.Skill;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.HashMap;

public class Blinkpool implements Skill {
    @Override
    public String getName() {
        return "Blinkpool";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public HashMap<String, String> getEffects(int investedSkillPoints) {
        HashMap<String,String> effects = new HashMap<String, String>();

        int range = 15;

        if(investedSkillPoints == 2){
            range = 20;
        } else if(investedSkillPoints == 3){
            range = 25;
        }

        effects.put("Range",String.valueOf(range));

        return effects;
    }

    @Override
    public int getIcon() {
        return 368;
    }

    @Override
    public int getIconDurability() {
        return 0;
    }

    @Override
    public RPGClass getRPGClass() {
        return RPGClass.MAGICIAN;
    }

    @Override
    public int getMinLevel() {
        return 15;
    }

    @Override
    public int getMaxInvestingPoints() {
        return 3;
    }

    @Override
    public int getBaseMPCost() {
        return 2;
    }

    @Override
    public void execute(Player p) {
        GameUser u = GameUser.getUser(p);
        int investedSkillPoints = u.getCurrentCharacter().getVariables().getInvestedSkillPoints(this);

        final int range = Integer.parseInt(getEffects(investedSkillPoints).get("Range"));

        Location loc = p.getEyeLocation();
        try {
            BlockIterator bi = new BlockIterator(loc, 0.0, range);

            Location lastLoc = null;
            while(bi.hasNext()){
                Block b = bi.next();

                if(b.getType() == Material.AIR || !b.getType().isSolid()){
                    lastLoc = b.getLocation();
                } else {
                    break;
                }
            }

            if(lastLoc == null) lastLoc = loc;

            int c = (int)Math.ceil(loc.distance(lastLoc) / 2F) - 1;
            if(c > 0){
                Vector v = lastLoc.toVector().subtract(loc.toVector()).normalize().multiply(2F);
                Location l = loc.clone();
                for (int i = 0; i < c; i++) {
                    l.add(v);
                    ParticleEffect.FLAME.display(0f,0f,0f,0f,1,l,600);
                }
            }

            double y = lastLoc.getY();

            while((lastLoc.getBlock() != null && lastLoc.getBlock().getType() != null && lastLoc.getBlock().getType() != Material.AIR)){
                y++;
                lastLoc.setY(y);
            }

            lastLoc.setY(y);
            lastLoc.setYaw(loc.getYaw());
            lastLoc.setPitch(loc.getPitch());
            p.getWorld().playEffect(p.getLocation(), Effect.ENDER_SIGNAL, 1);

            p.teleport(lastLoc);
        } catch(IllegalStateException ex){
            p.sendMessage(ChatColor.RED + "Teleport failed! Are you standing too close to something?");
        }
    }
}
