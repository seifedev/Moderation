package tech.seife.moderation.events;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import tech.seife.chatutilities.channels.Channel;
import tech.seife.moderation.Moderation;

import java.time.LocalDateTime;
import java.util.UUID;

public class OnPlayerCommandPreprocessEvent implements Listener {

    private final Moderation plugin;

    public OnPlayerCommandPreprocessEvent(Moderation plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent e) {

        if (shouldCancelEvent(e.getMessage(), e.getPlayer().getUniqueId(), e.getPlayer().getName())) {
            e.setCancelled(true);
        }

        for (UUID playerUuid : plugin.getCachedData().getSpyMembers()) {
            Bukkit.getPlayer(playerUuid).sendMessage("Player: " + e.getPlayer().getName() + " tried to send: " + e.getMessage());
        }
        plugin.getSpiedTextManager().saveSpiedText(e.getPlayer().getUniqueId(), e.getPlayer().getName(), e.getMessage());
    }

    private boolean shouldCancelEvent(String message, UUID playerUuid, String playerUsername) {
        if (message.split(" ")[0] != null) {
            String shortCut = message.split(" ")[0];
            return isPlayerMuted(playerUuid, playerUsername, shortCut);
        }
        return false;
    }

    private boolean isPlayerMuted(UUID playerUuid, String playerUsername, String shortCut) {
        if (playerUuid != null && playerUsername != null && shortCut != null) {
            for (Channel channel : plugin.getChatUtilities().getChannelManager().getChannels()) {
                if (channel.getShortCut().equalsIgnoreCase(shortCut)) {
                    if (plugin.getDataHandler().getDataManager().isPlayerMutedByUuid(playerUuid, channel.getName())) {
                        if (channel.getPlayersInChannel().contains(playerUuid) && plugin.getDataHandler().getDataManager().isPlayerMutedByUuid(playerUuid, channel.getName())) {
                            if (canUnmute(playerUsername, channel.getName())) {
                                plugin.getDataHandler().getDataManager().removeMute(playerUsername, channel.getName());
                                return false;
                            } else {
                                Bukkit.getPlayer(playerUuid).sendMessage("You're muted in this channel!");
                                return true;
                            }
                        } else {
                            Bukkit.getPlayer(playerUuid).sendMessage("You're muted in this channel!");
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }


    private boolean canUnmute(String playerUsername, String channelName) {
        if (plugin.getDataHandler().getDataManager().loadMutedPlayer(playerUsername, channelName) != null && plugin.getDataHandler().getDataManager().loadMutedPlayer(playerUsername, channelName).getReleaseDate() != null) {
            return LocalDateTime.now().isAfter(plugin.getDataHandler().getDataManager().loadMutedPlayer(playerUsername, channelName).getReleaseDate());
        }
        return false;
    }
}
