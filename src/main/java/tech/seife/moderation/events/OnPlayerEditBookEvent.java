package tech.seife.moderation.events;

import tech.seife.moderation.datamanager.dao.CachedData;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.UUID;

public class OnPlayerEditBookEvent implements Listener {

    private final CachedData cachedData;

    public OnPlayerEditBookEvent(CachedData cachedData) {
        this.cachedData = cachedData;
    }

    @EventHandler
    public void onPlayerEditBookEvent(PlayerEditBookEvent e) {
        if (cachedData.getAvailableStaff().isEmpty()) return;

        BookMeta bookMeta = e.getNewBookMeta();

        ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
        book.setItemMeta(bookMeta);

        UUID randomUuid = UUID.randomUUID();
        cachedData.getBooks().put(randomUuid, book);

        cachedData.getAvailableStaff()
                .forEach(uuid -> {
                    TextComponent message = new TextComponent("Player: " + e.getPlayer().getName() + " created a book, click me to view the contents");
                    message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/readBook " + randomUuid));
                    Bukkit.getPlayer(uuid).spigot().sendMessage(message);
                });
    }
}
