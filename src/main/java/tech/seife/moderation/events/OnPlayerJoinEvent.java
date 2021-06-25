package tech.seife.moderation.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import tech.seife.moderation.Moderation;
import tech.seife.moderation.datamanager.banned.BannedPlayer;

import java.time.LocalDateTime;

public class OnPlayerJoinEvent implements Listener {

    private final Moderation plugin;

    public OnPlayerJoinEvent(Moderation plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public boolean onPlayerJoinEvent(PlayerJoinEvent e) {
        giveRulesBooks(e);

        removeBanIfPossible(e);
        return true;
    }

    private void removeBanIfPossible(PlayerJoinEvent e) {
        if (plugin.getDataManager().isPlayerBannedUuidCheck(e.getPlayer().getUniqueId())) {
            if (!canReleaseBan(e.getPlayer().getName())) {
                BannedPlayer ban = plugin.getDataManager().retrieveCurrentBannedPlayerInformation(e.getPlayer().getName());
                e.getPlayer().kickPlayer("You have been banned for: " + ban.getReason() +
                        "\nYou got banned at: " + ban.getBannedDate() +
                        "\nThe ban expires at: " + ban.getReleaseDate());
            } else {
                plugin.getDataManager().removeBan(plugin.getDataManager().retrieveCurrentBannedPlayerInformation(e.getPlayer().getName()));
            }
        } else if (e.getPlayer().hasPermission("Moderation.viewItems")) {
            plugin.getCachedData().addAvailableStaff(e.getPlayer().getUniqueId());
        }
    }

    private void giveRulesBooks(PlayerJoinEvent e) {
        if (!e.getPlayer().hasPlayedBefore()) {
            if (plugin.getConfig().getItemStack("RuleBook") != null) {
                e.getPlayer().getInventory().addItem(plugin.getConfig().getItemStack("RuleBook"));
            }
        }
    }

    private boolean canReleaseBan(String playerUsername) {
        return LocalDateTime.now().isAfter(plugin.getDataManager().retrieveCurrentBannedPlayerInformation(playerUsername).getReleaseDate());
    }
}
