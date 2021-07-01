package tech.seife.moderation.datamanager.mutes;

import java.time.LocalDateTime;
import java.util.UUID;

public class MutedPlayer {

    private final int id;
    private final UUID mutedByUuid;
    private final UUID mutedPlayerUuid;
    private final String mutedByUsername;
    private final String mutedPlayerUsername;
    private final String channelName;
    private final LocalDateTime mutedDate;
    private final LocalDateTime releaseDate;


    public MutedPlayer(int id, UUID mutedByUuid, UUID mutedPlayerUuid, String mutedByUsername, String mutedPlayerUsername, String channelName, LocalDateTime mutedDate, LocalDateTime releaseDate) {
        this.id = id;
        this.mutedByUuid = mutedByUuid;
        this.mutedPlayerUuid = mutedPlayerUuid;
        this.mutedByUsername = mutedByUsername;
        this.mutedPlayerUsername = mutedPlayerUsername;
        this.channelName = channelName;
        this.mutedDate = mutedDate;
        this.releaseDate = releaseDate;
    }

    public int getId() {
        return id;
    }

    public UUID getMutedByUuid() {
        return mutedByUuid;
    }

    public UUID getMutedPlayerUuid() {
        return mutedPlayerUuid;
    }

    public String getMutedByUsername() {
        return mutedByUsername;
    }

    public String getChannelName() {
        return channelName;
    }

    public LocalDateTime getMutedDate() {
        return mutedDate;
    }

    public String getMutedPlayerUsername() {
        return mutedPlayerUsername;
    }

    public LocalDateTime getReleaseDate() {
        return releaseDate;
    }
}
