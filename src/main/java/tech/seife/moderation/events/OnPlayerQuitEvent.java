package tech.seife.moderation.events;

import tech.seife.moderation.datamanager.dao.CachedData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class OnPlayerQuitEvent implements Listener {

    private final CachedData cachedData;

    public OnPlayerQuitEvent(CachedData cachedData) {
        this.cachedData = cachedData;
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent e) {
        cachedData.getAvailableStaff().remove(e.getPlayer().getUniqueId());
    }
}