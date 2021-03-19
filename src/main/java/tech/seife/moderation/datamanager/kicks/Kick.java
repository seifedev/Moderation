package tech.seife.moderation.datamanager.kicks;

import java.time.LocalDateTime;
import java.util.UUID;

public class Kick {

    private final int id;
    private final UUID kickedPlayerUuid;
    private final UUID kickedByUuid;
    private final String kickedPlayerUsername;
    private final String kickedByUsername;
    private final String reason;
    private final LocalDateTime date;


    public Kick(int id, UUID kickedPlayerUuid, UUID kickedByUuid, String kickedPlayerUsername, String kickedByUsername, String reason, LocalDateTime date) {
        this.id = id;
        this.kickedPlayerUuid = kickedPlayerUuid;
        this.kickedByUuid = kickedByUuid;
        this.kickedPlayerUsername = kickedPlayerUsername;
        this.kickedByUsername = kickedByUsername;
        this.reason = reason;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public UUID getKickedPlayerUuid() {
        return kickedPlayerUuid;
    }

    public UUID getKickedByUuid() {
        return kickedByUuid;
    }

    public String getKickedPlayerUsername() {
        return kickedPlayerUsername;
    }

    public String getKickedByUsername() {
        return kickedByUsername;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getDate() {
        return date;
    }
}
