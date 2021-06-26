package tech.seife.moderation.guis;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import tech.seife.moderation.Moderation;
import tech.seife.moderation.datamanager.tickets.Ticket;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TicketHistoryInventory {

    private final Moderation plugin;

    private final Inventory ticketHistoryInventory;

    public TicketHistoryInventory(Moderation plugin, Player inventoryHolder, String ownerOfTicketHistory) {
        this.plugin = plugin;
        ticketHistoryInventory = Bukkit.createInventory(inventoryHolder, 54, "Ticket History Inventory Of: " + ownerOfTicketHistory);
    }

    public void createTicketsBooks(UUID inventoryHolder, Set<Integer> ids, String reporter) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (ids != null && ids.size() <= 54) {
                ConcurrentHashMap idSlots = new ConcurrentHashMap();

                AtomicInteger slot = new AtomicInteger();
                ids.forEach(id -> {
                    if (id != null && plugin.getDataHandler().getDataManager().verifyTicketId(id, reporter)) {
                        Ticket ticket = plugin.getDataHandler().getDataManager().retrieveTicket(id, reporter);

                        if (ticket != null) {
                            idSlots.put(slot.getAndIncrement(), id);

                                addTicketsToInventory(getBookTitle(ticket), ticket.getDescription(), reporter, slot.get());

                        }
                    }
                    plugin.getCachedData().getTicketInventorySlotId().put(inventoryHolder, idSlots);
                });
            }
        });
    }

    public void openInventory(Player player) {
        player.openInventory(ticketHistoryInventory);
    }


    private String getBookTitle(Ticket ticket) {
        return ticket.getSmallDescription() + " ticket id: #" + ticket.getId();
    }

    private void addTicketsToInventory(String title, String lore, String author, int slot) {
        ItemStack ticket = new ItemStack(Material.WRITTEN_BOOK);

        ticket.setItemMeta(getDefaultPunishmentBookMeta(title, Collections.singletonList(lore), author, ticket));

        ticketHistoryInventory.setItem(slot, ticket);
    }


    private BookMeta getDefaultPunishmentBookMeta(String title, List<String> lore, String author, ItemStack itemStack) {
        BookMeta bookMeta = (BookMeta) itemStack.getItemMeta();

        if (bookMeta != null) {
            bookMeta.setDisplayName(title);

            bookMeta.setLore(lore);

            bookMeta.setAuthor(author);

        }

        return bookMeta;
    }
}
