package tech.seife.moderation.datamanager.banned;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import tech.seife.moderation.Moderation;
import tech.seife.moderation.utils.MojangApiQuery;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class BannedPlayerManager {

    private final Moderation plugin;

    public BannedPlayerManager(Moderation plugin) {
        this.plugin = plugin;
    }

    public void addBannedPlayer(int id, UUID bannedByUuid, UUID bannedUuid, String bannedByPlayerName, String bannedByName, String reason, LocalDateTime bannedDate, LocalDateTime releaseDate) throws NullPointerException {
        if (bannedUuid == null) throw new NullPointerException("The uuid of the player to be banned is null");
        if (reason == null) throw new NullPointerException("There reason for the ban wasn't specified");
        if (releaseDate == null) throw new NullPointerException("The ban release date is null");

        BannedPlayer bannedPlayer = new BannedPlayer(id, bannedByUuid, bannedUuid, bannedByPlayerName, bannedByName, reason, bannedDate, releaseDate);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> plugin.getDataHandler().getDataManager().saveBan(bannedPlayer));
    }


    public ItemStack createBanBook(BannedPlayer bannedPlayer) {
        ItemStack banInformationBook = new ItemStack(Material.WRITTEN_BOOK);

        BookMeta meta = (BookMeta) banInformationBook.getItemMeta();

        meta.setTitle("Blank");
        meta.setAuthor(MojangApiQuery.getPlayerNameFromUuid(bannedPlayer.getBannedByUuid()));
        meta.setDisplayName("Banned player: " + MojangApiQuery.getPlayerNameFromUuid(bannedPlayer.getBannedUuid()));
        meta.setLore(Arrays.asList("Banned date: " + bannedPlayer.getBannedDate().toString(), "Ban release date: " + bannedPlayer.getReleaseDate().toString()));
        meta.setPages(Collections.singletonList(bannedPlayer.getReason()));

        banInformationBook.setItemMeta(meta);

        return banInformationBook;
    }


    public Set<BannedPlayer> getPlayersBanHistory(String playerUsername) {
        return plugin.getDataHandler().getDataManager().loadPlayerBanHistory(playerUsername);
    }
}
