package net.wrathofdungeons.dungeonrpg.guilds;

import de.dytanic.cloudnet.api.CloudNetAPI;
import de.dytanic.cloudnet.player.PlayerWhereAmI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.UUID;

public class GuildMember {
    private UUID uuid;
    private GuildRank guildRank;
    private Timestamp timeJoined;

    public UUID getUUID() {
        return uuid;
    }

    public GuildRank getRank() {
        return guildRank;
    }

    public void setGuildRank(GuildRank guildRank) {
        this.guildRank = guildRank;
    }

    public void setTimeJoined(Timestamp timeJoined) {
        this.timeJoined = timeJoined;
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    public Timestamp getTimeJoined() {
        return timeJoined;
    }

    public boolean isOnlineLocal(){
        return toBukkitPlayer() != null;
    }

    public boolean isOnlineGlobal(){
        return toCloudPlayer() != null && getServer() != null;
    }

    public String getServer(){
        return toCloudPlayer() != null ? toCloudPlayer().getServer() : null;
    }

    public PlayerWhereAmI toCloudPlayer(){
        return CloudNetAPI.getInstance().getOnlinePlayer(uuid);
    }

    public Player toBukkitPlayer(){
        return Bukkit.getPlayer(uuid);
    }
}
