package tech.seife.moderation.datamanager.dao;

import tech.seife.moderation.datamanager.banned.BannedPlayer;
import org.bukkit.inventory.ItemStack;
import tech.seife.moderation.utils.MessageManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CachedData {

    private final Set<UUID> availableStaff;
    private final HashMap<UUID, ItemStack> books;
    private final Set<UUID> onVanishModePlayers;
    private final Set<UUID> spyMembers;
    private final ConcurrentMap<UUID, ConcurrentMap<Integer, Integer>> ticketInventorySlotId;
    private final ConcurrentMap<UUID, ConcurrentMap<Integer, BannedPlayer>> bansInventorySlotBans;

    public CachedData() {
        onVanishModePlayers = new HashSet<>();
        availableStaff = new HashSet<>();
        books = new HashMap<>();
        spyMembers = new HashSet<>();
        ticketInventorySlotId = new ConcurrentHashMap<>();
        bansInventorySlotBans = new ConcurrentHashMap<>();
    }

    public ConcurrentMap<UUID, ConcurrentMap<Integer, BannedPlayer>> getBansInventorySlotBans() {
        return bansInventorySlotBans;
    }

    public ConcurrentMap<UUID, ConcurrentMap<Integer, Integer>> getTicketInventorySlotId() {
        return ticketInventorySlotId;
    }

    public Set<UUID> getSpyMembers() {
        return spyMembers;
    }

    public Set<UUID> getOnVanishModePlayers() {
        return onVanishModePlayers;
    }

    public Set<UUID> getAvailableStaff() {
        return availableStaff;
    }

    public HashMap<UUID, ItemStack> getBooks() {
        return books;
    }

}
