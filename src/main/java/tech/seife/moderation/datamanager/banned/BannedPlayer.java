package tech.seife.moderation.datamanager.banned;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class BannedPlayer {

    private final int id;
    private final UUID bannedByUuid;
    private final UUID bannedUuid;
    private final String bannedPlayerName;
    private final String bannedByName;
    private final String reason;
    private final LocalDateTime bannedDate;
    private final LocalDateTime releaseDate;

    public BannedPlayer(int id,UUID bannedByUuid, UUID bannedUuid, String bannedPlayerName, String bannedByName, String reason, LocalDateTime bannedDate, LocalDateTime releaseDate) {
        this.id = id;
        this.bannedByUuid = Objects.requireNonNull(bannedByUuid);
        this.bannedUuid = Objects.requireNonNull(bannedUuid);
        this.bannedPlayerName = Objects.requireNonNull(bannedPlayerName);
        this.bannedByName = Objects.requireNonNull(bannedByName);
        this.reason = Objects.requireNonNull(reason);
        this.bannedDate = Objects.requireNonNull(bannedDate);
        this.releaseDate = Objects.requireNonNull(releaseDate);
    }

    public UUID getBannedUuid() {
        return bannedUuid;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getBannedDate() {
        return bannedDate;
    }

    public UUID getBannedByUuid() {
        return bannedByUuid;
    }

    public LocalDateTime getReleaseDate() {
        return releaseDate;
    }

    public String getBannedPlayerName() {
        return bannedPlayerName;
    }

    public String getBannedByName() {
        return bannedByName;
    }
}
