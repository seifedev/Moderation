package tech.seife.moderation.events;

import tech.seife.moderation.datamanager.dao.CachedData;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import java.util.Objects;
import java.util.UUID;

public class OnSignChangeEvent implements Listener {

    private final CachedData cachedData;

    public OnSignChangeEvent(CachedData cachedData) {
        this.cachedData = cachedData;
    }


    @EventHandler
    public void onSignChangeEvent(SignChangeEvent e) {
        if (cachedData.getAvailableStaff().isEmpty()) return;
        for (UUID uuid : cachedData.getAvailableStaff()) {
            Objects.requireNonNull(Bukkit.getPlayer(uuid)).sendMessage("Player: " + e.getPlayer().getName() + " placed a sign at: \n" +
                    "World: " + e.getBlock().getLocation().getWorld().getName() + " X: " + e.getBlock().getLocation().getBlockX() + " Y:" + e.getBlock().getLocation().getY() + " Z:" + e.getBlock().getLocation().getZ() + "\n" +
                    "first line: " + e.getLine(0) + "\n" +
                    "second line: " + e.getLine(1) + "\n" +
                    "third line: " + e.getLine(2) + "\n" +
                    "fourth line: " + e.getLine(3));
        }
    }
}
