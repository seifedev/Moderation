package tech.seife.moderation.datamanager.dao;

import tech.seife.moderation.datamanager.banned.BannedPlayer;
import org.bukkit.inventory.ItemStack;
import tech.seife.moderation.utils.MessageManager;

import java.security.PublicKey;
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

    //    I know this method goes against encapsulation, but I can't think of something better. to be fixed.
    public ConcurrentMap<UUID, ConcurrentMap<Integer, BannedPlayer>> getBansInventorySlotBans() {
        return bansInventorySlotBans;
    }

    //    I know this method goes against encapsulation, but I can't think of something better. to be fixed.
    public ConcurrentMap<UUID, ConcurrentMap<Integer, Integer>> getTicketInventorySlotId() {
        return ticketInventorySlotId;
    }

    public Set<UUID> getSpyMembers() {
        return Set.copyOf(spyMembers);
    }

    public void addSpyMember(UUID playerUuid) {
        spyMembers.add(playerUuid);
    }

    public void removeSpyMember(UUID playerUuid) {
        spyMembers.remove(playerUuid);
    }

    public Set<UUID> getOnVanishModePlayers() {
        return Set.copyOf(onVanishModePlayers);
    }

    public void addVanishModePlayers(UUID playerUuid) {
        onVanishModePlayers.add(playerUuid);
    }

    public void removeVanishModePlayers(UUID playerUuid) {
        spyMembers.remove(playerUuid);
    }


    public Set<UUID> getAvailableStaff() {
        return Set.copyOf(availableStaff);
    }

    public void addAvailableStaff(UUID playerUuid) {
        availableStaff.add(playerUuid);
    }

    public void removeAvailableStaff(UUID playerUuid) {
        availableStaff.remove(playerUuid);
    }


    public Map<UUID, ItemStack> getBooks() {
        return Map.copyOf(books);
    }

    public void addBook(UUID playerUuid, ItemStack itemStack) {
        books.put(playerUuid, itemStack);
    }

    public void removeBooks(UUID playerUuid) {
        books.remove(playerUuid);
    }
}
