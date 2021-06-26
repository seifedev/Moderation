package tech.seife.moderation.datamanager.mutes;

import tech.seife.moderation.Moderation;
import org.bukkit.Bukkit;

import java.time.LocalDateTime;
import java.util.UUID;

public class MutedPlayerManager {

    private final Moderation plugin;

    public MutedPlayerManager(Moderation plugin) {
        this.plugin = plugin;
    }

    public void addMutedPlayer(UUID mutedByUuid, UUID mutedPlayerUuid, String mutedByUsername, String mutedPlayerUsername, String channelName, LocalDateTime mutedDate, LocalDateTime releaseDate){
        MutedPlayer mutedPlayer = new MutedPlayer(generateId(), mutedByUuid, mutedPlayerUuid, mutedByUsername, mutedPlayerUsername, channelName, mutedDate, releaseDate);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> plugin.getDataHandler().getDataManager().saveMute(mutedPlayer));
    }

    private int generateId() {
        return plugin.getDataHandler().getDataManager().getLastMuteId() + 1;
    }
}
