package tech.seife.moderation.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import tech.seife.chatutilities.channels.ChannelManager;
import tech.seife.moderation.Moderation;
import tech.seife.moderation.datamanager.dao.DataManager;
import tech.seife.moderation.datamanager.spiedtext.SpiedText;

import java.time.LocalDateTime;

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
                        if (channel.getPlayersInChannel().contains(e.getPlayer().getUniqueId()) && plugin.getDataManager().isPlayerMutedByUuid(e.getPlayer().getUniqueId(), channel.getName())) {
                            if (canUnmute(e.getPlayer().getName(), channel.getName())) {
                                plugin.getDataManager().removeMute(e.getPlayer().getName(), channel.getName());
                            } else {
                                e.getPlayer().sendMessage("You're muted in this channel");
                                e.setCancelled(true);
                            }
                        }
                    });
        }
    }

    private boolean canUnmute(String playerUsername, String channelName) {
        if (plugin.getDataManager() != null && plugin.getDataManager().loadMutedPlayer(playerUsername, channelName) != null && plugin.getDataManager().loadMutedPlayer(playerUsername, channelName).getReleaseDate() != null) {
            return LocalDateTime.now().isAfter(plugin.getDataManager().loadMutedPlayer(playerUsername, channelName).getReleaseDate());
        } else {
            return true;
        }
    }
}
