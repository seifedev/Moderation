package tech.seife.moderation.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import tech.seife.chatutilities.channels.ChannelManager;
import tech.seife.moderation.Moderation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OnAsyncPlayerChatEvent implements Listener {

    private final Moderation plugin;
    private final ChannelManager channelManager;

    public OnAsyncPlayerChatEvent(Moderation plugin, ChannelManager channelManager) {
        this.plugin = plugin;
        this.channelManager = channelManager;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent e) {
        if (channelManager != null && channelManager.getChannels() != null) {
            plugin.getSpiedTextManager().saveSpiedText(e.getPlayer().getUniqueId(), e.getPlayer().getName(), e.getMessage());
            channelManager.getChannels()
                    .forEach(channel -> {
                        if (channel.getPlayersInChannel().contains(e.getPlayer().getUniqueId()) && plugin.getDataHandler().getDataManager().isPlayerMutedByUuid(e.getPlayer().getUniqueId(), channel.getName())) {
                            if (canUnmute(e.getPlayer().getName(), channel.getName())) {
                                plugin.getDataHandler().getDataManager().removeMute(e.getPlayer().getName(), channel.getName());
                            } else {
                                e.setCancelled(true);
                            }
                        }
                    });
        }
    }

    private boolean canUnmute(String playerUsername, String channelName) {
        if (plugin.getDataHandler().getDataManager() != null && plugin.getDataHandler().getDataManager().loadMutedPlayer(playerUsername, channelName) != null && plugin.getDataHandler().getDataManager().loadMutedPlayer(playerUsername, channelName).getReleaseDate() != null) {
            LocalDateTime dateTime = LocalDateTime.parse(LocalDateTime.now().toString());
            System.out.println("dateTime: " + dateTime);
            System.out.println("plugin.getDataHandler().getDataManager().loadMutedPlayer(playerUsername, channelName).getReleaseDate(): " + plugin.getDataHandler().getDataManager().loadMutedPlayer(playerUsername, channelName).getReleaseDate());
            return dateTime.isAfter(plugin.getDataHandler().getDataManager().loadMutedPlayer(playerUsername, channelName).getReleaseDate());
        } else {
            System.out.println("6");
            return true;
        }
    }
}
