package tech.seife.moderation.events;

import tech.seife.moderation.Moderation;
import tech.seife.moderation.datamanager.banned.BannedPlayer;
import tech.seife.moderation.datamanager.tickets.Ticket;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class OnInventoryClickEvent implements Listener {

    private final Moderation plugin;

    public OnInventoryClickEvent(Moderation plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent e) {
        if (isTicketInventory(e.getView().getTitle())) {
            e.setCancelled(true);
            giveTicketToPlayer((Player) e.getWhoClicked(), e.getSlot());
        } else if (isBanInventory(e.getView().getTitle())) {
            e.setCancelled(true);
            giveBanBookToPlayer((Player) e.getWhoClicked(), e.getSlot());
        }
    }

    private void giveBanBookToPlayer(Player whoClicked, int banId) {
        if (plugin.getCachedData().getBansInventorySlotBans().get(whoClicked.getUniqueId()) != null) {

            BannedPlayer bannedPlayer = plugin.getCachedData().getBansInventorySlotBans().get(whoClicked.getUniqueId()).get(banId);

            if (bannedPlayer != null) {
                ItemStack itemStack = plugin.getBannedPlayerManager().createBanBook(bannedPlayer);
                whoClicked.getInventory().addItem(itemStack);
            }
        }
    }

    private boolean isBanInventory(String inventoryTitle) {
        return inventoryTitle.startsWith("Ban History Inventory Of: ");
    }

    private boolean isTicketInventory(String inventoryTitle) {
        return inventoryTitle.startsWith("Ticket History Inventory Of: ");
    }

    private void giveTicketToPlayer(Player whoClicked, int ticketId) {
        if (plugin.getCachedData().getTicketInventorySlotId().get(whoClicked.getUniqueId()) != null) {
            Ticket ticket = plugin.getDataManager().retrieveTicket(ticketId, whoClicked.getName());

            if (plugin.getTicketManager().transformTicketToBook(ticket) != null) {
                whoClicked.getInventory().addItem(plugin.getTicketManager().transformTicketToBook(ticket));
            }
        }
    }
}
