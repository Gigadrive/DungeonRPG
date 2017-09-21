package net.wrathofdungeons.dungeonrpg.skill.magician;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.Particle;
import net.wrathofdungeons.dungeonapi.util.ParticleEffect;
import net.wrathofdungeons.dungeonrpg.skill.Skill;
import net.wrathofdungeons.dungeonrpg.skill.SkillType;
import net.wrathofdungeons.dungeonrpg.user.RPGClass;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

public class Blinkpool implements Skill {
    @Override
    public String getName() {
        return "Blinkpool";
    }

    @Override
    public RPGClass getRPGClass() {
        return RPGClass.MAGICIAN;
    }

    @Override
    public SkillType getType() {
        return SkillType.ESCAPING_MOVE;
    }

    @Override
    public String getCombo() {
        return "RRR";
    }

    @Override
    public void execute(Player p) {
        Location loc = p.getEyeLocation();
        try {
            BlockIterator bi = new BlockIterator(loc, 0.0, 15);

            Location lastLoc = null;
            while(bi.hasNext()){
                Block b = bi.next();

                if(b.getType() == Material.AIR){
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

            p.teleport(lastLoc);
        } catch(IllegalStateException ex){
            p.sendMessage(ChatColor.RED + "Teleport failed! Are you standing too close to something?");
        }
    }
}
