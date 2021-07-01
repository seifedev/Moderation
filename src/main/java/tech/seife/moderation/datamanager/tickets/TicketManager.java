package tech.seife.moderation.datamanager.tickets;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import tech.seife.moderation.datamanager.dao.DataManager;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class TicketManager {

    private final Set<Ticket> tickets;

    private final DataManager dataManager;

    public TicketManager(DataManager dataManager) {
        tickets = new HashSet<>();
        this.dataManager = dataManager;
    }

    public ItemStack transformTicketToBook(Ticket ticket) {
        if (ticket != null) {
            ItemStack book = new ItemStack(Material.WRITTEN_BOOK);

            BookMeta bookMeta = (BookMeta) book.getItemMeta();

            bookMeta.setAuthor(Objects.requireNonNull(Bukkit.getPlayer(ticket.getReporterUuid())).getName());
            bookMeta.setTitle(ticket.getSmallDescription() + " ticket id: #" + ticket.getId());
            bookMeta.setLore(Arrays.asList("Creation Date: " + ticket.getCreationDate()));
            bookMeta.setLore(Arrays.asList("Made by: " + Bukkit.getPlayer(ticket.getReporterUuid()).getName()));
            bookMeta.setPages(ticket.getDescription());
            bookMeta.setGeneration(BookMeta.Generation.ORIGINAL);

            book.setItemMeta(bookMeta);

            return book;
        }
        return null;
    }

    public Ticket transformBookToTicket(Player player, ItemStack itemStack) {
        if (itemStack.getType().equals(Material.WRITTEN_BOOK)) {

            BookMeta bookMeta = (BookMeta) itemStack.getItemMeta();

            if (bookMeta != null && bookMeta.getAuthor() != null) {
                return new Ticket(generateTicketId(), player.getUniqueId(), player.getName(), bookMeta.getTitle(), bookMeta.getPages().toString(), LocalDateTime.now());
            }

        }
        return null;
    }

    private int generateTicketId() {
        return dataManager.getLastTicketId() + 1;
    }
}
