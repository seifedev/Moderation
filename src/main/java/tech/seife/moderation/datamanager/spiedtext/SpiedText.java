package tech.seife.moderation.datamanager.spiedtext;

import java.time.LocalDateTime;
import java.util.UUID;

public class SpiedText {

    private final UUID senderUuid;
    private final String playerUsername;
    private final String text;
    private final LocalDateTime date;


    public SpiedText(UUID senderUuid, String playerUsername, String text, LocalDateTime date) {
        this.senderUuid = senderUuid;
        this.playerUsername = playerUsername;
        this.text = text;
        this.date = date;
    }

    public UUID getSenderUuid() {
        return senderUuid;
    }

    public String getPlayerUsername() {
        return playerUsername;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getDate() {
        return date;
    }
}
