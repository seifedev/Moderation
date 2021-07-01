package tech.seife.moderation.guis;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import tech.seife.moderation.Moderation;
import tech.seife.moderation.datamanager.banned.BannedPlayer;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class BansHistoryInventory   {

    private final Inventory banHistoryInventory;
    private final Moderation plugin;

    public BansHistoryInventory(Moderation plugin, Player inventoryHolder, String playerUsername, String ownerOfBanHistory) {
        this.plugin = plugin;
        banHistoryInventory = Bukkit.createInventory(inventoryHolder, 54, "Ban History Inventory Of: " + ownerOfBanHistory);
        createBanBookTickets(inventoryHolder.getUniqueId(), playerUsername);
    }

    public void openInventory(Player player) {
        player.openInventory(banHistoryInventory);
    }

    private void createBanBookTickets(UUID inventoryHolder, String playerName) {
        AtomicInteger slot = new AtomicInteger();
        ConcurrentMap<Integer, BannedPlayer> bannedSlots = new ConcurrentHashMap();
        if (plugin.getBannedPlayerManager().getPlayersBanHistory(playerName) != null) {
            plugin.getBannedPlayerManager().getPlayersBanHistory(playerName)
                    .forEach(banned -> {
                        ItemStack book = plugin.getBannedPlayerManager().createBanBook(banned);
                        addToInventory(book, slot.getAndIncrement());

                        bannedSlots.put(slot.get(), banned);
                        plugin.getCachedData().getBansInventorySlotBans().put(inventoryHolder, bannedSlots);
                    });
        }

    }


    private void addToInventory(ItemStack itemStack, int slot) {
        banHistoryInventory.setItem(slot, itemStack);
    }
}
